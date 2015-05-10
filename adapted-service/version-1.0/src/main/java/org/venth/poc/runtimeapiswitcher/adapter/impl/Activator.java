package org.venth.poc.runtimeapiswitcher.adapter.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.venth.poc.runtimeapiswitcher.api.adapter.AdaptedService;
import org.venth.poc.runtimeapiswitcher.osgi.OsgiBundleResourceResolver;

/**
 * @author Venth on 03/05/2015
 */
@SuppressWarnings("PackageAccessibility")
public class Activator implements BundleActivator {
    private ServiceRegistration<AdaptedService> adaptedServiceRegistration;
    private ClassPathXmlApplicationContext springContext;

    @Override
    public void start(BundleContext context) throws Exception {
        preserveContextAndRun(() -> {
            springContext = new ClassPathXmlApplicationContext("classpath:/context-ver_1.xml") {
                @Override
                protected ResourcePatternResolver getResourcePatternResolver() {
                    return new OsgiBundleResourceResolver();
                }
            };
            adaptedServiceRegistration = context.registerService(
                    AdaptedService.class,
                    springContext.getBean(AdaptedService.class),
                    null
            );
        });
    }

    private void preserveContextAndRun(Runnable runnable) {
        Thread currentThread = Thread.currentThread();
        ClassLoader contextClassLoader = currentThread.getContextClassLoader();
        try {
            currentThread.setContextClassLoader(getClass().getClassLoader());
            runnable.run();
        } finally {
            currentThread.setContextClassLoader(contextClassLoader);
        }

    }

    @Override
    public void stop(BundleContext context) throws Exception {
        springContext.close();
        adaptedServiceRegistration.unregister();
    }
}
