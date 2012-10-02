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
//      Created Date :          15-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.basicECCClient;

import java.util.UUID;
import org.apache.log4j.Logger;




public class EMClientContainer
{
    public static Logger clientLogger;

    public static void main( String args[] )
    {
        EMClientController ctrl = new EMClientController();

        try
        {
            String rabbitServerIP = "127.0.0.1";
            if ( args.length == 1 ) rabbitServerIP = args[0];     

            clientLogger = Logger.getLogger( EMClientContainer.class );

            ctrl.start( rabbitServerIP,
                        UUID.fromString("00000000-0000-0000-0000-000000000000"), // EM ID
                        UUID.randomUUID() );                                     // ID of this client
        }
        catch (Exception e )
        { clientLogger.error( "Had a problem connecting to the EM:\n" + e.getMessage() ); }
    }
}
