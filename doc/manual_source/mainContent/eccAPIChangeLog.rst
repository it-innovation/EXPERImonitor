ECC API change log
==================

Summary of V2.0-beta1 changes
-----------------------------
This updated ECC dashboard and API now provides better support for client connectivity over the course of a series of experiments. Given a running RabbitMQ server, experimenters can now use the following features:

1. Start ECC clients before starting up the ECC dashboard or creating a new experiment
2. Run clients continuously between experiments without needing to explicitly re-start/reconnect their clients (particularly useful for ECC clients that are services themselves)
3. Shut down and then restart the ECC dashboard – clients that did not disconnect themselves during this time will engaged in the next new experiment

Please note that [1] will work for v1.2 clients but features [2] and [3] are only available to ECC clients that are re-compiled against the new V2.0-beta API and use the V2.0-beta dashboard (see option 3 below).

For users intending to use the V2.0-beta1 dashboard, please note that the message protocol has changed slightly. When an experiment is ended in the dashboard or the ECC is shutdown clients will no longer receive a disconnection message from the dashboard. If you leave your current code unchanged, you will need to manually disconnect and then re-connect your ECC client for each new experiment. More details for what this means under various scenarios is provided below.


Option 1: Keeping using V1.2 client API
---------------------------------------

Dashboards you can use: V1.2, V2.0-SNAPSHOT, V2.0-beta1
Code changes: none.

If you choose to run the latest dashboard (V2.0-beta1) with your V1.2 client, then your client will no longer receive a disconnect message so may have to be manually halted and then reconnected. If you do not halt your client it will be partially initialised by the ECC dashboard (and appear as a connected client) at the start of the next, new experiment - it will not, however, be able to send further metrics. Re-start and reconnect your client to fix this.

Option 2: Keeping using the current V2.0-SNAPSHOT client API
------------------------------------------------------------
Dashboards you can use: V2.0-SNAPSHOT, V2.0-beta1
Code changes: none.

Note this client API includes basic PROV support. 

Exactly as above described above: if you choose to run the latest dashboard (ECC V2.0-beta) with your current V2.0-SNAPSHOT client, you will need to disconnect and re-start your client manually after each experiment has completed (this dashboard will not send a de-registering message to your client after an experiment is over).

Option 3: Update your client to ECC V2.0-beta changes
-----------------------------------------------------
Dashboards you can use: V2.0-SNAPSHOT, V2.0-beta1
Code changes:

- Re-build your client against new API is required
- You must ensure your create a new metric model for each new experiment
- Minor package name refactors in the EDM specification package
- Minor PROVENANCE API create/get method changes

**Re-build your client**
You must re-build your code against the new ECC API version.

**You must ensure your create a new metric model for each new experiment**
With the previous pattern of behaviour, clients would be created and connected for each experiment and upon connection the ECC would ask for the client's metric model.  Now that a client can remain connected to the RabbitMQ server between experiments, clients must be prepared to re-send their metric model each time a new experiment is started in the ECC (during the 'Discovery' phase: in response to the 'onPopulateMetricGeneratorInfo()' event). In this case, we recommend you re-create an entirely new metric model (new UUIDs will be generated automatically for all model elements). Note that it is recommended that any additional resources directly linked to your metric model should be re-created/updated as necessary.

You also have the option of re-using Entites between experiments. To do this, follow these steps:

1. Create a new Metric Generator and metric group for the new experiment
2. Add the Entities you wish to re-use to the generator
3. Create and map new Measurement Sets to the appropriate Attributes in the usual way

**Minor package name refactors**
Unless you use our metric database locally, these changes will not affect you:
Package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec is now uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics
Package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao is now uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao

**Minor PROVENANCE API create/get method changes**
If your client uses the PROVENANCE API, be aware that EDMProvFactory 'getOrCreate' method calls have been split into separate 'create' and 'get' methods. You must always create Entities, Agents and Activities; if you wish to retrieve them from the EDMProvFactory you should use the appropriate 'get' method.


A few examples of such changes can be seen in our sample clients:

- BasicECCClient: Cleared old metric model when experiment starts (see ECCClientController.java, line 132)

 - PROVECCClient : Moved metric/provenance model creation from construction to when experiment starts (see ClientController.java line 372)

 - HeadlessClient: Moved measurement task scheduling from constructor to when experiment starts (see ECCHeadlessClient.java line 209)

V1.2 changes
------------
Below is a list of significant changes to the ECC API found in version 1.2.

=================================================================  ========================================================================================================================================================================
Change                                                             Description
=================================================================  ========================================================================================================================================================================
Added ECC shut-down confirmation dialogue                          Checks that the experimenter really wants to shutdown the ECC and reminds them of data export functionality
Added C# client support                                            Client writers can now use Microsoft's C# development platform to develop ECC clients
Updated to Vaadin 6.8.10 framework                                 Internal update to the web application used to run the ECC dashboard (includes ICE push framework) - does not impact client side development
Additional visualisation of metrics during live monitoring         The ECC dashboard now offers histograms for nominal and ordinal metric types during live monitoring
Added dynamic entity support                                       ECC clients can now dynamically declare Entities + attributes/new measurement sets at any stage during an experiment
Added entity 'enable/disable' support                              ECC clients can now tell the ECC to enable/disable specific entities during live monitoring; metric data for disabled entities is no longer pulled/accepted from a push
Added dynamic entity example sample                                An example of how declare new entities/measurements and enable/disable them was added to the ECC sample client collection
Added C++ client support                                           Client writers can now develop C++ ECC clients (requires Boost; cmake; RabbitMQ C; RabbitMQ C++ wrapper library)
=================================================================  ========================================================================================================================================================================

