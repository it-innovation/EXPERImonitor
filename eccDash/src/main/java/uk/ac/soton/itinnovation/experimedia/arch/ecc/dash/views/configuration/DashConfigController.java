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
//      Created By :            Dion Kitchener & Simon Crowle
//      Created Date :          29-Aug-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.configuration;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.configRegistry.api.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.SimpleView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;

import java.util.*;





public class DashConfigController implements DashConfigViewListener
{
  private final transient IECCLogger configLogger = Logger.getLogger( DashConfigController.class );
  
  private DashConfigView configView;
  private ConfigControllerListener configListener;
  private IECCDirectoryConfig dcf;
  private IECCProjectConfig   pcf;
  
  private Properties emProps;        // em.properties
  private Properties edmProps;       // edm.properties
  private Properties dashboardProps; // dashboard.properties
  
  // Online repository credentials here
  private String repositoryUsername = "experimedia";
  private String repositoryPassword = "ConfiG2013";
  
  // Root folder of the local configuration repository
  private String projectName;
  private String localConfigLocation;
 
  // Hash maps to store properties
  private Map<String,String>     configList = new HashMap<String, String>();
  private HashMap<String,String> emConfigProperties = new HashMap<String, String>();
  private HashMap<String,String> edmConfigProperties = new HashMap<String, String>();
  private HashMap<String,String> dashConfigProperties = new HashMap<String, String>();
          
  /**
   * Controller for the dashboard configuration component of the ECC Dashboard.
   * 
   * @param listener 
   */
  public DashConfigController( String localConfigPath,
                               ConfigControllerListener listener )
  {
    localConfigLocation = localConfigPath;
    configListener = listener;
 
    initialise();
  }
  
  /**
   * Implements a simple view to be used to build the dashboard configuration view.
   * 
   * @return - A configuration view.
   */
  public SimpleView getConfigView()
  {
    return configView;
  }
  
  /**
   * Retrieves EM properties.
   * 
   * @return - EM properties. 
   */
  public Properties getEMConfig()
  {
    // Client of this class wants the properties originally held in em.properties
    return emProps;
  }
  
  /**
   * Retrieves EDM properties
   * 
   * @return  - EDM properties.
   */
  public Properties getEDMConfig()
  {
    // Client of this class wants the properties originally held in edm.properties
    return edmProps;
  }
  
  /**
   * Retrieves dashboard configuration properties.
   * 
   * @return - Dashboard properties. 
   */
  public Properties getDashboardConfig()
  {
    // Client of this class wants the properties originally held in dashboard.properties
    return dashboardProps;
  }
  
  // Private methods -----------------------------------------------------------
  /**
   * Initialises a new dashboard configuration view instance.
   */
  private void initialise()
  {
    configView = new DashConfigView (this);
  }

  /**
   * Sets up a new project directories if they do not already exist, and accesses the 
   * configuration API.  Creates component feature directories if they do not exist.
   * 
   * @param projName        - The name of the project.
   * @param repoUsername    - The username of the online repository.
   * @param repoPassword    - The password of the online repository.
   * @throws Exception      - Throws if the API cannot be accessed or component features cannot be created
   */
  private void  initialiseProject( String projName, String repoUsername, String repoPassword ) throws Exception
  {
      // Safety first
      if ( projName == null || repoUsername == null || repoPassword == null )
      {
        String error = "Could not initialise project configuration - parameters invalid";
        
        configLogger.fatal( error );
        throw new Exception( error );
      }
      
      final String component = "ECC";
      projectName = projName;
    
      try
      {
           // Starts API and sets up project
          dcf = ECCConfigAPIFactory.getDirectoryConfigAccessor( projectName, repoUsername, repoPassword );
          pcf = ECCConfigAPIFactory.getProjectConfigAccessor( projectName, repoUsername, repoPassword );
          
          // Set up online directories for RabbitMQ, Database and Dashboard configuration
          pcf.createComponentFeature( component, "RabbitMQ" );
          pcf.createComponentFeature( component, "Dashboard" );
          pcf.createComponentFeature( component, "Database" );
          
          // Set up local directories for RabbitMQ, Database and Dashboard configuration
          pcf.createLocalComponentFeature( localConfigLocation, component, "RabbitMQ" );
          pcf.createLocalComponentFeature( localConfigLocation, component, "Dashboard" );
          pcf.createLocalComponentFeature( localConfigLocation,component, "Database" );
          
      }
      catch ( Exception ex )
      {
          // Pretty bad news here
          String error = "Failed to access ECC configuration service: " + ex.getMessage();
          
          throw new Exception( error );
      }     
  }
  
