package com.zenval.translatefiles.job.components;

import com.zenval.translatefiles.dto.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.core.io.FileSystemResource;

import java.util.ArrayList;
import java.util.List;

public class MultiFileItemReader implements ItemReader<List<String>> {
    private static final Logger logger = LoggerFactory.getLogger(MultiFileItemReader.class);

    private List<FlatFileItemReader<String>> flatFileItemReaders;
    private Files files;

    public MultiFileItemReader(Files files) throws Exception {
        this.files = files;
        flatFileItemReaders = new ArrayList<>(files.getPaths().size());
    }

    @Override
    public List<String> read() throws Exception {
        List<String> words = new ArrayList<>();
        for (FlatFileItemReader<String> flatFileItemReader : flatFileItemReaders) {
            String word = flatFileItemReader.read();
            if (word != null) {
                words.add(word);
            }
        }
        return words.isEmpty() ? null : words;
    }

    @BeforeStep
    public void init() throws Exception {
        for (String path : files.getPaths()) {
            FileSystemResource file = new FileSystemResource(path + Files.TRANSLATED_EXTENSION);

            if (!file.exists()) {
                file.getFile().createNewFile();
            }

            FlatFileItemReader<String> flatFileItemReader = new FlatFileItemReader<>();
            flatFileItemReader.setLineMapper((line, lineNumber) -> line);
            flatFileItemReader.afterPropertiesSet();
            flatFileItemReader.setStrict(true);
            flatFileItemReader.setResource(file);
            flatFileItemReader.open(new ExecutionContext());

            logger.info("Created reader for {}, content length: {}", file.getPath(), file.contentLength());

            flatFileItemReaders.add(flatFileItemReader);
        }
    }

    @AfterStep
    public void clear() {
        files.deleteTmp();
    }
}
