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
        File file1 = testFolder.newFile("file1.txt");
        File file2 = testFolder.newFile("file2.txt");
        File file3 = testFolder.newFile("file3.txt");

        FileUtils.writeStringToFile(file1, "f1line1\nf1line2", Charset.defaultCharset());
        FileUtils.writeStringToFile(file2, "f2line1\nf2line2", Charset.defaultCharset());
        FileUtils.writeStringToFile(file3, "f3line1\nf3line2", Charset.defaultCharset());

        List<File> all = Arrays.asList(file1, file2, file3);
        List<String> paths = all.stream().map(File::getPath).collect(Collectors.toList());

        Files files = mock(Files.class);
        when(files.getPaths()).thenReturn(paths);

        MultiFileItemReader multiFileItemReader = new MultiFileItemReader(files);

        //when
        List<String> firstLine = multiFileItemReader.read();

        //then
        assertThat(firstLine).hasSize(3);
        assertThat(firstLine).containsExactly("f1line1", "f2line1", "f3line1");
    }
}