/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl.spring.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 *
 * @author AAR1069
 */
@Configuration
public class TransactionManagerConfiguration {
    
    @Autowired
    @Qualifier("JobRepositoryDataSource")
    private DataSource jobRepositoryDataSource;
    
    @Bean
    public PlatformTransactionManager jobRepositoryTransactionManager() {
        DataSourceTransactionManager manager = new DataSourceTransactionManager();
        manager.setDataSource(jobRepositoryDataSource);
        return manager;
    }
}
