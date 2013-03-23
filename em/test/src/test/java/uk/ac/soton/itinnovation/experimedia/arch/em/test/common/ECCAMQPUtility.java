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
//      Created Date :          13-Dec-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.em.test.common;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import java.util.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.loggin.impl.Log4JImpl;




public class ECCAMQPUtility
{
  private AMQPConnectionFactory amqpFactory;
  private AMQPBasicChannel      amqpProviderChannel;
  private AMQPBasicChannel      amqpUserChannel;
  
  public static final UUID   EMProviderUUID      = UUID.fromString( "00000000-0000-0000-0000-000000000000" );
  public static final UUID   EMUserUUID          = UUID.fromString( "00000000-0000-0000-0000-0000000000FF" );
  public static final UUID   EMExperimentUUID    = UUID.fromString( "00000000-0000-0000-0000-00000000FF00" );
  public static final String EMExperimentNamedID = "Experiment 1";
  public static final String EMExperimentName    = "JUnit test experiment";
  public static final String EMExperimentDesc    = "This isn't really an experiment";
  public static final Date   EMStartDate         = new Date();
  
  public final IECCLogger utilLogger;
  
  
  public ECCAMQPUtility()
  {
    // Configure logging system
    Logger.setLoggerImpl( new Log4JImpl() );  
    utilLogger = Logger.getLogger( ECCAMQPUtility.class );
  }
  
  public void setUpAMQP() throws Exception
  {
    try
    { 
      // Create AMQP factory
      amqpFactory = new AMQPConnectionFactory();
      if ( amqpFactory == null ) throw new Exception( "Could not create AMQP Connection factory" );
      
      // Get local IP (running RabbitMQ)
      String localIP = amqpFactory.getLocalIP();
      if ( localIP == null ) throw new Exception( "Could not get local IP" );
      
      // Try setting the IP for the factory to use
      if ( !amqpFactory.setAMQPHostIPAddress( localIP ) )
        throw new Exception( "Could not set AMQP bus IP" );
      
      // Try connecting to RabbitMQ (will throw if there are problems)
      amqpFactory.connectToAMQPHost();
      
      // Try creating provider/user channels (will throw if there are internal problems)
      amqpProviderChannel = amqpFactory.createNewChannel();
      amqpUserChannel     = amqpFactory.createNewChannel();
      
      // Throw if we did not get a valid channel
      if ( amqpProviderChannel == null ) throw new Exception( "Could not create provider AMQP channel" );
      if ( amqpUserChannel == null ) throw new Exception( "Could not create user AMQP channel" );
    }
    catch ( Exception e ) { throw e; }
  }
  
  protected void tearDownAMQP() throws Exception
  {
    try
    {      
      // Tidy up AMQP resources
      if ( amqpProviderChannel != null ) amqpProviderChannel.close();
      if ( amqpUserChannel != null )     amqpUserChannel.close();
      
      amqpProviderChannel = null;
      amqpUserChannel     = null;
    }
    catch ( Exception e ) { throw e; }
  }
  
  public AMQPBasicChannel getProviderChannel()
  { return amqpProviderChannel; }
  
  public AMQPBasicChannel getUserChannel()
  { return amqpUserChannel; }
}
