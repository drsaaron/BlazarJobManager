/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl.spring.config;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author AAR1069
 */
@Configuration
public class JobLauncherConfiguration {

    @Autowired
    @Qualifier("jobRepository")
    private JobRepository jobRepository;
    
    @Bean(name = "jobLauncher") 
    public JobLauncher getJobLauncher() {
        SimpleJobLauncher l = new SimpleJobLauncher();
        l.setJobRepository(jobRepository);
        return l;
    }
}
