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
//      Created Date :          11-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.eccInterface;

import java.util.*;
import java.util.Map.*;




public class ECCInterfaceMessageDispatch implements Runnable
{
  private final Object dispatchLock = new Object();
  
  private Thread                           dispatchThread;
  private LinkedList<Entry<String,byte[]>> dispatchList;
  private boolean                          dispatchRunning;
  private IMessageDispatchListener         dispatchListener;
  
  
  public ECCInterfaceMessageDispatch()
  {
    dispatchList = new LinkedList<Entry<String,byte[]>>();
    dispatchThread = new Thread( this );
    dispatchThread.setPriority( Thread.MIN_PRIORITY );
  }
  
  public boolean start( IMessageDispatchListener listener )
  {
    if ( listener == null ) return false;
    
    dispatchListener = listener;
    dispatchRunning = true;
    dispatchThread.start();
    
    return true;
  }
  
  public void stop()
  {
    synchronized (dispatchLock)
    { dispatchRunning = false; }
  }
  
  public boolean isRunning()
  { return dispatchRunning; }
  
  public boolean addMessage( String queueName, byte[] data )
  {
    boolean addResult = false;
    
    if ( queueName != null && data != null )
    {
      synchronized (dispatchLock)
      {
        if ( dispatchRunning )
        {
          dispatchList.addLast(
            new HashMap.SimpleEntry<String, byte[]>( queueName, data ) ); 
          
          addResult = true;
        }
      }
    }
    
    return addResult;
  }
  
  // Runnable ------------------------------------------------------------------
  @Override
  public void run()
  {
    while ( dispatchRunning )
    {
      Entry<String, byte[]> amqpMessage = null;
      
      synchronized (dispatchLock)
      {
        if ( !dispatchList.isEmpty() )
        {
          amqpMessage = dispatchList.get( 0 );
          dispatchList.remove(0);
        }
      }
      
      // Dispatch next message
      if ( amqpMessage != null )
        dispatchListener.onSimpleMessageDispatched( amqpMessage.getKey(),
                                                    amqpMessage.getValue() );
    }
  }
}
