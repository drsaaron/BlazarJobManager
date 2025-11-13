/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface for a component that run & manage batch jobs.
 * 
 * @author AAR1069
 * @version $Id: IJobManager.java 193 2015-12-15 17:46:42Z aar1069 $
 */
public interface IJobManager {
    
    /**
     * run a job with the given parameters.
     * 
     * @param jobName
     * @param parameters
     * @return 
     */
    public JobStatus runJob(String jobName, Map<String, Object> parameters);

    /**
     * run a job by first building the parameters from a list of strings.
     * 
     * @param jobName
     * @param args
     * @return 
     */
    public JobStatus runJob(String jobName, String[] args);

    /**
     * run a job using a parameter builder to convert the argument list to parameters.
     * 
     * @param jobName
     * @param args
     * @param parameterBuilder
     * @return 
     */
    public JobStatus runJob(String jobName, String[] args, IJobParametersBuilder parameterBuilder);
    
    /**
     * run a job with no parameters.
     * 
     * @param jobName
     * @return 
     */
    default public JobStatus runJob(String jobName) { return runJob(jobName, new HashMap<>()); }
    
    /**
     * forcibly set a job to success.  this would be needed for a restartable job
     * that has failed but for which we want the next execution to start from scratch
     * as if the prior run had been successful.
     * 
     * @param jobName 
     */
    public void forceJobToSuccess(String jobName);
    
    /**
     * force a particular step of a job to success.  This would allow the next
     * execution of the job to pick up after that failing step.
     * 
     * @param jobName
     * @param stepName 
     */
    public void forceStepToSuccess(String jobName, String stepName);
    
    /**
     * get the status of a particular execution of a job
     * 
     * @param executionID
     * @return 
     */
    public JobInformation getJobInformation(long executionID);
}
