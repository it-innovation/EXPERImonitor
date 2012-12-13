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

package uk.ac.soton.itinnovation.experimedia.arch.em.test.v1;

import uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccEntryPoint.ECCMonitorEntryPointTest;
import uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccMonitor.*;

import junit.framework.*;
import org.apache.log4j.Logger;




public class ExperimentMonitorTestSuite extends TestCase
{
  private static Logger testLogger = Logger.getLogger( ExperimentMonitorTestSuite.class );
  
  public static void main( String[] args )
  {
    junit.textui.TestRunner.run( getTestSuite() );
    
    testLogger.info( "Experiment Monitor Test Suite has completed." );
    System.exit( 0 );
  }
  
  // Private methods -----------------------------------------------------------
  private static Test getTestSuite()
  {
    TestSuite suite = new TestSuite( "Experiment Monitor Test Suite" );
    
    // ECCMonitorInterface
    suite.addTestSuite( ECCMonitorEntryPointTest.class );
    suite.addTestSuite( ECCMonitorTest.class );
    suite.addTestSuite( ECCThroughputTest.class );
    
    return suite;
  }
}
