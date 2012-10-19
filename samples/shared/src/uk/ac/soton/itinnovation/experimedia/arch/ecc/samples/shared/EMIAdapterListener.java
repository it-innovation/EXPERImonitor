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
//      Created Date :          19-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;

import java.util.*;





public interface EMIAdapterListener
{
    /**
     * Notifies the listener of the result of attempting to register a connection
     * with the EM.
     * 
     * @param connected - true if connected.
     */
    void onEMConnectionResult( boolean connected, Experiment expInfo );
    
    /**
     * Notifies the listener of the EM's de-registration of this client. Listeners
     * should call the 'disconnectFromEM()' method to disconnect cleanly.
     * 
     * @param reason - Reason given by EM for de-registration
     */
    void onEMDeregistration( String reason );
    
    /**
     * Requests the listener of the result notify the EMIAdapter of the monitoring
     * phases it supports.
     * 
     * @param phasesOUT - OUT parameter to be used by listener to set the monitoring
     *                    phases they support
     * 
     */
    void onDescribeSupportPhases( EnumSet<EMPhase> phasesOUT );

    /**
     * Request the listener specifies whether they push or pull or both.
     * 
     * @param pushPullOUT - OUT parameter of two booleans, index 0 for PUSH support
     *                      and index 1 for PULL support
     */
    void onDescribePushPullBehaviours( Boolean[] pushPullOUT );
    
    /**
     * Request the listener populate a MetricGenerator set with the information
     * describing what metric data will be sent to the EM during live monitoring
     * and post report phases. Listeners should respond by using the 'setMetricGenerators(..)'
     * method on the EMInterfaceAdapter class.
     */
    void onPopulateMetricGeneratorInfo();
    
    /**
     * Notifies the listener that the EM has sent a discover phase time-out; this
     * means that the EM will no longer be accepting metric generator info from
     * this listener.
     * 
     */
    void onDiscoveryTimeOut();

    /**
     * The listener is requested to set up the metric generator indicated by the
     * provided ID and report the result of the set-up process in the OUT 
     * parameter.
     * 
     * @param metricGeneratorID - ID of the MetricGenerator
     * @param resultOUT         - result of the set-up (as OUT parameter)
     */
    void onSetupMetricGenerator( UUID metricGeneratorID, Boolean[] resultOUT );
    
    /**
     * Notifies the listener that time has run out for setting up the metric
     * generator identified by the ID. Listeners should stop trying to set up this
     * metric generator if this is on-going.
     * 
     * @param metricGeneratorID - ID of the metric generator being set up
     */
    void onSetupTimeOut( UUID metricGeneratorID );

    /**
     * Notifies the listener that it can now start pushing metric data
     */
    void onStartPushingMetricData();

    /**
     * Notifies the listener that the EM has successfully received a pushed
     * Report with the supplied ID. (The listener is now able to push the
     * next metric report)
     * 
     * @param lastReportID 
     */
    void onPushReportReceived( UUID lastReportID );
    
    /**
     * Notifies the listener that the EM has successfully received a pulled
     * Report with the supplied ID.
     * 
     * @param lastReportID 
     */
    void onPullReportReceived( UUID reportID );
    
    /**
     * Notifies the listener that the pulling of metric data from the MeasurementSet
     * identified has timed-out; listeners should not attempt to send this data until
     * asked again.
     * 
     * @param measurementSetID 
     */
    void onPullMetricTimeOut( UUID measurementSetID );

    /**
     * Notifies the listener that it should stop pushing metric data.
     */
    void onStopPushingMetricData();

    /**
     * Notifies the listener that it should generate metric data for the
     * supplied MeasurementSet ID - use the OUT parameter to set this data.
     * 
     * @param measurementSetID - MeasurementSet ID to report on
     * @param reportOUT        - Report 'OUT' parameter to insert data into
     */
    void onPullMetric( UUID measurementSetID, Report reportOUT );

    /**
     * Notifies the listener that they should construct a post report summary.
     * Use the OUT parameter to set this data.
     * 
     * @param summaryOUT - OUT summary report.
     */
    void onPopulateSummaryReport( EMPostReportSummary summaryOUT );

    /**
     * Notifies the listener that they should populate the data batch OUT
     * parameter with the appropriate data (requested by the EM). The data batch
     * parameter contains the MeasurementSet ID and range of data required.
     * 
     * @param batchOut - Data batch OUT parameter
     */
    void onPopulateDataBatch( EMDataBatch batchOut );
    
    /**
     * Notifies the listener that the data batch report (identified by the ID)
     * is now over-due and to cancel any process in place that is trying to
     * generate and then send it.
     * 
     * @param batchID - ID of the data batch requested by the EM.
     */
    void onReportBatchTimeOut( UUID batchID );

    /**
     * Requests the listener to complete a tear-down process and report the
     * result in the OUT parameter.
     * 
     * @param resultOUT - Result of the tear-down process as an OUT parameter
     */
    void onGetTearDownResult( Boolean[] resultOUT );
    
    /**
     * Notifies the listener that time has run out to report on the success of
     * its tearing down process.
     * 
     */
    void onTearDownTimeOut();
}
