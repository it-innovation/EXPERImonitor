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
//      Created Date :          23-Aug-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.soton.itinnovation.experimedia.arch.ecc.configRegistry.api;

/**
 * Interface to the API methods concerned with existing project configuration.
 * 
 */
public interface IECCProjectConfig {
    
    /**
     * Use this method to get the configuration info for named EXPERIMEDIA component feature.
     * For example, to get the ECC's RabbitMQ feature config data, request "ECC" and "RabbitMQ". 
     * If the configuration data exists on the repository, it will be returned as a JSON array of
     * configuration properties.
     * 
     * @param component - Name of the EXPERIMEDIA component required
     * @param feature   - Name of the component feature required.
     * @return          - The configuration data of the feature as a string or if empty "no_config"
     * 
     * @throws Exception - Throws when component and/or feature name is NULL or does not exist in repository.
     */
    String getConfigData( String component, String feature ) throws Exception;
    
    /**
     * Use this method to get the default configuration file for a named EXPERIMEDIA component feature.
     * For example, to get the ECC's RabbitMQ feature config data, request "ECC" and "RabbitMQ". 
     * If the configuration data exists on the repository, it will be returned as a JSON array of
     * configuration properties.
     * 
     * @param component - Name of the EXPERIMEDIA component required
     * @param feature   - Name of the component feature required.
     * @return          - The configuration data of the feature as a string or if empty "no_config"
     * 
     * @throws Exception - Throws when component and/or feature name is NULL.
    */
    String getDefaultConfigData ( String component, String feature ) throws Exception;
    
    
    /**
     * Use this method to find whether a configuration file exists for a specified component feature.
     * 
     * @param component - Name of the EXPERIMEDIA component required
     * @param feature   - Name of the component feature required.
     * @return          - True or false
     * @throws  Exception - Throws when component and/or feature name is NULL.
     */
    boolean componentFeatureConfigExists ( String component, String feature ) throws Exception;
    
    
    /**
     * Method used to create  locally stored directories for a component and feature in the
     * specified local repository.
     * 
     * @param localDirectoryPath - The drive and directory path of the repository
     * @param component - The named component.
     * @param feature   - The named feature.
     * @throws Exception - Throws if the component or feature is invalid.
     */
    void createLocalComponentFeature ( String localDirectoryPath, String component, String feature ) throws Exception;
    
    /**
     * Method to put a configuration file into the specified locally store component
     * feature directory.  Will create directories if they do not already exist. 
     * Will replace the file it already exists.
     * 
     * @param localDirectoryPath - The drive and directory path of the repository
     * @param component - The named component.
     * @param feature   - The named feature.
     * @param data      - The JSON string to be saved in the file.
     * 
     * @throws Exception - Throws if the component feature, or data is invalid. 
     */
    void putLocalComponentFeature( String localDirectoryPath, String component, String feature, String data) throws Exception;
    
    /**
     * Use this method to write configuration data for ECC specific feature. 
     * 
     * @param component     - Name of the EXPERIMEDIA component 
     * @param feature       - Name of the component feature
     * @param jsonConfig    - A JSON string containing the configuration data
     * 
     * @throws Exception    - Throws when component and/or feature name is NULL.
     */
    void putComponentFeatureConfig( String component, String feature, String jsonConfig ) throws Exception;
    
    /**
     * Use this method to write configuration data for ECC specific feature in the default configuration directory.
     * 
     * @param component     - Name of the EXPERIMEDIA component 
     * @param feature       - Name of the component feature
     * @param jsonConfig    - A JSON string containing the configuration data
     * 
     * @throws Exception    -Throws when component and/or feature name is NULL.
     */
    void putDefaultComponentFeature ( String component, String feature, String jsonConfig ) throws Exception; 
     
    
    /**
     * Use this method to delete a configuration file from a specified component feature
     * 
     * @param component     - Name of the EXPERIMEDIA component
     * @param feature       - Name of the component feature
     * 
     * @throws Exception    - Throws when component or feature is invalid. 
     */
    void deleteComponentFeatureConfig ( String component, String feature) throws Exception;
    
    /**
     * Use this method to retrieve a file stored on the local computer and convert that file to a string
     * 
     * @param filePath      - The drive, directory path, filename and extension of the file
     * @return              - The configuration data file as a string or if empty "no_config".
     * @throws Exception    - Throws when file does not exist.
     */
    String getLocalConfigFile(String filePath) throws Exception;
    
    /**
     * Use this method to retrieve file store in a local repository and convert that file to a string.
     * The method assumes that the local repository, components and features have been created. 
     * 
     * @param localDirectoryPath - The drive and directory path of the repository
     * @param component     - The component name.
     * @param feature       - The feature name.
     * @return              - The configuration file as a JSON string.
     * @throws Exception    - Throws when file does not exist.
     */
    String getLocalComponentFeature ( String localDirectoryPath, String component, String feature ) throws Exception;
    
    
    /**
     * Method used to return the project URL.
     * @return - The project URL
     *@throws Exception - Throws if project URl does not exist.
     */
    String getProjectUrl() throws Exception;
    
    /**
     * Method to check that a project exists in the repository.
     * The parameter projectUrl must include the entire URL including the repository,
     * for instance: "http://project.org/repository/projectname".
     * 
     * @param projectUrl     -  The entire URL of the project
     * @return               - True or false
     * @throws Exception     - Throws when the project URL is NULL.
     */
    boolean projectExists(String projectUrl)throws Exception;
    
    /**
     * Method to create both directories for a component and a feature if they do not
     * exist.
     * 
     * @param component - The component name.
     * @param feature   - The feature name.
     * @throws Exception - Throws if the component or the feature is invalid.
     */
    void createComponentFeature (String component, String feature) throws Exception;
    
    
    /**
     * Method to create both directories for a component and a feature if they do not
     * exist.
     * 
     * @param component - The component name.
     * @param feature   - The feature name.
     * @throws Exception - Throws if the component or the feature is invalid.
     */
    void createDefaultComponentFeature (String component, String feature) throws Exception;
    
}