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
//      Created Date :          16-Aug-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////



package uk.ac.soton.itinnovation.experimedia.arch.ecc.configRegistry.api;

import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.commons.io.FileUtils;

/**
 * Implementation of the API interface methods.
 * 
 */
public class ECCConfigAPIImpl implements IECCDirectoryConfig,
                                         IECCProjectConfig {
    
    private String apiProjectName;
    private String apiUserName;
    private String apiPassword;
    private String apiRepositoryUrl;
    private String apiDefaultPath;
    private String apiProjectPath;
    private String apiLocalRepositoryPath;
    
    /**
     * Method to set up the parameters of a project and creates a new set of directories
     * if they do not exist already.
     * @param projectName
     * @param repositoryUrl
     * @param username
     * @param password
     * @throws Exception 
     */
    public void configureAPI( String  projectName,
                              String  repositoryUrl,
                              String  username, 
                              String  password ) throws Exception
    {
        String paramError = null;
       
        
        // Test all input parameters first and throw exceptions if required
        if(projectName == null)
        {
            paramError = "Project name is invalid";
        }
        else if(repositoryUrl == null)
        {
            paramError = "Project repository URL is invalid";
        }
        else if(username == null)
        {
            paramError = "Project username is invalid";
        }
        else if(password == null)
        {
            paramError = "Project password is invalid";
        }
            
        // If no parameter errors exist set up new project
        if ( paramError == null )
        {
            
            apiProjectName = projectName;
            apiUserName = username;
            apiPassword = password;
            apiRepositoryUrl = repositoryUrl;
            apiDefaultPath =  "" + apiRepositoryUrl + "Default";
            apiProjectPath = "" + apiRepositoryUrl + apiProjectName;
            
            // if the project directory already exists do not make another of the same
            if(!projectExists(apiProjectPath))
            {
                addDirectory(apiProjectName);  
            }
        }
        else
        {
            throw new Exception( paramError );
        }
        
        
    }
    
    // IECCProjectConfig -------------------------------------------------------
    @Override
    public String getConfigData( String component, String feature ) throws Exception
    {
        if(component == null)
        {
            throw new Exception("Component is invalid");
        }
        else if(feature == null)
        {
            throw new Exception("Feature is not valid");
        }
        else
        {
          
            String componentUrl = "" + getProjectUrl() + "/" + component + "/" + feature + "/config.json";

            String configResource = "";

            if(projectExists(componentUrl))
            {
                Sardine method = SardineFactory.begin(apiUserName, apiPassword);
                try 
                {
                    InputStream is = method.get(componentUrl);
                    BufferedReader i = new BufferedReader(new InputStreamReader(is, "8859_1") );
                    int numBytes;
                    char[] buff = new char[512];

                    while((numBytes = i.read(buff))!=-1)
                    {
                        configResource += String.copyValueOf(buff, 0, numBytes);
                    }
                }
                catch ( IOException ioe )
                {
                    String error = "Could not get config data because: " + ioe.getMessage();
                    throw new Exception( error, ioe );
                }
            }
            else
            {
                return "no_config";
            }

            return configResource;
        }
    }
    
    @Override
     public String getDefaultConfigData ( String component, String feature ) throws Exception
     {
          if(component == null)
        {
            throw new Exception("Component is invalid");
        }
        else if(feature == null)
        {
            throw new Exception("Feature is not valid");
        }
        else
        {
          
            String componentUrl = "" + apiDefaultPath + "/" + component + "/" + feature + "/config.json";

            String configResource = "";

            if(projectExists(componentUrl))
            {
                Sardine method = SardineFactory.begin(apiUserName, apiPassword);
                try 
                {
                    InputStream is = method.get(componentUrl);
                    BufferedReader i = new BufferedReader(new InputStreamReader(is, "8859_1") );
                    int numBytes;
                    char[] buff = new char[512];

                    while((numBytes = i.read(buff))!=-1)
                    {
                        configResource += String.copyValueOf(buff, 0, numBytes);
                    }
                }
                catch ( IOException ioe )
                {
                    String error = "Could not get config data because: " + ioe.getMessage();
                    throw new Exception( error, ioe );
                }
            }
            else
            {
                return "no_config";
            }

            return configResource;
        }
         
     }
    
    @Override
    public void putComponentFeatureConfig( String component, String feature, String jsonConfig ) throws Exception
    {
        if(component == null)
        {
            throw new Exception("Component is invalid");
        }
        else if(feature == null)
        {
            throw new Exception("Feature is not valid");
        }
        else if(jsonConfig == null)
        {
            throw new Exception("The JSON data string is invalid");
        }
        else
        {   
            String configUrl = "" + getProjectUrl() + "/" + component + "/" + feature + "/config.json";
            
            if(!projectExists(configUrl))
                {
                     Sardine method = SardineFactory.begin(apiUserName, apiPassword);
                     
                     try
                     {
                         byte[] data = jsonConfig.getBytes();
                         method.put(configUrl, data);
                     }
                     catch(IOException ioe)
                     {
                         String error = "Could not put config data because: " + ioe.getMessage();
                         throw new Exception( error, ioe );
                     }
                }
            else
            {
                throw new Exception("The configuration data file already exists");
            }
        }

    }

    @Override
    public void putDefaultComponentFeature ( String component, String feature, String jsonConfig ) throws Exception
    {
        if(component == null)
        {
            throw new Exception("Component is invalid");
        }
        else if(feature == null)
        {
            throw new Exception("Feature is not valid");
        }
        else if(jsonConfig == null)
        {
            throw new Exception("The JSON data string is invalid");
        }
        else
        {   
            String configUrl = "" + apiDefaultPath + "/" + component + "/" + feature + "/config.json";
            Sardine method = SardineFactory.begin(apiUserName, apiPassword);
            
            if(!projectExists(configUrl))
                {
                    
                    
                    if(!projectExists( apiDefaultPath ))
                    {
                        addDirectory( "Default" );
                    }
                     
                     try
                     {
                         byte[] data = jsonConfig.getBytes();
                         method.put(configUrl, data);
                     }
                     catch(IOException ioe)
                     {
                         String error = "Could not put config data because: " + ioe.getMessage();
                         throw new Exception( error, ioe );
                     }
                }
            else
            {
                throw new Exception("The configuration data file already exists");
            }
        }

        
    }
    
    @Override
    public void deleteComponentFeatureConfig ( String component, String feature) throws Exception
    {
        if(component == null)
        {
            throw new Exception("Component is invalid");
        }
        else if(feature == null)
        {
            throw new Exception("Feature is not valid");
        }
        else
        {   
            String configUrl = "" + getProjectUrl() + "/" + component + "/" + feature + "/config.json";
            
            if(projectExists(configUrl))
            {
                try 
                {
                    Sardine method = SardineFactory.begin(apiUserName, apiPassword);
                    method.delete( configUrl );
                
                 } 
                catch (IOException ioe) 
                {
                     String error = "Could not delete the document in the repository because : " + ioe.getMessage();
                     throw new Exception( error, ioe );
                }   
            }
        }
    }
    
    @Override
    public boolean componentFeatureConfigExists ( String component, String feature ) throws Exception
    {
        if(component == null)
        {
            throw new Exception("Component is invalid");
        }
        else if(feature == null)
        {
            throw new Exception("Feature is not valid");
        }
        else
        {
          
            String componentUrl = "" + getProjectUrl() + "/" + component + "/" + feature + "/config.json";

            if(projectExists(componentUrl))
            {
                return true;
            }
            else
            {
               return false; 
            }
        
        }
        
    }
    
    
    @Override
    public void createLocalComponentFeature ( String localDirectoryPath, String component, String feature ) throws Exception
    {
        if( localDirectoryPath.isEmpty() || component.isEmpty() || feature.isEmpty())
        {
            throw new Exception("The component or feature given is not valid");
        }
        else
        {
            String localConfigFilepath = "" + localDirectoryPath + "/" + apiProjectName + "/" +component + "/" + feature + "/";
            
            try
            {
                File dir = new File( localConfigFilepath );
                dir.mkdirs();
                
            }
            catch(Exception ex)
            {
                String error = "Could get local component feature because : " + ex.getMessage();
                throw new Exception( error, ex );
            }
        }
    }
    
    @Override
    public void putLocalComponentFeature( String localDirectoryPath, String component, String feature, String data) throws Exception
    {
        try
        {
            String localConfigFile = "" + localDirectoryPath + "/" + apiProjectName + "/" + component + "/" + feature + "/" + "config.json";
            FileUtils.writeStringToFile(new File( localConfigFile ), data);
            
        }
        catch(Exception ioe)
        {
            String error = "Could save the local component feature because : " + ioe.getMessage();
            throw new Exception( error, ioe );
            
        }

       
    }
    
    
    @Override
    public String getLocalComponentFeature ( String localDirectoryPath, String component, String feature ) throws Exception
    {
        if( component.isEmpty() || feature.isEmpty())
        {
            throw new Exception("The component or feature is not valid");
        }
        else
        {
            String localConfigFilepath = "" + localDirectoryPath + "/" + apiProjectName +"/" + component + "/" + feature + "/" + "config.json";
            
            try
            {
                String localConfig = getLocalConfigFile( localConfigFilepath );
                return localConfig;
            }
            catch(Exception ex)
            {
                String error = "Could get local component feature because : " + ex.getMessage();
                throw new Exception( error, ex );

            }
        }
    }
    
    
    @Override
    public String getLocalConfigFile(String filePath) throws Exception
    {
        if(filePath == null)
        {
            throw new Exception("The filepath is not valid");
        }
        else
        {
            File configFile = new File(filePath);
            String configFileString = FileUtils.readFileToString(configFile);
            if( configFileString.isEmpty() )
            {
                return "no_config";
            }
            else
            {
                return configFileString;
            }
            
        }
      
    }

    @Override
    public String getProjectUrl() throws Exception
    {
        String projectUrl = "" + apiRepositoryUrl + apiProjectName;
       
        if(projectUrl == null)
        {
           throw new Exception("No project URL has been set"); 
        }
        return projectUrl; 
       
    }
    
    @Override
    public void createComponentFeature (String component, String feature) throws Exception
    {
        if(component == null)
        {
            throw new Exception("The component is invalid");
        }
        else if(feature == null)
        {
            throw new Exception("The feature is invalid");
        }
        else
        {   
            String componentUrl = "" + getProjectUrl() + "/" + component + "/";
            String componentFeatureUrl = "" + getProjectUrl() + "/" + component + "/" + feature + "/";
            
            if(!projectExists(componentFeatureUrl))
            {
                Sardine method =  SardineFactory.begin(apiUserName, apiPassword);
                try
               {
                   if(!projectExists(componentUrl))
                   {
                       method.createDirectory(componentUrl);
                   }   
                   
                   method.createDirectory( componentFeatureUrl );                   
               }
               catch (IOException ioe)
               {
                    String error = "Could add a new component feature because : " + ioe.getMessage();
                    throw new Exception( error, ioe );
               }
                
            }
            
        }
        
    }
    
    @Override
    public void createDefaultComponentFeature (String component, String feature) throws Exception
    {
        if(component == null)
        {
            throw new Exception("The component is invalid");
        }
        else if(feature == null)
        {
            throw new Exception("The feature is invalid");
        }
        else
        {   
            String componentUrl = "" + apiDefaultPath + "/" + component + "/";
            String componentFeatureUrl = "" + apiDefaultPath + "/" + component + "/" + feature + "/";
            
            if(!projectExists(componentFeatureUrl))
            {
                Sardine method =  SardineFactory.begin(apiUserName, apiPassword);
                try
               {
                   if(!projectExists(componentUrl))
                   {
                       method.createDirectory(componentUrl);
                   }   
                   
                   method.createDirectory( componentFeatureUrl );                   
               }
               catch (IOException ioe)
               {
                    String error = "Could add a new component feature because : " + ioe.getMessage();
                    throw new Exception( error, ioe );
               }
                
            }
            
        }
        
    }
    
    // IECCDirectoryConfig -----------------------------------------------------------
    
    @Override
    public void setLocalConfigPath(String localConfigPath) throws Exception
    {
        if(localConfigPath != null)
        {
            apiLocalRepositoryPath = localConfigPath;
        }
        else
        {
            throw new Exception("The local configuration path is invalid");
        }
    }
    
    @Override
    public String getLocalConfigPath() throws Exception
    {
        if(apiLocalRepositoryPath != null)
        {
            String localPath = apiLocalRepositoryPath;
            return localPath;
        }
        else
        {
            throw new Exception("The local configuration path is invalid");

        }
    }
    
    @Override
    public void createLocalRepository(String directoryPath) throws Exception
    {
        if(directoryPath == null)
        {
            throw new Exception("The directory path is invalid");
        }
        else
        {
            File folder = new File(directoryPath);
            folder.mkdirs();
            
        }
    }
    
    @Override
    public void createLocalDirectory(String directoryName) throws Exception
    {
        if(directoryName == null)
        {
            throw new Exception("The local configuration path is invalid");
        }
        else
        {
            try
            {
                String directoryPath = "" + getLocalConfigPath() + "/" + directoryName + "/";
                File folder = new File(directoryPath);
                folder.mkdirs();
            }
            catch(IOException ioe)
            {
                String error = "Could add a new directory because : " + ioe.getMessage();
                throw new Exception( error, ioe );
            }
        }
       
    }
    
    
    @Override
    public void addDirectory(String directoryName) throws Exception
    {
        if(directoryName == null)
        {
            throw new Exception("The directory name is invalid");
        }
        else
        {
        
            try {

                 String directoryUrl = "" + apiRepositoryUrl + directoryName + "/";

                 if(!projectExists(directoryUrl))
                 {
                     Sardine method = SardineFactory.begin(apiUserName, apiPassword);
                     method.createDirectory(directoryUrl);
                 }
                 else
                 {
                     throw new Exception("A directory with this name already exists");
                 }

            } 
            catch (IOException ioe) 
            {
                String error = "Could add a new directory because : " + ioe.getMessage();
                throw new Exception( error, ioe );
            }
        }
    }


    @Override
    public void deleteDirectory(String directoryName)throws Exception 
    {
        if(directoryName == null)
        {
            throw new Exception("The directory name is invalid");
        }
        else
        {
            try {

                 String directoryUrl = "" + apiRepositoryUrl + directoryName + "/";

                 if(projectExists(directoryUrl))
                 {
                    Sardine method = SardineFactory.begin(apiUserName, apiPassword);
                    method.delete(directoryUrl); 
                 }
                 else
                 {
                     throw new Exception("This directory does not exist");
                 }


            } 
            catch (IOException ioe) 
            {
                String error = "Could not delete the directory because : " + ioe.getMessage();
                throw new Exception( error, ioe );
            }
        }
}

   
    @Override
    public String getDocument(String filePath) throws Exception
    {
        
        if(filePath == null)
        {
            throw new Exception("The file path is invalid");
        }
        else
        {
           
            try {
                 String fileURL = "" + apiRepositoryUrl + filePath;

                 if(projectExists(fileURL))
                 {
                     Sardine method = SardineFactory.begin(apiUserName, apiPassword);
                     InputStream is = method.get(fileURL);
                     BufferedReader i = new BufferedReader(new InputStreamReader(is, "8859_1"));

                     String resource = "";
                     int numBytes;
                     char[] buff = new char[512];

                     while((numBytes = i.read(buff))!=-1)
                     {
                        resource += String.copyValueOf(buff, 0, numBytes);
                     }
                     return resource;
                     }
                 else
                 {
                     throw new Exception("The project configuration file does not exist");
                 }

             } 
            catch (IOException ioe) 
            {
                 String error = "Could not retrieve the document because : " + ioe.getMessage();
                 throw new Exception( error, ioe );
            }
             
        }
   }
    
   
    @Override
    public void putDocument(String sourceFilePath, String destinationFilePath )throws Exception 
    {
        
        if(sourceFilePath == null)
        {
            throw new Exception("The source file path is invalid");
        }
        else if(destinationFilePath == null)
        {
            throw new Exception("The destination file path is invalid");
        }
        else
        {
            String fileUrl = "" + apiRepositoryUrl + destinationFilePath;
            try {
                if(!projectExists(fileUrl))
                {
                     Sardine method = SardineFactory.begin(apiUserName, apiPassword);
                     byte[] data = FileUtils.readFileToByteArray(new File(sourceFilePath));
                     method.put(fileUrl, data);
                }
                else
                {
                    throw new Exception("The configuration document exists");
                }

            } 
            catch (IOException ioe) 
            {
                String error = "Could not upload the document to the repository because : " + ioe.getMessage();
                throw new Exception( error, ioe );
            }
        }
    }
    
    
    @Override
    public void deleteDocument(String filePath)throws Exception 
    {
        
        if(filePath == null)
        {
            throw new Exception("The filepath is not vaild");
        }
        else
        {
            try {
                String fileUrl = "" + apiRepositoryUrl + filePath;

                if(projectExists(fileUrl))
                {
                    Sardine method = SardineFactory.begin(apiUserName, apiPassword);
                    method.delete(fileUrl);
                }
            } 
            catch (IOException ioe) 
            {
                 String error = "Could not delete the document in the repository because : " + ioe.getMessage();
                 throw new Exception( error, ioe );
            }
      }
    }
    
   
    @Override
    public void moveDocument(String sourceFilePath, String destinationFilePath) throws Exception
    {
        if(sourceFilePath == null)
        {
            throw new Exception("The source filepath is not valid");
        }
        else if (destinationFilePath == null)
        {
            throw new Exception("The destination filepath is not valid");
        }
        else
        {
            try {
                 String sourceUrl = "" + apiRepositoryUrl + sourceFilePath;
                 String destinationUrl = "" + apiRepositoryUrl + destinationFilePath;

                 if(projectExists(sourceUrl))
                 {
                     Sardine method = SardineFactory.begin(apiUserName, apiPassword);
                     method.move(sourceUrl, destinationUrl); 
                 }

            } 
            catch (IOException ioe) 
            {
                 String error = "Could not move the document because : " + ioe.getMessage();
                 throw new Exception( error, ioe );
            }
        }
    }

    
    @Override
    public void copyDocument(String sourceFilePath, String destinationFilePath) throws Exception
    {
        
        if(sourceFilePath == null)
        {
            throw new Exception("The source filepath is not valid");
        }
        else if (destinationFilePath == null)
        {
            throw new Exception("The destination filepath is not valid");
        }
        else
        {
        try {
             String sourceUrl = "" + apiRepositoryUrl + sourceFilePath;
             String destinationUrl = "" + apiRepositoryUrl + destinationFilePath;
             
             
             if(projectExists(sourceUrl))
             {
                 Sardine method = SardineFactory.begin(apiUserName, apiPassword);
                 method.copy(sourceUrl, destinationUrl);
             } 
        } 
        catch (IOException ioe) 
        {
            String error = "Could not copy the document because : " + ioe.getMessage();
            throw new Exception( error, ioe );
        }
    }
 }
    

    @Override
    public boolean projectExists(String projectUrl)throws Exception
    {
        if(projectUrl == null)
        {
            throw new Exception("The project URL is not valid");
        }
        else
        {
            Sardine method = SardineFactory.begin(apiUserName, apiPassword);

            try {
                if(method.exists(projectUrl))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            } 
            catch (IOException ioe) 
            {
                String error = "Could not check if the project URL exists because : " + ioe.getMessage();
                throw new Exception( error, ioe );
            }
        }
    }
   
    @Override
    public boolean documentExists(String filePath)throws Exception 
    {
         
        if(filePath == null)
        {
            throw new Exception("The file path is not valid");
        }
        else
        {
            String fileUrl = "" + apiRepositoryUrl + filePath;
            Sardine method = SardineFactory.begin(apiUserName, apiPassword);

            try 
            {
                if(method.exists(fileUrl))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            } 
            catch (IOException ioe) 
            {
                String error = "Could not check if the document exists because : " + ioe.getMessage();
                throw new Exception( error, ioe );
            }
        }
    }
}

   

