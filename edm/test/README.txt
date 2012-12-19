EDM Tests
---------

This package contains unit tests for the EDM, which will be executed automatically if you run the 'test'
target in maven:

	mvn test -P test

The pre-requisites for the unit tests is that a Postgres database with the edm-metrics schema is set up
(see ../resources/README.txt) and connection details for this is configured for this test project. There
are two options for setting this up:

	1: updating the edm.properties file in the ./src/main/resources directory
	2: changing the settings.xml file in your maven repository

The default edm.properties file used for testing attempts to connect to a database on 'localhost:5432'
named 'edm-metrics-test' with username 'postgres' and password 'password'. So you need not do any changes
if this is correct for the machine that the tests are to be run on.

To configure the settings.xml file, you need to add the following:

You need not change this file if this is the set-up on the machine that the unit tests are run on. An
example is given below - please change according to your set-up.

  <profiles>
    <!-- EDM Metrics Database details for EDM testing -->
    <profile>
	  <id>edm-test</id>
	  <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <edm.metrics.dbURL>localhost:5432</edm.metrics.dbURL>
        <edm.metrics.dbName>edm-metrics-test</edm.metrics.dbName>
        <edm.metrics.dbUsername>postgres</edm.metrics.dbUsername>
        <edm.metrics.dbPassword>password</edm.metrics.dbPassword>
      </properties>
    </profile>
  </profiles>