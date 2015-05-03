package org.venth.poc.runtimeapiswitcher.webapp.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

/**
 * @author Venth on 02/05/2015
 */
public class OsgiFrameworkBootstraper extends AbstractFactoryBean<BundleContext> implements ServletContextAware {
    static final int STOP_TIMEOUT_IN_MILLIS = 10000;
    private static final Logger LOG = LoggerFactory.getLogger(OsgiFrameworkBootstraper.class);

    private Framework framework;
    private ServletContext servletContext;

    private String bundlesLocation;

    private List<String> extraPackages = Collections.emptyList();

    @Override
    public Class<?> getObjectType() {
        return BundleContext.class;
    }

    @Override
    protected BundleContext createInstance() throws Exception {
        return framework.getBundleContext();
    }

    public void stopFramework() throws BundleException, InterruptedException {
        framework.stop();
        framework.waitForStop(STOP_TIMEOUT_IN_MILLIS);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        framework = createOsgiFramework();
        framework.start();
        loadBundles(framework, bundlesLocation);

        super.afterPropertiesSet();
    }

    private void loadBundles(Framework framework, String bundlesLocation) throws IOException {
        BundleLoader bundleLoader = new BundleLoader(framework.getBundleContext());
        bundleLoader.installFrom(servletContext.getResource(bundlesLocation));
    }

    private Framework createOsgiFramework() {
        FrameworkFactory frameworkFactory = ServiceLoader.load(
                FrameworkFactory.class
        ).iterator().next();

        Map<String, String> config = new HashMap<>();
        try {
            config.put("felix.cache.rootdir", Files.createTempDirectory("osgi_bundles").toFile().getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        config.put("org.osgi.framework.storage.clean", "onFirstInit");
        config.put("felix.bootdelegation.implicit", Boolean.TRUE.toString());
        config.put("org.osgi.framework.system.packages.extra", String.join(";", extraPackages));

        return frameworkFactory.newFramework(config);
    }

    public void setBundlesLocation(String bundlesLocation) {
        this.bundlesLocation = bundlesLocation;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    protected static class BundleLoader {
        private BundleContext context;

        private BundleLoader(BundleContext context) {
            this.context = context;
        }


        public void installFrom(URL bundlesLocation) throws IOException {
            installAllFoundBundlesOn(bundlesLocation);
            startAllInstalledBundles();
        }

        private void startAllInstalledBundles() {
            Arrays.stream(context.getBundles())
                    .forEach(bundle -> {
                        try {
                            bundle.start();
                        } catch (BundleException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }

        private void installAllFoundBundlesOn(URL bundlesLocation) throws IOException {
            try(
                    Stream<Path> bundles = Files.list(
                        FileSystems.getDefault().getPath(
                                bundlesLocation.getFile()
                        )
                    )
            ) {
                bundles.forEach(path -> {
                    try {
                        context.installBundle(path.toUri().toURL().toExternalForm());
                    } catch (BundleException | MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    public void setExtraPackages(List<String> extraPackages) {
        this.extraPackages = extraPackages;
    }
}
