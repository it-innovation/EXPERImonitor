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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.processors.LiveMetricScheduler;
import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.types.UFAbstractEventManager;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.*;

import com.vaadin.ui.*;
import com.vaadin.Application;
import com.vaadin.terminal.FileResource;

import java.io.*;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricHelper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMDataBatch;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPostReportSummary;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.processors.LiveMetricSchedulerListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics.LiveMonitorController;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IReportDAO;





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
  private final transient Logger dashMainLog = Logger.getLogger( DashMainController.class );
  
  private Properties edmProps;
  private Properties emProps;
  
  private Window                rootWindow;
  private MainDashView          mainDashView;
  private WelcomeView           welcomeView;
  private MonitorControlView    monitorControlView;
  private ClientConnectionsView connectionsView;
  private ClientInfoView        clientInfoView;
  
  private transient LiveMonitorController liveMonitorController;
  
  private transient IMonitoringEDM      expDataManager;
  private transient IMetricGeneratorDAO expMGAccessor;
  private transient IReportDAO          expReportAccessor;
  private transient Experiment          currentExperiment;
  
  private transient IExperimentMonitor  expMonitor;
  private transient LiveMetricScheduler liveMetricScheduler;
  
  private boolean isShuttingDown          = false;
  private boolean waitingToStartNextPhase = false;
  private EMPhase currentPhase            = EMPhase.eEMUnknownPhase;
  

  public DashMainController()
  {}
  
  public void initialise( Window rootWin )
  {
    rootWindow = rootWin;
    
    createWelcomeView();
    
    if ( intitialiseECCResources() )
    {
      liveMetricScheduler   = new LiveMetricScheduler();
      
      
      welcomeView.setReadyToStart( true );
    }
  }
  
  public void shutdown()
  {
    if ( !isShuttingDown )
    {
      isShuttingDown = true;
      dashMainLog.info( "Shutting down the ECC dashboard" );
    
      if ( liveMetricScheduler != null ) liveMetricScheduler.shutDown();
      if ( expMonitor != null )          expMonitor.shutDown();
      if ( mainDashView != null )        mainDashView.shutDownUI();
      
      mainDashView        = null;
      expMonitor          = null;
      liveMetricScheduler = null;
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
    }
  }
  
  @Override
  public void onClientDisconnected( EMClient client )
  {
    if ( client != null )
    {
      if ( connectionsView != null ) connectionsView.removeClient( client );
      if ( clientInfoView != null )  clientInfoView.writeClientDisconnected( client );
      if ( liveMonitorController != null ) liveMonitorController.removeClientLiveView( client );
    }
  }
  
  @Override
  public void onLifecyclePhaseStarted( EMPhase phase )
  {
    currentPhase = phase;
    
    mainDashView.setExperimentPhase( phase );
    connectionsView.updateClientsInPhase( phase );
    
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
    if ( waitingToStartNextPhase )
    {
      waitingToStartNextPhase = false;
      
      try 
      { expMonitor.goToNextPhase(); }
      catch ( Exception e )
      { dashMainLog.error("Could not go to next phase: " + e.getMessage()); }
    }
  }
  
  @Override
  public void onNoFurtherLifecyclePhases()
  {
    
  }
  
  @Override
  public void onLifecycleReset()
  {
    // Reset to start and wait for new connections
    waitingToStartNextPhase = false;
    
    mainDashView.resetViews();
    
    // Create a new experiment
    createExperiment();
  }
  
  @Override
  public void onFoundClientWithMetricGenerators( EMClient client )
  {
    if ( client != null )
    {
      Set<MetricGenerator> generators = client.getCopyOfMetricGenerators();
      Iterator<MetricGenerator> mgIt = generators.iterator();
      
      // Pass to EDM
      UUID expID = currentExperiment.getUUID();
      while ( mgIt.hasNext() )
      {
        MetricGenerator mg = mgIt.next();
        try 
        { 
          expMGAccessor.saveMetricGenerator( mg, expID );
          
          if ( clientInfoView.getCurrentClientID().equals( client.getID() ) )
            clientInfoView.writeClientInfo( client );
        }
        catch ( Exception e )
        {
          String problem = "Failed to store metric generator";
          mainDashView.addLogMessage( problem );
          dashMainLog.error( problem ); 
        }
      }
      
      mainDashView.addLogMessage( client.getName() + "Got metrics model from " + client.getName() );
    }
  }
  
  @Override
  public void onClientSetupResult( EMClient client, boolean success )
  {
    
  }
  
  @Override
  public void onClientDeclaredCanPush( EMClient client )
  {
    
  }
  
  @Override
  public void onClientDeclaredCanBePulled( EMClient client )
  {
    if ( client != null )
    {
      if ( currentPhase.equals( EMPhase.eEMLiveMonitoring) )
      {
        try { liveMetricScheduler.addClient(client); }
        catch ( Exception e )
        {
          String problem = "Could not add pulling client to live monitoring: " + e.getMessage();
          mainDashView.addLogMessage( problem );
          dashMainLog.error( problem );
        }
      }
      else
      {
        String problem = "Client trying to start pull process whilst not in Live monitoring";
        mainDashView.addLogMessage( problem );
        dashMainLog.error( problem );
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
    
  }
  
  @Override
  public void onDataBatchMeasurementSetCompleted( EMClient client, UUID measurementSetID )
  {
    
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
      
      mainDashView = new MainDashView();
      mainDashView.addListener( this );
      rootWindow.addComponent( (Component) mainDashView.getImplContainer() );
    
      expMonitor.openEntryPoint( emProps );
      createExperiment();
    
      mainDashView.initialise();
      
      monitorControlView = mainDashView.getMonitorControlView();
      monitorControlView.addListener( this );
      
      connectionsView = mainDashView.getConnectionsView();
      connectionsView.addListener( this );
      
      clientInfoView = mainDashView.getClientInfoView();
      clientInfoView.addListener( this );
      
      liveMonitorController = mainDashView.getLiveMonitorController();
      liveMonitorController.initialse( expReportAccessor );
      
      if ( emProps != null && currentExperiment != null )
        monitorControlView.setExperimentInfo( emProps.getProperty( "Monitor_ID" ),
                                              currentExperiment );
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
  { shutdown(); }
  
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
    if ( expMonitor != null && !waitingToStartNextPhase )
    {
      // Perform immediate actions before going on to winding down phase
      switch ( currentPhase )
      {
        case eEMLiveMonitoring: liveMetricScheduler.stop();
      }
      
      if ( expMonitor.isCurrentPhaseActive() )
      {
        try
        {
          waitingToStartNextPhase = true;
          expMonitor.stopCurrentPhase();
        }
        catch ( Exception e )
        { dashMainLog.error( "Could not stop phase: " + expMonitor.getCurrentPhase().name() +
                             " because " + e.getMessage()); }
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
  public void onClientSelected( UUID clientID )
  {
    if ( clientID != null )
    {
      EMClient client = expMonitor.getClientByID( clientID );
      if ( client != null )
      {
        clientInfoView.writeClientInfo( client );
      }
    }
  }
  
  // ClientInfoViewListener ----------------------------------------------------
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
          Map<UUID, MeasurementSet> setsToMonitor = 
                MetricHelper.getMeasurementSetsForAttribute( targetAttribute, clientMGs );
          
          if ( !setsToMonitor.isEmpty() )
          {
            liveMonitorController.addliveView( client, targetAttribute,
                                               setsToMonitor.values() );
            
            clientInfoView.displayMessage( "Added live view",
                                           "For " + client.getName() + " : " +
                                           targetAttribute.getName() );
          }
          else problem = "Could not put attribute in live view: no measurements are associated with it";
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
    // EDM
    edmProps = tryGetPropertiesFile( "edm" );
    if ( edmProps == null )
    {
      String problem = "Could not find EDM configuration!";
      dashMainLog.error( problem );
      welcomeView.addLogInfo( problem );
      return false;
    }
    welcomeView.addLogInfo( "Found EDM configuration" );
    
    try
    {
      expDataManager = EDMInterfaceFactory.getMonitoringEDM( edmProps );
      expMGAccessor  = expDataManager.getMetricGeneratorDAO();
      expReportAccessor  = expDataManager.getReportDAO();
      
      if ( expDataManager.isDatabaseSetUpAndAccessible() )
        welcomeView.addLogInfo( "Started EDM OK" );
      else
      {
        String problem = "EDM database has not been setup correctly ";
        dashMainLog.error( problem );
        welcomeView.addLogInfo( problem );
        return false;
      }
    }
    catch ( Exception e )
    {
      String problem = "Could not start EDM: " + e.getMessage();
      dashMainLog.error( problem );
      welcomeView.addLogInfo( problem );
      return false;
    }
    
    // EM
    emProps = tryGetPropertiesFile( "em" );
    if ( emProps == null )
    {
      String problem = "Could not find EM configuration!";
      dashMainLog.error( problem );
      welcomeView.addLogInfo( problem );
      return false;
    }
   
    expMonitor = EMInterfaceFactory.createEM();
    expMonitor.addLifecyleListener( this );

    welcomeView.addLogInfo( "Waiting to open client enty point." );
    return true;
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
}
