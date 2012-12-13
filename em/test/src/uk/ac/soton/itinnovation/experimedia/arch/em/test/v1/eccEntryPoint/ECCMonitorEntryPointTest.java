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
//      Created Date :          31-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccEntryPoint;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.em.test.common.ECCBaseTest;

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
public class ECCMonitorEntryPointTest extends ECCBaseTest
{  
  public static void main( String[] args )
  { junit.textui.TestRunner.run( ECCMonitorEntryPointTest.class ); }
  
  
  public ECCMonitorEntryPointTest()
  { super(); }
  
  // Tests ---------------------------------------------------------------------
  public void testProviderInit()
  {
    // Create provider/user channels
    AMQPBasicChannel providerChannel = amqpUtil.getProviderChannel();
    AMQPBasicChannel userChannel     = amqpUtil.getUserChannel();
        
    // Check they are OK
    assertTrue( providerChannel != null );
    assertTrue( userChannel != null );
    
    // Create Entry Point test executor (give seprate channels for provider
    // and user)
    ECCMonitorEntryPointTestExecutor exe = 
            new ECCMonitorEntryPointTestExecutor( this,
                                                  providerChannel, 
                                                  userChannel );
    
    // Run it
    Thread testThread = new Thread( exe );
    testThread.setPriority( Thread.MIN_PRIORITY );
    testThread.start();
    
    waitForTestToComplete();
    
    // Check result
    assertTrue( exe.getTestResult() );
  }
}
