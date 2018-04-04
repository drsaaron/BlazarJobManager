/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl.spring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 *
 * @author AAR1069
 */
@Configuration
public class TaskExecutorConfiguration {
    
    @Bean(name = "taskExecutor")
    public TaskExecutor getTaskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }
}
