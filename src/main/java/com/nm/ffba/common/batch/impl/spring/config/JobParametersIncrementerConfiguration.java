/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nm.ffba.common.batch.impl.spring.config;

import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author scott
 */
@Configuration
public class JobParametersIncrementerConfiguration {
    
    @Bean
    public JobParametersIncrementer getJobParametersIncrementer() {
        return new RunIdIncrementer();
    }
}
