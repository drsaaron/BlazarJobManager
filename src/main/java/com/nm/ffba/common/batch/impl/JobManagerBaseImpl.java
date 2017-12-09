/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nm.ffba.common.batch.impl;

import com.nm.ffba.common.batch.IJobManager;
import com.nm.ffba.common.batch.JobStatus;
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
