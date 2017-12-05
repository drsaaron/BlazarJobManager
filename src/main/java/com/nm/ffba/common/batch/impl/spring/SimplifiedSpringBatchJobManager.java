/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nm.ffba.common.batch.impl.spring;

import com.nm.ffba.common.batch.IJobManager;
import com.nm.ffba.common.batch.IJobParametersBuilder;
import com.nm.ffba.common.batch.JobStatus;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;

/**
 * Implement a simplified job manager.  This version will not attempt to check for
 * prior execution of a job, nor will it handle incrementing a run ID for a duplicate
 * job execution.  This is provided because while the full implementation (@see SpringBatchJobManager)
 * works just fine with a job repository on MySQL, it does not work with DB2 or
 * Sybase because of unexplained charset conversion problems.
 * 
 * @author aar1069
 */
public class SimplifiedSpringBatchJobManager extends BaseSpringBatchJobManager implements IJobManager {
    
    private static final Logger logger = Logger.getLogger(SimplifiedSpringBatchJobManager.class);

    private JobStatus runJob(Job job, JobParameters parameters) {
        logger.info("starting job");
        try {
            JobExecution execution = getJobLauncher().run(job, parameters);
            ExitStatus status = execution.getExitStatus();
            logger.info("exit status = " + status);
            if (status.getExitCode().equals(ExitStatus.COMPLETED.getExitCode())) {
                return JobStatus.Success;
            } else {
                return JobStatus.Failure;
            }
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            logger.error("error executing job: " + e.getMessage(), e);
            throw new RuntimeException("error executing job: " + e.getMessage(), e);
        }
    }

    private void logParameters(JobParameters parameters) {
        logger.info("logging parameters");
        Map<String, JobParameter> p = parameters.getParameters();
        p.keySet().stream().forEach((name) -> {
            logger.info("parameter " + name + " = " + p.get(name));
        });
    }

    @Override
    public JobStatus runJob(String jobName, Map<String, Object> parameters) {
        logger.info("starting job " + jobName);

        // find the job.
        Job job = getJobs().get(jobName);
        if (job == null) {
            throw new IllegalArgumentException("Job " + jobName + " not defind.");
        }

        // build the parameters.
        JobParameters jobParameters = new JobParameters();
        for (String key : parameters.keySet()) {
            jobParameters = addParameter(jobParameters, key, parameters.get(key));
        }

        // log the parameters.
        logParameters(jobParameters);

        // run the job
        return runJob(job, jobParameters);
    }

    @Override
    public JobStatus runJob(String jobName, String[] args) {
        logger.info("running job " + jobName + " with arguments " + String.join(", ", args));
        IJobParametersBuilder parametersBuilder = getParameterBuilders().get(jobName);
        return runJob(jobName, args, parametersBuilder);
    }

    private Map<String, IJobParametersBuilder> parameterBuilders;

    public Map<String, IJobParametersBuilder> getParameterBuilders() {
        return parameterBuilders;
    }

    public void setParameterBuilders(Map<String, IJobParametersBuilder> parameterBuilders) {
        this.parameterBuilders = parameterBuilders;
    }

    @Override
    public JobStatus runJob(String jobName, String[] args, IJobParametersBuilder parameterBuilder) {
        Map<String, Object> parameters = parameterBuilder.buildJobParameters(args);
        return runJob(jobName, parameters);
    }

    
}
