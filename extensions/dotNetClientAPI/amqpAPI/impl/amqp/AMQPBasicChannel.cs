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

using RabbitMQ.Client;

using System;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp
{

    public class AMQPBasicChannel
    {
        private readonly log4net.ILog channelLogger = log4net.LogManager.GetLogger(System.Reflection.MethodBase.GetCurrentMethod().DeclaringType);
        private IModel amqpChannel;

        public static bool amqpQueueExists(AMQPConnectionFactory conFactory, String queueName)
        {
            // Safety first
            if (conFactory == null) throw new Exception("Could not test amqp queue: amqp connection factory is null");
            if (queueName == null) throw new Exception("Could not test amqp queue: queue name is null");
            if (!conFactory.isConnectionValid()) throw new Exception("Could not test amqp queue: factory connection is invalid");

            // Need to create an independent connection & channel to test for a queue -
            // a negative result automatically closes a channel.
            AMQPBasicChannel bc = conFactory.createNewChannel();
            IModel channelImpl = (IModel)bc.getChannelImpl();

            bool result = false;
            String resultInfo = "AMQP queue ( " + queueName + ") existence result: ";

            if (channelImpl != null && channelImpl.IsOpen)
            {
                // Try passively declaring the queue to see if it exists
                try
                {
                    channelImpl.QueueDeclarePassive(queueName);
                    result = true;
                    resultInfo += "exists";
                    channelImpl.Close();
                }
                catch (Exception)
                {
                    resultInfo += "does not exist";
                    // Channel will be automatically closed in this case
                }
            }
            else resultInfo += " could not test: channel is null or closed";

            return result;
        }

        public AMQPBasicChannel(IModel channel)
        { amqpChannel = channel; }

        public Object getChannelImpl()
        { return amqpChannel; }

        public bool isOpen()
        {
            if (amqpChannel != null)
                return (amqpChannel.IsOpen);

            return false;
        }

        public void close()
        {
            if (amqpChannel != null)
                if (amqpChannel.IsOpen)
                    try
                    {
                        amqpChannel.Close();
                        amqpChannel = null;
                    }
                    catch (Exception ioe)
                    {
                        String err = "Failed to close AMQP channel: " + ioe.Message;
                        channelLogger.Error(err);
                    }
        }
    }

} // namespace
