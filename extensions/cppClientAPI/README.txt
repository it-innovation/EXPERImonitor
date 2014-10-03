Building the EXPERImonitor C++ client
=====================================

Prerequisites
-------------
The EXPERImonitor C++ API is has been developed with a view to providing as much cross-platform compatibility as possible. There are three main 3rd party libraries you will be required to build for your system platform before compiling the EXPERImonitor API itself. These 3rd party libraries are:

  * rabbitmq-c-v0.5.0 (MIT licence)
    - C based protocol client for AMQP
    - Source can be found here: https://github.com/alanxz/rabbitmq-c
    
  * SimpleAmqpClient v2.4 (MIT licence)
    - C++ wrapper for rabbitmq-c-v0.5.0
    - Source can be found here: https://github.com/alanxz/SimpleAmqpClient
    
  * Boost 1.53.0 (Boost licence)
    - Not included in the EXPERImonitor distribution
    - Download from: http://www.boost.org/users/history/version_1_53_0.html
    
  * cmake 2.8 build system
    - Not included in the EXPERImonitor distribution
    - Download from: http://www.cmake.org/
    - Required to build:
        > rabbitmq-c-v0.5.0
        > SimpleAmqpClient V2.4

IMPORTANT NOTE: If you are building using Microsoft Windows and Microsoft Visual Studio 2010, you only need to follow steps:

  * Building the Boost Libraries
  * Building the Microsoft Visual Studio Solution  
  
  
Building the Boost Libraries
----------------------------
Almost all of the C++ libraries constructed in this API require Boost. Once you have downloaded and unzipped the Boost distribution, you sould:

    1. Boot-strap your BOOST libraries (to build system libraries for stage/lib/..)
      - Instructions for this can be found here: http://www.boost.org/doc/libs/1_54_0/more/getting_started/index.html
      
    2. Make sure you define your BOOST root directory path variable (see BOOST instruction set)
          
          
Building rabbitmq-c-v0.5.0
--------------------------
Dependencies: NONE.

A copy of the rabbitmq-c-v0.5.0 source can be found in the following directory:

  <EXPERImonitor API root>/extensions/cppClientAPI/amqpAPI/rabbitmq
    
Instructions on how to use cmake in conjunction with your platform compiler is provided in the README files within this folder. Having successfully compiled rabbitmq-c-v0.5.0 you should have release/debug version of the following binaries:

  * rabbitmq.lib (or equivalent library file)
  * rabbitmq.exp (or equivalent library export definition file)
  * rabbitmq.dll (or equivalent dynamic library executable)

  
SimpleAmqpClient
----------------
Dependencies:
  * BOOST
  * rabbitmq-c-v0.5.0
  
A copy of the SimpleAmqpClient source can be found in the following directory:

  <EXPERImonitor API root>/extensions/cppClientAPI/amqpAPI/simpleamqpclient
    
Instructions on how to use cmake in conjunction with your platform compiler is provided in the README files within this folder. Having successfully compiled SimpleAmqpClient you should have release/debug version of the following binaries:

  * simpleamqpclient.lib (or equivalent library file)
  * simpleamqpclient.exp (or equivalent library export definition file)
  * simpleamqpclient.dll (or equivalent dynamic library executable)

  
C++ EXPERImonitor API libraries
===============================
The following modular libraries need to be built before compiling an EXPERImonitor client:

  * eccCodeUtils.lib
  * amqp-Impl.lib
  * eccCommonDataModel.lib
  * eccEMClient-Impl.lib
  
The details for building each library are provided below.


EXPERImonitor library: eccCodeUtils
-----------------------------------
Dependencies:
  * BOOST
  
The source files required for this library can be found here:

 <EXPERImonitor API root>/extensions/cppClientAPI/eccCodeUtils/Utils
 
This should be built as a static library (currently available as a Visual Studio 2010 project), and result in the following binaries:

  * eccCodeUtils.lib (or equivalent library file)
  

EXPERImonitor library: amqp-Impl
--------------------------------
Dependencies:
    * BOOST
    * rabbitmq-c-v0.5.0
    * SimpleAmqpClient
    * eccCodeUtils
    
The source files required for this library can be found here:

  <EXPERImonitor API root>/extensions/cppClientAPI/amqp-Spec
  <EXPERImonitor API root>/extensions/cppClientAPI/amqp-Impl/amqp
  <EXPERImonitor API root>/extensions/cppClientAPI/amqp-Impl/faces
  
This should be built as a static library (currently available as a Visual Studio 2010 project), and result in the following binaries:

  * amqp-Impl.lib (or equivalent library file)

  
EXPERImonitor library: eccCommonDataModel
-----------------------------------------
Dependencies:
  * BOOST
  * eccCodeUtils

The source files required for this library can be found here:

  <EXPERImonitor API root>/extensions/cppClientAPI/eccCommonDataModel/Base
  <EXPERImonitor API root>/extensions/cppClientAPI/eccCommonDataModel/Experiment
  <EXPERImonitor API root>/extensions/cppClientAPI/eccCommonDataModel/Metrics
  <EXPERImonitor API root>/extensions/cppClientAPI/eccCommonDataModel/Monitor
  
This should be built as a static library (currently available as a Visual Studio 2010 project), and result in the following binaries:

  * eccCommonDataModel.lib (or equivalent library file)
  

EXPERImonitor library:  eccEMClient-Impl
----------------------------------------
Dependencies:
  * BOOST
  * SimpleAmqpClient
  * eccCodeUtils
  * amqp-Impl
  * eccCommonDataModel
  
The source files required for this library can be found here:

  <EXPERImonitor API root>/extensions/cppClientAPI/eccEMClient-Spec/impl
  <EXPERImonitor API root>/extensions/cppClientAPI/eccEMClient-Spec/listeners
  <EXPERImonitor API root>/extensions/cppClientAPI/eccEMClient-Impl/impl

This should be built as a static library (currently available as a Visual Studio 2010 project), and result in the following binaries:

    * eccEMClient-Impl.lib (or equivalent library file)

    
Example C++ EXPERImonitor client
================================    
Dependencies:
  * BOOST
  * SimpleAmqpClient
  * eccCodeUtils
  * amqp-Impl
  * eccCommonDataModel
  * eccEMClient-Impl

Having successfully built the EXPERImonitor API libraries, you are now able to compile the example client found here:

  <EXPERImonitor API root>/samples/basicCPPClient
  
NOTE: This client code lives in the SAMPLES folder because it is an example of an EXPERImonitor client, rather than code relating to the EXPERImonitor API itself. The code should be built as an executable (currently available as a Visual Studio 2010 project) and result in the following binary:

  * BasicCPPClient.exe (or equivalent)
  
This executable requires the 3rd party dynamic libraries rabbitmq.dll and SimpleAmqpClient.dll (or equivalent) to be present on the executable path to run.


-----------------------------------------------------------------------------------------------------------------  
Building the Microsoft Visual Studio Solution
-----------------------------------------------------------------------------------------------------------------
If you are building using Microsoft Windows and Microsoft Visual Studio 2010, you can build the EXPERImonitor C++ API in two steps:

  1. Install and build your BOOST libraries
  
  2. Open and build the Visual Studio solution:
  
      <EXPERImonitor API root>/extensions/cppClientAPI/vsSolution/CPP ECC API.sln

You should be able to run the basicCPPClient directly from Visual Studio.

