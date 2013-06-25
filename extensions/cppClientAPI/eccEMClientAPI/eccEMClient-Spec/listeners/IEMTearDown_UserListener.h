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

#include <boost/shared_ptr.hpp>
#include <boost/uuid/uuid.hpp>



namespace ecc_em_spec_faces_listeners
{
    /**
     * IEMTearDown_UserListener is a listener for user clients to enact tear-down
     * operations under the coordination of the EM.
     * 
     * @author sgc
     */
    class IEMTearDown_UserListener
    {
        typedef boost::shared_ptr<IEMTearDown_UserListener> ptr_t;

        /**
         * Notification from the EM that it is time to tear-down experiment based
         * resources.
         * 
         * @param senderID - ID of the EM
         */
        virtual void onTearDownMetricGenerators( boost::uuids::uuid senderID ) =0;

        /**
         * Notification from the EM that time has run-out for reporting the result
         * of the tear-down process to the EM.
         * 
         * @param senderID 
         */
        virtual void onTearDownTimeOut( boost::uuids::uuid senderID ) =0;
    };

} // namespace