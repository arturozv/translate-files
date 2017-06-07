package com.zenval.translatefiles.job.components;

import com.zenval.translatefiles.dto.TextAndLine;
import com.zenval.translatefiles.dto.Translation;
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
        String fileName = "file1";
        String textToTranslate = "textToTranslate";
        String translated = "translated";
        long line = 1l;
        TranslationService translationService = mock(TranslationService.class);

        when(translationService.translate(eq(textToTranslate), anyString(), anyString())).thenReturn(translated);

        TranslateProcessor translateProcessor = new TranslateProcessor(fileName, translationService);

        //when
        Translation result = translateProcessor.process(new TextAndLine(textToTranslate, line));

        //then
        assertThat(result.getText()).isEqualTo(textToTranslate);
        assertThat(result.getTranslated()).isEqualTo(translated);
        assertThat(result.getLine()).isEqualTo(line);
        assertThat(result.getFileId()).isEqualTo(fileName);

        verify(translationService, times(1)).translate(eq(textToTranslate), anyString(), anyString());
    }

    @Test
    public void when_exception_no_shutdown_translated_equals_text() throws Exception {
        //given
        String fileName = "file1";
        String textToTranslate = "textToTranslate";
        TranslationService translationService = mock(TranslationService.class);

        when(translationService.translate(eq(textToTranslate), anyString(), anyString())).thenThrow(new TranslationException("test", false));

        TranslateProcessor translateProcessor = new TranslateProcessor(fileName, translationService);

        //when
        Translation result = translateProcessor.process(new TextAndLine(textToTranslate, 1));

        //then
        assertThat(result.getTranslated()).isEqualTo(textToTranslate); //same as original text

        verify(translationService, times(1)).translate(eq(textToTranslate), anyString(), anyString());
    }

    @Test
    public void when_exception_and_shutdown() throws Exception {
        //given
        String fileName = "file1";
        String textToTranslate = "textToTranslate";
        TranslationService translationService = mock(TranslationService.class);

        when(translationService.translate(eq(textToTranslate), anyString(), anyString())).thenThrow(new TranslationException("test", true));

        TranslateProcessor translateProcessor = new TranslateProcessor(fileName, translationService);

        //when

        assertThatThrownBy(() -> {
            translateProcessor.process(new TextAndLine(textToTranslate, 1));

        }).isInstanceOf(TranslationException.class)
                .hasFieldOrPropertyWithValue("shutdown", true);

        //then
        verify(translationService, times(1)).translate(eq(textToTranslate), anyString(), anyString());
    }
}