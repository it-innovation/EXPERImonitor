ECC as a Service 2.2

Full build (requires valid PostgreSQL and Rabbit for tests):

	mvn install

Skip tests:

	mvn install  -Dmaven.test.skip=true

Run (will build, but won't run tests):

	mvn spring-boot:run

Settings:

	src/main/resources/application.properties


Dashboard (if ran in spring boot):

	http://localhost:8083/ECC

	or check src/main/resources/application.properties and:

	http://localhost:<server.port><server.context-path>


Dashboard (if ran as WAR, requires Tomcat v7.0.53+ for Servlet 3.0 support):

	http://localhost:8080/EccService-2.2