/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl.spring.config;

import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.dao.JacksonExecutionContextStringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

/**
 *
 * @author AAR1069
 */
@Configuration
public class DefaultSerializerConfiguration {
    
    @Bean
    public ExecutionContextSerializer batchDefaultSerializer() {
        /* going to use the jackson serializer, but we need to include com.blazrtech
           objects.  So basically do what the default constuctor
           does and include our own types.
        */
        PolymorphicTypeValidator b = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("java.util.")
                .allowIfSubType("java.sql.")
                .allowIfSubType("java.lang.")
                .allowIfSubType("java.math.")
                .allowIfSubType("java.time.")
                .allowIfSubType("java.net.")
                .allowIfSubType("java.xml.")
                .allowIfSubType("org.springframework.batch.")
                .allowIfSubType("com.blazartech.")
                .build();
        JsonMapper m = JsonMapper.builder().activateDefaultTyping(b).build();
        return new JacksonExecutionContextStringSerializer(m);
    }
}
