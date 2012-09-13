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
//      Created Date :          08-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp;

import java.io.*;
import java.net.*;

import com.rabbitmq.client.*;




public class AMQPConnectionFactory
{
  private InetAddress amqpHostIP;
  private Connection  amqpConnection;
  
  
  public AMQPConnectionFactory()
  {
  } 
  
  public boolean setAMQPHostIPAddress( String addr )
  {
    boolean ipSuccess = false;
    
    try
    {
      amqpHostIP = InetAddress.getByName( addr );
      ipSuccess = true;
    }
    catch ( UnknownHostException uhe ) { amqpHostIP = null; }
    
    return ipSuccess;
  }
  
  public String getLocalIP()
  {
    String localIPValue = null;
    
    try
    {
      InetAddress localIP = InetAddress.getLocalHost();
      localIPValue = localIP.getHostAddress();
    }
    catch ( UnknownHostException uhe ) {}
    
    return localIPValue;
  }
  
  public void connectToAMQPHost() throws Exception
  {    
    // Safety first
    if ( amqpHostIP == null ) throw new Exception( "AMQP Host IP not correct" );
    if ( amqpConnection != null ) throw new Exception( "Already connected to host" );
    
    ConnectionFactory amqpFactory = new ConnectionFactory();
    amqpFactory.setHost( amqpHostIP.getHostAddress() );
      
    try { amqpConnection = amqpFactory.newConnection(); }
    catch ( IOException ioe )
    { throw new Exception( "Could not create AMQP host connection" ); }
  }
  
  public boolean isConnectionValid()
  { return (amqpConnection != null); }
  
  public AMQPBasicChannel createNewChannel() throws Exception
  {
    if ( amqpConnection == null ) throw new Exception( "No AMSQP connection available" );
    
    return new AMQPBasicChannel( amqpConnection.createChannel() );
  }
}
