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
import java.util.Properties;
import javax.net.ssl.*;

import com.rabbitmq.client.*;
import org.apache.log4j.Logger;




public class AMQPConnectionFactory
{
    private static Logger factoryLog = Logger.getLogger( AMQPConnectionFactory.class );

    private InetAddress amqpHostIP;
    private int         amqpPortNumber = 5672;
    private Connection  amqpConnection;
    private String userName = null;
    private String userPass = null;


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

    public boolean setAMQPHostPort( int port )
    {
        if ( port < 1 ) return false;

        amqpPortNumber = port;
        return true;
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
        amqpFactory.setPort( amqpPortNumber );
        if (userPass != null) {
            
            if (userName != null) {
                factoryLog.info("Will try to login as \'" + userName + "\'");
                amqpFactory.setUsername(userName);
            } else {
                factoryLog.info("Will try to login as guest");
                amqpFactory.setUsername("guest");
            }
            
            amqpFactory.setPassword(userPass);
        }

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
        amqpFactory.setPort( amqpPortNumber );
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

        char[] trustPassphrase = password.toCharArray();  
        KeyStore tks = KeyStore.getInstance( "JKS" );
        try { tks.load( keystore, trustPassphrase ); }
        catch ( Exception e )
        {
          factoryLog.error( "Had problems loading keystore: " + e.getMessage() );
          throw e;
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
        { throw new Exception( "Could not create secure AMQP host connection" ); }
    }
    
    public void connectToAMQPHost( Properties emProps ) throws Exception
    {
        if ( emProps == null ) throw new Exception( "EM properties are NULL" );
        
        // Get shared properties first
        String rabbitServerIP   = emProps.getProperty( "Rabbit_IP" );
        String rabbitServerPort = emProps.getProperty( "Rabbit_Port" );
        
        if (emProps.containsKey("password")) {
            factoryLog.info("Will be using password to connect to AMQP");
            userPass = emProps.getProperty("password");
        } else {
            factoryLog.info("No password provided, let's hope AMQP does not require authentication");
            
        }
        
        if (emProps.containsKey("username")) {
            userName = emProps.getProperty("username");
            factoryLog.info("Will be using username \'" + userName + "\' to connect to AMQP");
        } else {
            factoryLog.info("No username provided, let's hope AMQP has user \'guest\'");
            
        }
        
        factoryLog.info( "Trying to connect to AMQP bus..." );
        
        // Proceed only if these at least exist
        if ( rabbitServerIP != null && rabbitServerPort != null )
        {
            int portNumber = Integer.parseInt( rabbitServerPort );
            
            setAMQPHostIPAddress( rabbitServerIP );
            setAMQPHostPort( portNumber );
          
            // Now check to see if we're using a verified connection type
            if ( emProps.containsKey("Rabbit_Keystore") && 
                 emProps.containsKey("Rabbit_KeystorePassword") )
            {
                InputStream ksStream = AMQPConnectionFactory.class.getResourceAsStream( emProps.getProperty("Rabbit_Keystore") ); 
                String ksPassword    = emProps.getProperty( "Rabbit_KeystorePassword" );

                try
                { connectToVerifiedAMQPHost( ksStream, ksPassword ); }
                catch ( Exception e )
                { factoryLog.error( "Could not connect to AMQP Bus: " + e.getMessage() ); }
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
                catch ( Exception e )
                { 
                  factoryLog.error( "Could not connect to " +
                                    (useSSL ? "(SSL)" : "(insecure)") +
                                    "AMQP Bus: " + e.getMessage() );
                }
            }
        }
    }

    public boolean isConnectionValid()
    { return (amqpConnection != null); }

    public AMQPBasicChannel createNewChannel() throws Exception
    {
        if ( amqpConnection == null ) throw new Exception( "No AMSQP connection available" );

        return new AMQPBasicChannel( amqpConnection.createChannel() );
    }
}
