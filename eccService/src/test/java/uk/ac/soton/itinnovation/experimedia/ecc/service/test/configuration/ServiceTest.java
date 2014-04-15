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

import javax.xml.bind.ValidationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import uk.co.soton.itinnovation.ecc.service.services.ConfigurationService;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.co.soton.itinnovation.ecc.service.Application;
import uk.co.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.EccConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.RabbitConfiguration;
import uk.co.soton.itinnovation.ecc.service.utils.Validate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD) // will reset everything after each test, comment this out if you want to test as singleton
public class ServiceTest {

    @Autowired
    ConfigurationService configurationService;

    public ServiceTest() {
    }

    @Test
    public void testGetDefaultRemoteConfiguration() {
        Assert.assertEquals(true, configurationService.isInitialised());
        Assert.assertEquals(false, configurationService.isConfigurationSet());

        EccConfiguration defaultRemoteEccConfiguration = configurationService.getRemoteConfiguration("Default");
        Assert.assertNotNull(defaultRemoteEccConfiguration);

        try {
            Validate.eccConfiguration(defaultRemoteEccConfiguration);
        } catch (ValidationException ex) {
            Assert.fail(ex.getMessage());
        }

        Assert.assertEquals(true, configurationService.selectEccConfiguration(defaultRemoteEccConfiguration));
        Assert.assertEquals(true, configurationService.isConfigurationSet());
    }

    @Test
    public void testUpdateDefaultRemoteConfiguration() {
        Assert.assertEquals(true, configurationService.isInitialised());
        Assert.assertEquals(false, configurationService.isConfigurationSet());

        // Fetch default remote configuration
        EccConfiguration defaultEccConfiguration = configurationService.getRemoteConfiguration("Default");
        Assert.assertNotNull(defaultEccConfiguration);

        // Check, update and re-check the configuration
        try {
            // Validate
            Validate.eccConfiguration(defaultEccConfiguration);

            // Modify the configuration
            RabbitConfiguration rc = defaultEccConfiguration.getRabbitConfig();
            rc.setIp("127.0.0.1");

            DatabaseConfiguration dc = defaultEccConfiguration.getDatabaseConfig();
            dc.setUrl("localhost:5432");
            dc.setUserName("postgres");
            dc.setUserPassword("password");

            // Update using a DIFFERENT project name
            defaultEccConfiguration.setProjectName("DefaultTest");
            configurationService.updateRemoteConfiguration(defaultEccConfiguration);

            // Retrieve and test the modified configuration
            EccConfiguration updatedConfig = configurationService.getRemoteConfiguration("DefaultTest");

            Validate.eccConfiguration(updatedConfig);

            rc = updatedConfig.getRabbitConfig();
            Assert.assertEquals("127.0.0.1", rc.getIp());

            dc = updatedConfig.getDatabaseConfig();
            Assert.assertEquals("localhost:5432", dc.getUrl());
            Assert.assertEquals("postgres", dc.getUserName());
            Assert.assertEquals("password", dc.getUserPassword());
        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }

        // Retrieve the configuration & check updates
    }

}
