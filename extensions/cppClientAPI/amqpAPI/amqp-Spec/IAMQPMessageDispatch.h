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
//      Created Date :          15-May-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#pragma once

#include "IAMQPMessageDispatchListener.h"


namespace ecc_amqpAPI_spec
{

    /**
     * IAMQPMessageDispatch acts as a dispatching service for AMQP subscribers -
     * add instances of this dispatch to the IAMQPMessageDispatchPump to enable
     * message processing
     * 
     * @author sgc
     */
    class IAMQPMessageDispatch
    {
    public:

      typedef boost::shared_ptr<IAMQPMessageDispatch> ptr_t;

        /**
         * The IAMQPMessageDispatchListener is called when a message targeted at
         * the dispatch arrives.
         * 
         * @param listener - class implementing the listener interface
         */
      virtual void setListener( IAMQPMessageDispatchListener::ptr_t listener ) =0;

        /**
         * Returns the current listener for this dispatch.
         * 
         * @return - class implementing the listener
         */
        virtual IAMQPMessageDispatchListener::ptr_t getListener() =0;
    };

} // namespace
