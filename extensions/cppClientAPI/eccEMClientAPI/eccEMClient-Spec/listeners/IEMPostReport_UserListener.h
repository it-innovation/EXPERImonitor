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

#include "EMDataBatch.h"

#include "ECCUtils.h"




namespace ecc_emClient_spec
{
/**
  * IEMPostReport_UserListener is a user client listener that listens to the EM
  * for post-report data generation requests.
  * 
  * @author sgc
  */
class IEMPostReport_UserListener
{
public:

    typedef boost::shared_ptr<IEMPostReport_UserListener> ptr_t;

    /**
      * Request from the EM to generate a post-report summary
      * 
      * @param senderID - ID of the EM
      */
    virtual void onRequestPostReportSummary( const UUID& senderID ) =0;
  
    /**
      * Request from the EM for the user client to generate metric data based
      * on the specification of the EMDataBatch instance.
      * 
      * @param senderID  - ID of the EM
      * @param reqBatch  - Instance of the data batch required by the EM
      */
    virtual void onRequestDataBatch( const UUID& senderID, 
                                     ecc_commonDataModel::EMDataBatch::ptr_t reqBatch ) =0;
  
    /**
      * Notification by the EM that time has run out for the client user to
      * send the batched data requested (identified by the ID) by the EM.
      * 
      * @param senderID  - ID of the EM
      * @param batchID   - ID of the batch data requested
      */
    virtual void notifyReportBatchTimeOut( const UUID& senderID, const UUID& batchID ) =0;
};

} // namespace