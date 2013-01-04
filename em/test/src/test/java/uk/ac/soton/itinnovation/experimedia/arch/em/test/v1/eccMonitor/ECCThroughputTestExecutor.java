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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.IAMQPMessageDispatchPump;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.IEMTest;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMTest_Listener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;

import uk.ac.soton.itinnovation.experimedia.arch.em.test.common.*;
import uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccEntryPoint.ECCMonitorEntryPointTest;
import uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccMonitor.thruPut.*;

import java.util.*;




public class ECCThroughputTestExecutor extends ECCBaseTestExecutor
                                       implements Runnable,
                                                  IEMTest_Listener
{
  private final int[] STREAM_SIZES   = { 128, 512, 1024, 2048, 4096, 8192, 16384 };  
  private final int[] STREAM_PUSHSES = { 10, 100, 1000, 2000 };
  private final ThruPutByteStore byteStore;
  
  private HashMap<Integer, Integer> streamErrorCounts;
  private long                      bytesRemaining;
  
  private IEMTest            providerTestFace;
  private OrderedThruExeNode senderNode;
  
  
  public ECCThroughputTestExecutor( TestEventListener listener,
                                    AMQPBasicChannel provider,
                                    AMQPBasicChannel user )
  {
    super( listener, provider, user );
    
    byteStore         = new ThruPutByteStore( STREAM_SIZES, STREAM_PUSHSES );
    streamErrorCounts = new HashMap<Integer, Integer>();
  }
  
  // ECCBaseTestExecutor -------------------------------------------------------
  @Override
  public boolean getTestResult()
  { return streamErrorCounts.isEmpty(); }
  
  // IEMTest_Listener ----------------------------------------------------------
  @Override
  public void onReceivedData( UUID senderID, int byteCount, byte[] dataBody )
  {
    if ( byteStore.validateByteData( byteCount, dataBody ) )
      bytesRemaining -= byteCount; // Data checked out OK
    else
    // Data was erroneous, note it
    {
      Integer count = streamErrorCounts.get( byteCount );
        if ( count != null )
          count++;
        else
          streamErrorCounts.put( count, 1 );
    }
    
    // Are we all done? Then notify
    if ( bytesRemaining == 0 ) notifyTestEnds( "ECC Monitoring Throughput Execution" );
  }
  
  // Runnable ------------------------------------------------------------------
  @Override
  public void run()
  {
    // Create default pumps & dispatches
    boolean pumpsOK = false;
    try
    { 
      initialiseDispatches( IAMQPMessageDispatchPump.ePumpPriority.MINIMUM );
      pumpsOK = true;
    }
    catch ( Exception e )
    { exeLogger.error( "Test initialisation problem: " + e.getMessage() ); }
    
    // Set up test interfaces
    if ( pumpsOK )
    {
      // Provider will receive data
      providerTestFace = providerFactory.createTest( ECCBaseTest.EMProviderUUID,
                                                     ECCBaseTest.EMUserUUID,
                                                     providerDispatch );
      
      providerTestFace.setListener( this );
      
      // Create (user based) sender node
      senderNode = new OrderedThruExeNode( ECCBaseTest.EMUserUUID,
                                           byteStore,
                                           userChannel,
                                           userFactory );
      
      // Prepare test stats
      bytesRemaining = 0;
      for ( Integer streamSize : STREAM_SIZES )
        for ( int pushRun : STREAM_PUSHSES  )
          bytesRemaining += (streamSize * pushRun);
      
      exeLogger.info( "Expectring to send: " + bytesRemaining + " bytes" );
      
      // Run test
      senderNode.sendData();
    }
  }
}
