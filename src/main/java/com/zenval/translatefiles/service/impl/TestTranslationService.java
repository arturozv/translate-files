package com.zenval.translatefiles.service.impl;

import com.zenval.translatefiles.service.TranslationService;

import java.util.Random;

public class TestTranslationService implements TranslationService {
    @Override
    public String translate(String text, String fromLanguage, String toLanguage) {
        return (char) (new Random().nextInt(26) + 'a') + "-" + text + "-translated";
    }
}
