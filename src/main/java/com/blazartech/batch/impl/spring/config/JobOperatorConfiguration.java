/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl.spring.config;

import org.springframework.batch.core.launch.support.JobOperatorFactoryBean;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

/**
 *
 * @author AAR1069
 */
@Configuration
public class JobOperatorConfiguration {

    @Autowired
    private JobRepository jobRepository;

    // should we use an async executor for the job execution?  Default is no (false).
    @Value("${batch.job.async:false}")
    private boolean useAsync;
    
    @Autowired
    private TaskExecutor taskExecutor;

    @Bean
    public JobOperatorFactoryBean jobOperator() {
        JobOperatorFactoryBean l = new JobOperatorFactoryBean();
        l.setJobRepository(jobRepository);
        if (useAsync) {
            l.setTaskExecutor(taskExecutor);
        }
        return l;
    }

}
