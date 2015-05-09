package org.venth.poc.runtimeapiswitcher.webapp.osgi;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.net.MalformedURLException;
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
        bundleLoader.installAndStartFrom(servletContext.getRealPath(bundlesLocation));
    }

    private Framework createOsgiFramework() {
        FrameworkFactory frameworkFactory = ServiceLoader.load(
                FrameworkFactory.class
        ).iterator().next();

        Map<String, String> config = new HashMap<>();
        /*
        * Sets the root directory used to calculate the bundle cache directory for relative directory names.
        * If org.osgi.framework.storage is set to a relative name, by default it is relative to
        * the current working directory. If this property is set, then it will be calculated as being relative to
        * the specified root directory.
        * */
        config.put("felix.cache.rootdir", getCreatedTemporaryDirectory());
        /*
        * Specifies a comma-delimited list of packages that should be exported via the System Bundle
        * from the framework class loader in addition to the packages
        * in org.osgi.framework.system.packages. The default value is empty.
        * If a value is specified, it is appended to the list of default or specified packages
        * in org.osgi.framework.system.packages.
        * */
        config.put("org.osgi.framework.system.packages.extra", String.join(",", extraPackages));
        /*
        * Determines whether the bundle cache is flushed. The value can either be "none" or "onFirstInit", where "none"
        * does not flush the bundle cache and "onFirstInit" flushes the bundle cache when the framework instance is
        * first initialized. The default value is "none".
        */
        config.put("org.osgi.framework.storage.clean", "onFirstInit");
        /*
        * Specifies whether the framework should try to guess when to implicitly boot delegate to
        * ease integration with external code. The default value is true.
        * */
        config.put("felix.bootdelegation.implicit", Boolean.TRUE.toString());
        /*
        * Specifies which class loader is used for boot delegation. Possible values are: boot for the boot class loader,
        * app for the application class loader, ext for the extension class loader, and framework for
        * the framework's class loader. The default is boot.
        */
        config.put("org.osgi.framework.bundle.parent", "boot");
        /*
        * Flag to indicate whether to activate the URL Handlers service for the framework instance;
        * the default value is true. Activating the URL Handlers service will result in
        * the URL.setURLStreamHandlerFactory() and URLConnection.setContentHandlerFactory() being called.
        * */
        config.put("felix.service.urlhandlers", Boolean.TRUE.toString());

        return frameworkFactory.newFramework(config);
    }

    private String getCreatedTemporaryDirectory() {
        try {
            return Files.createTempDirectory("osgi_bundles").toFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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


        public void installAndStartFrom(String bundlesLocation) throws IOException {
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

        private void installAllFoundBundlesOn(String bundlesLocation) throws IOException {
            try(
                    Stream<Path> bundles = Files.list(
                        FileSystems.getDefault().getPath(
                                bundlesLocation
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
