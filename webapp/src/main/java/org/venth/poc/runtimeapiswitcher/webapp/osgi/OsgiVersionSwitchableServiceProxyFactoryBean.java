package org.venth.poc.runtimeapiswitcher.webapp.osgi;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.ApplicationListener;
import org.venth.poc.runtimeapiswitcher.api.adapter.Versionable;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

/**
 * @author Venth on 02/05/2015
 */
public class OsgiVersionSwitchableServiceProxyFactoryBean<SERVICE extends Versionable> extends AbstractFactoryBean<SERVICE>
        implements ApplicationListener<AdapterVersionSwitchRequested> {

    private Class<SERVICE> serviceType;
    private BundleContext context;
    private String defaultVersion;

    private OsgiVersionSwitchableServiceMethodInterceptor<SERVICE> switchableService;

    @Override
    public Class<?> getObjectType() {
        return serviceType;
    }

    @Override
    protected SERVICE createInstance() throws Exception {
        ProxyFactoryBean factoryBean = new ProxyFactoryBean();
        switchableService = new OsgiVersionSwitchableServiceMethodInterceptor<>(
                serviceType,
                context,
                defaultVersion
        );

        switchableService.afterPropertiesSet();

        factoryBean.addAdvice(switchableService);
        factoryBean.setTargetClass(serviceType);
        factoryBean.setSingleton(true);

        return (SERVICE) factoryBean.getObject();
    }

    @Override
    public void onApplicationEvent(AdapterVersionSwitchRequested adapterVersionSwitchRequested) {
        try {
            switchableService.switchToServiceVersion(adapterVersionSwitchRequested.newVersion);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void releaseService() {
        switchableService.releaseService();
    }

    protected static class OsgiVersionSwitchableServiceMethodInterceptor<SERVICE extends Versionable> implements
            MethodInterceptor,
            InitializingBean {

        private Class<SERVICE> serviceType;
        private BundleContext context;
        private String defaultVersion;

        private ServiceReference<SERVICE> serviceReference;
        private SERVICE service;

        private ReadWriteLock lock = new ReentrantReadWriteLock();
        private Lock readLock = lock.readLock();
        private Lock writeLock = lock.writeLock();

        public OsgiVersionSwitchableServiceMethodInterceptor(Class<SERVICE> serviceType, BundleContext context, String defaultVersion) {

            this.serviceType = serviceType;
            this.context = context;
            this.defaultVersion = defaultVersion;
        }

        protected ServiceReference<SERVICE> lookupServiceOn(String requiredVersion) throws Exception {
            Collection<ServiceReference<SERVICE>> serviceReferences = context.getServiceReferences(serviceType, null);

            ServiceReference<SERVICE> serviceReference = serviceReferences.stream()
                    .filter(serviceOn(requiredVersion))
                    .findFirst()
                    .orElseThrow(() ->
                                    new RuntimeException("No service supports required version: " + requiredVersion)
                    );

            return serviceReference;
        }

        private Predicate<ServiceReference<SERVICE>> serviceOn(String requiredVersion) {
            return serviceRef -> {
                String supportedVersion;
                try {
                    SERVICE service = context.getService(serviceRef);
                    supportedVersion = service.version();
                } finally {
                    context.ungetService(serviceRef);
                }
                boolean requiredVersionMet = requiredVersion.equals(supportedVersion);
                return requiredVersionMet;
            };
        }

        public void releaseService() {
            boolean neededToReturnServiceToContainer = serviceReference != null;
            if (neededToReturnServiceToContainer) {
                context.ungetService(serviceReference);
            }
        }

        @Override
        public Object invoke(MethodInvocation invocation) throws Throwable {
            readLock.lock();
            try {
                Method methodToInvoke = invocation.getMethod();
                return methodToInvoke.invoke(service, invocation.getArguments());
            } finally {
                readLock.unlock();
            }
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            switchToServiceVersion(defaultVersion);
        }

        private void switchToServiceVersion(String requiredVersion) throws Exception {
            writeLock.lock();
            try {
                serviceReference = lookupServiceOn(requiredVersion);
                service = context.getService(serviceReference);
            } finally {
                writeLock.unlock();
            }
        }
    }

    public void setDefaultVersion(String defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    public void setContext(BundleContext context) {
        this.context = context;
    }

    public void setServiceType(Class<SERVICE> serviceType) {
        this.serviceType = serviceType;
    }

}