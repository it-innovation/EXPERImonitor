Indicative monitoring use-cases
===============================

To arrive at an effective data monitoring model and process, a number of experiment ‘archetypes’, based on EXPERIMEDIA’s driving experiments, have been considered.

Data source cases
-----------------

EXPERIMEDIA test-beds potentially offer a wide variety of sources of data for the generation QoS, QoE and QoC metrics. In the following sub-sections, a range of data sources are identified for each of the primary metric dimensions.

QoS data sources
~~~~~~~~~~~~~~~~

The set of potential data sources for QoS metrics that could be used for an EXPERIMEDIA test-bed is likely to be the largest of the three primary metric dimensions. This set has been divided into the following sub-sets:

*   Physical environment data sources



*   Physical infrastructure data sources



*   Logical infrastructure data sources



*   Digital content/application data sources



Examples from each of these sub-categories are provided below, see appendix A for the QoS metrics proposed for the EXPERIMEDIA driving experiments.

Physical environment data sources
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Data from the real world environment will be captured by physical sensors connected to the EXPERIMEDIA data monitoring system. This connectivity is unlikely to be direct since it is expected that most QoS based data will be delivered to a monitor server via a digital network and so will initially pass through a third party computing device or ‘data handler’. A few examples of physical environment data sources are provided in the table below:

+-------------+------------------------------+---------------------------------------------+
| **Measure** | **Source**                   | **Data handler**                            |
|             |                              |                                             |
+-------------+------------------------------+---------------------------------------------+
| Temperature | Embedded digital thermometer | Wirelessly connected mobile phone/tablet    |
|             |                              |                                             |
+-------------+------------------------------+---------------------------------------------+
| Light array | Fixed video camera           | Video stream proxy with network access      |
|             |                              |                                             |
+-------------+------------------------------+---------------------------------------------+
| Light array | Embedded video camera        | Wirelessly connected mobile phone/tablet    |
|             |                              |                                             |
+-------------+------------------------------+---------------------------------------------+
| User count  | Physical gate system         | Physically connected PC with network access |
|             |                              |                                             |
+-------------+------------------------------+---------------------------------------------+

Physical infrastructure data sources
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

In the context of an EXPERIMEDIA test-bed, the scope of the term ‘physical infrastructure’ refers to the hardware directly engaged with the creation, storage, access and delivery of digital content associated with an experiment. The physical hardware falling within this scope may also
require a third party data handler (this may be just software but it could also include additional hardware) to provide instrumentation. Examples of such data sources are provided below:

+---------------+----------------------+-----------------------------------------+
| **Measure**   | **Source**           | **Data handler**                        |
|               |                      |                                         |
+---------------+----------------------+-----------------------------------------+
| Battery life  | Mobile device        | None – connected to data monitor server |
|               |                      |                                         |
+---------------+----------------------+-----------------------------------------+
| Packet loss   | Network router       | Network monitor proxy                   |
|               |                      |                                         |
+---------------+----------------------+-----------------------------------------+
| CPU idle time | Mobile device/server | None – connected to data monitor server |
|               |                      |                                         |
+---------------+----------------------+-----------------------------------------+
| Disk reads    | Server               | None – connected to data monitor server |
|               |                      |                                         |
+---------------+----------------------+-----------------------------------------+

Metrics in this category are focussed on the physical operation of a piece of infrastructure hardware without reference to a specific software or service – these metrics are described below.

Logical infrastructure data sources
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Software and services that form a part of the test-bed infrastructure are identified as logical infrastructure data sources. Measurements taken from these sources are described as logical since they describe the performance of the infrastructure that is determined by the behaviour of the software or service that is run on the supporting physical infrastructure.

+-------------------------+-----------------+-----------------------------------------+
| **Measure**             | **Source**      | **Data handler**                        |
|                         |                 |                                         |
+-------------------------+-----------------+-----------------------------------------+
| Number of active VMs    | Cloud service   | None – connected to data monitor server |
|                         |                 |                                         |
+-------------------------+-----------------+-----------------------------------------+
| Connected WiFi users    | Network service | Network monitor proxy                   |
|                         |                 |                                         |
+-------------------------+-----------------+-----------------------------------------+
| Bandwidth throttle time | Network service | Network monitor proxy                   |
|                         |                 |                                         |
+-------------------------+-----------------+-----------------------------------------+


