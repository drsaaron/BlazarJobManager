/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nm.ffba.common.batch.main;

import com.nm.ffba.common.batch.IJobManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 * @author AAR1069
 */
public class SpringHelper {
    
    public static SpringHelper instance(String configFileName) { 
        return new SpringHelper(configFileName);
    }
    
    private final ApplicationContext context;
    
    private SpringHelper(String configFileName) {
        context = new ClassPathXmlApplicationContext(configFileName);
    }
    
    private Object getBean(String name) {
        return context.getBean(name);
    }
    
    public IJobManager getJobManager() {
        return (IJobManager) getBean("jobManager");
    }
}
