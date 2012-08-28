=== Experiment Data Manager ===

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

The database can be populated with test data via a class in the EDM java
implementation package; PopulateDB. This is a class with a main method, which
will create an entity with three attributes, an experiment with a metric generator,
a metric group and three measurement sets (for each of the attributes). It will
also create 3 reports, one for each of the measurement sets, with 50 random
measurements.

After running the PopulateDB class, the DB should be populated as per the log
file output:

2012-08-28 10:30:36,055 INFO 	[PopulateDB:136] Entity details:
2012-08-28 10:30:36,056 INFO 	[PopulateDB:137]   - UUID:  5718cd67-4310-4b2c-aeb9-9b72314630ca
2012-08-28 10:30:36,056 INFO 	[PopulateDB:138]   - Name:  VM
2012-08-28 10:30:36,056 INFO 	[PopulateDB:139]   - Desc:  A Virtual Machine
2012-08-28 10:30:36,056 INFO 	[PopulateDB:143]   - There are 3 attributes
2012-08-28 10:30:36,056 INFO 	[PopulateDB:147]     - Attribute details:
2012-08-28 10:30:36,056 INFO 	[PopulateDB:148]       - UUID:  4f2817b5-603a-4d02-a032-62cfca314962
2012-08-28 10:30:36,057 INFO 	[PopulateDB:149]       - Name:  CPU
2012-08-28 10:30:36,060 INFO 	[PopulateDB:150]       - Desc:  CPU performance
2012-08-28 10:30:36,060 INFO 	[PopulateDB:147]     - Attribute details:
2012-08-28 10:30:36,060 INFO 	[PopulateDB:148]       - UUID:  cd42b215-5235-4591-8be5-2d403911cb59
2012-08-28 10:30:36,060 INFO 	[PopulateDB:149]       - Name:  Network
2012-08-28 10:30:36,060 INFO 	[PopulateDB:150]       - Desc:  Network performance
2012-08-28 10:30:36,060 INFO 	[PopulateDB:147]     - Attribute details:
2012-08-28 10:30:36,060 INFO 	[PopulateDB:148]       - UUID:  a460987f-2ef8-4519-91f2-4a23954b16bd
2012-08-28 10:30:36,060 INFO 	[PopulateDB:149]       - Name:  Disk
2012-08-28 10:30:36,061 INFO 	[PopulateDB:150]       - Desc:  Disk performance

