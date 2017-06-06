package com.zenval.translatefiles.job;

import com.zenval.translatefiles.file.Files;
import com.zenval.translatefiles.job.components.FilePartitioner;
import com.zenval.translatefiles.service.BatchAggregator;

import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FilePartitionerTest {

    @Test
    public void when_null_partitionMap_isEmpty() {
        //given
        FilePartitioner filePartitioner = new FilePartitioner(null, mock(BatchAggregator.class));

        //when
        Map<String, ExecutionContext> partitionMap = filePartitioner.partition(0);

        //then
        assertThat(partitionMap).isEmpty();
    }


    @Test
    public void when_no_files_partitionMap_isEmpty() {
        //given
        Files files = mock(Files.class);
        when(files.getPaths()).thenReturn(new ArrayList<>());
        BatchAggregator batchAggregator = mock(BatchAggregator.class);
        FilePartitioner filePartitioner = new FilePartitioner(files, batchAggregator);

        //when
        Map<String, ExecutionContext> partitionMap = filePartitioner.partition(0);

        //then
        assertThat(partitionMap).isEmpty();
        verify(batchAggregator, times(0)).registerFileLength(anyString(), anyLong());
    }

    @Test
    public void when_files_partitionMap_isOk() {
        //given
        String fileName = "file1";
        Files files = mock(Files.class);
        when(files.getPaths()).thenReturn(Collections.singletonList(fileName));
        BatchAggregator batchAggregator = mock(BatchAggregator.class);
        FilePartitioner filePartitioner = new FilePartitioner(files, batchAggregator);

        //when
        Map<String, ExecutionContext> partitionMap = filePartitioner.partition(0);

        //then
        assertThat(partitionMap).isNotEmpty().hasSize(1);
        assertThat(partitionMap).containsKeys(fileName);
        assertThat(partitionMap.get(fileName).containsKey(FilePartitioner.FILE_KEY));
        assertThat(partitionMap.get(fileName).containsKey(FilePartitioner.LINE_COUNT_KEY));
        assertThat(partitionMap.get(fileName).containsKey(FilePartitioner.FILE_ID_KEY));

        verify(batchAggregator, times(1)).registerFileLength(anyString(), anyLong());
    }
}