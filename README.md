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
