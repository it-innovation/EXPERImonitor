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

#pragma once

#include "IAMQPMessageDispatch.h"
#include "IAMQPMessageDispatchListener.h"

#include "AMQPMessageDispatchPump.h"

#include <boost/thread/mutex.hpp>

#include <queue>


namespace ecc_amqpAPI_impl
{

  class AMQPMessageDispatch : public ecc_amqpAPI_spec::IAMQPMessageDispatch
  {
  public:

    typedef boost::shared_ptr<AMQPMessageDispatch> ptr_t;

    AMQPMessageDispatch();

    virtual ~AMQPMessageDispatch();

    bool addMessage( const std::string& queueName, const std::string& msg );

    bool hasOutstandingDispatches();

    void iterateDispatch();

    void setPump( AMQPMessageDispatchPump::ptr_t pump );

    // IAMQPMessageDispatch ------------------------------------------------------
    void setListener( ecc_amqpAPI_spec::IAMQPMessageDispatchListener::ptr_t listener );

    ecc_amqpAPI_spec::IAMQPMessageDispatchListener::ptr_t getListener();

  private:

    class QueueMsg
    {
    public:
      QueueMsg()
      : queueName(""), queueMsg("")
      {}

      QueueMsg( const std::string& qName, const std::string& msg )
        : queueName(qName), queueMsg(msg)
      { }

      QueueMsg& operator= ( const QueueMsg& rhs )
      {
        queueName = rhs.queueName;
        queueMsg  = rhs.queueMsg;

        return *this;
      }

      const std::string& getQueueName()
      { return queueName; }

      const std::string& getQueueMsg()
      { return queueMsg; }

      virtual ~QueueMsg() {}

    private:
      std::string queueName;
      std::string queueMsg;
    };

    //readonly IECCLogger dispatchLogger = Logger.getLogger( typeof(AMQPMessageDispatch) ); 

    AMQPMessageDispatchPump::ptr_t                        dispatchPump;
    boost::mutex                                          dispatchMutex;
    std::queue<QueueMsg>                                  dispatchQueue;
    ecc_amqpAPI_spec::IAMQPMessageDispatchListener::ptr_t dispatchListener;

  };

}