package com.zenval.translatefiles.job.components;

import com.zenval.translatefiles.dto.BatchGroup;
import com.zenval.translatefiles.dto.Translation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

public class BatchFileWriter implements ItemWriter<Translation> {
    private static final Logger logger = LoggerFactory.getLogger(BatchFileWriter.class);

    private Map<String, Long> lineCountByFile = new HashMap<>();
    private Map<Long, BatchGroup> wordsByLineNumber = new HashMap<>();
    private AtomicLong minLineNumber;

    @Override
    public void write(List<? extends Translation> items) throws Exception {
        logger.info("aggregating {} words.", items.size());
        @SuppressWarnings("unchecked") Map<Long, List<Translation>> wordsByLineNumber = groupByLine((List<Translation>) items);
        addToMainMap(wordsByLineNumber, this.wordsByLineNumber);
    }

    /**
     * Register a file and it's line count to be able to generate the batches by line
     *
     * @param fileId    file id (path)
     * @param lineCount number of lines of the file
     */
    public void registerFileLength(String fileId, Long lineCount) {
        lineCountByFile.put(fileId, lineCount);
        if (minLineNumber == null) {
            minLineNumber = new AtomicLong(lineCount);
        }
        minLineNumber.set(Math.min(minLineNumber.get(), lineCount));
        logger.info("file {} registered", fileId);
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
                writeBatch(batchGroup.getTranslations());
                to.remove(lineNumber);
            } else {
                to.put(lineNumber, batchGroup);
            }
        }
    }

    void writeBatch(List<Translation> translations) {

    }

    long getExpectedWordCount(long lineNumber) {
        if (lineNumber <= minLineNumber.get()) {
            return lineCountByFile.size();
        }
        return lineCountByFile.entrySet().stream().filter(a -> a.getValue() <= lineNumber).count();
    }
}
