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

#include "IAMQPMessageDispatchListener.h"

#include "AMQPBasicSubscriptionService.h"
#include "AMQPBasicChannel.h"
#include "AbstractAMQPInterface.h"
#include "ModelBase.h"

#include <boost/property_tree/json_parser.hpp>




namespace ecc_emClient_impl
{

class EMBaseInterface : public ecc_amqpAPI_spec::IAMQPMessageDispatchListener
{
public:

  typedef boost::shared_ptr<EMBaseInterface> ptr_t;

  EMBaseInterface( ecc_amqpAPI_impl::AMQPBasicSubscriptionService::ptr_t service,
                   ecc_amqpAPI_impl::AMQPBasicChannel::ptr_t             channel, 
                   const bool                                            asProvider );

  virtual ~EMBaseInterface();
  
  void shutdown();
  
  // IAMQPMessageDispatchListener ----------------------------------------------
  virtual void onSimpleMessageDispatched( const String& queueName, const Byte* data );
  
  // Protected methods ---------------------------------------------------------
protected:

  typedef boost::property_tree::ptree JSONTree;

  void initialiseAMQP( ecc_amqpAPI_impl::AbstractAMQPInterface::ptr_t eccIFace,
                       ecc_amqpAPI_impl::AMQPMessageDispatch::ptr_t   msgDispatch );
  
  bool executeMethod( const int methodID, 
                      const boost::container::list<ecc_commonDataModel::ModelBase::ptr_t>& parameters );
  
  // Derriving classes must implement ------------------------------------------------------
  virtual void onInterpretMessage( const int& methodID, const JSONTree& jsonTree ) =0;

  //protected static IECCLogger faceLogger = Logger.getLogger( typeof(EMBaseInterface) );

  String interfaceName;
  String interfaceVersion;
  bool   isProvider;
  
  ecc_amqpAPI_impl::AMQPBasicSubscriptionService::ptr_t amqpSubscriptService;
  ecc_amqpAPI_impl::AMQPBasicChannel::ptr_t             amqpChannel;
  ecc_amqpAPI_impl::AbstractAMQPInterface::ptr_t        amqpInterface;
  UUID                                                  interfaceUserID;
  UUID                                                  interfaceProviderID;
};

} // namespace