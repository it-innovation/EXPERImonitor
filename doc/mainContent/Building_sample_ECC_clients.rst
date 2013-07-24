Building Sample ECC Clients
===========================


Pre-requisites
--------------

Before running any of the samples provided in the ECC API, please ensure you have run through the ECC installation procedure described in the README in the root folder of this API.

ECC development tool
--------------------

To assist with initial client development, an 'ECC Container' desktop application has been created that can be quickly run to test client (rather than deploying the full ECC web-based service). This tool can be found in the samples folder::

  samples\basicECCContainer\

It behaves like a very simple ECC dashboard and so requires configuring in the same way (using the em.properties & edm.properties files - information on the use of these is provided in the main documentation). You can use this tool with the development of any of the clients described below. This tool has been pre-built for you in all official releases of the ECC API in the binaries ZIP; to run it, execute the BAT file::

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

3. Open the solution file::

    extensions/dotNetClientAPI/DotNet ECC Api.sln
  
4. Build the solution

5. Start the Basic ECC Container (see above) or ECC Dashboard (see below)

6. Run the 'SimpleHeadlessECCClient' EXE project (linked to solution; found in 'samples/basicDotNetClient')

    * This should connect the C# client to the ECC via your local RabbitMQ service using the EM UUID 00000000-0000-0000-0000-000000000000
    * Step through to the Live Monitoring phase to start gathering two simple metrics

    
C++ Client
----------

To build and run the C++ ECC client, you will first need to build the ECC C++ API.

Prerequisites
~~~~~~~~~~~~~

The ECC C++ API is has been developed with a view to providing as much cross-platform compatibility as possible. There are three main 3rd party libraries you will be required to build for your system platform before compiling the ECC API itself. These 3rd party libraries are:

  * rabbitmq-c-v0.3.0 (MIT licence)
    - C based protocol client for AMQP
    - Source can be found here: https://github.com/alanxz/rabbitmq-c
    
  * SimpleAmqpClient (MIT licence)
    - C++ wrapper for rabbitmq-c-v0.3.0
    - Source can be found here: https://github.com/alanxz/SimpleAmqpClient
    
  * Boost 1.53.0 (Boost licence)
    - Not included in the ECC distribution
    - Download from: http://www.boost.org/users/history/version_1_53_0.html
    
  * cmake 2.8 build system
    - Not included in the ECC distribution
    - Download from: http://www.cmake.org/
    - Required to build: rabbitmq-c-v0.3.0 and SimpleAmqpClient
	

IMPORTANT NOTE: If you are building using Microsoft Windows and Microsoft Visual Studio 2010, you only need to follow steps:

  * Building the Boost Libraries
  * Building the Microsoft Visual Studio Solution  
  
  
Building the Boost Libraries
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Almost all of the C++ libraries constructed in this API require Boost. Once you have downloaded and unzipped the Boost distribution, you sould:

    1. Boot-strap your BOOST libraries (to build system libraries for stage/lib/..)
		
		Instructions for this can be found here: http://www.boost.org/doc/libs/1_54_0/more/getting_started/index.html
      
    2. Make sure you define your BOOST root directory path variable (see BOOST instruction set)
          
          
Building rabbitmq-c-v0.3.0
~~~~~~~~~~~~~~~~~~~~~~~~~~

Dependencies: NONE.

A copy of the rabbitmq-c-v0.3.0 source can be found in the following directory::

  <ECC API root>/extensions/cppClientAPI/amqpAPI/rabbitmq-c-v0.3.0
    
Instructions on how to use cmake in conjunction with your platform compiler is provided in the README files within this folder. Having successfully compiled rabbitmq-c-v0.3.0 you should have release/debug version of the following binaries:

  * rabbitmq.lib (or equivalent library file)
  * rabbitmq.exp (or equivalent library export definition file)
  * rabbitmq.dll (or equivalent dynamic library executable)

  
SimpleAmqpClient
~~~~~~~~~~~~~~~~

Dependencies:
  * BOOST
  * rabbitmq-c-v0.3.0
  
A copy of the SimpleAmqpClient source can be found in the following directory::

  <ECC API root>/extensions/cppClientAPI/amqpAPI/rabbit-c-wrapper
    
