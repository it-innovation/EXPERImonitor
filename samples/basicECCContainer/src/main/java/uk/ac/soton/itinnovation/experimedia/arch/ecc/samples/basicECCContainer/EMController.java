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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.basicECCContainer;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;

import java.awt.event.*;
import java.io.*;
import java.util.*;




public class EMController implements IEMLifecycleListener
{
  private final Logger emCtrlLogger = LoggerFactory.getLogger(getClass());
  
  private IExperimentMonitor expMonitor;
  private EMLoginView        loginView;
  private EMView             mainView;
  
  private IMonitoringEDM      expDataMgr;
  private IMetricGeneratorDAO expMGAccessor;
  private IReportDAO          expReportAccessor;
  private Experiment          expInstance;
  
  private Timer           pullMetricTimer;
  private PullMetricsTask pullMetricsTask;
  
  
  public EMController()
  {

    expMonitor = EMInterfaceFactory.createEM();
    expMonitor.addLifecyleListener( this );
    
    try
    {
      // Try getting the EDM properties from a local file
      Properties edmProps = tryGetPropertiesFile( "edm" );
      
      // If available, use these properties
      if ( edmProps != null )
        expDataMgr = EDMInterfaceFactory.getMonitoringEDM( edmProps );
      else
        expDataMgr = EDMInterfaceFactory.getMonitoringEDM(); //... or go to default
      
      // Try starting from a local EM properties file
      Properties emProps = tryGetPropertiesFile( "em" );
      if ( emProps != null )
        start( emProps );
      else  // Otherwise, manual entry of basic configuration
      {
        loginView = new EMLoginView();
        loginView.setViewListener( new LoginViewListener() );
        loginView.setVisible( true );
      }
    } 
    catch (Exception ex)
    { emCtrlLogger.error( "Could not create Monitoring EDM", ex ); }
  }
 
  // IEMLifecycleListener ------------------------------------------------------
  @Override
  public void onClientConnected( EMClient client, boolean reconnected )
  {
    if ( mainView != null )
      mainView.addConnectedClient( client );
  }
  
  @Override
  public void onClientDisconnected( UUID clientID )
  {
		EMClient client = expMonitor.getClientByID( clientID );
		
    if ( mainView != null && client != null )
			mainView.removeClient( client );
  }
  
  @Override
  public void onClientStartedPhase( EMClient client, EMPhase phase )
  {
    if ( mainView != null )
      mainView.addLogText( "Client " + client.getName() + " started phase " +
                           phase.toString() );
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
      } break;
        
