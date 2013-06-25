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

namespace ecc_em_spec_faces
{

    /**
     * IEMTearDown supports a basic tear-down control and reporting procedure controlled
     * by the EM.
     * 
     * @author sgc
     */
    class IEMTearDown
    {
        typedef boost::shared_ptr<IEMTearDown> ptr_t;

        // Listeners -----------------------------------------------------------------
        /**
         * Sets the user listener for this interface
         * 
         * @param listener - Implementation of the 'user' side listener
         */
        virtual void setUserListener( IEMTearDown_UserListener::ptr_t listener ) =0;

        // User methods --------------------------------------------------------------
        /**
         * Notify the EM that this client is ready to start its tear-down process.
         */
        virtual void notifyReadyToTearDown() =0;

        /**
         * Send the EM the result of this user's tear-down process.
         * 
         * @param success 
         */
        virtual void sendTearDownResult( bool success ) =0;
    };

} // namespace
