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
//      Created Date :          04-Oct-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.headlessECCClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.headlessECCClient.tools.PsuedoRandomWalkTool;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.headlessECCClient.tools.MemoryUsageTool;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.*;

import java.util.*;
import org.apache.log4j.Logger;




public class ECCHeadlessClient implements EMIAdapterListener
{
    private final Logger clientLogger = Logger.getLogger( ECCHeadlessClient.class );
    private final String clientName;
    
    // Connection to the ECC
    private AMQPBasicChannel     amqpChannel;
    private EMInterfaceAdapter   emiAdapter;
    
    // Local metric data storage and scheduling
    private IMonitoringEDMAgent  edmAgent;
    private IReportDAO           edmReportDAO;
    private MeasurementScheduler measurementScheduler;
   
    // Experiment and measurement information
    private Experiment                      currentExperiment;
    private HashMap<UUID, MeasurementSet>   measurementSetMap;
    private HashSet<MeasurementTask>        scheduledMeasurementTasks;
    private HashMap<UUID, ITakeMeasurement> instantMeasurers;
    
    // Client state
    private boolean edmAgentOK  = false;
    private boolean schedulerOK = false;
    private boolean tryingToDisconnect = false;
    
    
    public ECCHeadlessClient( String name )
    {
        clientName = name;
        
        // Easy-to-find measurement sets based on their IDs
        measurementSetMap = new HashMap<UUID, MeasurementSet>();
        
        // Set of measurement tasks created for attributes ( see setupMeasurementForAttribute(..) ) 
        scheduledMeasurementTasks = new HashSet<MeasurementTask>();
        
        // For 'on-the-fly' measurements only - used when there is no persistence/scheduling support
        instantMeasurers = new HashMap<UUID, ITakeMeasurement>();
        
        // Add a shut-down hook for clean terminations
        Runtime.getRuntime().addShutdownHook( new ShutdownHook() );
    }
    
    /**
     * This method tries to connect to the ECC using the configuration specified in a properties data set.
     * Note: if this was successful, you should then try to register with the ECC (see tryRegisteringWithECCMonitor(..) )
     * 
     * @param emProps     - Properties file specifying connection to the ECC.
     * @return            - Returns true if a connection to the ECC was made
     * @throws Exception  - throws if the properties file was erroneous or a connection could not be made
     */
    public boolean tryConnectToAMQPBus( Properties emProps ) throws Exception
    { 
        boolean connectedOK = false;
        
        AMQPConnectionFactory connectFactory = new AMQPConnectionFactory();
        
        try
        { 
          connectFactory.connectToAMQPHost( emProps );
          amqpChannel = connectFactory.createNewChannel();
          connectedOK = true;
        }
        catch (Exception e)
        { clientLogger.error( "Could not connect to EM: " + e.getMessage()); }
        
        return connectedOK;
    }
    
    /**
     * This method attempts to create an EDMAgent and initialise a local PostgreSQL
     * data in which metric data will be stored.
     * 
     * @param edmProps  - Properties file specifying the database resource for the EDMAgent
     * @return          - Returns true if the EDMAgent was correctly set-up
     */
    public boolean initialiseLocalDataManagement( Properties edmProps )
    {
        edmAgentOK  = false;
        schedulerOK = false;
      
        // Initialise the 'mini' version of the EDM for local data management
        try
        {
            edmAgent = EDMInterfaceFactory.getMonitoringEDMAgent( edmProps );
            
            edmAgentOK = edmAgent.isDatabaseSetUpAndAccessible();
            if ( !edmAgentOK ) throw new Exception( "EDM Agent is not configured correctly" );
            
            edmReportDAO = edmAgent.getReportDAO();
        }
        catch ( Exception e )
        {
            clientLogger.error( "Could not create EDM agent" + e.getMessage() );
            return false;
        }
        
        // Initialise a scheduler to take/store measurements periodically
        try
        {
            measurementScheduler = new MeasurementScheduler();
            measurementScheduler.initialise( edmAgent );
            schedulerOK = true;
        }
        catch ( Exception e )
        {
            clientLogger.error( "Could not initialise measurement scheduler" );
            return false;
        }
        
        return true;
    }
    
