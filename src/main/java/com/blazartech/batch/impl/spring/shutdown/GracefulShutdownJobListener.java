/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.blazartech.batch.impl.spring.shutdown;

import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author aar1069
 */
@Component
@Scope("prototype") // necessary because each instance needs to keep track of the handler thread
public class GracefulShutdownJobListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(GracefulShutdownJobListener.class);
    
    @Autowired
    private Provider<GracefulShutdownHandler> shutdownHandlerProvider;

    private GracefulShutdownHandler shutdownHandler;
    
    @Override
    public void beforeJob(JobExecution je) {
        log.info("job {} is about to start, execution ID {}", je.getJobInstance().getJobName(), je.getId());
        
        // setup graceful shutdown
        shutdownHandler = shutdownHandlerProvider.get();
        shutdownHandler.setExecutionId(je.getId());
        Runtime.getRuntime().addShutdownHook(shutdownHandler);
    }

    @Override
    public void afterJob(JobExecution je) {
        log.info("job {} (execution {}) completed: {}", je.getJobInstance().getJobName(), je.getId(), je.getExitStatus());
        Runtime.getRuntime().removeShutdownHook(shutdownHandler);
        shutdownHandler = null;
    }
    
}
