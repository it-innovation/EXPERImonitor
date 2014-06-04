/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
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
//      Created By :            Dion Kitchener
//      Created Date :          04-July-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.dynamicEntityDemoClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import static uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.dynamicEntityDemoClient.ECCClientContainer.clientLogger;




public class ECCClientContainer
{
	public static final Logger clientLogger = LoggerFactory.getLogger(ECCClientContainer.class);

    public static void main( String args[] )
    {
        ECCClientController ctrl = new ECCClientController();

        try
        {
            String rabbitServerIP = "10.0.0.10";
            if ( args.length == 1 ) rabbitServerIP = args[0];     

            ctrl.start( rabbitServerIP,
                        UUID.fromString("00000000-0000-0000-0000-000000000000"), // EM ID
                        UUID.randomUUID() );                                     // ID of this client
        }
        catch (Exception e )
        { clientLogger.error( "Had a problem connecting to the EM:\n" + e.getMessage() ); }
    }
}
