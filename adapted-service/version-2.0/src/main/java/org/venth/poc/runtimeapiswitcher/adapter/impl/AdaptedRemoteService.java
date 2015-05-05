package org.venth.poc.runtimeapiswitcher.adapter.impl;

import org.venth.poc.runtimeapiswitcher.api.adapter.AdaptedService;
import org.venth.poc.runtimeapiswitcher.api.adapter.Holder;

/**
 * @author Venth on 01/05/2015
 */
public class AdaptedRemoteService implements AdaptedService {
    @Override
    public String version() {
        return "2.0";
    }

    @Override
    public Holder feature(Holder holder) {
        return new Holder(holder.message + " 2.0");
    }
}