Instructions on how to use cmake in conjunction with your platform compiler is provided in the README files within this folder. Having successfully compiled SimpleAmqpClient you should have release/debug version of the following binaries:

  * SimpleAmqpClient.lib (or equivalent library file)
  * SimpleAmqpClient.exp (or equivalent library export definition file)
  * SimpleAmqpClient.dll (or equivalent dynamic library executable)

  
C++ ECC API libraries
~~~~~~~~~~~~~~~~~~~~~

The following modular libraries need to be built before compiling an ECC client:

  * eccCodeUtils.lib
  * amqp-Impl.lib
  * eccCommonDataModel.lib
  * eccEMClient-Impl.lib
  
The details for building each library are provided below.


ECC library: eccCodeUtils
~~~~~~~~~~~~~~~~~~~~~~~~~

Dependencies:
  * BOOST
  
The source files required for this library can be found here::

 <ECC API root>/extensions/cppClientAPI/eccCodeUtils/Utils
 
This should be built as a static library (currently available as a Visual Studio 2010 project), and result in the following binaries:

  * eccCodeUtils.lib (or equivalent library file)
  

ECC library: amqp-Impl
~~~~~~~~~~~~~~~~~~~~~~

Dependencies:

    * BOOST
    * rabbitmq-c-v0.3.0
    * SimpleAmqpClient
    * eccCodeUtils

    
The source files required for this library can be found here::

  <ECC API root>/extensions/cppClientAPI/amqp-Spec
  <ECC API root>/extensions/cppClientAPI/amqp-Impl/amqp
  <ECC API root>/extensions/cppClientAPI/amqp-Impl/faces
  
This should be built as a static library (currently available as a Visual Studio 2010 project), and result in the following binaries:

  * amqp-Impl.lib (or equivalent library file)

  
ECC library: eccCommonDataModel
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  

Dependencies:
  
   * BOOST
   * eccCodeUtils

The source files required for this library can be found here::

  <ECC API root>/extensions/cppClientAPI/eccCommonDataModel/Base
  <ECC API root>/extensions/cppClientAPI/eccCommonDataModel/Experiment
  <ECC API root>/extensions/cppClientAPI/eccCommonDataModel/Metrics
  <ECC API root>/extensions/cppClientAPI/eccCommonDataModel/Monitor
  
This should be built as a static library (currently available as a Visual Studio 2010 project), and result in the following binaries:

  * eccCommonDataModel.lib (or equivalent library file)
  

ECC library:  eccEMClient-Impl
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Dependencies:
  * BOOST
  * SimpleAmqpClient
  * eccCodeUtils
  * amqp-Impl
  * eccCommonDataModel
  
The source files required for this library can be found here::

  <ECC API root>/extensions/cppClientAPI/eccEMClient-Spec/impl
  <ECC API root>/extensions/cppClientAPI/eccEMClient-Spec/listeners
  <ECC API root>/extensions/cppClientAPI/eccEMClient-Impl/impl

This should be built as a static library (currently available as a Visual Studio 2010 project), and result in the following binaries:

    * eccEMClient-Impl.lib (or equivalent library file)

    
Example C++ ECC client
~~~~~~~~~~~~~~~~~~~~~~

Dependencies:
  * BOOST
  * SimpleAmqpClient
  * eccCodeUtils
  * amqp-Impl
  * eccCommonDataModel
  * eccEMClient-Impl

Having successfully built the ECC API libraries, you are now able to compile the example client found here::

  <ECC API root>/samples/basicCPPClient
  
NOTE: This client code lives in the SAMPLES folder because it is an example of an ECC client, rather than code relating to the ECC API itself. The code should be built as an executable (currently available as a Visual Studio 2010 project) and result in the following binary:

  * BasicCPPClient.exe (or equivalent)
  
This executable requires the 3rd party dynamic libraries rabbitmq.dll and SimpleAmqpClient.dll (or equivalent) to be present on the executable path to run.

 
Building the Microsoft Visual Studio Solution
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If you are building using Microsoft Windows and Microsoft Visual Studio 2010, you can build the ECC C++ API in two steps:

  1. Install and build your BOOST libraries
  
  2. Open and build the Visual Studio solution::
  
      <ECC API root>/extensions/cppClientAPI/vsSolution/CPP ECC API.sln

You should be able to run the basicCPPClient directly from Visual Studio.
