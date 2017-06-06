package com.zenval.translatefiles.job.components;

import com.zenval.translatefiles.file.FileValidator;
import com.zenval.translatefiles.file.Files;
import com.zenval.translatefiles.file.InvalidFileException;
import com.zenval.translatefiles.service.BatchAggregator;
import com.zenval.translatefiles.service.TranslateService;
import com.zenval.translatefiles.service.TranslationException;

import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
        TranslateService translateService = mock(TranslateService.class);
        BatchAggregator batchAggregator = mock(BatchAggregator.class);

        when(translateService.translate(eq(textToTranslate), anyString(), anyString())).thenReturn(translated);

        TranslateProcessor translateProcessor = new TranslateProcessor(fileName, translateService, batchAggregator);

        //when
        String result = translateProcessor.process(new TextAndLine(textToTranslate, 1));

        //then
        assertThat(result).isEqualTo(translated);

        verify(translateService, times(1)).translate(eq(textToTranslate), anyString(), anyString());
        verify(batchAggregator, times(1)).enqueue(eq(translated), eq(1l), eq(fileName));
    }

    @Test
    public void when_exception_no_shutdown() throws Exception {
        //given
        String fileName = "file1";
        String textToTranslate = "textToTranslate";
        TranslateService translateService = mock(TranslateService.class);
        BatchAggregator batchAggregator = mock(BatchAggregator.class);

        when(translateService.translate(eq(textToTranslate), anyString(), anyString())).thenThrow(new TranslationException("test", false));

        TranslateProcessor translateProcessor = new TranslateProcessor(fileName, translateService, batchAggregator);

        //when
        String result = translateProcessor.process(new TextAndLine(textToTranslate, 1));

        //then
        assertThat(result).isEqualTo(textToTranslate); //original text

        verify(translateService, times(1)).translate(eq(textToTranslate), anyString(), anyString());
        verify(batchAggregator, times(1)).enqueue(eq(textToTranslate), eq(1l), eq(fileName));
    }

    @Test
    public void when_exception_and_shutdown() throws Exception {
        //given
        String fileName = "file1";
        String textToTranslate = "textToTranslate";
        TranslateService translateService = mock(TranslateService.class);
        BatchAggregator batchAggregator = mock(BatchAggregator.class);

        when(translateService.translate(eq(textToTranslate), anyString(), anyString())).thenThrow(new TranslationException("test", true));

        TranslateProcessor translateProcessor = new TranslateProcessor(fileName, translateService, batchAggregator);

        //when

        assertThatThrownBy(() -> {
            String result = translateProcessor.process(new TextAndLine(textToTranslate, 1));

        }).isInstanceOf(TranslationException.class)
                .hasFieldOrPropertyWithValue("shutdown", true);

        //then
        verify(translateService, times(1)).translate(eq(textToTranslate), anyString(), anyString());
    }
}