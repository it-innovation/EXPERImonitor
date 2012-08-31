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
//      Created Date :          15-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.basicEMContainer;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import java.awt.event.*;

import java.util.*;




public class EMController implements IEMLifecycleListener
{
  private IExperimentMonitor expMonitor;
  private EMView             mainView;
  private boolean            waitingToStartNextPhase = false;
  
  
  public EMController()
  {    
    expMonitor = EMInterfaceFactory.createEM();
    expMonitor.addLifecyleListener( this );
  }
  
  public void start( String rabbitIP, UUID emID )
  {
    mainView = new EMView( new MonitorViewListener() );
    mainView.setVisible( true );
    mainView.addWindowListener( new ViewWindowListener() );
    
    try
    { expMonitor.openEntryPoint( rabbitIP, emID ); }
    catch (Exception e) {}
  }
  
  // IEMLifecycleListener ------------------------------------------------------
  @Override
  public void onClientConnected( EMClient client )
  {
    if ( mainView != null )
      mainView.addConnectedClient( client.getID(), client.getName() );
  }
  
  @Override
  public void onClientDisconnected( EMClient client )
  {
    //TODO
  }
  
  @Override
  public void onLifecyclePhaseStarted( EMPhase phase )
  {
    // Manage modal behaviour of the view
    switch ( phase )
    {
      case eEMLiveMonitoring : mainView.enablePulling( true ); break;
        
      case eEMPostMonitoringReport : mainView.enabledPostReportPulling( true ); break;
        
      default:
      {
        mainView.enablePulling( false );
        mainView.enabledPostReportPulling( false );
      } break;
    }
    
    EMPhase nextPhase  = expMonitor.getNextPhase();
    mainView.setMonitoringPhaseValue( phase.toString(), nextPhase.toString() );
  }
  
  @Override
  public void onLifecyclePhaseCompleted( EMPhase phase )
  {
    mainView.setNextPhaseValue( expMonitor.getNextPhase().toString() );
    
    if ( waitingToStartNextPhase )
    {
      waitingToStartNextPhase = false;
      try 
      { expMonitor.goToNextPhase(); }
      catch ( Exception e )
      {}
    }
  }
  
  @Override
  public void onFoundClientWithMetricGenerators( EMClient client )
  {
    if ( client != null )
    {
      Set<MetricGenerator> generators = client.getCopyOfMetricGenerators();
      Iterator<MetricGenerator> mgIt = generators.iterator();
      
      while ( mgIt.hasNext() )
      {
        MetricGenerator mg = mgIt.next();
        
        mainView.addLogText( client.getName() + " has metric generator: " + mg.getName() );
      }
    }
  }
  
  @Override
  public void onClientSetupResult( EMClient client, boolean success )
  {
    if ( client != null )
      mainView.addLogText( client.getName() + ( success ? " setup SUCCEEDED" : " setup FAILED") );
  }
  
  @Override
  public void onGotMetricData( EMClient client, Report report )
  {
    if ( client != null && report != null )
    {
      MeasurementSet ms = report.getMeasurementSet();   
      if ( ms != null )
      {
        Set<Measurement> measures = ms.getMeasurements();
        if ( measures != null && !measures.isEmpty() )
          mainView.addLogText( client.getName() + 
                               " got metric data: " + 
                                measures.iterator().next().getValue() );
      }
    }
  }
  
  @Override
  public void onGotSummaryReport( EMClient client, EMPostReportSummary summary )
  {
    if ( client != null && summary != null )
    {
      Iterator<UUID> idIt = summary.getReportedMeasurementSetIDs().iterator();
      while( idIt.hasNext() )
      {
        Report report = summary.getReport( idIt.next() );
        if ( report != null )
        {
          mainView.addLogText( client.getName() + " got summary report from: " +
                               report.getFromDate().toString() + " to: "       +
                               report.getToDate().toString()                   +
                               " count[" + report.getNumberOfMeasurements() + "]" );
        }
      }
    }
  }
  
  @Override
  public void onGotDataBatch( EMClient client, EMDataBatch batch )
  {
    if ( client != null && batch != null )
    {
      int measurementCount = 0;
      
      MeasurementSet ms = batch.getMeasurementSet();
      if ( ms != null )
      {
        Set<Measurement> measures = ms.getMeasurements();
        if ( measures != null ) measurementCount = measures.size();
      }
      
      mainView.addLogText( client.getName() + " got batch ID: " + batch.getID().toString() + 
                           " carrying " + measurementCount + " measures" );
    }
  }
  
