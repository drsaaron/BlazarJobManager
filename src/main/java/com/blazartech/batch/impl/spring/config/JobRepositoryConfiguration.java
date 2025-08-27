/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl.spring.config;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 *
 * @author AAR1069
 */
@Configuration
public class JobRepositoryConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(JobRepositoryConfiguration.class);
    
    /*
    <batch:job-repository data-source="JobRepositoryDataSource" transaction-manager="transactionManager" serializer="batchDefaultSerializer" /> 
     */

    @Autowired
    @Qualifier("JobRepositoryDataSource")
    private DataSource jobRepositoryDataSource;

    @Autowired
    private PlatformTransactionManager jobRepositoryTransactionManager;

    @Autowired
    private ExecutionContextSerializer batchDefaultSerializer;

    @Value("${batch.job.repos.tablePrefix}")
    private String tablePrefix;

    @Bean
    public JobRepository jobRepository() throws Exception {
        logger.info("building job repo");
        JobRepositoryFactoryBean jr = new JobRepositoryFactoryBean();
        jr.setDataSource(jobRepositoryDataSource);
        jr.setSerializer(batchDefaultSerializer);
        jr.setTransactionManager(jobRepositoryTransactionManager);
        jr.setTablePrefix(tablePrefix);
        jr.afterPropertiesSet();
        return jr.getObject();
    }
}
