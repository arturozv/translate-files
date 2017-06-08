package com.zenval.translatefiles.job.components;

import com.zenval.translatefiles.dto.BatchGroup;
import com.zenval.translatefiles.dto.Translation;
import com.zenval.translatefiles.service.BatchAggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class BatchFileWriter implements ItemWriter<Translation> {
    private static final Logger logger = LoggerFactory.getLogger(BatchFileWriter.class);

    private BatchAggregator batchAggregator;
    private ItemWriter<String> flatFileItemWriter;

    @Autowired
    public BatchFileWriter(BatchAggregator batchAggregator, ItemWriter<String> flatFileItemWriter) {
        this.batchAggregator = batchAggregator;
        this.batchAggregator.registerCallback(this::writeBatch);
        this.flatFileItemWriter = flatFileItemWriter;

    }

    @Override
    public void write(List<? extends Translation> items) throws Exception {
        logger.info("aggregating {} words.", items.size());
        batchAggregator.aggregate(items.toArray(new Translation[items.size()]));
    }

    /**
     * Write a batch of lines
     * @param batchGroup
     */
    void writeBatch(BatchGroup batchGroup) {
        logger.info("writing batch {}", batchGroup);

        List<String> words = batchGroup.getTranslations().stream()
                .map(Translation::getTranslated)
                .sorted()
                .collect(Collectors.toList());

        try {
            flatFileItemWriter.write(words);
        } catch (Exception e) {
            logger.error("error writing batch {}", batchGroup, e);
        }
    }
}
