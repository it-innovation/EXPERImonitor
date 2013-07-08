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

#include "stdafx.h"

#include "EMBaseInterface.h"
#include "AMQPHalfInterfaceBase.h"
#include "AMQPFullInterfaceBase.h"

#include "ECCUtils.h"

using namespace ecc_amqpAPI_impl;
using namespace ecc_commonDataModel;

using namespace boost;
using namespace boost::container;
using namespace boost::property_tree;




namespace ecc_emClient_impl
{

EMBaseInterface::EMBaseInterface( AMQPBasicSubscriptionService::ptr_t sService,
                                  AMQPBasicChannel::ptr_t             channel, 
                                  const bool                          asProvider )
{
  amqpSubscriptService = sService;
  amqpChannel          = channel;
  isProvider           = asProvider;
}

EMBaseInterface::~EMBaseInterface()
{
    shutdown();
}
  
void EMBaseInterface::shutdown()
{
    if ( amqpInterface )
    {
        amqpInterface->shutdown();
        amqpInterface.reset();

        // Channel is managed elsewhere
    }
}
  
// IAMQPMessageDispatchListener ----------------------------------------------
void EMBaseInterface::onSimpleMessageDispatched( const String& queueName, const Byte* data )
{
  if ( !queueName.empty() && data != NULL )
  {
    std::string jsonData = fromByteArray( data );

    if ( !jsonData.empty() )
    {
      ptree              jsonTree;
      std::istringstream stream( jsonData );
      read_json( stream, jsonTree );

      int methodID = 0;

      onInterpretMessage( methodID, jsonTree );
    }
  }
}
  
// Protected methods ---------------------------------------------------------
void EMBaseInterface::initialiseAMQP( AbstractAMQPInterface::ptr_t eccIFace,
                                      AMQPMessageDispatch::ptr_t   msgDispatch )
{
  if ( eccIFace && msgDispatch )
  {
    amqpInterface = eccIFace;
      
    msgDispatch->setListener( IAMQPMessageDispatchListener::ptr_t(this) );
    amqpInterface->setMessageDispatch( msgDispatch );
   
    String faceName = interfaceName + L" " + interfaceVersion;
    
    AMQPHalfInterfaceBase::ptr_t halfFace = dynamic_pointer_cast<AMQPHalfInterfaceBase>(eccIFace);
    AMQPFullInterfaceBase::ptr_t fullFace = dynamic_pointer_cast<AMQPFullInterfaceBase>(eccIFace);

    if ( halfFace )
      halfFace->initialise( faceName, interfaceProviderID, isProvider );
    else 
      if ( fullFace )
        fullFace->initialise( faceName, interfaceProviderID, interfaceUserID, isProvider );
  }
}
  
bool EMBaseInterface::executeMethod( const int methodID, 
                                     const list<ModelBase::ptr_t>& params )
{
  bool result = false;
    
  if ( amqpInterface )
  {
    list<ModelBase::ptr_t> parameters;

    // Start JSON Array
    String payloadData = L"[" + intToString( methodID );

    if ( params.size() > 0 )
    {
      // Add parameters
      payloadData += L",";

      list<ModelBase::ptr_t>::const_iterator pIt = params.begin();
      while ( pIt != params.end() )
      {
        ModelBase::ptr_t model = *pIt;

        String serial = model->toJSON();

        if ( !serial.empty() )
        {
          payloadData += serial;
          payloadData += L",";
        }
        
        ++pIt;
      }
    }
    
    // End JSON Array
    payloadData += L"]";
      
    // Send data
    if ( amqpInterface->sendBasicMessage(payloadData) )
      result = true;

    // else faceLogger.error("Could not execute method " + methodID);
  }
    
  return result;
}

} // namespace