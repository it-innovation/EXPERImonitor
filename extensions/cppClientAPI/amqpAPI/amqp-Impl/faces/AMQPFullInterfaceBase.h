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
//      Created Date :          15-May-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#pragma once

#include "AbstractAMQPInterface.h"




namespace ecc_amqpAPI_impl
{

class AMQPFullInterfaceBase : public AbstractAMQPInterface
{
public:

  typedef boost::shared_ptr<AMQPFullInterfaceBase> ptr_t;

  AMQPFullInterfaceBase( AMQPBasicSubscriptionService::ptr_t sService,
                         AMQPBasicChannel::ptr_t             inChannel,
                         AMQPBasicChannel::ptr_t             outChannel );

  virtual ~AMQPFullInterfaceBase();

  bool initialise( const String& iName,
                    const UUID&   providerID,
                    const UUID&   userID,
                    const bool    asProvider );

private:
  bool setInitParams( const String& iName,
                      const UUID&   providerID,
                      const UUID&   userID,
                      const bool    asProvider );
};
  
} // namespace
