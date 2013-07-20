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

#include "ArrayWrapper.h"
#include "StringWrapper.h"

#include "ECCUtils.h"

#include <sstream>

using namespace ecc_amqpAPI_impl;
using namespace ecc_commonDataModel;

using namespace boost;
using namespace boost::container;
using namespace boost::property_tree;

using namespace std;


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
}

void EMBaseInterface::initialiseAMQPComms()
{
  if ( amqpInterface )
  {
    AMQPMessageDispatch::ptr_t msgDispatch = amqpInterface->getMessageDispatch();

    if ( msgDispatch )
    {
      msgDispatch->setListener( IAMQPMessageDispatchListener::ptr_t(shared_from_this()) );
   
      String faceName = interfaceName + L" " + interfaceVersion;
    
      AMQPHalfInterfaceBase::ptr_t halfFace = dynamic_pointer_cast<AMQPHalfInterfaceBase>(amqpInterface);
      AMQPFullInterfaceBase::ptr_t fullFace = dynamic_pointer_cast<AMQPFullInterfaceBase>(amqpInterface);

      if ( halfFace )
        halfFace->initialise( faceName, interfaceProviderID, isProvider );
      else 
        if ( fullFace )
          fullFace->initialise( faceName, interfaceProviderID, interfaceUserID, isProvider );
    }
  }
}
  
void EMBaseInterface::shutdown()
{
  if ( amqpInterface )
  {
    amqpInterface->shutdown();

    amqpSubscriptService = NULL;
    amqpChannel          = NULL;
    amqpInterface        = NULL;
  }
}
  
// IAMQPMessageDispatchListener ----------------------------------------------
void EMBaseInterface::onSimpleMessageDispatched( const std::string& queueName, const std::string& msg )
{
  if ( !queueName.empty() && !msg.empty() )
  {
    ptree              jsonTree;
    std::istringstream stream( msg );
    read_json( stream, jsonTree );

    JSONValue value = ( *jsonTree.begin() );

    // Pull out method ID from tree
    int methodID = getJSON_int( value );

    onInterpretMessage( methodID, jsonTree );
  }
}
  
// Protected methods ---------------------------------------------------------
void EMBaseInterface::setAMQPFaceAndDispatch( AbstractAMQPInterface::ptr_t eccIFace,
                                              AMQPMessageDispatch::ptr_t   msgDispatch )
{
  if ( eccIFace && msgDispatch )
  {
    amqpInterface = eccIFace;

    amqpInterface->setMessageDispatch( msgDispatch ); 
  }
}
  
bool EMBaseInterface::executeMethod( const int methodID, const EXEParamList& params )
{
  bool result = false;
    
  if ( amqpInterface )
  {
    // Wrap up all parameters in a JSON array, leading with method ID
    String payloadData = L"[";

    // Method ID
    payloadData.append( intToString( methodID ) + L"," );

    // JSON Array of parameters
    EXEParamList::const_iterator pIt = params.begin();
    while ( pIt != params.end() )
    {
      ModelBase::ptr_t model = *pIt;

      payloadData.append( model->toJSON() );
      payloadData.append( L"," );

      ++pIt;
    }

    // Remove last delimiter & wrap up
    if ( !params.empty() )
    {
      unsigned int plLen = payloadData.length();
      payloadData = payloadData.substr( 0, plLen-1 );
    }
    
    payloadData.append( L"]" );

    // Send data
    if ( amqpInterface->sendBasicMessage(payloadData) )
      result = true;

    // else faceLogger.error("Could not execute method " + methodID);
  }
    
  return result;
}

} // namespace