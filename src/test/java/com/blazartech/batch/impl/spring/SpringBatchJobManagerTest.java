/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package com.blazartech.batch.impl.spring;

import com.blazartech.batch.IJobParametersBuilder;
import com.blazartech.batch.JobStatus;
import com.blazartech.batch.impl.spring.config.JobParametersIncrementerConfiguration;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersIncrementer;
import org.springframework.batch.core.job.parameters.JobParametersValidator;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
    
    @MockitoBean
    private JobRepository jobRepository;
    
    @MockitoBean
    private JobOperator jobOperator;
    
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
        
        JobExecution executionNoop = new JobExecution(1L, instance1, null);
        executionNoop.setExitStatus(ExitStatus.NOOP);
        JobExecution executionCompleted = new JobExecution(200L, instance2, null); // needs to be highest number as completed would always be last
        executionCompleted.setExitStatus(ExitStatus.COMPLETED);
        JobExecution executionFailed = new JobExecution(3L, instance3, null);
        executionFailed.setExitStatus(ExitStatus.FAILED);
        JobExecution executionStopped = new JobExecution(4L, instance3, null);
        executionStopped.setExitStatus(ExitStatus.STOPPED);
        
        Mockito.when(jobRepository.getJobInstances(TEST_JOB_NAME, 0, 1))
                .thenReturn(List.of(instance1));
        
        Mockito.when(jobRepository.getJobInstances(TEST_FAIL_JOB_NAME, 0, 1))
                .thenReturn(List.of(instance2));
        
        Mockito.when(jobRepository.getJobInstances(TEST_STOPPED_JOB_NAME, 0, 1))
                .thenReturn(List.of(instance3));
        
        Mockito.when(jobRepository.getJobExecutions(instance1))
	    .thenReturn(List.of(executionNoop, executionFailed, executionCompleted));
        Mockito.when(jobRepository.getJobExecutions(instance2))
                .thenReturn(List.of(executionNoop, executionFailed));
        Mockito.when(jobRepository.getJobExecutions(instance3))
                .thenReturn(List.of(executionNoop, executionStopped));
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
    
    @Test
    public void testBuildJobParameters() {
        
        logger.info("buildJobParameters");
        
        Map<String, Object> parametersMap = Map.of("first", 1L, "second", 2L, "third", "three");
        JobParameters jp = instance.buildJobParameters(parametersMap);
        
        assertEquals(parametersMap.size(), jp.parameters().size());
        assertEquals(parametersMap.get("first"), jp.getLong("first"));
        assertEquals(parametersMap.get("third"), jp.getString("third"));
    }
    
    @Test
    public void testFindStepExecution_found() {
        
        logger.info("findStepExecution_found");
        
        StepExecution ex1 = new StepExecution(1, "step1", null);
        StepExecution ex2 = new StepExecution(2, "step2", null);
        StepExecution ex3 = new StepExecution(3, "step3", null);
        
        Collection<StepExecution> stepExecutions = List.of(ex1, ex2, ex3);
        
        StepExecution ex2found = instance.findStepExecution(stepExecutions, "step2");
        assertNotNull(ex2found);
        assertEquals(ex2.getId(), ex2found.getId());
    }
    
    @Test
    public void testFindStepExecution_notFound() {
        
        logger.info("findStepExecution_notFound");
        
        StepExecution ex1 = new StepExecution(1, "step1", null);
        StepExecution ex2 = new StepExecution(2, "step2", null);
        StepExecution ex3 = new StepExecution(3, "step3", null);
        
        Collection<StepExecution> stepExecutions = List.of(ex1, ex2, ex3);
        
        StepExecution ex4found = instance.findStepExecution(stepExecutions, "step4");
        assertNull(ex4found);
    }
    
    private void testJobStatus(ExitStatus exitStatus, JobStatus expectedJobStatus) {
        
        JobInstance ji = new JobInstance(1L, "testJob");
        JobExecution je = new JobExecution(1L, ji, null);
        je.setExitStatus(exitStatus);
        
        JobStatus status = instance.getJobStatus(je);
        
        assertEquals(expectedJobStatus, status);
    }
    
    @Test
    public void testGetJobStatus() {
        logger.info("getJobStatus");
        
        testJobStatus(ExitStatus.COMPLETED, JobStatus.Success);
        testJobStatus(ExitStatus.EXECUTING, JobStatus.Failure);
        testJobStatus(ExitStatus.UNKNOWN, JobStatus.Running);
        testJobStatus(ExitStatus.FAILED, JobStatus.Failure);
        testJobStatus(ExitStatus.STOPPED, JobStatus.Stopped);
        testJobStatus(ExitStatus.NOOP, JobStatus.Failure);
    }
    
    @Test
    public void testAddParameter() {
        logger.info("addParameter");
        
        JobParameters jp = new JobParameters();
        
        jp = instance.addParameter(jp, "testString", "myValue");
        
        assertEquals(1, jp.getIdentifyingParameters().size());
        assertTrue(jp.getParameter("testString").type() == String.class);
        
        jp = instance.addParameter(jp, "testDouble", 3.14159);
        
        assertEquals(2, jp.getIdentifyingParameters().size());
        assertTrue(jp.getParameter("testDouble").type() == Double.class);
        
        jp = instance.addParameter(jp, "testDate", LocalDate.parse("2026-12-31"));
        
        assertEquals(3, jp.getIdentifyingParameters().size());
        assertTrue(jp.getParameter("testDate").type() == String.class);
        
        final JobParameters jp2 = jp;
        assertThrows(IllegalArgumentException.class, () -> instance.addParameter(jp2, "throwMe", null));
    }
}