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
#include "EMPostReport.h"

#include "AMQPFullInterfaceBase.h"
#include "ModelBase.h"

using namespace ecc_emClient_spec;
using namespace ecc_amqpAPI_impl;
using namespace ecc_commonDataModel;

using namespace boost;
using namespace boost::container;



namespace ecc_emClient_impl
{

EMPostReport::EMPostReport( AMQPBasicSubscriptionService::ptr_t sService,
                            AMQPBasicChannel::ptr_t             channel,
                            AMQPMessageDispatch::ptr_t          dispatch,
                            const UUID&                         providerID,
                            const UUID&                         userID,
                            const bool                          isProvider )
: EMBaseInterface( sService, channel, isProvider )
{
  interfaceName    = L"IEMPostReport";
  interfaceVersion = L"0.1";
            
  interfaceProviderID = providerID;
  interfaceUserID     = userID;
    
  AMQPFullInterfaceBase::ptr_t fullFace = 
      AMQPFullInterfaceBase::ptr_t( new AMQPFullInterfaceBase( sService, channel ) );
  
  setAMQPFaceAndDispatch( dynamic_pointer_cast<AbstractAMQPInterface>(fullFace), dispatch );
}

EMPostReport::~EMPostReport()
{
}

// IECCReport ----------------------------------------------------------------
void EMPostReport::setUserListener( IEMPostReport_UserListener::ptr_t listener )
{ userListener = listener; }
  
// User methods --------------------------------------------------------------
// Method ID = 4
void EMPostReport::notifyReadyToReport()
{
  EXEParamList emptyParams;

  executeMethod( 4, emptyParams );
}
  
// Method ID = 5
void EMPostReport::sendReportSummary( EMPostReportSummary::ptr_t summary )
{
  EXEParamList paramsList;

  //paramsList.Add( summary );
    
  executeMethod( 5, paramsList );
}
  
// Method ID = 6
void EMPostReport::sendDataBatch( EMDataBatch::ptr_t populatedBatch )
{
  EXEParamList paramsList;

  //paramsList.Add( populatedBatch );
    
  executeMethod( 6, paramsList );
}
  
// Protected methods ---------------------------------------------------------
void EMPostReport::onInterpretMessage( const int& methodID, 
                                       const JSONTree& jsonTree )
{
  switch ( methodID )
  {
    case ( 1 ) :
    {
      if ( userListener )
        userListener->onRequestPostReportSummary( interfaceProviderID );
        
    } break;
        
    case ( 2 ) :
    {
      if ( userListener )
      {
        JSONTreeIt tIt = jsonTree.begin(); // Get past method ID first
        ++tIt;

        //EMDataBatch batch = JsonConvert.DeserializeObject<EMDataBatch>( jsonMethodData[1] );      

        //userListener.onRequestDataBatch( interfaceProviderID, batch );
      }
        
    } break;
        
    case ( 3 ) :
    {
      if ( userListener )
      {
        //Guid id = new Guid(jsonMethodData[1]);  
        //userListener.notifyReportBatchTimeOut( interfaceProviderID, id );
      }
        
    } break; 
  }
}


} // namespace