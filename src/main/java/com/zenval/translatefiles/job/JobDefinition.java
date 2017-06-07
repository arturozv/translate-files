package com.zenval.translatefiles.job;

import com.zenval.translatefiles.dto.TextAndLine;
import com.zenval.translatefiles.dto.Translation;
import com.zenval.translatefiles.job.components.BatchFileWriter;
import com.zenval.translatefiles.job.components.FilePartitioner;
import com.zenval.translatefiles.job.components.TranslateProcessor;
import com.zenval.translatefiles.service.BatchAggregator;
import com.zenval.translatefiles.service.TranslationService;
import com.zenval.translatefiles.service.impl.TestTranslationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.File;
import java.util.Arrays;

@Configuration
public class JobDefinition {
    private static final Logger logger = LoggerFactory.getLogger(JobDefinition.class);

    private final int threads = 4;

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory stepBuilder;

    @Bean(name = "translateFilesJob")
    public Job translateFilesJob(Step fileTranslateStep) {
        return jobs.get("translateFilesJob")
                .start(fileTranslateStep)
                .build();
    }

    private TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(threads);
        taskExecutor.setCorePoolSize(threads);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

    @Bean
    public TranslationService translateService() {
        return new TestTranslationService();
    }

    @Bean
    @StepScope
    public TranslateProcessor translateProcessor(@Value("#{stepExecutionContext[" + FilePartitioner.FILE_ID_KEY + "]}") final String fileId,
                                                 TranslationService translationService) {
        return new TranslateProcessor(fileId, translationService);
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
        int chunkSize = 1000;
        return stepBuilder.get("fileTranslateSlaveStep").
                <TextAndLine, Translation>chunk(chunkSize)
                .reader(fileReader)
                .processor(translateProcessor)
                .writer(itemWriter)
                .taskExecutor(taskExecutor())
                .throttleLimit(threads)
                .build();
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
    public CompositeItemWriter<Translation> multipleWriter(FlatFileItemWriter<Translation> fileWriterAsYouGo, BatchFileWriter batchFileWriter) {
        CompositeItemWriter<Translation> compositeItemWriter = new CompositeItemWriter<>();
        compositeItemWriter.setDelegates(Arrays.asList(fileWriterAsYouGo, batchFileWriter));
        compositeItemWriter.open(new ExecutionContext());
        return compositeItemWriter;
    }

    @Bean
    public BatchFileWriter batchFileWriter(BatchAggregator batchAggregator) throws Exception {
        return new BatchFileWriter(batchAggregator);
    }

    @Bean
    public FlatFileItemWriter<Translation> fileWriterAsYouGo() throws Exception {
        FlatFileItemWriter<Translation> fileWriter = new FlatFileItemWriter<>();
        fileWriter.setEncoding("UTF-8");
        fileWriter.setResource(new FileSystemResource(new File("AsYouGo.txt")));
        fileWriter.setShouldDeleteIfExists(true);
        fileWriter.setLineAggregator(new PassThroughLineAggregator<>());
        fileWriter.setLineAggregator(Translation::getTranslated);
        fileWriter.setSaveState(false);
        fileWriter.setTransactional(false);
        return fileWriter;
    }
}

