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

#include "IAMQPMessageDispatch.h"




namespace ecc_amqpAPI_spec
{
    // This enum is currently used internally and controls processing resource
    // for the pump - you will be able to access this behaviour using this enum
    // in later versions of this API.
    enum ePumpPriority
    {
        MINIMUM,
        NORMAL,
        HIGH
    };

    /**
     * IAMQPMessageDispatchPump controls the distribution of messages (through
     * dispatches) to clients listening to the AMQP bus.
     * 
     * @author Simon Crowle
     */
    class IAMQPMessageDispatchPump
    {
    public:

        typedef boost::shared_ptr<IAMQPMessageDispatchPump> ptr_t;

        /**
         * Starts the 'round-robin' distribution of messages queues in IAMQPMessageDispatch
         * instances added to the pump.
         * 
         * @return - Returns false if the pump was unable to start.
         */
        virtual bool startPump() = 0;

        /**
         * Stops the pump issuing messages through the  IAMQPMessageDispatch instances
         * added to it.
         */
        virtual void stopPump();

        /**
         * Stops the pump and empties any queued messages not currently being sent
         */
        virtual void emptyPump() =0;

        /**
         * Returns the state of the pump.
         * 
         * @return - Returns true if the pump is active.
         */
        virtual bool isPumping() =0;

        /**
         * Adds a message dispatch to the pump.
         * 
         * @param dispatch - instance of a dispatch.
         */
        virtual void addDispatch( IAMQPMessageDispatch::ptr_t dispatch ) =0;

        /**
         * Removes a dispatch instance from the pump.
         * 
         * @param dispatch - instance of the dispatch to be removed.
         */
        virtual void removeDispatch( IAMQPMessageDispatch::ptr_t dispatch) =0;
    };

} // namespace