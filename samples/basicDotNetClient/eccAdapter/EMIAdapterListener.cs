/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2012
//
// Copyright in this software belongs to University of Southampton
// IT Innovation Centre of Gamma House, Enterprise Road, 
// Chilworth Science Park, Southampton, SO16 7NS, UK.
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//      Created By :            Simon Crowle
//      Created Date :          10-Apr-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics;

using System.Collections.Generic;
using System;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared
{

public interface EMIAdapterListener
{
    /// <summary>
    ///Notifies the listener of the result of attempting to register a connection
    ///with the EM.
    ///
    ///@param connected - true if connected.
    /// </summary>
    void onEMConnectionResult( bool connected, Experiment expInfo );
    
    /// <summary>
    ///Notifies the listener of the EM's de-registration of this client. Listeners
    ///should call the 'disconnectFromEM()' method to disconnect cleanly.
    ///
    ///@param reason - Reason given by EM for de-registration
    /// </summary>
    void onEMDeregistration( string reason );
    
    /// <summary>
    ///Requests the listener of the result notify the EMIAdapter of the monitoring
    ///phases it supports.
    ///
    ///@param phasesREF - Reference based parameter to be used by listener to set the monitoring
    ///                   phases they support
    ///
    /// </summary>
    void onDescribeSupportedPhases( HashSet<EMPhase> phasesREF );

    /// <summary>
    ///Request the listener specifies whether they push or pull or both.
    ///
    ///@param pushPullOUT - REF parameter of two booleans, index 0 for PUSH support
    ///                     and index 1 for PULL support
    /// </summary>
    void onDescribePushPullBehaviours( ref bool[] pushPullREF );
    
    /// <summary>
    ///Request the listener populate a MetricGenerator set with the information
    ///describing what metric data will be sent to the EM during live monitoring
    ///and post report phases. Listeners should respond by using the 'setMetricGenerators(..)'
    ///method on the EMInterfaceAdapter class.
    /// </summary>
    void onPopulateMetricGeneratorInfo();
    
    /// <summary>
    ///Notifies the listener that the EM has sent a discover phase time-out; this
    ///means that the EM will no longer be accepting metric generator info from
    ///this listener.
    ///
    /// </summary>
    void onDiscoveryTimeOut();

    /// <summary>
    ///The listener is requested to set up the metric generator indicated by the
    ///provided ID and report the result of the set-up process in the OUT 
    ///parameter.
    ///
    ///@param metricGeneratorID - ID of the MetricGenerator
    ///@param resultREF         - result of the set-up (as REF parameter)
    /// </summary>
    void onSetupMetricGenerator(Guid metricGeneratorID, ref bool[] resultREF);
    
    /// <summary>
    ///Notifies the listener that time has run out for setting up the metric
    ///generator identified by the ID. Listeners should stop trying to set up this
    ///metric generator if this is on-going.
    ///
    ///@param metricGeneratorID - ID of the metric generator being set up
    /// </summary>
    void onSetupTimeOut(Guid metricGeneratorID);
    
    /// <summary>
    ///Notifies listener that live monitoring has begun.
    /// </summary>
    void onLiveMonitoringStarted();

    /// <summary>
    ///Notifies the listener that it can now start pushing metric data
    /// </summary>
    void onStartPushingMetricData();
    
    /// <summary>
    ///Notifies the listener that the EM has successfully received a pushed
    ///Report with the supplied ID. (The listener is now able to push the
    ///next metric report)
    ///
    ///@param lastReportID 
    /// </summary>
    void onPushReportReceived(Guid lastReportID);
    
    /// <summary>
    ///Notifies the listener that it should stop pushing metric data.
    void onStopPushingMetricData();
    
    /// <summary>
    ///Notifies the listener that the EM has successfully received a pulled
    ///Report with the supplied ID.
    ///
    ///@param lastReportID 
    /// </summary>
    void onPullReportReceived(Guid reportID);
    
    /// <summary>
    ///Notifies the listener that it should generate metric data for the
    ///supplied MeasurementSet ID - use the OUT parameter to set this data.
    ///
    ///@param measurementSetID - MeasurementSet ID to report on
    ///@param reportREF        - Report reference based parameter to insert data into
    /// </summary>
    void onPullMetric(Guid measurementSetID, Report reportREF);
    
    /// <summary>
    ///Notifies the listener that the pulling of metric data from the MeasurementSet
    ///identified has timed-out; listeners should not attempt to send this data until
    ///asked again.
    ///
    ///@param measurementSetID 
    /// </summary>
    void onPullMetricTimeOut(Guid measurementSetID);

    /// <summary>
    ///Notifies the listener that the EM has finished pulling (during the
    ///Live Monitoring phase)
    ///
    /// </summary>
    void onPullingStopped();

    /// <summary>
    ///Notifies the listener that they should construct a post report summary.
    ///Use the OUT parameter to set this data.
    ///
    ///@param summaryREF - Reference based summary report.
    /// </summary>
    void onPopulateSummaryReport( EMPostReportSummary summaryREF );

    /// <summary>
    ///Notifies the listener that they should populate the data batch OUT
    ///parameter with the appropriate data (requested by the EM). The data batch
    ///parameter contains the MeasurementSet ID and range of data required.
    ///
    ///@param batchOut - Data batch OUT parameter
    /// </summary>
    void onPopulateDataBatch( EMDataBatch batchOut );
    
    /// <summary>
    ///Notifies the listener that the data batch report (identified by the ID)
    ///is now over-due and to cancel any process in place that is trying to
    ///generate and then send it.
    ///
    ///@param batchID - ID of the data batch requested by the EM.
    /// </summary>
    void onReportBatchTimeOut(Guid batchID);

    /// <summary>
    ///Requests the listener to complete a tear-down process and report the
    ///result in the OUT parameter.
    ///
    ///@param resultREF - Result of the tear-down process as an reference parameter
    /// </summary>
    void onGetTearDownResult( ref bool[] resultREF );
    
    /// <summary>
    ///Notifies the listener that time has run out to report on the success of
    ///its tearing down process.
    /// </summary>
    void onTearDownTimeOut();
}

} // namespace
