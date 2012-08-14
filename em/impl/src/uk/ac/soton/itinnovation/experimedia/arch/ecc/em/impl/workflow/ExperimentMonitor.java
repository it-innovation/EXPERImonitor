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
//      Created Date :          13-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecyle.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.dataModel.EMClient;

import java.util.*;




public class ExperimentMonitor implements IExperimentMonitor,
                                          EMConnectionManagerListener,
                                          EMLifecycleManagerListener
{
  private IExperimentMonitor.eStatus monitorStatus = IExperimentMonitor.eStatus.NOT_YET_INITIALISED;
  private IExperimentMonitorListener expMonitorListener;
  private AMQPBasicChannel           amqpChannel;
  
  private EMConnectionManager connectionManager;
  private EMLifecycleManager  lifecycleManager;
  
  
  public ExperimentMonitor()
  {
  }
 
  // IExperimentMonitor --------------------------------------------------------
  @Override
  public eStatus getStatus()
  { return monitorStatus; }
  
  @Override
  public void openEntryPoint( String rabbitServerIP, UUID epID ) throws Exception
  {
    // Safety first
    if ( rabbitServerIP == null || rabbitServerIP.equals("") )
      throw new Exception( "Rabbit server IP is invalid" );
    
    if ( epID == null ) 
      throw new Exception( "Entry point ID is null" );
    
    // Try initialising a connection with the Rabbit Server
    try { initialise(rabbitServerIP); }
    catch( Exception e ) { throw e; }
    
    if ( monitorStatus != IExperimentMonitor.eStatus.INITIALISED ) 
      throw new Exception( "Not in a state to open entry point" );
    
    // Initialise connection manager
    if ( !connectionManager.initialise( epID, amqpChannel, lifecycleManager ) )
      throw new Exception( "Could not open entry point interface!" );
    
    // Initialise lifecycle manager
    lifecycleManager.initialise( amqpChannel, epID, this );
  
    monitorStatus = IExperimentMonitor.eStatus.ENTRY_POINT_OPEN;
  }
  
  @Override
  public Set<Map.Entry<UUID, String>> getConnectedClientInfo()
  {
    if ( connectionManager == null )
      return new HashSet<Map.Entry<UUID, String>>();
    
    return connectionManager.getConnectedClientInfo();
  }
  
  @Override
  public void startLifecycle() throws Exception
  {
    if ( monitorStatus != IExperimentMonitor.eStatus.ENTRY_POINT_OPEN )
      throw new Exception( "Not in a state ready to start lifecycle" );
    
    if ( connectionManager.getConnectedClientCount() == 0 )
      throw new Exception( "No clients connected to monitor" );
  }
  
  @Override
  public void endLifecycle() throws Exception
  {
    if ( amqpChannel != null ) amqpChannel.close();
  }
  
  @Override
  public void setListener( IExperimentMonitorListener listener )
  { if ( listener != null ) expMonitorListener = listener; }
  
  // EMConnectionManagerListener -----------------------------------------------
  @Override
  public void onClientRegistered( EMClient client )
  {
    if ( expMonitorListener != null )
      expMonitorListener.onClientRegistered( client.getID(),
                                             client.getName() );
  }
  
  // EMLifecycleManagerListener ------------------------------------------------
  
  
  // Private methods -----------------------------------------------------------
  private void initialise( String rabbitServerIP ) throws Exception
  {
    AMQPConnectionFactory amqpCF = new AMQPConnectionFactory();
    
    if ( !amqpCF.setAMQPHostIPAddress(rabbitServerIP) )
      throw new Exception( "Could not set the server IP correctly" );
    
    amqpCF.connectToAMQPHost();
    
    if ( !amqpCF.isConnectionValid() ) throw new Exception( "Could not connect to Rabbit server" );
    
    amqpChannel = amqpCF.createNewChannel();
    
    if ( amqpChannel == null ) throw new Exception( "Could not create AMQP channel" );
    
    connectionManager = new EMConnectionManager();
    lifecycleManager  = new EMLifecycleManager();
    
    monitorStatus = IExperimentMonitor.eStatus.INITIALISED;
  }
}
