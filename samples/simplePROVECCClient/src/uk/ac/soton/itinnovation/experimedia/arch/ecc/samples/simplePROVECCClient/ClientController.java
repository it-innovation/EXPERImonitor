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
//      Created By :            Simon Crowle
//      Created Date :          27-Sep-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.simplePROVECCClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.*;

import java.util.*;
import javax.swing.JOptionPane;




public class ClientController implements ClientViewListener
{
    // Logger
    private static IECCLogger ctrLogger = Logger.getLogger( ClientController.class );

    // ECC connection gear
    private AMQPConnectionFactory amqpFactory;
    private EMInterfaceAdapter    eccAdapter;
    private EMEventHandler        eventHandler;
    private boolean               connectedToECC;
    private boolean               monitoringActive;

    // The UI and names of selected items in the UI
    private ClientView view;
    private String selectedAgent;
    private String selectedActivity;
    private String selectedEntity;
    
    // Simple metric generator and group for this client
    private MetricGenerator metricGenGenerator;
    private MetricGroup     agentMetricGroup;
    
    // Simple agent activity model: what agents did last & how many activities they have started
    private HashMap<String, EDMActivity> currentAgentActivities;      // Agent IRI x last PROV activity
    private HashMap<String, Integer>     currentAgentActivityCount;   // Agent IRI x activity count

    
    public ClientController()
    {
        metricGenGenerator = new MetricGenerator();
        agentMetricGroup   = MetricHelper.createMetricGroup( "Agent group", 
                                                             "Metrics associated with PROV agents", 
                                                             metricGenGenerator );
        
        currentAgentActivities    = new HashMap<String, EDMActivity>();
        currentAgentActivityCount = new HashMap<String, Integer>();
    }

    public void initialise( Properties eccProps )
    {
        // Try to connect to the ECC
        if ( tryConnectToECC(eccProps) )
        {      
            view = new ClientView( this );
            view.setVisible( true );
        }
        else // If that fails, shutdown
            shutdown();
    }

    // ClientViewListener ------------------------------------------------------
    @Override
    public void onClientViewClosed()
    {
        shutdown();
    }

    @Override
    public void onAgentSelected( String agent )
    {
        if ( agent != null ) selectedAgent = agent;
    }

    @Override
    public void onActivitySelected( String activity )
    {
        if ( activity != null ) selectedActivity = activity;
    }

    @Override
    public void onEntitySelected( String entity )
    {
        if ( entity != null ) selectedEntity = entity;
    }

    @Override
    public void onSendECCData()
    {
        // Check we are connected and in live monitoring mode
        if ( connectedToECC && monitoringActive )
        {
            // Make sure we have selections from the UI
            if ( selectedAgent != null && selectedActivity != null && selectedEntity != null )
            {
                try
                {
                    // Get the PROV agent the user has selected
                    EDMProvFactory factory = EDMProvFactory.getInstance();
                    
                    EDMAgent currPROVAgent = factory.getAgent(selectedAgent);
                    if (currPROVAgent==null) {
                    	currPROVAgent = factory.createAgent( selectedAgent, // Unique identifier
                                                             selectedAgent );   // Friendly name
                    }

                    // Update ECC with PROV report
                    sendPROVData( currPROVAgent );

                    // Update ECC with agent activity observation
                    sendMetricData( currPROVAgent );
                    
                    // Notify user data has been sent
                    JOptionPane.showMessageDialog( view, "Sent PROV & Metric data to ECC", "Send status", 
                                                   JOptionPane.INFORMATION_MESSAGE );
                }
                catch ( Exception ex )
                { displayError( "Problems sending to ECC", "Could not find PROV agent to send to ECC: " + ex.getMessage() ); }
            }
            else
              displayError( "Not ready to send", "Please select an Agent, Activity & Entity" ); 
        }
        else
          displayError( "Not connected to ECC/monitoring anymore", "Please check connection to ECC" );
    }

