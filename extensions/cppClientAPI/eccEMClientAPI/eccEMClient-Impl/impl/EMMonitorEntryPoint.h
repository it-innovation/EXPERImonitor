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
//      Created Date :          04-Jul-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#pragma once

#include "IEMMonitorEntryPoint.h"
#include "EMBaseInterface.h"




namespace ecc_emClient_impl
{

class EMMonitorEntryPoint : public EMBaseInterface,
                            public ecc_emClient_spec::IEMMonitorEntryPoint
{
public:

  typedef boost::shared_ptr<EMMonitorEntryPoint> ptr_t;
  
  EMMonitorEntryPoint( ecc_amqpAPI_impl::AMQPBasicSubscriptionService::ptr_t sService,
                       ecc_amqpAPI_impl::AMQPBasicChannel::ptr_t             inChannel,
                       ecc_amqpAPI_impl::AMQPBasicChannel::ptr_t             outChannel,
                       ecc_amqpAPI_impl::AMQPMessageDispatch::ptr_t          dispatch,
                       const UUID&                                           providerID,
                       bool                                                  isProvider );

  virtual ~EMMonitorEntryPoint();
  
  virtual void shutdown();

  // IECCMonitorEntryPoint -----------------------------------------------------
  // Method ID = 1
  virtual void registerAsEMClient( const UUID& userID, const String& userName );

  // Protected methods ---------------------------------------------------------
protected:
  virtual void onInterpretMessage( const int& methodID, const JSONTree& jsonTree );
};

} // namespace