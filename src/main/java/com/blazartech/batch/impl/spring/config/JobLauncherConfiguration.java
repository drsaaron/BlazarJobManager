/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl.spring.config;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

/**
 *
 * @author AAR1069
 */
@Configuration
public class JobLauncherConfiguration {

    @Autowired
    @Qualifier("jobRepository")
    private JobRepository jobRepository;

    // should we use an async executor for the job execution?  Default is no (false).
    @Value("${batch.job.async:false}")
    private boolean useAsync;

    @Bean(name = "jobLauncher")
    public JobLauncher getJobLauncher() {
        TaskExecutorJobLauncher l = new TaskExecutorJobLauncher();
        l.setJobRepository(jobRepository);
        if (useAsync) {
            l.setTaskExecutor(simpleAsyncTaskExecutor());
        }
        return l;
    }

    public SimpleAsyncTaskExecutor simpleAsyncTaskExecutor() {
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setConcurrencyLimit(10);
        return simpleAsyncTaskExecutor;
    }
}
