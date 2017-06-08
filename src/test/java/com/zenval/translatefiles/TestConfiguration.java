package com.zenval.translatefiles;

import com.zenval.translatefiles.file.Files;
import com.zenval.translatefiles.file.InvalidFileException;
import com.zenval.translatefiles.job.JobDefinition;
import com.zenval.translatefiles.service.TranslationService;
import com.zenval.translatefiles.service.impl.TestTranslationService;

import org.springframework.batch.core.Job;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Arrays;

@Configuration
@Import(JobDefinition.class)
public class TestConfiguration {

    @Autowired
    private Job job;

    @Bean
    public Files files() throws InvalidFileException {
        return Files.Factory.newInstance(Arrays.asList("Set1.txt", "Set2.txt", "Set3.txt"));
    }

    @Bean
    public TranslationService translationService() {
        return new TestTranslationService();
    }

    @Bean
    public JobLauncherTestUtils jobLauncherTestUtils(){
        JobLauncherTestUtils jobLauncherTestUtils = new JobLauncherTestUtils();
        jobLauncherTestUtils.setJob(job);
        return jobLauncherTestUtils;

    }
}
