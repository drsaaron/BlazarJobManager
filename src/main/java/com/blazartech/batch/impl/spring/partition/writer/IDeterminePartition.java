/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blazartech.batch.impl.spring.partition.writer;

/**
 * interface for a component that will determine the partition number to be used
 * for an object.  The partition grid consists of a set of partitions numbered 0
 * to some maximum value.  This component will determine into which of those a given
 * object will be placed.
 * 
 * @author aar1069
 * @param <T> type of object to be partitioned
 */
public interface IDeterminePartition<T> {
    
    public int determinePartition(T object);
}
