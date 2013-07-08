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

#include "ECCUtils.h"




namespace ecc_emClient_spec
{
/**
  * IEMSetup_UserListener is a user client listener that listens to instruction
  * from the EM regarding setting up their metric generators.
  * 
  * @author sgc
  */
class IEMSetup_UserListener
{
public:

    typedef boost::shared_ptr<IEMSetup_UserListener> ptr_t;

    /**
      * Notification from the EM that the user client should attempt to set-up
      * the MetricGenerator identified by the generator ID
      * 
      * @param senderID - ID of the EM
      * @param genID    - ID of the MetricGenerator that requires setting up
      */
    virtual void onSetupMetricGenerator( const UUID& senderID, const UUID& genID ) =0;
  
    /**
      * Notifies the user client that time has run out to set-up the MetricGenerator
      * set up by
      * 
      * @param senderID - ID of the EM
      * @param genID    - ID of the MetricGenerator that required setting up
      */
    virtual void onSetupTimeOut( const UUID& senderID, const UUID& genID ) =0;
};

} // namespace