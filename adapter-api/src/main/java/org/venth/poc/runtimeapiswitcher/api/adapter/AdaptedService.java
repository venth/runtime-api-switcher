package org.venth.poc.runtimeapiswitcher.api.adapter;

/**
 * @author Venth on 01/05/2015
 */
public interface AdaptedService {
    String version();

    Holder feature(Holder holder);
}
