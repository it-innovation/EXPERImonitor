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
  private String repositoryUsername = "rw";
  private String repositoryPassword = "test";
  
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
   * Gets the name of the current project configuration
   * 
   * @return - name of project
   */
  public String getProjectName()
  {
    return projectName;
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
    // Test to see if local configuration folder is available; create it if not
    if ( localConfigLocation != null )
      createFolder( localConfigLocation );
    else 
      configLogger.error( "Configuration location is null - cannot save configurations locally" );
    
    // Create configuration view
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
  private void initialiseProject( String projName, String repoUsername, String repoPassword ) throws Exception
  {
    // Safety first
    if ( projName == null || repoUsername == null || repoPassword == null )
    {
      String error = "Could not initialise project configuration - parameters invalid";

      configLogger.fatal( error );
      throw new Exception( error );
    }

    projectName = projName;
    
    // Create local configuration folders first
    createProjConfigFolders( projectName, "ECC", "RabbitMQ" );
    createProjConfigFolders( projectName, "ECC", "Database" );
    createProjConfigFolders( projectName, "ECC", "Dashboard" );

    // Now try accessing the configuration server
    try
    {
        // Starts API and sets up project
        pcf = ECCConfigAPIFactory.getProjectConfigAccessor( projectName, repoUsername, repoPassword );
        dcf = ECCConfigAPIFactory.getDirectoryConfigAccessor( projectName, repoUsername, repoPassword );

        // Set up online directories for RabbitMQ, Database and Dashboard configuration
        pcf.createComponentFeature( "ECC", "RabbitMQ" );
        pcf.createComponentFeature( "ECC", "Dashboard" );
        pcf.createComponentFeature( "ECC", "Database" );
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
     String  monitorID =""; 
     String  rabbitIP = "";
     String  rabbitPort ="";
     String  rabbitUsername ="";
     String  rabbitPassword=""; 
     String  rabbitKeystore="";
     boolean useRabbitSSL = false;
     String  dbUrl="";
     String  dbName="";
     String  dbUsername="";
     String  dbPassword="";
     String  dbType="";
     String  snapshotCount="";
     String  nagiosUrl="";
      
     if ( configList !=null )
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
              monitorID = configEntry.getValue();

            else if ( configField.equals( "Rabbit_IP" ) )
              rabbitIP = configEntry.getValue();

            else if ( configField.equals( "Rabbit_Port" ) )
              rabbitPort = configEntry.getValue();

            else if ( configField.equals( "Rabbit_Username" ) )
              rabbitUsername = configEntry.getValue();

            else if ( configField.equals( "Rabbit_Password" ) )
              rabbitPassword = configEntry.getValue();

            else if ( configField.equals( "Rabbit_Keystore" ) )
              rabbitKeystore = configEntry.getValue();

            else if ( configField.equals( "Rabbit_Use_SSL" ) )
              useRabbitSSL = Boolean.valueOf(configEntry.getValue() );

            else if ( configField.equals("dbURL" ))
              dbUrl = configEntry.getValue();   

            else if ( configField.equals( "dbName") )
              dbName = configEntry.getValue();

            else if ( configField.equals( "dbUsername" ))
              dbUsername = configEntry.getValue();

            else if ( configField.equals( "dbPassword" ))
              dbPassword = configEntry.getValue();

            else if ( configField.equals( "dbType" ))
              dbType = configEntry.getValue();

            else if ( configField.equals( "livemonitor.defaultSnapshotCountMax" ))
              snapshotCount = configEntry.getValue();

            else if ( configField.equals( "nagios.fullurl" ) )
              nagiosUrl = configEntry.getValue();
          } 
        }

        // Send the configuration data to the view.
        configView.showConfig( monitorID, rabbitIP, rabbitPort, 
                               rabbitUsername, rabbitPassword, 
                               rabbitKeystore, useRabbitSSL,
                               dbUrl, dbName, dbUsername, dbPassword, dbType,
                               snapshotCount, nagiosUrl );
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
                                  String data,
                                  boolean saveToServer ) throws Exception
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
          String error = "Could not save config locally: " + e.getMessage();
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
      if ( saveToServer )
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
            String error = "Could not save config to server: " + e.getMessage();
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
    public void onUpdateConfiguration( String  monitorID,
                                       String  rabbitIP, 
                                       String  rabbitPort,
                                       String  rabbitUsername,
                                       String  rabbitPassword,
                                       String  rabbitKeystore,
                                       boolean useRabbitSSL,
                                       String  dbUrl,
                                       String  dbName,
                                       String  dbUsername,
                                       String  dbPassword,
                                       String  dbType,
                                       String  snapshotCount,
                                       String  nagiosUrl )
    {
      try
      {
        String rabbitSsl = String.valueOf( useRabbitSSL );
            
        emConfigProperties.put( "Monitor_ID",      monitorID );
        emConfigProperties.put( "Rabbit_IP",       rabbitIP );
        emConfigProperties.put( "Rabbit_Port",     rabbitPort );
        emConfigProperties.put( "Rabbit_Username", rabbitUsername );
        emConfigProperties.put( "Rabbit_Password", rabbitPassword );
        emConfigProperties.put( "Rabbit_Use_SSL",  rabbitSsl );

        // Ensure properties are written correctly for SSL options
        if ( useRabbitSSL )
          emConfigProperties.put( "Rabbit_Keystore" , rabbitKeystore );

        String emConfigString = configJsonString( emConfigProperties );
        saveConfiguration( "ECC" , "RabbitMQ", emConfigString, true );

        edmConfigProperties.put( "dbName", dbName );
        edmConfigProperties.put( "dbURL" , dbUrl );
        edmConfigProperties.put( "dbUsername" , dbUsername );
        edmConfigProperties.put( "dbPassword", dbPassword );
        edmConfigProperties.put( "dbType" , dbType );

        String edmConfigString = configJsonString( edmConfigProperties );
        saveConfiguration( "ECC" , "Database", edmConfigString, true  );

        dashConfigProperties.put( "livemonitor.defaultSnapshotCountMax" , snapshotCount );
        dashConfigProperties.put( "nagios.fullurl", nagiosUrl );

        String dashConfigString = configJsonString( dashConfigProperties );
        
        saveConfiguration( "ECC" , "Dashboard", dashConfigString, true );

        configurationComplete();
      }
      catch ( Exception ex )
      {
        configLogger.error( "Could not update configuration:" + ex.getMessage() );
      }
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
            final String component   = "ECC";
            final String featureRb   = "RabbitMQ";
            final String featureDB   = "Database";
            final String featureDash = "Dashboard";
            
            // Attempts to find local configuration data------------------------
            String targetRabbitConfigData    = loadLocalConfigFeature( projectName, component, featureRb );
            String targetDatabaseConfigData  = loadLocalConfigFeature( projectName, component, featureDB );
            String targetDashboardConfigData = loadLocalConfigFeature( projectName, component, featureDash );
            
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
                        targetRabbitConfigData = pcf.getDefaultConfigData( component , featureRb );
                }
                catch ( Exception e )
                { configLogger.fatal( "Could not find remote dashboard config data: " + e.getMessage() ); }
                
                // Database
                try
                {
                    if ( pcf.componentFeatureConfigExists( component , featureDB ) )
                        targetDatabaseConfigData = pcf.getConfigData( component, featureDB );
                    else
                        targetDatabaseConfigData = pcf.getDefaultConfigData( component , featureDB );
                }
                catch ( Exception e )
                { configLogger.fatal( "Could not find remote database config data: " + e.getMessage() ); }
                
                // Dashboard
                try
                {
                    if ( pcf.componentFeatureConfigExists( component , featureDash ) )
                        targetDashboardConfigData = pcf.getConfigData( component, featureDash );
                    else
                        targetDashboardConfigData = pcf.getDefaultConfigData( component , featureDash );
                }
                catch ( Exception e )
                { configLogger.fatal( "Could not find remote database config data: " + e.getMessage() ); }
            }
            
            // Check for indications of poor configuration data
            boolean configOK = true;
            
            if ( targetRabbitConfigData    == null ||
                 targetDatabaseConfigData  == null ||
                 targetDashboardConfigData == null )
              configOK = false;
            
            else if ( targetRabbitConfigData.equals("no_config") ||
                      targetDatabaseConfigData.equals("no_config") ||
                      targetDashboardConfigData.equals("no_config") )
              configOK = false;
            
            // If we have config data, present to user, otherwise throw
            if ( configOK )
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
    private boolean createProjConfigFolders( String projectName,
                                             String compName,
                                             String feature )
    {
      // Test to see if local configuration folder is available; create it if not
      if ( localConfigLocation != null )
      {
        String configPath = localConfigLocation + "/" + projectName;
        
        if ( createFolder(configPath) )
        {
          configPath += "/" + compName;
          
          if ( createFolder(configPath) )
          {
            configPath += "/" + feature;
            
            if ( createFolder(configPath) )
              configLogger.info( "Create project config folder: " + configPath );
          }
        }
      }
      else configLogger.error( "Configuration location is null - cannot save configurations locally" );
      
      return false;
    }
    
    private String loadLocalConfigFeature( String projectName,
                                           String compName,
                                           String feature )
    {
      String configData = "";
      
      File confFile = new File( localConfigLocation + "/" + projectName + "/" + 
                                compName + "/" + feature + "/config.json" );
      
      if ( confFile.exists() )
      {
        try
        {
          BufferedReader br = new BufferedReader( new FileReader(confFile) );
          
          String confLine;
          do
          {
            confLine = br.readLine();
            
            if ( confLine != null ) configData += confLine;
            
          } while (confLine != null);
          
          br.close();
        }
        catch ( IOException ioe )
        { configLogger.error( "Error reading local configuration for: " + projectName ); }
      }
      
      // Invalidate data if nothing found
      if ( configData.length() == 0 ) configData = null;
      
      return configData;
    }
    
    private boolean createFolder( String folderPath )
    {
      boolean result = false;
      
      File folder = new File( folderPath );
      
      if ( folder.exists() && folder.isDirectory() )
        result = true;
      else
      {
        if ( folder.mkdir() )
          result = true;
        else
          configLogger.error( "Could not create config path: " + folderPath );
      }
      
      return result;
    }
    
    private void fallbackToLocalMachineConfig()
    {
      configList.clear();
      
      final String featureRb   = "RabbitMQ";
      final String featureDB   = "Database";
      final String featureDash = "Dashboard";
      
      final String targetRabbitConfigData = "{\"Rabbit_Use_SSL\":\"false\",\"Rabbit_Keystore\":\"/main/resources/rabbitKeyStore.jks\",\"Rabbit_IP\":\"127.0.0.1\",\"Rabbit_Port\":\"5672\",\"Rabbit_Password\":\"guest\",\"Rabbit_Username\":\"guest\",\"Monitor_ID\":\"00000000-0000-0000-0000-000000000000\"}";
      final String targetDatabaseConfigData = "{\"dbPassword\":\"password\",\"dbName\":\"edm-metrics\",\"dbType\":\"postgresql\",\"dbURL\":\"localhost:5432\",\"dbUsername\":\"postgres\"}";
      final String targetDashboardConfigData = "{\"livemonitor.defaultSnapshotCountMax\":\"20\",\"nagios.fullurl\":\"http://username:password@host/nagios\"}";
      
      configList.put( featureRb,   targetRabbitConfigData    );
      configList.put( featureDB,   targetDatabaseConfigData  );
      configList.put( featureDash, targetDashboardConfigData );

      sendDataToView( configList );
    }
}
