/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl;

import com.blazartech.batch.IJobManager;
import com.blazartech.batch.JobStatus;
import java.util.HashMap;

/**
 * Base class for any job manager implementation.
 * 
 * @author AAR1069
 */
public abstract class JobManagerBaseImpl implements IJobManager {

    @Override
    public JobStatus runJob(String jobName) {
        return runJob(jobName, new HashMap<>());
    }
    
}
