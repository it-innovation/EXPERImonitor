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

#include "AMQPBasicSubscriptionService.h"

#include "SimpleAmqpClient\AmqpException.h"

using namespace AmqpClient;

using namespace std;
using namespace boost;
using namespace boost::uuids;





namespace ecc_amqpAPI_impl
{

  AMQPBasicSubscriptionService::AMQPBasicSubscriptionService()
  : serviceRunning( false ), pollingInterval( MIN_POLL_INTERVAL )
  {
  }

  AMQPBasicSubscriptionService::~AMQPBasicSubscriptionService()
  {
  }

  void AMQPBasicSubscriptionService::startService( int pollInterval )
  {
    if ( !serviceRunning && !serviceThread )
    {
      if ( pollInterval >= MIN_POLL_INTERVAL ) pollingInterval = posix_time::milliseconds(pollInterval);
      
      serviceThread = Thread_ptr( new thread( &AMQPBasicSubscriptionService::run, this ) );

      serviceRunning = true;
    }
  }

  void AMQPBasicSubscriptionService::stopService()
  {
    serviceRunning = false;
  }

  bool AMQPBasicSubscriptionService::subscribe( AMQPBasicSubscriptionProcessor::ptr_t processor )
  {
    if ( processor )
    {
      lock_guard<mutex> lock( subscriberMutex );

      // Only add if we don't already have the processor
      uuid procID = processor->getProcessorID();

      if ( subscribers.find( procID ) == subscribers.end() )
        subscribers.insert( make_pair<uuid, AMQPBasicSubscriptionProcessor::ptr_t>(procID, processor) );
    }

    return false;
  }

  bool AMQPBasicSubscriptionService::unsubscribe( AMQPBasicSubscriptionProcessor::ptr_t processor )
  {
    if ( processor )
    {
      lock_guard<mutex> lock( subscriberMutex );

      // Only remove if we already have the processor
      uuid procID = processor->getProcessorID();
      SubscriberMap::iterator subIt = subscribers.find( procID );
      
      if ( subIt != subscribers.end() ) subscribers.erase( subIt );
    }

    return false;
  }

  // Private methods -----------------------------------------------------------
  void AMQPBasicSubscriptionService::run()
  {
    while( serviceRunning )
    {
      // Create a copy of the current subscribers for polling
      SubscriberMap currSubscribers;
      {
        lock_guard<mutex> lock( subscriberMutex );
        currSubscribers.insert( subscribers.begin(), subscribers.end() );
      }

      // If we have subscribers, run through checking their queues for
      // new messages
      if ( !currSubscribers.empty() )
      {
        SubscriberMap::iterator subIt = currSubscribers.begin();
        while ( subIt != currSubscribers.end() )
        {
          AMQPBasicSubscriptionProcessor::ptr_t processor = subIt->second;

          Channel::ptr_t  channel = processor->getAMQPChannel();
          string          queue   = toNarrow( processor->getAMQPQueueName() );
          Envelope::ptr_t envTarget;

          // If a waiting message has been collected, notify onwards (subscriber acknowledges)
          try
          {
            if ( channel->BasicGet( envTarget, queue, false ) )
              if ( envTarget )
                processor->handleBasicMessage( envTarget );
          }
          catch ( AmqpException* ae )
          {
            std::string error( "Had problems getting AMQP Message: " );
            
            if ( ae )
              error.append( ae->reply_text() );
            else
              error.append( "Could not get futher error data." );
            
            cout << error << endl;
          }

          ++subIt;
        }
      }

      // Wait a fixed interval between the polling process
      const system_time nextTimeOut = get_system_time() + pollingInterval;
      {
        unique_lock<mutex> lock( pollingMutex );
        serviceIntervalCondition.timed_wait( lock, nextTimeOut );
      }
    }

    serviceThread = NULL;
  }

}