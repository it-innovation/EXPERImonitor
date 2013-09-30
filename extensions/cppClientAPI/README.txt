Building the ECC C++ client
===========================

Prerequisites
-------------
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
    - Required to build:
        > rabbitmq-c-v0.3.0
        > SimpleAmqpClient

IMPORTANT NOTE: If you are building using Microsoft Windows and Microsoft Visual Studio 2010, you only need to follow steps:

  * Building the Boost Libraries
  * Building the Microsoft Visual Studio Solution  
  
  
Building the Boost Libraries
----------------------------
Almost all of the C++ libraries constructed in this API require Boost. Once you have downloaded and unzipped the Boost distribution, you sould:

    1. Boot-strap your BOOST libraries (to build system libraries for stage/lib/..)
      - Instructions for this can be found here: http://www.boost.org/doc/libs/1_54_0/more/getting_started/index.html
      
    2. Make sure you define your BOOST root directory path variable (see BOOST instruction set)
          
          
Building rabbitmq-c-v0.3.0
--------------------------
Dependencies: NONE.

A copy of the rabbitmq-c-v0.3.0 source can be found in the following directory:

  <ECC API root>/extensions/cppClientAPI/amqpAPI/rabbitmq-c-v0.3.0
    
Instructions on how to use cmake in conjunction with your platform compiler is provided in the README files within this folder. Having successfully compiled rabbitmq-c-v0.3.0 you should have release/debug version of the following binaries:

  * rabbitmq.lib (or equivalent library file)
  * rabbitmq.exp (or equivalent library export definition file)
  * rabbitmq.dll (or equivalent dynamic library executable)

  
SimpleAmqpClient
----------------
Dependencies:
  * BOOST
  * rabbitmq-c-v0.3.0
  
A copy of the SimpleAmqpClient source can be found in the following directory:

  <ECC API root>/extensions/cppClientAPI/amqpAPI/rabbit-c-wrapper
    
Instructions on how to use cmake in conjunction with your platform compiler is provided in the README files within this folder. Having successfully compiled SimpleAmqpClient you should have release/debug version of the following binaries:

  * SimpleAmqpClient.lib (or equivalent library file)
  * SimpleAmqpClient.exp (or equivalent library export definition file)
  * SimpleAmqpClient.dll (or equivalent dynamic library executable)

  
C++ ECC API libraries
=====================
The following modular libraries need to be built before compiling an ECC client:

  * eccCodeUtils.lib
  * amqp-Impl.lib
  * eccCommonDataModel.lib
  * eccEMClient-Impl.lib
  
The details for building each library are provided below.


ECC library: eccCodeUtils
-------------------------
Dependencies:
  * BOOST
  
The source files required for this library can be found here:

 <ECC API root>/extensions/cppClientAPI/eccCodeUtils/Utils
 
This should be built as a static library (currently available as a Visual Studio 2010 project), and result in the following binaries:

  * eccCodeUtils.lib (or equivalent library file)
  

ECC library: amqp-Impl
----------------------
Dependencies:
    * BOOST
    * rabbitmq-c-v0.3.0
    * SimpleAmqpClient
    * eccCodeUtils
    
The source files required for this library can be found here:

  <ECC API root>/extensions/cppClientAPI/amqp-Spec
  <ECC API root>/extensions/cppClientAPI/amqp-Impl/amqp
  <ECC API root>/extensions/cppClientAPI/amqp-Impl/faces
  
This should be built as a static library (currently available as a Visual Studio 2010 project), and result in the following binaries:

  * amqp-Impl.lib (or equivalent library file)

  
ECC library: eccCommonDataModel
-------------------------------  
Dependencies:
  * BOOST
  * eccCodeUtils

The source files required for this library can be found here:

  <ECC API root>/extensions/cppClientAPI/eccCommonDataModel/Base
  <ECC API root>/extensions/cppClientAPI/eccCommonDataModel/Experiment
  <ECC API root>/extensions/cppClientAPI/eccCommonDataModel/Metrics
  <ECC API root>/extensions/cppClientAPI/eccCommonDataModel/Monitor
  
This should be built as a static library (currently available as a Visual Studio 2010 project), and result in the following binaries:

  * eccCommonDataModel.lib (or equivalent library file)
  

ECC library:  eccEMClient-Impl
------------------------------
Dependencies:
  * BOOST
  * SimpleAmqpClient
  * eccCodeUtils
  * amqp-Impl
  * eccCommonDataModel
  
The source files required for this library can be found here:

  <ECC API root>/extensions/cppClientAPI/eccEMClient-Spec/impl
  <ECC API root>/extensions/cppClientAPI/eccEMClient-Spec/listeners
  <ECC API root>/extensions/cppClientAPI/eccEMClient-Impl/impl

This should be built as a static library (currently available as a Visual Studio 2010 project), and result in the following binaries:

    * eccEMClient-Impl.lib (or equivalent library file)

    
Example C++ ECC client
======================    
Dependencies:
  * BOOST
  * SimpleAmqpClient
  * eccCodeUtils
  * amqp-Impl
  * eccCommonDataModel
  * eccEMClient-Impl

Having successfully built the ECC API libraries, you are now able to compile the example client found here:

  <ECC API root>/samples/basicCPPClient
  
NOTE: This client code lives in the SAMPLES folder because it is an example of an ECC client, rather than code relating to the ECC API itself. The code should be built as an executable (currently available as a Visual Studio 2010 project) and result in the following binary:

  * BasicCPPClient.exe (or equivalent)
  
This executable requires the 3rd party dynamic libraries rabbitmq.dll and SimpleAmqpClient.dll (or equivalent) to be present on the executable path to run.


-----------------------------------------------------------------------------------------------------------------  
Building the Microsoft Visual Studio Solution
-----------------------------------------------------------------------------------------------------------------
If you are building using Microsoft Windows and Microsoft Visual Studio 2010, you can build the ECC C++ API in two steps:

  1. Install and build your BOOST libraries
  
  2. Open and build the Visual Studio solution:
  
      <ECC API root>/extensions/cppClientAPI/vsSolution/CPP ECC API.sln

You should be able to run the basicCPPClient directly from Visual Studio.
