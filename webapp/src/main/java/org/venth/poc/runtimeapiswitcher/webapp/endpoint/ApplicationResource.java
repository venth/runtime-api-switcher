package org.venth.poc.runtimeapiswitcher.webapp.endpoint;

import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author Venth
 */
@Component
@Path("/application-service")
public class ApplicationResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/version")
    public String getVersion() {
        return "unknown version";
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/version")
    public void switchAdapterVersionTo(String version) {

    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/feature")
    public String handleFeatureByAdapter() {
        return "";
    }
}
