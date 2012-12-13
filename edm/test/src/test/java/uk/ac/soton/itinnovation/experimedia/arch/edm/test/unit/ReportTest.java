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
//      Created Date :          2012-12-13
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.edm.test.unit;

import java.util.Date;
import java.util.Random;
import java.util.UUID;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.NoDataException;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IReportDAO;
import uk.ac.soton.itinnovation.experimedia.arch.edm.test.general.PopulateDB;

/**
 *
 * @author Vegard Engen
 */
@RunWith(JUnit4.class)
public class ReportTest extends TestCase
{
    IMonitoringEDM edm = null;
    IReportDAO reportDAO = null;
    static Logger log = Logger.getLogger(ReportTest.class);
    
    @BeforeClass
    public static void beforeClass()
    {
        log.info("Report tests");
    }
    
    @Before
    public void beforeEachTest() throws Exception
    {
        edm = EDMInterfaceFactory.getMonitoringEDM();
        edm.clearMetricsDatabase();
        reportDAO = edm.getReportDAO();
        
        // save experiment, entities, metric generators, metric groups and measurement sets
        PopulateDB.saveExperiment(edm, PopulateDB.expUUID);
        PopulateDB.saveEntity1(edm, PopulateDB.entity1UUID, PopulateDB.entity1attribute1UUID, PopulateDB.entity1attribute2UUID, PopulateDB.entity1attribute3UUID);
        PopulateDB.saveMetricGenerator1(edm, PopulateDB.expUUID, PopulateDB.entity1UUID, PopulateDB.entity1attribute1UUID, PopulateDB.entity1attribute2UUID, PopulateDB.entity1attribute3UUID, PopulateDB.mGen1UUID, PopulateDB.mGrp1UUID, PopulateDB.mGrp1mSet1UUID, PopulateDB.mGrp1mSet2UUID, PopulateDB.mGrp1mSet3UUID);
    }
    
    private static Report getReportWithRandomMeasurements(UUID reportUUID, UUID mSetUUID, int numMeasurements)
    {
        MeasurementSet mSet = new MeasurementSet(mSetUUID);
        Random rand = new Random();
        rand.setSeed(new Date().getTime());
        long timeStampFrom = 0;
        long timeStampTo = 0;
        for (int i = 0; i < numMeasurements; i++)
        {
            long timeStamp = new Date().getTime() - (1000 * (numMeasurements-i));
            Measurement measurement = new Measurement(UUID.randomUUID(), mSetUUID, new Date(timeStamp), String.valueOf(rand.nextInt(500)));
            //Measurement measurement = new Measurement(UUID.randomUUID(), mSetUUID, new Date(timeStamp), null);
            mSet.addMeasurement(measurement);
            
            if (i == 0)
            {
                timeStampFrom = timeStamp;
                timeStampTo = timeStamp;
            }
            else
            {
                if (timeStamp > timeStampTo) {
                    timeStampTo = timeStamp;
                } else if (timeStamp < timeStampFrom) {
                    timeStampFrom = timeStamp;
                }
            }
        }
        
        Report report = new Report(reportUUID, mSet, new Date(), new Date(timeStampFrom), new Date(timeStampTo), mSet.getMeasurements().size());
        return report;
    }

    @Test
    public void testSaveReport_validNoMeasurements()
    {
        log.info(" - saving report without measurements");
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveReport(report, false);
        } catch (Exception ex) {
            fail("Unable to save Report without measurements: " + ex.getMessage());
        }
        
        // try to get report from the DB and check that it has the measurements
        Report reportDB = null;
        try {
            reportDB = reportDAO.getReport(report.getUUID(), false);
            
            assertNotNull("Report instance from the DB is NULL - should have just been saved with measurements", reportDB);
            assertNotNull("MeasurementSet of Report instance from the DB is NULL - should have just been saved with measurements", reportDB.getMeasurementSet());
            assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL - should have just been saved with measurements", reportDB.getMeasurementSet());
            assertTrue("Report returned from DB should have contained 0 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().isEmpty());
        } catch (Exception ex) {
            fail("Unable to get Report that was just saved with measurements: " + ex.getMessage());
        }
    }
    
    @Test
    public void testSaveReport_validWithMeasurements()
    {
        log.info(" - saving report with measurements");
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveReport(report, true);
        } catch (Exception ex) {
            fail("Unable to save Report with measurements: " + ex.getMessage());
        }
        
        // try to get report from the DB and check that it has the measurements
        Report reportDB = null;
        try {
            reportDB = reportDAO.getReport(report.getUUID(), true);
            
            assertNotNull("Report instance from the DB is NULL - should have just been saved with measurements", reportDB);
            assertNotNull("MeasurementSet of Report instance from the DB is NULL - should have just been saved with measurements", reportDB.getMeasurementSet());
            assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL - should have just been saved with measurements", reportDB.getMeasurementSet());
            assertTrue("Report returned from DB should have contained 5 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().size() == 5);
        } catch (Exception ex) {
            fail("Unable to get Report that was just saved with measurements: " + ex.getMessage());
        }
    }
    
    @Test
    public void testSaveReport_validOnlyMeasurements()
    {
        log.info(" - saving measurements for report");
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        // try to get report from the DB (shouldn't be there)
        Report reportDB = null;
        try {
            reportDB = reportDAO.getReport(report.getUUID(), true);
            fail("Got a report that shouldn't have been saved (when saving measurements only)");
        } catch (Exception ex) {}
    }
    
    @Test
    public void testSetReportMeasurementsSyncFlag()
    {
        
    }
    
    @Test
    public void testDeleteReport()
    {
        
    }
    
    @Test
    public void testGetReport_byUUID_withoutMeasurements()
    {
        
    }
    
    @Test
    public void testGetReport_byUUID_withMeasurements()
    {
        
    }
    
    @Test
    public void testGetReport_all_withoutMeasurements()
    {
        
    }
    
    @Test
    public void testGetReport_all_withMeasurements()
    {
        
    }
    
    @Test
    public void testGetReport_UnsyncedMeasurementsFromDate_withoutMeasurements()
    {
        
    }
    
    @Test
    public void testGetReport_UnsyncedMeasurementsFromDate_withMeasurements()
    {
        
    }
}
