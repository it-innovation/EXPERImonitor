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

  AMQPMessageDispatch::AMQPMessageDispatch()
  {
  }

  AMQPMessageDispatch::~AMQPMessageDispatch()
  {
    dispatchPump = NULL;
  }

  bool AMQPMessageDispatch::addMessage( const String& queueName, const Byte* data )
  {
    bool addResult = false;

    if ( !queueName.empty() && data != NULL )
    {
      lock_guard<mutex> lock( dispatchMutex );

      QueueMsg msg( queueName, data );

      dispatchQueue.push( msg );
      dispatchPump->notifyDispatchWaiting();
      addResult = true;
    }
    
    return addResult;
  }

  bool AMQPMessageDispatch::hasOutstandingDispatches()
  {
      bool outstanding;

      {
        lock_guard<mutex> lock( dispatchMutex );

        outstanding = !dispatchQueue.empty();
      }

      return outstanding;
  }

  void AMQPMessageDispatch::iterateDispatch()
  {
    QueueMsg nextMessage;
    bool gotMessage = false;

    {
      lock_guard<mutex> lock( dispatchMutex );

      if ( !dispatchQueue.empty() )
      {
        nextMessage = dispatchQueue.front();
        dispatchQueue.pop();
        gotMessage = true;
      }
        
      if ( gotMessage && dispatchListener )
        dispatchListener->onSimpleMessageDispatched( nextMessage.queueName,
                                                     nextMessage.data );

    }
  }

  void AMQPMessageDispatch::setPump( AMQPMessageDispatchPump::ptr_t pump )
  { dispatchPump = pump; }
  
  // IAMQPMessageDispatch ------------------------------------------------------
  void AMQPMessageDispatch::setListener( IAMQPMessageDispatchListener::ptr_t listener )
  { dispatchListener = listener; }
  
  IAMQPMessageDispatchListener::ptr_t AMQPMessageDispatch::getListener()
  { return dispatchListener; }
  
}