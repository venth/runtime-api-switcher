package org.venth.poc.runtimeapiswitcher.webapp.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.venth.poc.runtimeapiswitcher.api.adapter.FeatureSupport;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author Venth on 02/05/2015
 */
public class OsgiServiceProxy<SERVICE extends FeatureSupport> extends AbstractFactoryBean<SERVICE> {

    private Class<SERVICE> serviceType;
    private BundleContext context;

    private ServiceReference<SERVICE> serviceReference;
    private Set<String> requiredFeatures = Collections.emptySet();

    public void setRequiredFeatures(Set<String> requiredFeatures) {
        this.requiredFeatures = requiredFeatures;
    }

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
        Collection<ServiceReference<SERVICE>> serviceReferences = context.getServiceReferences(serviceType, null);

        serviceReference = serviceReferences.stream()
                .filter(whenRequiredFeaturesAreDefined())
                .filter(serviceSupportedAllRequiredFeatures())
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException("No service supports: " + String.join(", ", requiredFeatures))
                );

        return context.getService(serviceReference);
    }

    private Predicate<ServiceReference<SERVICE>> serviceSupportedAllRequiredFeatures() {
        return serviceRef -> {
            Set<String> supportedFeatures;
            FeatureSupport service;
            try {
                service = context.getService(serviceRef);
                supportedFeatures = service.supports();
            } finally {
                context.ungetService(serviceRef);
            }
            boolean requirementsMet = requiredFeatures.containsAll(supportedFeatures);

            return requirementsMet;
        };
    }

    private Predicate<ServiceReference<SERVICE>> whenRequiredFeaturesAreDefined() {
        return serviceRef -> !requiredFeatures.isEmpty();
    }

    public void stop() {
        boolean neededToReturnServiceToContainer = serviceReference != null;
        if (neededToReturnServiceToContainer) {
            context.ungetService(serviceReference);
        }
    }
}
