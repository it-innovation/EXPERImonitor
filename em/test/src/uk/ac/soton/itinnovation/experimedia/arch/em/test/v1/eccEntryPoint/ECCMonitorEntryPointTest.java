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
//      Created By :            sgc
//      Created Date :          31-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccEntryPoint;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import junit.framework.*;
import java.util.*;




/**
 * ECCMonitorEntryPointTest is a test case that demonstrates entry-point connection
 * to the Experiment Monitor. This class is also extended by other test cases
 * to re-use the 'setUp()' and 'tearDown()' methods. Since EM messages are passed
 * across an AMQP bus, the test itself must be run by ECCMonitorEntryPointTestExecutor
 * on a thread.
 * 
 * static UUIDs:
 *
 * EMProviderUUID - UUID used to specify the 'provider' (acting as the EM)
 * EMUserUUID     - UUID used to specify a 'user' (or client connecting to the EM)
 * 
 * @author sgc
 */
public class ECCMonitorEntryPointTest extends TestCase
{
  protected AMQPConnectionFactory amqpFactory;
  protected AMQPBasicChannel      amqpProviderChannel;
  protected AMQPBasicChannel      amqpUserChannel;
  
  public static final UUID EMProviderUUID = UUID.fromString( "00000000-0000-0000-0000-000000000000" );
  public static final UUID EMUserUUID     = UUID.fromString( "00000000-0000-0000-0000-0000000000FF" );
  
  public static void main( String[] args )
  { junit.textui.TestRunner.run( ECCMonitorEntryPointTest.class ); }
  
  
  
  
  public ECCMonitorEntryPointTest()
  { super(); }
  
  // Tests ---------------------------------------------------------------------
  public void testProviderInit()
  {
    // Make sure AMQP resources are OK
    assertTrue( amqpFactory != null );
    assertTrue( amqpProviderChannel != null );
    assertTrue( amqpUserChannel != null );
    
    // Create Entry Point test executor (give seprate channels for provider
    // and user)
    ECCMonitorEntryPointTestExecutor exe = 
            new ECCMonitorEntryPointTestExecutor( amqpProviderChannel,
                                                  amqpUserChannel );
    
    // Run it
    Thread testThread = new Thread( exe );
    testThread.start();
    
    // Wait for a short time
    try { Thread.sleep( 2000 ); } catch ( InterruptedException ie ) {}
    
    // Check result
    assertTrue( exe.getTestResult() );
  }
  
  // Protected methods ---------------------------------------------------------
  @Override
  protected void setUp() throws Exception
  {
    try
    { 
      super.setUp();
      
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
  
  @Override
  protected void tearDown() throws Exception
  {
    try
    {
      super.tearDown();
      
      // Tidy up AMQP resources
      if ( amqpProviderChannel != null ) amqpProviderChannel.close();
      if ( amqpUserChannel != null )     amqpUserChannel.close();
    }
    catch ( Exception e ) { throw e; }
  }
}
