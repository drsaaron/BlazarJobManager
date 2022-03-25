/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch;

import java.io.Serializable;

/**
 *
 * @author AAR1069
 */
public class JobInformation implements Serializable {
 
    private JobStatus status;
    private long executionID;

    public long getExecutionID() {
        return executionID;
    }

    public void setExecutionID(long executionID) {
        this.executionID = executionID;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    
}
