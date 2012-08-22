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
    public void testSaveExperiment()
    {
        ExperimentDataManager edm = new ExperimentDataManager();
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            fail ("Unable to get Experiment DAO");
        }
        
        Experiment exp = new Experiment();
        exp.setUUID(UUID.fromString("bfe4c710-61ba-46f8-a519-be2f7808192e"));
        exp.setName("Strawberry Experiment Extravagansa");
        exp.setDescription("A very boring description...");
        exp.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp.setEndTime(new Date());
        exp.setExperimentID("3543");
        try {
            expDAO.saveExperiment(exp);
        } catch (Exception ex) {
            fail("Unable to save experiment");
        }
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
        
        Experiment exp = null;
        try {
            //exp = expDAO.getExperiment(UUID.fromString("bfe4c710-61ba-46f8-a519-be2f7808192e"));
            exp = expDAO.getExperiment(UUID.fromString("791a3fa9-edd1-4f11-b7ce-22c06a47107f"));
        } catch (Exception ex) {
            fail("Unable to get experiment");
        }
        
        assertTrue(exp != null);
        assertTrue(exp.getUUID() != null);
        assertTrue(exp.getName() != null);
    }
}
