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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecyle.EMLifecycleManager;

import java.util.UUID;





public class ExperimentMonitor implements IExperimentMonitor
{
  private IExperimentMonitor.eStatus monitorStatus = IExperimentMonitor.eStatus.NOT_YET_INITIALISED;
  private IExperimentMonitorListener expMonitorListener;
  private AMQPBasicChannel           amqpChannel;
  
  private EMConnectionManager connectionManager;
  private EMLifecycleManager  lifecycleManager;
  
  
  public ExperimentMonitor()
  {
  }
  
  public void initialise( String rabbitServerIP ) throws Exception
  {
    if ( rabbitServerIP == null ) throw new Exception( "Rabbit Server IP address is empty" );
    
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
  
  // IExperimentMonitor --------------------------------------------------------
  @Override
  public eStatus getStatus()
  { return monitorStatus; }
  
  @Override
  public void openEntryPoint( UUID epID ) throws Exception
  {
    // Safety first
    if ( monitorStatus != IExperimentMonitor.eStatus.INITIALISED ) 
      throw new Exception( "Not in a state to open entry point" );
    
    if ( epID == null ) 
      throw new Exception( "Entry point ID is null" );
    
    // Initialise connection manager
    if ( !connectionManager.initialise( epID, amqpChannel, lifecycleManager ) )
      throw new Exception( "Could not open entry point interface!" );
    
    // Initialise lifecycle manager
    lifecycleManager.initialise( epID, amqpChannel );
  
    monitorStatus = IExperimentMonitor.eStatus.ENTRY_POINT_OPEN;
  }
  
  @Override
  public void startLifecycle()
  {
  }
  
  @Override
  public void endLifecycle()
  {
    if ( amqpChannel != null )    amqpChannel.close();
  }
  
  @Override
  public void setListener( IExperimentMonitorListener listener )
  { if ( listener != null ) expMonitorListener = listener; }
}
