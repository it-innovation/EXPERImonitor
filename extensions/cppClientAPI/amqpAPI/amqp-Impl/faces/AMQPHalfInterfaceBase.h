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

#include <boost/uuid/uuid.hpp>


namespace ecc_amqpAPI_impl
{

class AMQPHalfInterfaceBase : public AbstractAMQPInterface
{
public:

  typedef boost::shared_ptr<AMQPHalfInterfaceBase> ptr_t;
  
  AMQPHalfInterfaceBase( AMQPBasicSubscriptionService::ptr_t sService,
                         AMQPBasicChannel::ptr_t             inChannel,
                         AMQPBasicChannel::ptr_t             outChannel );

  virtual ~AMQPHalfInterfaceBase();

  bool initialise( const String& iName,
                   const UUID&   targetID, 
                   const bool    asProvider );

protected:
  
  virtual void createInterfaceExchangeNames( const String& iName );

  virtual void assignBindings();

private:

  bool setInitParams( const String& iName, const UUID& targetID, const bool asProvider );

};

} // namespace
