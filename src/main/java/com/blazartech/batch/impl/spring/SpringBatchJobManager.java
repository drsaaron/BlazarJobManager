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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersIncrementer;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.batch.core.step.StepExecution;
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

    private Boolean checkExitStatusForNewInstance(ExitStatus exitStatus) {
        switch (exitStatus.getExitCode()) {
            case "COMPLETED" -> {
                logger.info("job completed successfully, so new instance needed");
                return true;
            }
            case "NOOP" ->
                logger.info("status is noop, continuing");
            case "STOPPED" -> {
                logger.info("status is stopped, so no new instance needed");
                return false;
            }
            case "FAILED" -> {
                logger.info("status failed, no new instance");
                return false;
            }
            default ->
                logger.info("unexpected status: " + exitStatus);
        }
        return null;
    }

    // define a comparator to sort the job instances in descending order by id so that
    // the first in the list would be the most recent (highest ID).  The repository seems to do
    // this on its own, but as the ligic in isNewInstanceNeeded will rely on that
    // behavior, be explicit.
    private static final Comparator<JobExecution> JOB_EXECUTION_NEW_INSTANCE_COMPARATOR = (i1, i2) -> Long.compare(i2.getId(), i1.getId());
    
    public boolean isNewInstanceNeeded(Job job) {
        logger.info("checking prior runs of the job to determine if new instance needed");

        // is the job even restartable?
        if (!job.isRestartable()) {
            logger.info("job is not restartable, so a new instance is definitely needed.");
            return true;
        }
        
        // a restartable job
        String jobIdentifier = job.getName();
        List<JobInstance> lastInstances = getJobRepository().getJobInstances(jobIdentifier, 0, 1);
        List<Boolean> statuses = lastInstances.stream()
                .map(instance -> getJobRepository().getJobExecutions(instance))
                .flatMap(lastExecutions -> lastExecutions.stream())
                .sorted(JOB_EXECUTION_NEW_INSTANCE_COMPARATOR)
                .map(execution -> execution.getExitStatus())
                .map(exitStatus -> checkExitStatusForNewInstance(exitStatus))
                .filter(status -> status != null) // filter out the statuses we don't care about, to just get those that actually complete the job in some way
                .collect(Collectors.toList());
        logger.info("got {} exit statuses", statuses.size());

        // if the status list is empty, we cannot say one way or another, so create a new instance
        if (statuses.isEmpty()) {
            // not really sure, so return true to be safe;
            logger.info("no idea, so returning true");
            return true;
        } else {
            // grab the first status.  We've filtered down to completed statuses
            // and sorted such that the most recent one is first.  The most recent
            // value is what determines if a new instance is needed.
            return statuses.getFirst();
        }
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
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException | InvalidJobParametersException e) {
            logger.error("error executing job: " + e.getMessage(), e);
            throw new RuntimeException("error executing job: " + e.getMessage(), e);
        }
    }

    private void logParameters(JobParameters parameters) {
        logger.info("logging parameters");
	parameters.parameters().forEach(p -> logger.info("parameter {} = {}", p.name(), p.value()));
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
