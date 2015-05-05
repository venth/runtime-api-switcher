# runtime-api-switcher
Runtime API Switcher is a proof of concept that an OSGI container may be embedded in a web application and
provide support for different adapter versions.

Thanks of the different version of an adapter, there is possibility to switch between contracts at runtime.

Rules of engagement:
Because, it's a PoC I wrote only three test scenarios to verify, if the switching will work. These scenarios are
contained in RuntimeApiSwitcherTest.

mvn clean package -DskipTests

Tests skipping is needed because I didn't embedded tomcat plugin into the build (laziness). 
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
<bean parent="osgiServiceProxy" p:serviceType="org.venth.poc.runtimeapiswitcher.api.adapter.AdaptedService" />
```
In the application's code the declared service could be injected for example by @Autowire annotation.
