/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl.spring.config;

import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.Jackson2ExecutionContextStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author AAR1069
 */
@Configuration
public class DefaultSerializerConfiguration {
    
    @Bean
    public ExecutionContextSerializer batchDefaultSerializer() {
        return new Jackson2ExecutionContextStringSerializer();
    }
}
