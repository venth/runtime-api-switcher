package org.venth.poc.runtimeapiswitcher.webapp.osgi;

import org.springframework.context.ApplicationEvent;

/**
 * @author Venth on 05/05/2015
 */
public class AdapterVersionSwitchRequested extends ApplicationEvent {
    private static final Object SOURCE = new Object();
    public final String newVersion;

    public AdapterVersionSwitchRequested(String newVersion) {
        super(SOURCE);
        this.newVersion = newVersion;
    }
}
