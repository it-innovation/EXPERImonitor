Metric data model
=================

The metric data model is used within the ECC by the EM, EDM and the Dashboard (to display and explore the monitoring data), and will be needed by any components generating metrics that should be stored in the ECC. A diagram of the metric data model is given below in :ref:`Figure 1 <fig_metric_data_model>`. Each entity in the diagram is represents a Java class in a library that is used by the ECC components; they are also available to external component developers who may implement their 'metric generators' in Java.


  .. _fig_metric_data_model:

  .. figure:: ../_static/img/reference/metric-data-model.png
     :align: center
     :scale: 100

     **Figure 1:** The ECC metric data model


The metric data model explicitly models an Experiment, which should have one or more Metric Generators associated with it. A Metric Generator can be likened to the thing that observes an Entity in the experiment (which is explicitly modeled as well). An Entity encapsulates a set of Attributes, which can be observed. Note that we do not describe how the Attributes are observed within the Entity, as different Metric Generators may observe them differently. That is, with different Units, for example, 'memory usage' could be measured in kilobytes or megabytes. We will return to this below.

Some examples from EXPERIMEDIA current base-line components are:

* Metric Generator: WeGov

	* Entity: Schladming Twitter Group
	
		* Attribute: People tweeting count
		
		* Attribute: Tweet count
		
	* Entity: Usage of WeGov Dashboard
	
		* Attribute: People using the dashboard
		
		* Attribute: Average number of widgets
		
* Metric Generator: Cloud Metric Generator

	* Entity: Cloud
	
		* Attribute: VM count
		
		* Attribute: Physical host count
		
	* Entity: VM
	
		* Attribute: CPU usage
		
		* Attribute: Memory usage

The rest of the metric data model specifies the observations of the Entity Attributes, which are organised within one or more Metric Groups. A Metric Group can be seen as a logical grouping of metrics, such as metrics relating to Performance, User statistics or Quality of Experience.

Actual Measurements are part of a Measurement Set, which specifies which Attribute it is for and the Metric of the Measurements. A Metric is specified by a Unit and a MetricType. The Unit, as briefly mentioned above, details how the attribute has been measured. For the 'Cloud Metric Generator', above, the 'memory usage' attribute may be measured in kilobytes, for example. This is likely to be a uniform Unit for this Entity as it is a single component that observes it and generates the measurements. However, for user generated metrics, this may not be the case, particularly if users are in different parts of the world where the common units of measurements may differ. Therefore, we model the Unit so that conversions may be possible within the ECC to present a uniform view of monitoring information.

The Metric Type is used to give information about the measurement, which is important for know what statistical operations that can be performed with the data, or how to display data on the UI. It is possible to specify the Metric Type as either nominal, ordinal, interval or ratio. 

There is also a Report mechanism, which is used as a means to transfer measurements from components that generate metrics to the EM, from the EM to the EDM and the UI. It is also is used to give meta-data/statistics for the Measurements of a Measurement Set.