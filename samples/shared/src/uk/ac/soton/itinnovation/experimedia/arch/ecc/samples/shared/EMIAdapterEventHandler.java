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
//      Created Date :          27-Sep-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;

import java.util.*;




public class EMIAdapterEventHandler implements EMIAdapterListener
{
  private boolean          pushable, pullable;
  private EnumSet<EMPhase> phaseSupport;
  
  public EMIAdapterEventHandler()
  {
    phaseSupport = EnumSet.noneOf( EMPhase.class );
  }
  
  /**
   * Creates an ECC event handler with push and pull semantics already set; phase
   * support defaults to discovery & live monitoring only. Extending classes 
   * must still override appropriate methods.
   * 
   * @param push - Sets push behaviour for ECC
   * @param pull - Sets pull behaviour for ECC
   */
  public EMIAdapterEventHandler( boolean push, boolean pull )
  {
    phaseSupport = EnumSet.noneOf( EMPhase.class );
    
    pushable = push;
    pullable = pull;
    
    // Discovery must be supported by default
    phaseSupport.add( EMPhase.eEMLiveMonitoring );
  }
   
  // EMIAdapterListener --------------------------------------------------------
  @Override
  public void onEMConnectionResult( boolean connected, Experiment exp )
  {}

  @Override
  public void onEMDeregistration( String reason )
  {}

  @Override
  public void onDescribeSupportedPhases( EnumSet<EMPhase> phasesOUT )
  { 
    phasesOUT.addAll( phaseSupport ); 
  }

  @Override
  public void onDescribePushPullBehaviours( Boolean[] pushPullOUT )
  {
    pushPullOUT[0] = pushable;
    pushPullOUT[1] = pullable;
  }

  @Override
  public void onPopulateMetricGeneratorInfo()
  {}

  @Override
  public void onDiscoveryTimeOut()
  {}

  @Override
  public void onSetupMetricGenerator(UUID uuid, Boolean[] blns)
  {}

  @Override
  public void onSetupTimeOut(UUID uuid)
  {}

  @Override
  public void onLiveMonitoringStarted()
  {}

  @Override
  public void onStartPushingMetricData()
  {}

  @Override
  public void onPushReportReceived(UUID uuid)
  {}

  @Override
  public void onStopPushingMetricData()
  {}

  @Override
  public void onPullReportReceived(UUID uuid)
  {}

  @Override
  public void onPullMetric(UUID uuid, Report report)
  {}

  @Override
  public void onPullMetricTimeOut(UUID uuid)
  {}

  @Override
  public void onPullingStopped()
  {}

  @Override
  public void onPopulateSummaryReport(uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPostReportSummary emprs)
  {}

  @Override
  public void onPopulateDataBatch(uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMDataBatch emdb)
  {}

  @Override
  public void onReportBatchTimeOut(UUID uuid)
  {}

  @Override
  public void onGetTearDownResult(Boolean[] blns)
  {}

  @Override
  public void onTearDownTimeOut()
  {}
}

