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
//      Created Date :          09-Apr-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.ecc.service.test.experiment;

import java.util.Date;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.co.soton.itinnovation.ecc.service.Application;
import uk.co.soton.itinnovation.ecc.service.domain.EccConfiguration;
import uk.co.soton.itinnovation.ecc.service.services.ConfigurationService;
import uk.co.soton.itinnovation.ecc.service.services.ExperimentService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // will reset everything after each test, comment this out if you want to test as singleton
public class ExperimentServiceTest {

    private EccConfiguration eccConfig;

    @Autowired
    ExperimentService expService;

    @Autowired
    ConfigurationService configurationService;

    public ExperimentServiceTest() {

    }

    @Before
    public void setUp() {
        Assert.assertTrue(configurationService.isInitialised());

        // Safer as doesn't depend on availability of http://config.experimedia.eu
        // Online can be a separate test
        eccConfig = configurationService.getLocalConfiguration();
        Assert.assertNotNull(eccConfig);

        configurationService.selectEccConfiguration(eccConfig);
        Assert.assertTrue(configurationService.isConfigurationSet());

        Assert.assertTrue(configurationService.startExperimentService());
    }

    @Test
    public void testStartStopExperiment() {
        try {

            Date expDate = new Date();
            String expName = "Test experiment " + expDate.toString();

            Assert.assertNull(expService.getActiveExperiment());

            expService.startExperiment("DefaultTest", expName, "JUnit test");

            // Check experiment meta-data
            Experiment activeExp = expService.getActiveExperiment();

            Assert.assertNotNull(activeExp);
            Assert.assertNotNull(activeExp.getUUID());
            Assert.assertNotNull(activeExp.getExperimentID());
            Assert.assertNotNull(activeExp.getName());
            Assert.assertNotNull(activeExp.getDescription());
            Assert.assertNotNull(activeExp.getStartTime());
            Assert.assertNull(activeExp.getEndTime());

            expService.stopExperiment();
            Assert.assertNotNull(activeExp.getEndTime()); // Local copy

            Assert.assertNull(expService.getActiveExperiment()); // Service copy

        } catch (Exception ex) {
            Assert.fail(ex.getMessage());
        }
    }

}
