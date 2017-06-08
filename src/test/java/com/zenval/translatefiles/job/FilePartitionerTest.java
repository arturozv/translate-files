package com.zenval.translatefiles.job;

import com.zenval.translatefiles.file.Files;
import com.zenval.translatefiles.job.components.FilePartitioner;

import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FilePartitionerTest {

    @Test
    public void when_null_partitionMap_isEmpty() {
        //given
        FilePartitioner filePartitioner = new FilePartitioner(null);

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
        FilePartitioner filePartitioner = new FilePartitioner(files);

        //when
        Map<String, ExecutionContext> partitionMap = filePartitioner.partition(0);

        //then
        assertThat(partitionMap).isEmpty();
    }

    @Test
    public void when_files_partitionMap_isOk() {
        //given
        String fileName = "file1";
        Files files = mock(Files.class);
        when(files.getPaths()).thenReturn(Collections.singletonList(fileName));
        FilePartitioner filePartitioner = new FilePartitioner(files);

        //when
        Map<String, ExecutionContext> partitionMap = filePartitioner.partition(0);

        //then
        assertThat(partitionMap).isNotEmpty().hasSize(1);
        assertThat(partitionMap).containsKeys(fileName);
        assertThat(partitionMap.get(fileName).containsKey(FilePartitioner.FILE_ID_KEY));
    }
}