/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nm.ffba.common.batch.impl.spring.partition.writer;

import java.util.Collection;

/**
 * an interface for a collection of objects.  this will be needed for partitioned
 * writers.
 * 
 * @author aar1069
 * @param <T>
 */
public interface IObjectSet<T> {
    
    public void setObjects(Collection<T> objects);
    public Collection<T> getObjects();
    
    /**
     * Create a new object of this type.  This method should return an empty object
     * so it is not the same as the clone method.
     * 
     * @return 
     */
    public IObjectSet<T> createNew();
}
