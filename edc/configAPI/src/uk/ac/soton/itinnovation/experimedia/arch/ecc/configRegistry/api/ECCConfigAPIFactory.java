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

/**
 * Factory class to create API interface instances.
 * 
 */
public class ECCConfigAPIFactory {
   
    private static final String repositoryURL = "http://config-experimedia/conf/";
    
    private ECCConfigAPIFactory(){}
    /**
     * Method to create a new instance of the directory configuration interface.
     * 
     * @param projectName   - Name of the project.
     * @param username      - Username of the project document repository.
     * @param password      - Password of the project document repository.
     * @return - Returns an instance of the director configuration interface.
     * @throws Exception 
     */
    public static IECCDirectoryConfig getDirectoryConfigAccessor( String  projectName,
                                       String  username, 
                                       String  password ) throws Exception
    {
        IECCDirectoryConfig config;
        
        try
        { 
            config = createImpl( projectName, username, password );
        }
        catch (Exception ex)
        { throw ex; }
        
        return config;
    }
    /**
     * Method to create a new instance of the project configuration interface.  
     * 
     * @param projectName   - Name of the project.
     * @param username      - Username of the project repository.
     * @param password      - Password of the project repository.
     * @return - Returns an instance of the project configuration interface.
     * @throws Exception 
     */
    public static IECCProjectConfig getProjectConfigAccessor( String  projectName,
                                       String  username, 
                                       String  password ) throws Exception
    {
        IECCProjectConfig config;
                
        try
        { 
            config = createImpl( projectName, username, password );
        }
        catch (Exception ex)
        { throw ex; }
        
        return config;
    }
    /**
     * Method creates a new instance of the method implementation class.
     * 
     * @param projectName   - Name of the project.
     * @param username      - Username of the project repository.
     * @param password      - Password of the project repository.
     * @return - Returns an instance of the method implementation class.
     * @throws Exception 
     */
    private static ECCConfigAPIImpl createImpl( String  projectName,
                                       String  username, 
                                       String  password ) throws Exception
    {
        ECCConfigAPIImpl newAPI = new ECCConfigAPIImpl();
        
        try
        {
            newAPI.configureAPI( projectName, repositoryURL, username, password );
        }
        catch (Exception ex) { throw ex; }
        
        return newAPI;
    }
}
