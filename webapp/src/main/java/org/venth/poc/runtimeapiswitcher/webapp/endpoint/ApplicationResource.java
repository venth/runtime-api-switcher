package org.venth.poc.runtimeapiswitcher.webapp.endpoint;

import org.springframework.stereotype.Component;
import org.venth.poc.runtimeapiswitcher.adapter.api.AdaptedService;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Venth
 */
@Component
@Path("/application-service")
public class ApplicationResource {

    private Map<String, AdaptedService> services = new HashMap<>();
    private AdaptedService version_unknown = new AdaptedService() {

        @Override
        public String version() {
            return "unknown version";
        }

        @Override
        public String feature() {
            return "wrong handling adapter";
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
        return activeService.get().feature();
    }
}
