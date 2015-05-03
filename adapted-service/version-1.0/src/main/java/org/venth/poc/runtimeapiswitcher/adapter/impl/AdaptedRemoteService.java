package org.venth.poc.runtimeapiswitcher.adapter.impl;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.venth.poc.runtimeapiswitcher.adapter.api.AdaptedService;

/**
 * @author Venth on 01/05/2015
 */
@Component
@Provides
public class AdaptedRemoteService implements AdaptedService {
    @Override
    public String version() {
        return "1.0";
    }

    @Override
    public String feature() {
        return "feature handled by service version 1.0";
    }
}
