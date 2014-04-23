/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2014
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
//      Created Date :          22-Apr-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.co.soton.itinnovation.ecc.service.domain;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




public class PROVDatabaseConfiguration
{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private Properties provProps;
    
    private String repoTemplate;
    private String sesameServerURL;
    private String repositoryID;
    private String repositoryName;
    private String ontPrefix;
    private String ontBaseURI;
    

    public PROVDatabaseConfiguration()
    {
        createDefaultConfiguration();
    }
    
    public PROVDatabaseConfiguration( String template, String ssURL,
                                      String repID, String repName, 
                                      String prefix, String baseURI )
    {  
        repoTemplate = template;
        sesameServerURL = ssURL;
        repositoryID = repID;
        repositoryName = repName;
        ontPrefix = prefix;
        ontBaseURI = baseURI;
        
        provProps = new Properties();
        provProps.put( "owlim.repoTemplate", repoTemplate );
        provProps.put( "owlim.sesameServerURL", sesameServerURL );
        provProps.put( "owlim.repositoryID", repositoryID );
        provProps.put( "owlim.repositoryName", repositoryName );
        provProps.put( "ont.Prefix", ontPrefix );
        provProps.put( "ont.BaseURI", ontBaseURI );
    }
    
    public Properties getPROVRepoProperties()
    {
        return provProps;
    }
    
    // Private methods ---------------------------------------------------------
    private void createDefaultConfiguration()
    {
        provProps = new Properties();
        
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is = cl.getResourceAsStream( "prov.properties" );
        
        try
        {
            provProps.load( is );
        }
        catch ( IOException ioex )
        {
            logger.error( "Could not create default PROV configuration properties" );
        }
    }
}