2012-08-28 10:30:37,461 INFO 	[PopulateDB:262] Experiment details:
2012-08-28 10:30:37,461 INFO 	[PopulateDB:263]   * Basic info
2012-08-28 10:30:37,461 INFO 	[PopulateDB:264]     - UUID:  bfe4c710-61ba-46f8-a519-be2f7808192e
2012-08-28 10:30:37,461 INFO 	[PopulateDB:265]     - Name:  Experiment
2012-08-28 10:30:37,461 INFO 	[PopulateDB:266]     - Desc:  A test experiment
2012-08-28 10:30:37,463 INFO 	[PopulateDB:267]     - Start: Wed Aug 22 14:33:41 BST 2012 (1345642421005)
2012-08-28 10:30:37,463 INFO 	[PopulateDB:268]     - End:   Mon Aug 24 11:57:11 BST 2015 (1440413831014)
2012-08-28 10:30:37,463 INFO 	[PopulateDB:269]     - ID:    /locations/experiment/1337
2012-08-28 10:30:37,463 INFO 	[PopulateDB:274]   * There's 1 metric generator(s)
2012-08-28 10:30:37,463 INFO 	[PopulateDB:278]     - MetricGenerator details:
2012-08-28 10:30:37,463 INFO 	[PopulateDB:279]       - UUID: 782e5097-2e29-4219-a984-bf48dfcd7f63
2012-08-28 10:30:37,464 INFO 	[PopulateDB:280]       - Name: Experiment MetricGenerator
2012-08-28 10:30:37,465 INFO 	[PopulateDB:282]       - Desc: A metric generator
2012-08-28 10:30:37,465 INFO 	[PopulateDB:287]       * There's 1 metric group(s)
2012-08-28 10:30:37,465 INFO 	[PopulateDB:291]         - MetricGroup details:
2012-08-28 10:30:37,465 INFO 	[PopulateDB:292]           - UUID: 189064a5-f1d8-41f2-b2c1-b88776841009
2012-08-28 10:30:37,465 INFO 	[PopulateDB:293]           - Name: Quality of Service
2012-08-28 10:30:37,467 INFO 	[PopulateDB:295]           - Desc: A group of QoS metrics
2012-08-28 10:30:37,467 INFO 	[PopulateDB:300]           * There's 3 measurement set(s)
2012-08-28 10:30:37,526 INFO 	[PopulateDB:312]             - MeasurementSet details:
2012-08-28 10:30:37,526 INFO 	[PopulateDB:313]               - UUID: 3b915932-41b1-45d7-b4f6-2de4f30020b7
2012-08-28 10:30:37,526 INFO 	[PopulateDB:314]               - Attribute:
2012-08-28 10:30:37,526 INFO 	[PopulateDB:316]                   - UUID:  cd42b215-5235-4591-8be5-2d403911cb59
2012-08-28 10:30:37,526 INFO 	[PopulateDB:317]                   - Name:  Network
2012-08-28 10:30:37,527 INFO 	[PopulateDB:318]                   - Desc:  Network performance
2012-08-28 10:30:37,527 INFO 	[PopulateDB:322]               - Metric:
2012-08-28 10:30:37,527 INFO 	[PopulateDB:323]                   - UUID:  ** RANDOM UUID HERE **
2012-08-28 10:30:37,528 INFO 	[PopulateDB:324]                   - Type:  RATIO
2012-08-28 10:30:37,545 INFO 	[PopulateDB:325]                   - Unit:  bit/s
2012-08-28 10:30:37,602 INFO 	[PopulateDB:312]             - MeasurementSet details:
2012-08-28 10:30:37,602 INFO 	[PopulateDB:313]               - UUID: 2b915932-41b1-45d7-b4f6-2de4f30020b8
2012-08-28 10:30:37,602 INFO 	[PopulateDB:314]               - Attribute:
2012-08-28 10:30:37,602 INFO 	[PopulateDB:316]                   - UUID:  4f2817b5-603a-4d02-a032-62cfca314962
2012-08-28 10:30:37,603 INFO 	[PopulateDB:317]                   - Name:  CPU
2012-08-28 10:30:37,603 INFO 	[PopulateDB:318]                   - Desc:  CPU performance
2012-08-28 10:30:37,603 INFO 	[PopulateDB:322]               - Metric:
2012-08-28 10:30:37,603 INFO 	[PopulateDB:323]                   - UUID:  ** RANDOM UUID HERE **
2012-08-28 10:30:37,604 INFO 	[PopulateDB:324]                   - Type:  RATIO
2012-08-28 10:30:37,604 INFO 	[PopulateDB:325]                   - Unit:  ms
2012-08-28 10:30:37,670 INFO 	[PopulateDB:312]             - MeasurementSet details:
2012-08-28 10:30:37,671 INFO 	[PopulateDB:313]               - UUID: 4b915932-41b1-45d7-b4f6-2de4f30020b6
2012-08-28 10:30:37,671 INFO 	[PopulateDB:314]               - Attribute:
2012-08-28 10:30:37,671 INFO 	[PopulateDB:316]                   - UUID:  a460987f-2ef8-4519-91f2-4a23954b16bd
2012-08-28 10:30:37,671 INFO 	[PopulateDB:317]                   - Name:  Disk
2012-08-28 10:30:37,671 INFO 	[PopulateDB:318]                   - Desc:  Disk performance
2012-08-28 10:30:37,671 INFO 	[PopulateDB:322]               - Metric:
2012-08-28 10:30:37,673 INFO 	[PopulateDB:323]                   - UUID:  ** RANDOM UUID HERE **
2012-08-28 10:30:37,674 INFO 	[PopulateDB:324]                   - Type:  RATIO
2012-08-28 10:30:37,674 INFO 	[PopulateDB:325]                   - Unit:  ms

