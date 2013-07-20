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

#include "IEMSetup_UserListener.h"




namespace ecc_emClient_spec
{

    class IEMMetricGenSetup
    {
    public:

        typedef boost::shared_ptr<IEMMetricGenSetup> ptr_t;


        // Listeners -----------------------------------------------------------------
        /**
         * Sets the user listener for this interface
         * 
         * @param listener - Implementation of the 'user' side listener
         */
        virtual void setUserListener( IEMSetup_UserListener::ptr_t listener) =0;

        /**
        * Shuts down the interface
        */
        virtual void shutdown() =0;

        // User methods --------------------------------------------------------------
        /**
         * Notifies the EM that the user is ready to set up its MetricGenerators
         */
        virtual void notifyReadyToSetup() =0;

        /**
         * Notifies the EM of the MetricGenerator (identified by the ID) set-up result
         * 
         * @param genID   - ID of the MetricGenerator
         * @param success - Success or failure of the set-up attempt
         */
        virtual void notifyMetricGeneratorSetupResult( const UUID& genID, const bool success) =0;
    };

} // namespace
