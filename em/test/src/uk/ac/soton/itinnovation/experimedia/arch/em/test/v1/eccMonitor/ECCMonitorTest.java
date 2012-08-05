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
//      Created Date :          02-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccMonitor;

import uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccEntryPoint.ECCMonitorEntryPointTest;




/**
 * ECCMonitorTest uses ECCMonitorTestExecutor to provide a basic test for the
 * following interfaces:
 * 
 *  * IECCMonitor (the 'foundation' interface of the EM)
 *  * IECCTest    (a simple data transfer testing interface of the EM)
 * 
 * See ECCMonitorEntryPoint test for the set-up/tear-down procedures; see 
 * ECCMonitorTestExecutor for more details
 * 
 * @author sgc
 */
public class ECCMonitorTest extends ECCMonitorEntryPointTest
{
  public static void main( String[] args )
  { junit.textui.TestRunner.run( ECCMonitorTest.class ); }
  
  
  public ECCMonitorTest()
  { super(); }
  
  // Tests ---------------------------------------------------------------------
  public void testGetMonitorInterface()
  {
    assertTrue( amqpFactory != null );
    assertTrue( amqpProviderChannel != null );
    assertTrue( amqpUserChannel != null );
    
    ECCMonitorTestExecutor exe =
            new ECCMonitorTestExecutor( amqpProviderChannel,
                                        amqpUserChannel );
    
    Thread testThread = new Thread( exe );
    testThread.setPriority( Thread.MIN_PRIORITY );
    testThread.start();
    
    // Wait for a short time
    try { Thread.sleep( 2000 ); } catch ( InterruptedException ie ) {}
    
    // Check result
    assertTrue( exe.getTestResult() );
  }
  
  
  // Protected methods ---------------------------------------------------------
  @Override
  protected void setUp() throws Exception
  { super.setUp(); }
  
  @Override
  protected void tearDown() throws Exception
  { super.tearDown(); }
}
