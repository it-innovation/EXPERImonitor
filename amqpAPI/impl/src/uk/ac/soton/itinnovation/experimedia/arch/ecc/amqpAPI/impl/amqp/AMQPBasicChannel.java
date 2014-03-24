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
//      Created Date :          11-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.IAMQPChannelListener;

import com.rabbitmq.client.*;

import java.io.IOException;




public class AMQPBasicChannel
{
	private final static IECCLogger channelLogger = Logger.getLogger( AMQPBasicChannel.class );
	
  private Channel                 amqpChannel;
  private ChannelShutdownListener rabbitListener;
  private IAMQPChannelListener    channelListener;
  private boolean                 isClosingDown;
  
	
	public static boolean amqpQueueExists( AMQPConnectionFactory conFactory,
																				 String queueName ) throws Exception
	{
		// Safety first
		if ( conFactory == null ) throw new Exception( "Could not test amqp queue: amqp connection factory is null" );
		if ( queueName == null ) throw new Exception( "Could not test amqp queue: queue name is null" );
		if ( !conFactory.isConnectionValid() ) throw new Exception( "Could not test amqp queue: factory connection is invalid" );
		
		// Need to create an independent connection & channel to test for a queue -
		// a negative result automatically closes a channel.
		
		AMQPBasicChannel bc = conFactory.createNewChannel();
		Channel channelImpl = (Channel) bc.getChannelImpl();
		
		boolean result     = false;
		String  resultInfo = "AMQP queue ( " + queueName + ") existence result: ";
		
		if ( channelImpl != null && channelImpl.isOpen() )
		{
			// Try passively declaring the queue to see if it exists
			try
			{
				channelImpl.queueDeclarePassive( queueName );
				result = true;
				resultInfo += "exists";
				channelImpl.close();
			}
			catch ( IOException ex )
			{ 
				resultInfo += "does not exist";
				// Channel will be automatically closed in this case
			}

		}
		else resultInfo += " could not test: channel is null or closed";
		
		channelLogger.info( resultInfo ); 
		
		return result;
	}
  
  public AMQPBasicChannel( Channel channel )
  { 
    amqpChannel = channel;
  }
  
  public void setListener( IAMQPChannelListener listener )
  {
    if ( listener == null )
    {
      // Remove any existing listener is new listener is null
      if ( rabbitListener != null )
        amqpChannel.removeShutdownListener( rabbitListener );
    }
    else
    {
      rabbitListener = new ChannelShutdownListener();
      amqpChannel.addShutdownListener( rabbitListener );
      
      channelListener = listener;
    }
  }
  
  public Object getChannelImpl()
  { return amqpChannel; }
  
  public boolean isOpen()
  {
    if ( amqpChannel != null )
      return ( amqpChannel.isOpen() );
    
    return false;
  }
  
  public void close()
  {
    if ( amqpChannel != null && !isClosingDown )
      if ( amqpChannel.isOpen() )
        try 
        {
          isClosingDown = true;
          
          if ( rabbitListener != null )
            amqpChannel.removeShutdownListener( rabbitListener );
          
          amqpChannel.close();
          amqpChannel = null;
        }
        catch (IOException ioe) {}
  } 
  
  // Private methods/classes ---------------------------------------------------
  private void notifyChannelClosed( ShutdownSignalException sse )
  {
    if ( !isClosingDown && sse != null )
    {
      boolean connectionOK = ( !sse.isHardError() );
      
      if ( channelListener != null )
        channelListener.onChannelShutdown( connectionOK,
                                           sse.getMessage() );
    }
  }
  
  private class ChannelShutdownListener implements ShutdownListener
  {
    @Override
    public void shutdownCompleted( ShutdownSignalException cause )
    { notifyChannelClosed(cause);}
  }
}