  /**
   * Method to send retrieved configuration data to the dashboard configuration view.
   * 
   * @param configList - A HashMap containing configuration data for all features.
   */
  private void sendDataToView ( Map<String, String> configList )
  {
     String monitorID =""; 
     String rabbitIP = "";
     String rabbitPort =""; 
     String rabbitKeystore=""; 
     String rabbitPassword=""; 
     boolean useRabbitSSL = false;
     String dbUrl="";
     String dbName="";
     String dbUsername="";
     String dbPassword="";
     String dbType="";
     String snapshotCount="";
     String nagiosUrl="";
      
      if( configList !=null )
      {          
          // iterate through both configs
          for (Map.Entry<String,String> entry : configList.entrySet())
          {
              String data = entry.getValue();
              
              // Use a GSON object to parse the JSON string to a Map.
              Map<String, String> config = new Gson().fromJson( data , new TypeToken<Map<String, String>>(){}.getType());
              
              // Iterate the map to extract the configuration data fields.
              for (Map.Entry<String,String> configEntry : config.entrySet())
              {
                  String configField = configEntry.getKey(); 
                  if ( configField.equals( "Monitor_ID" ) )
                  {
                      monitorID = configEntry.getValue();
                  }
                  if ( configField.equals( "Rabbit_IP" ) )
                  {
                      rabbitIP = configEntry.getValue();
                  }
                  if ( configField.equals( "Rabbit_Port" ) )
                  {
                      rabbitPort = configEntry.getValue();
                  }
                  if ( configField.equals( "Rabbit_Keystore" ) )
                  {
                      rabbitKeystore = configEntry.getValue();
                  }
                  if ( configField.equals( "Rabbit_KeystorePassword" ) )
                  {
                      rabbitPassword = configEntry.getValue();
                  }
                  if ( configField.equals( "Rabbit_Use_SSL" ) )
                  {
                      useRabbitSSL = Boolean.valueOf(configEntry.getValue() );
                  }
                  if ( configField.equals("dbURL" ))
                  {
                      dbUrl = configEntry.getValue();   
                  }
                  if( configField.equals( "dbName") )
                  {
                      dbName = configEntry.getValue();
                  }
                  if( configField.equals( "dbUsername" ))
                  {
                      dbUsername = configEntry.getValue();
                  }
                  if( configField.equals( "dbPassword" ))
                  {
                      dbPassword = configEntry.getValue();
                  }
                  if( configField.equals( "dbType" ))
                  {
                      dbType = configEntry.getValue();
                  }
                  if ( configField.equals( "livemonitor.defaultSnapshotCountMax" ))
                  {
                      snapshotCount = configEntry.getValue();
                  }
                  if( configField.equals( "nagios.fullurl" ) )
                  {
                      nagiosUrl = configEntry.getValue();
                  }
              }
              
          }
          // Send the configuration data to the view.
              configView.showConfig( 
                                  monitorID, 
                                  rabbitIP, 
                                  rabbitPort, 
                                  rabbitKeystore, 
                                  rabbitPassword, 
                                  useRabbitSSL,
                                  dbUrl,
                                  dbName,
                                  dbUsername,
                                  dbPassword,
                                  dbType,
                                  snapshotCount,
                                  nagiosUrl);
          
          // Tell the view to display the footer after the config panel
          configView.showConfigFooter( true );
          
      }
      
  }
  
  /**
   * Method called when configuration is complete which informs the main dash
   * controller that the configuration is complete.
   * 
   */
  private void configurationComplete()
  {
      if ( configDataIsOK() )
      {
           // Save configuration data to the properties objects
          emProps = new Properties();
          emProps.putAll( emConfigProperties );
          
          edmProps = new Properties();
          edmProps.putAll( edmConfigProperties );
          
          dashboardProps = new Properties();
          dashboardProps.putAll( dashConfigProperties );
          
          configListener.onConfigurationCompleted();
      }
  }
  
  /**
   * Saves configuration data to online and local repositories.
   * 
   * @param component   - The name of the component.
   * @param feature     - The name of the feature.
   * @param data        - The configuration as a string.
   * @throws Exception  - Throws when data cannot not be saved.
   */
  private void saveConfiguration( String component, 
                                  String feature, 
                                  String data ) throws Exception
  {
      if ( component == null || feature == null || data == null || data.isEmpty() )
      {
          String error = "Could not save configuration: parameters invalid";

          configLogger.error( error );
          throw new Exception( error );
      }
      
      // Try saving locally
      FileOutputStream fos = null;
      try
      {        
          String target = localConfigLocation + "/" + projectName + "/" + 
                          component + "/" + feature + "/config.json";
          
          // Need to manually write this document locally
          File file = new File( target );
          
          // Delete existing file
          if ( file.exists() ) file.delete();
          
          file.createNewFile();
          
          fos = new FileOutputStream( file );
          fos.write( data.getBytes() );
      }
      catch ( Exception e )
      {
          String error = "Could not write local configuration data: " + e.getMessage();
          configLogger.error( error );
      }
      finally
      { 
        if ( fos != null )
        {
          fos.flush();
          fos.close();
        }
      }
      
      // Now save remotely
      try
      {
          // Delete old file if it exists
          if ( pcf.componentFeatureConfigExists( component, feature ) )
            pcf.deleteComponentFeatureConfig( component, feature );
          
          // Write new file
          pcf.putComponentFeatureConfig( component, feature, data );
      }
      catch ( Exception e )
      {
          String error = "Could not save remove configuration data because : " + e.getMessage();
          configLogger.error( error );
      }
  }
  
