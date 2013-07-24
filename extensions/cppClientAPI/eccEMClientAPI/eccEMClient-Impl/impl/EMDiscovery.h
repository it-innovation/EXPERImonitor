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

#include "IEMDiscovery.h"

#include "EMBaseInterface.h"
#include "EMPhase.h"
#include "MetricGenerator.h"




namespace ecc_emClient_impl
{

class EMDiscovery : public EMBaseInterface,
                    public ecc_emClient_spec::IEMDiscovery
{
public:

  typedef boost::shared_ptr<EMDiscovery> ptr_t;
  
  EMDiscovery( ecc_amqpAPI_impl::AMQPBasicSubscriptionService::ptr_t sService,
               ecc_amqpAPI_impl::AMQPBasicChannel::ptr_t             channel,
               ecc_amqpAPI_impl::AMQPMessageDispatch::ptr_t          dispatch,
               const UUID&                                           providerID,
               const UUID&                                           userID,
               const bool                                            isProvider );

  virtual ~EMDiscovery();
  
  // IECCMonitor ---------------------------------------------------------------
  virtual void setUserListener( ecc_emClient_spec::IEMDiscovery_UserListener::ptr_t listener);
  
  virtual void shutdown();

  // User methods --------------------------------------------------------------
  // Method ID = 8
  virtual void readyToInitialise();
  
  // Method ID = 9
  virtual void sendActivePhases( const ecc_commonDataModel::EMPhaseSet& supportedPhases );
  
  // Method ID = 10
  virtual void sendDiscoveryResult( const bool discoveredGenerators );
  
  // Method ID = 11
  virtual void sendMetricGeneratorInfo( const ecc_commonDataModel::MetricGenerator::Set& generators );

  // Method ID = 14
  virtual void enableEntityMetricCollection( const UUID& entityID, const bool enabled );
  
  // Method ID = 12
  virtual void clientDisconnecting();
  
  // Protected methods ---------------------------------------------------------
protected:

  virtual void onInterpretMessage( const int& methodID, const JSONTree& jsonTreea );

  private:
    ecc_emClient_spec::IEMDiscovery_UserListener::ptr_t userListener;
};

} // namespace