package com.zenval.translatefiles.job.components;

import com.zenval.translatefiles.service.TranslationException;
import com.zenval.translatefiles.service.TranslationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class TranslateProcessor implements ItemProcessor<String, String> {
    private static final Logger logger = LoggerFactory.getLogger(TranslateProcessor.class);

    private TranslationService translationService;

    @Autowired
    public TranslateProcessor(TranslationService translationService) {
        this.translationService = translationService;
    }

    @Override
    public String process(String item) throws Exception {
        String translated;

        try {
            translated = translationService.translate(item, "en", "sv");

        } catch (TranslationException e) {
            logger.error("Error translating {} for file {}: {}", item, e.getMessage(), e);
            if (e.shouldShutdown()) {
                throw e;
            } else {
                translated = item;
            }
        }

        return translated;
    }
}