  /**
   * Converts the data from a HashMap into a JSON string.
   * 
   * @param configProps - The configuration data as a HashMap.
   * @return - The configuration data as a string. 
   */
  private String configJsonString( HashMap<String,String> configProps )
  {
       // parse the received data into a JSON string
            StringBuilder sb = new StringBuilder( "{");  
            
               for (Map.Entry<String,String> entry : configProps.entrySet())
              {
                String configkey = entry.getKey();
                String configvalue = entry.getValue();

                sb.append( "\"" ).append( configkey ).append( "\"" );
                sb.append(":");
                sb.append( "\"" ).append( configvalue ).append( "\"" );
                sb.append( "," );
              }
            sb.deleteCharAt( sb.length()-1 ); 
            sb.append( "}" );
            
            String configData = sb.toString();
            
            return configData;
      
  }
  
  /**
   * Checks that all ECC configurations have data.
   * 
   * @return - True or false.
   */
  private boolean configDataIsOK()
  {
      if ( !emConfigProperties.isEmpty() && !edmConfigProperties.isEmpty() && !dashConfigProperties.isEmpty() )
      {
          return true;
      }
      else
      {
          return false;
      }    
  }
  
  // View listener methods------------------------------------------------------
  
    @Override
    public void onUpdateConfiguration( String monitorID, 
                                       String rabbitIP, 
                                       String rabbitPort, 
                                       String rabbitKeystore, 
                                       String rabbitPassword, 
                                       boolean useRabbitSSL,
                                       String dbUrl,
                                       String dbName,
                                       String dbUsername,
                                       String dbPassword,
                                       String dbType,
                                       String snapshotCount,
                                       String nagiosUrl) throws Exception
    {
            String rabbitSsl = String.valueOf( useRabbitSSL );
            
            emConfigProperties.put( "Monitor_ID", monitorID );
            emConfigProperties.put( "Rabbit_IP", rabbitIP );
            emConfigProperties.put( "Rabbit_Port" , rabbitPort );
            emConfigProperties.put( "Rabbit_Use_SSL" , rabbitSsl );
            
            // Ensure properties are written correctly for SSL options
            if ( useRabbitSSL )
            {
                emConfigProperties.put( "Rabbit_Keystore" , rabbitKeystore );
                emConfigProperties.put( "Rabbit_KeystorePassword" , rabbitPassword );
            }
            
            String emConfigString = configJsonString( emConfigProperties );
            saveConfiguration( "ECC" , "RabbitMQ", emConfigString );
            
            edmConfigProperties.put( "dbName", dbName );
            edmConfigProperties.put( "dbURL" , dbUrl );
            edmConfigProperties.put( "dbUsername" , dbUsername );
            edmConfigProperties.put( "dbPassword", dbPassword );
            edmConfigProperties.put( "dbType" , dbType );
            
            String edmConfigString = configJsonString( edmConfigProperties );
            saveConfiguration( "ECC" , "Database", edmConfigString );
            
            dashConfigProperties.put( "livemonitor.defaultSnapshotCountMax" , snapshotCount );
            dashConfigProperties.put( "nagios.fullurl", nagiosUrl );
            
            String dashConfigString = configJsonString( dashConfigProperties );
            saveConfiguration( "ECC" , "Dashboard", dashConfigString );
            
            configurationComplete();
    }