    /**
     * Attempts to register with the ECC, after a connect to the ECC has been established (see tryConnectToAMQPBus(..) ).
     * 
     * @param monitorID  - The UUID of the ECC monitoring system to connect to.
     * @param clientID   - The UUID that uniquely represents this client instance.
     * @return           - Returns true if a registration request was successfully sent.
     * @throws Exception - Throws if a connection to the ECC is not already established or the ID parameters are NULL.
     */
    public boolean tryRegisteringWithECCMonitor( UUID monitorID,
                                                 UUID clientID ) throws Exception
    {
        // Safety first
        if ( monitorID == null || clientID == null ) throw new Exception( "ID parameter is null" );
        if ( amqpChannel == null ) throw new Exception( "No connection with AMQP bus" );
      
        // Create Experiment Monitor interface adapter to help listen to the ECC/EM
        emiAdapter = new EMInterfaceAdapter( this );
        
        // And try registering
        try
        {
            emiAdapter.registerWithEM( clientName, amqpChannel, 
                                       monitorID, clientID );
        }
        catch ( Exception e )
        {
            clientLogger.error( "Could not attempt registration with Experiment monitor" );
            throw e;
        }
        
        return true;
    }
    
    /**
     * Disconnects from the ECC.
     */
    public void disconnectFromECCMonitor()
    {
        tryDisconnecting();
    }
    
    // EMIAdapterListener ------------------------------------------------------
    @Override
    public void onEMConnectionResult( boolean connected, Experiment expInfo )
    {
        boolean connectionAndExperimentOK = false;
      
        // If we're connected, then store some experiment data from the ECC
        if ( connected )
        {
            if ( expInfo != null )
            {
              currentExperiment = new Experiment();
              currentExperiment.setName( expInfo.getName() );
              currentExperiment.setDescription( expInfo.getDescription() );
              currentExperiment.setStartTime( expInfo.getStartTime() );
              
              // We will save this experiment data in the EDM Agent (if available)
              // during the discovery phase
              
              connectionAndExperimentOK = true;
            }
            else clientLogger.error( "Experiment information is null" );
        }
        else clientLogger.error( "Connection refused by ECC" );
        
        // If we didn't get past this stage, there's no point in continuing
        if ( !connectionAndExperimentOK ) tryDisconnecting();
    }
    
    @Override
    public void onEMDeregistration( String reason )
    {
        // We've been de-registered by the ECC. Display the reason and then disconnect.
        clientLogger.info( "Got disconnected from EM: " + reason );
        tryDisconnecting();
    }
    
    @Override
    public void onDescribeSupportedPhases( EnumSet<EMPhase> phasesOUT )
    {
        // We're going to skip the Set-up and Tear-down phases...
        // ... we MUST support the discovery phase by default.
        phasesOUT.add( EMPhase.eEMLiveMonitoring );
        phasesOUT.add( EMPhase.eEMPostMonitoringReport );
    }
    
    @Override
    public void onDescribePushPullBehaviours( Boolean[] pushPullOUT )
    {
        // We're just going to support pulling for this client
        pushPullOUT[0] = false; // No pushing
        pushPullOUT[1] = true;  // Will support pulling
    }

    @Override
    public void onPopulateMetricGeneratorInfo()
    {
        // Time to start defining what metrics we can produce for this experiment
        if ( currentExperiment != null )
        {
            // Define all metric generators for this experiment
            defineExperimentMetrics( currentExperiment );
            
            // If we have an EDMAgent available, save our experiment & metric generators locally
            if ( edmAgentOK )
            {
                try
                {
                  IExperimentDAO dao = edmAgent.getExperimentDAO();
                  dao.saveExperiment( currentExperiment );
                }
                catch ( Exception e )
                {
                  clientLogger.error( "Had problems saving data with the ExperimentDAO: " + e.getMessage() );
                  edmAgentOK  = false;
                  schedulerOK = false;
                }
            }
            
            // Even if the EDMAgent isn't available, we can still send data, so
            // notify the adapter of our metrics
            emiAdapter.setMetricGenerators( currentExperiment.getMetricGenerators() );
        
        }
        else  // Things are bad if we can't describe our metric generators - so disconnect
        {
          tryDisconnecting();
          clientLogger.error( "Trying to populate metric generator info - but current experiment is null. Disconnecting" );
        }
    }

