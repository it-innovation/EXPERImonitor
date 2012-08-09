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





public class AMQPMessageDispatchPump implements Runnable,
                                                IAMQPMessageDispatchPump
                                                
{
  private final Object pumpLock = new Object();
  
  private String  pumpName;
  private Thread  pumpThread;
  private boolean isPumping = false;
  
  private LinkedList<AMQPMessageDispatch> dispatchList;
  
  
  
  
  public AMQPMessageDispatchPump( String pName, 
                                  IAMQPMessageDispatchPump.ePumpPriority priority )
  {
    pumpName = pName;
    pumpThread = new Thread( this );
    
    switch ( priority )
    {
      case HIGH    : pumpThread.setPriority( Thread.MIN_PRIORITY ); break;
      
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
    if ( !dispatchList.isEmpty() && !isPumping )
    {
      isPumping = true;
      pumpThread.start();
      
      return true;
    }
    
    return false;
  }
  
  @Override
  public void stopPump()
  {
    synchronized( pumpLock ) 
    { isPumping = false; }
  }
  
  @Override
  public boolean isPumping()
  { return isPumping; }
  
  @Override
  public void addDispatch( IAMQPMessageDispatch dispatch )
  {
    AMQPMessageDispatch amqpDisp = (AMQPMessageDispatch) dispatch;
    
    if ( amqpDisp != null )
      synchronized (pumpLock)
        { dispatchList.addLast( amqpDisp ); }
  }
  
  // Runnable ------------------------------------------------------------------
  @Override
  public void run()
  {
    while ( isPumping )
    {
      // Make a safe copy of the current list for processing (this may change at run-time)
      LinkedList<AMQPMessageDispatch> currentDispatches = new LinkedList<AMQPMessageDispatch>();
      
      synchronized (pumpLock)
      { currentDispatches.addAll( dispatchList ); }
      
      // Run through all dispatchers, iterating one dispatch
      Iterator<AMQPMessageDispatch> dispIt = currentDispatches.iterator();
      while ( dispIt.hasNext() )
      { dispIt.next().iterateDispatch(); }
    }
  }
}
