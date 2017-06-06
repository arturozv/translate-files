package com.zenval.translatefiles;

import com.zenval.translatefiles.file.Files;
import com.zenval.translatefiles.file.InvalidFileException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class TranslateFilesApplication {
    private static final Logger logger = LoggerFactory.getLogger(TranslateFilesApplication.class);

    public static void main(String[] args) {

        String[] paths = new String[]{"Set1.txt", "Set2.txt", "Set3.txt"};

        final Files files = getFiles(paths);

        SpringApplication application = getApplication(files);
        ConfigurableApplicationContext context = application.run();

        JobLauncher jobLauncher = context.getBean(JobLauncher.class);
        Job job = (Job) context.getBean("translateFilesJob");

        try {

            JobExecution jobExecution = jobLauncher.run(job, new JobParametersBuilder().addString("run.id", UUID.randomUUID().toString()).toJobParameters());
            logger.info("Process finished! {}", jobExecution);

        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            logger.error("Error executing batch process: {}", e.getMessage(), e);
            System.exit(1);
        }

        context.close();
    }

    private static SpringApplication getApplication(Files files) {
        SpringApplication application = new SpringApplication(TranslateFilesApplication.class);
        application.setBannerMode(Banner.Mode.OFF);
        application.addInitializers(
                cxt -> cxt
                        .getBeanFactory()
                        .registerSingleton("files", files)
        );
        return application;
    }

    private static Files getFiles(String... paths) {
        Files files = null;
        try {
            files = Files.Factory.newInstance(Arrays.asList(paths));
        } catch (InvalidFileException e) {
            logger.error("Invalid arguments: {}", e.getMessage(), e);
            System.exit(1);
        }
        return files;
    }
}