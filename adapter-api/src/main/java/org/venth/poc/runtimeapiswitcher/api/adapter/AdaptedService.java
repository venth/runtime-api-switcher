package org.venth.poc.runtimeapiswitcher.api.adapter;

/**
 * @author Venth on 01/05/2015
 */
public interface AdaptedService extends Versionable {
    Holder feature(Holder holder);
}
