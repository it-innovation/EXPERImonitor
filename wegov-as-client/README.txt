Files not in maven to be installed manually or via a pom hack:

mvn install:install-file -Dfile=./lib/entities29.jar -DgroupId=com.mindprod -DartifactId=entities -Dversion=1.0 -Dpackaging=jar  -DgeneratePom=true

mvn install:install-file -Dfile=./lib/java_fathom-1.1.0.jar -DgroupId=com.representqueens -DartifactId=entities -Dversion=1.1.0 -Dpackaging=jar  -DgeneratePom=true

mvn install:install-file -Dfile=./lib/weka.jar -DgroupId=weka -DartifactId=entities -Dversion=1.0 -Dpackaging=jar  -DgeneratePom=true


Build everything:

	mvn clean install -Dmaven.test.skip=true
	
	
Build search tool in wegov-tools/search

	mvn install		(not required if you ran install above)
	mvn assembly:single


Build analysis tool in wegov-tools/analysis

	mvn install		(not required if you ran install above)
	mvn assembly:single
	
Build analysis tool in wegov-tools/inject

	mvn install		(not required if you ran install above)
	mvn assembly:single


Build database maintenance in wegov-database-maintenance:

	edit src/main/java/databasemaintenance/DatabaseMaintenance.java
	 - add yourself as a user, if necessary (example on line 52)

	mvn install
	mvn assembly:single

	to run in the same folder:

		java -jar target/wegov-database-maintenance-1.0-jar-with-dependencies.jar

or if you want logging:

    java -Dlog4j.configuration=file:./log4j.properties -jar target/wegov-database-maintenance-1.0-jar-with-dependencies.jar

Create the database tables for Quartz

	psql WeGov -f wegov-dashboard/src/main/resources/quartz/tables_postgres.sql (for Windows, run script using pgAdmin)


"Wegov" is the name of the database (should match quartz settings).

the jdbc connection string in quartz.properties must match that in coordinator.properties

e.g. quartz.properties

org.quartz.dataSource.WEGOVDS.URL = jdbc:postgresql://localhost:5432/WeGov
org.quartz.dataSource.WEGOVDS.user = wegov
org.quartz.dataSource.WEGOVDS.password = wegov


e.g. coordinator.proprties:

# POSTGRESQL SERVER CONFIGURATION (admin access)
postgres.url=jdbc:postgresql://localhost:5432/
postgres.login=wegov
postgres.pass=wegov

# POSTGRESQL DATABASES AND SCHEMAS CONFIGURATION
/*postgres.database.name=wegovlabs*/
postgres.database.name=WeGov
postgres.database.mgt.name=WeGovManagement
postgres.database.data.name=WeGovRawData


Quartz looks like it uses the default schema of the login user. in the above examples
it is "wegov", and the default schema is set to be "public", so the sql script to create
quartz tables must be in public for the "wegov" user.

Edit configuration files in wegov-dashboard, if necessary

	1) coordinator.properties
	2) src/main/resources/quartz/quartz.properties

	N.B. Ensure that database URL, username, password in quartz.properties match the settings in coordinator.properties,
	     and the database name used when creating Quartz tables above.

Run jetty in wegov-dashboard/:

	mvn jetty:run-war -Dmaven.test.skip=true



// to use log4j properties to log out to a file use the following with the log4j.properties file in the wegov-dashboard file:

  mvn jetty:run-war -Dlog4j.configuration=file:./log4j.properties -Dmaven.test.skip=true

	remotely:
	nohup mvn jetty:run-war -Dmaven.test.skip=true > log-`date +"%m_%d_%Y"`.txt 2> errors-`date +"%m_%d_%Y"`.txt < /dev/null 
Generate keystore (see http://docs.codehaus.org/display/JETTY/How+to+configure+SSL for details):

	openssl pkcs12 -inkey wegov.pem -in wegov.it-innovation.soton.ac.uk.crt -export -out wegov-ssl.pkcs12
	keytool -importkeystore -srckeystore wegov-ssl.pkcs12 -srcstoretype PKCS12 -destkeystore wegov-ssl.keystore