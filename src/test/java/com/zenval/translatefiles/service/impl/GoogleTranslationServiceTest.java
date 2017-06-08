package com.zenval.translatefiles.service.impl;

import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class GoogleTranslationServiceTest {

    private GoogleTranslationService googleTranslationService = new GoogleTranslationService();

    @Test
    public void test() throws Exception {
        String result = googleTranslationService.translate("hello world", "en", "es");
        assertThat(result).isEqualToIgnoringCase("hola mundo");
    }

    @Test
    public void test_weird_characters() throws Exception {
        String result = googleTranslationService.translate("✌", "en", "es");
        assertThat(result).isNotNull();
    }


    @Ignore
    @Test
    public void test_rate_limit() throws Exception {
        int threads = 4;
        ExecutorService executorService = Executors.newFixedThreadPool(threads);

        IntStream.rangeClosed(1, threads).forEach(t -> executorService.submit(() -> IntStream.range(1, 1000).forEach(i -> {
            googleTranslationService.translate("hello world " + t + "-" + i, "en", "es");
        })));

        executorService.awaitTermination(10, TimeUnit.MINUTES);
    }

}