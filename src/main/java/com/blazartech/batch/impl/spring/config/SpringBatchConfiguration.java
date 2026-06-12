/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.blazartech.batch.impl.spring.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.batch.autoconfigure.BatchAutoConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author scott
 */
@Configuration
@EnableAutoConfiguration(exclude = BatchAutoConfiguration.class)
@EnableBatchProcessing
public class SpringBatchConfiguration {
    
}