    // Private methods ---------------------------------------------------------
    private void createPROVAgents()
    {
        // Create agents with some additional example ontology data
        EDMProvFactory factory = EDMProvFactory.getInstance();

        try
        {
            // Create Alice
            EDMAgent agentAlice = factory.createAgent("Alice", "Alice");
            
            // Create Bob
            EDMAgent agentBob = factory.createAgent( "Bob", "Bob" );
            
            // Create Carol
            EDMAgent agentCarol = factory.createAgent( "Carol", "Carol" );
            
            // Create FOAF ontology mapping in factory
            factory.addOntology("foaf", "http://xmlns.com/foaf/0.1/");

            // Describe agents as FOAF 'Person'
            agentAlice.addOwlClass( factory.getNamespaceForPrefix( "foaf" ) + "Person" );
            agentBob.addOwlClass( factory.getNamespaceForPrefix( "foaf" ) + "Person" );
            agentCarol.addOwlClass( factory.getNamespaceForPrefix( "foaf" ) + "Person" );

            // Create some simple relationships...
            // Alice knows Bob
            agentAlice.addTriple( factory.getNamespaceForPrefix( "foaf" ) + "knows", 
                                  agentBob.getIri(), 
                                  EDMTriple.TRIPLE_TYPE.OBJECT_PROPERTY );

            // Bob knows Carol
            agentBob.addTriple( factory.getNamespaceForPrefix( "foaf" ) + "knows", 
                                agentCarol.getIri(), 
                                EDMTriple.TRIPLE_TYPE.OBJECT_PROPERTY );

            // Carol knows Alice
            agentCarol.addTriple( factory.getNamespaceForPrefix( "foaf" ) + "knows", 
                                  agentAlice.getIri(), 
                                  EDMTriple.TRIPLE_TYPE.OBJECT_PROPERTY );

        }
        catch ( Exception ex )
        { displayError( "Could not create PROV Agents", ex.getMessage() ); }
    }
    
    private void createAgentMetrics()
    {
        // We'll model how many times each PROV agent starts a new activity      
        try
        {
            EDMProvFactory factory = EDMProvFactory.getInstance();

            makeAgentMetric( factory.getAgent("Alice") );
            makeAgentMetric( factory.getAgent("Bob") );
            makeAgentMetric( factory.getAgent("Carol") );
        }
        catch ( Exception ex )
        { displayError( "Could not create metrics for agents: ", ex.getMessage() ); } 
    }
    
    private void makeAgentMetric( EDMAgent agent )
    {
        // Metric entity
        Entity metEntity = new Entity();              // Create
        metEntity.setName( agent.getFriendlyName() ); // Friendly name
        metEntity.setEntityID( agent.getIri() );      // Link to PROV agent

        metricGenGenerator.addEntity( metEntity );    // Add to our generator

        // We'll observe how active this agent will be at run-time
        Attribute activityCount = MetricHelper.createAttribute( "Agent activity count", 
                                                                "Number of times this agent has started an activity", 
                                                                metEntity );

        // Create a measurement set for the attribute
        MetricHelper.createMeasurementSet( activityCount, MetricType.RATIO, 
                                           new Unit( "PROV Activity total" ), 
                                           agentMetricGroup );

        // Set their activity count to zero
        currentAgentActivityCount.put( agent.getIri(), new Integer(0) );
    }

    private void sendMetricData( EDMAgent agent )
    { 
        // Find metric agent using PROV agent IRI
        String provIRI = agent.getIri();

        Entity observedAgent = MetricHelper.getEntityFromID( provIRI,
                                                             metricGenGenerator );
        if ( observedAgent != null )
        {
            // Get attribute
            Attribute attr = MetricHelper.getAttributeByName( "Agent activity count", 
                                                              observedAgent );

            // Get measurement set
            MeasurementSet ms = MetricHelper.getMeasurementSetForAttribute( attr, 
                                                                            metricGenGenerator );

            // Update the activity count for this agent
            Integer count       = currentAgentActivityCount.get( provIRI );
            Measurement measure = new Measurement( count.toString() );

            // Make a report for the ECC
            Report eccReport = MetricHelper.createMeasurementReport( ms, measure );

            // Push report to ECC
            eccAdapter.pushMetric( eccReport );
        }
        else displayError( "Could not send metric data", "Could not find metric entity to send ECC" );   
    }
    
