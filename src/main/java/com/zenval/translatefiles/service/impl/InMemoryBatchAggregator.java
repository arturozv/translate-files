package com.zenval.translatefiles.service.impl;

import com.zenval.translatefiles.dto.BatchGroup;
import com.zenval.translatefiles.dto.Translation;
import com.zenval.translatefiles.service.BatchAggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.summingLong;
import static java.util.stream.Collectors.toList;

@Component
public class InMemoryBatchAggregator implements BatchAggregator {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryBatchAggregator.class);

    private Map<String, Long> lineCountByFile = new HashMap<>();
    private Map<Long, Long> expectedTranslationsByLineNumber = new HashMap<>();
    private Map<Long, BatchGroup> wordsByLineNumber = new HashMap<>();
    private AtomicLong processedLineNumber = new AtomicLong(1);
    private AtomicLong minLineNumber;

    private Callback callback;

    @Override
    public synchronized void aggregate(Translation... translations) {
        Map<Long, List<Translation>> wordsByLineNumber = groupByLine(Arrays.asList(translations));
        addToMainMap(wordsByLineNumber);
    }

    @Override
    public void registerCallback(Callback callback) {
        this.callback = callback;
    }

    public void registerFileLength(String fileId, Long lineCount) {
        lineCountByFile.put(fileId, lineCount);
        if (minLineNumber == null) {
            minLineNumber = new AtomicLong(lineCount);
        }
        minLineNumber.set(Math.min(minLineNumber.get(), lineCount));
        logger.info("file {} registered", fileId);
    }

    @Override
    public long getTotalLines() {
        return lineCountByFile.values().stream().collect(summingLong(Long::longValue));
    }

    Map<Long, List<Translation>> groupByLine(List<Translation> toProcess) {
        return toProcess.stream().collect(groupingBy(Translation::getLine, mapping(Function.identity(), toList())));
    }

    void addToMainMap(Map<Long, List<Translation>> from) {

        for (Map.Entry<Long, List<Translation>> entry : from.entrySet()) {
            Long lineNumber = entry.getKey();
            List<Translation> words = entry.getValue();

            BatchGroup batchGroup = this.wordsByLineNumber.get(lineNumber);

            if (batchGroup == null) {
                Long expectedWordCount = getExpectedWordCount(lineNumber);
                batchGroup = new BatchGroup(lineNumber, expectedWordCount);
            }

            batchGroup.getTranslations().addAll(words);

            logger.debug("Checking line {}. Expected: {}, current: {}", lineNumber, batchGroup.getExpectedWordCount(), batchGroup.getTranslations().size());

            if (batchGroup.getTranslations().size() == batchGroup.getExpectedWordCount()) {
                writeBatch(lineNumber, batchGroup);
            } else {
                this.wordsByLineNumber.put(lineNumber, batchGroup);
            }
        }
    }

    void writeBatch(Long lineNumber, BatchGroup batchGroup) {
        logger.debug("line {} complete!", lineNumber);
        if (processedLineNumber.get() >= lineNumber) {


            this.wordsByLineNumber.remove(lineNumber);
            callback.onLineComplete(batchGroup);
        }


    }

    Long getExpectedWordCount(long lineNumber) {
        Long expected = expectedTranslationsByLineNumber.get(lineNumber);

        if (expected == null) {
            if (lineNumber <= minLineNumber.get()) {
                expected = (long) lineCountByFile.size();
            } else {
                List<String> files = lineCountByFile.entrySet().stream().filter(fileLineCount -> fileLineCount.getValue() >= lineNumber).map(Map.Entry::getKey).collect(Collectors.toList());
                logger.debug("getExpectedWordCount -> lineNumber: {}, files: {}, lineCountByFile: {}", lineNumber, files, lineCountByFile);
                expected = (long) files.size();
            }
            expectedTranslationsByLineNumber.put(lineNumber, expected);
        }
        return expected;
    }


    /**
     * GETTERS FOR TESTS
     **/
    Map<String, Long> getLineCountByFile() {
        return lineCountByFile;
    }
    Map<Long, Long> getExpectedTranslationsByLineNumber() {
        return expectedTranslationsByLineNumber;
    }
    Map<Long, BatchGroup> getWordsByLineNumber() {
        return wordsByLineNumber;
    }
    AtomicLong getProcessedLineNumber() {
        return processedLineNumber;
    }
    AtomicLong getMinLineNumber() {
        return minLineNumber;
    }
}
