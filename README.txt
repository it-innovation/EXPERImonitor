Experiment Content Component
============================

Copyright
---------

The source code in this distribution is © University of Southampton IT Innovation Centre 2012.

Licences
--------

The licence for this software can be found in the file ITInnov-EXPERIMEDIA-licence-v1.0.txt
A complete list of licences for this software and associated third party software can be found in the file IPR.txt

Building
--------
MICROSOFT WINDOWS

The software uses maven v3 (http://maven.apache.org/).
To compile the software packages:

1. Open a Command window at the top level POM directory (the same directory as this README)

2. Type:
	mvn install


Usage
-----
MICROSOFT WINDOWS

This usage scenario assumes you will run a RabbitMQ server on your local machine.

1. Install RabbitMQ v2.8.4 for Windows

2. Open an adminstrator level Command window in your RabbitMQ server 'sbin' folder

3. Start the RabbitMQ server, by typing:
	rabbitmq-service start
	
4. Run 'releases/EMProtocolTestReleaseV1/runTest.bat'

5. 2 tests should be successful

6. Shutdown your RabbitMQ server by typing in your RabbitMQ 'sbin' Command window:
	rabbitmq-service stop


