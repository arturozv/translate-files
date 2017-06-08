package com.zenval.translatefiles.job;

import com.zenval.translatefiles.dto.Files;
import com.zenval.translatefiles.file.FileLineCounter;
import com.zenval.translatefiles.job.components.FilePartitioner;
import com.zenval.translatefiles.job.components.MultiFileItemReader;
import com.zenval.translatefiles.job.components.TranslateProcessor;
import com.zenval.translatefiles.service.TranslationService;
import com.zenval.translatefiles.service.impl.TestTranslationService;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Configuration
public class JobDefinition {
    private static final Logger logger = LoggerFactory.getLogger(JobDefinition.class);
    private static final String AS_YOU_GO_FILE = "AsYouGo.txt";
    private static final String BATCHED_FILE = "Batched.txt";

    private final int threads = 4;
    private final int chunkSize = 10000;

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory stepBuilder;

    @Bean(name = "translateFilesJob")
    public Job translateFilesJob(Step clearOldFilesStep, Step translateFilesStep, Step generateBatchedFileStep, Step validateOutputFilesStep) {
        return jobs.get("translateFilesJob")
                .start(clearOldFilesStep)
                .next(translateFilesStep)
                .next(generateBatchedFileStep)
                .next(validateOutputFilesStep)
                .build();
    }

    @Bean
    public TranslationService translateService() {
        return new TestTranslationService();
        //return new GoogleTranslationService();
    }

    @Bean
    public Step clearOldFilesStep() {
        return stepBuilder.get("clearOldFilesStep").tasklet((contribution, chunkContext) -> {
            FileUtils.write(new File(AS_YOU_GO_FILE), "", Charset.defaultCharset());
            FileUtils.write(new File(BATCHED_FILE), "", Charset.defaultCharset());
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step translateFilesStep(FilePartitioner filePartitioner, @Qualifier("translateFilesSlaveStep") Step translateFilesSlaveStep) {
        return stepBuilder.get("translateFilesStep")
                .partitioner(translateFilesSlaveStep)
                .partitioner("filePartitioner", filePartitioner)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean(name = "translateFilesSlaveStep")
    public Step translateFilesSlaveStep(FlatFileItemReader<String> fileReader,
                                       TranslateProcessor translateProcessor,
                                       CompositeItemWriter<String> itemWriter) {
        return stepBuilder.get("translateFilesSlaveStep").
                <String, String>chunk(chunkSize)
                .reader(fileReader)
                .processor(translateProcessor)
                .writer(itemWriter)
                .taskExecutor(taskExecutor())
                .throttleLimit(threads)
                .build();
    }

    @Bean("generateBatchedFileStep")
    public Step generateBatchedFileStep(MultiFileItemReader translatedMultiResourceItemReader, @Qualifier("batchedItemWriter") ItemWriter<String> batchedItemWriter) {
        return stepBuilder.get("generateBatchedFileStep").
                <List<String>, String>chunk(1) //has to be 1 by 1 to read sequentially each line of each file
                .reader(translatedMultiResourceItemReader)
                .processor(item -> item.stream().sorted().collect(Collectors.joining("\n")))
                .writer(batchedItemWriter)
                .build();
    }

    @Bean
    public MultiFileItemReader translatedMultiResourceItemReader(Files files) throws Exception {
        return new MultiFileItemReader(files);
    }

    @Bean("batchedItemWriter")
    public ItemWriter<String> batchedItemWriter() throws Exception {
        return getItemWriter(BATCHED_FILE);
    }

    @Bean
    public Step validateOutputFilesStep(Files files) {
        return stepBuilder.get("validateOutputFilesStep").tasklet((contribution, chunkContext) -> {
            long totalLines = files.getTotalLinesCount();
            Long asyougoCount = FileLineCounter.getFileLineCount(new File(AS_YOU_GO_FILE)) - 1;
            Long batchedCount = FileLineCounter.getFileLineCount(new File(BATCHED_FILE)) - 1;
            logger.info("RESULT -> Total lines: {}, {} lines: {}, {} lines: {}", totalLines, AS_YOU_GO_FILE, asyougoCount, BATCHED_FILE, batchedCount);
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<String> fileReader(@Value("#{stepExecutionContext[" + FilePartitioner.FILE_ID_KEY + "]}") final String fileId) throws Exception {
        FlatFileItemReader<String> fileReader = new FlatFileItemReader<>();
        fileReader.setEncoding("UTF-8");
        fileReader.setLinesToSkip(0);
        fileReader.setResource(new FileSystemResource(fileId));
        fileReader.setLineMapper((line, lineNumber) -> line);
        return fileReader;
    }

    @Bean
    @StepScope
    public TranslateProcessor translateProcessor(TranslationService translationService) {
        return new TranslateProcessor(translationService);
    }

    @Bean
    public CompositeItemWriter<String> multipleWriter(@Qualifier("fileWriterAsYouGo") ItemWriter<String> fileWriterAsYouGo,
                                                      @Qualifier("translatedFileWriter") ItemWriter<String> translatedFileWriter) {
        CompositeItemWriter<String> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(fileWriterAsYouGo, translatedFileWriter));
        compositeItemWriter.open(new ExecutionContext());
        return compositeItemWriter;
    }

    @StepScope
    @Bean("fileWriterAsYouGo")
    public ItemWriter<String> fileWriterAsYouGo() throws Exception {
        return getItemWriter(AS_YOU_GO_FILE);
    }


    @StepScope
    @Bean("translatedFileWriter")
    public ItemWriter<String> translatedFileWriter(@Value("#{stepExecutionContext[" + FilePartitioner.FILE_ID_KEY + "]}") final String fileId) throws Exception {
        return getItemWriter(fileId + Files.TRANSLATED_EXTENSION);
    }

    private ItemWriter<String> getItemWriter(String file) throws Exception {
        FlatFileItemWriter<String> fileWriter = new FlatFileItemWriter<>();
        fileWriter.setEncoding("UTF-8");
        fileWriter.setResource(new FileSystemResource(file));
        fileWriter.setShouldDeleteIfExists(false);
        fileWriter.setAppendAllowed(true);
        fileWriter.setLineAggregator(new PassThroughLineAggregator<>());
        fileWriter.setSaveState(false);
        fileWriter.setTransactional(false);
        fileWriter.open(new ExecutionContext());
        fileWriter.afterPropertiesSet();
        return fileWriter;
    }


    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(threads);
        taskExecutor.setCorePoolSize(threads);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }
}