    private void sendPROVData( EDMAgent agent )
    {
        try
        {
            EDMProvFactory factory = EDMProvFactory.getInstance();

            // First make sure we have stopped the last activity associated with the
            EDMActivity lastActivity = currentAgentActivities.get( agent.getUniqueIdentifier() );
            if ( lastActivity != null )
              agent.stopActivity( lastActivity ); // Get the agent to stop the last activity

            // Now create a unique activity associated with this statement
            EDMActivity activity = agent.startActivity( "activity_" + UUID.randomUUID().toString(), // Unique id
                                                        selectedActivity );                         // Friendly name

            // Update the activity model for this agent
            updateCurrentAgentActivity( agent, activity );

            // Link activity with PROV entity
            EDMEntity entity = activity.generateEntity( selectedEntity, selectedEntity );
            activity.useEntity( entity );

            // We're finished. Send a report to the ECC
            EDMProvReport report = factory.createProvReport();
            eccAdapter.pushPROVStatement( report );

            // Log out the prov we have just generated
            logPROVReportSent( report );
        }
        catch ( Exception ex )
        { ex.printStackTrace();
        	displayError( "Could not create PROV report", ex.getMessage() ); }   
    }
    
    private void logPROVReportSent( EDMProvReport report )
    {
        if ( report != null )
        {
            Collection<EDMTriple> triples = report.getTriples().values();

            String provTripleList = "";

            for ( EDMTriple triple : triples )
                provTripleList += triple.toString() + "\n";

            ctrLogger.info( "Sent PROV: \n" + provTripleList );
        }
    }

    private void displayError( String title, String detail )
    {
        JOptionPane.showMessageDialog( view, detail, title, 
                                       JOptionPane.ERROR_MESSAGE );
    }

    private boolean tryConnectToECC( Properties eccProps )
    {
        String error;

        if ( eccProps != null )
        {
            // Create connection to Rabbit server ----------------------------------
            try
            {
                amqpFactory = new AMQPConnectionFactory();
                amqpFactory.connectToAMQPHost( eccProps );

                AMQPBasicChannel amqpChannel = amqpFactory.createNewChannel();

                EMEventHandler eventHandler = new EMEventHandler();
                eccAdapter = new EMInterfaceAdapter( eventHandler );

                String eccIDVal = (String) eccProps.get( "Monitor_ID" );
                UUID eccID = UUID.fromString( eccIDVal );

                eccAdapter.registerWithEM( "ECC Simple PROV Client", amqpChannel, 
                                           eccID, 
                                           UUID.randomUUID() );

              return true;
            }
            catch ( Exception ex )
            { error = "Could not connect to RabbitMQ: " + ex.getMessage(); }
        }
        else error = "ECC Properties are null";

        if ( error != null ) displayError( "ECC Connection error", error );

        return false;
    }

    private void onConnectionResult( boolean connected )
    {
        if ( connected )
        {
            connectedToECC = connected;

            createPROVAgents();

            createAgentMetrics();
        }
    }

    private void setMonitoringActive( boolean active )
    { monitoringActive = active; }

    private void sendMetricInfoToECC()
    {
        // Put generator in a set and send to ECC
        HashSet<MetricGenerator> mGens = new HashSet<MetricGenerator>();
        mGens.add( metricGenGenerator );

        eccAdapter.sendMetricGenerators( mGens );
    }

    private void shutdown()
    {
        if ( eccAdapter != null )
            try
            { 
                eccAdapter.disconnectFromEM();
                connectedToECC = false;
            }
            catch ( Exception ex )
            { ctrLogger.error( "Could not cleanly disconnect from ECC", ex ); }

        eccAdapter = null;

        if ( amqpFactory != null ) amqpFactory.closeDownConnection();
    }

    private void updateCurrentAgentActivity( EDMAgent agent, EDMActivity activity )
    {
        if ( agent != null )
        {
            // Update activity count for this agent
            String agentIRI = agent.getIri();
            
            Integer count = currentAgentActivityCount.get( agentIRI );
            count++;
            currentAgentActivityCount.put( agentIRI, count );
          
            // Update the current activity
            currentAgentActivities.remove( agentIRI );
            currentAgentActivities.put( agentIRI, activity ); // Activity can be null
        }
    }

    // Private classes -----------------------------------------------------------
    private class EMEventHandler extends EMIAdapterEventHandler
    {
      public EMEventHandler()
      {
        super( true, false );
      }

      @Override
      public void onEMConnectionResult( boolean connected, Experiment exp )
      { onConnectionResult( connected ); }

      @Override
      public void onEMDeregistration( String reason )
      { onConnectionResult( false ); }

      @Override
      public void onPopulateMetricGeneratorInfo()
      { sendMetricInfoToECC(); }

      @Override
      public void onStartPushingMetricData()
      { setMonitoringActive( true ); }

      @Override
      public void onStopPushingMetricData()
      { setMonitoringActive( false ); }
  }
}
