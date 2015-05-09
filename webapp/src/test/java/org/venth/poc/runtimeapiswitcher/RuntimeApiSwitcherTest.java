package org.venth.poc.runtimeapiswitcher;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.venth.poc.runtimeapiswitcher.test.IntegrationTest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Venth
 * please start the server with the web application manually
 */
@Category(IntegrationTest.class)
public class RuntimeApiSwitcherTest {

    private WebTarget versionResource;
    private WebTarget featureResource;

    @Test
    public void application_exposes_service_via_rest() {
        //given the server hosting the application service is running
        //please start the server with the web application manually

        //when the exposed application service is called for a version
        String serviceResponse = currentAdapterVersion();

        //then the service responded
        assertNotNull(serviceResponse);
    }

    @Test
    public void currently_active_service_adapter_is_responding() {
        //given the server hosting the application service is running

        //when the service adapter for version "1.0" is active
        String versionForActivation = "1.0";
        switchAdapterVersionTo(versionForActivation);

        //then the service responded with version 1.0
        assertEquals(versionForActivation, currentAdapterVersion());

        //and feature service is handled by version 1.0
        assertEquals("Feature handled by service version 1.0", handledFeatureResult());

    }

    @Test
    public void switched_service_is_responding() {
        //given the server hosting the application service is running

        //and the service adapter for version "1.0" is active
        String versionForActivation = "1.0";
        switchAdapterVersionTo(versionForActivation);

        //when switched to service adapter version 2.0
        String switchedVersion = "2.0";
        switchAdapterVersionTo(switchedVersion);

        //then the service responded with version 2.0
        String serviceResponse = currentAdapterVersion();
        assertEquals(switchedVersion, serviceResponse);

        //and feature service is handled by version 2.0
        assertEquals("Feature handled by service version 2.0", handledFeatureResult());
    }

    private void switchAdapterVersionTo(String version) {
        versionResource
                .request(MediaType.TEXT_PLAIN)
                .put(
                        Entity.entity(version, MediaType.TEXT_PLAIN)
                );
    }

    private String handledFeatureResult() {
        return featureResource
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);
    }

    private String currentAdapterVersion() {
        return versionResource
                .request(MediaType.TEXT_PLAIN)
                .get(String.class);
    }

    @Before
    public void setUp() {
        Client client = JerseyClientBuilder.newClient();
        WebTarget applicationService = client.target("http://localhost:8080/application-service");
        versionResource = applicationService.path("version");
        featureResource = applicationService.path("feature");
    }
}
