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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.*;
import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.types.UFAbstractEventManager;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.client.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.dataExport.DataExportController;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics.LiveMonitorController;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.processors.*;

import com.vaadin.ui.*;
import com.vaadin.Application;
import com.vaadin.terminal.FileResource;
import org.vaadin.artur.icepush.ICEPush;

import java.io.*;
import java.net.URL;
import java.util.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UIResource;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics.visualizers.BaseMetricVisual;




public class DashMainController extends UFAbstractEventManager
                                implements Serializable,
                                           IEMLifecycleListener,
                                           WelcomeViewListener,
                                           MainDashViewListener,
                                           MonitorControlViewListener,
                                           ClientConnectionsViewListener,
                                           ClientInfoViewListener,
                                           LiveMetricSchedulerListener
{
  private final transient IECCLogger dashMainLog  = Logger.getLogger( DashMainController.class );
  private final transient UIResource viewResource = new UIResource();
  
  private Properties dashboardProps;
  private Properties edmProps;
  private Properties emProps;
  
  private Window                rootWindow;
  private MainDashView          mainDashView;
  private WelcomeView           welcomeView;
  private MonitorControlView    monitorControlView;
  private ClientConnectionsView connectionsView;
  private ClientInfoView        clientInfoView;
 
  private transient IMonitoringEDM      expDataManager;
  private transient IMetricGeneratorDAO expMGAccessor;
  private transient IReportDAO          expReportAccessor;
  private transient Experiment          currentExperiment;
  
  private transient IExperimentMonitor    expMonitor;
  private transient LiveMetricScheduler   liveMetricScheduler;
  private transient LiveMonitorController liveMonitorController;
  
  private boolean isShuttingDown       = false;
  private boolean waitingForPhaseToEnd = false;
  private EMPhase currentPhase         = EMPhase.eEMUnknownPhase;
  
  private ICEPush icePusher;
  

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

      createWelcomeView();

      if ( intitialiseECCResources() )
      {
        liveMetricScheduler = new LiveMetricScheduler();
        welcomeView.setReadyToStart( true );
      }
    }
  }
  
  public void shutdown()
  {
    if ( !isShuttingDown )
    {
      isShuttingDown = true;
      
      if ( icePusher != null )
      {
        rootWindow.removeComponent( icePusher );
        icePusher = null;
      }
      
      viewResource.cleanUp();
      
      if ( dashMainLog != null )           dashMainLog.info( "Shutting down the ECC dashboard" );
      if ( liveMetricScheduler != null )   liveMetricScheduler.shutDown();
      if ( liveMonitorController != null ) liveMonitorController.shutDown();
      if ( expMonitor != null )            expMonitor.shutDown();
      if ( mainDashView != null )          mainDashView.shutDownUI();
      
      mainDashView        = null;
      expMonitor          = null;
      liveMetricScheduler = null;
      
      rootWindow.getApplication().close();
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
      
      icePusher.push();
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
      if ( icePusher != null ) icePusher.push();
    }
  }
  
  @Override
  public void onClientStartedPhase( EMClient client, EMPhase phase )
  {
    if ( client != null && phase != null)
    {
      connectionsView.updateClientPhase( client.getID(), phase);
      icePusher.push();
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
    }
  }
  
  @Override
  public void onLifecyclePhaseCompleted( EMPhase phase )
  {
    // Perform stopping actions, as required
    switch ( currentPhase )
    {
      case eEMLiveMonitoring: liveMetricScheduler.stop();
    }
    
    // If we have been waiting for a phase to end (so we can move to the next)
    // then advance the phase
    if ( waitingForPhaseToEnd )
      try
      {
        waitingForPhaseToEnd = false;
        expMonitor.goToNextPhase();
      }
      catch ( Exception e )
      {
        String problem = "Could not advance to next phase: " + e.getMessage();
        mainDashView.addLogMessage( problem );
        dashMainLog.error( problem );
      }
  }
  
  @Override
  public void onNoFurtherLifecyclePhases()
  {
    waitingForPhaseToEnd = false;
    
    mainDashView.setExperimentPhase( EMPhase.eEMProtocolComplete );
  }
  
  @Override
  public void onLifecycleReset()
  {
    // Reset to start and wait for new connections
    waitingForPhaseToEnd = false;
    
    liveMetricScheduler.reset();
    mainDashView.resetViews();
    
    // Create a new experiment
    createExperiment();
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
        
      icePusher.push();
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
          icePusher.push();
        }
      }
      else
      {
        String problem = "Client trying to start pull process whilst not in Live monitoring";
        mainDashView.addLogMessage( problem );
        dashMainLog.error( problem );
        icePusher.push();
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
        liveMonitorController.processLiveData( client, report ); 
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
    if ( emProps != null )
    try
    {
      rootWindow.removeAllComponents(); // Get rid of the welcome view
      
      createCommonUIResources(); // Create common resources before we create the main view
      
      icePusher = new ICEPush();
      rootWindow.addComponent( icePusher );
      
      mainDashView = new MainDashView();
      mainDashView.addListener( this );
      rootWindow.addComponent( (Component) mainDashView.getImplContainer() );
    
      expMonitor.openEntryPoint( emProps );
      createExperiment();
    
      mainDashView.initialise( icePusher );
      
      monitorControlView = mainDashView.getMonitorControlView();
      monitorControlView.addListener( this );
      
      connectionsView = mainDashView.getConnectionsView();
      connectionsView.addListener( this );
      
      clientInfoView = mainDashView.getClientInfoView();
      clientInfoView.addListener( this );
      
      liveMonitorController = mainDashView.getLiveMonitorController();
      liveMonitorController.initialse( expReportAccessor );
      
      // Just initialise this component - don't need to hang on to it
      DataExportController dec = mainDashView.getDataExportController();
      dec.initialise( expMonitor, expReportAccessor );
      
      trySetupDashboard();
      
      if ( emProps != null && currentExperiment != null )
      {
        String rabbitConInfo = emProps.getProperty( "Rabbit_IP" ) + ":" + 
                               emProps.getProperty( "Rabbit_Port" );
        
        monitorControlView.setExperimentInfo( rabbitConInfo,
                                              emProps.getProperty( "Monitor_ID" ),
                                              currentExperiment );
      }
    }
    catch ( Exception e )
    {
      String problem = "Had problems with Rabbit: " + e.getMessage();
      dashMainLog.error( problem );
      welcomeView.addLogInfo( problem );
    }
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
    try
    { 
      expMonitor.startLifecycle( currentExperiment ); 
    }
    catch ( Exception e )
    { dashMainLog.error( "Could not start experiment lifecycle: " + e.getMessage() ); }
  }
  
  @Override
  public void onNextPhaseClicked()
  {
    if ( expMonitor != null )
    {      
      if ( expMonitor.isCurrentPhaseActive() && !waitingForPhaseToEnd )
      {
        try
        {
          waitingForPhaseToEnd = true;
          expMonitor.stopCurrentPhase();
        }
        catch ( Exception e )
        { 
          dashMainLog.error( "Could not stop phase: " + expMonitor.getCurrentPhase().name() +
                             " because " + e.getMessage());
          
          waitingForPhaseToEnd = false;
        }
      }
      else
        try { expMonitor.goToNextPhase(); }
        catch ( Exception e )
        { dashMainLog.error( "Could not stop current phase: it is inactive"); }
    }
  }
  
  @Override
  public void onRestartExperimentClicked()
  {
    mainDashView.addLogMessage( "Attempting to re-start with a new experiment" );
    try
    { expMonitor.resetLifecycle(); }
    catch ( Exception e )
    {
      String problem = "Could not re-start ECC because: " + e.getMessage();
      mainDashView.addLogMessage( problem );
      dashMainLog.error( problem );
    }
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
    welcomeView = new WelcomeView();
    welcomeView.addListener( this );
    
    rootWindow.addComponent( (Component) welcomeView.getImplContainer() );
  }
  
  private boolean createExperiment()
  {
    Date expDate = new Date();
    currentExperiment  = new Experiment();
    currentExperiment.setName( UUID.randomUUID().toString() );
    currentExperiment.setDescription( "Current experiment" );
    currentExperiment.setStartTime( expDate );
    currentExperiment.setExperimentID( expDate.toString() );
    
    try
    {      
      IExperimentDAO expDAO = expDataManager.getExperimentDAO();
      expDAO.saveExperiment( currentExperiment );
    }
    catch ( Exception e )
    {
      dashMainLog.error( "Could not create new experiment: " + e.getMessage() );
      return false;
    }
    
    return true;
  }
  
  private boolean intitialiseECCResources()
  {
    // ECC configuration
    dashboardProps = tryGetPropertiesFile( "dashboard" );
    if ( dashboardProps == null )
    {
      String problem = "Could not find dashboard configuration - using defaults";
      dashMainLog.info( problem );
      welcomeView.addLogInfo( problem );
    }
    else
    { welcomeView.addLogInfo( "Found dashboard configuration" ); }
    
    // EDM configuration
    edmProps = tryGetPropertiesFile( "edm" );
    if ( edmProps == null )
    {
      String problem = "Could not find EDM configuration!";
      dashMainLog.error( problem );
      welcomeView.addLogInfo( problem );
      return false;
    }
    welcomeView.addLogInfo( "Found EDM configuration" );
    
    // Try set up the EDM (errors encapsulated in method)
    if ( !trySetupEDM() ) return false;
    
    // EM configuration
    emProps = tryGetPropertiesFile( "em" );
    if ( emProps == null )
    {
      String problem = "Could not find EM configuration!";
      dashMainLog.error( problem );
      welcomeView.addLogInfo( problem );
      return false;
    }
   
    // Ready the EM for connection
    expMonitor = EMInterfaceFactory.createEM();
    expMonitor.addLifecyleListener( this );

    welcomeView.addLogInfo( "Waiting to open client enty point." );
    return true;
  }
  
  private boolean trySetupEDM()
  {
    boolean result = false;
    
    try
    {
      expDataManager = EDMInterfaceFactory.getMonitoringEDM( edmProps );
      expMGAccessor  = expDataManager.getMetricGeneratorDAO();
      expReportAccessor  = expDataManager.getReportDAO();
      
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
    
    return result;
  }
  
  private boolean trySetupDashboard()
  {
    boolean result = false;
    
    if ( dashboardProps != null )
    {
      String snapshotVal = dashboardProps.getProperty( "livemonitor.defaultSnapshotCountMax" );
      if ( snapshotVal != null )
      {
        Integer max = Integer.parseInt(snapshotVal);
        if ( max != null ) BaseMetricVisual.setDefaultSnapshotMaxPointCount( max );
      }
      
      
      String fullURL = dashboardProps.getProperty( "nagios.fullurl" );
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
    if ( mainDashView != null && icePusher != null )
    {
      mainDashView.updateViewport();
      icePusher.push();
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
    {
      if ( option.equals("yes") ) shutdown();
    }
  }
}
