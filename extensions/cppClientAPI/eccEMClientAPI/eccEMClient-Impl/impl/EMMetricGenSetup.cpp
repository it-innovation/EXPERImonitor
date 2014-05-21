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
#include "EMMetricGenSetup.h"

#include "AMQPFullInterfaceBase.h"
#include "ModelBase.h"

#include "BoolWrapper.h"
#include "UUIDWrapper.h"

using namespace ecc_emClient_spec;
using namespace ecc_amqpAPI_impl;
using namespace ecc_commonDataModel;

using namespace boost;
using namespace boost::container;



namespace ecc_emClient_impl
{

EMMetricGenSetup::EMMetricGenSetup( AMQPBasicSubscriptionService::ptr_t sService,
                                    AMQPBasicChannel::ptr_t             inChannel,
                                    AMQPBasicChannel::ptr_t             outChannel,
                                    AMQPMessageDispatch::ptr_t          dispatch,
                                    const UUID&                         providerID,
                                    const UUID&                         userID,
                                    bool                                isProvider )
: EMBaseInterface( sService, inChannel, outChannel, isProvider )
{
  interfaceName    = L"IEMMetricGenSetup";
  interfaceVersion = L"0.1";
            
  interfaceProviderID = providerID;
  interfaceUserID     = userID;
    
  AMQPFullInterfaceBase::ptr_t fullFace = 
      AMQPFullInterfaceBase::ptr_t( new AMQPFullInterfaceBase( sService, inChannel, outChannel ) );
  
  setAMQPFaceAndDispatch( dynamic_pointer_cast<AbstractAMQPInterface>(fullFace), dispatch );
}

EMMetricGenSetup::~EMMetricGenSetup()
{
}

void EMMetricGenSetup::shutdown()
{
  EMBaseInterface::shutdown();

  userListener = NULL;
}
  
// IEMMonitorSetup -----------------------------------------------------------
void EMMetricGenSetup::setUserListener( IEMSetup_UserListener::ptr_t listener )
{ userListener = listener; }
  
// User methods --------------------------------------------------------------
// Method ID = 3
void EMMetricGenSetup::notifyReadyToSetup()
{
  EXEParamList emptyParams;

  executeMethod( 3, emptyParams );
}
  
// Method ID = 4
void EMMetricGenSetup::notifyMetricGeneratorSetupResult( const UUID& genID, const bool success )
{
  EXEParamList paramsList;

  paramsList.push_back( UUIDWrapper::ptr_t( new UUIDWrapper(genID) ) );

  paramsList.push_back( BoolWrapper::ptr_t( new BoolWrapper(success) ) );
    
  executeMethod( 4, paramsList );
}
  
// Protected methods ---------------------------------------------------------
void EMMetricGenSetup::onInterpretMessage( const int& methodID, const JSONTree& jsonTree )
{   
  switch ( methodID )
  {
    case ( 1 ) :
    { 
      if ( userListener )
      {
        JSONTreeIt tIt = jsonTree.begin(); // Get past method ID first
        ++tIt;

        UUID genID = getJSON_UUID( *tIt );
 
        userListener->onSetupMetricGenerator( interfaceProviderID, genID );
      }
        
    } break;
        
    case ( 2 ) :
    {
      if ( userListener )
      {
        JSONTreeIt tIt = jsonTree.begin(); // Get past method ID first
        ++tIt;

        UUID genID = getJSON_UUID( *tIt );

        userListener->onSetupTimeOut( interfaceProviderID, genID );
      }
        
    } break;
  }
 }

} // namespace
