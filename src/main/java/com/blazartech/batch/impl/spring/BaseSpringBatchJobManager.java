/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl.spring;

import com.blazartech.batch.IJobManager;
import com.blazartech.batch.IJobParametersBuilder;
import com.blazartech.batch.JobInformation;
import com.blazartech.batch.JobStatus;
import com.blazartech.batch.impl.JobManagerBaseImpl;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 *
 * @author aar1069
 */
public abstract class BaseSpringBatchJobManager extends JobManagerBaseImpl implements IJobManager {

    private static final Logger logger = LoggerFactory.getLogger(BaseSpringBatchJobManager.class);

    public JobParameters addParameter(JobParameters parameters, String key, JobParameter parameter) {
        JobParametersBuilder builder = new JobParametersBuilder(parameters);
        return builder.addJobParameter(key, parameter).toJobParameters();
    }

    public JobParameters addParameter(JobParameters parameters, String key, String parameter) {
        return addParameter(parameters, key, new JobParameter<>(parameter, String.class));
    }

    public JobParameters addParameter(JobParameters parameters, String key, Long parameter) {
        return addParameter(parameters, key, new JobParameter<>(parameter, Long.class));
    }

    public JobParameters addParameter(JobParameters parameters, String key, Date parameter) {
        return addParameter(parameters, key, new JobParameter<>(parameter, Date.class));
    }

    public JobParameters addParameter(JobParameters parameters, String key, Double parameter) {
        return addParameter(parameters, key, new JobParameter<>(parameter, Double.class));
    }

    public JobParameters addParameter(JobParameters parameters, String key, Object parameter) {
        if (parameter instanceof String string) {
            return addParameter(parameters, key, string);
        } else if (parameter instanceof Long long1) {
            return addParameter(parameters, key, long1);
        } else if (parameter instanceof Date date) {
            return addParameter(parameters, key, date);
        } else if (parameter instanceof Double double1) {
            return addParameter(parameters, key, double1);
        } else {
            return addParameter(parameters, key, parameter.toString());
        }
    }

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    @Qualifier("batchJobMap")
    private Map<String, Job> jobs;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private JobRepository jobRepository;

    public JobRepository getJobRepository() {
        return jobRepository;
    }

    public void setJobRepository(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public JobLauncher getJobLauncher() {
        return jobLauncher;
    }

    public void setJobLauncher(JobLauncher jobLauncher) {
        this.jobLauncher = jobLauncher;
    }

    public Map<String, Job> getJobs() {
        return jobs;
    }

    public void setJobs(Map<String, Job> jobs) {
        this.jobs = jobs;
    }

    public JobExplorer getJobExplorer() {
        return jobExplorer;
    }

    public void setJobExplorer(JobExplorer jobExplorer) {
        this.jobExplorer = jobExplorer;
    }

    public JobExecution getLastJobExecution(JobInstance lastInstance) {
        List<JobExecution> executions = jobExplorer.getJobExecutions(lastInstance);
        if (executions == null || executions.isEmpty()) {
            logger.info("no prior executions found.");
            return null;
        }
        return executions.get(0);
    }

    public JobExecution getLastJobExecution(String jobName) {
        return getLastJobExecution(getLastJobInstance(jobName));
    }

    public JobInstance getLastJobInstance(String jobName) {
        List<JobInstance> instances = jobExplorer.getJobInstances(jobName, 0, 1);
        if (instances == null || instances.isEmpty()) {
            logger.info("no prior job instances found.");
            return null;
        }
        JobInstance lastInstance = instances.get(0);
        return lastInstance;
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
        jobRepository.update(lastExecution);
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
        jobRepository.update(lastStepExecution);
    }

    @Resource(name = "batchJobParameterBuilderMap")
    private Map<String, IJobParametersBuilder> parameterBuilders;

    public Map<String, IJobParametersBuilder> getParameterBuilders() {
        return parameterBuilders;
    }

    protected JobStatus getJobStatus(JobExecution execution) {
        logger.info("getting exit status for execution {}", execution.getId());
        ExitStatus status = execution.getExitStatus();
        logger.info(status.toString());

        if (status.getExitCode().equals(ExitStatus.COMPLETED.getExitCode())) {
            return JobStatus.Success;
        } else if (status.getExitCode().equals(ExitStatus.UNKNOWN.getExitCode())) {
            return JobStatus.Running;
        } else {
            return JobStatus.Failure;
        }
    }

    @Override
    public JobInformation getJobInformation(long executionID) {
        JobExecution execution = getJobExplorer().getJobExecution(executionID);
        if (execution != null) {
            JobStatus status = getJobStatus(execution);
            JobInformation info = new JobInformation();
            info.setExecutionID(executionID);
            info.setStatus(status);
            return info;
        }

        // couldn't find the thing
        JobInformation info = new JobInformation();
        info.setExecutionID(-1);
        info.setStatus(JobStatus.Unknown);
        return info;
    }
}
