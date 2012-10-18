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
import java.security.*;
import javax.net.ssl.*;

import com.rabbitmq.client.*;
import org.apache.log4j.Logger;




public class AMQPConnectionFactory
{
  private static Logger factoryLog = Logger.getLogger( AMQPConnectionFactory.class );
  
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
    if ( amqpHostIP     == null ) throw new Exception( "AMQP Host IP not correct" );
    if ( amqpConnection != null ) throw new Exception( "Already connected to host" );
    
    ConnectionFactory amqpFactory = new ConnectionFactory();
    amqpFactory.setHost( amqpHostIP.getHostAddress() );
      
    try { amqpConnection = amqpFactory.newConnection(); }
    catch ( Exception e )
    { throw new Exception( "Could not create AMQP host connection: " + e.getMessage() ); }
  }
  
  public void connectToAMQPSSLHost() throws Exception
  {
    // Safety first
    if ( amqpHostIP     == null ) throw new Exception( "AMQP Host IP not correct" );
    if ( amqpConnection != null ) throw new Exception( "Already connected to host" );
    
    ConnectionFactory amqpFactory = new ConnectionFactory();
    amqpFactory.setHost( amqpHostIP.getHostAddress() );
    amqpFactory.setPort( 5671 );
    amqpFactory.useSslProtocol();
    
    try { amqpConnection = amqpFactory.newConnection(); }
    catch ( Exception e )
    { throw new Exception( "Could not create AMQP host SSL connection: " + e.getMessage() ); }
  }
  
  public void connectToVerifiedAMQPHost( InputStream keystore,
                                         String      password ) throws Exception
  {
    // Safety first
    if ( amqpHostIP     == null ) throw new Exception( "AMQP Host IP not correct" );
    if ( amqpConnection != null ) throw new Exception( "Already connected to host" );
    if ( password       == null ) throw new Exception( "Password is null" );
    
    // KEYSTORE and PASSWORD ONE
    char[] keyPassphrase = password.toCharArray();
    KeyStore ks = KeyStore.getInstance( "PKCS12" );
    try { ks.load( keystore, keyPassphrase ); }
    catch ( Exception e )
    {
      factoryLog.error( "Had problems loading keystore: " + e.getMessage() );
      throw e;
    }
    
    KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
    kmf.init( ks, keyPassphrase );

    // TRUST STORE and PASSWORD TWO
    char[] trustPassphrase = password.toCharArray();  
    KeyStore tks = KeyStore.getInstance( "JKS" );
    try { tks.load( keystore, trustPassphrase ); }
    catch ( Exception e )
    {
      factoryLog.error( "Had problems loading keystore (for trust management): " + e.getMessage() );
      throw e;
    }

    TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
    tmf.init( tks );

    SSLContext sslContext = SSLContext.getInstance("SSLv3");
    sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );

    ConnectionFactory amqpFactory = new ConnectionFactory();
    amqpFactory.setHost( amqpHostIP.getHostAddress() );
    amqpFactory.setPort( 5671 );
    amqpFactory.useSslProtocol( sslContext );
      
    try { amqpConnection = amqpFactory.newConnection(); }
    catch ( IOException ioe )
    { throw new Exception( "Could not create secure AMQP host connection" ); }
  }
  
  public boolean isConnectionValid()
  { return (amqpConnection != null); }
  
  public AMQPBasicChannel createNewChannel() throws Exception
  {
    if ( amqpConnection == null ) throw new Exception( "No AMSQP connection available" );
    
    return new AMQPBasicChannel( amqpConnection.createChannel() );
  }
}