    @Override
    public void onFindConfigurations( String projectName)
    {
        boolean configServiceOK = false;
        
        try
        {
          initialiseProject( projectName, repositoryUsername , repositoryPassword );
          configServiceOK = true;
        }
        catch ( Exception ex )
        {
          configView.displayWarning( "Could not connect to EXPERIMEDIA configuration service",
                                     "If you have already configured the ECC, it may still be possible to continue." +
                                     " Otherwise, please contact the EXPERIMEDIA project team" );
        }
       
        if ( projectName != null )
        {
            final String component          = "ECC";
            final String featureRb          = "RabbitMQ";
            final String featureDB          = "Database";
            final String featureDash        = "Dashboard";
            final String localProjectTarget = localConfigLocation + projectName + "/" + component;
            
            String targetRabbitConfigData = null;
            String targetDatabaseConfigData = null;
            String targetDashboardConfigData = null;
            
            // Attempts to find local configuration data------------------------
            try
            {
                File file = new File( localProjectTarget + "/" + featureRb + "/config.json" );
                
                if ( file.exists() )
                    targetRabbitConfigData = pcf.getLocalComponentFeature( localConfigLocation, component, featureRb );
            }
            catch (Exception e)
            { configLogger.warn( "Could not find local Rabbit config data : " + e.getMessage() ); }
            
            try
            {
                File file = new File( localProjectTarget + "/" + featureDB + "/config.json" );
                
                if ( file.exists() )
                    targetDatabaseConfigData = pcf.getLocalComponentFeature( localConfigLocation, component, featureDB );
            }
            catch(Exception e)
            { configLogger.warn( "Could not find local Database config data : " + e.getMessage() ); }
            
            try
            {
                File file = new File( localProjectTarget + "/" + featureDash + "/config.json" );
                
                if ( file.exists() )
                      targetDashboardConfigData = pcf.getLocalComponentFeature( localConfigLocation, component, featureDash );
            }
            catch(Exception e)
            { configLogger.warn( "Could not find local dashboard config data : " + e.getMessage() ); }
            
            // If no local data, try the remote configuration server (if available )
            if ( configServiceOK && ( targetRabbitConfigData    == null ||
                                      targetDatabaseConfigData  == null ||
                                      targetDashboardConfigData == null ) )
            {       
                // Rabbit
                try
                {
                    if ( pcf.componentFeatureConfigExists( component , featureRb ) )
                        targetRabbitConfigData = pcf.getConfigData( component, featureRb );
                    else
                        targetRabbitConfigData = null; // TODO: FIX THIS: pcf.getDefaultConfigData( component , featureRb );
                }
                catch ( Exception e )
                { configLogger.fatal( "Could not find remote dashboard config data : " + e.getMessage() ); }
                
                // Database
                try
                {
                    if ( pcf.componentFeatureConfigExists( component , featureDB ) )
                        targetDatabaseConfigData = pcf.getConfigData( component, featureDB );
                    else
                        targetDatabaseConfigData = null; // TODO: FIX THIS: pcf.getDefaultConfigData( component , featureDB );
                }
                catch ( Exception e )
                { configLogger.fatal( "Could not find remote database config data: " + e.getMessage() ); }
                
                // Dashboard
                try
                {
                    if ( pcf.componentFeatureConfigExists( component , featureDash ) )
                        targetDashboardConfigData = pcf.getConfigData( component, featureDash );
                    else
                        targetDashboardConfigData = null; // TODO: FIX THIS: pcf.getDefaultConfigData( component , featureDash );
                }
                catch ( Exception e )
                { configLogger.fatal( "Could not find remote database config data: " + e.getMessage() ); }
            }
            
            // If we have config data, present to user, otherwise throw
            if ( targetRabbitConfigData    != null &&
                 targetDatabaseConfigData  != null &&
                 targetDashboardConfigData != null )
            {
                configList.put( featureRb,   targetRabbitConfigData    );
                configList.put( featureDB,   targetDatabaseConfigData  );
                configList.put( featureDash, targetDashboardConfigData );
                
                sendDataToView( configList );
            }
            else
            {
                String error = "Could not get sufficient configuration data to start ECC";
                configLogger.fatal( error );
                
                error = "Falling back on to local machine configuration as last resort";
                configLogger.info( error );
                
                fallbackToLocalMachineConfig();
            }
        }
   }
    
    // Private methods ---------------------------------------------------------
    private void fallbackToLocalMachineConfig()
    {
      configList.clear();
      
      final String featureRb   = "RabbitMQ";
      final String featureDB   = "Database";
      final String featureDash = "Dashboard";
      
      final String targetRabbitConfigData = "{\"Rabbit_Port\":\"5672\",\"Rabbit_Use_SSL\":\"false\",\"Rabbit_IP\":\"127.0.0.1\",\"Rabbit_Keystore\":\"/main/resources/rabbitKeyStore.jks\",\"Monitor_ID\":\"00000000-0000-0000-0000-000000000000\",\"Rabbit_KeystorePassword\":\"password\"}";
      final String targetDatabaseConfigData = "{\"dbPassword\":\"password\",\"dbName\":\"edm-metrics\",\"dbType\":\"postgresql\",\"dbURL\":\"localhost:5432\",\"dbUsername\":\"postgres\"}";
      final String targetDashboardConfigData = "{\"livemonitor.defaultSnapshotCountMax\":\"50\",\"nagios.fullurl\":\"http://username:password@host/nagios\"}";
      
      configList.put( featureRb,   targetRabbitConfigData    );
      configList.put( featureDB,   targetDatabaseConfigData  );
      configList.put( featureDash, targetDashboardConfigData );

      sendDataToView( configList );
    }
}
