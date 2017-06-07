package com.zenval.translatefiles.job.components;

import com.zenval.translatefiles.dto.BatchGroup;
import com.zenval.translatefiles.dto.Translation;
import com.zenval.translatefiles.service.BatchAggregator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class BatchFileWriter implements ItemWriter<Translation> {
    private static final Logger logger = LoggerFactory.getLogger(BatchFileWriter.class);

    private BatchAggregator batchAggregator;

    @Autowired
    public BatchFileWriter(BatchAggregator batchAggregator) {
        this.batchAggregator = batchAggregator;
        this.batchAggregator.registerCallback(this::writeBatch);
    }

    @Override
    public void write(List<? extends Translation> items) throws Exception {
        logger.info("aggregating {} words.", items.size());
        batchAggregator.aggregate(items.toArray(new Translation[items.size()]));
    }

    void writeBatch(BatchGroup batchGroup) {
        logger.info("writing batch {}", batchGroup);
    }

    /*@AfterStep
    public void waitUntilDone() throws InterruptedException {
        while (!batchAggregator.isCompleted()) {
            Thread.sleep(100);
        }
    }*/
}
