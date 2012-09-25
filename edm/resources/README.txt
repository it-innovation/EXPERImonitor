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

	../impl/src/main/resources/edm.properties

The database can be populated with test data via a class in the EDM java
implementation package; PopulateDB. This is a class with a main method that
will create an experiment, 3 entities (with multiple attribute) and 3 metric
generators (one for each entity). Each metric generator will have a metric group
and measurement sets for each of the attributes for the respective entity. It will
also create reports for each of the measurement set with 20 random measurements.

After running the PopulateDB class, the DB should be populated as per the log
file output shown below:

2012-09-14 09:53:19,443 INFO 	[PopulateDB:568] Experiment details:
2012-09-14 09:53:19,444 INFO 	[PopulateDB:569]   * Basic info
2012-09-14 09:53:19,444 INFO 	[PopulateDB:570]     - UUID:  bfe4c710-61ba-46f8-a519-be2f7808192e
2012-09-14 09:53:19,444 INFO 	[PopulateDB:571]     - Name:  Experiment
2012-09-14 09:53:19,444 INFO 	[PopulateDB:572]     - Desc:  A test experiment
2012-09-14 09:53:19,459 INFO 	[PopulateDB:573]     - Start: Wed Aug 22 14:33:41 BST 2012 (1345642421005)
2012-09-14 09:53:19,459 INFO 	[PopulateDB:574]     - End:   Mon Aug 24 11:57:11 BST 2015 (1440413831014)
2012-09-14 09:53:19,459 INFO 	[PopulateDB:575]     - ID:    /locations/experiment/1337
2012-09-14 09:53:19,460 INFO 	[PopulateDB:580]   * There's 3 metric generator(s)
2012-09-14 09:53:19,460 INFO 	[PopulateDB:584]     - MetricGenerator details:
2012-09-14 09:53:19,460 INFO 	[PopulateDB:585]       - UUID: 982e5097-2e29-4219-a984-bf48dfcd7f63
2012-09-14 09:53:19,460 INFO 	[PopulateDB:586]       - Name: POI Service MetricGenerator
2012-09-14 09:53:19,460 INFO 	[PopulateDB:588]       - Desc: A metric generator
2012-09-14 09:53:19,460 INFO 	[PopulateDB:593]       * There's 1 metric group(s)
2012-09-14 09:53:19,460 INFO 	[PopulateDB:597]         - MetricGroup details:
2012-09-14 09:53:19,460 INFO 	[PopulateDB:598]           - UUID: 989064a5-f1d8-41f2-b2c1-b88776841009
2012-09-14 09:53:19,460 INFO 	[PopulateDB:599]           - Name: Quality of Service
2012-09-14 09:53:19,461 INFO 	[PopulateDB:601]           - Desc: A group of QoS metrics
2012-09-14 09:53:19,463 INFO 	[PopulateDB:606]           * There's 2 measurement set(s)
2012-09-14 09:53:19,497 INFO 	[PopulateDB:618]             - MeasurementSet details:
2012-09-14 09:53:19,497 INFO 	[PopulateDB:619]               - UUID: 9b915932-41b1-45d7-b4f6-2de4f30020b8
2012-09-14 09:53:19,498 INFO 	[PopulateDB:620]               - Attribute:
2012-09-14 09:53:19,498 INFO 	[PopulateDB:622]                   - UUID:  7f2817b5-603a-4d02-a032-62cfca314962
2012-09-14 09:53:19,498 INFO 	[PopulateDB:623]                   - Name:  POI requests
2012-09-14 09:53:19,498 INFO 	[PopulateDB:624]                   - Desc:  The number of POI requests
2012-09-14 09:53:19,498 INFO 	[PopulateDB:628]               - Metric:
2012-09-14 09:53:19,498 INFO 	[PopulateDB:629]                   - UUID:  2c125aff-1db0-4bc1-838e-3adad95c5b38
2012-09-14 09:53:19,499 INFO 	[PopulateDB:630]                   - Type:  RATIO
2012-09-14 09:53:19,499 INFO 	[PopulateDB:631]                   - Unit:  POI requests per minute
2012-09-14 09:53:19,530 INFO 	[PopulateDB:618]             - MeasurementSet details:
2012-09-14 09:53:19,530 INFO 	[PopulateDB:619]               - UUID: 9b915932-41b1-45d7-b4f6-2de4f30020b7
2012-09-14 09:53:19,530 INFO 	[PopulateDB:620]               - Attribute:
2012-09-14 09:53:19,530 INFO 	[PopulateDB:622]                   - UUID:  7d42b215-5235-4591-8be5-2d403911cb59
2012-09-14 09:53:19,530 INFO 	[PopulateDB:623]                   - Name:  POI response time
2012-09-14 09:53:19,530 INFO 	[PopulateDB:624]                   - Desc:  The response time for the POI requests
2012-09-14 09:53:19,530 INFO 	[PopulateDB:628]               - Metric:
2012-09-14 09:53:19,531 INFO 	[PopulateDB:629]                   - UUID:  b8379590-7869-43cc-a1e7-c4497dd7a6de
2012-09-14 09:53:19,531 INFO 	[PopulateDB:630]                   - Type:  RATIO
2012-09-14 09:53:19,531 INFO 	[PopulateDB:631]                   - Unit:  ms
2012-09-14 09:53:19,531 INFO 	[PopulateDB:639]       * There's 1 entity/entities in the metric generator
2012-09-14 09:53:19,531 INFO 	[PopulateDB:643]         - UUID:  7718cd67-4310-4b2c-aeb9-9b72314630ca
2012-09-14 09:53:19,531 INFO 	[PopulateDB:644]         - Name:  POI Service
2012-09-14 09:53:19,534 INFO 	[PopulateDB:645]         - Desc:  Point Of Interest Service
2012-09-14 09:53:19,534 INFO 	[PopulateDB:649]         - There are 2 attributes
2012-09-14 09:53:19,534 INFO 	[PopulateDB:653]           - Attribute details:
2012-09-14 09:53:19,534 INFO 	[PopulateDB:654]             - UUID:  7d42b215-5235-4591-8be5-2d403911cb59
2012-09-14 09:53:19,535 INFO 	[PopulateDB:655]             - Name:  POI response time
2012-09-14 09:53:19,535 INFO 	[PopulateDB:656]             - Desc:  The response time for the POI requests
2012-09-14 09:53:19,536 INFO 	[PopulateDB:653]           - Attribute details:
2012-09-14 09:53:19,537 INFO 	[PopulateDB:654]             - UUID:  7f2817b5-603a-4d02-a032-62cfca314962
2012-09-14 09:53:19,537 INFO 	[PopulateDB:655]             - Name:  POI requests
2012-09-14 09:53:19,537 INFO 	[PopulateDB:656]             - Desc:  The number of POI requests
2012-09-14 09:53:19,537 INFO 	[PopulateDB:584]     - MetricGenerator details:
2012-09-14 09:53:19,537 INFO 	[PopulateDB:585]       - UUID: 782e5097-2e29-4219-a984-bf48dfcd7f63
2012-09-14 09:53:19,539 INFO 	[PopulateDB:586]       - Name: VM MetricGenerator
2012-09-14 09:53:19,539 INFO 	[PopulateDB:588]       - Desc: A metric generator
2012-09-14 09:53:19,539 INFO 	[PopulateDB:593]       * There's 1 metric group(s)
2012-09-14 09:53:19,539 INFO 	[PopulateDB:597]         - MetricGroup details:
2012-09-14 09:53:19,539 INFO 	[PopulateDB:598]           - UUID: 189064a5-f1d8-41f2-b2c1-b88776841009
2012-09-14 09:53:19,540 INFO 	[PopulateDB:599]           - Name: Quality of Service
2012-09-14 09:53:19,540 INFO 	[PopulateDB:601]           - Desc: A group of QoS metrics
2012-09-14 09:53:19,542 INFO 	[PopulateDB:606]           * There's 3 measurement set(s)
2012-09-14 09:53:19,571 INFO 	[PopulateDB:618]             - MeasurementSet details:
2012-09-14 09:53:19,571 INFO 	[PopulateDB:619]               - UUID: 3b915932-41b1-45d7-b4f6-2de4f30020b7
2012-09-14 09:53:19,571 INFO 	[PopulateDB:620]               - Attribute:
2012-09-14 09:53:19,572 INFO 	[PopulateDB:622]                   - UUID:  cd42b215-5235-4591-8be5-2d403911cb59
2012-09-14 09:53:19,572 INFO 	[PopulateDB:623]                   - Name:  Network
2012-09-14 09:53:19,572 INFO 	[PopulateDB:624]                   - Desc:  Network performance
2012-09-14 09:53:19,572 INFO 	[PopulateDB:628]               - Metric:
2012-09-14 09:53:19,572 INFO 	[PopulateDB:629]                   - UUID:  2bf6a873-d2e8-4762-9597-b42dc7925dae
2012-09-14 09:53:19,572 INFO 	[PopulateDB:630]                   - Type:  RATIO
2012-09-14 09:53:19,573 INFO 	[PopulateDB:631]                   - Unit:  bits per second
2012-09-14 09:53:19,605 INFO 	[PopulateDB:618]             - MeasurementSet details:
2012-09-14 09:53:19,605 INFO 	[PopulateDB:619]               - UUID: 2b915932-41b1-45d7-b4f6-2de4f30020b8
2012-09-14 09:53:19,605 INFO 	[PopulateDB:620]               - Attribute:
2012-09-14 09:53:19,605 INFO 	[PopulateDB:622]                   - UUID:  4f2817b5-603a-4d02-a032-62cfca314962
2012-09-14 09:53:19,605 INFO 	[PopulateDB:623]                   - Name:  CPU
2012-09-14 09:53:19,605 INFO 	[PopulateDB:624]                   - Desc:  CPU performance
2012-09-14 09:53:19,605 INFO 	[PopulateDB:628]               - Metric:
2012-09-14 09:53:19,605 INFO 	[PopulateDB:629]                   - UUID:  27a7e24c-fa69-470d-8fbe-9cf4edc9d347
2012-09-14 09:53:19,606 INFO 	[PopulateDB:630]                   - Type:  RATIO
2012-09-14 09:53:19,606 INFO 	[PopulateDB:631]                   - Unit:  ms
2012-09-14 09:53:19,637 INFO 	[PopulateDB:618]             - MeasurementSet details:
2012-09-14 09:53:19,637 INFO 	[PopulateDB:619]               - UUID: 4b915932-41b1-45d7-b4f6-2de4f30020b6
2012-09-14 09:53:19,637 INFO 	[PopulateDB:620]               - Attribute:
2012-09-14 09:53:19,637 INFO 	[PopulateDB:622]                   - UUID:  a460987f-2ef8-4519-91f2-4a23954b16bd
2012-09-14 09:53:19,638 INFO 	[PopulateDB:623]                   - Name:  Disk
2012-09-14 09:53:19,638 INFO 	[PopulateDB:624]                   - Desc:  Disk performance
2012-09-14 09:53:19,638 INFO 	[PopulateDB:628]               - Metric:
2012-09-14 09:53:19,638 INFO 	[PopulateDB:629]                   - UUID:  44d67478-fcaa-4b58-9325-e9005451dade
2012-09-14 09:53:19,638 INFO 	[PopulateDB:630]                   - Type:  RATIO
2012-09-14 09:53:19,638 INFO 	[PopulateDB:631]                   - Unit:  ms
2012-09-14 09:53:19,638 INFO 	[PopulateDB:639]       * There's 1 entity/entities in the metric generator
2012-09-14 09:53:19,639 INFO 	[PopulateDB:643]         - UUID:  5718cd67-4310-4b2c-aeb9-9b72314630ca
2012-09-14 09:53:19,639 INFO 	[PopulateDB:644]         - Name:  VM
2012-09-14 09:53:19,641 INFO 	[PopulateDB:645]         - Desc:  A Virtual Machine
2012-09-14 09:53:19,641 INFO 	[PopulateDB:649]         - There are 3 attributes
2012-09-14 09:53:19,642 INFO 	[PopulateDB:653]           - Attribute details:
2012-09-14 09:53:19,642 INFO 	[PopulateDB:654]             - UUID:  4f2817b5-603a-4d02-a032-62cfca314962
2012-09-14 09:53:19,642 INFO 	[PopulateDB:655]             - Name:  CPU
2012-09-14 09:53:19,642 INFO 	[PopulateDB:656]             - Desc:  CPU performance
2012-09-14 09:53:19,642 INFO 	[PopulateDB:653]           - Attribute details:
2012-09-14 09:53:19,644 INFO 	[PopulateDB:654]             - UUID:  cd42b215-5235-4591-8be5-2d403911cb59
2012-09-14 09:53:19,645 INFO 	[PopulateDB:655]             - Name:  Network
2012-09-14 09:53:19,645 INFO 	[PopulateDB:656]             - Desc:  Network performance
2012-09-14 09:53:19,645 INFO 	[PopulateDB:653]           - Attribute details:
2012-09-14 09:53:19,645 INFO 	[PopulateDB:654]             - UUID:  a460987f-2ef8-4519-91f2-4a23954b16bd
2012-09-14 09:53:19,646 INFO 	[PopulateDB:655]             - Name:  Disk
2012-09-14 09:53:19,648 INFO 	[PopulateDB:656]             - Desc:  Disk performance
2012-09-14 09:53:19,648 INFO 	[PopulateDB:584]     - MetricGenerator details:
2012-09-14 09:53:19,648 INFO 	[PopulateDB:585]       - UUID: 882e5097-2e29-4219-a984-bf48dfcd7f63
2012-09-14 09:53:19,648 INFO 	[PopulateDB:586]       - Name: AVC MetricGenerator
2012-09-14 09:53:19,649 INFO 	[PopulateDB:588]       - Desc: A metric generator
2012-09-14 09:53:19,650 INFO 	[PopulateDB:593]       * There's 1 metric group(s)
2012-09-14 09:53:19,650 INFO 	[PopulateDB:597]         - MetricGroup details:
2012-09-14 09:53:19,650 INFO 	[PopulateDB:598]           - UUID: 889064a5-f1d8-41f2-b2c1-b88776841009
2012-09-14 09:53:19,651 INFO 	[PopulateDB:599]           - Name: Quality of Service
2012-09-14 09:53:19,651 INFO 	[PopulateDB:601]           - Desc: A group of QoS metrics
2012-09-14 09:53:19,651 INFO 	[PopulateDB:606]           * There's 3 measurement set(s)
2012-09-14 09:53:19,688 INFO 	[PopulateDB:618]             - MeasurementSet details:
2012-09-14 09:53:19,688 INFO 	[PopulateDB:619]               - UUID: 8b915932-41b1-45d7-b4f6-2de4f30020b8
2012-09-14 09:53:19,688 INFO 	[PopulateDB:620]               - Attribute:
2012-09-14 09:53:19,688 INFO 	[PopulateDB:622]                   - UUID:  6f2817b5-603a-4d02-a032-62cfca314962
2012-09-14 09:53:19,689 INFO 	[PopulateDB:623]                   - Name:  File ingest
2012-09-14 09:53:19,689 INFO 	[PopulateDB:624]                   - Desc:  
2012-09-14 09:53:19,689 INFO 	[PopulateDB:628]               - Metric:
2012-09-14 09:53:19,689 INFO 	[PopulateDB:629]                   - UUID:  6a545c5b-5ac0-4bdf-997d-ac39eef87087
2012-09-14 09:53:19,689 INFO 	[PopulateDB:630]                   - Type:  RATIO
2012-09-14 09:53:19,689 INFO 	[PopulateDB:631]                   - Unit:  Files ingested per minute
2012-09-14 09:53:19,722 INFO 	[PopulateDB:618]             - MeasurementSet details:
2012-09-14 09:53:19,722 INFO 	[PopulateDB:619]               - UUID: 8b915932-41b1-45d7-b4f6-2de4f30020b7
2012-09-14 09:53:19,722 INFO 	[PopulateDB:620]               - Attribute:
2012-09-14 09:53:19,722 INFO 	[PopulateDB:622]                   - UUID:  6d42b215-5235-4591-8be5-2d403911cb59
2012-09-14 09:53:19,722 INFO 	[PopulateDB:623]                   - Name:  AV streams out
2012-09-14 09:53:19,722 INFO 	[PopulateDB:624]                   - Desc:  
2012-09-14 09:53:19,722 INFO 	[PopulateDB:628]               - Metric:
2012-09-14 09:53:19,723 INFO 	[PopulateDB:629]                   - UUID:  62d36279-6201-4516-b012-bc473cedfa6b
2012-09-14 09:53:19,723 INFO 	[PopulateDB:630]                   - Type:  RATIO
2012-09-14 09:53:19,724 INFO 	[PopulateDB:631]                   - Unit:  AV streams out per minute
2012-09-14 09:53:19,758 INFO 	[PopulateDB:618]             - MeasurementSet details:
2012-09-14 09:53:19,758 INFO 	[PopulateDB:619]               - UUID: 8b915932-41b1-45d7-b4f6-2de4f30020b6
2012-09-14 09:53:19,758 INFO 	[PopulateDB:620]               - Attribute:
2012-09-14 09:53:19,758 INFO 	[PopulateDB:622]                   - UUID:  6460987f-2ef8-4519-91f2-4a23954b16bd
2012-09-14 09:53:19,758 INFO 	[PopulateDB:623]                   - Name:  Frame rate
2012-09-14 09:53:19,759 INFO 	[PopulateDB:624]                   - Desc:  
2012-09-14 09:53:19,759 INFO 	[PopulateDB:628]               - Metric:
2012-09-14 09:53:19,760 INFO 	[PopulateDB:629]                   - UUID:  082db913-5ca2-4bb5-920d-c08e478d6010
2012-09-14 09:53:19,760 INFO 	[PopulateDB:630]                   - Type:  RATIO
2012-09-14 09:53:19,760 INFO 	[PopulateDB:631]                   - Unit:  Average frame rate per second
2012-09-14 09:53:19,760 INFO 	[PopulateDB:639]       * There's 1 entity/entities in the metric generator
2012-09-14 09:53:19,761 INFO 	[PopulateDB:643]         - UUID:  6718cd67-4310-4b2c-aeb9-9b72314630ca
2012-09-14 09:53:19,761 INFO 	[PopulateDB:644]         - Name:  AVC
2012-09-14 09:53:19,763 INFO 	[PopulateDB:645]         - Desc:  Audio Visual Component
2012-09-14 09:53:19,763 INFO 	[PopulateDB:649]         - There are 3 attributes
2012-09-14 09:53:19,763 INFO 	[PopulateDB:653]           - Attribute details:
2012-09-14 09:53:19,763 INFO 	[PopulateDB:654]             - UUID:  6460987f-2ef8-4519-91f2-4a23954b16bd
2012-09-14 09:53:19,763 INFO 	[PopulateDB:655]             - Name:  Frame rate
2012-09-14 09:53:19,763 INFO 	[PopulateDB:656]             - Desc:  
2012-09-14 09:53:19,763 INFO 	[PopulateDB:653]           - Attribute details:
2012-09-14 09:53:19,766 INFO 	[PopulateDB:654]             - UUID:  6f2817b5-603a-4d02-a032-62cfca314962
2012-09-14 09:53:19,766 INFO 	[PopulateDB:655]             - Name:  File ingest
2012-09-14 09:53:19,766 INFO 	[PopulateDB:656]             - Desc:  
2012-09-14 09:53:19,766 INFO 	[PopulateDB:653]           - Attribute details:
2012-09-14 09:53:19,766 INFO 	[PopulateDB:654]             - UUID:  6d42b215-5235-4591-8be5-2d403911cb59
2012-09-14 09:53:19,767 INFO 	[PopulateDB:655]             - Name:  AV streams out
2012-09-14 09:53:19,770 INFO 	[PopulateDB:656]             - Desc:  

