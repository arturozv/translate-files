package com.zenval.translatefiles.service.impl;

import com.zenval.translatefiles.dto.BatchGroup;
import com.zenval.translatefiles.dto.Translation;
import com.zenval.translatefiles.service.BatchAggregator;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class InMemoryBatchAggregatorTest {

    @Test
    public void register_file() {
        //given
        InMemoryBatchAggregator batchAggregator = new InMemoryBatchAggregator();
        long min = 10l;
        long max = 20l;

        //when
        batchAggregator.registerFileLength("file1", min);
        batchAggregator.registerFileLength("file2", max);

        //then
        assertThat(batchAggregator.getLineCountByFile().get("file1")).isEqualTo(min);
        assertThat(batchAggregator.getLineCountByFile().get("file2")).isEqualTo(max);

        assertThat(batchAggregator.getTotalLines()).isEqualTo(min + max);
        assertThat(batchAggregator.getMinLineNumber().get()).isEqualTo(min);
    }


    @Test
    public void get_expected_word_count_by_line_single_file() {
        //given
        InMemoryBatchAggregator batchAggregator = new InMemoryBatchAggregator();

        //when
        batchAggregator.registerFileLength("file1", 2l);

        //then
        assertThat(batchAggregator.getExpectedWordCount(1l)).isEqualTo(1l);
        assertThat(batchAggregator.getExpectedWordCount(2l)).isEqualTo(1l);
    }

    @Test
    public void get_expected_word_count_by_line_multiple_files() {
        //given
        InMemoryBatchAggregator batchAggregator = new InMemoryBatchAggregator();

        //when
        batchAggregator.registerFileLength("file1", 1l);
        batchAggregator.registerFileLength("file2", 2l);
        batchAggregator.registerFileLength("file3", 3l);
        batchAggregator.registerFileLength("file4", 5l);

        //then
        assertThat(batchAggregator.getExpectedWordCount(1l)).isEqualTo(4l);
        assertThat(batchAggregator.getExpectedWordCount(2l)).isEqualTo(3l);
        assertThat(batchAggregator.getExpectedWordCount(3l)).isEqualTo(2l);
        assertThat(batchAggregator.getExpectedWordCount(4l)).isEqualTo(1l);
        assertThat(batchAggregator.getExpectedWordCount(4l)).isEqualTo(1l); //same to check cache
        assertThat(batchAggregator.getExpectedWordCount(5l)).isEqualTo(1l);
    }

    @Test
    public void group_words_by_line() {
        //given
        InMemoryBatchAggregator batchAggregator = new InMemoryBatchAggregator();

        Translation t1 = new Translation("text1", "translated1", 1l, "file1");
        Translation t2 = new Translation("text2", "translated2", 2l, "file1");
        Translation t3 = new Translation("text3", "translated2", 1l, "file2");

        //when
        Map<Long, List<Translation>> result = batchAggregator.groupByLine(Arrays.asList(t1, t2, t3));

        //then
        assertThat(result.get(1l).size()).isEqualTo(2);
        assertThat(result.get(1l)).contains(t1);
        assertThat(result.get(1l)).contains(t3);

        assertThat(result.get(2l).size()).isEqualTo(1);
        assertThat(result.get(2l)).contains(t2);
    }

    @Test
    public void batch_completed_single(){
        //given
        InMemoryBatchAggregator batchAggregator = new InMemoryBatchAggregator();
        BatchAggregator.Callback callback = mock(BatchAggregator.Callback.class);
        batchAggregator.registerCallback(callback);

        BatchGroup batchGroup = new BatchGroup(1l, 2l);
        batchGroup.getTranslations().add(new Translation("line1", "translated1", 1l, "file1"));
        batchGroup.getTranslations().add(new Translation("line1", "translated1", 1l, "file2"));

        //when
        batchAggregator.batchGroupCompleted(batchGroup);

        //then
        verify(callback, times(1)).onLineComplete(batchGroup);
    }

    @Test
    public void batch_completed_multiple_ordered(){
        //given
        InMemoryBatchAggregator batchAggregator = new InMemoryBatchAggregator();
        BatchAggregator.Callback callback = mock(BatchAggregator.Callback.class);
        batchAggregator.registerCallback(callback);

        BatchGroup batchGroup1 = new BatchGroup(1l, 2l);
        batchGroup1.getTranslations().add(new Translation("line1", "translated1", 1l, "file1"));
        batchGroup1.getTranslations().add(new Translation("line1", "translated1", 1l, "file2"));

        BatchGroup batchGroup2 = new BatchGroup(2l, 2l);
        batchGroup2.getTranslations().add(new Translation("line2", "translated2", 2l, "file1"));
        batchGroup2.getTranslations().add(new Translation("line2", "translated2", 2l, "file2"));

        //when
        batchAggregator.batchGroupCompleted(batchGroup1);
        //then
        verify(callback, times(1)).onLineComplete(batchGroup1);

        //when
        batchAggregator.batchGroupCompleted(batchGroup2);
        //then
        verify(callback, times(1)).onLineComplete(batchGroup2);
    }

    @Test
    public void batch_completed_multiple_unordered(){
        //given
        InMemoryBatchAggregator batchAggregator = new InMemoryBatchAggregator();
        BatchAggregator.Callback callback = mock(BatchAggregator.Callback.class);
        batchAggregator.registerCallback(callback);

        BatchGroup batchGroup1 = new BatchGroup(1l, 2l);
        batchGroup1.getTranslations().add(new Translation("line1", "translated1", 1l, "file1"));
        batchGroup1.getTranslations().add(new Translation("line1", "translated1", 1l, "file2"));

        BatchGroup batchGroup2 = new BatchGroup(2l, 2l);
        batchGroup2.getTranslations().add(new Translation("line2", "translated2", 2l, "file1"));
        batchGroup2.getTranslations().add(new Translation("line2", "translated2", 2l, "file2"));

        //when
        batchAggregator.batchGroupCompleted(batchGroup2);
        //then
        verify(callback, times(0)).onLineComplete(any());

        //when
        batchAggregator.batchGroupCompleted(batchGroup1);
        //then
        verify(callback, times(1)).onLineComplete(batchGroup1);
        verify(callback, times(1)).onLineComplete(batchGroup2);
    }

    @Test
    public void aggregate_single_file(){
        //given
        InMemoryBatchAggregator batchAggregator = new InMemoryBatchAggregator();
        BatchAggregator.Callback callback = mock(BatchAggregator.Callback.class);

        batchAggregator.registerCallback(callback);
        batchAggregator.registerFileLength("file1", 2l);

        Translation t1 = new Translation("line1", "translated1", 1l, "file1");
        Translation t2 = new Translation("line2", "translated2", 2l, "file1");

        //when
        batchAggregator.aggregate(t1, t2);

        //then
        verify(callback, times(1)).onLineComplete(new BatchGroup(1l, 2l));
        verify(callback, times(1)).onLineComplete(new BatchGroup(2l, 2l));
    }


    @Test
    public void aggregate_multiple_files_single_invocation(){
        //given
        InMemoryBatchAggregator batchAggregator = new InMemoryBatchAggregator();
        BatchAggregator.Callback callback = mock(BatchAggregator.Callback.class);

        batchAggregator.registerCallback(callback);
        batchAggregator.registerFileLength("file1", 2l);
        batchAggregator.registerFileLength("file2", 2l);
        batchAggregator.registerFileLength("file3", 1l);

        Translation t1 = new Translation("line1", "translated1", 1l, "file1");
        Translation t2 = new Translation("line2", "translated2", 2l, "file1");
        Translation t3 = new Translation("line1", "translated1", 1l, "file2");
        Translation t4 = new Translation("line2", "translated2", 2l, "file2");
        Translation t5 = new Translation("line1", "translated1", 1l, "file3");

        //when
        batchAggregator.aggregate(t1, t2, t3, t4, t5);
        //then
        verify(callback, times(1)).onLineComplete(new BatchGroup(1l, 2l));
        verify(callback, times(1)).onLineComplete(new BatchGroup(2l, 2l));
    }


    @Test
    public void aggregate_multiple_files_multiple_invocations(){
        //given
        InMemoryBatchAggregator batchAggregator = new InMemoryBatchAggregator();
        BatchAggregator.Callback callback = mock(BatchAggregator.Callback.class);

        batchAggregator.registerCallback(callback);
        batchAggregator.registerFileLength("file1", 2l);
        batchAggregator.registerFileLength("file2", 2l);
        batchAggregator.registerFileLength("file3", 1l);

        Translation t1 = new Translation("line1", "translated1", 1l, "file1");
        Translation t2 = new Translation("line2", "translated2", 2l, "file1");
        Translation t3 = new Translation("line1", "translated1", 1l, "file2");
        Translation t4 = new Translation("line2", "translated2", 2l, "file2");
        Translation t5 = new Translation("line1", "translated1", 1l, "file3");

        //when
        batchAggregator.aggregate(t1, t3, t5);
        batchAggregator.aggregate(t2, t4);
        //then
        verify(callback, times(1)).onLineComplete(new BatchGroup(1l, 2l));
        verify(callback, times(1)).onLineComplete(new BatchGroup(2l, 2l));
    }
}