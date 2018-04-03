/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl.spring.partition;

import java.util.HashMap;
import java.util.Map;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

/**
 * A partitioner that establish a grid comprised of the divisor, which is the 
 * grid size, and a remainder.
 * 
 * @author AAR1069
 */
@Component
public class ModuloPartitioner implements Partitioner {

    @Override
    public Map<String, ExecutionContext> partition(int i) {
        Map<String, ExecutionContext> result = new HashMap<>();
        
        for (int p = 0; p < i; p++) {
            ExecutionContext context = new ExecutionContext();
            context.putInt("divisor", i);
            context.putInt("remainder", p);
            result.put("partition-" + p, context);
        }
        
        return result;
    }
    
}
