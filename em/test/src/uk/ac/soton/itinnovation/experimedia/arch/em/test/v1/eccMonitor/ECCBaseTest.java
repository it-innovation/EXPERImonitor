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

package uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccMonitor;

import java.util.*;
import junit.framework.TestCase;




public class ECCBaseTest extends TestCase
{
  protected ECCAMQPUtility amqpUtil;
  
  public static final UUID EMProviderUUID        = UUID.fromString( "00000000-0000-0000-0000-000000000000" );
  public static final UUID EMUserUUID            = UUID.fromString( "00000000-0000-0000-0000-0000000000FF" );
  public static final UUID EMExperimentUUID      = UUID.fromString( "00000000-0000-0000-0000-00000000FF00" );
  public static final String EMExperimentNamedID = "Experiment 1";
  public static final String EMExperimentName    = "JUnit test experiment";
  public static final String EMExperimentDesc    = "This isn't really an experiment";
  public static final Date   EMStartDate         = new Date();
  
  
  public ECCBaseTest()
  {
    super();
    
    amqpUtil = new ECCAMQPUtility();
  }
  
  // Protected methods ---------------------------------------------------------
  @Override
  protected void setUp() throws Exception
  { 
    super.setUp();
    
    try { amqpUtil.setUpAMQP(); }
    catch ( Exception e )
    { amqpUtil.utilLogger.error( "Test set-up error: " + e.getMessage()); }
  }
  
  @Override
  protected void tearDown() throws Exception
  { 
    super.tearDown();
    
    try { amqpUtil.tearDownAMQP(); }
    catch ( Exception e )
    { amqpUtil.utilLogger.error( "Test tear-down error: " + e.getMessage() ); }
  }
}
