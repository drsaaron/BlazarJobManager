/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl.spring;

import com.blazartech.batch.IJobManager;
import com.blazartech.batch.IJobParametersBuilder;
import com.blazartech.batch.JobStatus;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A job manager implemented using the spring batch framework. The
 * implementation will have a map of job name to job, which would be configured
 * via spring. This also has a map of job name to parameter builder so that an
 * array of strings can be converted to a parameter set.
 *
 * @author AAR1069
 */
@Component("SpringBatchJobManager")
public class SpringBatchJobManager extends BaseSpringBatchJobManager implements IJobManager {

    private static final Logger logger = LoggerFactory.getLogger(SpringBatchJobManager.class);

    @Autowired
    private JobParametersIncrementer incrementer;

    public JobParametersIncrementer getIncrementer() {
        return incrementer;
    }

    public void setIncrementer(JobParametersIncrementer incrementer) {
        this.incrementer = incrementer;
    }

    private JobParameters getLastRunJobParameters(Job job) {
        logger.info("getting parameters for last job run");

        String jobIdentifier = job.getName();
        JobInstance lastInstance = getLastJobInstance(jobIdentifier);
        if (lastInstance == null) { return null; }
        logger.info("last instance ID = " + lastInstance.getId());

        JobExecution lastExecution = getLastJobExecution(lastInstance);
        return lastExecution.getJobParameters();
    }

    public boolean isNewInstanceNeeded(Job job) {
        logger.info("checking prior runs of the job to determine if new instance needed");

        // is the job even restartable?
        if (!job.isRestartable()) {
            logger.info("job is not restartable, so a new instance is definitely needed.");
            return true;
        }
        
        // a restartable job
        String jobIdentifier = job.getName();
        List<JobInstance> lastInstances = getJobExplorer().getJobInstances(jobIdentifier, 0, 1);
        for (JobInstance instance : lastInstances) {
            List<JobExecution> lastExecutions = getJobExplorer().getJobExecutions(instance);
            for (JobExecution execution : lastExecutions) {
                ExitStatus exitStatus = execution.getExitStatus();
                switch (exitStatus.getExitCode()) {
                    case "COMPLETED" -> {
                        logger.info("job completed successfully, so new instance needed");
                        return true;
                    }
                    case "NOOP" -> logger.info("status is noop, continuing");
                    case "FAILED" -> {
                        logger.info("status failed, no new instance");
                        return false;
                    }
                    default -> logger.info("unexpected status: " + exitStatus);
                }
            }
        }

        // not really sure, so return true to be safe;
        logger.info("no idea, so returning true");
        return true;
    }

    private JobStatus runJob(Job job, JobParameters parameters) {
        logger.info("starting job");
        try {
            // we need to add run.id to the parameters so that we can get the correct instance.
            JobParameters lastRunParameters = getLastRunJobParameters(job);
            if (lastRunParameters != null) {
                Long lastRunID = lastRunParameters.getLong("run.id");
                if (lastRunID != null) {
                    parameters = addParameter(parameters, "run.id", lastRunID);
                } else {
                    logger.info("last run.id is null");
                }
            }

            // check for new status.
            if (lastRunParameters == null) {
                logger.info("job has never run, so starting fresh.");
            } else {
                if (isNewInstanceNeeded(job)) {
                    logger.info("creating new job instance.");

                    // get the next instance of the job.
                    parameters = getIncrementer().getNext(parameters);
                } else {
                    logger.info("using old instance.");
                }
            }

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

    @Override
    public JobStatus runJob(String jobName, String[] args, IJobParametersBuilder parameterBuilder) {
        Map<String, Object> parameters = parameterBuilder.buildJobParameters(args);
        return runJob(jobName, parameters);
    }

    @Override
    public void forceJobToSuccess(String jobName) {
        logger.info("forcing last execution of " + jobName + " to success");

        // get the last execution of the job.
        JobExecution lastExecution = getLastJobExecution(jobName);
        
        // sanity checks.
        if (lastExecution == null) {
            logger.info("job has never been executed!");
            throw new IllegalStateException("job " + jobName + " has never been run");
        } else if (lastExecution.getExitStatus().getExitCode().equals("COMPLETED")) {
            logger.info("job has already completed successfully!");
            throw new IllegalStateException("job " + jobName + " has already completed successfully");
        }

        // update status.
        lastExecution.setStatus(BatchStatus.COMPLETED);
        lastExecution.setExitStatus(ExitStatus.COMPLETED);
        getJobRepository().update(lastExecution);
    }

    @Override
    public void forceStepToSuccess(String jobName, String stepName) {
        logger.info("forcing step " + stepName + " in job " + jobName + " to success.");
        
        // get the last execution of the job.
        JobExecution lastExecution = getLastJobExecution(jobName);
        
        // sanity checks.
        if (lastExecution == null) {
            logger.info("job has never been executed!");
            throw new IllegalStateException("job " + jobName + " has never been run");
        } else if (lastExecution.getExitStatus().getExitCode().equals("COMPLETED")) {
            logger.info("job has already completed successfully!");
            throw new IllegalStateException("job " + jobName + " has already completed successfully");
        }
        
        // get the steps from that run.
        Collection<StepExecution> stepExecutions = lastExecution.getStepExecutions();
        
        // find that step.
        StepExecution lastStepExecution = null;
        for (StepExecution step : stepExecutions) {
            if (step.getStepName().equals(stepName)) {
                lastStepExecution = step;
                break;
            }
        }
        
        // sanity check.
        if (lastStepExecution == null) {
            throw new IllegalArgumentException("step " + stepName + " not found in last execution of " + jobName);
        } else if (lastStepExecution.getExitStatus().getExitCode().equals("COMPLETED")) {
            logger.info("step has already completed successfully!");
            throw new IllegalStateException("step " + stepName + " has already completed successfully");
        }
        
        // update
        lastStepExecution.setStatus(BatchStatus.COMPLETED);
        lastStepExecution.setExitStatus(ExitStatus.COMPLETED);
        getJobRepository().update(lastStepExecution);
    }
}
