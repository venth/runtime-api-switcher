package org.venth.poc.runtimeapiswitcher.webapp.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.venth.poc.runtimeapiswitcher.api.adapter.AdaptedService;
import org.venth.poc.runtimeapiswitcher.api.adapter.Holder;
import org.venth.poc.runtimeapiswitcher.webapp.osgi.AdapterVersionSwitchRequested;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Venth
 */
@Component
@Path("/application-service")
public class ApplicationResource {


    private ApplicationEventPublisher eventPublisher;
    private AdaptedService adaptedService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/version")
    public String getVersion() {
        return adaptedService.version();
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/version")
    public void switchAdapterVersionTo(String version) {
        eventPublisher.publishEvent(new AdapterVersionSwitchRequested(version));
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/feature")
    public String handleFeatureByAdapter() {
        return adaptedService.feature(new Holder("Feature handled by service version")).message;
    }

    @Autowired
    public void setAdaptedService(AdaptedService adaptedService) {
        this.adaptedService = adaptedService;
    }

    @Autowired
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
}