      case eEMPostMonitoringReport : 
      {
        mainView.enabledPostReportPulling( true );
      } break;
    }
    
    mainView.enableTimeOuts( true );
    mainView.setMonitoringPhaseValue( phase );
  }
  
  @Override
  public void onLifecyclePhaseCompleted( EMPhase phase )
  {
    mainView.addLogText( "Completed phase: " + phase.name() );
  }
  
  @Override
  public void onNoFurtherLifecyclePhases()
  {
    mainView.addLogText( "No further lifecycle phases are available" );
  }
  
  @Override
  public void onLifecycleEnded()
  {    
    try
    {
      expMonitor.resetLifecycle();
      mainView.resetView();
    }
    catch ( Exception ex )
    {
      mainView.addLogText( "Had problems resetting the experiment lifecycle: " + ex.getMessage() );
    }
  }
  
  @Override
  public void onFoundClientWithMetricGenerators( EMClient client, Set<MetricGenerator> newGens )
  {
    if ( client != null )
    {
      Iterator<MetricGenerator> mgIt = newGens.iterator();
      
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
      mgIt = newGens.iterator();
      while ( mgIt.hasNext() )
      {
        MetricGenerator mg = mgIt.next();
        mainView.updateClient( client );
        mainView.addLogText( client.getName() + " has metric generator: " + mg.getName() );
      }
    }
  }
  
  @Override
  public void onClientEnabledMetricCollection( EMClient client, UUID entityID, boolean enabled )
  {
    // DION TO DO:
    //
    // 1) Check in-coming parameters are valid
    //
    // 2) Output a message to the mainView describing this event
    //
    // That's all we're going to do for this development tool
  }
  
  @Override
  public void onClientSetupResult( EMClient client, boolean success )
  {
    if ( client != null )
      mainView.addLogText( client.getName() + ( success ? " setup SUCCEEDED" : " setup FAILED") );
  }
  
  @Override
  public void onClientDeclaredCanPush( EMClient client )
  {
    if ( client != null )
      mainView.addLogText( client.getName() + " has declared PUSHing capable" );
  }
  
  @Override
  public void onClientDeclaredCanBePulled( EMClient client )
  {
    if ( client != null )
      mainView.addLogText( client.getName() + " has declared PULLable capable" );
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
            try { expReportAccessor.saveReport(report, true); }
              catch ( Exception e )
              { emCtrlLogger.error( "Failed to store report data" ); }
          
          // Notify UI
          Measurement m = measures.iterator().next();
          if ( m != null )
            mainView.addLogText( "Data MS[" +
                                 ms.getID().toString() +
                                 "] Time[" + m.getTimeStamp().toString() + "] = " + 
                                 m.getValue() );
        }
      }
    }
  }
  
  @Override
  public void onGotPROVData( EMClient client, EDMProvReport statement )
  {
    if ( client != null && statement != null )
      mainView.addLogText( "Got PROV data from client: " + client.getName() + 
                           " dated at: " + statement.getCopyOfDate().toString() );
  }
  
  @Override
  public void onGotSummaryReport( EMClient client, EMPostReportSummary summary )
  {
    if ( client != null && summary != null )
    {
      // Just notify UI
      boolean gotMeasurementSets = false;
      
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
          
          gotMeasurementSets = true;
        }
      }
      
      // If we didn't get anything, mention it to the user
      if ( !gotMeasurementSets )
        mainView.addLogText( client.getName() + " reported no measurements" );
    }
  }
  
  @Override
  public void onGotDataBatch( EMClient client, EMDataBatch batch )
  {
    if ( client != null && batch != null )
    {      
      // Push batched data into the EDM (if we can)
      if ( expReportAccessor != null )
        try { expReportAccessor.saveReport( batch.getBatchReport(), true ); }
        catch ( Exception e )
        { emCtrlLogger.error( "Could not save batch report data: " + e.getMessage() ); }
    }
  }
  
  @Override
  public void onDataBatchMeasurementSetCompleted( EMClient client, UUID measurementSetID )
  {
    if ( client != null && measurementSetID != null )
    {
      mainView.addLogText( client.getName() + " has finished batching measurement set " + 
                           measurementSetID.toString() );
    }
  }
  
  @Override
  public void onAllDataBatchesRequestComplete( EMClient client )
  {
    if ( client != null )
      mainView.addLogText( "Client " + client.getName() + " has completed post-reporting phase" );
  }
  
  @Override
  public void onClientTearDownResult( EMClient client, boolean success )
  {
    if ( client != null )
      mainView.addLogText( client.getName() + ( success ? " tear-down SUCCEEDED" : " tear-down FAILED") );
  }
  
  // Private methods -----------------------------------------------------------
  private Properties tryGetPropertiesFile( String configName )
  {
    Properties props        = null;
    InputStream propsStream = null;
    
    // Try find the properties file
    File propFile = new File( configName + ".properties" );
    if ( propFile.exists() )
      try
      { propsStream = (InputStream) new FileInputStream( propFile ); }
      catch ( IOException ioe )
      { emCtrlLogger.error( "Could not open " + configName + " configuration file" ); }
    
    // Try load the property stream
    if ( propsStream != null )
    {
      props = new Properties();
      try { props.load( propsStream ); }
      catch ( IOException ioe )
      { 
        emCtrlLogger.error( "Could not load " + configName + " configuration" );
        props = null;
      }
    }

    // Tidy up
    if ( propsStream != null )
      try 
      { propsStream.close(); }
      catch ( IOException ioe )
      { emCtrlLogger.error( "Could not close " + configName + " config file" ); }
    
    return props; 
  }
  
  private boolean clearECCEDM()
  {
    boolean clearedOK = false;
    
    if ( expDataMgr != null )
    {
      try 
      {
        expDataMgr.clearMetricsDatabase();
        clearedOK = true;
      }
      catch ( Exception e )
      { emCtrlLogger.error( "Could not clear EDM database: " + e.getLocalizedMessage()); }
    }
    
    return clearedOK;
  }
  
  private void start( Properties emProps ) throws Exception
  {
    emCtrlLogger.info( "Trying to connect to Rabbit server" );
    
    try
    { 
      expMonitor.openEntryPoint( emProps );
      
      mainView = new EMView( new MonitorViewListener() );
      mainView.setVisible( true );
      mainView.addWindowListener( new ViewWindowListener() );
    }
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
  
  private void onViewClosed()
  {
    try { expMonitor.endLifecycle(); }
    catch ( Exception ex ) 
    {
      emCtrlLogger.error( "Could not end lifecyclye: " + ex.getMessage() );
    }
  }
  
  private void startMonitoringProcess() throws Exception
  {
    createExperiment();
    
    if ( expMonitor == null || expInstance == null )
      throw new Exception( "Experiment monitor and/or experiment information is not ready" );
    
      try
      {         
        EMPhase phase = expMonitor.startLifecycle( expInstance );
        
        mainView.setMonitoringPhaseValue( phase );
      }
      catch ( Exception e ) { throw e; }
  }
  
  private void restartMonitoringProcess()
  {
    if ( expMonitor != null )
    {
      try
      {
        mainView.addLogText( "Ending experiment lifecycle..." );
        expMonitor.endLifecycle();
      }
      catch (Exception e )
      { mainView.addLogText( "Had problems ending experiment lifecycle: " + e.getMessage() ); }
    } 
    else
      mainView.addLogText( "Could not re-start experiment monitor - monitor is NULL" );
  }
  
  private void startUpNextPhase()
  {
    if ( expMonitor != null )
    {
      try
      { 
        expMonitor.goToNextPhase();
        EMPhase phase = expMonitor.getCurrentPhase();

        mainView.setMonitoringPhaseValue( phase );

        mainView.addLogText( "Started phase:" + phase.toString() );
      }
      catch ( Exception ex )
      {
        String error = "Could not go to next phase: " + ex.getMessage();
        mainView.addLogText( error );
        emCtrlLogger.error( error );
      }
    } 
  }
  
  private synchronized void pullMetrics( boolean noteBusyClients )
  {
    if ( expMonitor != null )
    {
      Set<EMClient> clients = expMonitor.getCurrentPhaseClients();
      Iterator<EMClient> cIt = clients.iterator();
      
      while ( cIt.hasNext() )
      {
        EMClient client = cIt.next();
        
        if ( !client.isPullingMetricData() )
        {
          try { expMonitor.pullAllMetrics( client ); }
          catch ( Exception e )
          { emCtrlLogger.error( "Could not pull metrics for client: " +
                                client.getName() + ", because: " + 
                                e.getMessage() ); 
          }
        }
        else if ( noteBusyClients )
        {
          String msg = "Client " + client.getName() + " is busy generating metrics";
          emCtrlLogger.info( msg );
          mainView.addLogText( msg );
        }
      }
    }
  }
  
  private void pullPostReports()
  {
    if ( expMonitor != null )
    {
      Set<EMClient> clients = expMonitor.getCurrentPhaseClients();
      Iterator<EMClient> cIt = clients.iterator();
      
      while ( cIt.hasNext() )
      {
        EMClient client = cIt.next();
        
        try
        { 
          expMonitor.getAllDataBatches( client );
          mainView.addLogText( "Requesting missing data from client: " + client.getName() );
        }
        catch ( Exception e )
        {
          mainView.addLogText( "Could not get any further data from client: " + client.getName() + " because: " + e.getMessage() );
          emCtrlLogger.error( "Could not request data batches from client: " + e.getMessage());
        }
      }
    }
  }
  
  private boolean createExperiment()
  {
    boolean result = false;
    
    if (expDataMgr == null)
    {
      emCtrlLogger.error("EDM not created");
      return false;
    }
    
    Date expDate = new Date();
    expInstance  = new Experiment();
    expInstance.setName( UUID.randomUUID().toString() );
    expInstance.setDescription( "Sample ExperimentMonitor based experiment" );
    expInstance.setStartTime( expDate );
    expInstance.setExperimentID( expDate.toString() );
    
    // If we have a working EDM, set up the EDM interfaces
    if ( expDataMgr.isDatabaseSetUpAndAccessible() )
    {
      try
      {
        expDataMgr.clearMetricsDatabase();
        
        expMGAccessor     = expDataMgr.getMetricGeneratorDAO();
        expReportAccessor = expDataMgr.getReportDAO();

        IExperimentDAO expDAO = expDataMgr.getExperimentDAO();
        expDAO.saveExperiment( expInstance );
        result = true;
      }
      catch ( Exception e )
      { emCtrlLogger.error( "Could not initialise experiment"); }
    }
    else
      emCtrlLogger.error( "Could not access EDM database" );
    
    return result;
  }
  
  private void sendTimeOut( EMClient client )
  {
    emCtrlLogger.info( "Sending timeout" );
    
    try { expMonitor.notifyClientOfTimeOut(client); }
    catch ( Exception e )
    { 
      mainView.addLogText( "Couldn't time-out client " +
                           client.getName() + " : " +
                           e.getLocalizedMessage() );
    }
  }
  
  private void setAutoPulling( boolean autoOn )
  {
    // Set up timer resource if required
    if ( pullMetricTimer == null ) pullMetricTimer = new Timer();
    
    if ( autoOn )
    {
      // Reschedule pulling task if not already active
      if ( pullMetricsTask == null )
      {
        pullMetricsTask = new PullMetricsTask();
        pullMetricTimer.scheduleAtFixedRate(pullMetricsTask, 0, 1000);
      }
    }
    else
      stopAutoPulling();
  }
  
  private void stopAutoPulling()
  {
    if ( pullMetricTimer != null && pullMetricsTask != null )
    {
      pullMetricsTask.cancel();
      pullMetricTimer.purge();
      pullMetricsTask = null;
    }
  }
  
  // Internal event handling ---------------------------------------------------
  private class ViewWindowListener extends WindowAdapter
  {
    @Override
    public void windowClosed( WindowEvent we )
    { onViewClosed(); }
  }
  
  private class LoginViewListener implements EMLoginViewListener
  {
    @Override
    public void onStartECC( String rabbitIP, UUID emID, boolean clearEDM )
    {
      if ( clearEDM )
      {
        if ( clearECCEDM() )
          emCtrlLogger.info( "Successfully cleared EDM data." );
        else
          emCtrlLogger.warn( "Could not clear EDM data" );
      }
      
      if ( loginView != null )
      {
        loginView.setVisible( false );
        loginView.dispose();
        loginView = null;
      }

      try
      {
        Properties basicProps = new Properties();
        basicProps.put( "Rabbit_IP", rabbitIP );
        basicProps.put( "Rabbit_Port", "5672" );
        basicProps.put( "Monitor_ID", emID.toString() );
        
        start( basicProps );
      }
      catch ( Exception e )
      { emCtrlLogger.error( "Could not start the ECC: " + e.getMessage()); }
    }
  }
  
  private class MonitorViewListener implements EMViewListener
  {
    @Override
    public void onStartMonitoringClicked()
    { 
      try 
      { 
        startMonitoringProcess(); 
      }
      catch ( Exception e )
      { emCtrlLogger.error( "Could not start the monitoring process: " + e.getMessage() ); }
    }
    
    @Override
    public void onRestartMonitoringClicked()
    { restartMonitoringProcess(); }
    
    @Override
    public void onNextPhaseButtonClicked()
    { startUpNextPhase(); }
    
    @Override
    public void onPullMetricButtonClicked()
    { pullMetrics( true ); }
    
    @Override
    public void onAutoFireClicked( boolean autoOn )
    { setAutoPulling( autoOn ); }
    
    @Override
    public void onPullPostReportButtonClicked()
    { pullPostReports(); }
    
    @Override
    public void onSendTimeOut( EMClient client )
    { sendTimeOut( client ); }
  }
  
  // Internal task handling ----------------------------------------------------
  private class PullMetricsTask extends TimerTask
  {
    public PullMetricsTask()
    { super(); }
    
    @Override
    public void run()
    { pullMetrics( false ); }
  }
}
