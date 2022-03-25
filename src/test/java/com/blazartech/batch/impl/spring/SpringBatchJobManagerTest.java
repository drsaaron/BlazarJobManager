/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package com.blazartech.batch.impl.spring;

import com.blazartech.batch.IJobParametersBuilder;
import com.blazartech.batch.impl.spring.config.JobParametersIncrementerConfiguration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 *
 * @author aar1069
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    SpringBatchJobManagerTest.SpringBatchJobManagerTestConfiguration.class,
    JobParametersIncrementerConfiguration.class
})
public class SpringBatchJobManagerTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SpringBatchJobManagerTest.class);
    
    @Configuration
    static class SpringBatchJobManagerTestConfiguration {
        
        @Bean
        public SpringBatchJobManager instance() {
            return new SpringBatchJobManager();
        }
        
        @Bean("batchJobMap")
        public Map<String, Job> batchJobMap() {
            return new HashMap<>();
        }
        
        @Bean("batchJobParameterBuilderMap")
        public Map<String, IJobParametersBuilder> batchJobParameterBuilderMap() {
            return new HashMap<>();
        }
    }
    
    @Autowired
    private SpringBatchJobManager instance;
    
    @MockBean
    private JobExplorer jobExplorer;
    
    @MockBean
    private JobRepository jobRepository;
    
    @MockBean
    private JobLauncher jobLauncher;
    
    public SpringBatchJobManagerTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
        JobInstance instance1 = new JobInstance(1L, TEST_JOB_NAME);
        JobInstance instance2 = new JobInstance(2L, TEST_FAIL_JOB_NAME);
        JobInstance instance3 = new JobInstance(3L, TEST_STOPPED_JOB_NAME);
        
        JobExecution execution1 = new JobExecution(1L);
        execution1.setExitStatus(ExitStatus.NOOP);
        JobExecution execution2 = new JobExecution(2L);
        execution2.setExitStatus(ExitStatus.COMPLETED);
        JobExecution execution3 = new JobExecution(3L);
        execution3.setExitStatus(ExitStatus.FAILED);
        JobExecution execution4 = new JobExecution(4L);
        execution4.setExitStatus(ExitStatus.STOPPED);
        
        Mockito.when(jobExplorer.getJobInstances(TEST_JOB_NAME, 0, 1))
                .thenReturn(List.of(instance1));
        
        Mockito.when(jobExplorer.getJobInstances(TEST_FAIL_JOB_NAME, 0, 1))
                .thenReturn(List.of(instance2));
        
        Mockito.when(jobExplorer.getJobInstances(TEST_STOPPED_JOB_NAME, 0, 1))
                .thenReturn(List.of(instance3));
        
        Mockito.when(jobExplorer.getJobExecutions(instance1))
                .thenReturn(List.of(execution1, execution2));
        Mockito.when(jobExplorer.getJobExecutions(instance2))
                .thenReturn(List.of(execution1, execution3));
        Mockito.when(jobExplorer.getJobExecutions(instance3))
                .thenReturn(List.of(execution1, execution4));
    }
    
    @AfterEach
    public void tearDown() {
    }

    private static final String TEST_JOB_NAME = "TestJob";
    private static final String TEST_FAIL_JOB_NAME = "TestFailJob";
    private static final String TEST_STOPPED_JOB_NAME = "TestStoppedJob";
    
    static class TestJob implements Job {

        public TestJob(String name, boolean restartable) {
            this.name = name;
            this.restartable = restartable;
        }
        
        private final String name;
        private final boolean restartable;
        
        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isRestartable() {
            return restartable;
        }

        @Override
        public void execute(JobExecution je) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public JobParametersIncrementer getJobParametersIncrementer() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public JobParametersValidator getJobParametersValidator() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
    @Test
    public void testIsNewInstanceNeeded_success() {
        
        logger.info("isNewInstanceNeeded_success");
        
        Job job = new TestJob(TEST_JOB_NAME, true);
        boolean result = instance.isNewInstanceNeeded(job);
        assertEquals(true, result);
    }
    
    @Test
    public void testIsNewInstanceNeeded_failed() {
        
        logger.info("isNewInstanceNeeded_failed");
        
        Job job = new TestJob(TEST_FAIL_JOB_NAME, true);
        boolean result = instance.isNewInstanceNeeded(job);
        assertEquals(false, result);
    }
    
    @Test
    public void testIsNewInstanceNeeded_stopped() {
        
        logger.info("isNewInstanceNeeded_stopped");
        
        Job job = new TestJob(TEST_STOPPED_JOB_NAME, true);
        boolean result = instance.isNewInstanceNeeded(job);
        assertEquals(false, result);
    }
    
    @Test
    public void testIsNewInstanceNeeded_nonrestartable() {
        
        logger.info("isNewInstanceNeeded_restartable");
        
        Job job = new TestJob(TEST_FAIL_JOB_NAME, false);
        boolean result = instance.isNewInstanceNeeded(job);
        assertEquals(true, result);
    }
}
