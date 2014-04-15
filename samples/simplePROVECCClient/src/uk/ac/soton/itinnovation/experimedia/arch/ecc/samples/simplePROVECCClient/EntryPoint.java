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
//      Created By :            Simon Crowle
//      Created Date :          27-Sep-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.simplePROVECCClient;

import java.util.Properties;




public class EntryPoint
{
    public static void main( String args[] )
    {
        // Create a configuration for connection to a locally hosted ECC
        Properties eccProps = new Properties();
        eccProps.put( "Rabbit_IP"  , "127.0.0.1" );
        eccProps.put( "Rabbit_Port", "5672" );
        eccProps.put( "Rabbit_Username", "guest" );
        eccProps.put( "Rabbit_Password", "guest" );
        eccProps.put( "Monitor_ID" , "00000000-0000-0000-0000-000000000000" );

        // Create controller and initialise
        ClientController cc = new ClientController();
        cc.initialise( eccProps );
    }
}
