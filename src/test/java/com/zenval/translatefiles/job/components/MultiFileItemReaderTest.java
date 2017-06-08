package com.zenval.translatefiles.job.components;

import com.zenval.translatefiles.file.Files;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MultiFileItemReaderTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();


    @Test
    public void file_reads_multiple_lines() throws Exception {
        //given
        File file1 = testFolder.newFile("file1.txt.translated");
        File file2 = testFolder.newFile("file2.txt.translated");
        File file3 = testFolder.newFile("file3.txt.translated");

        FileUtils.writeStringToFile(file1, "f1line1\nf1line2", Charset.defaultCharset());
        FileUtils.writeStringToFile(file2, "f2line1\nf2line2", Charset.defaultCharset());
        FileUtils.writeStringToFile(file3, "f3line1\nf3line2\nf3line3", Charset.defaultCharset());

        List<File> all = Arrays.asList(file1, file2, file3);
        List<String> paths = all.stream().map(f-> f.getPath().replace(".translated", "")).collect(Collectors.toList());

        Files files = mock(Files.class);
        when(files.getPaths()).thenReturn(paths);

        MultiFileItemReader multiFileItemReader = new MultiFileItemReader(files);
        multiFileItemReader.init();

        //when
        List<String> firstLine = multiFileItemReader.read();
        //then
        assertThat(firstLine).hasSize(3);
        assertThat(firstLine).containsExactly("f1line1", "f2line1", "f3line1");

        //when
        List<String> secondLine = multiFileItemReader.read();
        //then
        assertThat(secondLine).hasSize(3);
        assertThat(secondLine).containsExactly("f1line2", "f2line2", "f3line2");

        //when
        List<String> thirdLine = multiFileItemReader.read();
        //then
        assertThat(thirdLine).hasSize(1);
        assertThat(thirdLine).containsExactly("f3line3");

        //when
        List<String> forthLine = multiFileItemReader.read();
        //then
        assertThat(forthLine).isNull();
    }
}