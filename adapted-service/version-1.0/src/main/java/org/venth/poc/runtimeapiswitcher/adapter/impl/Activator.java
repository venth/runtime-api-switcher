package org.venth.poc.runtimeapiswitcher.adapter.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.venth.poc.runtimeapiswitcher.api.adapter.AdaptedService;

/**
 * @author Venth on 03/05/2015
 */
public class Activator implements BundleActivator {
    private ServiceRegistration<AdaptedService> adaptedServiceRegistration;
    private ClassPathXmlApplicationContext springContext;

    @Override
    public void start(BundleContext context) throws Exception {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            springContext = new ClassPathXmlApplicationContext("classpath:/context-ver_1.xml");
            adaptedServiceRegistration = context.registerService(
                    AdaptedService.class,
                    springContext.getBean(AdaptedService.class),
                    null
            );
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        springContext.close();
        adaptedServiceRegistration.unregister();
    }
}
