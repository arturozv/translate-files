package com.zenval.translatefiles.job.components;

import com.zenval.translatefiles.service.TranslationException;
import com.zenval.translatefiles.service.TranslationService;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by arturo on 06/06/17.
 */
public class TranslateProcessorTest {

    @Test
    public void when_isOk() throws Exception {
        //given
        String textToTranslate = "textToTranslate";
        String translated = "translated";
        TranslationService translationService = mock(TranslationService.class);

        when(translationService.translate(eq(textToTranslate), anyString(), anyString())).thenReturn(translated);

        TranslateProcessor translateProcessor = new TranslateProcessor(translationService);

        //when
        String result = translateProcessor.process(textToTranslate);

        //then
        assertThat(result).isEqualTo(translated);

        verify(translationService, times(1)).translate(eq(textToTranslate), anyString(), anyString());
    }

    @Test
    public void when_exception_no_shutdown_translated_equals_text() throws Exception {
        //given
        String textToTranslate = "textToTranslate";
        TranslationService translationService = mock(TranslationService.class);

        when(translationService.translate(eq(textToTranslate), anyString(), anyString())).thenThrow(new TranslationException("test", false));

        TranslateProcessor translateProcessor = new TranslateProcessor(translationService);

        //when
        String result = translateProcessor.process(textToTranslate);

        //then
        assertThat(result).isEqualTo(textToTranslate); //same as original text

        verify(translationService, times(1)).translate(eq(textToTranslate), anyString(), anyString());
    }

    @Test
    public void when_exception_and_shutdown() throws Exception {
        //given
        String textToTranslate = "textToTranslate";
        TranslationService translationService = mock(TranslationService.class);

        when(translationService.translate(eq(textToTranslate), anyString(), anyString())).thenThrow(new TranslationException("test", true));

        TranslateProcessor translateProcessor = new TranslateProcessor(translationService);

        //when

        assertThatThrownBy(() -> {
            translateProcessor.process(textToTranslate);

        }).isInstanceOf(TranslationException.class)
                .hasFieldOrPropertyWithValue("shutdown", true);

        //then
        verify(translationService, times(1)).translate(eq(textToTranslate), anyString(), anyString());
    }
}