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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.*;

import java.util.*;
import javax.swing.JOptionPane;




public class ClientController implements ClientViewListener
{
  private static IECCLogger ctrLogger = Logger.getLogger( ClientController.class );
  
  private AMQPConnectionFactory amqpFactory;
  private EMInterfaceAdapter    eccAdapter;
  private EMEventHandler        eventHandler;
  private boolean               connectedToECC;
  private boolean               monitoringActive;
  
  private String selectedAgent;
  private String selectedActivity;
  private String selectedEntity;
  private HashMap<EDMAgent, EDMActivity> currentAgentActivities;
  
  private ClientView view;
  
  
  public ClientController()
  {
    currentAgentActivities = new HashMap<EDMAgent, EDMActivity>();
  }
  
  public void initialise( Properties eccProps ) throws Exception
  {
    if ( tryConnectToECC(eccProps) )
    {      
      view = new ClientView( this );
      view.setVisible( true );
    }
    else
      shutdown();
  }
  
  // ClientViewListener --------------------------------------------------------
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
  public void onSendProvData()
  {
    if ( connectedToECC )
    {
      if ( selectedAgent != null && selectedActivity != null && selectedEntity != null )
      {
        EDMProvFactory factory = EDMProvFactory.getInstance();
        
        try
        {
          // First make sure we have stopped the last activity associated with the
          // current agent (if an activity exists)
          EDMAgent agent = factory.getOrCreateAgent( selectedAgent, selectedAgent );
          
          EDMActivity lastActivity = currentAgentActivities.get( agent );
          if ( lastActivity != null )
            agent.stopActivity( lastActivity );
          
          // Now create a unique activity associated with this statement
          EDMActivity activity = agent.startActivity( UUID.randomUUID().toString(), selectedActivity );
          
          // Remember it so that we can stop it next time the agent does something new
          updateCurrentAgentActivity( agent, activity );
          
          // Link it with the entity
          EDMEntity entity = activity.generateEntity( selectedEntity, selectedEntity );
          activity.useEntity( entity );
          
          // We're finished. Send a report to the ECC
          EDMProvReport report = factory.createProvReport();
          eccAdapter.pushPROVStatement( report );
        }
        catch ( Exception ex )
        { displayError( "Could not create PROV report", ex.getMessage() ); }
      }
      else
        displayError( "Not ready to send", "Please select an Agent, Activity & Entity" );
    }
    else
      displayError( "Not connected to ECC", "Have you got an ECC running?" );
  }
  
  // Private methods -----------------------------------------------------------
  private void displayError( String title, String detail )
  {
    JOptionPane.showMessageDialog( view, detail, title, 
                                     JOptionPane.ERROR_MESSAGE );
  }
  
  private boolean tryConnectToECC( Properties eccProps )
  {
    String error = null;
    
    if ( eccProps != null )
    {
      // Create connection to Rabbit server ------------------------------------
      try
      {
        amqpFactory = new AMQPConnectionFactory();
        amqpFactory.connectToAMQPHost( eccProps );
        
        AMQPBasicChannel amqpChannel = amqpFactory.createNewChannel();
        
        eventHandler = new EMEventHandler();
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
    connectedToECC = connected;
  }
  
  private void setMonitoringActive( boolean active )
  {
    monitoringActive = active;
  }
  
  private void sendMetricInfoToECC()
  {
    HashSet<MetricGenerator> empty = new HashSet<MetricGenerator>();
    
    eccAdapter.sendMetricGenerators( empty );
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
      currentAgentActivities.remove( agent );
      currentAgentActivities.put( agent, activity ); // Activity can be null
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
