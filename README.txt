Experiment Content Component
============================
----------------------------

Copyright
=========
The source code in this distribution is © Copyright University of Southampton IT Innovation Centre 2012-2013.

Licences
========
The licence for this software is LGPL v2.1; the licence agreement for this can be found in folder 'licences'.

A complete list of licences for this software and associated third party software can be found in the file IPR.txt and in sub-folders within the source directories.
  
Documentation
=============
The primary source of ECC documentation can be found in the 'doc' folder. This documentation is encapsulated in RST format; readers have a choice of generating a printable version in a variety of formats including HTML and PDF - please see the README instructions in the doc folder.

Javadoc for the source code may be found in /doc/javaDoc' folder. In addition to this, supplementary READMEs relating to ECC client development can be found in the 'samples' folder. 
Brief notes 'quick start' notes can be found below.


Installing 3rd party software required by the ECC
=================================================
You will need to install the following 3rd party software:

	- Java JDK 1.6 or better
	- Apache Maven 3
  - Rabbit MQ server
	- PostgreSQL server 9.0+
	- Apache Tomcat 7.x
  
  Setting up Java and Maven
  -------------------------
  - Get the Java JDK here: http://www.oracle.com/technetwork/java/javase/downloads/index.html
  - Get Maven from here: http://maven.apache.org/download.cgi
  
  
  Setting up RabbitMQ (Windows)
  -----------------------------
  This usage scenario assumes you will run a RabbitMQ server on your local machine.

    1. Install RabbitMQ v2.8.6 for Windows
      (please follow instructions from the RabbitMQ website, including the dependency on Erlang).

    2. Open an adminstrator level Command window in your RabbitMQ server 'sbin' folder

    3. Start the RabbitMQ server, by typing:
      rabbitmq-service start

    
  Setting up PostgreSQL
  ---------------------
  You will need to install PostgreSQL 9.2 to support ECC metric storage. 

    1. Visit the PostgreSQL website and install PostgreSQL 9.2
  
    2. Make sure you note your PostgreSQL username and password (you will need this for configuration)
  
    3. Follow the steps found in the following README to setup an ECC database inside PostgreSQL:
  
      <ECC API root>\edm\resources\README.txt
  
  
  Setting up Tomcat 7.x
  ---------------------
  Tomcat can be used 'out of the box':
  
    1. Visit Apache's website and download the appropriate ZIP file, see:
      http://tomcat.apache.org/download-70.cgi
    
    2. Unzip the Tomcat distribution
    
    3. Start up and stop Tomcat for the first time:
    
      a) <Tomcat root>\bin\startup.bat (or .sh)
      
      b) Check your can view the Tomcat UI: http://localhost:8080
      
      c) <Tomcat root>\bin\shutdown.bat (or .sh)
      
    4. (Optional) - you may wish to modify the Tomcat user credentials to suit use
    
      - See <Tomcat root>\conf\tomcat-users.xml

      
Building and running the ECC
============================
The ECC dashboard can be quickly built to run on your local development machine by following these steps:

1. Using a command line in the root of the ECC API type:

      mvn clean install

  ... all modules in the ECC API should be reported as successfully built.
  
  You should find the ECC web dashboard WAR file created in the following location:
  
      <ECC API root>\eccDash\target\experimedia-arch-ecc-eccDash-1.2-SNAPSHOT.war

 
2. Deploy and run the ECC dashboard, using:

	2.1 Tomcat (default deployment)
	---------------------------------------------------------------------------------------------------
	  Step 1: Start up Tomcat

	  Step 2: Copy WAR file generated in step 1 to Tomcat's 'webapps' directory
	          (Tomcat should automatically unpack and deploy for you)
	  
	  Result: You should see the ECC dashboard running on: http://localhost:8080/experimedia-arch-ecc-eccDash-1.2-SNAPSHOT/

	2.2 Tomcat (your particular configuration)
	---------------------------------------------------------------------------------------------------
	  Step 1: Follow step 2.1

	  Step 2: Using Tomcat's management UI, STOP the current ECC dashboard

	  Step 3: Modify the any of the ECC property files you require:
		  <Tomcat root>\webapps\experimedia-arch-ecc-eccDash-1.2-SNAPSHOT\WEB-INF\dashboard.properites
		  <Tomcat root>\webapps\experimedia-arch-ecc-eccDash-1.2-SNAPSHOT\WEB-INF\edm.properites       (PostgreSQL login details here)
		  <Tomcat root>\webapps\experimedia-arch-ecc-eccDash-1.2-SNAPSHOT\WEB-INF\em.properites        (RabbitMQ server details here)

	  Step 4: Using Tomcat's management UI:
		> Expire any existing ECC dashboard sessions
		> RELOAD the ECC dashboard


Using JuJu to deploy the ECC
============================
Juju can be used to deploy the ECC in a cloud environment - for instance EC2, OpenStack or in local virtual machines. For more information about Juju please refer to the Ubuntu documentation.


Installing the ECC using Juju/OpenStack
---------------------------------------
1. Make sure juju is bootstrapped and "juju status" returns something like:

machines:
  0:
    agent-state: running
    dns-name: 192.168.0.7
    instance-id: f0b8f237-aac6-49a0-9766-d0103edc9138
    instance-state: running

2. Ensure that Maven and Java (1.5 or better) are installed

3. Download the ECC source code

4. Build the ECC (mvn clean install from the source folder, see above for build instructions)

5. Run script deployDashboard.sh in this folder to deploy ECC Dashboard
