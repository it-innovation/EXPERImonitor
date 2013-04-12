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
//      Created Date :          08-Apr-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec
{

    /**
     * IAMQPMessageDispatch acts as a dispatching service for AMQP subscribers -
     * add instances of this dispatch to the IAMQPMessageDispatchPump to enable
     * message processing
     * 
     * @author sgc
     */
    public interface IAMQPMessageDispatch
    {
        /**
         * The IAMQPMessageDispatchListener is called when a message targeted at
         * the dispatch arrives.
         * 
         * @param listener - class implementing the listener interface
         */
        void setListener(IAMQPMessageDispatchListener listener);

        /**
         * Returns the current listener for this dispatch.
         * 
         * @return - class implementing the listener
         */
        IAMQPMessageDispatchListener getListener();
    }

} // namespace