    @Override
    public void onDiscoveryTimeOut()
    { /* Not implemented in this demo */ }
    
    @Override
    public void onSetupMetricGenerator( UUID genID, Boolean[] resultOUT )
    { /* This demo has opted out of this phase */ }
    
    @Override
    public void onSetupTimeOut( UUID metricGeneratorID )
    { /* This demo has opted out of this pase */ }

    @Override
    public void onLiveMonitoringStarted()
    {
        // If we have an EDM Agent and scheduling, then start taking periodic measurements
        if ( edmAgentOK && schedulerOK )
            startMeasuring();
    }
    
    @Override
    public void onStartPushingMetricData()
    { /* Pushing not implemented in this demo */ }

    @Override
    public void onPushReportReceived( UUID reportID )
    { /* Pushing not implemented in this demo */ }
    
    @Override
    public void onStopPushingMetricData()
    { /* Pushing not implemented in this demo */ }
    
    @Override
    /*
     * Note that 'reportOut' is an OUT parameter provided by the adapter
     */
    public void onPullMetric( UUID measurementSetID, Report reportOUT )
    {      
        // If we have an EDMAgent running, then get the latest measurement from there
        if ( edmAgentOK )
        {
            try
            { 
                Report edmReport = edmReportDAO.getReportForLatestMeasurement( measurementSetID, true );
                
                // Save this report internally (without data - we already have it stored in the EDMAgent)
                // we'll retrieve this report after acknowledgement of the report by the ECC later
                edmReportDAO.saveReport( edmReport, false );
                
                // And copy it out to be sent to the ECC
                reportOUT.copyReport( edmReport, true );
            }
            catch ( Exception e )
            { clientLogger.equals( "Could not pull metric " + measurementSetID.toString() +
                                   "from Agent EDM" ); }
            
        }
        else  // Otherwise, immediately generate the metric 'on-the-fly'
        {      
            ITakeMeasurement sampler = instantMeasurers.get( measurementSetID );
            MeasurementSet   mSet    = measurementSetMap.get( measurementSetID );
          
            if ( sampler != null && mSet != null )
            {
                // Make an empty measurement set for this data first
                MeasurementSet emptySet = new MeasurementSet( mSet, false );
                reportOUT.setMeasurementSet( emptySet );
              
                sampler.takeMeasure( reportOUT );
            }
            else
                clientLogger.error( "Could not find measurement sampler for " + 
                                    measurementSetID.toString() );
        }
    }
    
    @Override
    public void onPullingStopped()
    {
        // No need to carry on taking measurements, so stop all scheduled activity
        stopMeasuring();
    }
    
    @Override
    public void onPullReportReceived( UUID reportID )
    {      
        // If we had an EDM running, we can mark which reports have been successfully
        // received by the ECC
        if ( edmAgentOK )
        {
            try
            {
              // If we have previously stored this report, mark the measurements
              // as synchronized with the ECC
              Report ackRep = edmReportDAO.getReport( reportID, false );
              
              if ( ackRep != null )
              {
                  edmReportDAO.setReportMeasurementsSyncFlag( reportID, true );
                  edmReportDAO.deleteReport(reportID, false); // Delete the report (but keep the measurements)
              }
              else
                clientLogger.info( "Could not find report to sync!" );
            }
            catch ( Exception e )
            { clientLogger.warn( "Could not mark report " + reportID.toString() +
                                 " as received by the ECC"); }
        }
    }
    
    @Override
    public void onPullMetricTimeOut( UUID measurementSetID )
    { /* Not implemented for this demo */ }

