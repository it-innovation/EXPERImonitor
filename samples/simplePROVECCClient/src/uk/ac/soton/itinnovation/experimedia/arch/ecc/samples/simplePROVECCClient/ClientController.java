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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;

import java.util.*;




public class ClientController implements ClientViewListener
{
  private static IECCLogger ctrLogger = Logger.getLogger( ClientController.class );
  
  private AMQPConnectionFactory amqpFactory;
  private AMQPBasicChannel      amqpChannel;
  private EMInterfaceAdapter    eccAdapter;
  private EMEventHandler        eventHandler;
  
  private ClientView view;
  
  private boolean connectedToECC;
  private boolean monitoringActive;
  
  
  public ClientController()
  {
  }
  
  public void initialise( Properties eccProps ) throws Exception
  {
    String error = null;
    
    if ( eccProps != null )
    {
      // Create connection to Rabbit server ------------------------------------
      try
      {
        amqpFactory = new AMQPConnectionFactory();
        amqpFactory.connectToAMQPHost( eccProps );
        amqpChannel = amqpFactory.createNewChannel();
        
        eventHandler = new EMEventHandler();
        eccAdapter = new EMInterfaceAdapter( eventHandler );
        
        String eccIDVal = (String) eccProps.get( "Monitor_ID" );
        UUID eccID = UUID.fromString( eccIDVal );
        
        eccAdapter.registerWithEM( "ECC Simple PROV Client", amqpChannel, 
                                   eccID, 
                                   UUID.randomUUID() );
        
        view = new ClientView( this );
        view.setVisible( true );
      }
      catch ( Exception ex )
      { error = "Could not connect to RabbitMQ: " + ex.getMessage(); }
    }
    else error = "ECC Properties are null";
    
    if ( error != null ) throw new Exception( error );
  }
  
  // ClientViewListener --------------------------------------------------------
  @Override
  public void onClientViewClosed()
  {
    shutdown();
  }
  
  @Override
  public void onSendServerPROVClicked()
  {
    if ( connectedToECC )
    {
      EDMProvReport serverStatement = new EDMProvReport();
      
      eccAdapter.pushPROVStatement( serverStatement );
    }
  }
  
  @Override
  public void onSendClientPROVClicked()
  {
    if ( connectedToECC )
    {
      EDMProvReport clientStatement = new EDMProvReport();
      
      eccAdapter.pushPROVStatement( clientStatement );
    }
  }
  
  // Private methods -----------------------------------------------------------
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
    try
    { 
      eccAdapter.disconnectFromEM();
      connectedToECC = false;
    }
    catch ( Exception ex )
    { ctrLogger.error( "Could not cleanly disconnect from ECC" ); }
    
    eccAdapter = null;
    amqpChannel.close();
    amqpFactory.closeDownConnection();
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
