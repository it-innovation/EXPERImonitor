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

import com.rabbitmq.client.*;

import java.io.IOException;




public class AMQPBasicChannel
{
  private Channel amqpChannel;
  
  public AMQPBasicChannel( Channel channel )
  { amqpChannel = channel; }
  
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
    if ( amqpChannel != null )
      if ( amqpChannel.isOpen() )
        try 
        { 
          amqpChannel.close();
          amqpChannel = null;
        }
        catch (IOException ioe) {}
  }
  
}
