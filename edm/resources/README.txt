=== Experiment Data Manager README ===

The file 'edm-metrics.sql' is an SQL script to create the tables and some of the
data required for the EDM metrics database.

The Experiment Data Manager (EDM) uses PostgreSQL as a database solution, so the
first step is to CREATE THE DATABASE. You can configure the EDM to connect to the
database in its properties file, but you may save yourself one additional edit
by sticking to the default name:

	edm-metrics

PS: make the DB with UTF-8 encoding, e.g.:

	CREATE DATABASE "edm-metrics"
	WITH ENCODING='UTF8'
	CONNECTION LIMIT=-1;
	
To load the database, there's various options to do that; here's one:

	On the command line, assuming you're in the same directory as the
	'edm-metrics.sql' file and the database username is 'postgres', execute:
	
		psql -d edm-metrics -U postgres -f edm-metrics.sql


Configuring the EDM to connect to the database can be done in:

	./impl/src/main/resources/edm.properties