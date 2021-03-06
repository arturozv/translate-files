package com.zenval.translatefiles.service;

public interface TranslationService {

    /**
     * Translate a text from a language to another
     *
     * @param text         text to be translated
     * @param fromLanguage origin language
     * @param toLanguage   destination language
     * @return translated text
     * @throws TranslationException if there's an error
     */
    String translate(String text, String fromLanguage, String toLanguage) throws TranslationException;

}
