Experiment Content Component (ECC) v.${project.version} source release
=============================================================
-------------------------------------------------------------

Copyright
=========
The source code in this distribution is © Copyright University of Southampton IT Innovation Centre 2012-2014.

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

Brief 'quick start' notes can be found below regarding either using Vagrant or installing dependencies manually.


Using Vagrant to deploy the ECC
===============================
Vagrant (see http://vagrantup.com) along with a suitable virtual machine system (such as Oracle VirtualBox) can be used to simply build and deploy the ECC and all its dependencies including a RabbitMQ server.  This is particularly useful for development and testing.

Assuming a working vagrant system, this is done by first choosing your preferred web server (Apache or Glassfish are supported) and then starting the vagrant virtual machine set up.

1. Choosing your preferred web server
-------------------------------------
Vagrant based support for the ECC offers you two set up options:

 * Tomcat 7 deployment (Vagrantfile.tomcat)
 * Glassfish 3 deployment (Vagrantfile.glassfish)

Make a copy of hte deployment file you wish to use and rename it to 'Vagrantfile' (no file extension)

2. Starting the vagrant virtual machine
---------------------------------------
This is done by simply typing "vagrant up" in the <ECC API root> folder which will execute the instructions found in the "Vagrantfile" you have just selected.  The shell script embedded in the vagrantfile is also a useful reference for deploying the system in Linux.


Once the build is completed, the ECC and a RabbitMQ server are hosted in a virtual machine with the necessary ports mapped through to the host machine.  The port mapping can be changed via environment variables in the host machine:

ECC_IP: the IP address for the ECC VM to use (default 10.0.0.10)
ECC_PORT: the port to map the ECC to on the host machine (default 8090)
RABBIT_IP: the IP address to use for RabbitMQ (default 10.0.0.10)
RABBIT_PORT: the port to map RabbitMQ to on the host machine (default 5682)
RABBIT_MGT_PORT: the port to map RabbitMQ management interface to on the host machine (default 55682)

The sample clients distributed with the ECC generally attempt to connect to a RabbitMQ server running on localhost with the default 5672 port.  To make these clients work with the ECC and RabbitMQ in the vagrant VM you must map the guest VM's RabbitMQ port through to port 5672 in the host machine.  This is achieved by setting the variable "RABBIT_PORT" to 5672 on the host machine.


Installing 3rd party software required by the ECC
=================================================
You will need to install the following 3rd party software:

	- Java JDK 1.7 or better
	- Apache Maven 3
	- Rabbit MQ server
	- PostgreSQL server 9.0+
	- Apache Tomcat 7.x
	- Ontotext OWLIM lite 5.4

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


  Setting up OWLIM lite 5.4
  -------------------------
	1.	Register at http://www.ontotext.com/owlim/owlim-lite-registration for a copy.
		A zip file will be send to you by email after your registration.

	2.	Copy the file owlim-lite-5.4.jar from the /lib directory in the zip file into
		the /thirdPartyLibs directory.

	3.	Extract openrdf-workbench.war and openrdf-sesame.war from the /sesame_owlim
		directory in the zip file and deploy them on your tomcat server.



Building and running the ECC
============================
The ECC dashboard can be quickly built to run on your local development machine by following these steps:

1. Install third party libraries required to build the ECC

	1.1 Open a command line and navigate to:

		<ECC API root>/thirdPartyLibs

	1.2 Run the installLibraries.bat file
		- Note that Linux users will need to run a shell script version of this file


2. Using a command line in the root of the ECC API type:

      mvn clean install

  ... all modules in the ECC API should be reported as successfully built.

  You should find the ECC web dashboard WAR file created in the following location:

      <ECC API root>\eccService\target\EccService-2.1.war


3. Deploy and run the ECC dashboard, using:

	3.1 Tomcat (default deployment)
	---------------------------------------------------------------------------------------------------
	  Step 1: Start up Tomcat

	  Step 2: Copy WAR file generated in step 1 to Tomcat's 'webapps' directory
	          (Tomcat should automatically unpack and deploy for you)

	  Result: You should see the ECC dashboard running on: http://localhost:8080/EccService-2.1/





