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

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Component
public class InMemoryBatchAggregator implements BatchAggregator {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryBatchAggregator.class);

    private Map<String, Long> lineCountByFile = new HashMap<>();
    private Map<Long, BatchGroup> wordsByLineNumber = new HashMap<>();
    private AtomicLong minLineNumber;

    private Callback callback;

    @Override
    public void aggregate(Translation... translations) {
        Map<Long, List<Translation>> wordsByLineNumber = groupByLine(Arrays.asList(translations));
        addToMainMap(wordsByLineNumber, this.wordsByLineNumber);
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
    public boolean isCompleted() {
        return wordsByLineNumber.isEmpty();
    }

    Map<Long, List<Translation>> groupByLine(List<Translation> toProcess) {
        return toProcess.stream().collect(groupingBy(Translation::getLine, mapping(Function.identity(), toList())));
    }

    void addToMainMap(Map<Long, List<Translation>> from, Map<Long, BatchGroup> to) {

        for (Map.Entry<Long, List<Translation>> entry : from.entrySet()) {
            Long lineNumber = entry.getKey();
            List<Translation> words = entry.getValue();

            BatchGroup batchGroup = to.get(lineNumber);

            if (batchGroup == null) {
                Long expectedWordCount = getExpectedWordCount(lineNumber);
                batchGroup = new BatchGroup(lineNumber, expectedWordCount);
            }

            batchGroup.getTranslations().addAll(words);

            logger.info("Checking line {}. Expected: {}, current: {}", lineNumber, batchGroup.getExpectedWordCount(), batchGroup.getTranslations().size());

            if (batchGroup.getTranslations().size() == batchGroup.getExpectedWordCount()) {
                logger.info("line {} complete!", lineNumber);
                writeBatch(batchGroup);
                to.remove(lineNumber);
            } else {
                to.put(lineNumber, batchGroup);
            }
        }
    }

    void writeBatch(BatchGroup batchGroup) {
        callback.onLineComplete(batchGroup);
    }

    long getExpectedWordCount(long lineNumber) {
        if (lineNumber <= minLineNumber.get()) {
            return lineCountByFile.size();
        }
        return lineCountByFile.entrySet().stream().filter(a -> a.getValue() <= lineNumber).count();
    }
}
