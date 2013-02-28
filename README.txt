Experiment Content Component
============================

Copyright
---------

The source code in this distribution is © Copyright University of Southampton IT Innovation Centre 2012-2013.

Licences
--------

The licence for this software can be found in the file ITInnov-EXPERIMEDIA-licence-v1.0.txt
A complete list of licences for this software and associated third party software can be found in the file IPR.txt and in sub-folders within the source directories.

Building
--------

MICROSOFT WINDOWS

The software uses maven v3 (http://maven.apache.org/).
To compile the software packages:

1. Open a Command window at the top level POM directory (src directory)

2. Type:
	mvn install


Documentation
-------------

Javadoc for the source code may be found in the 'apidocs' folder.
Further information may be found in the 'T05 ECC samples notes V1.0.docx' file.
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
	
4. Run 'runBasicECCContainer.bat' from this distribution's 'bin/ECC API samples' folder.

5. Run 'runBasicECCClient.bat' from this distribution's 'bin/ECC API samples' folder.

6. Run through the experiment process using the ECC Container's GUI.

7. Shut down both client and container applications.

8. Shutdown your RabbitMQ server by typing in your RabbitMQ 'sbin' Command window:
	rabbitmq-service stop

Batch files for running the client and container applications so that they connect to a remote RabbitMQ server
are also provided.  They need to be adjusted to set the IP address of the RabbitMQ server.

	
Experiment Data Manager (EDM)
-----------------------------

The Experiment Data Manager manages the data, such as monitoring information,
which is stored via the ECC Monitor (EM) and displayed via the UI.

The EDM depends on a PostgreSQL database in the back-end, which needs to be set up
before the EDM can be used. Installation instructions can be found in a separate
README: ./src/edm/resources/README.txt


ECC dashboard
-------------

The ECC dashboard can be quickly built to run on your local development machine by following
these steps:

1. Install the following:
	- Java 1.6 or better
	- Apache Maven 3
	- PostgreSQL server 9.0+ (for EDM), configured in edm.properties
	- Rabbit MQ server (for EM)
	- Apache Tomcat 7.x (if deploying on Tomcat)

2. Prepare the PostgreSQL database to work with settings in edm.properties file (see step 7.2)
	* See the README in ./src/edm/resources/README.txt for more information

3. Prepare RabbitMQ server to work with settings in em.properties file (see step 7.2)
	* See the 'T05 ECC samples notes V1.0' technical note for more information

4. (Optional) Prepare your NAGIOS server, noting the full URL for use in the dashboard.properties file (see step 7.2)

5. Install your Tomcat server

6. Build the dashboard.
	Type: mvn clean install

7. Deploy and run, using:

	7.1 Tomcat (default deployment)
	---------------------------------------------------------------------------------------------------
	  Step 1: Start up Tomcat

	  Step 2: Copy WAR file generated in step 6 to Tomcat's 'webapps' directory
	          (Tomcat should automatically unpack and deploy for you)
	  
	  Result: You should see the ECC dashboard running on: http://localhost:8080/eccDash-1.1-SNAPSHOT/

	7.2 Tomcat (your particular configuration)
	---------------------------------------------------------------------------------------------------
	  Step 1: Follow 7.1

	  Step 2: Using Tomcat's management UI, STOP the current ECC dashboard

	  Step 3: Modify the any of the ECC property files you require:
		  <Tomcat root>\webapps\eccDash-1.1-SNAPSHOT\WEB-INF\dashboard.properites
		  <Tomcat root>\webapps\eccDash-1.1-SNAPSHOT\WEB-INF\edm.properites
		  <Tomcat root>\webapps\eccDash-1.1-SNAPSHOT\WEB-INF\em.properites

	  Step 4: Using Tomcat's management UI:
		> Expire any existing ECC dashboard sessions
		> RELOAD the ECC dashboard


Using JuJu to deploy the ECC
----------------------------

Juju can be used to deploy the ECC in a cloud environment - for instance EC2, OpenStack or in local virtual machines. For more information about Juju please refer to the Ubuntu documentation.


Installing the ECC
------------------

1. Make sure juju is bootstrapped and "juju status" returns something like:

machines:
  0:
    agent-state: running
    dns-name: 192.168.0.7
    instance-id: f0b8f237-aac6-49a0-9766-d0103edc9138
    instance-state: running

2. Ensure that Maven and Java (1.5 or better) are installed

3. Download the ECC source code

4. Build the ECC (mvn clean install from the source folder)

5. Run script deployDashboard.sh in this folder to deploy ECC Dashboard
