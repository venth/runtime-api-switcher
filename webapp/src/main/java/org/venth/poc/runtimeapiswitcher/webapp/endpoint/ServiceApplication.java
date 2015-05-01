package org.venth.poc.runtimeapiswitcher.webapp.endpoint;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * @author Venth
 */
public class ServiceApplication extends ResourceConfig {
    public ServiceApplication() {

        //resources
        register(ApplicationResource.class);
    }
}
