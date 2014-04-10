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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.co.soton.itinnovation.ecc.service.domain.EccConfiguration;
import uk.co.soton.itinnovation.ecc.service.services.ConfigurationService;
import uk.co.soton.itinnovation.ecc.service.services.ExperimentService;

public class ExperimentServiceTest {

    private EccConfiguration eccConfig;
    private ExperimentService expService;

    public ExperimentServiceTest() {

    }
    /*

     @Before
     public void setUp() {
     // Use a statically defined, local configuration for this test
     eccConfig = ConfigurationService.createDefaultConfiguration("DefaultTest");

     // Validate
     try {
     Assert.assertTrue(ConfigurationService.validateConfiguration(eccConfig));

     expService = new ExperimentService();
     } catch (Exception ex) {
     Assert.fail(ex.getMessage());
     }
     }

     @After
     public void tearDown() {
     try {
     expService.shutdown();
     } catch (Exception ex) {
     Assert.fail(ex.getMessage());
     }
     }

     @Test
     public void testServiceDefaultInitialisation() {
     // Validate
     try {
     //            expService.init( eccConfig );
     } catch (Exception ex) {
     Assert.fail(ex.getMessage());
     }
     }

     @Test
     public void testStartStopExperiment() {
     try {
     //            expService.init( eccConfig );

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

     expService.shutdown();
     } catch (Exception ex) {
     Assert.fail(ex.getMessage());
     }
     }
     */
}
