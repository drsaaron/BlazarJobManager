/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl.spring.partition;

import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 *
 * @author scott
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
    ModuloPartitionerTest.ModuloPartitionerTestConfiguration.class
})
public class ModuloPartitionerTest {
    
    private static final Logger logger = LoggerFactory.getLogger(ModuloPartitionerTest.class);
    
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
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    private static final int PARTITION_SIZE = 10;
    
    /**
     * Test of partition method, of class ModuloPartitioner.
     */
    @Test
    public void testPartitionSize() {
        logger.info("testPartitionSize");
        
        Map<String, ExecutionContext> result = moduloPartitioner.partition(PARTITION_SIZE);
        
        logger.debug("testing partition size");
        assertEquals(PARTITION_SIZE, result.keySet().size());
    }

    @Test
    public void testPartitionSetup() {
        logger.info("testPartitionSetup");
        
        Map<String, ExecutionContext> result = moduloPartitioner.partition(PARTITION_SIZE);
        
        for (int p = 0; p < PARTITION_SIZE; p++) {
            ExecutionContext context = result.get("partition-" + p);
            assertNotNull(context);
            
            assertEquals(PARTITION_SIZE, context.getInt("divisor"));
            assertEquals(p, context.getInt("remainder"));
        }
    }
}