The table above outlines a few examples; once again some intermediate data handlers may be required where the software or service itself may not be modified to interface with EXPERIMEDIA monitoring systems.

Digital content/application data sources
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The final sub-category of QoS data sources focusses on the content or application domain that is particular to the experiment and venue itself. In this case, there will be a wide range of possible measurements (depending on the context of the experiment), but fewer direct and indirect sources from which the data can be gathered. Based on EXPERIMEDIA’s driving experiments, an illustration of the potential measurements from a variety of sources is presented in the table below.

+-----------------------------------+--------------------------------------+------------------------+
| **Measure**                       | **Source**                           | **Driving experiment** |
|                                   |                                      |                        |
+-----------------------------------+--------------------------------------+------------------------+
| POI content accesses/user         | Schladming POI database              | Schladming             |
|                                   |                                      |                        |
+-----------------------------------+--------------------------------------+------------------------+
| Rendered frames/second            | Augmented reality mobile application | Schladming             |
|                                   |                                      |                        |
+-----------------------------------+--------------------------------------+------------------------+
| Video transcoding target bitrate  | AVC transcoding component            | CAR                    |
|                                   |                                      |                        |
+-----------------------------------+--------------------------------------+------------------------+
| Video frame random access speed   | AVC media distribution component     | CAR                    |
|                                   |                                      |                        |
+-----------------------------------+--------------------------------------+------------------------+
| Expert SN posts/programme         | SCC SN analytics component           | FHW                    |
|                                   |                                      |                        |
+-----------------------------------+--------------------------------------+------------------------+
| Virtual location visits/programme | Tholos content repository            | FHW                    |
|                                   |                                      |                        |
+-----------------------------------+--------------------------------------+------------------------+

In these cases, it is assumed that all monitoring of measures will be conducted by the content service or application and delivered to the data monitoring server.

QoE data sources
~~~~~~~~~~~~~~~~

EXPERIMEDIA frames quality of experience as a synthesis of objective and subjective measurements which, combined, provide the experimenter with a rich picture of a user’s experience within the context of their interaction with FMI technologies. Data sources acting as a basis for QoE metrics are sub-divided as follows:

*   Physical sources
    *(objective)*



*   Human-computer interaction sources
    *(objective)*



*   Human activity reporting sources
    *(*
    *subjective*
    *)*



*   Experiential reporting sources
    *(subjective)*



*   Social network analytics sources
    *(subjective)*



As with some QoS monitoring scenarios, QoE metrics may be generated from direct and indirect sources. Indicative examples of each of the five QoE sub-categories are provided in the sections below.

Physical sources
^^^^^^^^^^^^^^^^

Some venues may provide the opportunity to directly instrument users during an experiment – EXPERIMEDIA’s CAR venue is a good example. Physical data capture may vary in its methods including physiological sensors and remote sensing (using physical modelling and analytics).

+-------------+---------------------------------------------------+------------------------------------------+
| **Measure** | **Source**                                        | **Data handler**                         |
|             |                                                   |                                          |
+-------------+---------------------------------------------------+------------------------------------------+
| Heart-rate  | Physically attached sensor & wireless transmitter | Networked monitor proxy (local PC)       |
|             |                                                   |                                          |
+-------------+---------------------------------------------------+------------------------------------------+
| Gesture     | Human motion tracker                              | Networked monitor proxy (local PC)       |
|             |                                                   |                                          |
+-------------+---------------------------------------------------+------------------------------------------+
| Proximity   | Embedded GPS receiver                             | Wirelessly connected mobile phone/tablet |
|             |                                                   |                                          |
+-------------+---------------------------------------------------+------------------------------------------+

The table above presents a number of potential quantitative measures that could be used to augment a quality of experience data set. As with some QoS instrumentation techniques, some of the physical data sources will require a dedicated data handler.

Human-computer interaction sources
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

