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

    class IEMLiveMonitor
    {
        typedef boost::shared_ptr<IEMLiveMonitor> ptr_t;


        // Listeners -----------------------------------------------------------------
        /**
         * Sets the user listener for this interface
         * 
         * @param listener - Implementation of the 'user' side listener
         */
        virtual void setUserListener( IEMLiveMonitor_UserListener::ptr_t listener ) =0;

        // User methods --------------------------------------------------------------
        /**
         * Notifies the EM that this user is ready to start pushing metric data.
         */
        virtual void notifyReadyToPush() =0;

        /**
         * Sends a Report instance to the EM. IMPORTANT NOTE: users MUST pair this
         * method call with the IEMLiveMonitor_UserListener.onReceivedPush(..) event
         * issued to the user by the EM. Users MUST NOT attempt to push data to the 
         * EM on an ad-hoc basis - they must wait for the onReceivedPush(..) response
         * from the EM before attempting another push.
         * 
         * @param report - Instance of a Report containing measurement data (for now,
         * for a single time frame).
         */
        virtual void pushMetric( Report report ) =0;

        /**
         * Tells the EM that this user has finished pushing Reports.
         * 
         */
        virtual void notifyPushingCompleted() =0;

        /**
         * Notifies the EM that this user is ready to respond to pull requests from
         * the EM. 
         */
        virtual void notifyReadyForPull() =0;

        /**
         * Sends a pulled Report (in response to a request from the EM) to the EM.
         * 
         * @param report  - Instance of a Report containing measurement data (for now,
         * for a single time frame).
         */
        virtual void sendPulledMetric( Report report ) =0;
    };

} // namespace
