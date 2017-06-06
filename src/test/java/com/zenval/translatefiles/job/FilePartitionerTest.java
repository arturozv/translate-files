package com.zenval.translatefiles.job;

import com.zenval.translatefiles.file.Files;

import org.junit.Test;
import org.springframework.batch.item.ExecutionContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FilePartitionerTest {

    @Test
    public void when_null_partitionMap_isEmpty() {

        FilePartitioner filePartitioner = new FilePartitioner(null);

        Map<String, ExecutionContext> partitionMap = filePartitioner.partition(0);

        assertThat(partitionMap).isEmpty();
    }


    @Test
    public void when_no_files_partitionMap_isEmpty() {
        Files files = mock(Files.class);
        when(files.getPaths()).thenReturn(new ArrayList<>());

        FilePartitioner filePartitioner = new FilePartitioner(files);

        Map<String, ExecutionContext> partitionMap = filePartitioner.partition(0);

        assertThat(partitionMap).isEmpty();
    }

    @Test
    public void when_files_partitionMap_isOk() {
        String fileName = "file1";
        Files files = mock(Files.class);
        when(files.getPaths()).thenReturn(Collections.singletonList(fileName));

        FilePartitioner filePartitioner = new FilePartitioner(files);

        Map<String, ExecutionContext> partitionMap = filePartitioner.partition(0);

        assertThat(partitionMap).isNotEmpty().hasSize(1);
        assertThat(partitionMap).containsKeys(fileName);
        assertThat(partitionMap.get(fileName).containsKey(FilePartitioner.FILE_KEY));
        assertThat(partitionMap.get(fileName).containsKey(FilePartitioner.LINE_COUNT_KEY));
    }
}