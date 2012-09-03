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
package uk.ac.soton.itinnovation.experimedia.arch.edm.test.v1;

import java.util.Date;
import java.util.Set;
import java.util.UUID;
import junit.framework.*;
import org.junit.Test;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.ExperimentDataManager;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IExperimentDAO;

public class ExperimentTest extends TestCase
{
    
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(ExperimentTest.class);

        System.out.println("EDM Experiment Test Complete");
        System.exit(0);
    }
    
    public ExperimentTest()
    {
        super();
    }
    
    @Test
    public void testSaveExperiment_valid()
    {
        ExperimentDataManager edm = new ExperimentDataManager();
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            fail ("Unable to get Experiment DAO");
        }
        
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
        assertTrue(true);
    }
    
    @Test
    public void testSaveExperiment_duplicateUUID()
    {
        ExperimentDataManager edm = new ExperimentDataManager();
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            fail ("Unable to get Experiment DAO");
        }
        
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
        ExperimentDataManager edm = new ExperimentDataManager();
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            fail ("Unable to get Experiment DAO");
        }
        
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
        ExperimentDataManager edm = new ExperimentDataManager();
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            fail ("Unable to get Experiment DAO");
        }
        
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
        
        Experiment exp = null;
        try {
            exp = expDAO.getExperiment(exp1.getUUID(), false);
        } catch (Exception ex) {
            fail("Unable to get experiment due to an exception: " + ex.getMessage());
        }
        
        assertNotNull(exp);
        assertNotNull(exp.getUUID());
        assertNotNull(exp.getName());
        assertNotNull(exp.getDescription());
        assertNotNull(exp.getStartTime());
        assertNotNull(exp.getEndTime());
        assertNotNull(exp.getExperimentID());
    }
    
    @Test
    public void testGetExperiments()
    {
        ExperimentDataManager edm = new ExperimentDataManager();
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            fail ("Unable to get Experiment DAO");
        }
        
        Set<Experiment> experiments = null;
        
        try {
            experiments = expDAO.getExperiments(false);
            
            assertNotNull(experiments);
            assertFalse(experiments.isEmpty());
        } catch (Exception ex) {
            fail("Unable to get experiments from DB");
        }
    }
}
