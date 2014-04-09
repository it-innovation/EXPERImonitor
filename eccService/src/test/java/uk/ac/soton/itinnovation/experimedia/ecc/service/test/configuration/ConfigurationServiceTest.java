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
//      Created Date :          08-Apr-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.ecc.service.test.configuration;

import org.junit.*;
import uk.co.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.EccConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.RabbitConfiguration;
import uk.co.soton.itinnovation.ecc.service.services.ConfigurationService;


public class ConfigurationServiceTest
{
    private ConfigurationService configService;
    
    public ConfigurationServiceTest()
    {
    }
    
    @Before
    public void setUp() throws Exception
    {
        configService = new ConfigurationService();
        configService.init( "rw", "test" );
    }
    
    @After
    public void tearDown() throws Exception
    {
        // No tear-down actions here
    }
    
    @Test
    public void testGetDefaultConfiguration()
    {
        Assert.assertEquals( true, configService.isServiceInitialised() );
        
        EccConfiguration ecc = configService.getConfiguration( "Default" );
        Assert.assertNotNull( ecc );
        
        try
        {
            Assert.assertTrue( ConfigurationService.validateConfiguration(ecc) );
        }
        catch ( Exception ex )
        { Assert.fail( ex.getMessage() ); }
    }
    
    @Test
    public void testUpdateConfiguration()
    {
        Assert.assertEquals( true, configService.isServiceInitialised() );
        
        // Create a default configuration
        EccConfiguration ecc = configService.getConfiguration( "Default" );
        Assert.assertNotNull( ecc );
        
        // Check, update and re-check the configuration
        try
        {
            // Validate
            Assert.assertTrue( ConfigurationService.validateConfiguration(ecc) );
            
            // Modify the configuration
            RabbitConfiguration rc = ecc.getRabbitConfig();
            rc.setIp( "127.0.0.1" );
            
            DatabaseConfiguration dc = ecc.getDatabaseConfig();
            dc.setUrl( "localhost:5432" );
            dc.setUserName( "postgres" );
            dc.setUserPassword( "password" );
            
            // Update using a DIFFERENT project name
            configService.updateConfiguration( "DefaultTest", ecc );
            
            // Retrieve and test the modified configuration
            EccConfiguration updatedConfig = configService.getConfiguration( "DefaultTest" );
            
            Assert.assertTrue( ConfigurationService.validateConfiguration(updatedConfig) );
            
            rc = updatedConfig.getRabbitConfig();
            Assert.assertEquals( "127.0.0.1", rc.getIp() );
            
            dc = updatedConfig.getDatabaseConfig();
            Assert.assertEquals( "localhost:5432", dc.getUrl() );
            Assert.assertEquals( "postgres",       dc.getUserName() );
            Assert.assertEquals( "password",       dc.getUserPassword() );
        }
        catch ( Exception ex )
        { Assert.fail( ex.getMessage() ); }
        
        // Retrieve the configuration & check updates
    }
}
