/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.blazartech.batch.impl.spring.shutdown;

import com.blazartech.batch.IJobManager;
import com.blazartech.batch.JobInformation;
import com.blazartech.batch.JobStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.launch.JobExecutionNotRunningException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 *
 * @author aar1069
 */
@Component
@Scope("prototype")
@Slf4j
public class GracefulShutdownHandler extends Thread {

    @Autowired
    private JobOperator jobOperator;
    
    private long executionId;
    
    @Autowired
    private IJobManager jobManager;

    public void setExecutionId(long executionId) {
        this.executionId = executionId;
    }

    @Override
    public void run() {
        log.info("graceful shutdown");
        try {
            JobInformation currentStatus = jobManager.getJobInformation(executionId);
            if (currentStatus.getStatus() == JobStatus.Running) {
                log.info("trying to gracefully shutdown");
                jobOperator.stop(executionId);
                Thread.sleep(10000); // give time for the any currently running steps to complete
            } else {
                log.info("job is already complete");
            }
        } catch (JobExecutionNotRunningException | NoSuchJobExecutionException | InterruptedException e) {
            log.error("unable to gracefully shutdown: " + e.getMessage(), e);
        }
    }
}
