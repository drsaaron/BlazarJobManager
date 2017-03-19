/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nm.ffba.common.batch;

import java.util.Map;

/**
 * Interface for a component that will convert a string of arguments into a
 * set of parameters appropriate for batch execution.
 * 
 * @author aar1069
 */
public interface IJobParametersBuilder {
    
    public Map<String, Object> buildJobParameters(String[] arguments);
}
