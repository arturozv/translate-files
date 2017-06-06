package com.zenval.translatefiles.service.impl;

import com.zenval.translatefiles.service.TranslateService;

public class TestTranslateService implements TranslateService {
    @Override
    public String translate(String text, String fromLanguage, String toLanguage) {
        return text;
    }
}
