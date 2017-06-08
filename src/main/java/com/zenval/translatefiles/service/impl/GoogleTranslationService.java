package com.zenval.translatefiles.service.impl;

import com.eclipsesource.json.Json;
import com.zenval.translatefiles.service.TranslationException;
import com.zenval.translatefiles.service.TranslationService;

import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class GoogleTranslationService implements TranslationService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleTranslationService.class);
    private final static String URL = "https://translate.googleapis.com/translate_a/single?client=gtx&dt=t";

    @Override
    public String translate(String text, String fromLanguage, String toLanguage) throws TranslationException {
        String result;

        try {
            URI uri = new URIBuilder(URL)
                    .addParameter("sl", fromLanguage)
                    .addParameter("tl", toLanguage)
                    .addParameter("q", text)
                    .build();

            String responseAsJson = Request.Get(uri)
                    .execute()
                    .returnContent()
                    .asString();

            result = Json.parse(responseAsJson).asArray().get(0).asArray().get(0).asArray().get(0).asString();

            logger.debug("{} -> {}", text, result);
        } catch (Exception e) {
            logger.error("error translating from {} to {} -> [{}]: {} ", fromLanguage, toLanguage, text, e.getMessage(), e);
            throw new TranslationException(e.getMessage(), false);
        }

        return result;
    }
}
