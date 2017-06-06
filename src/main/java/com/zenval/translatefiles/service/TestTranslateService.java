package com.zenval.translatefiles.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestTranslateService implements TranslateService {
    @Override
    public String translate(String text, String fromLanguage, String toLanguage) {
        return text;
    }
}
