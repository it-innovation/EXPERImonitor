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
//      Created Date :          20-June-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#pragma once

#include "EMInterfaceType.h"
#include "ECCUtils.h"



namespace ecc_emClient_spec
{
/**
  * This listener provides access to methods executed by the provider (the EM)
  * which are sent to the user. This includes requests to generate interfaces; 
  * confirmation of registration; request for activity phase support etc.
  * 
  * @author sgc
  */
class IEMDiscovery_UserListener
{
public:

    typedef boost::shared_ptr<IEMDiscovery_UserListener> ptr_t;

    /**
      * Request by the provider for the user to create a (user) interface instance
      * of the type EMInterfaceType.
      * 
      * @param senderID - ID of the EM sending this event
      * @param type     - type (EMInterfaceType) of interface required
      */
      virtual void onCreateInterface( const UUID& senderID, const ecc_commonDataModel::EMInterfaceType& type ) =0;
  
    /**
      * Provider (EM) notifies the user that they have successfully (or not)
      * registered.
      * 
      * @param senderID       - ID of the EM sending this event
      * @param confirmed      - Confirmed registration - provider is aware of user (or not)
      * @param experimentID   - Unique ID for the experiment being run by the EM
      * @param expName        - Name of the experiment being run by the EM
      * @param expDescription - Short description of the experiment being run by the EM
      * @param createTime     - Creation time of the experiment
      */
    virtual void onRegistrationConfirmed( const UUID&      senderID, 
                                          const bool       confirmed,
                                          const UUID&      expUniqueID,
                                          const String&    expNamedID,
                                          const String&    expName,
                                          const String&    expDescription,
                                          const TimeStamp& createTime ) =0;
  
    /**
      * Provider (EM) has notified the user that they are being de-registered. Clients
      * should reply by using the clientDisconnecting() method on the discovery interface.
      * 
      * @param senderID  - ID of the EM sending this event
      * @param reason    - Brief description for the reason for de-registering
      */
    virtual void onDeregisteringThisClient( const UUID&   senderID,
                                            const String& reason ) =0;
  
    /**
      * EM requests a list of supported activities by the user.
      * 
      * @param senderID - ID of the EM sending this event
      */
    virtual void onRequestActivityPhases( const UUID& senderID ) =0;
  
    /**
      * EM requests the user discovers any metric generators that are available
      * to generate metric data to send to the EM.
      * 
      * @param senderID - ID of the EM sending this event
      */
    virtual void onDiscoverMetricGenerators( const UUID& senderID ) =0;
  
    /**
      * EM requests the user sends it a model of the generators that it has available
      * to it
      * 
      * @param senderID - ID of the EM sending this event
      */
    virtual void onRequestMetricGeneratorInfo( const UUID& senderID ) =0;
  
    /**
      * EM notifies the user that time has run out to discover any more metric
      * generators - the user should stop this process if it is continuing.
      * 
      * @param senderID - ID of the EM sending this event
      */
    virtual void onDiscoveryTimeOut( const UUID& senderID ) =0;
  
    /**
      * The EM has sent a status monitoring end-point for the user to (optionally)
      * use to provide active reporting of the systems it is currently using.
      * 
      * @param senderID - ID of the EM sending this event
      */
    virtual void onSetStatusMonitorEndpoint( const UUID& senderID,
                                             const String& endPoint ) =0;
};

} // namespace