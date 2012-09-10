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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.ExperimentDataManager;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.*;

import java.awt.event.*;
import org.apache.log4j.Logger;
import java.util.*;




public class EMController implements IEMLifecycleListener
{
  private final Logger emCtrlLogger = Logger.getLogger( EMController.class );
  
  private IExperimentMonitor expMonitor;
  private EMView             mainView;
  private boolean            waitingToStartNextPhase = false;
  
  private IExperimentDataManager expDataMgr;
  private IMetricGeneratorDAO    expMGAccessor;
  private IReportDAO             expReportAccessor;
  private IMeasurementSetDAO     expMSAccessor;
  private Experiment             expInstance;
  
  
  public EMController()
  {    
    expMonitor = EMInterfaceFactory.createEM();
    expMonitor.addLifecyleListener( this );
    
    expDataMgr = new ExperimentDataManager();
  }
  
  public void start( String rabbitIP, UUID emID ) throws Exception
  {
    emCtrlLogger.info( "Trying to connect to Rabbit server on " + rabbitIP );
    
    mainView = new EMView( new MonitorViewListener() );
    mainView.setVisible( true );
    mainView.addWindowListener( new ViewWindowListener() );
    
    try
    { expMonitor.openEntryPoint( rabbitIP, emID ); }
    catch (Exception e)
    {
      emCtrlLogger.error( "Could not open entry point on Rabbit server" );
      throw e; 
    }
    
    boolean dmOK = createExperiment();
    
    if ( !dmOK )
    {
      emCtrlLogger.error( "Had problems setting up the EDM" );
      throw new Exception( "Could not set up EDM" );
    }
  }
  
  // IEMLifecycleListener ------------------------------------------------------
  @Override
  public void onClientConnected( EMClient client )
  {
    if ( mainView != null )
      mainView.addConnectedClient( client );
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
      case eEMLiveMonitoring : 
      {
        mainView.enablePulling( true );
        mainView.enabledPostReportPulling( false );
      } break;
        
      case eEMPostMonitoringReport : 
      {
        mainView.enablePulling( false );
        mainView.enabledPostReportPulling( true );
      } break;
        
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
      
      // Pass to EDM
      if ( expMGAccessor != null && expInstance != null )
      {
        UUID expID = expInstance.getUUID();
        
        while ( mgIt.hasNext() )
        {
          MetricGenerator mg = mgIt.next();
          try { expMGAccessor.saveMetricGenerator( mg, expID ); }
          catch ( Exception e )
          { emCtrlLogger.error( "Failed to store metric generator" ); }
        }
      }
      
      // Update UI
      mgIt = generators.iterator();
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
        {
          // Notify EDM
          if ( expReportAccessor != null )
            try { expReportAccessor.saveReport(report); }
              catch ( Exception e )
              { emCtrlLogger.error( "Failed to store report data" ); }
          
          // Notify UI
          mainView.addLogText( client.getName() + 
                               " got metric data: " + 
                                measures.iterator().next().getValue() );
        }
      }
    }
  }
  
  @Override
  public void onGotSummaryReport( EMClient client, EMPostReportSummary summary )
  {
    if ( client != null && summary != null )
    {
      // Just notify UI
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
      
      // We could notify the EDM of this batch data here, but this demo will
      // only send duplicated data, so we won't do this.
      
      // Notify UI with summary of batch
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
  
  private boolean createExperiment()
  {
    boolean result = false;
    
    try
    {
      expMGAccessor     = expDataMgr.getMetricGeneratorDAO();
      expReportAccessor = expDataMgr.getReportDAO();
      expMSAccessor     = expDataMgr.getMeasurementSetDAO();

      Date expDate = new Date();
      expInstance  = new Experiment();
      expInstance.setName( UUID.randomUUID().toString() );
      expInstance.setDescription( "Sample ExperimentMonitor based experiment" );
      expInstance.setStartTime( expDate );
      expInstance.setExperimentID( expDate.toString() );

      IExperimentDAO expDAO = expDataMgr.getExperimentDAO();
      expDAO.saveExperiment( expInstance );
      result = true;
    }
    catch ( Exception e )
    { emCtrlLogger.error( "Could not initialise experiment"); }
    
    return result;
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
