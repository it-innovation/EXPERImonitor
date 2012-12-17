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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;
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
        log.info("Report tests beginning");
    }
    
    @Before
    public void beforeEachTest()
    {
        try {
            edm = EDMInterfaceFactory.getMonitoringEDM();
            edm.clearMetricsDatabase();
            reportDAO = edm.getReportDAO();

            // save experiment, entities, metric generators, metric groups and measurement sets
            PopulateDB.saveExperiment(edm, PopulateDB.expUUID);
            PopulateDB.saveEntity1(edm, PopulateDB.entity1UUID, PopulateDB.entity1attribute1UUID, PopulateDB.entity1attribute2UUID, PopulateDB.entity1attribute3UUID);
            PopulateDB.saveMetricGenerator1(edm, PopulateDB.expUUID, PopulateDB.entity1UUID, PopulateDB.entity1attribute1UUID, PopulateDB.entity1attribute2UUID, PopulateDB.entity1attribute3UUID, PopulateDB.mGen1UUID, PopulateDB.mGrp1UUID, PopulateDB.mGrp1mSet1UUID, PopulateDB.mGrp1mSet2UUID, PopulateDB.mGrp1mSet3UUID);
        } catch (Exception ex) {
            log.error("Unable to set up the EDM and populate the DB with required data before starting Report tests");
        }
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
            long timeStamp = new Date().getTime() - (10 * (numMeasurements-i));
            Measurement measurement = new Measurement(UUID.randomUUID(), mSetUUID, new Date(timeStamp), String.valueOf(rand.nextInt(500)));
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
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
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
            assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL - should have just been saved with measurements", reportDB.getMeasurementSet().getMeasurements());
            assertTrue("Report returned from DB should have contained 0 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().isEmpty());
        } catch (Exception ex) {
            fail("Unable to get Report that was just saved with measurements: " + ex.getMessage());
        }
    }
    
    @Test
    public void testSaveReport_validWithMeasurements()
    {
        log.info(" - saving report with measurements");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
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
            assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL - should have just been saved with measurements", reportDB.getMeasurementSet().getMeasurements());
            assertTrue("Report returned from DB should have contained 5 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().size() == 5);
        } catch (Exception ex) {
            fail("Unable to get Report that was just saved with measurements: " + ex.getMessage());
        }
    }
    
    @Test
    public void testSaveReport_validOnlyMeasurements()
    {
        log.info(" - saving measurements for report");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        
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
        log.info(" - setting sync flag for measurements for report");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveReport(report, true);
        } catch (Exception ex) {
            fail("Unable to save report (1) with measurements: " + ex.getMessage());
        }
        
        Report report2 = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveReport(report2, true);
        } catch (Exception ex) {
            fail("Unable to save report (2) with measurements: " + ex.getMessage());
        }
        
        // setting synch flag
        try {
            reportDAO.setReportMeasurementsSyncFlag(report.getUUID(), true);
        } catch (Exception ex) {
            fail("Unable to set sync flag for measurements for report: " + ex.getMessage());
        }
        
        Report reportDB = null;
        try {
            reportDB = reportDAO.getReport(report.getUUID(), true);    
        } catch (Exception ex) {
            fail("Unable to get a report that should have been saved with measurements, that's being tested for setting sync flag to true: " + ex.toString());
        }
        
        assertNotNull("Report instance from the DB is NULL - should have just been saved with measurements and sync flags set to true", reportDB);
        assertNotNull("MeasurementSet of Report instance from the DB is NULL - should have just been saved with measurements and sync flags set to true", reportDB.getMeasurementSet());
        assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL - should have just been saved with measurements and sync flags set to true", reportDB.getMeasurementSet().getMeasurements());
        assertTrue("Report returned from DB should have contained 5 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().size() == 5);
        
        for (Measurement measurement : reportDB.getMeasurementSet().getMeasurements())
        {
            assertTrue("A measurement (" + measurement.getUUID() + ") for report has not been flagged as synchronised: " + measurement.isSynchronised(), measurement.isSynchronised());
        }
        
        Report reportDB2 = null;
        try {
            reportDB2 = reportDAO.getReportForAllMeasurements(PopulateDB.mGrp1mSet1UUID, true);
        } catch (Exception ex) {
            fail("Unable to get a report for all measurements: " + ex.toString());
        }
        
        assertNotNull("Report instance for all measurements from the DB is NULL", reportDB2);
        assertNotNull("MeasurementSet of Report instance for all measurements from the DB is NULL", reportDB2.getMeasurementSet());
        assertNotNull("Set of Measurement in the MeasurementSet of Report instance for all measurements from the DB is NULL", reportDB2.getMeasurementSet().getMeasurements());
        assertTrue("Report for all measurements returned from DB should have contained 10 measurements, but contained " + reportDB2.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB2.getMeasurementSet().getMeasurements().size() == 10);
        
        int numSynced = 0;
        for (Measurement measurement : reportDB.getMeasurementSet().getMeasurements())
        {
            if(measurement.isSynchronised()) {
                numSynced++;
            }
        }
        
        assertTrue("Report for all measurements returned from the DB should have contained 5 synchronised measurements, but contained " + numSynced, numSynced == 5);
    }
    
    @Test
    public void testGetReport_byUUID_withoutMeasurements()
    {
        log.info(" - getting report by UUID without the measurements");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveReport(report, true);
        } catch (Exception ex) {
            fail("Unable to save report with measurements: " + ex.getMessage());
        }
        
        Report reportDB = null;
        try {
            reportDB = reportDAO.getReport(report.getUUID(), false);
        } catch (Exception ex) {
            fail("Unable to get report from DB without measurements: " + ex.getMessage());
        }
        
        assertNotNull("Report instance from the DB is NULL", reportDB);
        assertNotNull("MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet());
        assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet().getMeasurements());
        assertTrue("Report returned from DB should have contained 0 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().isEmpty());
    }
    
    @Test
    public void testGetReport_byUUID_withMeasurements()
    {
        log.info(" - getting report by UUID with measurements");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveReport(report, true);
        } catch (Exception ex) {
            fail("Unable to save report with measurements: " + ex.getMessage());
        }
        
        Report reportDB = null;
        try {
            reportDB = reportDAO.getReport(report.getUUID(), true);
        } catch (Exception ex) {
            fail("Unable to get report from DB with measurements: " + ex.getMessage());
        }
        
        assertNotNull("Report instance from the DB is NULL", reportDB);
        assertNotNull("MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet());
        assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet().getMeasurements());
        assertTrue("Report returned from DB should have contained 5 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().size() == 5);
    }
    
    @Test
    public void testGetReport_latest_withoutMeasurements()
    {
        log.info(" - getting report for latest measurement, but without the actual measurement");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        Report reportDB = null;
        try {
            reportDB = reportDAO.getReportForLatestMeasurement(PopulateDB.mGrp1mSet1UUID, false);
        } catch (Exception ex) {
            fail("Unable to get report from DB with measurements: " + ex.getMessage());
        }
        
        assertNotNull("Report instance from the DB is NULL", reportDB);
        assertNotNull("MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet());
        assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet().getMeasurements());
        assertTrue("Report returned from DB should have contained 0 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().isEmpty());
        assertNotNull("Report returned from DB should have indicated 1 measurements, but variable is NULL", reportDB.getNumberOfMeasurements());
        assertTrue("Report returned from DB should have indicated 1 measurements, but got " + reportDB.getNumberOfMeasurements(), reportDB.getNumberOfMeasurements() == 1);
    }
    
    @Test
    public void testGetReport_latest_withMeasurements()
    {
        log.info(" - getting report for latest measurement with the actual measurement");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        Report reportDB = null;
        try {
            reportDB = reportDAO.getReportForLatestMeasurement(PopulateDB.mGrp1mSet1UUID, true);
        } catch (Exception ex) {
            fail("Unable to get report from DB with measurements: " + ex.getMessage());
        }
        
        assertNotNull("Report instance from the DB is NULL", reportDB);
        assertNotNull("MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet());
        assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet().getMeasurements());
        assertTrue("Report returned from DB should have contained 1 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().size() == 1);
        assertNotNull("Report returned from DB should have indicated 1 measurements, but variable is NULL", reportDB.getNumberOfMeasurements());
        assertTrue("Report returned from DB should have indicated 1 measurements, but got " + reportDB.getNumberOfMeasurements(), reportDB.getNumberOfMeasurements() == 1);
    }
    
    @Test
    public void testGetReport_all_withoutMeasurements()
    {
        log.info(" - getting report for all measurements, but without the actual measurements");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        Report reportDB = null;
        try {
            reportDB = reportDAO.getReportForAllMeasurements(PopulateDB.mGrp1mSet1UUID, false);
        } catch (Exception ex) {
            fail("Unable to get report from DB with measurements: " + ex.getMessage());
        }
        
        assertNotNull("Report instance from the DB is NULL", reportDB);
        assertNotNull("MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet());
        assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet().getMeasurements());
        assertTrue("Report returned from DB should have contained 0 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().isEmpty());
        assertNotNull("Report returned from DB should have indicated 10 measurements, but variable is NULL", reportDB.getNumberOfMeasurements());
        assertTrue("Report returned from DB should have indicated 10 measurements, but got " + reportDB.getNumberOfMeasurements(), reportDB.getNumberOfMeasurements() == 10);
    }
    
    @Test
    public void testGetReport_all_withMeasurements()
    {
        log.info(" - getting report for all measurements, with the actual measurements");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        Report reportDB = null;
        try {
            reportDB = reportDAO.getReportForAllMeasurements(PopulateDB.mGrp1mSet1UUID, true);
        } catch (Exception ex) {
            fail("Unable to get report from DB with measurements: " + ex.getMessage());
        }
        
        assertNotNull("Report instance from the DB is NULL", reportDB);
        assertNotNull("MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet());
        assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet().getMeasurements());
        assertNotNull("Report returned from DB should have indicated 10 measurements, but variable is NULL", reportDB.getNumberOfMeasurements());
        assertTrue("Report returned from DB should have indicated 10 measurements, but got " + reportDB.getNumberOfMeasurements(), reportDB.getNumberOfMeasurements() == 10);
        assertTrue("Report returned from DB should have contained 10 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().size() == 10);
    }
    
    @Test
    public void testGetReport_fromDate_withoutMeasurements()
    {
        log.info(" - getting report for measurements after date, without the actual measurements");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        try { Thread.sleep(100); } catch (InterruptedException ex) { }
        
        report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        Date fromDate = report.getFromDate();
        
        try { Thread.sleep(100); } catch (InterruptedException ex) { }
        
        report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        Report reportDB = null;
        try {
            reportDB = reportDAO.getReportForMeasurementsFromDate(PopulateDB.mGrp1mSet1UUID, fromDate, false);
        } catch (Exception ex) {
            fail("Unable to get report from DB from date " + fromDate + ", without measurements: " + ex.getMessage());
        }
        
        assertNotNull("Report instance from the DB is NULL", reportDB);
        assertNotNull("MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet());
        assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet().getMeasurements());
        assertNotNull("Report returned from DB should have indicated 10 measurements, but variable is NULL", reportDB.getNumberOfMeasurements());
        assertTrue("Report returned from DB should have indicated 10 measurements, but got " + reportDB.getNumberOfMeasurements(), reportDB.getNumberOfMeasurements() == 10);
        assertTrue("Report returned from DB should have contained 0 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().isEmpty());
    }
    
    @Test
    public void testGetReport_fromDate_withMeasurements()
    {
        log.info(" - getting report for measurements after date, with the actual measurements");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        try { Thread.sleep(100); } catch (InterruptedException ex) { }
        
        report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        Date fromDate = report.getFromDate();
        
        try { Thread.sleep(100); } catch (InterruptedException ex) { }
        
        report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        Report reportDB = null;
        try {
            reportDB = reportDAO.getReportForMeasurementsFromDate(PopulateDB.mGrp1mSet1UUID, fromDate, true);
        } catch (Exception ex) {
            fail("Unable to get report from DB from date " + fromDate + ", with measurements: " + ex.getMessage());
        }
        
        assertNotNull("Report instance from the DB is NULL", reportDB);
        assertNotNull("MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet());
        assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet().getMeasurements());
        assertNotNull("Report returned from DB should have indicated 10 measurements, but variable is NULL", reportDB.getNumberOfMeasurements());
        assertTrue("Report returned from DB should have indicated 10 measurements, but got " + reportDB.getNumberOfMeasurements(), reportDB.getNumberOfMeasurements() == 10);
        assertTrue("Report returned from DB should have contained 10 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().size() == 10);
    }
    
    @Test
    public void testGetReport_forTimePeriod_withoutMeasurements()
    {
        log.info(" - getting report for measurements with a given time period, without the actual measurements");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        try { Thread.sleep(100); } catch (InterruptedException ex) { }
        
        report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        Date fromDate = report.getFromDate();
        Date toDate = report.getToDate();
        
        try { Thread.sleep(100); } catch (InterruptedException ex) { }
        
        report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        Report reportDB = null;
        try {
            reportDB = reportDAO.getReportForMeasurementsForTimePeriod(PopulateDB.mGrp1mSet1UUID, fromDate, toDate, false);
        } catch (Exception ex) {
            fail("Unable to get report from DB from date " + fromDate + ", without measurements: " + ex.getMessage());
        }
        
        assertNotNull("Report instance from the DB is NULL", reportDB);
        assertNotNull("MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet());
        assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet().getMeasurements());
        assertNotNull("Report returned from DB should have indicated 5 measurements, but variable is NULL", reportDB.getNumberOfMeasurements());
        assertTrue("Report returned from DB should have indicated 5 measurements, but got " + reportDB.getNumberOfMeasurements(), reportDB.getNumberOfMeasurements() == 5);
        assertTrue("Report returned from DB should have contained 0 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().isEmpty());
    }
    
    @Test
    public void testGetReport_forTimePeriod_withMeasurements()
    {
        log.info(" - getting report for measurements with a given time period, with the actual measurements");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        try { Thread.sleep(100); } catch (InterruptedException ex) { }
        
        report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        Date fromDate = report.getFromDate();
        Date toDate = report.getToDate();
        
        try { Thread.sleep(100); } catch (InterruptedException ex) { }
        
        report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveMeasurements(report);
        } catch (Exception ex) {
            fail("Unable to save measurements for report: " + ex.getMessage());
        }
        
        Report reportDB = null;
        try {
            reportDB = reportDAO.getReportForMeasurementsForTimePeriod(PopulateDB.mGrp1mSet1UUID, fromDate, toDate, true);
        } catch (Exception ex) {
            fail("Unable to get report from DB from date " + fromDate + ", with measurements: " + ex.getMessage());
        }
        
        assertNotNull("Report instance from the DB is NULL", reportDB);
        assertNotNull("MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet());
        assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet().getMeasurements());
        assertNotNull("Report returned from DB should have indicated 5 measurements, but variable is NULL", reportDB.getNumberOfMeasurements());
        assertTrue("Report returned from DB should have indicated 5 measurements, but got " + reportDB.getNumberOfMeasurements(), reportDB.getNumberOfMeasurements() == 5);
        assertTrue("Report returned from DB should have contained 5 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().size() == 5);
    }
    
    @Test
    public void testGetReport_UnsyncedMeasurementsFromDate_withoutMeasurements()
    {
        log.info(" - getting report for unsynchronised measurements (without actual measurements)");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 20);
        try {
            reportDAO.saveReport(report, true);
        } catch (Exception ex) {
            fail("Unable to save report (1) with measurements: " + ex.getMessage());
        }
        
        Report report2 = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 20);
        try {
            reportDAO.saveReport(report2, true);
        } catch (Exception ex) {
            fail("Unable to save report (2) with measurements: " + ex.getMessage());
        }
        
        // setting synch flag
        try {
            reportDAO.setReportMeasurementsSyncFlag(report.getUUID(), true);
        } catch (Exception ex) {
            fail("Unable to set sync flag for measurements for report: " + ex.getMessage());
        }
        
        Report reportDB = null;
        Date fromDate = new Date(Long.parseLong("1346146187675"));
        int numMeasurements = 10;
        try {
            reportDB = reportDAO.getReportForUnsyncedMeasurementsFromDate(PopulateDB.mGrp1mSet1UUID, fromDate, numMeasurements, false);
        } catch (Exception ex) {
            fail("Unable to get report for unsynchronised measurements: " + ex.getMessage());
        }
        
        assertNotNull("Report instance from the DB is NULL", reportDB);
        assertNotNull("MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet());
        assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet().getMeasurements());
        assertNotNull("Report returned from DB should have indicated 10 measurements, but variable is NULL", reportDB.getNumberOfMeasurements());
        assertTrue("Report returned from DB should have indicated 10 measurements, but got " + reportDB.getNumberOfMeasurements(), reportDB.getNumberOfMeasurements() == 10);
        assertTrue("Report returned from DB should have contained 0 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().isEmpty());
    }
    
    @Test
    public void testGetReport_UnsyncedMeasurementsFromDate_withMeasurements()
    {
        log.info(" - getting report for unsynchronised measurements (with actual measurements)");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 20);
        try {
            reportDAO.saveReport(report, true);
        } catch (Exception ex) {
            fail("Unable to save report (1) with measurements: " + ex.getMessage());
        }
        
        Report report2 = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 20);
        try {
            reportDAO.saveReport(report2, true);
        } catch (Exception ex) {
            fail("Unable to save report (2) with measurements: " + ex.getMessage());
        }
        
        // setting synch flag
        try {
            reportDAO.setReportMeasurementsSyncFlag(report.getUUID(), true);
        } catch (Exception ex) {
            fail("Unable to set sync flag for measurements for report: " + ex.getMessage());
        }
        
        Report reportDB = null;
        Date fromDate = new Date(Long.parseLong("1346146187675"));
        int numMeasurements = 10;
        try {
            reportDB = reportDAO.getReportForUnsyncedMeasurementsFromDate(PopulateDB.mGrp1mSet1UUID, fromDate, numMeasurements, true);
        } catch (Exception ex) {
            fail("Unable to get report for unsynchronised measurements: " + ex.getMessage());
        }
        
        assertNotNull("Report instance from the DB is NULL", reportDB);
        assertNotNull("MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet());
        assertNotNull("Set of Measurement in the MeasurementSet of Report instance from the DB is NULL", reportDB.getMeasurementSet().getMeasurements());
        assertNotNull("Report returned from DB should have indicated 10 measurements, but variable is NULL", reportDB.getNumberOfMeasurements());
        assertTrue("Report returned from DB should have indicated 10 measurements, but got " + reportDB.getNumberOfMeasurements(), reportDB.getNumberOfMeasurements() == 10);
        assertTrue("Report returned from DB should have contained 10 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().size() == 10);
    }
    
    @Test
    public void testDeleteReport_noMeasurements()
    {
        log.info(" - deleting report, but not the measurements");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveReport(report, true);
        } catch (Exception ex) {
            fail("Unable to save report with measurements: " + ex.getMessage());
        }
        
        try {
            reportDAO.deleteReport(report.getUUID(), false);
        } catch (Exception ex) {
            fail("Unable to delete report (without measurements): " + ex.getMessage());
        }
        
        // try to get report from the DB (shouldn't be there)
        Report reportDB = null;
        try {
            reportDB = reportDAO.getReport(report.getUUID(), true);
            fail("Got report from the DB that should have been deleted");
        } catch (Exception ex) { }
        
        // try to get report for all measurements - to check that the measurements are there
        try {
            reportDB = reportDAO.getReportForAllMeasurements(PopulateDB.mGrp1mSet1UUID, true);
        } catch (Exception ex) {
            fail("Unable to get a report for all measurements: " + ex.toString());
        }
        
        assertNotNull("Report instance for all measurements from the DB is NULL", reportDB);
        assertNotNull("MeasurementSet of Report instance for all measurements from the DB is NULL", reportDB.getMeasurementSet());
        assertNotNull("Set of Measurement in the MeasurementSet of Report instance for all measurements from the DB is NULL", reportDB.getMeasurementSet().getMeasurements());
        assertTrue("Report for all measurements returned from DB should have contained 5 measurements, but contained " + reportDB.getMeasurementSet().getMeasurements().size() + " measurement(s)", reportDB.getMeasurementSet().getMeasurements().size() == 5);
    }
    
    @Test
    public void testDeleteReport_withMeasurements()
    {
        log.info(" - deleting report with the measurements");
        
        if ((edm == null) || (reportDAO == null)) {
            fail("EDM not set up, cannot perform test");
        }
        
        
        Report report = getReportWithRandomMeasurements(UUID.randomUUID(), PopulateDB.mGrp1mSet1UUID, 5);
        try {
            reportDAO.saveReport(report, true);
        } catch (Exception ex) {
            fail("Unable to save report with measurements: " + ex.getMessage());
        }
        
        try {
            reportDAO.deleteReport(report.getUUID(), true);
        } catch (Exception ex) {
            fail("Unable to delete report (without measurements): " + ex.getMessage());
        }
        
        // try to get report from the DB (shouldn't be there)
        Report reportDB = null;
        try {
            reportDB = reportDAO.getReport(report.getUUID(), true);
            fail("Got report from the DB that should have been deleted");
        } catch (Exception ex) { }
        
        // try to get report for all measurements - to check that the measurements aren't still there
        try {
            reportDB = reportDAO.getReportForAllMeasurements(PopulateDB.mGrp1mSet1UUID, true);
            fail("Got report from the DB that should have been deleted");
        } catch (Exception ex) { }
    }
}
