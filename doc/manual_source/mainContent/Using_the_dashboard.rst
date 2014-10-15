Using the EXPERImonitor Dashboard
=================================

After building and deploying the EXPERImonitor it is then possible to configure the EXPERImonitor, start and control an experimental process using the EXPERImonitor Dashboard.


EXPERImonitor Configuration View
--------------------------------

After deploying the EXPERImonitor, you should be able to access the EXPERImonitor Configuration page in your browser (see :doc:`Getting started with the EXPERImonitor </mainContent/Getting_started_with_the_ECC>` section):

 .. image:: images/dashboard_configuration.png
  :width: 100 %

You should be able to use any configuration by manually entering the details on the right side of the screen, choose between or use as a template:

#. Local configuration loaded from **application.properties** file (located in **eccService/src/main/resources** folder in source distribution or in **EccService-2.2/WEB-INF/classes** folder in your web server deployment).

#. Remote configurations located at WebDAV server config.experimedia.eu.

Once you have selected a configuration, you have an option to save it on config.experimedia.eu by checking "Save or update this configuration on WebDAV server" (existing configurations will be overwritten). **Use this configuration** button will submit the configuration to the EXPERImonitor. In case of configuration being invalid or selected RabbitMQ/Database servers inaccessible, a warning message will be displayed and you will be able to edit the configuration and try again. Otherwise, you should be redirected to the Experiment view of the Dashboard.


EXPERImonitor Experiment View
-----------------------------

Depending on your current EXPERImonitor state (experiment in progress, previous experiments stored in the database, clean install), this view will always show a dialog window with all or some of the following options:

#. Go to current experiment (only if an experiment is in progress).

#. Browse data for one of the latest experiments (previous experiments stored in the database).

#. Start new experiment (always available)

 .. image:: images/dashboard_select_experiment.png
  :width: 100 %


Starting a new experiment
~~~~~~~~~~~~~~~~~~~~~~~~~

Select **Start new experiment** in the dialog window and click **Let's Go**. Give your experiment a name and description beforehand, which is optional. You should see the following:

 .. image:: images/dashboard_empty_experiment.png
   :width: 100 %

Connect your clients and click **Refresh Clients, Entities and Attributes below** button. All clients and their metrics should appear like so (EXPERIMEDIA SAD service used as an example):

 .. image:: images/dashboard_sad_connected.png
   :width: 100 %

Use drop-down menus in respective columns to filter Clients, Entities (by client) or Attributes (by entity). **Download data** links will start CSV file download for:

* Whole experiment (all reported entities and attributes).

* Single client (the client's entities and attributes).

* Single entity (the entity's attributes).

* Single attribute.

 .. image:: images/dashboard_download_example.png
   :width: 100 %

Metrics reported by EXPERImonitor clients can be monitored live in **Live Metrics: up to 10 latest measurements** part of the view by adding them with **Add to Live metrics** controls on entries in  Clients, Entities or Attributes lists. Live metrics can be removed individually or by Client/Entity/Attribute by clicking on **Remove from Live metrics** link.

 .. image:: images/dashboard_sad_service_entity.png
   :width: 100 %

Clicking on **Refresh Clients, Entities and Attributes below** button will clear live metrics display and refresh all clients and metrics lists.



Joining experiment in progress
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Select **Current experiment** in the dialog window if this option is available and click **Let's Go**. You should see currently connected clients and their metrics in Clients, Entities, Attributes part of the view. You will have to use **Refresh Clients, Entities and Attributes below** button to keep that list current.



Browse data for one of the latest experiments
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If the database selected in your current configuration contains data from previously ran experiments, a **Browse data for one of the latest experiments** option should be visible on the initial Experiment view dialog window. Select that option and one of the previous experiments from the drop down list underneath and click on **Let's Go** button. You should be redirected to the data view of the selected experiment where all reported metrics should be available for download for:

* the whole experiment
* an entity
* an attribute

(data per client is not currently available):

 .. image:: images/dashboard_view_data.png
   :width: 100 %

Other controls in this view:

#. **Back to experiment monitor**: goes back to Experiment view.

#. **Select different experiment** lets switch to data view for other previous experiments:

 .. image:: images/dashboard_data_other_experiments.png
   :width: 100 %


Experiment controls
~~~~~~~~~~~~~~~~~~~

* **Stop experiment** in the top right corner will disconnect all clients and stop currently running experiment. New *Options* link will become available:

  .. image:: images/dashboard_stop_experiment_options.png
   :width: 100 %

* **Restart EXPERImonitor** in the bottom left corner will (confirmation is displayed beforehand):

	#. stop currently running experiment (if one is in progress),

	#. disconnect all clients,

	#. close database and Rabbit MQ connections,

	#. redirect to Configuration view.
