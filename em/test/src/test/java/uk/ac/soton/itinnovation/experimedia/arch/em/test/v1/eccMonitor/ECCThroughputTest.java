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

package uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccMonitor;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;

import uk.ac.soton.itinnovation.experimedia.arch.em.test.common.ECCBaseTest;

import org.junit.*;





public class ECCThroughputTest extends ECCBaseTest
{ 
  public ECCThroughputTest()
  { super(); }
  
  // Tests ---------------------------------------------------------------------
  @Test
  public void testOrderedDataTransfer()
  {
    // Create provider/user channels
    AMQPBasicChannel providerChannel = amqpUtil.getProviderChannel();
    AMQPBasicChannel userChannel     = amqpUtil.getUserChannel();
        
    // Check they are OK
    Assert.assertEquals( true, providerChannel != null );
    Assert.assertEquals( true, userChannel != null );
    
    // Create test executor
    ECCThroughputTestExecutor exe =
            new ECCThroughputTestExecutor( this, providerChannel, userChannel );
    
    Thread testThread = new Thread( exe );
    testThread.setPriority( Thread.NORM_PRIORITY );
    testThread.start();
    
    waitForTestToComplete();
    
    // Check result
    Assert.assertEquals( true, exe.getTestResult() );
  }
}
