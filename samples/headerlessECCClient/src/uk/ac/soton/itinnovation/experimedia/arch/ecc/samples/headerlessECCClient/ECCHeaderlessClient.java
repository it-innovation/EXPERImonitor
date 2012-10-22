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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.headerlessECCClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.headerlessECCClient.tools.*;

import java.io.InputStream;
import java.util.*;
import org.apache.log4j.Logger;





public class ECCHeaderlessClient implements EMIAdapterListener
{
    private final Logger clientLogger = Logger.getLogger( ECCHeaderlessClient.class );
    private final String clientName;
    
    private AMQPBasicChannel     amqpChannel;
    private EMInterfaceAdapter   emiAdapter;
    private IMonitoringEDMAgent  edmAgent;
    private MeasurementScheduler measurementScheduler;
    private boolean              edmAgentOK  = false;
    private boolean              schedulerOK = false;
    
    private Experiment                      currentExperiment;
    private HashMap<UUID, MeasurementSet>   measurementSetMap;
    private HashSet<MeasurementTask>        scheduledMeasurementTasks;
    private HashMap<UUID, ITakeMeasurement> measurementSetInstantSamplers;
    
    
    public ECCHeaderlessClient( String name )
    {
        clientName = name;
        
        // Easy-to-find measurement sets based on their IDs
        measurementSetMap = new HashMap<UUID, MeasurementSet>();
        
        // Set of measurement tasks created for attributes ( see setupMeasurementForAttribute(..) ) 
        scheduledMeasurementTasks = new HashSet<MeasurementTask>();
        
        // For 'on-the-fly' measurements only - used when there is no persistence/scheduling support
        measurementSetInstantSamplers = new HashMap<UUID, ITakeMeasurement>(); 
    }
    
    public boolean tryConnectToAMQPBus( String rabbitServerIP, 
                                        int portNumber,
                                        boolean useSSL ) throws Exception
    {
        // Safety first
        if ( rabbitServerIP == null ) throw new Exception( "IP parameter is invalid" );
        if ( portNumber < 1 ) throw new Exception( "Port number is invalid" );
        
        AMQPConnectionFactory amqpFactory = new AMQPConnectionFactory();
        amqpFactory.setAMQPHostIPAddress( rabbitServerIP );
        amqpFactory.setAMQPHostPort( portNumber );
        
        try
        {
            if ( useSSL )
              amqpFactory.connectToAMQPSSLHost();
            else
              amqpFactory.connectToAMQPHost();
            
            amqpChannel = amqpFactory.createNewChannel();
            
            clientLogger.info( "Connected to the AMQP (using " +
                                (useSSL ? "SSL" : "non-secured") + " channel)" );
        }
        catch ( Exception e )
        { 
            clientLogger.error( "Headerless client problem: could not connect to" +
                                (useSSL ? "SSL" : "non-secured") + " AMQP channel" ); 
            throw e; 
        }
        
        return true;
    }
        
    public boolean tryVerifiedConnectToAMQPBus( String      rabbitServerIP,
                                                int         portNumber,
                                                InputStream keystoreStream,
                                                String      keystorePassword
                                              ) throws Exception
    {
        // Safety first
        if ( rabbitServerIP == null )   throw new Exception( "IP parameter is NULL" );
        if ( portNumber < 1 )           throw new Exception( "Port number is invalid" );
        if ( keystoreStream == null )   throw new Exception( "Certificate resource is NULL" );
        if ( keystorePassword == null ) throw new Exception( "Certificate password is NULL" );
        
        AMQPConnectionFactory amqpFactory = new AMQPConnectionFactory();
        amqpFactory.setAMQPHostIPAddress( rabbitServerIP );
        amqpFactory.setAMQPHostPort( portNumber );
        
        try
        {
            amqpFactory.connectToVerifiedAMQPHost( keystoreStream,
                                                   keystorePassword );

            amqpChannel = amqpFactory.createNewChannel();

            clientLogger.info( "Connected to the AMQP (using a verified SSL channel)" );
        }
        catch ( Exception e )
        { 
            clientLogger.error( "Headerless client problem: could not connect to SSL verified AMQP" ); 
            throw e;
        }
        
        return true;
    }
    
