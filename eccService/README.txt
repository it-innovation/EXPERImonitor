ECC as a Service

Full build (requires valid PostgreSQL and Rabbit for tests):

	mvn install

Skip tests:

	mvn install  -Dmaven.test.skip=true

Run (will build, but won't run tests):

	mvn spring-boot:run

Settings:

	src/main/resources/application.properties


Dashboard:

	http://localhost:8083/ECC

	or check src/main/resources/application.properties and:

	http://localhost:<server.port><server.context-path>