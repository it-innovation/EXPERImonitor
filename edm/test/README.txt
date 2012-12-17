EDM Tests
---------

This package contains unit tests for the EDM, which will be executed automatically if you do a 'mvn install'.

A pre-requisite for the unit tests is that the edm.properties file in the ./src/main/resources directory
is set up with connection details to a Postgres database with the edm-metrics schema loaded. For information
about how to do this, please see ../resources/README.txt

The default edm.properties file used for testing attempts to connect to a database on localhost named

	edm-metrics-test

You need not change this file if this is the set-up on the machine that the unit tests are run on.