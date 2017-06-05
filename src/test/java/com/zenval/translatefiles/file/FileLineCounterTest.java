package com.zenval.translatefiles.file;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.Charset;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class FileLineCounterTest {

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void file_empty() throws Exception {
        File file = testFolder.newFile("empty.txt");

        Long count = FileLineCounter.getFileLineCount(file);

        assertThat(count, is(0l));
    }

    @Test
     public void file_not_empty() throws Exception {
        File file = testFolder.newFile("not_empty1.txt");
        FileUtils.writeStringToFile(file, "line1", Charset.defaultCharset());

        Long count = FileLineCounter.getFileLineCount(file);

        assertThat(count, is(1l));
    }

    @Test
    public void file_not_empty_multiple() throws Exception {
        File file = testFolder.newFile("not_empty1.txt");
        FileUtils.writeStringToFile(file, "line1\nline2", Charset.defaultCharset());

        Long count = FileLineCounter.getFileLineCount(file);

        assertThat(count, is(2l));
    }

}