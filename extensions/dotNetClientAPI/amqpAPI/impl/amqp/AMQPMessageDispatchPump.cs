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
//      Created Date :          08-Apr-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec;

using System;
using System.Threading;
using System.Collections.Generic;






namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp
{

public class AMQPMessageDispatchPump : IAMQPMessageDispatchPump
                                                
{
  private readonly IECCLogger pumpLogger  = Logger.getLogger( typeof(AMQPMessageDispatchPump) );
  private readonly Object     pumpingLock = new Object();
  private readonly Object     listLock    = new Object();
  private readonly Object     waitingLock = new Object();

  private string        pumpName;
  private Thread        pumpThread;
  private ePumpPriority pumpPriority;
  
  private bool isDispatchPumping = false;
  private bool dispatchesWaiting = false;
  
  private LinkedList<IAMQPMessageDispatch> dispatchList;
  
 
  public AMQPMessageDispatchPump( String pName, ePumpPriority priority )
  {
    pumpName     = pName;
    pumpPriority = priority;    
    dispatchList = new LinkedList<IAMQPMessageDispatch>();
  }
  
  // IAMQPMessageDispatchPump --------------------------------------------------
  public bool startPump()
  {
    if ( !isDispatchPumping )
    {
      startPumpThread();
      return true;
    }
    
    return false;
  }
  
  public void stopPump()
  {
    lock ( pumpingLock )
    { 
        isDispatchPumping = false;
        Monitor.Pulse(pumpingLock);
    }
  }
  
  public void emptyPump()
  {
    lock ( listLock )
    { dispatchList.Clear(); } 
  }
  
  public bool isPumping()
  {
    bool result = false;
    
    lock ( pumpingLock )
    { result = isDispatchPumping; }
    
    return result;
  }
  
  public void addDispatch( IAMQPMessageDispatch dispatch )
  {
    AMQPMessageDispatch amqpDisp = (AMQPMessageDispatch) dispatch;
    
    if ( amqpDisp != null )
    {
      amqpDisp.setPump( this );
      
      lock ( listLock )
      { dispatchList.AddLast( amqpDisp ); }
    }  
  }
  
  public void removeDispatch( IAMQPMessageDispatch dispatch )
  {
    if ( dispatch != null )
      lock( listLock )
      { dispatchList.Remove(dispatch); }
  }
  
  // Runnable ------------------------------------------------------------------
  public void run()
  {
      while ( isDispatchPumping )
      {
          // If we don't have any dispatches waiting, cool it for a bit
          lock (waitingLock)
          {
              try
              { if (!dispatchesWaiting) Monitor.Wait(waitingLock); } // Wait for new dispatches
              catch (SynchronizationLockException) { }
          }

          // Make a safe copy of the current list for processing (this may change at run-time)
          LinkedList<AMQPMessageDispatch> currentDispatches = new LinkedList<AMQPMessageDispatch>();
          
          lock ( listLock )
          {
              foreach ( AMQPMessageDispatch disp in dispatchList )
                    currentDispatches.AddLast( disp );
          }
          
          // Run through all dispatchers, iterating one dispatch
          foreach ( AMQPMessageDispatch disp in currentDispatches )
              disp.iterateDispatch();

          // Check for outstanding dispatches
          dispatchesWaiting = false;
          
          foreach ( AMQPMessageDispatch disp in currentDispatches )
          {
              if ( disp.hasOutstandingDispatches() )
              {
                  dispatchesWaiting = true;
                  break;
              }
          }
      }
      
      pumpLogger.info( "Pump: " + pumpName + " has stopped" );
  }
  
  public void notifyDispatchWaiting()
  {
      lock (waitingLock) 
      { 
          dispatchesWaiting = true;
          Monitor.Pulse(waitingLock);
      } 
  }
  
  // Private methods -----------------------------------------------------------
  private void startPumpThread()
  {
      lock ( pumpingLock )
      {
          //pumpThread = new Thread( this, "DispatchPump (" + pumpName + ")" );
          pumpThread = new Thread( this.run );
          pumpThread.Name = "DispatchPump (" + pumpName + ")";
          
          
          switch ( pumpPriority )
          {
              case ePumpPriority.HIGH    : pumpThread.Priority = ThreadPriority.AboveNormal; break;
              
              case ePumpPriority.NORMAL  : pumpThread.Priority = ThreadPriority.Normal; break;
              
              case ePumpPriority.MINIMUM :
              default                    : pumpThread.Priority = ThreadPriority.BelowNormal; break;
          }

          isDispatchPumping = true;
      }
      
      pumpThread.Start();
      
      pumpLogger.info( "Pump: " + pumpName + " has started" );
  }
}

} // namespace
