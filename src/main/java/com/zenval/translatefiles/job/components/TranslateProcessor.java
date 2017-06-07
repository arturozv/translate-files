package com.zenval.translatefiles.job.components;

import com.zenval.translatefiles.service.Translation;
import com.zenval.translatefiles.service.TranslationException;
import com.zenval.translatefiles.service.TranslationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

public class TranslateProcessor implements ItemProcessor<TextAndLine, Translation> {
    private static final Logger logger = LoggerFactory.getLogger(TranslateProcessor.class);

    private TranslationService translationService;

    private final String fileId;

    @Autowired
    public TranslateProcessor(String fileId, TranslationService translationService) {
        this.translationService = translationService;
        this.fileId = fileId;
    }

    @Override
    public Translation process(TextAndLine item) throws Exception {
        String translated;

        try {
            translated = translationService.translate(item.getText(), "en", "sv");

        } catch (TranslationException e) {
            logger.error("Error translating {} for file {}: {}", item, fileId, e.getMessage(), e);

            if (e.shouldShutdown()) {
                throw e;
            } else {
                translated = item.getText();
            }
        }

        return new Translation(item.getText(), translated, item.getLine(), fileId);
    }
}
