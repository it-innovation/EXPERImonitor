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
The primary source of ECC documentation can be found in the 'doc' folder. This documentation is encapsulated in RST format; readers have a choice of generating a printable version in a variety of formats including HTML and PDF.
For convenience, an HTML version of our documentation can be found here:

<ECC API root>/doc/manual/html

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

1. Install third party libraries required to build the ECC

	1.1 Open a command line and navigate to:
	
		<ECC API root>/thirdPartyLib
		
	1.2 Run the installLibraries.bat file
		- Note that Linux users will need to run a shell script version of this file
	

2. Using a command line in the root of the ECC API type:

      mvn clean install

  ... all modules in the ECC API should be reported as successfully built.
  
  You should find the ECC web dashboard WAR file created in the following location:
  
      <ECC API root>\eccDash\target\experimedia-arch-ecc-eccDash-2.0-SNAPSHOT.war

 
3. Deploy and run the ECC dashboard, using:

	3.1 Tomcat (default deployment)
	---------------------------------------------------------------------------------------------------
	  Step 1: Start up Tomcat

	  Step 2: Copy WAR file generated in step 1 to Tomcat's 'webapps' directory
	          (Tomcat should automatically unpack and deploy for you)
	  
	  Result: You should see the ECC dashboard running on: http://localhost:8080/experimedia-arch-ecc-eccDash-2.0-SNAPSHOT/


Using Vagrant to deploy the ECC
===============================
Vagrant can be used to automatically deploy the ECC, for more information on using Vagrant, look in:

	<ECC API root>/doc/ECC_Vagrant
	


