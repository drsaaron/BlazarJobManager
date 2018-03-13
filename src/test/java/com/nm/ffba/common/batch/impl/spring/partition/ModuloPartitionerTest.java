/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nm.ffba.common.batch.impl.spring.partition;

import java.util.Map;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *
 * @author scott
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
    ModuloPartitionerTest.ModuloPartitionerTestConfiguration.class
})
public class ModuloPartitionerTest {
    
    private static final Logger logger = Logger.getLogger(ModuloPartitionerTest.class);
    
    static class ModuloPartitionerTestConfiguration {
        
        @Bean
        public ModuloPartitioner getModuloPartitioner() {
            return new ModuloPartitioner();
        }
    }
    
    @Autowired
    private ModuloPartitioner moduloPartitioner;
    
    public ModuloPartitionerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    static final int partitionSize = 10;
    
    /**
     * Test of partition method, of class ModuloPartitioner.
     */
    @Test
    public void testPartitionSize() {
        logger.info("testPartitionSize");
        
        Map<String, ExecutionContext> result = moduloPartitioner.partition(partitionSize);
        
        logger.debug("testing partition size");
        assertEquals(partitionSize, result.keySet().size());
    }

    @Test
    public void testPartitionSetup() {
        logger.info("testPartitionSetup");
        
        Map<String, ExecutionContext> result = moduloPartitioner.partition(partitionSize);
        
        for (int p = 0; p < partitionSize; p++) {
            ExecutionContext context = result.get("partition-" + p);
            assertNotNull(context);
            
            assertEquals(partitionSize, context.getInt("divisor"));
            assertEquals(p, context.getInt("remainder"));
        }
    }
}
