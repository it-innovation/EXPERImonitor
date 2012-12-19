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

package test.java.uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccMonitor;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMTest_Listener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;

import test.java.uk.ac.soton.itinnovation.experimedia.arch.em.test.common.*;





public class ECCThroughputTestExecutor extends ECCBaseTestExecutor
                                       implements Runnable,
                                                  IEMTest_Listener
{
  public ECCThroughputTestExecutor( TestEventListener listener,
                                    AMQPBasicChannel provider,
                                    AMQPBasicChannel user )
  {
    super( listener, provider, user );
  }
  
  // ECCBaseTestExecutor -------------------------------------------------------
  @Override
  public boolean getTestResult()
  {
    return true;
  }
  
  // IEMTest_Listener ----------------------------------------------------------
  @Override
  public void onReceivedData( int byteCount, byte[] dataBody )
  {
    
  }
  
  // Runnable ------------------------------------------------------------------
  @Override
  public void run()
  {
    notifyTestEnds( "ECC Throughput Test Executor" );
  }
}
