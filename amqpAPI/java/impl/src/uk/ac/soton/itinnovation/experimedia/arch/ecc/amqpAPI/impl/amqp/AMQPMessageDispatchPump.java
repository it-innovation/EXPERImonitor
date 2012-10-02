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
//      Created Date :          09-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.*;

import java.util.*;
import org.apache.log4j.Logger;





public class AMQPMessageDispatchPump implements Runnable,
                                                IAMQPMessageDispatchPump
                                                
{
  private final Logger pumpLogger  = Logger.getLogger( AMQPMessageDispatchPump.class );
  private final Object listLock    = new Object();
  private final Object waitingLock = new Object();
  
  private String  pumpName;
  private Thread  pumpThread;
  private boolean isPumping         = false;
  private boolean dispatchesWaiting = false;
  
  private LinkedList<AMQPMessageDispatch> dispatchList;
  
 
  public AMQPMessageDispatchPump( String pName, 
                                  IAMQPMessageDispatchPump.ePumpPriority priority )
  {
    pumpName = pName;
    pumpThread = new Thread( this, "DispatchPump (" + pumpName + ")" );
    
    switch ( priority )
    {
      case HIGH    : pumpThread.setPriority( Thread.MAX_PRIORITY ); break;
      
      case NORMAL  : pumpThread.setPriority( Thread.NORM_PRIORITY ); break;
      
      case MINIMUM:
           default : pumpThread.setPriority( Thread.MIN_PRIORITY ); break;
    }
    
    dispatchList = new LinkedList<AMQPMessageDispatch>();
  }
  
  // IAMQPMessageDispatchPump --------------------------------------------------
  @Override
  public boolean startPump()
  {
    if ( !isPumping )
    {
      isPumping = true;
      pumpThread.start();
      
      return true;
    }
    
    return false;
  }
  
  @Override
  public synchronized void stopPump()
  { isPumping = false; }
  
  @Override
  public synchronized boolean isPumping()
  { return isPumping; }
  
  @Override
  public void addDispatch( IAMQPMessageDispatch dispatch )
  {
    AMQPMessageDispatch amqpDisp = (AMQPMessageDispatch) dispatch;
    
    if ( amqpDisp != null )
    {
      amqpDisp.setPump( this );
      
      synchronized ( listLock )
      { dispatchList.addLast( amqpDisp ); }
    }  
  }
  
  @Override
  public void removeDispatch( IAMQPMessageDispatch dispatch )
  {
    if ( dispatch != null )
      synchronized( listLock )
      { dispatchList.removeFirstOccurrence(dispatch); }
  }
  
  // Runnable ------------------------------------------------------------------
  @Override
  public void run()
  {
    while ( isPumping )
    {
      // If we don't have any dispatches waiting, cool it for a bit
      while ( !dispatchesWaiting )
        try { Thread.sleep(50); } catch (InterruptedException ie) { break; }
      
      // Make a safe copy of the current list for processing (this may change at run-time)
      LinkedList<AMQPMessageDispatch> currentDispatches = new LinkedList<AMQPMessageDispatch>();
      
      synchronized ( listLock )
      { currentDispatches.addAll( dispatchList ); }
      
      // Run through all dispatchers, iterating one dispatch
      Iterator<AMQPMessageDispatch> dispIt = currentDispatches.iterator();
      while ( dispIt.hasNext() )
      { dispIt.next().iterateDispatch(); }
      
      // Check for outstanding dispatches
      synchronized ( waitingLock )
      {
        dispatchesWaiting = false;
        dispIt = currentDispatches.iterator();
        while ( dispIt.hasNext() )
        {
          if ( dispIt.next().hasOutstandingDispatches() )
          {
            dispatchesWaiting = true;
            break;
          }
        }
      }
    }
  }
  
  // Protected methods ---------------------------------------------------------
  protected void notifyDispatchWaiting()
  { 
    synchronized ( waitingLock ) 
    { dispatchesWaiting = true; } 
  }
}
