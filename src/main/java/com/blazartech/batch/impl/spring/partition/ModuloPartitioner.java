/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl.spring.partition;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.stereotype.Component;

/**
 * A partitioner that establish a grid comprised of the divisor, which is the
 * grid size, and a remainder.
 *
 * @author AAR1069
 */
@Component
public class ModuloPartitioner implements Partitioner {

    /**
     * create an execution context for a partition
     *
     * @param i number of partitions
     * @param p this partition's number
     * @return
     */
    public ExecutionContext buildExecutionContext(int i, int p) {
        ExecutionContext context = new ExecutionContext();
        context.putInt("divisor", i);
        context.putInt("remainder", p);
        return context;
    }

    @Override
    public Map<String, ExecutionContext> partition(int i) {
        Map<String, ExecutionContext> result
                /* loop for partitions from 0 to i, building a context
                   for each partition, and up the map
                */
                = IntStream.range(0, i)
                        .mapToObj(p -> buildExecutionContext(i, p))
                        
                        /* build to a map whose key is of the form partition-1 and
                           value is the context created above for that partition.
                           Functkion.identity() is shorthand for a lambda context -> context
                        */
                        .collect(Collectors.toMap(context -> "partition-" + context.get("remainder"), Function.identity()));

        return result;
    }

}
