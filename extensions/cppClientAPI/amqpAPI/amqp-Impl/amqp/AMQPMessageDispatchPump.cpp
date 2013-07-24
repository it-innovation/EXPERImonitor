/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
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
//      Created Date :          15-May-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#include "stdafx.h"

#include "AMQPMessageDispatch.h"

#include <boost/thread/lock_guard.hpp>

using namespace ecc_amqpAPI_spec;
using namespace std;
using namespace boost;




namespace ecc_amqpAPI_impl
{
  AMQPMessageDispatchPump::AMQPMessageDispatchPump( const String& pName, const ePumpPriority& priority )
    : isDispatchPumping(false), dispatchesWaiting(false)
  {
    pumpName     = pName;
    pumpPriority = priority;
  }

  AMQPMessageDispatchPump::~AMQPMessageDispatchPump()
  {
  }
  
  // IAMQPMessageDispatchPump --------------------------------------------------
  bool AMQPMessageDispatchPump::startPump()
  {
    if ( !isDispatchPumping )
    {
      startPumpThread();
      return true;
    }
    
    return false;
  }
  
  void AMQPMessageDispatchPump::stopPump()
  {
    {
      lock_guard<mutex> lock( pumpingMutex );
      pumpThread = NULL;
      isDispatchPumping = false; 
    }

    {
      lock_guard<mutex> lock( waitingMutex );
      waitingMonitor.notify_one();
    }
  }
  
  void AMQPMessageDispatchPump::emptyPump()
  {
    lock_guard<mutex> lock( listMutex );
    dispatchList.clear();
  }
  
  bool AMQPMessageDispatchPump::isPumping()
  {
    bool result = false;
    {
      lock_guard<mutex> lock( pumpingMutex );
      result = isDispatchPumping; 
    }
    
    return result;
  }
  
  void AMQPMessageDispatchPump::addDispatch( IAMQPMessageDispatch::ptr_t dispatch )
  {
    AMQPMessageDispatch::ptr_t amqpDisp = dynamic_pointer_cast<AMQPMessageDispatch>( dispatch );
    
    if ( amqpDisp )
    {
      amqpDisp->setPump( shared_from_this() );
      
      {
        lock_guard<mutex> lock( listMutex );
        dispatchList.push_back( dispatch ); 
      }
    }  
  }
  
  void AMQPMessageDispatchPump::removeDispatch( IAMQPMessageDispatch::ptr_t dispatch )
  {
    if ( dispatch )
    {
      lock_guard<mutex> lock( listMutex );
      dispatchList.remove( dispatch );
    }
  }
  
  void AMQPMessageDispatchPump::notifyDispatchWaiting()
  {
      lock_guard<mutex> lock( waitingMutex );
      
      dispatchesWaiting = true;
      
      waitingMonitor.notify_one();
  }
  
  // Private methods -----------------------------------------------------------
  void AMQPMessageDispatchPump::startPumpThread()
  {
    if ( !isDispatchPumping )
    {
      lock_guard<mutex> lock( pumpingMutex );

      // Thread naming and prioritisation is not possible at Boost level; platform
      // specific code needs to be injected here at some point

      pumpThread = thread_ptr( new thread( &AMQPMessageDispatchPump::run, 
                               shared_from_this() ) );

      isDispatchPumping = true;
    }
            
    //pumpLogger.info( "Pump: " + pumpName + " has started" );
  }

  // Runnable ------------------------------------------------------------------
  void AMQPMessageDispatchPump::run()
  {
    while ( isDispatchPumping )
    {
      // If we don't have any dispatches waiting, cool it for a bit
      {
        unique_lock<mutex> lock( waitingMutex );

        if ( !dispatchesWaiting ) waitingMonitor.wait( lock ); // Wait for new dispatches
      }

      // Make a safe copy of the current list for processing (this may change at run-time)
      std::list<AMQPMessageDispatch::ptr_t> currentDispatches;    
      {
        lock_guard<mutex> lock( listMutex );
          
        list<IAMQPMessageDispatch::ptr_t>::iterator dispIt = dispatchList.begin();
        while ( dispIt != dispatchList.end() )
        {
          AMQPMessageDispatch::ptr_t dispatch = dynamic_pointer_cast<AMQPMessageDispatch>( *dispIt );
          currentDispatches.push_back( dispatch );
          ++dispIt;
        }
      }
          
      // Run through all dispatchers, iterating one dispatch
      list<AMQPMessageDispatch::ptr_t>::iterator dispIt = currentDispatches.begin();
      while ( dispIt != currentDispatches.end() )
      {
        AMQPMessageDispatch::ptr_t dispatch = *dispIt;
        dispatch->iterateDispatch();
        ++dispIt;
      }

      // Check for outstanding dispatches
      dispatchesWaiting = false;
      
      dispIt = currentDispatches.begin();
      while ( dispIt != currentDispatches.end() )
      {
        AMQPMessageDispatch::ptr_t dispatch = *dispIt;

        if ( dispatch->hasOutstandingDispatches() )
        {
          dispatchesWaiting = true;
          break;
        }

        ++dispIt;
      }
    }
    
    //pumpLogger.info( "Pump: " + pumpName + " has stopped" );
  }

}