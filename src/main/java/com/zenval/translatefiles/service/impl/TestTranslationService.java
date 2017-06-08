package com.zenval.translatefiles.service.impl;

import com.zenval.translatefiles.service.TranslationService;

public class TestTranslationService implements TranslationService {
    @Override
    public String translate(String text, String fromLanguage, String toLanguage) {
        return text + "-translated";
    }
}
