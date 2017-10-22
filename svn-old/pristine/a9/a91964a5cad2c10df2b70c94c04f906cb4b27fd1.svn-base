/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nm.ffba.common.batch.main;

import com.nm.ffba.common.batch.IJobManager;
import com.nm.ffba.common.batch.JobStatus;
import org.apache.log4j.Logger;

/**
 * Main program for executing jobs.  The invoker will pass the name of the spring
 * configuration file, the job name, and any arguments for the job.
 * 
 * @author AAR1069
 */
public class JobRunner {

    private static final Logger logger = Logger.getLogger(JobRunner.class);
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            throw new RuntimeException("usage: configFileName jobName [arguments]");
        }

        String configFileName = args[0];
        String jobName = args[1];

        SpringHelper helper = SpringHelper.instance(configFileName);
        IJobManager jobManager = helper.getJobManager();

    //    jobManager.forceJobToSuccess(jobName);
    //    jobManager.forceStepToSuccess(jobName, "projectedPaymentProcessorIdentifyProjectedPaymentsStep");
        
        JobStatus status = jobManager.runJob(jobName, args);
        if (status == JobStatus.Failure) {
            System.exit(1);
        }
    }

}
