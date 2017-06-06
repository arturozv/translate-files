package com.zenval.translatefiles.job.components;

import com.zenval.translatefiles.service.BatchAggregator;
import com.zenval.translatefiles.service.TranslateService;
import com.zenval.translatefiles.service.TranslationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class TranslateProcessor implements ItemProcessor<TextAndLine, String> {
    private static final Logger logger = LoggerFactory.getLogger(TranslateProcessor.class);

    private TranslateService translateService;
    private BatchAggregator batchAggregator;

    private final String fileId;

    @Autowired
    public TranslateProcessor(String fileId, TranslateService translateService, BatchAggregator batchAggregator) {
        this.translateService = translateService;
        this.fileId = fileId;
        this.batchAggregator = batchAggregator;
    }

    @Override
    public String process(TextAndLine item) throws Exception {
        String translated;

        try {
            translated = translateService.translate(item.getText(), "en", "sv");

        } catch (TranslationException e) {
            logger.error("Error translating {} for file {}: {}", item, fileId, e.getMessage(), e);
            if (e.shouldShutdown()) {
                throw e;
            } else {
                translated = item.getText();
            }
        }

        batchAggregator.enqueue(translated, item.getLine(), fileId);

        return translated;
    }
}
