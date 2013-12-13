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
//      Created Date :          01-Feb-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveData.LiveMonitorController;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.configuration.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.schedulers.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.*;

import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.types.UFAbstractEventManager;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.client.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.visualizers.metrics.BaseMetricVisual;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.dataExport.DataExportController;

import com.vaadin.ui.*;
import com.vaadin.Application;
import com.vaadin.terminal.FileResource;

import java.io.*;
import java.net.URL;
import java.util.*;




public class DashMainController extends UFAbstractEventManager
                                implements Serializable,
                                           IEMLifecycleListener,
                                           ConfigControllerListener,
                                           WelcomeViewListener,
                                           MainDashViewListener,
                                           MonitorControlViewListener,
                                           ClientConnectionsViewListener,
                                           ClientInfoViewListener,
                                           LiveMetricSchedulerListener
{
  private final transient IECCLogger dashMainLog  = Logger.getLogger( DashMainController.class );
  private final transient UIResource viewResource = new UIResource();
  
  private Window                rootWindow;
  private CreateExperimentView  createExpView;
  private MainDashView          mainDashView;
  private WelcomeView           welcomeView;
  private MonitorControlView    monitorControlView;
  private ClientConnectionsView connectionsView;
  private ClientInfoView        clientInfoView;
  
  private transient DashConfigController configController;
 
  private transient IMonitoringEDM      expDataManager;
  private transient IMetricGeneratorDAO expMGAccessor;
  private transient IReportDAO          expReportAccessor;
  private transient Experiment          currentExperiment;
  
  private transient IExperimentMonitor    expMonitor;
  private transient LiveMetricScheduler   liveMetricScheduler;
  private transient LiveMonitorController liveMonitorController;
  
  private boolean entryPointOpened = false;
  private boolean isShuttingDown   = false;
  
  private EMPhase currentPhase   = EMPhase.eEMUnknownPhase;
  
  private UIPushManager pushManager;
  

  public DashMainController()
  {}
  
  public Window getMainWindow()
  { return rootWindow; }
  
  public void initialise( Window rootWin )
  {
    if ( rootWin != null )
    {
      rootWindow = rootWin;
      rootWindow.setStyleName( "eccDashDefault" );      
      rootWindow.addListener( new DashWindowResizeListener() );

      initialiseConfiguration();
      
      // Create common resources
      createCommonUIResources();
      
      expMonitor = EMInterfaceFactory.createEM();
      expMonitor.addLifecyleListener( this );
      
      liveMetricScheduler = new LiveMetricScheduler();
      pushManager         = new UIPushManager( rootWindow );
    }
  }
  
  public void shutdown()
  {
    if ( !isShuttingDown )
    {
      isShuttingDown = true;
      
      if ( pushManager != null )
      {
        pushManager.shutdown();
        pushManager = null;
      }
      
      viewResource.cleanUp();
      
      if ( dashMainLog != null )           dashMainLog.info( "Shutting down the ECC dashboard" );
      if ( liveMetricScheduler != null )   liveMetricScheduler.shutDown();
      if ( liveMonitorController != null ) liveMonitorController.shutDown();
      if ( expMonitor != null )            expMonitor.shutDown();
      if ( mainDashView != null )          mainDashView.shutDownUI();
      
      createExpView       = null;
      mainDashView        = null;
      expMonitor          = null;
      liveMetricScheduler = null;
      
      rootWindow.getApplication().close();
    }
  }
  
  // ConfigControllerListener --------------------------------------------------
  @Override
  public void onConfigurationCompleted()
  {
    String problem = null;
    
    createWelcomeView();
    
    // ECC configuration
    Properties props = configController.getDashboardConfig();
    if ( props == null )
    {
      problem = "Could not find dashboard configuration - using defaults";
      dashMainLog.info( problem );
      welcomeView.addLogInfo( problem );
    }
    else
    { welcomeView.addLogInfo( "Found dashboard configuration" ); }
    
    // EDM configuration
    props = configController.getEDMConfig();
    if ( props == null )
    {
      problem = "Could not find EDM configuration!";
      dashMainLog.error( problem );
      welcomeView.addLogInfo( problem );
    }
    welcomeView.addLogInfo( "Found EDM configuration" );
    
    // Try set up the EDM (errors encapsulated in method)
    if ( !trySetupEDM() )
    {
      problem = "Could not set up EDM!";
      dashMainLog.error( problem );
      welcomeView.addLogInfo( problem );
    }
    
    // EM configuration
    props = configController.getEMConfig();
    if ( props == null )
    {
      problem = "Could not find EM configuration!";
      dashMainLog.error( problem );
      welcomeView.addLogInfo( problem );
    }
    
    // If no problems, allow experiment to start
    if ( problem == null )
    {
      welcomeView.addLogInfo( "Waiting to open RabbitMQ entry point..." );
      welcomeView.setReadyToStart( true );
    }
  }
  
  // IEMLifecycleListener ------------------------------------------------------
  @Override
  public void onClientConnected( EMClient client, boolean reconnected )
  {
    if ( client != null )
    {
      if ( connectionsView != null )
        connectionsView.addClient( client );
      else
      {
        String problem = "Client tried to connect before ECC has fully initialised: " + client.getName();
        if ( mainDashView != null ) mainDashView.addLogMessage( problem );
        dashMainLog.error( problem );
      }
      
      pushManager.pushUIUpdates();
    }
  }
  
  @Override
  public void onClientDisconnected( EMClient client )
  {
    if ( client != null )
    {
      if ( connectionsView != null ) connectionsView.removeClient( client );
      if ( clientInfoView != null  ) clientInfoView.updateClientConnectivityStatus( client, false );
      
      if ( liveMetricScheduler != null )
        try { liveMetricScheduler.removeClient( client ); }
        catch ( Exception e )
        {
          String problem = "Could not unschedule client from live monitoring: " + e.getMessage();
          dashMainLog.error( problem );
          mainDashView.addLogMessage( problem );
        }
      
      if ( liveMonitorController != null ) liveMonitorController.removeClientLiveView( client );
      if ( pushManager != null ) pushManager.pushUIUpdates();
    }
  }
  
  @Override
  public void onClientStartedPhase( EMClient client, EMPhase phase )
  {
    if ( client != null && phase != null)
    {
      connectionsView.updateClientPhase( client.getID(), phase );
      pushManager.pushUIUpdates();
    }
  }
  
  @Override
  public void onLifecyclePhaseStarted( EMPhase phase )
  {
    currentPhase = phase;
    
    mainDashView.setExperimentPhase( phase );
    connectionsView.updateClientsInPhase( phase );
    
    // Perform starting actions, as required
    switch ( currentPhase )
    {
      case eEMLiveMonitoring :
      {
        liveMetricScheduler.start( expMonitor );
      } break;
        
      case eEMPostMonitoringReport :
      {
        liveMetricScheduler.stop();
      } break;
    }
  }
  
  @Override
  public void onLifecyclePhaseCompleted( EMPhase phase )
  {
    String msg = phase.toString() + " has completed.";
    
    mainDashView.addLogMessage( msg );
    dashMainLog.info( msg );
  }
  
  @Override
  public void onNoFurtherLifecyclePhases()
  {
    String msg = "No further experiment phases available";
    
    mainDashView.addLogMessage( msg );
    dashMainLog.info( msg );
    
    mainDashView.setExperimentPhase( EMPhase.eEMProtocolComplete );
  }
  
  @Override
  public void onLifecycleEnded()
  {
    mainDashView.displayMessage( "Experiment ended", "Now creating a new experiment..." );
    
    liveMetricScheduler.stop();
    liveMetricScheduler.reset();
    
    try
    {
      expMonitor.resetLifecycle();
      
      // Create a new experiment
      mainDashView.resetViews();
      
      displayCreateExperimentView();
    }
    catch ( Exception ex )
    {
      String error = "Could not reset experiment lifecycle: " + ex.getMessage();
      dashMainLog.error( error );
      mainDashView.addLogMessage( error );
    }
  }
  
  @Override
  public void onFoundClientWithMetricGenerators( EMClient client, Set<MetricGenerator> newGens )
  {
    if ( client != null )
    {
      // Pass only new metric generators to EDM
      UUID expID = currentExperiment.getUUID();
      
      Iterator<MetricGenerator> mgIt = newGens.iterator();
      while ( mgIt.hasNext() )
      {
        MetricGenerator mg = mgIt.next();
        
        // Check metric generator has at least one entity
        if ( !MetricHelper.getAllEntities( mg ).isEmpty() )
          try 
          { expMGAccessor.saveMetricGenerator( mg, expID ); }
          catch ( Exception e )
          {
            String problem = "Failed to store metric generator";
            mainDashView.addLogMessage( problem );
            dashMainLog.error( problem );
          }
      }
      
      // Update UI with all metric generators
      UUID clientID = client.getID();
      Set<MetricGenerator> allGenerators = client.getCopyOfMetricGenerators();
      
      connectionsView.updateClientSummaryInfo( clientID, 
                                               MetricHelper.getAllEntities( allGenerators ).size(),
                                               MetricHelper.getAllMeasurementSets( allGenerators ).size() );
      
      // If we are not in discovery phase, alert user that new metric model data has arrived
      if ( !currentPhase.equals(EMPhase.eEMDiscoverMetricGenerators) )
        connectionsView.displayAlert( clientID, "New metric data available" );
          
      if ( clientID.equals(connectionsView.getSelectedClientID()) )
      {
        clientInfoView.writeClientInfo( client );
        mainDashView.addLogMessage( client.getName() + "Got new metrics model from " + client.getName() );
      }
        
      pushManager.pushUIUpdates();
    }
  }
  
  @Override
  public void onClientEnabledMetricCollection( EMClient client, UUID entityID, boolean enabled )
  {
  }
  
  @Override
  public void onClientSetupResult( EMClient client, boolean success )
  {
  }
  
  @Override
  public void onClientDeclaredCanPush( EMClient client )
  {
    // DION TO DO?:
    //
    // 1) Add some 'pull'/'push' icons to client view
    //
    // 2) Update the view to display PUSH icon
  }
  
  @Override
  public void onClientDeclaredCanBePulled( EMClient client )
  {
    if ( client != null )
    {
      if ( currentPhase.equals( EMPhase.eEMLiveMonitoring) )
      {
        try 
        { liveMetricScheduler.addClient( client ); }
        catch ( Exception e )
        {
          String problem = "Could not add pulling client to live monitoring: " + e.getMessage();
          mainDashView.addLogMessage( problem );
          dashMainLog.error( problem );
          pushManager.pushUIUpdates();
        }
      }
      else
      {
        String problem = "Client trying to start pull process whilst not in Live monitoring";
        mainDashView.addLogMessage( problem );
        dashMainLog.error( problem );
        pushManager.pushUIUpdates();
      }
    }
  }
  
  @Override
  public void onGotMetricData( EMClient client, Report report )
  {
    if ( client != null && report != null )
    {
      try
      { 
        liveMonitorController.processLiveMetricData( client, report ); 
      }
      catch ( Exception e )
      {
        String problem = "Could not save measurements for client: " + 
                         client.getName() + " because: " + e.getMessage();
        
        mainDashView.addLogMessage( problem );
        dashMainLog.error( problem );
      }
    }
  }
  
  @Override
  public void onGotPROVData( EMClient client, EDMProvReport statement )
  {
    if ( client != null && statement != null )
    {
      try
      {
        liveMonitorController.processLivePROVData( client, statement );
      }
      catch ( Exception e )
      {
        String problem = "Could not save provenance statement for client " +
                         client.getName() + " because: " + e.getMessage();
        
        mainDashView.addLogMessage( problem );
        dashMainLog.error( problem );
      }
    }
  }
  
  @Override
  public void onGotSummaryReport( EMClient client, EMPostReportSummary summary )
  {
    if ( client != null )
    {
      if ( summary != null )
      {
        mainDashView.addLogMessage( "Got metric summary report from " + client.getName() );
        try
        {
          expMonitor.getAllDataBatches( client );
          mainDashView.addLogMessage( "Requested missing metric data from " + client.getName() );
        }
        catch ( Exception e )
        {
          String problem = "Could not request missing metric data from " +
                           client + " because: " + e.getMessage();
          
          mainDashView.addLogMessage( problem );
          dashMainLog.error( problem );
        }
      }
      else
      {
        String problem = "Client " + client.getName() + " provided an empty summary report";
        mainDashView.addLogMessage( problem );
        dashMainLog.error( problem );
      }
    }
  }
  
  @Override
  public void onGotDataBatch( EMClient client, EMDataBatch batch )
  {
    if ( client != null && batch != null )
    {
      // Push batched data into the EDM (if we can)
      if ( expReportAccessor != null )
        try
        { 
          expReportAccessor.saveReport( batch.getBatchReport(), true ); 
        }
        catch ( Exception e )
        {
          String problem = "Could not save batch data report: " + e.getMessage();
          mainDashView.addLogMessage( problem );
          dashMainLog.error( problem );
        }
    }
  }
  
  @Override
  public void onDataBatchMeasurementSetCompleted( EMClient client, UUID measurementSetID )
  {
    if ( client != null && measurementSetID != null )
    {
      mainDashView.addLogMessage( "Client " + client.getName()  + 
                                  " has finished batching MS: " +
                                  measurementSetID.toString() ); 
    }
  }
  
  @Override
  public void onAllDataBatchesRequestComplete( EMClient client )
  {
    if ( client != null )
    { mainDashView.addLogMessage( "Finished getting missing data from " + client.getName() ); }
  }
  
  @Override
  public void onClientTearDownResult( EMClient client, boolean success )
  {
    if ( client != null )
    { mainDashView.addLogMessage( "Client " + client.getName() + " has finished tearing down" ); }
  }
          
  // WelcomeViewListener -------------------------------------------------------
  @Override
  public void onStartECCClicked()
  {
    rootWindow.removeAllComponents(); // Get rid of all other views

    // Create views, if they do not already exist
    if ( mainDashView == null )
    {
      // Main view
      mainDashView = new MainDashView();
      rootWindow.addComponent( (Component) mainDashView.getImplContainer() );
      mainDashView.initialise( pushManager );
      mainDashView.addListener( this );

      // Monitor control view
      monitorControlView = mainDashView.getMonitorControlView();
      monitorControlView.addListener( this );

      // Connections view
      connectionsView = mainDashView.getConnectionsView();
      connectionsView.addListener( this );

      clientInfoView = mainDashView.getClientInfoView();
      clientInfoView.addListener( this );

      // Live monitor view
      liveMonitorController = mainDashView.getLiveMonitorController();
      liveMonitorController.initialse( expReportAccessor );

      // Just initialise this component - don't need to hang on to it
      DataExportController dec = mainDashView.getDataExportController();
      dec.initialise( expMonitor, expReportAccessor );

      // Configure dashboard specifics
      trySetupDashboard();
      
      // Try open Entry Point on RabbitMQ & start a new experiment
      try
      {
        Properties emProps = configController.getEMConfig();
        expMonitor.openEntryPoint( emProps );
        
        entryPointOpened = true;        
      }
      catch ( Exception ex )
      {
       String problem = "Had problems opening an entry point on the RabbitMQ server";
       mainDashView.displayWarning( problem, ex.getMessage() );

       dashMainLog.error( problem + ": " + ex.getMessage() );
      }
    }
    else // Just plug views back into window
      rootWindow.addComponent( (Component) mainDashView.getImplContainer() );
    
    // Start a new experiment if all OK
    if ( entryPointOpened ) displayCreateExperimentView();
  }
  
  @Override
  public void onBackToConfigClicked()
  {
    rootWindow.removeAllComponents(); // Remove all views
    
    // Re-display config view to user
    SimpleView configView = configController.getConfigView();
    
    if ( configView != null )
    {
      // Listen to high level configuration view events
      configView.addListener( this );
      rootWindow.addComponent( (Component) configView.getImplContainer() );
    }
    else dashMainLog.error( "Tried to redisplay config view, but it is not ready" );
  }
  
  // MainDashViewListener ------------------------------------------------------
  @Override
  public void onShutdownECCClicked()
  {
    String[] options = { "no", "yes" };
    
    AlertView av = new AlertView( "Shut down confirmation",
                                  "Shutting down the ECC will cause the experiment to finish and all clients will be " +
                                  "sent a disconnection message. It is strongly recommended you export your data before " +
                                  "shutting down. Are you sure you want to shut down?",
                                  options,
                                  new UserShutdownListener() );
    
    rootWindow.addWindow( av.getWindow() );
  }
  
  // MonitorControlListener ----------------------------------------------------
  @Override
  public void onStartLifecycleClicked()
  {
    // TO BE REMOVED
  }
  
  @Override
  public void onNextPhaseClicked()
  {
    try
    {
      expMonitor.goToNextPhase();
    }
    catch ( Exception ex )
    {
      String error = "Could not move on to next phase: " + ex.getMessage();
      mainDashView.addLogMessage( error );
      dashMainLog.error( error );
    }
  }
  
  @Override
  public void onStopExperimentClicked()
  {
    // Check user really wants to start a new experiment
    String[] options = { "no", "yes" };
    
    AlertView av = new AlertView( "Stop current experiment confirmation",
                                  "Are you sure you want to stop this experiment? " +
                                  "You may want to export your data before finishing.",
                                  options,
                                  new StopExperimentListener() );
    
    rootWindow.addWindow( av.getWindow() );
  }
  
  // ClientConnectionsViewListener ---------------------------------------------
  @Override
  public void onViewClientSelected( UUID clientID )
  {
    if ( clientID != null )
    {
      EMClient client = expMonitor.getClientByID( clientID );
      if ( client != null )
      {
        // Reset alerts on connection view
        connectionsView.displayAlert( clientID, null );
        
        // Update client view
        clientInfoView.writeClientInfo( client );
        
        // Switch focus if necessary
        mainDashView.switchViewFocus( 0 );
      }
    }
  }
  
  @Override
  public void onViewClientDisconnect( UUID clientID, boolean force )
  {
    if ( clientID != null )
    {
        EMClient client = expMonitor.getClientByID( clientID );
        if ( client != null )
        {
          if ( !force ) // Try to disconnect nicely
          {
            try
            { expMonitor.deregisterClient( client , "ECC experimenter disconnected the client" ); }
            catch ( Exception e )
            {
              String error = "Had problems de-registering client: " + client.getName();
              dashMainLog.error( error + " " + e.getMessage() );
              mainDashView.addLogMessage( error );
            }
          }
          else // Assume client is dead and clean our ECC side
          {
            // Notify out to log
            String msg = "Forcing client " + client.getName() + " disconnection";
            dashMainLog.info( msg );
            mainDashView.addLogMessage( msg );
            
            // Manually remove the client
            try { expMonitor.forceClientDisconnection( client ); }
            catch ( Exception e )
            {
              String error = "Had problems forcibly de-registering: " + client.getName();
              dashMainLog.error( error + " " + e.getMessage() );
              mainDashView.addLogMessage( error );
            }
            
            // Remove client scheduling
            if ( liveMetricScheduler != null )
              try { liveMetricScheduler.removeClient( client ); }
              catch ( Exception e )
              {
                String error = "Could not unschedule client: " + client.getName();
                dashMainLog.error( error + " " + e.getMessage() );
                mainDashView.addLogMessage( error );
              }
            
            // Remove client metrics from live view
            if ( liveMonitorController != null )
              liveMonitorController.removeClientLiveView(client);
            
            // Force removal of client from connected list
            connectionsView.removeClient( client );
          }
        }
    }
  }
  
  // ClientInfoViewListener ----------------------------------------------------
  @Override
  public void onAddEntityToLiveView( UUID entityID )
  {
    String problem    = null;
    UUID currClientID = clientInfoView.getCurrentClientID();
    EMClient client   = null;
    
    if ( entityID != null && currClientID != null )
    {
      client = expMonitor.getClientByID( currClientID );
      if ( client != null )
      { 
        Set<MetricGenerator> clientMGs = client.getCopyOfMetricGenerators();
        
        Map<UUID, Entity> entities = MetricHelper.getAllEntities( clientMGs );
        if ( entities.containsKey(entityID) )
        {
          Entity entity = entities.get( entityID );
          Iterator<Attribute> attIt = entity.getAttributes().iterator();
          
          while ( attIt.hasNext() )
          {
            Attribute targetAttribute = attIt.next();
            
            Map<UUID, MeasurementSet> setsToMonitor =
                MetricHelper.getMeasurementSetsForAttribute( targetAttribute, 
                                                             clientMGs );
            
            liveMonitorController.addliveView( client, entity, targetAttribute,
                                               setsToMonitor.values() );
            
          }
        }
        else problem = "Could not put entity into live view: client does not hold entity";
      }
    }
    
    // Report problems, if any
    if ( problem != null )
    {
      dashMainLog.error( problem );
      mainDashView.addLogMessage( problem );
      mainDashView.displayWarning( "Problem adding attribute to Live view", 
                                    problem );
    }
    else // report success
      clientInfoView.displayMessage( "Added entity's attributes to live view",
                                     "For " + client.getName() );
  }
  
  @Override
  public void onAddAttributeToLiveView( UUID attributeID )
  {
    String problem  = null;
    UUID currClientID = clientInfoView.getCurrentClientID();
    
    if ( attributeID != null && currClientID != null )
    {
      EMClient client = expMonitor.getClientByID( currClientID );
      if ( client != null )
      {
        Set<MetricGenerator> clientMGs = client.getCopyOfMetricGenerators();
        
        Attribute targetAttribute = MetricHelper.getAttributeFromID( attributeID, 
                                                                     clientMGs );
        if ( targetAttribute != null )
        {
          Entity entity = MetricHelper.getEntityFromID( targetAttribute.getEntityUUID(), 
                                                        clientMGs );
          if ( entity != null )
          {
            Map<UUID, MeasurementSet> setsToMonitor = 
                MetricHelper.getMeasurementSetsForAttribute( targetAttribute, clientMGs );
          
            if ( !setsToMonitor.isEmpty() )
            {
              liveMonitorController.addliveView( client, entity, targetAttribute,
                                                 setsToMonitor.values() );

              clientInfoView.displayMessage( "Added live view",
                                             "For " + client.getName() + " : " +
                                             targetAttribute.getName() );
            }
            else problem = "Could not put attribute in live view: no measurements are associated with it";
            
          } else problem = "Could not put attribute into live view: attribute has no entity associated with it";
        }
        else problem = "Could not put attribute in live view: client does not hold attribute";
      }
      else problem = "Could not put attribute in live view: client no longer exists";
    }
    else
      problem = "Could not identify attribute selected (null)";
    
    // Report problems, if any
    if ( problem != null )
    {
      dashMainLog.error( problem );
      mainDashView.addLogMessage( problem );
      mainDashView.displayWarning( "Problem adding attribute to Live view", 
                                    problem );
    }
  }
  
  // LiveMetricSchedulerListener -----------------------------------------------
  @Override
  public void onIssuedClientMetricPull( EMClient client )
  {
    mainDashView.addLogMessage( "Pulling metrics from: " + client.getName() );
  }
  
  @Override
  public void onPullMetricFailed( EMClient client, String reason )
  {
    if ( client != null && reason != null )
    {
      String problem = "Had problem pulling metric data from client " +
                       client.getName() + " " + reason;
      
      mainDashView.addLogMessage( problem );
      dashMainLog.error( problem );
    }
  }
  
  // Private methods -----------------------------------------------------------
  private void createWelcomeView()
  {
    rootWindow.removeAllComponents(); // Get rid of other views
    
    welcomeView = new WelcomeView();
    welcomeView.addListener( this );
    
    rootWindow.addComponent( (Component) welcomeView.getImplContainer() );
  }
  
  private void displayCreateExperimentView()
  {    
    if ( rootWindow != null && configController != null )
    { 
      createExpView = 
              new CreateExperimentView( configController.getProjectName(),
                                        new CreateExperimentListener() );
      
      rootWindow.addWindow( createExpView.getWindow() );
    }
  }
  
  private void initialiseConfiguration()
  {
    // Create configuration controller if we do not already have one
    if ( configController == null && rootWindow != null )
    {
      Application thisApp = rootWindow.getApplication();
      String basePath = thisApp.getContext().getBaseDirectory().getAbsolutePath();
    
      configController = new DashConfigController( basePath + "/configs/", this );
      
      // Display config view to user
      SimpleView configView = configController.getConfigView();
      
      if ( configView != null )
      {
        // Listen to high level configuration view events
        configView.addListener( this );
      
        rootWindow.addComponent( (Component) configView.getImplContainer() );
      }
    }
  }
  
  private void tryCreateExperiment( String projName, String expName, String expDesc )
  {
    if ( projName != null && expName != null && expDesc != null &&
         expMonitor != null && monitorControlView != null )
    {
      // Destory view
      Window cevWindow = createExpView.getWindow();
      rootWindow.removeWindow( cevWindow );
      cevWindow = null;
      
      pushManager.restart();
    
      // Create new experiment
      currentExperiment = new Experiment();
      currentExperiment.setExperimentID( projName );
      currentExperiment.setName( expName );
      currentExperiment.setDescription( expDesc );
      currentExperiment.setStartTime( new Date() );
      
      try
      {
        IExperimentDAO expDAO = expDataManager.getExperimentDAO();
        expDAO.saveExperiment( currentExperiment );
        
        Properties emProps = configController.getEMConfig();
        if ( emProps != null )
        {
          String rabbitConInfo = emProps.getProperty( "Rabbit_IP" ) + ":" + 
                                 emProps.getProperty( "Rabbit_Port" );
        
          monitorControlView.setExperimentInfo( rabbitConInfo,
                                                emProps.getProperty( "Monitor_ID" ),
                                                currentExperiment );
        }
      
        // Go straight into live monitoring
        expMonitor.startLifecycle( currentExperiment, EMPhase.eEMLiveMonitoring );
      }
      catch ( Exception e )
      {
        String problem = "Could not start experiment because: " + e.getMessage();
        dashMainLog.error( problem );
        monitorControlView.displayWarning( "Problems starting experiment", problem );
      }
    }
    else
    {
      String problem = "Failed to start experiment: initialisation incomplete";
      dashMainLog.error( problem );
      
      if ( monitorControlView != null )
        monitorControlView.displayWarning( "Problems starting experiment", problem );
    }
  }
  
  private void cancelCreateExperiment()
  {
    // Destory view
    Window cevWindow = createExpView.getWindow();
    rootWindow.removeWindow( cevWindow );
    cevWindow = null;
    
    dashMainLog.info( "Experiment creation was cancelled by user" );
    
    // Return to configuration completed state
    onConfigurationCompleted();
  }
  
  private void finishCurrentExperiment()
  {
    boolean saveSuccess = false;
    
    if ( currentExperiment != null )
      try
      {
        mainDashView.addLogMessage( "Attempting to save experiment end time" );
        
        currentExperiment.setEndTime( new Date() );
        IExperimentDAO expDAO = expDataManager.getExperimentDAO();
        expDAO.finaliseExperiment( currentExperiment );

        saveSuccess = true;
      }
      catch ( Exception ex )
      {
        String problem = "Could not update Experiment finish date" + ex.getMessage();
        mainDashView.addLogMessage( problem );
        dashMainLog.error( problem );
      }
    
    if ( saveSuccess )
    {
      try
      {
        mainDashView.addLogMessage( "Saved OK" );
        expMonitor.endLifecycle(); 
      }
      catch ( Exception e )
      {
        String problem = "Could not re-start ECC because: " + e.getMessage();
        mainDashView.addLogMessage( problem );
        dashMainLog.error( problem );
      }
    }
    else
      mainDashView.displayMessage( "Experiment save error",
                                   "Could not finalise experiment data; please contact EXPERIMEDIA team" );
  }
  
  private boolean trySetupEDM()
  {
    boolean result = false;
    
    if ( configController != null )
    {
      try
      {
        expDataManager    = EDMInterfaceFactory.getMonitoringEDM( configController.getEDMConfig() );
        expMGAccessor     = expDataManager.getMetricGeneratorDAO();
        expReportAccessor = expDataManager.getReportDAO();

        if ( expDataManager.isDatabaseSetUpAndAccessible() )
        {
          welcomeView.addLogInfo( "Started EDM OK" );
          result = true;
        }
        else
        {
          String problem = "EDM database has not been setup correctly ";
          dashMainLog.error( problem );
          welcomeView.addLogInfo( problem );
        }
      }
      catch ( Exception e )
      {
        String problem = "Could not start EDM: " + e.getMessage();
        dashMainLog.error( problem );
        welcomeView.addLogInfo( problem );
      }
    }
    
    return result;
  }
  
  private boolean trySetupDashboard()
  {
    boolean result = false;
    
    if ( configController != null )
    {
      Properties props = configController.getDashboardConfig();
      
      String snapshotVal = props.getProperty( "livemonitor.defaultSnapshotCountMax" );
      if ( snapshotVal != null )
      {
        Integer max = Integer.parseInt(snapshotVal);
        if ( max != null ) BaseMetricVisual.setDefaultSnapshotMaxPointCount( max );
      }
      
      
      String fullURL = props.getProperty( "nagios.fullurl" );
      if ( fullURL != null )
      {
        try
        {
          URL url = new URL( fullURL );
          mainDashView.pointToNAGIOS( url );
          result = true;
        }
        catch (Exception e) 
        {
          String problem = "Could not parse NAGIOS URL for systems monitor";
          dashMainLog.error( problem );
          welcomeView.addLogInfo( problem );
        } 
      }
    }
    
    return result;
  }
  
  private Properties tryGetPropertiesFile( String configName )
  {
    Properties props = null;
    
    Application thisApp = rootWindow.getApplication();
    String basePath = thisApp.getContext().getBaseDirectory().getAbsolutePath();
    
    FileResource resource = 
            new FileResource( new File( basePath + "/WEB-INF/" + configName + ".properties" ),
                              thisApp );
    
    InputStream propsStream = null;
    
    if ( resource != null )
    try { propsStream = (InputStream) new FileInputStream( resource.getSourceFile() ); }
    catch ( IOException ioe )
    {
      String problem = "Could not find " + configName + " configuration file";
      welcomeView.addLogInfo( problem );
      dashMainLog.error( problem );
    }
    
    if ( propsStream != null )
    {
      props = new Properties();
      try
      { 
        props.load( propsStream ); 
        propsStream.close();
      }
      catch ( IOException ioe )
      {
        String problem = "Could not load " + configName + " configuration stream";
        welcomeView.addLogInfo( problem );
        dashMainLog.error( problem );
        props = null;
      }
    }
    else
    {
      String problem = "Could not find properties file " + configName;
      welcomeView.addLogInfo( problem );
      dashMainLog.error( problem );
    }
    
    return props; 
  }
  
  private void createCommonUIResources()
  {
    viewResource.createResource( "experimediaLogo", "img/expLogo.jpg" );
    viewResource.createResource( "versionIcon",     "img/versionIcon.png" );
    viewResource.createResource( "closeIcon",       "img/closeIcon.png" );
    viewResource.createResource( "minimiseIcon",    "img/minimiseIcon.png" );
    viewResource.createResource( "maximiseIcon",    "img/maximiseIcon.png" );
    viewResource.createResource( "alertIcon",       "img/warningIcon.png" );
    viewResource.createResource( "graphIcon",       "img/graphIcon.png"   );
    viewResource.createResource( "chartIcon",       "img/chartIcon.png"   );
    viewResource.createResource( "monitorIcon",       "img/monitorIcon.png"   );
    viewResource.createResource( "databaseIcon",       "img/databaseIcon.png"   );
    viewResource.createResource( "tickIcon",       "img/tickIcon.png"   );
  }
  
  // Event handlers ------------------------------------------------------------
  private void onDashWindowResized()
  {
    if ( mainDashView != null && pushManager != null )
    {
      mainDashView.updateViewport();
      pushManager.pushUIUpdates();
    }
  }

  private class DashWindowResizeListener implements Window.ResizeListener
  {
    @Override
    public void windowResized( Window.ResizeEvent re ) { onDashWindowResized(); }
  }
  
  private class UserShutdownListener implements AlertViewListener
  {
    @Override
    public void onAlertResponse( String option )
    { if ( option.equals("yes") ) shutdown(); }
  }
  
  private class StopExperimentListener implements AlertViewListener
  {
    @Override
    public void onAlertResponse( String option )
    { if ( option.equals("yes") ) finishCurrentExperiment(); }
  }
  
  private class CreateExperimentListener implements CreateExperimentViewListener
  {
    @Override
    public void onStartExperiment( String projName, String expName, String expDesc )
    { tryCreateExperiment( projName, expName, expDesc ); }
    
    @Override
    public void onCancelStartExperiment()
    { cancelCreateExperiment();}
  }
}