  @Override
  public void onClientTearDownResult( EMClient client, boolean success )
  {
    if ( client != null )
      mainView.addLogText( client.getName() + ( success ? " tear-down SUCCEEDED" : " tear-down FAILED") );
  }
  
  // Private methods -----------------------------------------------------------
  private void onViewClosed()
  {
    try { expMonitor.endLifecycle(); }
    catch ( Exception e ) {}
  }
  
  private void startMonitoringProcess()
  {
    if ( expMonitor != null )
      try
      { 
        EMPhase phase = expMonitor.startLifecycle();
        mainView.setMonitoringPhaseValue( phase.toString(), null );
      }
      catch ( Exception e ) {}
  }
  
  private void startUpNextPhase()
  {
    if ( expMonitor != null && !waitingToStartNextPhase )
    {
      if ( expMonitor.isCurrentPhaseActive() )
      {
        try
        {
          waitingToStartNextPhase = true;
          expMonitor.stopCurrentPhase();
        }
        catch ( Exception e )
        {}
      }
      else
        try { expMonitor.goToNextPhase(); }
        catch ( Exception e )
        {}
    }  
  }
  
  private void pullMetrics()
  {
    if ( expMonitor != null )
    {
      //TODO: Visitor pattern!
      Set<EMClient> clients = expMonitor.getCurrentPhaseClients();
      Iterator<EMClient> cIt = clients.iterator();
      
      while ( cIt.hasNext() )
      {
        EMClient client = cIt.next();
        
        Iterator<MetricGenerator> mgIt = client.getCopyOfMetricGenerators().iterator();
        while ( mgIt.hasNext() )
        {
          Iterator<MetricGroup> groupIt = mgIt.next().getMetricGroups().iterator();
          while ( groupIt.hasNext() )
          {
            Iterator<MeasurementSet> setIt = groupIt.next().getMeasurementSets().iterator();
            while( setIt.hasNext() )
            {
              MeasurementSet ms = setIt.next();
              if ( ms != null )
                try { expMonitor.pullMetric( client, ms.getUUID() ); }
                catch (Exception e ) {}
            }
          }
        }
      }
    }
  }
  
  private void pullPostReports()
  {
    if ( expMonitor != null )
    {
      // TODO: Visitor pattern!
      Set<EMClient> clients = expMonitor.getCurrentPhaseClients();
      Iterator<EMClient> cIt = clients.iterator();
      
      while ( cIt.hasNext() )
      {
        EMClient client = cIt.next();
        EMPostReportSummary reportSummary = client.getPostReportSummary();
        
        if ( reportSummary != null )
        {
          Iterator<UUID> reportIt = reportSummary.getReportedMeasurementSetIDs().iterator();
          while ( reportIt.hasNext() )
          {
            Report report = reportSummary.getReport( reportIt.next() );
            if ( report != null )
            {
              // MENTAL HEALTH WARNING: We're only going to pull a small amount 
              // of data that will be created by the client sample demo here
              if ( report.getNumberOfMeasurements() == 2 )
              {
                EMDataBatch batch = new EMDataBatch( report.getMeasurementSet(),
                                                     report.getFromDate(),
                                                     report.getToDate() );
                
                try { expMonitor.requestDataBatch( client, batch ); }
                catch ( Exception e ) {}
              }
            }
          }
        }
      }
    }
  }
  
  // Internal event handling ---------------------------------------------------
  private class ViewWindowListener extends WindowAdapter
  {
    @Override
    public void windowClosed( WindowEvent we )
    { onViewClosed(); }
  }
  
  private class MonitorViewListener implements EMViewListener
  {
    @Override
    public void onStartPhasesButtonClicked()
    { startMonitoringProcess(); }
    
    @Override
    public void onNextPhaseButtonClicked()
    { startUpNextPhase(); }
    
    @Override
    public void onPullMetricButtonClicked()
    { pullMetrics(); }
    
    @Override
    public void onPullPostReportButtonClicked()
    { pullPostReports(); }
  }
}