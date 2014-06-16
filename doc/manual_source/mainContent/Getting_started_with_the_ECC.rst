Getting started with the ECC
============================

The ‘bare bones’ software required for deploying and running the ECC are as follows:

*   Java JDK
    1.7



*   Maven 3.0



*   RabbitMQ
    server
    2.8.6
    (or better)



*   PostGreSQL 9.1



*   Apache Tomcat 7.x



*   The ECC software package



We advise running the sample applications provided in this release on an operating system and computer that meets the minimal requirements
of both RabbitMQ and PostGreSQL.

The following sequence of steps illustrates how to manually set up the ECC on a
single system (Microsoft Windows 7
in this case).
It should be noted that the RabbitMQ server can be deployed separately and remotely connected (given the appropriate IP address and access permissions).


Acquiring the ECC software package
----------------------------------

Before proceeding with installation of EXPERIMEDIA’s ECC, you will first need access to download the ECC package. Access is provided using an SVN server and must be arranged by permission. Please contact one of the following people to realise this:

*   Stephen Phillips (
    `scp@it-innovation.soton.ac.uk <mailto:scp@it-innovation.soton.ac.uk>`_
    )



*   Simon Crowle (
    `sgc@it-innovation.soton.ac.uk <mailto:sgc@it-innovation.soton.ac.uk>`_
    )



You will be provided with SVN login details after which you can download the software package, provided as a ZIP file, in the following location::

	.\trunk\ECC\<ECC version name>\<ECC version>.zip

Download and unzip this file. You will need to use its contents
later on in the set up process.


ECC Dependencies and Licenses
-----------------------------

This software requires various third-party libraries to compile and/or execute. These libraries and their associated licences are detailed in the following file with the ECC API:

	<ECC API root>\IPR.txt


Installing JavaJDK
------------------

The ECC system runs using Java 1.7. To build and run the ECC, you will need to install the appropriate JDK
for your system; please visit:

`http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html <http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html>`_


Installing Maven
----------------

The ECC build process is executed using Maven 3.0. You can download this here:

`http://maven.apache.org/download.cgi <http://maven.apache.org/download.cgi>`_

Some development systems (such as NetBeans) come with Maven included.


Installing RabbitMQ
-------------------

You can download the RabbitMQ server from:

`http://www.rabbitmq.com/download.html <http://www.rabbitmq.com/download.html>`_

This usage scenario assumes you will run a RabbitMQ server on your local Windows machine.

     1. Install RabbitMQ v2.8.6 or later for Windows(please follow instructions from the RabbitMQ website, including the dependency on Erlang).

     2. Open an adminstrator level Command window in your RabbitMQ server 'sbin' folder

     3. Start the RabbitMQ server, by typing: rabbitmq-service start


After following the standard installation procedure outlined by RabbitMQ, you should have a RabbitMQ server service running locally, using the default RabbitMQ connection:
localhost:5672. If you also installed the web-based management system, you should be able to view the status of your RabbitMQ server by navigating to
`http://localhost:55672 <http://localhost:55672>`_  The ECC is set up by default to use this local address


Installing PostGreSQL
---------------------

Final your installation of PostGreSQL here:

`http://www.postgresql.org/download/ <http://www.postgresql.org/download/>`_

After following the installation instructions for your platform,
you should have the PosGreSQL database service running and have a username and password ready.
The default login details used by the ECC to connect to the database are as follows:

*   Username: postgres



*   Password: password



Having done this, you should set up the database using the following steps:


#.  Create a database called edm-metrics. (This is the default name)


#.  Execute the SQL script found in the ECC package:

      *   Location: edm\resources\edm-metrics-postgres.sql


      *   Execution from the command line::

			psql -d edm-metrics -U*<username>*-f edm-metrics-postgres.sql


Above, the term *<username>* refers to your username.

More detail information on setting up the EDM and running test procedures can be found in the ECC package::

		edm\resources\edm-metrics-postgres.sql\README.txt



Installing Apache Tomcat 7.x
----------------------------

The Apache Tomcat server software is used to host the ECC dashboard; you can download the Tomcat server here:

`http://tomcat.apache.org/download-70.cgi <http://tomcat.apache.org/download-70.cgi>`_

After installing the Tomcat server, you should have a Tomcat service running – this can be verified by navigating to the management web page usually found at
`http://localhost:8080/ <http://localhost:8080/>`_ .


Building and deploying the ECC locally
--------------------------------------

Having set up the requisite support software, you are now ready to build and deploy the ECC. To do this,
follow these steps:

1. Using a command line in the root of the ECC API type::

	cd thirdPartyLibs

	installLibraries.[bat] or [sh]

	cd ..

	mvn clean install



 All modules in the ECC API should be reported as successfully built. You should find the ECC web dashboard WAR file created in the following location::

		<ECC API root>\eccService\target\EccService-2.1.war


2. Deploy and run the ECC dashboard, using:

	2.1. Tomcat (default deployment)

	    Step 1: Start up Tomcat.

	    Step 2: Copy **EccService-2.1.war** to Tomcat's **webapps** directory (Tomcat should automatically unpack and deploy it for you immediately).

	    Result: You should see the ECC dashboard running on: http://localhost:8080/EccService-2.1.

	2.2. Glassfish (alternative deployment)

		Step 1: Update Glassfish Java permissions for socket access using **vagrantConf/glassfish/java.policy** file

		Step 2: Copy the following JARs from **eccService/target/EccService-2.1/WEB-INF/lib/** into Glassfish **/lib/endorsed** folder for Logback library support:

			* logback-core-1.1.2.jar
			* logback-classic-1.1.2.jar
			* jul-to-slf4j-1.7.6.jar

		Step 3: Configure Glassfish support for Logback libraries by updating the following files:

			* <Glassfish home>/glassfish/domains/domain1/config/logback.xml
			* <Glassfish home>/glassfish/domains/domain1/config/logging.properties
			* <Glassfish home>/glassfish/domains/domain1/domain.xml

		with the ones from **vagrantConf/glassfish** folder.

		Step 4: Start default Glassfish domain and database.

		Step 5: Deploy **EccService-2.1.war**.

		Result: You should see the ECC dashboard running on: http://localhost:8080/EccService-2.1.

	2.3. Spring boot (developer's deployment)

		Step 1: Change into **eccService** folder.

		Step 2: Run ``mvn spring:boot`` command.

		Result: ECC Dashboard deployed in build-in Tomcat on: http://localhost:8083/ECC (port and context configurable via ``server.port`` and ``server.context-path`` properties in **eccService/src/main/resources/application.properties** file).

	2.4. Vagrant (automatic deployment via virtual machines)

		Step 1: Install Vagrant (http://www.vagrantup.com) and Virtual Box (https://www.virtualbox.org)

		Step 2: Rename:

			* **<ECC API root>/Vagrantfile.tomcat** into **Vagrantfile** to deploy ECC in a virtual machine with Tomcat server.

			* **<ECC API root>/Vagrantfile.glassfish** into **Vagrantfile** to deploy ECC in a virtual machine with Glassfish server.

		Step 3: In **<ECC API root>** run ``vagrant up`` and wait for the script to finish.

		Result: You should see the ECC dashboard running on: http://localhost:8090/ECC.


3. If you deployed the Dashboard correctly, you should see ECC Service Configuration page in your browser:

 .. image:: images/dashboard_configuration.png
  :width: 100 %


You are now ready to configure ECC and start a new experiment. For further instructions please go to :doc:`Using the ECC Dashboard </mainContent/Using_the_dashboard>` section.