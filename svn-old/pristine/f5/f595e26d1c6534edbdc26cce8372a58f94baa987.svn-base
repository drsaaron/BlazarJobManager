/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nm.ffba.common.batch.impl.spring;

import com.nm.ffba.common.batch.IJobManager;
import com.nm.ffba.common.batch.IJobParametersBuilder;
import com.nm.ffba.common.batch.JobStatus;
import com.nm.ffba.common.batch.impl.JobManagerBaseImpl;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
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
@Component
public class SpringBatchJobManager extends JobManagerBaseImpl implements IJobManager {

    private static final Logger logger = Logger.getLogger(SpringBatchJobManager.class);

    private JobParameters addParameter(JobParameters parameters, String key, JobParameter parameter) {
        JobParametersBuilder builder = new JobParametersBuilder(parameters);
        return builder.addParameter(key, parameter).toJobParameters();
    }

    private JobParameters addParameter(JobParameters parameters, String key, String parameter) {
        return addParameter(parameters, key, new JobParameter(parameter));
    }

    private JobParameters addParameter(JobParameters parameters, String key, Long parameter) {
        return addParameter(parameters, key, new JobParameter(parameter));
    }

    private JobParameters addParameter(JobParameters parameters, String key, Date parameter) {
        return addParameter(parameters, key, new JobParameter(parameter));
    }

    private JobParameters addParameter(JobParameters parameters, String key, Double parameter) {
        return addParameter(parameters, key, new JobParameter(parameter));
    }

    private JobParameters addParameter(JobParameters parameters, String key, Object parameter) {
        if (parameter instanceof String) {
            return addParameter(parameters, key, (String) parameter);
        } else if (parameter instanceof Long) {
            return addParameter(parameters, key, (Long) parameter);
        } else if (parameter instanceof Date) {
            return addParameter(parameters, key, (Date) parameter);
        } else if (parameter instanceof Double) {
            return addParameter(parameters, key, (Double) parameter);
        } else {
            return addParameter(parameters, key, parameter.toString());
        }
    }

    @Autowired
    private JobLauncher jobLauncher;
    
    private Map<String, Job> jobs;
    
    @Autowired
    private JobExplorer jobExplorer;
    
    @Autowired
    private JobParametersIncrementer incrementer;
    
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

    public JobParametersIncrementer getIncrementer() {
        return incrementer;
    }

    public void setIncrementer(JobParametersIncrementer incrementer) {
        this.incrementer = incrementer;
    }

    private JobInstance getLastJobInstance(String jobName) {
        List<JobInstance> instances = jobExplorer.getJobInstances(jobName, 0, 1);
        if (instances == null || instances.isEmpty()) {
            logger.info("no prior job instances found.");
            return null;
        }
        JobInstance lastInstance = instances.get(0);
        return lastInstance;
    }

    private JobExecution getLastJobExecution(JobInstance lastInstance) {
        List<JobExecution> executions = jobExplorer.getJobExecutions(lastInstance);
        if (executions == null || executions.isEmpty()) {
            logger.info("no prior executions found.");
            return null;
        }
        return executions.get(0);
    }

    private JobExecution getLastJobExecution(String jobName) {
        return getLastJobExecution(getLastJobInstance(jobName));
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

    private boolean isNewInstanceNeeded(Job job) {
        logger.info("checking prior runs of the job to determine if new instance needed");

        // is the job even restartable?
        if (!job.isRestartable()) {
            logger.info("job is not restartable, so a new instance is definitely needed.");
            return true;
        }
        
        // a restartable job
        String jobIdentifier = job.getName();
        List<JobInstance> lastInstances = jobExplorer.getJobInstances(jobIdentifier, 0, 1);
        for (JobInstance instance : lastInstances) {
            List<JobExecution> lastExecutions = jobExplorer.getJobExecutions(instance);
            for (JobExecution execution : lastExecutions) {
                ExitStatus exitStatus = execution.getExitStatus();
                switch (exitStatus.getExitCode()) {
                    case "COMPLETED":
                        logger.info("job completed successfully, so new instance needed");
                        return true;
                    case "NOOP":
                        logger.info("status is noop, continuing");
                        break;
                    case "FAILED":
                        logger.info("status failed, no new instance");
                        return false;
                    default:
                        logger.info("unexpected status: " + exitStatus);
                        break;
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
                long lastRunID = lastRunParameters.getLong("run.id");
                parameters = addParameter(parameters, "run.id", lastRunID);
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
}
