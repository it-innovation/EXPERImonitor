EXPERImonitor
=============

Overview
--------

EXPERImonitor is a framework focused on the management of experiment content that allows developers to explore the relationship between QoS and QoE in complex distributed multimedia systems. The tool is specifically designed to support the observation of systems where user-centricity, mobility, ad hoc participation and real-time access to information are critical to success. 

EXPERImonitor uses a hybrid data model that combines formal low level metric reporting with semantic provenance information. The hybrid approach provides the ability to collect large quantities of measurement data (e.g. service response times, network latency, user satisfaction, etc) whilst allowing for exploration of causation between observations within such data (e.g. user satisfaction in relation to service response time). 

The ability to efficiently traverse experiment content between QoS and QoE is an essential capability for evaluation of complex socio-technical systems.  Data exploration can provide indications of factors that influence each other and is used to segment data for further investigation and analysis. With ever growing big data sets generated by Internet systems, EXPERImonitor can significantly reduce the time from observation to insight.

EXPERImonitor is a web service with a web-based admin interface and a REST API which connects to clients via RabbitMQ in order to receive high-volume monitoring data.  Client APIs are available in multiple languages (Java, Android, C#, C++, Ruby). The web interface offers a live view of incoming metric data and a data explorer view for completed experiments. Data may also be exported to CSV files for further analysis in more specialised tools.

The software was developed primarily in the [EXPERIMEDIA](http://www.experimedia.eu) project to support distributed multimedia experiments. The [3DLive](http://3dliveproject.eu/wp/) and [STEER](http://fp7-steer.eu/) projects also contributed to and used the software.

Screenshots
-----------

**Participant QoE summary**

![Participant QoE summary](http://experimonitor.readthedocs.org/en/latest/_images/explorerQoEUsefulness.png)

**Live metric dashboard**

![Live metric dashboard](http://experimonitor.readthedocs.org/en/latest/_images/dashboard_sad_service_entity2.png)

**Overlaying activities on a QoS graph**

![Overlaying activities on a QoS graph](http://experimonitor.readthedocs.org/en/latest/_images/explorerQoSLiftResponseAll.png)

Copyright
---------

The source code in this distribution is � Copyright University of Southampton IT Innovation Centre 2012-2014.

Licences
--------

The licence for this software is [LGPL v2.1](./LICENCE.txt).

A complete list of licences for this software and associated third party software can be found in the file [IPR.txt](./IPR.txt) and in the [licenses](licenses) folder.

Contact
-------

For further information on collaboration, support or alternative licensing, please contact:

* Website: http://www.it-innovation.soton.ac.uk
* Email: info@it-innovation.soton.ac.uk
* Telephone: +44 (0)23 8059 8866

Binaries
--------

Java artifacts (JAR, source code and JavaDoc) can be found in the [IT Innovation maven repository](http://repo.it-innovation.soton.ac.uk/maven2/release/uk/ac/soton/itinnovation/experimedia/). Non-java binaries are not available to download at the moment.

Documentation
-------------

The primary source of EXPERImonitor documentation can be found in the ['doc' folder](./doc/manual_source/index.rst) or online at [ReadTheDocs](http://experimonitor.readthedocs.org/en/latest/). The documentation is written in RST format; readers have a choice of generating a printable version in a variety of formats including HTML and PDF. Your distribution may have [pre-built documentation](./doc/manual/html) or you can build it yourself following the [instructions provided](./doc/README.txt).

[Javadoc for the source code](./doc/javaDoc) is included in packaged distributions (or from the maven repository). In addition to this, supplementary READMEs relating to EXPERImonitor client development can be found in the ['samples' folder](./samples).

### Building with Vagrant

[Vagrant](http://vagrantup.com) along with a suitable virtual machine system (such as Oracle VirtualBox) can be used to simply build and deploy the EXPERImonitor and all its dependencies including a RabbitMQ server.  This is particularly useful for development and testing.

If you are familiar with vagrant then just rename either `Vagrantfile.tomcat` or `Vagrantfile.glassfish` to `Vagrantfile` and execute `vagrant up`. Further instructions are provided in the documentation.