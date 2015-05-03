package org.venth.poc.runtimeapiswitcher.webapp.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * @author Venth on 02/05/2015
 */
public class OsgiServiceProxy<SERVICE> extends AbstractFactoryBean<SERVICE> {

    private Class<SERVICE> serviceType;
    private BundleContext context;

    private ServiceReference<SERVICE> serviceReference;

    public void setContext(BundleContext context) {
        this.context = context;
    }

    public void setServiceType(Class<SERVICE> serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public Class<?> getObjectType() {
        return serviceType;
    }

    @Override
    protected SERVICE createInstance() throws Exception {
        serviceReference = context.getServiceReference(serviceType);
        return context.getService(serviceReference);
    }

    public void stop() {
        boolean neededToReturnServiceToContainer = serviceReference != null;
        if (neededToReturnServiceToContainer) {
            context.ungetService(serviceReference);
        }
    }
}
