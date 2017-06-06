package com.zenval.translatefiles.job;

import com.zenval.translatefiles.service.TranslateService;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TranslateProcessor implements ItemProcessor<TextAndLine, String> {

    private TranslateService translateService;

    @Autowired
    public TranslateProcessor(TranslateService translateService) {
        this.translateService = translateService;
    }

    @Override
    public String process(TextAndLine item) throws Exception {
        return translateService.translate(item.getText(), "en", "sv");
    }
}
