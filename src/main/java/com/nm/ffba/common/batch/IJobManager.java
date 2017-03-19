/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nm.ffba.common.batch;

import java.util.Map;

/**
 * Interface for a component that run & manage batch jobs.
 * 
 * @author AAR1069
 * @version $Id: IJobManager.java 13 2016-10-30 21:11:25Z scott $
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
    public JobStatus runJob(String jobName);
    
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
}
