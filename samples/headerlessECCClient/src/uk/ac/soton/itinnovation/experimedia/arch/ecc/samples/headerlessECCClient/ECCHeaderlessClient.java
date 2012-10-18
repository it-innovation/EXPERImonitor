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
    
    private Experiment                     currentExperiment;
    private HashMap<UUID, MeasurementSet>  measurementSetMap;
    private HashMap<UUID, MeasurementTask> tasksByMeasurementSetMap;
    
    
    public ECCHeaderlessClient( String name )
    {
        clientName = name;
        
        measurementSetMap = new HashMap<UUID, MeasurementSet>();
    }
    
    public boolean tryConnectToAMQPBus( String rabbitServerIP, boolean useSSL ) throws Exception
    {
        // Safety first
        if ( rabbitServerIP == null ) throw new Exception( "IP parameter is invalid" );
        
        AMQPConnectionFactory amqpFactory = new AMQPConnectionFactory();
        amqpFactory.setAMQPHostIPAddress( rabbitServerIP );
        
        try
        {
            if ( useSSL )
              amqpFactory.connectToAMQPSSLHost();
            else
              amqpFactory.connectToAMQPHost();
            
            
            amqpChannel = amqpFactory.createNewChannel();
            clientLogger.info( "Connected to the AMQP (using non-secured channel)" );
        }
        catch ( Exception e )
        { clientLogger.error( "Headerless client problem: could not connect to ECC" ); throw e; }
        
        return true;
    }
        
    public boolean tryVerifiedConnectToAMQPBus( String      rabbitServerIP,
                                                InputStream certificateResource,
                                                String      certificatePassword
                                              ) throws      Exception
    {
        // Safety first
        if ( rabbitServerIP == null )      throw new Exception( "IP parameter is NULL" );
        if ( certificateResource == null ) throw new Exception( "Certificate resource is NULL" );
        if ( certificatePassword == null ) throw new Exception( "Certificate password is NULL" );
        
        AMQPConnectionFactory amqpFactory = new AMQPConnectionFactory();
        amqpFactory.setAMQPHostIPAddress( rabbitServerIP );
        
        try
        {
            amqpFactory.connectToVerifiedAMQPHost( certificateResource,
                                                   certificatePassword );

            amqpChannel = amqpFactory.createNewChannel();

            clientLogger.info( "Connected to the AMQP (using non-secured channel)" );
        }
        catch ( Exception e )
        { clientLogger.error( "Headerless client problem: could not connect to ECC" ); throw e; }
        
        return true;
    }
    
    public boolean initialiseLocalDataManagement()
    {
        edmAgentOK  = false;
        schedulerOK = false;
      
        // Initialise the 'mini' version of the EDM for local data management
        try
        {
            edmAgent = EDMInterfaceFactory.getMonitoringEDMAgent();
            edmAgentOK   = true; // Assume OK for now
        }
        catch ( Exception e )
        {
            clientLogger.error( "Could not create EDM agent" );
            return false;
        }
        
        // Initialise a scheduler to take/store measurements periodically
        measurementScheduler     = new MeasurementScheduler();
        tasksByMeasurementSetMap = new HashMap<UUID, MeasurementTask>(); 
      
        try
        { 
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
                                       clientID, monitorID );
        }
        catch ( Exception e )
        {
            clientLogger.error( "Could not attempt registration with Experiment monitor" );
            throw e;
        }
        
        return true;
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

    }
    
    @Override
    public void onSetupMetricGenerator( UUID genID, Boolean[] resultOUT )
    {
        // Assume that we haven't set up properly at first, then verify
        resultOUT[0] = false;
      
        if ( currentExperiment != null )
        {
            // Check we've got the metric generator and measurement tasks are ready
          Iterator<MetricGenerator> mGenIt = currentExperiment.getMetricGenerators().iterator();
          
        }
    }
    
    @Override
    public void onSetupTimeOut( UUID metricGeneratorID )
    {
      
    }

    @Override
    public void onStartPushingMetricData()
    {

    }

    @Override
    public void onPushReportReceived( UUID reportID )
    {

    }
    
    @Override
    public void onPullReportReceived( UUID reportID )
    {

    }
    
    @Override
    public void onPullMetricTimeOut( UUID measurementSetID )
    { 

    }

    @Override
    public void onStopPushingMetricData()
    {

    }

    @Override
    /*
    * Note that 'reportOut' is an OUT parameter provided by the adapter
    */
    public void onPullMetric( UUID measurementSetID, Report reportOut )
    {

    }

    @Override
    /*
    * Note that the summaryOUT parameter is an OUT parameter supplied by the
    * adapter
    */
    public void onPopulateSummaryReport( EMPostReportSummary summaryOUT )
    {

    }

    @Override
    public void onPopulateDataBatch( EMDataBatch batchOut )
    {

    }
    
    @Override
    public void onReportBatchTimeOut( UUID batchID )
    { 

    }

    @Override
    public void onGetTearDownResult( Boolean[] resultOUT )
    {

    }

    @Override
    public void onTearDownTimeOut()
    { 
      
    }
    
    // Private methods ---------------------------------------------------------
    private boolean tryDisconnecting()
    {
        boolean disconnectedOK = false;
      
        try 
        { 
            emiAdapter.deregisterWithEM();
            disconnectedOK = true;
        }
        catch ( Exception e )
        { clientLogger.error( "Could not de-register with the EM/ECC" ); }

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
        resourceGroup.setName( "Local data metrics" );
        resourceGroup.setDescription( "Group representing data related metrics" );
        
        MetricGroup walkGroup = new MetricGroup();
        mGen.addMetricGroup( walkGroup );
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
                                      100 ); // Measure every 100 ms
        
        attr = new Attribute();
        thisComputer.addAttribute( attr );
        attr.setName( "Walker A" );
        attr.setDescription( "Random walker starting at 90 degrees" );
        setupMeasurementForAttribute( attr,
                                      walkGroup, 
                                      MetricType.INTERVAL,
                                      new Unit("Degrees"),
                                      new PsuedoRandomWalkTool( 90 ),
                                      200 ); // Measure every 200 ms
        
        attr = new Attribute();
        thisComputer.addAttribute( attr );
        attr.setName( "Walker B" );
        attr.setDescription( "Random walker starting at 10 degrees" );
        setupMeasurementForAttribute( attr,
                                      walkGroup, 
                                      MetricType.INTERVAL,
                                      new Unit("Degrees"),
                                      new PsuedoRandomWalkTool( 10 ),
                                      400 ); // Measure every 400 ms
        
    }
    
    private Set<MeasurementSet> getMeasurementSets( MetricGenerator mGen )
    {
        HashSet<MeasurementSet> msetsTarget = new HashSet<MeasurementSet>();
        
        if ( mGen != null )
        {
            Iterator<MetricGroup> mgIt = mGen.getMetricGroups().iterator();
            while ( mgIt.hasNext() )
            {
                MetricGroup mg = mgIt.next();
                Iterator<MeasurementSet> msIt = mg.getMeasurementSets().iterator();
                
                while ( msIt.hasNext() ) { msetsTarget.add( msIt.next() ); }
            }
        }
        
        return msetsTarget;
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
        
        // Create an automatic measurement task to periodically take measurements
        try
        {
            // Must keep hold of task reference to ensure continued sampling
            MeasurementTask task = measurementScheduler.
                    createMeasurementTask( ms,           // MeasurementSet
                                           listener,     // Listener that will take measurement
                                           -1,           // Monitor indefinitely...
                                           intervalMS ); // ... each 'X' milliseconds
            
            tasksByMeasurementSetMap.put( ms.getUUID(), task );
        }
        catch (Exception e )
        { clientLogger.error( "Could not define measurement for attribute " + attr.getName() ); }
    }
    
    private void startMeasuring()
    {
        Iterator<MeasurementTask> taskIt =
                tasksByMeasurementSetMap.values().iterator();
        
        while ( taskIt.hasNext() )
        {
            taskIt.next().startMeasuring();
        }
    }
}
