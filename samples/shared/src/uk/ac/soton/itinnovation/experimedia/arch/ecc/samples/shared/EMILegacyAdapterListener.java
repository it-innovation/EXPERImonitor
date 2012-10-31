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
//      Created Date :          23-Oct-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import java.util.UUID;




public interface EMILegacyAdapterListener
{
    /**
     * Notifies the listener of the result of attempting to register a connection
     * with the EM.
     * 
     * @param connected - true if connected.
     */
    void onEMConnectionResult( boolean connected );

    /**
     * Request the listener populate a MetricGenerator set with the information
     * describing what metric data will be sent to the EM during live monitoring
     * and post report phases. Listeners should respond by using the 'setMetricGenerators(..)'
     * method on the EMInterfaceAdapter class.
     */
    void onPopulateMetricGeneratorInfo();

    /**
     * The listener is requested to set up the metric generator indicated by the
     * provided ID and report the result of the set-up process in the OUT 
     * parameter.
     * 
     * @param generatorID - ID of the MetricGenerator
     * @param resultOUT   - result of the set-up (as OUT parameter)
     */
    void onSetupMetricGenerator( UUID generatorID, Boolean[] resultOUT );

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
    void onLastPushProcessed( UUID lastReportID );

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
     * Requests the listener to complete a tear-down process and report the
     * result in the OUT parameter.
     * 
     * @param resultOUT - Result of the tear-down process as an OUT parameter
     */
    void onGetTearDownResult( Boolean[] resultOUT );
}
