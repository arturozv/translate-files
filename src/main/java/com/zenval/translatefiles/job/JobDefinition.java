package com.zenval.translatefiles.job;

import com.zenval.translatefiles.file.Files;
import com.zenval.translatefiles.service.TestTranslateService;
import com.zenval.translatefiles.service.TranslateService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
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

@Configuration
public class JobDefinition {
    private static final Logger logger = LoggerFactory.getLogger(JobDefinition.class);

    @Autowired
    private JobBuilderFactory jobs;

    @Autowired
    private StepBuilderFactory stepBuilder;

    @Autowired
    @Qualifier("files")
    private Files files;

    @Bean(name = "translateFilesJob")
    public Job translateFilesJob(Step translateFilesValidationStep, Step fileTranslateStep) {
        return jobs.get("translateFilesJob")
                .start(translateFilesValidationStep)
                .next(fileTranslateStep)
                .build();
    }

    private TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(1);
        taskExecutor.setCorePoolSize(1);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

    @Bean
    public TranslateService translateService() {
        return new TestTranslateService();
    }

    @Bean
    public Step translateFilesValidationStep() {
        logger.info("pathFiles: {}", files);
        return stepBuilder.get("translateFilesValidationStep").tasklet((contribution, chunkContext) -> {
            logger.info("Tasklet");

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
    public Step fileTranslateSlaveStep(FlatFileItemReader<String> fileReader,
                                       TranslateProcessor translateProcessor,
                                       FlatFileItemWriter<String> fileWriter) {
        int chunkSize = 1000;
        return stepBuilder.get("fileTranslateSlaveStep").
                <String, String>chunk(chunkSize)
                .reader(fileReader)
                .processor(translateProcessor)
                .writer(fileWriter)
                .taskExecutor(taskExecutor())
                .throttleLimit(1)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<String> fileReader(@Value("#{stepExecutionContext[file]}") final File file) throws Exception {
        FlatFileItemReader<String> fileReader = new FlatFileItemReader<>();
        fileReader.setEncoding("UTF-8");
        fileReader.setLinesToSkip(0);
        fileReader.setResource(new FileSystemResource(file));
        fileReader.setLineMapper((line, lineNumber) -> line);
        return fileReader;
    }

    @Bean
    public FlatFileItemWriter<String> fileWriter() throws Exception {
        FlatFileItemWriter<String> fileWriter = new FlatFileItemWriter<>();
        fileWriter.setEncoding("UTF-8");
        fileWriter.setResource(new FileSystemResource(new File("AsYouGo.txt")));
        fileWriter.setShouldDeleteIfExists(true);
        fileWriter.setLineAggregator(new PassThroughLineAggregator<>());
        return fileWriter;
    }
}

