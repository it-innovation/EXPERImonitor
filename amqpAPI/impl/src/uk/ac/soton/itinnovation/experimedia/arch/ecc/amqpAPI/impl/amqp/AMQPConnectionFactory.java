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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Properties;
import javax.net.ssl.*;

import com.rabbitmq.client.*;




public class AMQPConnectionFactory
{
	private final Logger factoryLog = LoggerFactory.getLogger(getClass());

    private InetAddress amqpHostIP;
    private int         amqpPortNumber  = 5672;
    private Connection  amqpConnection;
    private String      userName        = null;
    private String      userPass        = null;
    private Integer     heartbeatRate   = null;


    public AMQPConnectionFactory()
    {} 

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
    
    public void closeDownConnection()
    {
      if ( amqpConnection != null )
        try 
        {
          if ( amqpConnection.isOpen() ) amqpConnection.close();
          
          amqpConnection = null;
        }
        catch (Exception ex) 
        { factoryLog.error("Could not close down connection" + ex.getMessage() ); }
    }

    public boolean setAMQPHostPort( int port )
    {
        if ( port < 1 ) return false;

        amqpPortNumber = port;
        return true;
    }
    
    public void setRabbitUserLogin( String name, String password )
    {
      if ( name != null && password != null )
      {
        userName = name;
        userPass = password;
      }
    }
    
    public void setRabbitHeartbeatRate( int rate )
    {
      if ( rate > 1 ) heartbeatRate = rate;
    }

    public String getLocalIP()
    {
      String localIPValue = null;

      try
      {
          InetAddress localIP = InetAddress.getLocalHost();
          localIPValue = localIP.getHostAddress();
      }
      catch ( UnknownHostException uhe ) 
      { factoryLog.error( "Could not create valid IP address: " + uhe.getMessage() ); }

      return localIPValue;
    }

    public void connectToAMQPHost() throws Exception
    {    
        // Safety first
        if ( amqpHostIP     == null ) throw new Exception( "AMQP Host IP not correct" );
        if ( amqpConnection != null ) throw new Exception( "Already connected to host" );

        ConnectionFactory amqpFactory = new ConnectionFactory();
        amqpFactory.setHost( amqpHostIP.getHostAddress() );
        amqpFactory.setPort( amqpPortNumber );
        
        // Select login credentials (if available)
        String selUserName = "guest";
        String selPassword = "guest";
        
        if ( userName != null && userPass != null )
        {
          selUserName = userName;
          selPassword = userPass;
        }
        
        // Set login details
        amqpFactory.setUsername( selUserName );
        amqpFactory.setPassword( selPassword );
        factoryLog.info("Logging into RabbitMQ as \'" + userName + "\'");
        
        // Set heartbeat rate, if available
        if ( heartbeatRate != null )
          amqpFactory.setRequestedHeartbeat( heartbeatRate );
        
        // Execute log-in
        try { amqpConnection = amqpFactory.newConnection(); }
        catch ( Exception ex )
        { throw new Exception( "Could not create AMQP host connection", ex ); }
    }

    public void connectToAMQPSSLHost() throws Exception
    {
        // Safety first
        if ( amqpHostIP     == null ) throw new Exception( "AMQP Host IP not correct" );
        if ( amqpConnection != null ) throw new Exception( "Already connected to host" );

        ConnectionFactory amqpFactory = new ConnectionFactory();
        amqpFactory.setHost( amqpHostIP.getHostAddress() );
        amqpFactory.setPort( amqpPortNumber );
        amqpFactory.useSslProtocol();

        try { amqpConnection = amqpFactory.newConnection(); }
        catch ( Exception ex )
        { throw new Exception( "Could not create AMQP host SSL connection: ", ex ); }
    }

    public void connectToVerifiedAMQPHost( InputStream keystore,
                                           String      password ) throws Exception
    {
        // Safety first
        if ( amqpHostIP     == null ) throw new Exception( "AMQP Host IP not correct" );
        if ( amqpConnection != null ) throw new Exception( "Already connected to host" );
        if ( password       == null ) throw new Exception( "Password is null" );

        char[] trustPassphrase = password.toCharArray();  
        KeyStore tks = KeyStore.getInstance( "JKS" );
        try { tks.load( keystore, trustPassphrase ); }
        catch ( Exception ex )
        {
          factoryLog.error( "Had problems loading keystore: " + ex.getMessage() );
          throw ex;
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
        tmf.init( tks );

        SSLContext sslContext = SSLContext.getInstance("SSLv3");
        sslContext.init( null, tmf.getTrustManagers(), null );

        ConnectionFactory amqpFactory = new ConnectionFactory();
        amqpFactory.setHost( amqpHostIP.getHostAddress() );
        amqpFactory.setPort( amqpPortNumber );
        amqpFactory.useSslProtocol( sslContext );

        try { amqpConnection = amqpFactory.newConnection(); }
        catch ( IOException ioe )
        { throw new Exception( "Could not create secure AMQP host connection", ioe ); }
    }
    
    public void connectToAMQPHost( Properties emProps ) throws Exception
    {
        if ( emProps == null ) throw new Exception( "EM properties are NULL" );
        
        // Get shared properties first
        String rabbitServerIP   = emProps.getProperty( "Rabbit_IP" );
        String rabbitServerPort = emProps.getProperty( "Rabbit_Port" );
        
        // If a password exists for the Rabbit connection, use it
        if ( emProps.containsKey("Rabbit_Password") ) 
        {
            factoryLog.info( "Will be using password to connect to AMQP" );
            userPass = emProps.getProperty( "Rabbit_Password" );
        } 
        else
          factoryLog.info( "No password provided, will try guest login" );
        
        // If username and password are supplied, use these
        if ( emProps.containsKey("Rabbit_Username") )
        {
            userName = emProps.getProperty("Rabbit_Username");
            factoryLog.info("Will be using username \'" + userName + "\' to connect to AMQP");
        } 
        else 
            factoryLog.info("No username provided, will try guest login");
        
        factoryLog.info( "Trying to connect to AMQP bus..." );
        
        // Proceed only if these at least exist
        if ( rabbitServerIP != null && rabbitServerPort != null )
        {
            int portNumber = Integer.parseInt( rabbitServerPort );
            
            setAMQPHostIPAddress( rabbitServerIP );
            setAMQPHostPort( portNumber );
          
            // Now check to see if we're using a verified connection type
            if ( emProps.containsKey("Rabbit_Keystore") )
            {
                InputStream ksStream = AMQPConnectionFactory.class.getResourceAsStream( emProps.getProperty("Rabbit_Keystore") );

                try
                { connectToVerifiedAMQPHost( ksStream, userPass ); }
                catch ( Exception ex )
                { throw ex; }
            }
            else
            {
                // Might still try connect to the (unverified) AMQP server using SSL
                boolean useSSL = false;
                if ( emProps.containsKey("Rabbit_Use_SSL") )
                    useSSL = ( emProps.getProperty( "Rabbit_Use_SSL" ).equals("true") );
                
                try
                { 
                    if ( useSSL ) 
                        connectToAMQPSSLHost();
                    else
                      connectToAMQPHost();
                }
                catch ( Exception ex )
                { throw ex; }
            }
        }
        else
          throw new Exception( "Could not connect: IP/Port are invalid" ); 
    }

    public boolean isConnectionValid()
    { return (amqpConnection != null); }

    public AMQPBasicChannel createNewChannel() throws Exception
    {
        if ( amqpConnection == null ) throw new Exception( "No AMSQP connection available" );
        
        return new AMQPBasicChannel( amqpConnection.createChannel() );
    }
}
