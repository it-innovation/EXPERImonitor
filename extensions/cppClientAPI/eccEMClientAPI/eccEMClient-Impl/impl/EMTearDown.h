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

#include "IEMTearDown.h"
#include "EMBaseInterface.h"




namespace ecc_emClient_impl
{

class EMTearDown : public EMBaseInterface,
                   public ecc_emClient_spec::IEMTearDown
{
public:

  typedef boost::shared_ptr<EMTearDown> ptr_t;

  EMTearDown( ecc_amqpAPI_impl::AMQPBasicSubscriptionService::ptr_t sService,
              ecc_amqpAPI_impl::AMQPBasicChannel::ptr_t             channel,
              ecc_amqpAPI_impl::AMQPMessageDispatch::ptr_t          dispatch,
              const UUID&                                           providerID,
              const UUID&                                           userID,
              const bool                                            isProvider );

  virtual ~EMTearDown();
  
  // IECCTearDown --------------------------------------------------------------
  virtual void setUserListener( ecc_emClient_spec::IEMTearDown_UserListener::ptr_t listener );
  
  // User methods --------------------------------------------------------------
  // Method ID = 3
  virtual void notifyReadyToTearDown();
  
  // Method ID = 4
  virtual void sendTearDownResult( const bool success );
  
  // Protected methods ---------------------------------------------------------
protected:
  
  virtual void onInterpretMessage( const int& methodID, const JSONTree& jsonTree );

private:
  
  ecc_emClient_spec::IEMTearDown_UserListener::ptr_t userListener;
};

} // namespace
