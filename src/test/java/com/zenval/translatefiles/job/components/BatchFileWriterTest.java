package com.zenval.translatefiles.job.components;

import com.zenval.translatefiles.dto.BatchGroup;
import com.zenval.translatefiles.dto.Translation;
import com.zenval.translatefiles.service.BatchAggregator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.item.ItemWriter;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class BatchFileWriterTest {

    @Mock
    private ItemWriter<String> itemWriter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void writer_calls_aggregator() throws Exception {
        //given
        Translation translation = new Translation("text", "translated", 1l, "file1");
        BatchAggregator batchAggregator = mock(BatchAggregator.class);
        BatchFileWriter batchFileWriter = new BatchFileWriter(batchAggregator, itemWriter);

        //when
        batchFileWriter.write(Collections.singletonList(translation));

        //then
        verify(batchAggregator, times(1)).aggregate(eq(translation));
    }

    @Test
    public void write_batch_calls_writer_sorted() throws Exception {
        //given
        BatchAggregator batchAggregator = mock(BatchAggregator.class);
        BatchGroup batchGroup = new BatchGroup(1l, 1l);
        batchGroup.getTranslations().addAll(Arrays.asList(new Translation("text", "translatedB", 1l, "file1"), new Translation("text2", "translatedA", 1l, "file2")));
        BatchFileWriter batchFileWriter = new BatchFileWriter(batchAggregator, itemWriter);

        //when
        batchFileWriter.writeBatch(batchGroup);

        //then
        verify(itemWriter, times(1)).write(Arrays.asList("translatedA", "translatedB"));
    }

}