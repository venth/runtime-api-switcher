package org.venth.poc.runtimeapiswitcher.adapter.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.venth.poc.runtimeapiswitcher.api.adapter.AdaptedService;

/**
 * @author Venth on 03/05/2015
 */
public class Activator implements BundleActivator {
    private ServiceRegistration<AdaptedService> adaptedServiceRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        adaptedServiceRegistration = context.registerService(AdaptedService.class, new AdaptedRemoteService(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        adaptedServiceRegistration.unregister();
    }
}