    @Override
    /*
    * Note that the summaryOUT parameter is an OUT parameter supplied by the
    * adapter
    */
    public void onPopulateSummaryReport( EMPostReportSummary summaryOUT )
    {
        // If we have an EDMAgent running, this will be easy
        if ( edmAgentOK )
        {
            // Go through all our known MeasurementSets and get a summary of the data
            Iterator<UUID> msIDIt = measurementSetMap.keySet().iterator();
            while ( msIDIt.hasNext() )
            {
                UUID msID = msIDIt.next();
                
                // Ask the EDM for the summary report (but without actual measurements)
                try
                {
                    // Don't actually retrieve all the measurement data - it's just a summary
                    Report report = edmReportDAO.getReportForAllMeasurements( msID, false ); 
                    summaryOUT.addReport( report );
                }
                catch ( Exception e )
                { clientLogger.error( "Could not get summary report for MeasurementSet " + msID.toString()); }
            }
        }
        else
        {
            // We don't have any EDMAgent persistence, so we'll just have to send basic
            // summary statistics
            Iterator<UUID> msIDIt = instantMeasurers.keySet().iterator();
            while ( msIDIt.hasNext() )
            {
                // Get measurement and measurement set for sampler
                UUID msID                = msIDIt.next();
                ITakeMeasurement sampler = instantMeasurers.get( msID );
                MeasurementSet mset      = measurementSetMap.get( msID );
                
                if ( sampler != null && mset != null )
                {
                    // Create a report for this measurement set + summary stats 
                    Report report = new Report();
                    report.setMeasurementSet( mset );
                    report.setFromDate( sampler.getFirstMeasurementDate() );
                    report.setToDate( sampler.getLastMeasurementDate() );
                    report.setNumberOfMeasurements( sampler.getMeasurementCount() );
                    
                    summaryOUT.addReport( report );
                }
            }
        }
    }

    @Override
    public void onPopulateDataBatch( EMDataBatch batchOUT )
    {
        // If we have been storing metrics using the EDM & Scheduler, get some
        // previously unsent data
        UUID msID = batchOUT.getExpectedMeasurementSetID();
      
        if ( edmAgentOK && schedulerOK )
            try
            {
                // Try get some data based on the batch requested
                Report edmBatch = edmReportDAO.getReportForUnsyncedMeasurementsFromDate( msID, 
                                                                                         batchOUT.getCopyOfExpectedDataStart(),
                                                                                         batchOUT.getExpectedMeasurementCount(),
                                                                                         true );
                batchOUT.setBatchReport( edmBatch );
            }
            catch( Exception e )
            { clientLogger.error( "Could not get batch report for MeasurementSet " + msID.toString()); }
            
        
        // No EDM means we don't have any persistence, so do nothing (effectively
        // returning an empty data batch)
    }
    
    @Override
    public void onReportBatchTimeOut( UUID batchID )
    { /* Not implemented in this demo */ }

    @Override
    public void onGetTearDownResult( Boolean[] resultOUT )
    { /* This demo has opted out of this phase */ }

    @Override
    public void onTearDownTimeOut()
    { /* This demo has opted out of this phase */ }
    
    // Private methods ---------------------------------------------------------
    private synchronized boolean tryDisconnecting()
    {
        boolean disconnectedOK = false;
        
        // Don't repeatedly try to disconnect
        if ( !tryingToDisconnect )
          try 
          { 
              tryingToDisconnect = true;
              emiAdapter.disconnectFromEM();
              disconnectedOK = true;
          }
          catch ( Exception e )
          { clientLogger.error( "Could not de-register with the EM/ECC" + e.getMessage() ); }

        return disconnectedOK;
    }
    
