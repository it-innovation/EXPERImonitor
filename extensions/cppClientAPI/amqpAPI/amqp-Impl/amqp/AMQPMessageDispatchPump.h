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

#include "IAMQPMessageDispatchPump.h"
#include "IAMQPMessageDispatch.h"

#include <boost/thread.hpp>
#include <boost/thread/recursive_mutex.hpp>

#include <list>




namespace ecc_amqpAPI_impl
{

  class AMQPMessageDispatchPump : public boost::enable_shared_from_this<AMQPMessageDispatchPump>,
                                  public ecc_amqpAPI_spec::IAMQPMessageDispatchPump
  {
  public:

    typedef boost::shared_ptr<AMQPMessageDispatchPump> ptr_t;
    
    AMQPMessageDispatchPump( const String& pName, 
                             const ecc_amqpAPI_spec::ePumpPriority& priority );

    virtual ~AMQPMessageDispatchPump();
    
    void notifyDispatchWaiting();

    // IAMQPMessageDispatchPump --------------------------------------------------
    virtual bool startPump();
      
    virtual void stopPump();
    
    virtual void emptyPump();
    
    virtual bool isPumping();
    
    virtual void addDispatch( ecc_amqpAPI_spec::IAMQPMessageDispatch::ptr_t dispatch );
    
    virtual void removeDispatch( ecc_amqpAPI_spec::IAMQPMessageDispatch::ptr_t dispatch );
    

  private:
    
    void startPumpThread();

    void run();

    //IECCLogger pumpLogger  = Logger.getLogger( typeof(AMQPMessageDispatchPump) );
    
    typedef boost::shared_ptr<boost::thread> thread_ptr;

    boost::mutex              pumpingMutex;
    boost::mutex              listMutex;
    boost::mutex              waitingMutex;
    boost::condition_variable waitingMonitor;

    thread_ptr                      pumpThread;
    std::wstring                    pumpName;     // Platform specific code required
    ecc_amqpAPI_spec::ePumpPriority pumpPriority; // Platform specific code required
  
    bool isDispatchPumping;
    bool dispatchesWaiting;
  
    std::list<ecc_amqpAPI_spec::IAMQPMessageDispatch::ptr_t> dispatchList;
  };
}