An additional set of objective measures that can potentially provide insight into the quality of experience is that associated with logging human-computer interactions. Included in this set of
measures are simple forms of device operation (physical button pushes) to more complex interactions, such as the completion of an information processing task via a human-machine dialogue or the movement and actions of an avatar in a virtual space.

+-----------------------+----------------------------------+------------------------------------------+
| **Measure**           | **Source**                       | **Data handler**                         |
|                       |                                  |                                          |
+-----------------------+----------------------------------+------------------------------------------+
| Data input error rate | Venue provider’s web application | Web application server                   |
|                       |                                  |                                          |
+-----------------------+----------------------------------+------------------------------------------+
| Task completion       | Pervasive game engine            | Game engine server                       |
|                       |                                  |                                          |
+-----------------------+----------------------------------+------------------------------------------+
| AR target selection   | AR client viewer                 | Wirelessly connected mobile phone/tablet |
|                       |                                  |                                          |
+-----------------------+----------------------------------+------------------------------------------+

The table above provides some examples of different kinds of human-computer interaction logging in various contexts. Measures such as these would necessarily be gathered by instrumenting the software associated with the delivery of content within the experiment. This quantitative information provides an important contextualising dimension to the subjective data types discussed in the proceeding sections.

Human activity reporting sources
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Many of the experimental scenarios envisaged within EXPERIMEDIA include tight coupling of on-line, user-generated content that includes textual, audio, pictorial and video streams. All of these mediums have the potential to offer self-reporting data relating to personal or group activities and thus can act as a source of quality of experience data; examples are provided in the table below.

+--------------------------+-------------------------------+----------------------------+
| **Measure**              | **Source**                    | **Data handler/processor** |
|                          |                               |                            |
+--------------------------+-------------------------------+----------------------------+
| User clustering/grouping | On-line posting/photo tagging | SCC analytics component    |
|                          |                               |                            |
+--------------------------+-------------------------------+----------------------------+
| Activity/event frequency | On-line posting/photo tagging | SCC analytics component    |
|                          |                               |                            |
+--------------------------+-------------------------------+----------------------------+
| Activity/event frequency | On-line video posting         | AVC analytics component    |
|                          |                               |                            |
+--------------------------+-------------------------------+----------------------------+

Due to the nature of the medium (informal and irregular human communication) from which activity data could be extracted, many of the activity measures will require an intermediary that is capable of formally classifying the subjective reporting of human activities for the purpose of providing data for later analysis.

Experiential reporting sources
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Currently, it is impossible to directly measure a ‘human experience’ – evidence of this phenomenon can only be gathered by using self-reporting techniques that generate data which can be analysed within a theoretical framework. Unlike some of the other measures described above, many of the sampling techniques used in capturing QoE measures gather samples (typically along an ordinal scale) which are aggregated to derive a final metric.

+-----------------------------------+-----------------------+------------------------------------------+
| **Measure**                       | **Source**            | **Data handler**                         |
|                                   |                       |                                          |
+-----------------------------------+-----------------------+------------------------------------------+
| Perceived ease of use (PEU)       | On-line questionnaire | Web application server                   |
|                                   |                       |                                          |
+-----------------------------------+-----------------------+------------------------------------------+
| Positive/negative affect valences | QoE sampler           | Wirelessly connected mobile phone/tablet |
|                                   |                       |                                          |
+-----------------------------------+-----------------------+------------------------------------------+

Two examples of components used in QoE evaluation methods are presented in the table above.
*Perceived ease of use*
is one component that is derived from a sample of scaled questionnaire responses used in Davis’ Technology Acceptance model. Positive and negative affect valences are dimensions that encapsulate self-report samples of experience including bi-polar descriptors such as
*irritable*
and
*relaxed*
,
*attentive*
and
*distracted*
.

QoC data sources
~~~~~~~~~~~~~~~~

Projected EXPERIMEDIA experimental contexts imagine that users will engage with a range of online content from a variety of social media providers such as
*Facebook*
,
*Twitter*
or
*YouTube*
. During an experiment it will be important to recognise that differing online social content providers will be used, however from an experimental data monitoring point of view, the sources of such data will be abstracted by the SCC component. The focus for QoC data sources is therefore sub-divided into analytical dimensions:

*   Content analysis



*   Social analysis







