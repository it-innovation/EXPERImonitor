ECC internal components in more detail
======================================

Experiment deployment & configuration
-------------------------------------

**<CURRENTLY BEING UPDATED>**

Experiment specification
------------------------


**<CURRENTLY BEING UPDATED>**

The specification component consists of a set of configuration files that allow the experimenter to describe the resources and security details for the ECC components and their dependencies. This includes:

*   EM network security certificates



*   EM entry point ID



Experiment monitor
------------------


**<CURRENTLY BEING UPDATED>**

The EM manages the delivery of experiment data (QoS/QoE/QoC metrics) to the EDM from other components, connected via an AMQP bus (RabbitMQ is used as the implementation). Experimenters will also have access to a user interface that controls the experimental monitoring process.

**API Documentation**

See:

*   *experimedia-arch-ecc-em-spec*
    JavaDoc



*   *experimedia-arch-ecc-common-dataModel-metrics*
    JavaDoc



*   *experimedia-arch-ecc-common-dataModel-monitor*
    JavaDoc

*	*T05 ECC samples notes.docx*

Experiment data manager
-----------------------


**<CURRENTLY BEING UPDATED>**

The EDM manages the storage and retrieval of experiment related data. The current release of the EDM persists monitoring data of entities in experiments. This monitoring data is stored in a PostgreSQL 9.1.x (relational) database, according to a schema reflecting the experiment metrics
model, see the *T05 ECC samples notes* document for an overview. Monitoring data is delivered to the EDM for storage by the EM and can be monitored by experimenters via a user interface.

**API Documentation**

See:

*   *experimedia-arch-ecc-edm-spec*
    JavaDoc



*   *experimedia-arch-ecc-common-dataModel-metrics*
    JavaDoc


*	*experimedia-arch-ecc-common-dataModel-experiment*
	JavaDoc

Experiment security
-------------------


**<CURRENTLY BEING UPDATED>**

The SERSCIS Access Modeller (SAM) takes a model of a system (e.g. a set of objects within a computer program or a set of machines
and services on a network) and attempts to verify certain security properties about the system, by exploring all the ways access can propagate through the system.

It is designed to handle dynamic systems (e.g. systems containing factories which may create new objects at runtime) and systems where behaviour of some of the objects is unknown or not trusted.

**Location**: `http://www.serscis.eu/sam/ <http://www.serscis.eu/sam/>`_
				(Open Source)

**API documentation**: `http://www.serscis.eu/sam/ <http://www.serscis.eu/sam/>`_