    public boolean initialiseLocalDataManagement( Properties edmProps )
    {
        edmAgentOK  = false;
        schedulerOK = false;
        
        //TODO: Something with these EDM properties?
      
        // Initialise the 'mini' version of the EDM for local data management
        try
        {
            edmAgent   = EDMInterfaceFactory.getMonitoringEDMAgent();
            edmAgentOK = edmAgent.isDatabaseSetUpAndAccessible();
            if ( !edmAgentOK ) throw new Exception( "EDM Agent is not configured correctly" );
            
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
    
    public void disconnectFromECCMonitor()
    {
        tryDisconnecting();
    }
    
    // EMIAdapterListener ------------------------------------------------------
    @Override
    public void onEMConnectionResult( boolean connected, Experiment expInfo )
    {
        boolean connectionAndExperimentOK = false;
      
        if ( connected )
        {
            if ( expInfo != null )
            {
              currentExperiment = new Experiment();
              currentExperiment.setName( expInfo.getName() );
              currentExperiment.setDescription( expInfo.getDescription() );
              currentExperiment.setStartTime( expInfo.getStartTime() );
              
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
        clientLogger.info( "Got disconnected from EM: " + reason );
        tryDisconnecting();
    }
    
    @Override
    public void onDescribeSupportPhases( EnumSet<EMPhase> phasesOUT )
    {
        // We're going to skip the Set-up and Tear-down phases...
        // ... we MUST support the discovery phase by default, but don't need to include
        phasesOUT.add( EMPhase.eEMLiveMonitoring );
        phasesOUT.add( EMPhase.eEMPostMonitoringReport );
    }
    
    @Override
    public void onDescribePushPullBehaviours( Boolean[] pushPullOUT )
    {
        // We're just going to support pulling for this client
        pushPullOUT[0] = false; // No pushing
        pushPullOUT[1] = true;
    }

    @Override
    public void onPopulateMetricGeneratorInfo()
    {
        // Time to start defining what metrics we can produce for this experiment
        if ( currentExperiment != null )
        {
            // Define all metric generators for this experiment
            defineExperimentMetrics( currentExperiment );
            
            // If we have an EDMAgent available, save our experiment & metric generators
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
    { 
        //TO DO
    }
    
    @Override
    public void onSetupMetricGenerator( UUID genID, Boolean[] resultOUT )
    { /* This demo has opted out of this phase */ }
    
    @Override
    public void onSetupTimeOut( UUID metricGeneratorID )
    { /* This demo has opted out of this pase */ }

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
        clientLogger.info( "Got pull for: " + measurementSetID.toString() );
      
        // If we have an EDMAgent running, then get the latest measurement from there
        if ( edmAgentOK )
        {        
            //TODO: Use EDMAgent to get the latest data for this MS
            
        }
        else  // Otherwise, immediately generate the metric 'on-the-fly'
        {      
            ITakeMeasurement sampler = measurementSetInstantSamplers.get( measurementSetID );
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
    public void onPullReportReceived( UUID reportID )
    {
        //TODO
    }
    
    @Override
    public void onPullMetricTimeOut( UUID measurementSetID )
    { 
        //TODO
    }

    @Override
    /*
    * Note that the summaryOUT parameter is an OUT parameter supplied by the
    * adapter
    */
    public void onPopulateSummaryReport( EMPostReportSummary summaryOUT )
    {
        //TODO
    }

    @Override
    public void onPopulateDataBatch( EMDataBatch batchOut )
    {
        //TODO
    }
    
    @Override
    public void onReportBatchTimeOut( UUID batchID )
    { 
        //TODO
    }

    @Override
    public void onGetTearDownResult( Boolean[] resultOUT )
    { /* This demo has opted out of this phase */ }

    @Override
    public void onTearDownTimeOut()
    { /* This demo has opted out of this phase */ }
    
    // Private methods ---------------------------------------------------------
    private boolean tryDisconnecting()
    {
        boolean disconnectedOK = false;
      
        try 
        { 
            emiAdapter.disconnectFromEM();
            disconnectedOK = true;
        }
        catch ( Exception e )
        { clientLogger.error( "Could not de-register with the EM/ECC" + e.getMessage() ); }

        return disconnectedOK;
    }
    
    private void defineExperimentMetrics( Experiment experiment )
    {
        measurementSetMap.clear(); // This map will be useful later
      
        // Set up top-level groups ---------------------------------------------
        MetricGenerator mGen = new MetricGenerator();
        experiment.addMetricGenerator( mGen );
        mGen.setName( "Headerless Metric Generator" );
        mGen.setDescription( new Date().toString() );
        
        MetricGroup resourceGroup = new MetricGroup();
        mGen.addMetricGroup( resourceGroup );
        resourceGroup.setMetricGeneratorUUID( resourceGroup.getUUID() );
        resourceGroup.setName( "Local data metrics" );
        resourceGroup.setDescription( "Group representing data related metrics" );
        
        MetricGroup walkGroup = new MetricGroup();
        mGen.addMetricGroup( walkGroup );
        walkGroup.setMetricGeneratorUUID( walkGroup.getUUID() );
        walkGroup.setName( "Random walkers group" );
        walkGroup.setDescription( "Containers pseudo random number based walkers" );

        // Define what you are going to measure and how ------------------------
        Entity thisComputer = new Entity();
        mGen.addEntity( thisComputer );
        thisComputer.setName( "This computer" );
        thisComputer.setDescription( System.getProperty("user.name") );
        
        Attribute attr = new Attribute();
        thisComputer.addAttribute( attr );
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
            measurementSetInstantSamplers.put( ms.getUUID(), listener );
    }
    
    private void startMeasuring()
    {
        Iterator<MeasurementTask> taskIt = scheduledMeasurementTasks.iterator();
        while ( taskIt.hasNext() )
        {
            taskIt.next().startMeasuring();
        }
    }
}
