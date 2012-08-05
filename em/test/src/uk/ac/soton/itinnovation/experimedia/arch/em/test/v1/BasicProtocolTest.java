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

package uk.ac.soton.itinnovation.experimedia.arch.em.test.v1;

import uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccEntryPoint.ECCMonitorEntryPointTest;
import uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccMonitor.ECCMonitorTest;

import junit.framework.*;




public class BasicProtocolTest
{
  public static void main( String[] args )
  {
    junit.textui.TestRunner.run( getEMEntryPointSuite() );
  }
  
  // Private methods -----------------------------------------------------------
  private static Test getEMEntryPointSuite()
  {
    TestSuite suite = new TestSuite( "EM EntryPoint Tests" );
    
    // ECCMonitorEntryPointInterface
    suite.addTestSuite( ECCMonitorEntryPointTest.class );
    
    // ECCMonitorInterface
    suite.addTestSuite( ECCMonitorTest.class );
    
    return suite;
  }
}
