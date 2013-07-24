ECC API change log
==================


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


