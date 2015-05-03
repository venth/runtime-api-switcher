package org.venth.poc.runtimeapiswitcher.webapp.endpoint;

import org.springframework.stereotype.Component;
import org.venth.poc.runtimeapiswitcher.api.adapter.AdaptedService;
import org.venth.poc.runtimeapiswitcher.api.adapter.Holder;

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

    private Map<String, AdaptedService> services = Collections.emptyMap();
    private AdaptedService version_unknown = new AdaptedService() {

        @Override
        public String version() {
            return "unknown";
        }

        @Override
        public Set<String> supports() {
            return Collections.emptySet();
        }

        @Override
        public Holder feature(Holder holder) {
            return new Holder("wrong handling adapter");
        }
    };

    private AtomicReference<AdaptedService> activeService = new AtomicReference<>(version_unknown);


    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/version")
    public String getVersion() {
        return activeService.get().version();
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/version")
    public void switchAdapterVersionTo(String version) {
        Optional<AdaptedService> adaptedService = Optional.ofNullable(services.get(version));
        activeService.set(adaptedService.orElse(version_unknown));
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/feature")
    public String handleFeatureByAdapter() {
        return activeService.get().feature(new Holder("Feature handled by service version")).message;
    }

    @Resource(name = "adaptedServices")
    public void setAdaptedServices(Map<String, AdaptedService> services) {
        this.services = services;
    }
}
