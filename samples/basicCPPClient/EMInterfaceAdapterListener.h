/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
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
//      Created Date :          08-Jul-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#pragma once

#include "Experiment.h"
#include "EMPhase.h"
#include "Report.h"
#include "EMPostReportSummary.h"
#include "EMDataBatch.h"




class EMIAdapterListener
{
public:

  typedef boost::shared_ptr<EMIAdapterListener> ptr_t;

  /**
    * Notifies the listener of the result of attempting to register a connection
    * with the EM.
    * 
    * @param connected - true if connected.
    */
  virtual void onEMConnectionResult( const bool connected, 
                                     ecc_commonDataModel::Experiment::ptr_t expInfo ) =0;
    
  /**
    * Notifies the listener of the EM's de-registration of this client. Listeners
    * should call the 'disconnectFromEM()' method to disconnect cleanly.
    * 
    * @param reason - Reason given by EM for de-registration
    */
  virtual void onEMDeregistration( const String& reason ) =0;
    
  /**
    * Requests the listener of the result notify the EMIAdapter of the monitoring
    * phases it supports.
    * 
    * @param phasesOUT - OUT parameter to be used by listener to set the monitoring
    *                    phases they support
    * 
    */
  virtual void onDescribeSupportedPhases( ecc_commonDataModel::EMPhaseSet& phasesOUT ) =0;

  /**
    * Request the listener specifies whether they push or pull or both.
    * 
    * @param pushPullOUT - OUT parameter of two booleans, index 0 for PUSH support
    *                      and index 1 for PULL support
    */
  virtual void onDescribePushPullBehaviours( bool* pushPullOUT ) =0;
    
  /**
    * Request the listener populate a MetricGenerator set with the information
    * describing what metric data will be sent to the EM during live monitoring
    * and post report phases. Listeners should respond by using the 'setMetricGenerators(..)'
    * method on the EMInterfaceAdapter class.
    */
  virtual void onPopulateMetricGeneratorInfo() =0;
    
  /**
    * Notifies the listener that the EM has sent a discover phase time-out; this
    * means that the EM will no longer be accepting metric generator info from
    * this listener.
    * 
    */
  virtual void onDiscoveryTimeOut() =0;

  /**
    * The listener is requested to set up the metric generator indicated by the
    * provided ID and report the result of the set-up process in the OUT 
    * parameter.
    * 
    * @param metricGeneratorID - ID of the MetricGenerator
    * @param resultOUT         - result of the set-up (as OUT parameter)
    */
  virtual void onSetupMetricGenerator( const UUID& metricGeneratorID, 
                                       bool* resultOUT ) =0;
    
  /**
    * Notifies the listener that time has run out for setting up the metric
    * generator identified by the ID. Listeners should stop trying to set up this
    * metric generator if this is on-going.
    * 
    * @param metricGeneratorID - ID of the metric generator being set up
    */
  virtual void onSetupTimeOut( const UUID& metricGeneratorID ) =0;
    
  /**
    * Notifies listener that live monitoring has begun.
    */
  virtual void onLiveMonitoringStarted() =0;

  /**
    * Notifies the listener that it can now start pushing metric data
    */
  virtual void onStartPushingMetricData() =0;
    
  /**
    * Notifies the listener that the EM has successfully received a pushed
    * Report with the supplied ID. (The listener is now able to push the
    * next metric report)
    * 
    * @param lastReportID 
    */
  virtual void onPushReportReceived( const UUID& lastReportID ) =0;
    
  /**
    * Notifies the listener that it should stop pushing metric data.
    */
  virtual void onStopPushingMetricData() =0;
    
  /**
    * Notifies the listener that the EM has successfully received a pulled
    * Report with the supplied ID.
    * 
    * @param lastReportID 
    */
  virtual void onPullReportReceived( const UUID& reportID ) =0;
    
  /**
    * Notifies the listener that it should generate metric data for the
    * supplied MeasurementSet ID - use the OUT parameter to set this data.
    * 
    * @param measurementSetID - MeasurementSet ID to report on
    * @param reportOUT        - Report 'OUT' parameter to insert data into
    */
  virtual void onPullMetric( const UUID& measurementSetID, 
                             ecc_commonDataModel::Report::ptr_t reportOUT ) =0;
    
  /**
    * Notifies the listener that the pulling of metric data from the MeasurementSet
    * identified has timed-out; listeners should not attempt to send this data until
    * asked again.
    * 
    * @param measurementSetID 
    */
  virtual void onPullMetricTimeOut( const UUID& measurementSetID ) =0;

  /**
    * Notifies the listener that the EM has finished pulling (during the
    * Live Monitoring phase)
    * 
    */
  virtual void onPullingStopped() =0;

  /**
    * Notifies the listener that they should construct a post report summary.
    * Use the OUT parameter to set this data.
    * 
    * @param summaryOUT - OUT summary report.
    */
  virtual void onPopulateSummaryReport( ecc_commonDataModel::EMPostReportSummary::ptr_t summaryOUT ) =0;

  /**
    * Notifies the listener that they should populate the data batch OUT
    * parameter with the appropriate data (requested by the EM). The data batch
    * parameter contains the MeasurementSet ID and range of data required.
    * 
    * @param batchOut - Data batch OUT parameter
    */
  virtual void onPopulateDataBatch( ecc_commonDataModel::EMDataBatch::ptr_t batchOut ) =0;
    
  /**
    * Notifies the listener that the data batch report (identified by the ID)
    * is now over-due and to cancel any process in place that is trying to
    * generate and then send it.
    * 
    * @param batchID - ID of the data batch requested by the EM.
    */
  virtual void onReportBatchTimeOut( const UUID& batchID ) =0;

  /**
    * Requests the listener to complete a tear-down process and report the
    * result in the OUT parameter.
    * 
    * @param resultOUT - Result of the tear-down process as an OUT parameter
    */
  virtual void onGetTearDownResult( bool* resultOUT ) =0;
    
  /**
    * Notifies the listener that time has run out to report on the success of
    * its tearing down process.
    * 
    */
  virtual void onTearDownTimeOut() =0;
};