    private void defineExperimentMetrics( Experiment experiment )
    {
        measurementSetMap.clear(); // This map will be useful later for reporting measurement summaries
      
        // Set up top-level groups ---------------------------------------------
        MetricGenerator mGen = new MetricGenerator();
        experiment.addMetricGenerator( mGen );
        mGen.setName( "Headless Metric Generator" );
        mGen.setDescription( new Date().toString() );
        
        MetricGroup resourceGroup = new MetricGroup();
        mGen.addMetricGroup( resourceGroup );
        resourceGroup.setMetricGeneratorUUID( mGen.getUUID() );
        resourceGroup.setName( "Local data metrics" );
        resourceGroup.setDescription( "Group representing data related metrics" );
        
        MetricGroup walkGroup = new MetricGroup();
        mGen.addMetricGroup( walkGroup );
        walkGroup.setMetricGeneratorUUID( mGen.getUUID() );
        walkGroup.setName( "Random walkers group" );
        walkGroup.setDescription( "Containers pseudo random number based walkers" );

        // Define what you are going to measure and how ------------------------
        Entity thisComputer = new Entity();
        mGen.addEntity( thisComputer );
        thisComputer.setName( "This computer" );
        thisComputer.setDescription( System.getProperty("user.name") );
        
        Attribute attr = new Attribute();
        thisComputer.addAttribute( attr );
        attr.setEntityUUID( thisComputer.getUUID() );
        attr.setName( "Available memory" );
        attr.setDescription( "Memory used by client JVM" );
        setupMeasurementForAttribute( attr,
                                      resourceGroup, 
                                      MetricType.RATIO,
                                      new Unit("Kilobytes"),
                                      new MemoryUsageTool(),
                                      1000 ); // Measure every second
        
        attr = new Attribute();
        thisComputer.addAttribute( attr );
        attr.setEntityUUID( thisComputer.getUUID() );
        attr.setName( "Walker A" );
        attr.setDescription( "Random walker starting at 90 degrees" );
        setupMeasurementForAttribute( attr,
                                      walkGroup, 
                                      MetricType.INTERVAL,
                                      new Unit("Degrees"),
                                      new PsuedoRandomWalkTool( 90 ),
                                      2000 ); // Measure every 2 seconds
        
        attr = new Attribute();
        thisComputer.addAttribute( attr );
        attr.setEntityUUID( thisComputer.getUUID() );
        attr.setName( "Walker B" );
        attr.setDescription( "Random walker starting at 10 degrees" );
        setupMeasurementForAttribute( attr,
                                      walkGroup, 
                                      MetricType.INTERVAL,
                                      new Unit("Degrees"),
                                      new PsuedoRandomWalkTool( 10 ),
                                      4000 ); // Measure every 4 seconds
        
    }
    
    private void setupMeasurementForAttribute( Attribute        attr,
                                               MetricGroup      parentGroup,
                                               MetricType       type,
                                               Unit             unit,
                                               ITakeMeasurement listener,
                                               long             intervalMS )
    {
        // Define the measurement set
        MeasurementSet ms = new MeasurementSet();
        parentGroup.addMeasurementSets( ms );
        ms.setMetricGroupUUID( parentGroup.getUUID() );
        ms.setAttributeUUID( attr.getUUID() );
        ms.setMetric( new Metric(UUID.randomUUID(), type, unit) );
        
        // Map this measurement set for later
        measurementSetMap.put( ms.getUUID(), ms );
        
        // If available, create an automatic measurement task to periodically take measurements
        if ( edmAgentOK && schedulerOK )
            try
            {
                // Must keep hold of task reference to ensure continued sampling
                MeasurementTask task = measurementScheduler.
                        createMeasurementTask( ms,           // MeasurementSet
                                               listener,     // Listener that will take measurement
                                               -1,           // Monitor indefinitely...
                                               intervalMS ); // ... each 'X' milliseconds
                
                scheduledMeasurementTasks.add( task );
            }
            catch (Exception e )
            { clientLogger.error( "Could not define measurement task for attribute " + attr.getName() ); }
        else
            // If we can't schedule & store measurements, just have the samplers handy
            instantMeasurers.put( ms.getUUID(), listener );
    }
    
    private void startMeasuring()
    {
        // Run through all our measurement tasks and start them up
        Iterator<MeasurementTask> taskIt = scheduledMeasurementTasks.iterator();
        while ( taskIt.hasNext() )
        {
            taskIt.next().startMeasuring();
        }
    }
    
    private void stopMeasuring()
    {
        // Run through all our measurement tasks and stop them
        Iterator<MeasurementTask> taskIt = scheduledMeasurementTasks.iterator();
        while ( taskIt.hasNext() )
        {
            taskIt.next().stopMeasuring();
        }
    }
    
    // Private shut-down hook --------------------------------------------------
    // This class is used for emergency shut-downs
    private class ShutdownHook extends Thread
    {
        @Override
        public void run()
        {
              clientLogger.info( "Got a terminate signal, so closing down.." );
              stopMeasuring();
              tryDisconnecting();  
        }
    }
}
