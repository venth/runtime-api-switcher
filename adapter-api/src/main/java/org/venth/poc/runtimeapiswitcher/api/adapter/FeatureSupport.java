package org.venth.poc.runtimeapiswitcher.api.adapter;

import java.util.Set;

/**
 * @author Venth on 03/05/2015
 */
public interface FeatureSupport {
    String version();

    Set<String> supports();
}