V1.1 changes
------------
Below is a list of significant changes to the ECC API found in version 1.1.

=================================================================  ======================================================================================================================================================
Change                                                             Description
=================================================================  ======================================================================================================================================================
Clients can connect to experiment at any time                      ECC clients no longer have connect during the discovery phase of an experiment, but can do so at any time.
Added additional Entity/Attribute query functions in MetricHelper  ECC client writers can now use the MetricHelper class to perform searches on Entities/Attributes/MeasurementSets
Updated dashboard implementation                                   Updated ECC dashboard implementation that fully implements all experiment phases; makes live monitoring of metrics easier & makes deployment simpler
Metric data export added                                           Experimenters can now export metric data held by the ECC at run-time to a CSV file for external analysis
Modified time-stamp standard for data export                       Changed the time-stamping of exported data sets to ISO-8601
Added measurement rules for ECC to follow during live monitoring   Clients can now specify (for each measurement set) how quickly the ECC requests data from the client and how many times during an experiment
Added Android support for ECC client writers                       The ECC API was modified to enable client writers to build for the Android platform
=================================================================  ======================================================================================================================================================


V1.0 changes
------------
Below is a list of significant changes to the ECC API found in version 1.0.

============================================================  ================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================
Change                                                        Description
============================================================  ================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================
Surefire tests added under a configuration profile            EM and EDM libraries now contain JUNIT tests that can be run using the following command: mvn test –P test. You will need to have a locally running RabbitMQ/PostgreSQL service running (respectively) for these tests to complete successfully.
Sphinx documentation started                                  In the next release of the ECC API, all documentation will be maintained in Sphinx format under the ‘doc’ folder. See doc/README.txt for further information.
ECC snapshots on-line                                         Snapshots of the ECC API will be periodically uploaded to IT-Innovation’s barooga server (barooga.it-innovation.soton.ac.uk).
EDC charms added                                              The follow Juju charms have been added to the ECC component: RabbitMQ; PostgreSQL; ECC web dashboard; WeGov client; Headless client
AMQP connection method update                                 EM property file now supports keys ‘username’ and ‘password’ for non-default connection to a RabbitMQ server. The AMQPConnectionFactory class will use this information, if it is available. Sample client code has been updated to demonstrate the use of this functionality.
Updated EM JUNIT test cases                                   The EM test module has been refactored and updated to include further AMQP test cases (including corner-case and performance tests).
Updated EDM JUNIT test cases                                  The EDM test module has been updated to include addition tests for storage/retrieval of: entities, metric generators and reports.
Added experiment ‘restart’ support                            Experiments can be re-started using the JDesktop ECC container application. Connected clients will be sent a disconnection message and the experiment process will reset to wait for new clients.
Web based ECC dashboard available                             A web based view of the ECC is now available as a WAR that should be deployed in the root of an Apache TomCat server. Local RabbitMQ & PostgreSQL are also required.
Updated EMIAdapterListener                                    ECC clients can now use an updated EMIAdapterListener class; this provides additional experiment information; disconnection notification support; extended support for phase and push/pull behaviour description; time-out event notification.
EMILegacyAdapterListener added                                For client writers who wish to test their V0.9 code against V1.0 binaries, a legacy listener class has been added to shield V0.9 code from data/event changes found in V1.0 (these are simply not exposed to old V0.9 code).
EMClient class updated                                        The monitor based class ‘EMClient’ now maintains state about its Post-Reporting activities.
EMDataBatch class updated                                     The data batch class has extended semantics regarding expected and actual data gathered from ECC clients (during a request from the ECC during Post-Reporting phase). Batches also now encapsulate data as a Report.
‘Headless’ client sample added                                An additional sample has been added that runs as a client without a GUI. Additionally, this client demonstrates: Property file-based connection configuration for ECC connection; SSL based secure connection to the ECC; Use of the ECC AgentEDM API to locally store metrics; Use of the ECC AgentEDM API to retrieve metrics for the ECC; Post-reporting phase support (collection of unreported metrics during Live Monitoring); Use of the shared samples classes to support automatic (background) scheduling of metric based measurement.
MetricHelper class added                                      Client writers can now use the MetricHelper class (see the metric data model package) to assist them in organising metric model classes.
UI state fixes to the JDesktop ECC Container                  A number of fixes relating the presentation of experiment state, client connection status, and available entities/metrics have been made to the ECC Container application.
EM/EDM property files now used the JDesktop ECC Container     The JDesktop ECC Container now picks up EM and EDM configuration properties from local files em.properties and edm.properties respectively.
Updated EDM database schema                                   The schema used to stored experiment/metric data has been updated to support the V1.0 data model. Old V0.9 schemas should be removed.
EDM support for ‘synchronized’ data                           The EDM can now mark specific reports/measurements as ‘synchronized’ with the ECC: clients should consider using this when they receive report acknowledgement messages from the ECC during Live Monitoring.
============================================================  ================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================


