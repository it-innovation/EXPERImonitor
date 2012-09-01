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
//      Created Date :          11-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.*;

import java.util.*;
import java.util.Map.*;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;




public class AMQPMessageDispatch implements IAMQPMessageDispatch
{
  private final Logger dispatchLogger = Logger.getLogger( AMQPMessageDispatch.class );  
  private final Object dispatchLock = new Object();
  
  private AMQPMessageDispatchPump                   dispatchPump;
  private LinkedBlockingQueue<Entry<String,byte[]>> dispatchQueue;
  private IAMQPMessageDispatchListener              dispatchListener;
  
  
  public AMQPMessageDispatch()
  {
    dispatchQueue = new LinkedBlockingQueue<Entry<String,byte[]>>();
  }
  
  // Protected methods ---------------------------------------------------------
  protected void setPump( AMQPMessageDispatchPump pump )
  { dispatchPump = pump; }
  
  protected boolean addMessage( String queueName, byte[] data )
  {
    boolean addResult = false;
    
    if ( queueName != null && data != null )
    {
      synchronized( dispatchLock )
      { 
        try
        { 
          dispatchQueue.put( new HashMap.SimpleEntry<String, byte[]>( queueName, data ) );
          addResult = true;
        }
        catch ( InterruptedException ie ) 
        { dispatchLogger.error( "Could not add AMQP message"); }
          
        dispatchPump.notifyDispatchWaiting();
      }
    }
    
    return addResult;
  }
  
  protected boolean iterateDispatch()
  {
    boolean hasDispatches;
    
    synchronized( dispatchLock )
    {
      if ( !dispatchQueue.isEmpty() && dispatchListener != null )
      {
        try
        { 
          Entry<String,byte[]> nextMessage = dispatchQueue.take();
          
          dispatchListener.onSimpleMessageDispatched( nextMessage.getKey(),
                                                      nextMessage.getValue() );
        }
        catch ( InterruptedException ie )
        { dispatchLogger.error( "Could not dispatch AMQP message"); }
      }
      
      hasDispatches = !dispatchQueue.isEmpty();
    }
    
    return hasDispatches;
  }
  
  // IAMQPMessageDispatch ------------------------------------------------------
  @Override
  public void setListener( IAMQPMessageDispatchListener listener )
  { dispatchListener = listener; }
  
  @Override
  public IAMQPMessageDispatchListener getListener()
  { return dispatchListener; }
}
