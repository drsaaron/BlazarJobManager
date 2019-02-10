/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl.spring.config;

import com.blazartech.products.crypto.BlazarCryptoFile;
import javax.sql.DataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author AAR1069
 */
@Configuration
public class JobRepositoryDataSourceConfiguration {

    private static final Logger logger = Logger.getLogger(JobRepositoryDataSourceConfiguration.class);

    @Value("${batch.job.repos.userID}")
    private String userID;

    @Value("${batch.job.repos.resourceID}")
    private String resourceID;

    @Value("${batch.job.repos.url}")
    private String url;

    @Value("${batch.job.repos.driverClass}")
    private String driverClass;

    @Value("${batch.job.repos.poolSize}")
    private int poolSize;

    @Autowired
    private BlazarCryptoFile cryptoFile;

    @Bean(name = "JobRepositoryDataSource", destroyMethod = "")
    public DataSource getJobRepositoryDataSource() {
        logger.info("building data source for " + url + " via dbcp");
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName(driverClass);
        ds.setUrl(url);
        ds.setUsername(userID);
        ds.setPassword(cryptoFile.getPassword(userID, resourceID));
        ds.setInitialSize(poolSize);
        ds.setMaxTotal(poolSize);
        return ds;
    }
}
