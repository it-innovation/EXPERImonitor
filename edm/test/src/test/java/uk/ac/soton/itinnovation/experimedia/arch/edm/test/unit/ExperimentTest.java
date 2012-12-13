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
package test.java.uk.ac.soton.itinnovation.experimedia.arch.edm.test.unit;

import java.util.Date;
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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.MonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IExperimentDAO;

@RunWith(JUnit4.class)
public class ExperimentTest extends TestCase
{
    IMonitoringEDM edm = null;
    IExperimentDAO expDAO = null;
    static Logger log = Logger.getLogger(ExperimentTest.class);
    
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ExperimentTest.class);

        log.info("EDM Experiment Test Complete");
        System.exit(0);
    }
    
    public ExperimentTest()
    {
        super();
    }
    
    @BeforeClass
    public static void beforeClass()
    {
        log.info("Experiment tests");
    }
    
    @Before
    public void beforeEachTest() throws Exception
    {
        edm = EDMInterfaceFactory.getMonitoringEDM();
        edm.clearMetricsDatabase();
        expDAO = edm.getExperimentDAO();
    }
    
    @Test
    public void testSaveExperiment_valid()
    {
        log.info(" - saving valid experiment");
        
        Experiment exp1 = new Experiment();
        exp1.setName("Test 1");
        exp1.setDescription("A very boring description...");
        exp1.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp1.setEndTime(new Date());
        exp1.setExperimentID("3543");
        try {
            expDAO.saveExperiment(exp1);
        } catch (Exception ex) {
            fail("Unable to save experiment");
        }
    }
    
    @Test
    public void testSaveExperiment_duplicateUUID()
    {
        log.info(" - saving experiment with duplicate UUID");
        
        Experiment exp2 = new Experiment();
        exp2.setName("Test 2");
        try {
            expDAO.saveExperiment(exp2);
        } catch (Exception ex) {
            fail("Unable to save experiment");
        }
        
        // should not save because of identical UUID
        Experiment exp3 = new Experiment();
        exp3.setUUID(exp2.getUUID());
        exp3.setName("Test 3");
        try {
            expDAO.saveExperiment(exp3);
            fail("Experiment should not have saved, it had a duplicate UUID");
        } catch (Exception ex) { }
    }
    
    @Test
    public void testSaveExperiment_noName()
    {
        log.info(" - saving experiment with no name");
        
        // should not save because of missinng information (UUID generated, but name not set)
        Experiment exp4 = new Experiment();
        try {
            expDAO.saveExperiment(exp4);
            fail("Experiment should not have saved, it did not have a name set");
        } catch (Exception ex) { }
    }
    
    @Test
    public void testGetExperiment()
    {
        log.info(" - saving and retrieving an experiment");
        
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
        log.info(" - saving and retrieving all experiments");
        
        Experiment exp1 = new Experiment();
        exp1.setName("Experiment 1");
        exp1.setDescription("A description...");
        exp1.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp1.setEndTime(new Date());
        exp1.setExperimentID("3543");
        try {
            expDAO.saveExperiment(exp1);
        } catch (Exception ex) {
            fail("Unable to save experiment");
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
            fail("Unable to save experiment");
        }
        
        Set<Experiment> experiments = null;
        
        try {
            experiments = expDAO.getExperiments(false);
        } catch (Exception ex) {
            fail("Unable to get experiments from DB");
        }
        
        assertNotNull("Experiment set returned from DB is NULL", experiments);
        assertTrue("Experiment set returned from DB should have contained 2 experiments, but contained " + experiments.size() + " experiment(s)", experiments.size() == 2);
    }
}
