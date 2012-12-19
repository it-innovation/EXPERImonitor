/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2012
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
//      Created By :            Vegard Engen
//      Created Date :          2012-08-13
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.edm.test.unit;

import java.util.Date;
import java.util.Properties;
import java.util.Set;
import junit.framework.*;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IExperimentDAO;

@RunWith(JUnit4.class)
public class ExperimentTest extends TestCase
{
    IMonitoringEDM edm = null;
    IExperimentDAO expDAO = null;
    static Logger log = Logger.getLogger(ExperimentTest.class);
    
    @BeforeClass
    public static void beforeClass()
    {
        log.info("Experiment tests executing...");
    }
    
    @Before
    public void beforeEachTest()
    {
        try {
            Properties prop = getProperties();
            edm = EDMInterfaceFactory.getMonitoringEDM(prop);
            edm.clearMetricsDatabase();
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            log.error("Failed to set up EDM and get Experiment DAO: " + ex.toString());
        }
    }
    
    public Properties getProperties()
    {
        Properties prop = new Properties();
        
        try {
            prop.load(AGeneralTest.class.getClassLoader().getResourceAsStream(EDMTestSuite.propertiesFile));
        } catch (Exception ex) {
            log.error("Error with loading configuration file " + EDMTestSuite.propertiesFile + ": " + ex.getMessage(), ex);
            return null;
        }
        
        return prop;
    }
    
    @Test
    public void testSaveExperiment_valid_full()
    {
        log.debug(" - saving valid experiment");
        
        if ((edm == null) || (expDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        Experiment exp = new Experiment();
        exp.setName("Test experiment");
        exp.setDescription("A very boring description...");
        exp.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp.setEndTime(new Date());
        exp.setExperimentID("3543");
        try {
            expDAO.saveExperiment(exp);
        } catch (Exception ex) {
            fail("Unable to save experiment: " + ex.toString());
        }
    }
    
    @Test
    public void testSaveExperiment_valid_minimal()
    {
        log.debug(" - saving valid experiment (with minimal info)");
        
        if ((edm == null) || (expDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        Experiment exp = new Experiment();
        exp.setName("Test experiment");
        try {
            expDAO.saveExperiment(exp);
        } catch (Exception ex) {
            fail("Unable to save experiment: " + ex.toString());
        }
    }
    
    @Test
    public void testSaveExperiment_duplicateUUID()
    {
        log.debug(" - saving experiment with duplicate UUID");
        
        if ((edm == null) || (expDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        Experiment exp1 = new Experiment();
        exp1.setName("Test experiment");
        try {
            expDAO.saveExperiment(exp1);
        } catch (Exception ex) {
            fail("Unable to save experiment: " + ex.toString());
        }
        
        // should not save because of identical UUID
        Experiment exp2 = new Experiment();
        exp2.setUUID(exp1.getUUID());
        exp2.setName("Duplicate experiment");
        try {
            expDAO.saveExperiment(exp2);
            fail("Experiment should not have saved, it had a duplicate UUID");
        } catch (Exception ex) { }
    }
    
    @Test
    public void testSaveExperiment_noName()
    {
        log.debug(" - saving experiment with no name");
        
        if ((edm == null) || (expDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        // should not save because of missinng information (UUID generated, but name not set)
        Experiment exp4 = new Experiment();
        try {
            expDAO.saveExperiment(exp4);
            fail("Experiment should not have saved, it did not have a name set");
        } catch (Exception ex) { }
    }
    
    @Test
    public void testGetExperimentByUUID()
    {
        log.debug(" - saving and retrieving an experiment by UUID");
        
        if ((edm == null) || (expDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        Experiment exp1 = new Experiment();
        exp1.setName("Experiment");
        exp1.setDescription("A description...");
        exp1.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp1.setEndTime(new Date());
        exp1.setExperimentID("3543");
        try {
            expDAO.saveExperiment(exp1);
        } catch (Exception ex) {
            fail("Unable to save experiment: " + ex.toString());
        }
        
        Experiment exp = null;
        try {
            exp = expDAO.getExperiment(exp1.getUUID(), false);
        } catch (Exception ex) {
            fail("Unable to get experiment due to an exception: " + ex.getMessage());
        }
        
        assertNotNull("Experiment tretrieved from the DB is NULL", exp);
        assertNotNull("Experiment tretrieved from the DB doesn't have the UUID set", exp.getUUID());
        assertNotNull("Experiment tretrieved from the DB doesn't have the name set", exp.getName());
        assertNotNull("Experiment tretrieved from the DB doesn't have the description set", exp.getDescription());
        assertNotNull("Experiment tretrieved from the DB doesn't have the start time set", exp.getStartTime());
        assertNotNull("Experiment tretrieved from the DB doesn't have the end time set", exp.getEndTime());
        assertNotNull("Experiment tretrieved from the DB doesn't have the experiment ID set", exp.getExperimentID());
    }
    
    @Test
    public void testGetExperiments()
    {
        log.debug(" - saving and retrieving all experiments");
        
        if ((edm == null) || (expDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        Experiment exp1 = new Experiment();
        exp1.setName("Experiment 1");
        exp1.setDescription("A description...");
        exp1.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp1.setEndTime(new Date());
        exp1.setExperimentID("3543");
        try {
            expDAO.saveExperiment(exp1);
        } catch (Exception ex) {
            fail("Unable to save experiment: " + ex.toString());
        }
        
        Experiment exp2 = new Experiment();
        exp2.setName("Experiment 2");
        exp2.setDescription("A description...");
        exp2.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp2.setEndTime(new Date());
        exp2.setExperimentID("5740");
        try {
            expDAO.saveExperiment(exp2);
        } catch (Exception ex) {
            fail("Unable to save experiment: " + ex.toString());
        }
        
        Set<Experiment> experiments = null;
        
        try {
            experiments = expDAO.getExperiments(false);
        } catch (Exception ex) {
            fail("Unable to get experiments from DB: " + ex.toString());
        }
        
        assertNotNull("Experiment set returned from DB is NULL", experiments);
        assertTrue("Experiment set returned from DB should have contained 2 experiments, but contained " + experiments.size() + " experiment(s)", experiments.size() == 2);
    }
}
