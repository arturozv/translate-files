package com.zenval.translatefiles.service.impl;

import com.zenval.translatefiles.dto.Translation;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


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
    public void get_expected_word_count_by_line() {
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


}