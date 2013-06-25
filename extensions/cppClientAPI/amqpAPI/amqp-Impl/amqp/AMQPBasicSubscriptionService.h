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
//      Created Date :          17-June-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#pragma once

#include "AMQPBasicSubscriptionProcessor.h"

#include <hash_map>
#include <boost/uuid/uuid.hpp>



namespace ecc_amqpAPI_impl
{
  // Forward declarations -------------
  class AMQPBasicSubscriptionProcessor;
  // ----------------------------------

  class AMQPBasicSubscriptionService
  {
  public:

    typedef boost::shared_ptr<AMQPBasicSubscriptionService> ptr_t;

    AMQPBasicSubscriptionService();

    virtual ~AMQPBasicSubscriptionService();

    void startService( int pollInterval );

    void stopService();

    bool subscribe( boost::shared_ptr<AMQPBasicSubscriptionProcessor> processor );

    bool unsubscribe( boost::shared_ptr<AMQPBasicSubscriptionProcessor> processor );

  private:

    void run();

    const static int MIN_POLL_INTERVAL = 100; // Milliseconds

    typedef std::hash_map<boost::uuids::uuid, boost::shared_ptr<AMQPBasicSubscriptionProcessor>> SubscriberMap;
    typedef boost::shared_ptr<boost::thread>                                                     thread_ptr;

    thread_ptr                      serviceThread;
    boost::condition_variable       serviceIntervalCondition;
    boost::posix_time::milliseconds pollingInterval;
    volatile bool                   serviceRunning;
    boost::mutex                    subscriberMutex;
    boost::mutex                    pollingMutex;
    SubscriberMap                   subscribers;
  };
}