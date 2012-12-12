***********************
Experiment Data Manager
***********************

TODO: VE

Overview
========

The Experiment Data Manager (EDM), manages the access and persistence of monitoring data. Within the ECC, this data is collected by the EM and the experimenters get a view on this data from the UI. Storage of monitoring data is done according to the metric data model, allocated on a per-experiment basis and it can be accessed within a time-frame for subsequent analysis.

We also provide an EDM Agent, which can be used for local storage of monitoring data collected by Metric Generators and for transferring this to the EM in the ECC. We assume that it may not be possible for all metrics to be transferred to the ECC during the 'live monitoring phase'. This could be due to network failures during that phase or simply that the metric generator produces monitoring data at a higher frequency than what is sent to the EM.

The EDM in the ECC is stores the monitoring data in a PostgreSQL database. The  current version of the EDM Agent also uses PostgreSQL, but other implementations are planned to give options for devices that cannot run such a database solution.


API
===

Refer to Java doc 