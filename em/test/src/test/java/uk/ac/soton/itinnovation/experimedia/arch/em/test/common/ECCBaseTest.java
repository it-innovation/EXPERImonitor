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
//      Created for Project :   experimedia-arch-ecc-em-test
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.em.test.common;

import java.util.*;
import org.junit.*;




public class ECCBaseTest implements TestEventListener
{
  private boolean testIsRunning;
  
  protected ECCAMQPUtility amqpUtil;
  
  public static final UUID   EMProviderUUID      = UUID.fromString( "00000000-0000-0000-0000-000000000000" );
  public static final UUID   EMUserUUID          = UUID.fromString( "00000000-0000-0000-0000-0000000000FF" );
  public static final UUID   EMExperimentUUID    = UUID.fromString( "00000000-0000-0000-0000-00000000FF00" );
  public static final String EMExperimentNamedID = "Experiment 1";
  public static final String EMExperimentName    = "JUnit test experiment";
  public static final String EMExperimentDesc    = "This isn't really an experiment";
  public static final Date   EMStartDate         = new Date();
  
  
  public ECCBaseTest()
  {
    super();
    
    amqpUtil = new ECCAMQPUtility();
  }
  
  // TestEventListener ---------------------------------------------------------
  @Override
  public void onTestCompleted()
  { setIsTestRunning( false ); }
  

  @Before
  public void setUp() throws Exception
  { 
    try
    { 
      amqpUtil.setUpAMQP();
      
      testIsRunning = true;
    }
    catch ( Exception e )
    { amqpUtil.utilLogger.error( "Test set-up error: " + e.getMessage()); }
  }
  
  @After
  public void tearDown() throws Exception
  { 
    try { amqpUtil.tearDownAMQP(); }
    catch ( Exception e )
    { amqpUtil.utilLogger.error( "Test tear-down error: " + e.getMessage() ); }
  }
  
  protected synchronized void setIsTestRunning( boolean running )
  {
    testIsRunning = running;
    notifyAll();
  }
  
  protected synchronized void waitForTestToComplete()
  {
    while ( testIsRunning ) 
      try { wait(); }
      catch ( InterruptedException ie ) {}
  }
}
