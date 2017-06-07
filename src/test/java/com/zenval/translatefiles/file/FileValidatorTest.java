package com.zenval.translatefiles.file;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FileValidatorTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void file_ok() throws Exception {
        File fileOk = testFolder.newFile("ok.txt");
        FileUtils.writeStringToFile(fileOk, "content", Charset.defaultCharset());

        assertThatCode(() -> {
            FileValidator.validate(fileOk);

        }).doesNotThrowAnyException();
    }

    @Test
    public void file_empty() throws Exception {
        File fileEmpty = testFolder.newFile("empty.txt");

        assertThatThrownBy(() -> {
            FileValidator.validate(fileEmpty);

        }).isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("empty");
    }

    @Test
    public void file_directory() throws Exception {
        File fileDirectory = testFolder.newFolder("folder.txt");

        assertThatThrownBy(() -> {
            FileValidator.validate(fileDirectory);

        }).isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("directory");
    }

    @Test
    public void file_non_readable() throws Exception {
        File fileNonReadable = testFolder.newFile("non_readable.txt");
        fileNonReadable.setReadable(false);

        assertThatThrownBy(() -> {
            FileValidator.validate(fileNonReadable);

        }).isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("readable");
    }

    @Test
    public void file_null() throws Exception {
        File fileNull = null;

        assertThatThrownBy(() -> {
            FileValidator.validate(fileNull);

        }).isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("null");
    }

    @Test
    public void file_with_empty_lines() throws Exception {
        File fileOk = testFolder.newFile("emptylines.txt");
        FileUtils.writeStringToFile(fileOk, "content1\ncontent2\n", Charset.defaultCharset());

        assertThatCode(() -> {
            FileValidator.validate(fileOk);

        }).isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("empty");
    }
}