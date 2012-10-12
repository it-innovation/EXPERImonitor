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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDMAgent;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.headerlessECCClient.tools.*;

import java.io.InputStream;
import java.util.*;
import org.apache.log4j.Logger;





public class ECCHeaderlessClient
{
    private final Logger clientLogger = Logger.getLogger( ECCHeaderlessClient.class );
    
    private AMQPBasicChannel    amqpChannel;
    
    private IMonitoringEDMAgent  expDataAgent;
    private MeasurementScheduler measurementScheduler;
    
    private Experiment           currentExperiment;
    private Set<MeasurementTask> measurementTasks;
    
    
    public ECCHeaderlessClient()
    {}
        
    public void tryConnectToECC( String      rabbitServerIP,
                                 InputStream certificateResource,
                                 String      certificatePassword,
                                 UUID        monitorID,
                                 UUID        clientID ) throws Exception
    {
        // Safety first
        if ( rabbitServerIP == null ) throw new Exception( "IP parameter is invalid" );
        if ( monitorID == null || clientID == null ) throw new Exception( "ID parameter is null" );
        
        AMQPConnectionFactory amqpFactory = new AMQPConnectionFactory();
        amqpFactory.setAMQPHostIPAddress( rabbitServerIP );
        
        try
        {
            if ( certificateResource != null )
            {
                amqpFactory.connectToSecureAMQPHost( certificateResource,
                                                     certificatePassword );
                
                amqpChannel = amqpFactory.createNewChannel();
                
                clientLogger.info( "Connected to the ECC (using non-secured channel)" );
            }
            else
            {
                amqpFactory.connectToAMQPHost();
                amqpChannel = amqpFactory.createNewChannel();
                
                clientLogger.info( "Connected to the ECC (using non-secured channel)" );
            }
        }
        catch ( Exception e )
        { clientLogger.error( "Headerless client problem: could not connect to ECC" ); throw e; }
    }
    
    public void initialiseDataManagement() throws Exception
    {
        // Initialise the 'mini' version of the EDM for local data management
        expDataAgent       = EDMInterfaceFactory.getMonitoringEDMAgent();
        measurementScheduler = new MeasurementScheduler();
        measurementTasks     = new HashSet<MeasurementTask>();
        
        try
        { measurementScheduler.initialise( expDataAgent ); }
        catch ( Exception e )
        {
            clientLogger.error( "Could not initialise measurement scheduler" );
            throw e;
        }
    }
    
    public void beginMonitoring() throws Exception
    {
        // Testing for now: try out a series of measurement tasks
        // Just create a dummy experiment for now
        currentExperiment = new Experiment();
        currentExperiment.setName( "Unconnected experiment" );
        currentExperiment.setDescription( "This experiment is only for stand-alone testing purposes" );
        
        defineExperimentMetrics( currentExperiment );
        
        startMeasuring();
    }
    
    // Private methods ---------------------------------------------------------
    private void defineExperimentMetrics( Experiment experiment )
    {
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
        
        // Create an automatic measurement task to periodically take measurements
        try
        {
            // Must keep hold of task reference to ensure continued sampling
            MeasurementTask task = measurementScheduler.
                                  createMeasurementTask( ms,           // MeasurementSet
                                                         listener,     // Listener that will take measurement
                                                         -1,           // Monitor indefinitely...
                                                         intervalMS ); // ... each 'X' milliseconds
            
            measurementTasks.add( task );
        }
        catch (Exception e )
        { clientLogger.error( "Could not define measurement for attribute " + attr.getName() ); }
    }
    
    private void startMeasuring()
    {
        Iterator<MeasurementTask> taskIt = measurementTasks.iterator();
        while ( taskIt.hasNext() )
        {
            taskIt.next().startMeasuring();
        }
    }
}
