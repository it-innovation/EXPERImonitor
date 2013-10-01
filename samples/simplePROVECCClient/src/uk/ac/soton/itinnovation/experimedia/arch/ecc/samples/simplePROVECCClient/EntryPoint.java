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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.loggin.impl.Log4JImpl;

import java.util.Properties;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.configRegistry.api.ECCConfigAPIFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.configRegistry.api.IECCProjectConfig;




public class EntryPoint
{
  private static IECCLogger clientLogger = Logger.getLogger( EntryPoint.class );
  
  public static void main( String args[] )
  {
    // Configure logging system
    Logger.setLoggerImpl( new Log4JImpl() );
    
    String error = null;
    
    if ( args.length == 1 && !args[0].isEmpty() )
    {
      String projectName = args[0];
      
      try
      {
        Properties eccProps = tryGetECCConfig( projectName );
        
        if ( eccProps != null )
        {
          ClientController cc = new ClientController();
          cc.initialise( eccProps );
        }
      }
      catch ( Exception ex )
      { error = "Could not initialise properly for project " + projectName; }
    }
    else error = "Need one argument, the project name ('gandalf' for example)";
    
    if ( error != null ) clientLogger.error( error );
  }
  
  private static Properties tryGetECCConfig( String projectName ) throws Exception
  {
    Properties eccProps = null;
    String setupError   = null;
    
    if ( projectName != null )
    {
      try
      {
        IECCProjectConfig projConfig = 
            ECCConfigAPIFactory.getProjectConfigAccessor( projectName, 
                                                          "experimedia",  // 'Pre-configured' for now
                                                          "ConfiG2013" ); // 'Pre-configured' for now
        final String comp    = "ECC";
        final String feature = "RabbitMQ";
        
        if ( projConfig.componentFeatureConfigExists(comp, feature) )
        {
          String rabbitJSON = projConfig.getConfigData(comp, feature);
          
          // The API doesn't offer data as properties; this needs to be added.
          // Return to this later
          eccProps = new Properties();
          eccProps.put( "Rabbit_IP"  , "127.0.0.1" );
          eccProps.put( "Rabbit_Port", "5672" );
          eccProps.put( "Monitor_ID" , "00000000-0000-0000-0000-000000000000" );
        }
        else setupError = "Cannot find ECC Rabbit configuration data!";
      }
      catch ( Exception ex )
      {

      }
    }
    else setupError = "Could not ECC config: project name is null";
    
    if ( setupError != null ) throw new Exception( setupError );
    
    return eccProps;
  }
}
