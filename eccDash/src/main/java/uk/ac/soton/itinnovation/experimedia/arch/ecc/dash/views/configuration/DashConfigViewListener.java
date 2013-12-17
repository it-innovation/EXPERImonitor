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
//      Created Date :          04-Sept-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////


package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.configuration;


public interface DashConfigViewListener 
{
    /**
     * Method to collect and update configuration data.
     * 
     * @param dashboardID
     * @param rabbitIP
     * @param rabbitPort
     * @param rabbitUsername
     * @param rabbitPassword
     * @param userRabbitSSL
     * @param dbUrl
     * @param dbName
     * @param dbUsername
     * @param dbPassword
     * @param dbType
     * @param snapshotCount
     * @param nagiosUrl
     */
    void onUpdateConfiguration( String  dashboardID, 
                                String  rabbitIP,
                                String  rabbitPort,
                                String  rabbitKeystore,
                                String  rabbitUsername,
                                String  rabbitPassword,
                                boolean userRabbitSSL,
                                String  dbUrl,
                                String  dbName,
                                String  dbUsername,
                                String  dbPassword,
                                String  dbType,
                                String  snapshotCount,
                                String  nagiosUrl );
   
    /**
    * Method to find out if a configuration data is available for a specified project.
    * If no existing configuration data is available the method will look in the
    * 'Default' directory for default configuration for each specified component feature.
    */
    void onFindConfigurations( String projectName );
}
