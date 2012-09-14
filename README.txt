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


Documentation
-------------

Javadoc for the source code may be found in the 'apidocs' folder.
Further information may be found in the 'T05 ECC samples notes.docx' file.
Brief notes are below.

Experiment Manager (EM) Usage
-----------------------------

MICROSOFT WINDOWS

This usage scenario assumes you will run a RabbitMQ server on your local machine.
The biaries and batch files are included in this release in the bin folder.

1. Install RabbitMQ v2.8.6 for Windows
   (please follow instructions from the RabbitMQ website, including the dependency on Erlang).

2. Open an adminstrator level Command window in your RabbitMQ server 'sbin' folder

3. Start the RabbitMQ server, by typing:
	rabbitmq-service start
	
4. Run 'runEMContainerLOCAL.bat' from this distribution's 'bin' folder.

5. Run 'runEMClientLOCAL.bat' from this distribution's 'bin' folder.

6. Run through the experiment process using the EMContainer's GUI.

7. Shut down both client and container applications.

8. Shutdown your RabbitMQ server by typing in your RabbitMQ 'sbin' Command window:
	rabbitmq-service stop

Batch files for running the client and container applications so that they connect to a remote RabbitMQ server
are also provided.  They need to be adjusted to set the IP address of the RabbitMQ server.

	
Experiment Data Manager (EDM)
-----------------------------

The Experiment Data Manager manages the data, such as monitoring information,
which is stored via the Experiment Monitor (EM) and displayed via the UI.

The EDM depends on a PostgreSQL database in the back-end, which needs to be set up
before the EDM can be used. Installation instructions can be found in a separate
README: ./src/edm/resources/README.txt
