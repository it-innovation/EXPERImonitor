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
//      Created Date :          04-Oct-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.headerlessECCClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import java.io.InputStream;
import java.util.UUID;
import org.apache.log4j.Logger;




public class ECCHeaderlessClient
{
    private final Logger clientLogger = Logger.getLogger( ECCHeaderlessClient.class );
    
    private AMQPBasicChannel amqpChannel;
    
    
    public ECCHeaderlessClient()
    {
      
    }
    
    public void tryConnectToECC( String      rabbitServerIP,
                                 InputStream certificateResource,
                                 String      certificatePassword,
                                 UUID        monitorID,
                                 UUID        clientID ) throws Exception
    {
        // Safety first
        if ( rabbitServerIP == null ) throw new Exception( "IP parameter is invalid" );
        if ( monitorID == null || clientID == null ) throw new Exception( "ID parameter is null" );
        
        AMQPConnectionFactory amqpFactory = new AMQPConnectionFactory();
        amqpFactory.setAMQPHostIPAddress( rabbitServerIP );
        
        try
        {
            if ( certificateResource != null )
            {
                amqpFactory.connectToSecureAMQPHost( certificateResource,
                                                     certificatePassword );
                
                amqpChannel = amqpFactory.createNewChannel();
                
                clientLogger.info( "Connected to the ECC (using non-secured channel)" );
            }
            else
            {
                amqpFactory.connectToAMQPHost();
                amqpChannel = amqpFactory.createNewChannel();
                
                clientLogger.info( "Connected to the ECC (using non-secured channel)" );
            }
        }
        catch ( Exception e )
        { clientLogger.error( "Headerless client problem: could not connect to ECC" ); throw e; }
    }
}
