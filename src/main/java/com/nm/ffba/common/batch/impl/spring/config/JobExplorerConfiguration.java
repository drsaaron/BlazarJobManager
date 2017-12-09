/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nm.ffba.common.batch.impl.spring.config;

import javax.sql.DataSource;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.repository.ExecutionContextSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author AAR1069
 */
@Configuration
public class JobExplorerConfiguration {
    
    @Autowired
    @Qualifier("JobRepositoryDataSource")
    private DataSource jobRepositoryDataSource;
    
    @Autowired
    @Qualifier("batchDefaultSerializer")
    private ExecutionContextSerializer batchDefaultSerializer;
    
    @Value("${batch.job.repos.tablePrefix}")
    private String prefix;
    
    @Bean(name = "jobExplorer") 
    public JobExplorerFactoryBean getJobExplorer() {
        JobExplorerFactoryBean je = new JobExplorerFactoryBean();
        je.setDataSource(jobRepositoryDataSource);
        je.setSerializer(batchDefaultSerializer);
        je.setTablePrefix(prefix);
        return je;
    }
}
