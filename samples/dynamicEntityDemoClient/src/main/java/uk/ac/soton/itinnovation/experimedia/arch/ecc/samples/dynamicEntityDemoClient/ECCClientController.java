/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
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
//      Created By :            Dion Kitchener  
//      Created Date :          04-July-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.dynamicEntityDemoClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;

import java.util.*;
import javax.swing.JOptionPane;




public class ECCClientController implements EMIAdapterListener,
                                            ECCClientViewListener,
                                            ECCNewEntityViewListener
{
	private final Logger clientLogger = LoggerFactory.getLogger(getClass());

    private AMQPBasicChannel       amqpChannel;
    private EMInterfaceAdapter     emiAdapter;
    private ECCClientView          clientView;
    private String                 clientName;
    private MetricGenerator        metricGenerator;
    private MetricGroup            metricGroup;
    private HashMap<UUID, Boolean> enabledEntityMap; // Entity ID map to enabled/disabled flag
    private HashMap<UUID,UUID>     msEntitymap;      // MeasurementSet ID to Entity ID map (links a MeasurementSet to an Entity by ID)
    

    public ECCClientController()
    {
        // Hash map to store entity UUID and enable status
        enabledEntityMap = new HashMap<UUID, Boolean>();
        
        // Hash map to store measurement set UUID and entity UUID
        msEntitymap = new HashMap<UUID, UUID>();
        
        // Only one metric generator and metric group
        metricGenerator = new MetricGenerator();
        metricGenerator.setName( "Demo metric generator" );
        
        // Only one metric group
        metricGroup = MetricHelper.createMetricGroup( "Example group", "Group to add new metrics in", metricGenerator );    
    }

    public void start( String rabbitServerIP,
                       UUID expMonitorID,
                       UUID clientID ) throws Exception
    {
        if ( rabbitServerIP != null &&
             expMonitorID   != null &&
             clientID       != null )
        {
            clientLogger.info( "Trying to connect to Rabbit server on " + rabbitServerIP );

            // Create connection to Rabbit server ------------------------------------
            AMQPConnectionFactory amqpFactory = new AMQPConnectionFactory();
            amqpFactory.setAMQPHostIPAddress( rabbitServerIP );
            try
            {
                amqpFactory.connectToAMQPHost();
                amqpChannel = amqpFactory.createNewChannel();
            }
            catch ( Exception e ) 
            {
                clientLogger.error( "Could not connect to Rabbit server" );
                throw e; 
            }

            // Set up a simple view --------------------------------------------------
            Date date = new Date();
            clientName = date.toString();
            clientView = new ECCClientView( clientName, this, this );
            clientView.setVisible( true );

            // Create EM interface adapter, listen to it...
            emiAdapter = new EMInterfaceAdapter( this );

            // ... and try registering with the EM.

            try { emiAdapter.registerWithEM( clientName,
                                            amqpChannel, 
                                            expMonitorID, clientID ); }
            catch ( Exception e ) 
            {
                clientLogger.error( "Could not attempt registration with EM" );
                throw e; 
            }
        }
    }

    // EMIAdapterListener --------------------------------------------------------
    @Override
    public void onEMConnectionResult( boolean connected, Experiment expInfo )
    {
        if ( connected )
        {
          clientView.setStatus( "Connected to EM" );
          clientView.addLogMessage( "Linked to experiment: " + expInfo.getName() );
          clientView.enableAddEntity(true);
        }
        else
          clientView.setStatus( "Refused connection to EM" );
    }

    @Override
    public void onEMDeregistration( String reason )
    {
        clientView.addLogMessage( "Got disconnected from EM: " + reason );
        
        try
        { emiAdapter.disconnectFromEM(); }
        catch ( Exception e )
        { clientLogger.error( "Had problems disconnecting from EM: " + e.getMessage() ); }
        
        // Apologise to the user
        JOptionPane.showMessageDialog( null, "ECC disconnected this client: " + reason );
        
        clientView.dispose();
        System.exit(0);
    }
    
    @Override
    public void onDescribeSupportedPhases( EnumSet<EMPhase> phasesOUT )
    {
        phasesOUT.add( EMPhase.eEMSetUpMetricGenerators );
        phasesOUT.add( EMPhase.eEMLiveMonitoring );
        phasesOUT.add( EMPhase.eEMPostMonitoringReport );
        phasesOUT.add( EMPhase.eEMTearDown );
    }
    
    @Override
    public void onDescribePushPullBehaviours( Boolean[] pushPullOUT )
    {
        // Support for both push and pull
        pushPullOUT[0] = true;
        pushPullOUT[1] = true;
    }
    
    @Override
    public void onPopulateMetricGeneratorInfo()
    {
      // Not going to create metric generators in code, but will send them as the
      // user creates them. So just send back an empty set
      
      HashSet<MetricGenerator> mgSet = new HashSet<MetricGenerator>();
      emiAdapter.sendMetricGenerators( mgSet );
    }

    @Override
    public void onDiscoveryTimeOut()
    { clientView.addLogMessage( "Got discovery time-out message" ); }
    
    @Override
    public void onSetupMetricGenerator( UUID genID, Boolean[] resultOUT )
    {
        clientView.setStatus( "Setting up generators" );

        // Just signal that the metric generator is ready
        resultOUT[0] = true;

        clientView.addLogMessage( "Completed generator set-up" );
    }
    
    @Override
    public void onSetupTimeOut( UUID metricGeneratorID )
    { clientView.addLogMessage( "Got set-up time-out message" ); }
    
    @Override
    public void onLiveMonitoringStarted()
    {
        clientView.addLogMessage( "ECC has started Live Monitoring process" );  
    }

    @Override
    public void onStartPushingMetricData()
    {
        // Allow the human user to manually push some data
        clientView.addLogMessage( "Enabling metric push" );
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
    { clientView.addLogMessage( "Got live pull time-out message" ); }

    @Override
    public void onStopPushingMetricData()
    {
        // Stop manual pushing of data by the human user
        clientView.addLogMessage( "Disabling metric push" );
    }

    @Override
   /**
    * Note that 'reportOut' is an OUT parameter provided by the adapter
    * Method to send measurements if the entity is enabled.
    * Checks hash maps for entity and measurement set details.
    */
    public void onPullMetric( UUID measurementSetID, Report reportOut)
    {
        if (msEntitymap.containsKey( measurementSetID ))
        {
            UUID entityID = msEntitymap.get(measurementSetID);
            
            if (enabledEntityMap.containsKey( entityID ) ) // The ECC should not ask for 'disabled' entities anyway
            {
               boolean enable = enabledEntityMap.get( entityID );
               
               if (enable)
               {
                    // Create an empty instance of our measurement set
                    MeasurementSet targetSet = MetricHelper.getMeasurementSet( metricGenerator, measurementSetID );

                    // Create a copy of the measurement set, but with no measurements in
                    Report newReport = MetricHelper.createEmptyMeasurementReport( targetSet );

                    // Generate a simulated measurement using a random number between 1 and 20
                    // These measurements are for demonstration purposes only
                    // In a real situation these numbers will be defined and generated by the entity
                    int ran = 1 + ( int )( Math.random() * 20 );
                    String mes = "" + ran;
                    Measurement m = new Measurement( mes );

                    // Add measurement
                    newReport.getMeasurementSet().addMeasurement( m );
                    newReport.setNumberOfMeasurements( 1 );

                    // Copy report into OUT parameter
                    reportOut.copyReport( newReport, true );
               }
            }
        }
        
       
    }

    @Override
    public void onPullingStopped()
    {
        clientView.addLogMessage( "ECC has stopped pulling" );
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
    public void onPopulateDataBatch( EMDataBatch batchOUT )
    {
  
    }
    
    @Override
    public void onReportBatchTimeOut( UUID batchID )
    { clientView.addLogMessage( "Got post-report time-out message" ); }

    @Override
    public void onGetTearDownResult( Boolean[] resultOUT )
    {
        clientView.setStatus( "Tearing down" );
        clientView.addLogMessage( "Tearing down metric generators" );
        clientView.enableAddEntity( false );

        // Signal we've successfully torn-down
        resultOUT[0] = true;
    }

    // EMClientViewListener ------------------------------------------------------
    @Override
    public void onPushDataClicked()
    {
        
    }
    
    @Override
    public void onClientViewClosed()
    {
        // Need to notify that we're leaving...
        try { emiAdapter.disconnectFromEM();}
        catch ( Exception e )
        { clientLogger.error( "Could not cleanly disconnect from EM:\n" + e.getMessage() ); }
    }
    
    @Override
    public void onTearDownTimeOut()
    { clientView.addLogMessage( "Got tear-down time-out message" ); }

    
    // ECCNewEntityViewListener events -----------------------------------------
    
    /**
     * Collects new entity and attribute details and creates a new entity object and metric generators
     * @param entityName
     * @param attList
     * @param entityDesc 
     */
    @Override
    public void onNewEntityInfoEntered( String entityName,ArrayList<String> attList,String entityDesc )
    {

        // Create a new entity to be observed (this Java VM)
        Entity entityBeingObserved = new Entity();
        entityBeingObserved.setName( entityName );
        entityBeingObserved.setDescription( entityDesc );
        
        // Link entity with metric generator
        metricGenerator.addEntity( entityBeingObserved );

        for(int i =0; i < attList.size(); i++)
        {   
            //Extract attribute details
            String att = ( String ) attList.get(i);
            String[] atts = att.split(",");
            String attName = atts[0];
            String attDesc = atts[1];
            String attUnit = atts[2];
            String attMetricType = atts[3];
            
            //Create new attribute and associate it it with the correct entity
            Attribute entityAttribute = MetricHelper.createAttribute( attName, attDesc, entityBeingObserved );
            
            // ... a single MeasurementSet (representing the measures for the attibute)
            MeasurementSet mes =  MetricHelper.createMeasurementSet( entityAttribute, 
                                                                     MetricType.fromValue(attMetricType),
                                                                     new Unit( attUnit ),
                                                                     metricGroup );
            //Get the measurement set UUID
            UUID mesID = mes.getID();
            
            //Get the entity UUID
            UUID entID = entityBeingObserved.getUUID();
            
            //Store to measurement set UUID with the entity UUID
            msEntitymap.put(mesID, entID);
        }
        clientView.addLogMessage( "Created new entity: " + entityBeingObserved.getName() );
        
        //Get the new entity details
        UUID entityID = entityBeingObserved.getUUID();
        String eName = entityBeingObserved.getName(); 
        boolean enable = true;
        
        //Update entity map
        enabledEntityMap.put(entityID, enable);
        
        //Send entity details to the client view
        clientView.enableEntity( entityID, eName, enable );   
        
        //Creating a hash set to store metric generators
        HashSet<MetricGenerator> mgSet = new HashSet<MetricGenerator>();
        mgSet.add( metricGenerator );
        
        //Send metric generators to the EM
        emiAdapter.sendMetricGenerators( mgSet );  
    }  
    
    /**
     * Updates the entity hash map to change the status of the entity
     * @param entity
     * @param status 
     */
    @Override
    public void onEntityStatusChanged( UUID entityID, String eName, boolean status )
    {
        // Update entity map
        enabledEntityMap.put( entityID, status );
        
        // Send entity details to client view
        clientView.enableEntity( entityID, eName, status );
        
        // Send enablement to ECC
        emiAdapter.sendEntityEnabled( entityID, status );
    }

}
    
