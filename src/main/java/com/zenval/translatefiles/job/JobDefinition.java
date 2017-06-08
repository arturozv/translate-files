package com.zenval.translatefiles.job;

import com.zenval.translatefiles.dto.Files;
import com.zenval.translatefiles.dto.TextAndLine;
import com.zenval.translatefiles.dto.Translation;
import com.zenval.translatefiles.file.FileLineCounter;
import com.zenval.translatefiles.job.components.FilePartitioner;
import com.zenval.translatefiles.job.components.MultiFileItemReader;
import com.zenval.translatefiles.job.components.TranslateProcessor;
import com.zenval.translatefiles.service.BatchAggregator;
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
import org.springframework.batch.item.file.transform.LineAggregator;
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

    private final int threads = 1;
    private final int chunkSize = 10000;

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory stepBuilder;

    @Bean(name = "translateFilesJob")
    public Job translateFilesJob(Step clearFilesStep, Step fileTranslateStep, Step batchedFileStep, Step validateFilesStep) {
        return jobs.get("translateFilesJob")
                .start(clearFilesStep)
                .next(fileTranslateStep)
                .next(batchedFileStep)
                .next(validateFilesStep)
                .build();
    }

    @Bean
    public TranslationService translateService() {
        return new TestTranslationService();
        //return new GoogleTranslationService();
    }

    @Bean
    public Step clearFilesStep(Files files) {
        return stepBuilder.get("clearFilesStep").tasklet((contribution, chunkContext) -> {
            FileUtils.write(new File(AS_YOU_GO_FILE), "", Charset.defaultCharset());
            FileUtils.write(new File(BATCHED_FILE), "", Charset.defaultCharset());
            files.getPaths().forEach(path -> new File(path + ".translated").delete());
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    public Step fileTranslateStep(FilePartitioner filePartitioner, @Qualifier("fileTranslateSlaveStep") Step fileTranslateSlaveStep) {
        return stepBuilder.get("fileTranslateStep")
                .partitioner(fileTranslateSlaveStep)
                .partitioner("filePartitioner", filePartitioner)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean(name = "fileTranslateSlaveStep")
    public Step fileTranslateSlaveStep(FlatFileItemReader<TextAndLine> fileReader,
                                       TranslateProcessor translateProcessor,
                                       CompositeItemWriter<Translation> itemWriter) {
        return stepBuilder.get("fileTranslateSlaveStep").
                <TextAndLine, Translation>chunk(chunkSize)
                .reader(fileReader)
                .processor(translateProcessor)
                .writer(itemWriter)
                .taskExecutor(taskExecutor())
                .throttleLimit(threads)
                .build();
    }

    @Bean("batchedFileStep")
    public Step batchedFileStep(MultiFileItemReader translatedMultiResourceItemReader, @Qualifier("batchedItemWriter") ItemWriter<String> batchedItemWriter) {
        return stepBuilder.get("batchedFileStep").
                <List<String>, String>chunk(1)
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
        return getItemWriter(BATCHED_FILE, null);
    }

    @Bean
    public Step validateFilesStep(BatchAggregator batchAggregator) {
        return stepBuilder.get("validateFilesStep").tasklet((contribution, chunkContext) -> {
            long totalLines = batchAggregator.getTotalLines();
            Long asyougoCount = FileLineCounter.getFileLineCount(new File(AS_YOU_GO_FILE)) - 1;
            Long batchedCount = FileLineCounter.getFileLineCount(new File(BATCHED_FILE)) - 1;
            logger.info("Total lines: {}, {} lines: {}, {} lines: {}", totalLines, AS_YOU_GO_FILE, asyougoCount, BATCHED_FILE, batchedCount);
            return RepeatStatus.FINISHED;
        }).build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<TextAndLine> fileReader(@Value("#{stepExecutionContext[" + FilePartitioner.FILE_KEY + "]}") final File file) throws Exception {
        FlatFileItemReader<TextAndLine> fileReader = new FlatFileItemReader<>();
        fileReader.setEncoding("UTF-8");
        fileReader.setLinesToSkip(0);
        fileReader.setResource(new FileSystemResource(file));
        fileReader.setLineMapper(TextAndLine::new);
        return fileReader;
    }

    @Bean
    @StepScope
    public TranslateProcessor translateProcessor(@Value("#{stepExecutionContext[" + FilePartitioner.FILE_ID_KEY + "]}") final String fileId,
                                                 TranslationService translationService) {
        return new TranslateProcessor(fileId, translationService);
    }

    @Bean
    public CompositeItemWriter<Translation> multipleWriter(@Qualifier("fileWriterAsYouGo") ItemWriter<Translation> fileWriterAsYouGo,
                                                           @Qualifier("translatedFileWriter") ItemWriter<Translation> translatedFileWriter) {
        CompositeItemWriter<Translation> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(fileWriterAsYouGo, translatedFileWriter));
        compositeItemWriter.open(new ExecutionContext());
        return compositeItemWriter;
    }

    @StepScope
    @Bean("fileWriterAsYouGo")
    public ItemWriter<Translation> fileWriterAsYouGo() throws Exception {
        return getItemWriter(AS_YOU_GO_FILE, Translation::getTranslated);
    }


    @StepScope
    @Bean("translatedFileWriter")
    public ItemWriter<Translation> translatedFileWriter(@Value("#{stepExecutionContext[" + FilePartitioner.FILE_ID_KEY + "]}") final String fileId) throws Exception {
        return getItemWriter(fileId + ".translated", Translation::getTranslated);
    }

    private <T> ItemWriter<T> getItemWriter(String file, LineAggregator<T> lineAggregator) throws Exception {
        FlatFileItemWriter<T> fileWriter = new FlatFileItemWriter<>();
        fileWriter.setEncoding("UTF-8");
        fileWriter.setResource(new FileSystemResource(new File(file)));
        fileWriter.setShouldDeleteIfExists(false);
        fileWriter.setAppendAllowed(true);
        fileWriter.setLineAggregator(new PassThroughLineAggregator<>());
        if (lineAggregator != null) {
            fileWriter.setLineAggregator(lineAggregator);
        }
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

