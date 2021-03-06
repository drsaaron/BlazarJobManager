/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.main;

import com.blazartech.batch.IJobManager;
import com.blazartech.batch.JobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main program for executing jobs. The invoker will pass the name of the spring
 * configuration file, the job name, and any arguments for the job.
 *
 * The implementation assumes an XML configuration file being present, though
 * that XML can in turn enable annotation configuration. The reason for this is
 * that the invoker can then specify any configuration at run-time, thus
 * allowing simple overrides of job definitions or configurations.
 *
 * @author AAR1069
 */
public class JobRunner {

    private static final Logger logger = LoggerFactory.getLogger(JobRunner.class);

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                throw new RuntimeException("usage: configFileName jobName [arguments]");
            }

            String configFileName = args[0];
            String jobName = args[1];

            // load the configuration and get the JobManager
            logger.info("loading the configuration file");
            SpringHelper helper = SpringHelper.instance(configFileName);
            IJobManager jobManager = helper.getJobManager();

            // run the job.
            logger.info("starting job " + jobName);
            JobStatus status = jobManager.runJob(jobName, args);
            if (status == JobStatus.Failure) {
                System.exit(1);
            }

            // done.  Explicitly exit as for some reason the java process doesn't seem to
            // end otherwise.
            logger.info("finished.");
            System.exit(0);
        } catch (RuntimeException e) {
            logger.error("error running job: " + e.getMessage(), e);
            System.exit(1);
        }
    }

}
