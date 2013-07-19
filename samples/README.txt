Building ECC Clients
====================


Pre-requisites
--------------
Before running any of the samples provided in the ECC API, please ensure you have run through the ECC installation procedure described in the README in the root folder of this API.


ECC development tool
--------------------
To assist with initial client development, an 'ECC Container' desktop application has been created that can be quickly run to test client (rather than deploying the full ECC web-based service). This tool can be found in the samples folder:

  samples\basicECCContainer\

It behaves like a very simple ECC dashboard and so requires configuring in the same way (using the em.properties & edm.properties files - information on the use of these is provided in the main documentation). You can use this tool with the development of any of the clients described below. This tool has been pre-built for you in all official releases of the ECC API in the binaries ZIP; to run it, execute the BAT file:

  <ECC distribution root>\<Release>_Binaries.zip\ECC API samples\runBasicContainer.bat

If you are working from a SNAPSHOT view, then you will need to build and run the tool manually.
  
  
Java clients
------------
Running the Java clients is an easy way to get started with the ECC. The ECC API provides a collection of Java based client examples, these are:

  * basicECCClient - A simple client with desktop GUI that pushes/gets pulled for one metric
  
  * headlessECCClient - A more advanced command line only client that also uses a local database to store its metrics
  
  * dynamicEntityDemoClient - A more advanced GUI based client that allows the user to 'enable'/'disable' metric collection for specific entities

You can use any Maven supporting Java development tool to build and run these samples.

  
Android client
--------------
An Android ECC client has also been provided: 

  * basicAndroidClient - A simple example of an Android implementation for the ECC.
  
This must be uploaded to an Android 4.2.x device - it uses the default EM UUID (00000000-0000-0000-0000-000000000000); the user can specify the IP address of the RabbitMQ server in the UI.
Please see the README in the development folder of this client for further build information.


C# Client
---------
To build and run the C# client, you will first need to build the ECC C# API. In order to do so, first install Visual Studio 2010 (updating to include all service packs is recommended). This ECC platform is under development and does not support EDMAgent functionality. Steps for building and running the ECC C# client follows:

1. Set up your RabbitMQ/PostgreSQL services locally (see documentation)

2. Install Visual Studio 2010

3. Open the solution file:

  extensions/dotNetClientAPI/DotNet ECC Api.sln
  
4. Build the solution

5. Start the Basic ECC Container (see above) or ECC Dashboard (see below)

6. Run the 'SimpleHeadlessECCClient' EXE project (linked to solution; found in 'samples/basicDotNetClient')

    * This should connect the C# client to the ECC via your local RabbitMQ service using the EM UUID 00000000-0000-0000-0000-000000000000
    * Step through to the Live Monitoring phase to start gathering two simple metrics

    
C++ Client
----------
To build and run the C++ ECC client, you will first need to build the ECC C++ API.
Due to the nature of C++ builds across multiple platforms, this is a more complex process: you will need to follow the README found in:

  <ECC API root>/extensions/cppClientAPI/
