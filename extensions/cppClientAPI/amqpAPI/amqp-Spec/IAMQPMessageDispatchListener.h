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

namespace ecc_amqpAPI_spec
{

    /**
     * The IAMQPMessageDispatchListener interface must be used to receive messages
     * distributed by the IAMQPMessageDispatch.
     * 
     * @author Simon Crowle
     */
    class IAMQPMessageDispatchListener
    {
    public:

      typedef boost::shared_ptr<IAMQPMessageDispatchListener> ptr_t;

      /**
        * onSimpleMessageDispatched offers an data event from the AMQP bus that
        * encapsulates a block of data and the queue through which it was sent.
        * 
        * @param queueName - Name of the queue in which the data travelled
        * @param data      - The data itself
        */
      virtual void onSimpleMessageDispatched( const String& queueName, const Byte* data ) =0;
    };

} // namespace
