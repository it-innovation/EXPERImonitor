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

#include "IEMPostReport_UserListener.h"

#include "EMPostReportSummary.h"



namespace ecc_emClient_spec
{

    class IEMPostReport
    {
    public:

        typedef boost::shared_ptr<IEMPostReport> ptr_t;

        // Listeners -----------------------------------------------------------------    
        /**
         * Sets the user listener for this interface
         * 
         * @param listener - Implementation of the 'user' side listener
         */
        virtual void setUserListener( IEMPostReport_UserListener::ptr_t listener ) =0;

        // User methods --------------------------------------------------------------
        /**
         * Notifies the EM that this user is ready to report.
         */
        virtual void notifyReadyToReport() =0;

        /**
         * Sends a post report summary instance to the EM.
         * 
         * @param summary - summary instance of the report. Fields must not be null.
         */
        virtual void sendReportSummary( ecc_commonDataModel::EMPostReportSummary::ptr_t summary ) =0;

        /**
         * Sends a data batch (based on the specification provided by the EM from the
         * requestDataBatch(..) method.
         * 
         * @param populatedBatch - data batch with metric data. Fields must not be null.
         */
        virtual void sendDataBatch( ecc_commonDataModel::EMDataBatch::ptr_t populatedBatch ) =0;
    };

} // namespace
