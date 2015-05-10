# runtime-api-switcher
Runtime API Switcher is a proof of concept that an OSGI container may be embedded in a web application and
provide support for different adapter versions.

Thanks of the different version of an adapter, there is possibility to switch between contracts at runtime.

Rules of engagement:
Because, it's a PoC I wrote only three test scenarios to verify, if the switching will work. These scenarios are
contained in RuntimeApiSwitcherTest.

mvn clean integration-test

Adapter bundles are copied to /WEB-INF/bundles directory of Web Application during pre-package phase.

Once, the web application is built, deliver it, please, to a standalone tomcat running on localhost:8080.
Then you may run the integration test against working Web Application hosted on your tomcat.

The container's lifecycle is handled by org.venth.poc.runtimeapiswitcher.webapp.osgi.OsgiFrameworkBootstraper declared
as osgiContainer. The class bootstraps felix container, loads bundles and exposes stopFramework method.  


```
<bean
        id="osgiContainer"
        class="org.venth.poc.runtimeapiswitcher.webapp.osgi.OsgiFrameworkBootstraper"
        destroy-method="stopFramework"
        p:bundlesLocation="/WEB-INF/bundles"
>
    <property name="extraPackages">
        <list>
            <value>org.venth.poc.runtimeapiswitcher.api.adapter</value>
        </list>
    </property>
</bean>
```

Exported services are available by usage of OsgiServiceProxyFactoryBean class. The factory creates a proxy bean, which
encapsulates all calls to osgi service in order to use a indicated service version. Note that, if an application publishes
the event AdapterVersionSwitchRequested, the proxy will react on this event by trying switching osgi service on the fly.

The proxy bean is also responsible for releasing a service, when the application is about to shutdown.  

In spring descriptor there is parent bean declaration (shorthand decreasing typing):

```
<bean id="osgiServiceProxy" class="org.venth.poc.runtimeapiswitcher.webapp.osgi.OsgiVersionSwitchableServiceProxyFactoryBean"
          destroy-method="releaseService"
          abstract="true"
          p:context-ref="osgiContainer"
          p:defaultVersion="1.0"
/>
```

and an instance declaration:

```
<bean parent="osgiServiceProxy" 
    p:serviceType="org.venth.poc.runtimeapiswitcher.api.adapter.AdaptedService"
/>
```
In the application's code the declared service could be injected for example by @Autowire annotation.

Bundling Spring isn't easy task, still is doable. 

Issues:
- Bundled spring cannot find any beans declared by annotations (found solution for that ;)
- spring has a lot of different dependencies... some of them are unnecessary. The bundling process makes 
  the dependencies to be mandatory.

The solution for many spring dependencies is usage of an optional resolution directive provided by osgi.
```
<Import-Package>
    *;resolution:=optional
</Import-Package>
```

Classpath scanning seems to be handled only for equinox platform. Please see class: 
org.springframework.core.io.support.PathMatchingResourcePatternResolver
```java
static {
    try {
        // Detect Equinox OSGi (e.g. on WebSphere 6.1)
        Class<?> fileLocatorClass = ClassUtils.forName("org.eclipse.core.runtime.FileLocator",
                PathMatchingResourcePatternResolver.class.getClassLoader());
        equinoxResolveMethod = fileLocatorClass.getMethod("resolve", URL.class);
        logger.debug("Found Equinox FileLocator for OSGi bundle URL resolution");
    }
    catch (Throwable ex) {
        equinoxResolveMethod = null;
    }
}
```

Spring cannot handle Felix used in this PoC, because Felix doesn't provide any FileLocator. I wrote a simple class
looking for pattern classpath*:**.class. The implemented pattern resolver doesn't look inside bundled jars. It enumerates
only classes located direclty on bundled classpath.
org.venth.poc.runtimeapiswitcher.osgi.OsgiBundleResourceResolver

To use this resolver I simply created a anonymous class from ClassPathXmlApplicationContext.
```java
springContext = new ClassPathXmlApplicationContext("classpath:/context-ver_1.xml") {
    @Override
    protected ResourcePatternResolver getResourcePatternResolver() {
        return new OsgiBundleResourceResolver();
    }
};
```

Test "RuntimeApiSwitcherTest" proves, that spring annotation scanning is working. To run integration tests use:
```
mvn integration-test
```
