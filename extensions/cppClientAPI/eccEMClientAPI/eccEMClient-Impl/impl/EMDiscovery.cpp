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
#include "EMDiscovery.h"

#include "AMQPFullInterfaceBase.h"
#include "ModelBase.h"

using namespace ecc_amqpAPI_impl;
using namespace ecc_commonDataModel;
using namespace ecc_emClient_spec;

using namespace boost;
using namespace boost::container;



namespace ecc_emClient_impl
{

EMDiscovery::EMDiscovery( AMQPBasicSubscriptionService::ptr_t sService,
                          AMQPBasicChannel::ptr_t             channel,
                          AMQPMessageDispatch::ptr_t          dispatch,
                          const UUID&                         providerID,
                          const UUID&                         userID,
                          const bool                          isProvider )
: EMBaseInterface( sService, channel, isProvider )
{
  interfaceName    = L"IEMDiscovery";
  interfaceVersion = L"0.1";
            
  interfaceProviderID = providerID;
  interfaceUserID     = userID;
    
  AMQPFullInterfaceBase::ptr_t fullFace = 
      AMQPFullInterfaceBase::ptr_t( new AMQPFullInterfaceBase( sService, channel ) );
  
  initialiseAMQP( dynamic_pointer_cast<AbstractAMQPInterface>(fullFace), dispatch );
}
  
// IECCMonitor ---------------------------------------------------------------
void EMDiscovery::setUserListener( IEMDiscovery_UserListener::ptr_t listener)
{ userListener = listener; }
    
// User methods --------------------------------------------------------------
// Method ID = 8
void EMDiscovery::readyToInitialise()
{
  list<ModelBase::ptr_t> emptyParams;

  executeMethod( 8, emptyParams );
}
  
// Method ID = 9
void EMDiscovery::sendActivePhases( const EMPhaseSet& supportedPhases )
{
  list<ModelBase::ptr_t> paramsList;

  //paramsList.Add( supportedPhases );
    
  executeMethod( 9, paramsList );
}
  
// Method ID = 10
void EMDiscovery::sendDiscoveryResult( const bool discoveredGenerators )
{
  list<ModelBase::ptr_t> paramsList;

  //paramsList.Add( discoveredGenerators );
    
  executeMethod( 10, paramsList );
}
  
// Method ID = 11
void EMDiscovery::sendMetricGeneratorInfo( const MetricGenerator::Set& generators )
{
  list<ModelBase::ptr_t> paramsList;

  //paramsList.Add( generators );
    
  executeMethod( 11, paramsList );
}
  
// Method ID = 12
void EMDiscovery::clientDisconnecting()
{
  list<ModelBase::ptr_t> emptyParams;

  executeMethod( 12, emptyParams );
}
  
// Protected methods ---------------------------------------------------------
void EMDiscovery::onInterpretMessage( const int& methodID, const JSONTree& jsonTree )
{
  switch ( methodID )
  {
    case ( 1 ) :
    {
      if ( userListener )
      {
          //// Need to convert from enum string values manually here
          //String enumVal = jsonMethodData[1];
          //EMInterfaceType faceType = EMInterfaceType.eEMUnknownInface;

          //if (enumVal.Equals("eEMSetup"))            faceType = EMInterfaceType.eEMSetup;
          //else if (enumVal.Equals("eEMLiveMonitor")) faceType = EMInterfaceType.eEMLiveMonitor;
          //else if (enumVal.Equals("eEMPostReport"))  faceType = EMInterfaceType.eEMPostReport;
          //else if (enumVal.Equals("eEMTearDown"))    faceType = EMInterfaceType.eEMTearDown;

          //if (faceType != EMInterfaceType.eEMUnknownInface)
          //    userListener.onCreateInterface( interfaceProviderID, faceType );
      }
        
    } break;
        
    case ( 2 ) :
    {
      if ( userListener )
      {
          //bool confirmed      = Boolean.Parse(jsonMethodData[1]);
          //Guid expUniqueID    = new Guid(jsonMethodData[2]);
          //string expNamedID   = jsonMethodData[3];
          //string expName      = jsonMethodData[4];
          //string expDesc      = jsonMethodData[5];
          //DateTime createTime = DateTime.Parse(jsonMethodData[6]);

          //userListener.onRegistrationConfirmed( interfaceProviderID, 
          //                                      confirmed, expUniqueID, 
          //                                      expNamedID, expName, expDesc,
          //                                      createTime );
      }
    } break;
        
    case ( 3 ) :
    {
      if ( userListener )
        userListener->onRequestActivityPhases( interfaceProviderID );
        
    } break;
        
    case ( 4 ) :
    {
      if ( userListener )
        userListener->onDiscoverMetricGenerators( interfaceProviderID );
        
    } break;
        
    case ( 5 ) :
    {
      if ( userListener )
        userListener->onRequestMetricGeneratorInfo( interfaceProviderID );
        
    } break;
        
    case ( 6 ) :
    {
      if ( userListener )
        userListener->onDiscoveryTimeOut( interfaceProviderID );
        
    } break;
        
    case ( 7 ) :
    {
      if ( userListener )
      {
        //string endPoint = jsonMethodData[1];
        //userListener.onSetStatusMonitorEndpoint( interfaceProviderID, endPoint );
      } 
        
    } break;
          
    case ( 13 ) :
    {
      if ( userListener )
      {
        //string reason = jsonMethodData[1];
        //userListener.onDeregisteringThisClient( interfaceProviderID, reason );
      }
        
    } break;
  }
}

} // namespace