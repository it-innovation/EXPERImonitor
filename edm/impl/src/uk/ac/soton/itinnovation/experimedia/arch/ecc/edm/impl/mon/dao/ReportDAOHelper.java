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
//      Created Date :          2012-08-23
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.mon.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.NoDataException;

/**
 * A helper class for validating and executing queries for the Reports.
 * 
 * @author Vegard Engen
 */
public class ReportDAOHelper
{
    static Logger log = Logger.getLogger(ReportDAOHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(Report report, Connection connection) throws Exception
    {
        if (report == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Report object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, mSetUUID, from timestamp, to timestamp, numMeasurements
        
        if (report.getUUID() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Report UUID is NULL"));
        }
        
        if (report.getMeasurementSet() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Report's MeasurementSet is NULL"));
        }
        
        if (report.getReportDate() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Report's reporting date is NULL"));
        }
        
        if (report.getFromDate() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Report's from date is NULL"));
        }
        
        if (report.getToDate() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Report's to date is NULL"));
        }
        
        if (report.getNumberOfMeasurements() == null)
        {
            return new ValidationReturnObject(false, new IllegalArgumentException("The Report's number of measurements value is NULL"));
        }
        /*
        // check if it exists in the DB already
        try {
            if (objectExists(report.getUUID(), connection))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Report already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }
        */
        // if checking for measurement set
        /*
        if (!MeasurementSetHelper.objectExists(report.getMeasurementSet().getUUID(), connection))
        {
            return new ValidationReturnObject(false, new RuntimeException("The Reports's MeasurementSet does not exist (UUID: " + report.getMeasurementSet().getUUID().toString() + ")"));
        }
        */
        return new ValidationReturnObject(true);
    }
    
    public static boolean objectExists(UUID uuid, Connection connection) throws Exception
    {
        return DBUtil.objectExistsByUUID("Report", "reportUUID", uuid, connection, false);
    }
    
    public static void saveReport(Report report, boolean saveMeasurements, Connection connection) throws Exception
    {
        // should save any measurements that may be included
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        // also checks that the MeasurementSet exists (by it's UUID)
        ValidationReturnObject returnObj = ReportDAOHelper.isObjectValidForSave(report, connection);
        if (!returnObj.valid)
        {
            log.error("Cannot save the Report object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot save the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot save the Report because the connection to the DB is closed");
        }
        
        try {
            String query = "INSERT INTO Report (reportUUID, mSetUUID, reportTimeStamp, fromDateTimeStamp, toDateTimeStamp, numMeasurements) VALUES ("
                    + "?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, report.getUUID(), java.sql.Types.OTHER);
            pstmt.setObject(2, report.getMeasurementSet().getUUID(), java.sql.Types.OTHER);
            pstmt.setLong(3, report.getReportDate().getTime());
            pstmt.setLong(4, report.getFromDate().getTime());
            pstmt.setLong(5, report.getToDate().getTime());
            pstmt.setInt(6, report.getNumberOfMeasurements());
            
            pstmt.executeUpdate();
        } catch (Exception ex) {
            log.error("Error while saving Report: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving Report: " + ex.getMessage(), ex);
        }
        
        if (saveMeasurements)
        {
            // save any measurements if not NULL
            try {
                if ((report.getMeasurementSet().getMeasurements() != null) && !report.getMeasurementSet().getMeasurements().isEmpty())
                {
                    log.debug("Saving " + report.getMeasurementSet().getMeasurements().size() + " measurements for the report");
                    MeasurementDAOHelper.saveMeasurementsForSet(report.getMeasurementSet().getMeasurements(), report.getMeasurementSet().getUUID(), connection);
                }
            } catch (Exception ex) {
                throw ex;
            }

            // save report - measurment link(s), if any measurements
            try {
                if ((report.getMeasurementSet().getMeasurements() != null) && !report.getMeasurementSet().getMeasurements().isEmpty())
                {
                    log.debug("Saving Report - Measurement links");
                    // not validating measurement here because an exception would have been thrown above if there was an issue
                    for (Measurement measurement : report.getMeasurementSet().getMeasurements())
                    {
                        linkReportAndMeasurement(report.getUUID(), measurement.getUUID(), connection);
                    }
                }
            } catch (Exception ex) {
                throw ex;
            }
        }
    }
    
    public static void linkReportAndMeasurement(UUID reportUUID, UUID measurementUUID, Connection connection) throws Exception
    {
        if (reportUUID == null)
        {
            log.error("Cannot link report and measurement because the report UUID is NULL!");
            throw new IllegalArgumentException("Cannot link report and measurement because the report UUID is NULL!");
        }
        
        if (measurementUUID == null)
        {
            log.error("Cannot link report and measurement because the measurement UUID is NULL!");
            throw new IllegalArgumentException("Cannot link report and measurement because the measurement UUID is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot link the report and measurement because the connection to the DB is closed");
            throw new RuntimeException("Cannot link the report and measurement because the connection to the DB is closed");
        }
        
        try {
            String query = "INSERT INTO Report_Measurement (reportUUID, measurementUUID) VALUES (?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, reportUUID, java.sql.Types.OTHER);
            pstmt.setObject(2, measurementUUID, java.sql.Types.OTHER);
            
            pstmt.executeUpdate();
        } catch (Exception ex) {
            log.error("Error while saving Report - Measurement link: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving Report - Measurement link: " + ex.getMessage(), ex);
        }
    }
    
    // used when a report is generated from existing measurements in the DB
    public static void saveReportWithoutMeasurements(Report report, Connection connection) throws Exception
    {
        // should save any measurements that may be included
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        // also checks that the MeasurementSet exists (by it's UUID)
        ValidationReturnObject returnObj = ReportDAOHelper.isObjectValidForSave(report, connection);
        if (!returnObj.valid)
        {
            log.error("Cannot save the Report object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot save the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot save the Report because the connection to the DB is closed");
        }
        
        try {
            String query = "INSERT INTO Report (reportUUID, mSetUUID, reportTimeStamp, fromDateTimeStamp, toDateTimeStamp, numMeasurements) VALUES ("
                    + "?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, report.getUUID(), java.sql.Types.OTHER);
            pstmt.setObject(2, report.getMeasurementSet().getUUID(), java.sql.Types.OTHER);
            pstmt.setLong(3, report.getReportDate().getTime());
            pstmt.setLong(4, report.getFromDate().getTime());
            pstmt.setLong(5, report.getToDate().getTime());
            pstmt.setInt(6, report.getNumberOfMeasurements());
            
            pstmt.executeUpdate();
        } catch (Exception ex) {
            log.error("Error while saving Report: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving Report: " + ex.getMessage(), ex);
        }
    }
    
    public static void saveMeasurementsForReport(Report report, Connection connection) throws Exception
    {
        if (report == null)
        {
            log.error("Cannot save the measurements for the report because the Report object is NULL");
            throw new IllegalArgumentException("Cannot save the measurements for the report because the Report object is NULL");
        }
        if (report.getMeasurementSet() == null)
        {
            log.error("Cannot save the measurements for the report because the MeasurementSet object is NULL");
            throw new IllegalArgumentException("Cannot save the measurements for the report because the MeasurementSet object is NULL");
        }
        if ((report.getMeasurementSet().getMeasurements() == null) || report.getMeasurementSet().getMeasurements().isEmpty())
        {
            log.error("Cannot save the measurements for the report because there are no Measurement objects");
            throw new IllegalArgumentException("Cannot save the measurements for the report because there are no Measurement objects");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot save the measurements for the report because the connection to the DB is closed");
            throw new RuntimeException("Cannot save the measurements for the report because the connection to the DB is closed");
        }
        
        try {
            log.debug("Saving " + report.getMeasurementSet().getMeasurements().size() + " measurements for the report");
            MeasurementDAOHelper.saveMeasurementsForSet(report.getMeasurementSet().getMeasurements(), report.getMeasurementSet().getUUID(), connection);
        } catch (Exception ex) {
            log.error("Error while saving the measurements for the report: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving the measurements for the report: " + ex.getMessage(), ex);
        }
    }
    
    public static Report getReport(UUID reportUUID, boolean withMeasurements, Connection connection) throws Exception
    {
        if (reportUUID == null)
        {
            log.error("Cannot get a Report object with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot get a Report object with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Report because the connection to the DB is closed");
        }
        
        Report report = null;
        try {
            String query = "SELECT * FROM Report WHERE reportUUID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, reportUUID, java.sql.Types.OTHER);
            ResultSet rs = pstmt.executeQuery();
            
            // check if anything got returned
            if (rs.next())
            {
                UUID mSetUUID = null;
                Date reportTimeStamp = null;
                Date fromDateTimeStamp = null;
                Date toDateTimeStamp = null;
                Integer numMeasurements = null;
                
                try {
                    mSetUUID = UUID.fromString(rs.getString("mSetUUID"));
                } catch (NullPointerException npe) {
                    throw new RuntimeException("Could not get the measurement set UUID from the database");
                } catch (Exception ex) {
                    throw new RuntimeException("Unable to retrieve report time, because the timestamp in the database could not be converted!", ex);
                }
                
                try {
                    reportTimeStamp = new Date(Long.parseLong(rs.getString("reportTimeStamp")));
                } catch (NullPointerException npe) {
                    throw new RuntimeException("Could not get the report time stamp from the database.", npe);
                } catch (Exception ex) {
                    throw new RuntimeException("Unable to retrieve report time, because the timestamp in the database could not be converted!", ex);
                }
                
                try {
                    fromDateTimeStamp = new Date(Long.parseLong(rs.getString("fromDateTimeStamp")));
                } catch (NullPointerException npe) {
                    throw new RuntimeException("Could not get the from-date time stamp from the database.", npe);
                } catch (Exception ex) {
                    throw new RuntimeException("Unable to retrieve from-date time, because the timestamp in the database could not be converted!", ex);
                }
                
                try {
                    toDateTimeStamp = new Date(Long.parseLong(rs.getString("toDateTimeStamp")));
                } catch (NullPointerException npe) {
                    throw new RuntimeException("Could not get the to-date time stamp from the database.", npe);
                } catch (Exception ex) {
                    throw new RuntimeException("Unable to retrieve to-date time, because the timestamp in the database could not be converted!", ex);
                }
                
                try {
                    numMeasurements = rs.getInt("numMeasurements");
                } catch (NullPointerException npe) {
                    throw new RuntimeException("Could not get the numMeasurements parameter from the database.", npe);
                } catch (Exception ex) {
                    throw new RuntimeException("Unable to retrieve Measurement instance, because the timestamp in the DB could not be converted!", ex);
                }
                
                report = new Report(reportUUID, new MeasurementSet(mSetUUID), reportTimeStamp, fromDateTimeStamp, toDateTimeStamp, numMeasurements);
            }
            else // nothing in the result set
            {
                log.error("There is no Report with the given UUID: " + reportUUID.toString());
                throw new NoDataException("There is no Report with the given UUID: " + reportUUID.toString());
            }
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        }
        
        if (withMeasurements)
        {
            // get the measurements
            Set<Measurement> measurements = new HashSet<Measurement>();

            try {
                String query = "SELECT * FROM Measurement WHERE measurementUUID IN "
                        + "(SELECT measurementUUID FROM Report_Measurement WHERE reportUUID = ?)";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setObject(1, reportUUID, java.sql.Types.OTHER);
                ResultSet rs = pstmt.executeQuery();

                // check if anything got returned
                if (!rs.next())
                {
                    log.debug("There are no measurements for the report (UUID = " + reportUUID.toString() + ").");
                    throw new NoDataException("There are no measurements for the report (UUID = " + reportUUID.toString() + ").");
                }

                // process each result item
                do {
                    String measurementUUIDstr = rs.getString("measurementUUID");
                    String mSetUUIDstr = rs.getString("mSetUUID");
                    String timeStampStr = rs.getString("timeStamp");
                    String value = rs.getString("value");

                    Date timeStamp = new Date(Long.parseLong(timeStampStr));
                    UUID measurementUUID = UUID.fromString(measurementUUIDstr);
                    UUID mSetUUID = UUID.fromString(mSetUUIDstr);

                    measurements.add(new Measurement(measurementUUID, mSetUUID, timeStamp, value));
                } while (rs.next());

            } catch (NoDataException nde) {
                throw nde;
            } catch (Exception ex) {
                log.error("Caught an exception when getting measurements for report: " + ex.getMessage());
                throw new RuntimeException("Failed to get the report when getting the measurements: " + ex.getMessage(), ex);
            }

            report.getMeasurementSet().setMeasurements(measurements);
        }
        
        return report;
    }

    public static Report getReportForLatestMeasurement(UUID mSetUUID, boolean withMeasurements, Connection connection) throws Exception
    {
        if (mSetUUID == null)
        {
            log.error("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Report because the connection to the DB is closed");
        }
        
        Report report = null;
        try {
            String query = "SELECT * FROM Measurement WHERE mSetUUID = ? ORDER BY timeStamp DESC LIMIT 1";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, mSetUUID, java.sql.Types.OTHER);
            
            ResultSet rs = pstmt.executeQuery();
            
            report = getReportFromMeasurementResultSet(mSetUUID, rs, withMeasurements);
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        }
        /*
        try {
            ReportHelper.saveReportWithoutMeasurements(report, connection, closeDBcon);
        } catch (Exception ex) {
            log.error("Caught an exception when trying to save the Report object generated: " + ex.getMessage(), ex);
        }
        */
        return report;
    }
    
    public static Report getReportForAllMeasurements(UUID mSetUUID, boolean withMeasurements, Connection connection) throws Exception
    {
        if (mSetUUID == null)
        {
            log.error("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Report because the connection to the DB is closed");
        }
        
        Report report = null;
        try {
            String query = "SELECT * FROM Measurement WHERE mSetUUID = ?";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, mSetUUID, java.sql.Types.OTHER);
            
            ResultSet rs = pstmt.executeQuery();
            report = getReportFromMeasurementResultSet(mSetUUID, rs, withMeasurements);
            
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        }
        /*
        try {
            ReportHelper.saveReportWithoutMeasurements(report, connection, false);
        } catch (Exception ex) {
            log.error("Caught an exception when trying to save the Report object generated: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        */
        return report;
    }
    
    public static Report getReportForMeasurementsAfterDate(UUID mSetUUID, Date fromDate, boolean withMeasurements, Connection connection) throws Exception
    {
        if (mSetUUID == null)
        {
            log.error("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
        }
        
        if (fromDate == null)
        {
            log.error("Cannot generate a Report object for the MeasurementSet from the given date because it is NULL!");
            throw new IllegalArgumentException("Cannot generate a Report object for the MeasurementSet from the given date because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Report because the connection to the DB is closed");
        }
        
        Report report = null;
        try {
            String query = "SELECT * FROM Measurement WHERE mSetUUID = ?"
                    + " AND timeStamp >= ?";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, mSetUUID, java.sql.Types.OTHER);
            pstmt.setLong(2, fromDate.getTime());
            
            ResultSet rs = pstmt.executeQuery();
            report = getReportFromMeasurementResultSet(mSetUUID, rs, withMeasurements);
            
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        }
        /*
        try {
            ReportHelper.saveReportWithoutMeasurements(report, connection, false);
        } catch (Exception ex) {
            log.error("Caught an exception when trying to save the Report object generated: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        */
        return report;
    }
    
    public static Report getReportForMeasurementsForTimePeriod(UUID mSetUUID, Date fromDate, Date toDate, boolean withMeasurements, Connection connection) throws Exception
    {
        if (mSetUUID == null)
        {
            log.error("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
        }
        
        if (fromDate == null)
        {
            log.error("Cannot generate a Report object for the MeasurementSet from the given date because it is NULL!");
            throw new IllegalArgumentException("Cannot generate a Report object for the MeasurementSet from the given date because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Report because the connection to the DB is closed");
        }
        
        Report report = new Report();
        // get measurements, which will allow the calculation of the other values for the Report object
        try {
            String query = "SELECT * FROM Measurement WHERE mSetUUID = ? AND timeStamp BETWEEN ? AND ?";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, mSetUUID, java.sql.Types.OTHER);
            pstmt.setLong(2, fromDate.getTime());
            pstmt.setLong(3, toDate.getTime());
            
            ResultSet rs = pstmt.executeQuery();
            report = getReportFromMeasurementResultSet(mSetUUID, rs, withMeasurements);
            
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        }
        
        /*
        try {
            ReportHelper.saveReportWithoutMeasurements(report, connection, false);
        } catch (Exception ex) {
            log.error("Caught an exception when trying to save the Report object generated: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        */
        return report;
    }
    
    private static Report getReportFromMeasurementResultSet(UUID mSetUUID, ResultSet rs, boolean withMeasurements) throws Exception
    {
        Report report = new Report();
        Date reportDate = new Date();
        Date fromDate = null;
        Date toDate = null;
        Integer numberOfMeasurements = null;
        
        MeasurementSet mSet = new MeasurementSet(mSetUUID);
        Set<Measurement> measurements = new HashSet<Measurement>();
        /* get the measurement set (won't have values)
        MeasurementSet mSet = null;
        try {
            mSet = MeasurementSetHelper.getMeasurementSet(mSetUUID, false, connection, false);
        } catch (Exception ex) {
            log.error("Failed to generate report, because the MeasurementSet couldn't be retrieved from the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Failed to generate report, because the MeasurementSet couldn't be retrieved from the database: " + ex.getMessage(), ex);
        }*/
        
        try {
            int numRows = 0;
            Long timeStampFrom = null;
            Long timeStampTo = null;
            
            // check if anything got returned
            if (!rs.next())
            {
                log.debug("There are no measurements in the database for the given time period of the measurement set to generate a report from");
                throw new NoDataException("There are no measurements in the database for the given time period of the measurement set to generate a report from");
            }
            
            // process each result item
            do {
                String measurementUUIDstr = rs.getString("measurementUUID");
				String timeStampStr = rs.getString("timeStamp");
                String value = rs.getString("value");
                
                try {
                    UUID measurementUUID = UUID.fromString(measurementUUIDstr);
                    Date timeStampDate = new Date(Long.parseLong(timeStampStr));
                    
                    if (numRows == 0)
                    {
                        timeStampFrom = timeStampDate.getTime();
                        timeStampTo = timeStampDate.getTime();
                    }
                    else if (timeStampDate.getTime() > timeStampTo)
                    {
                        timeStampTo = timeStampDate.getTime();
                    }
                    else if (timeStampDate.getTime() < timeStampFrom)
                    {
                        timeStampFrom = timeStampDate.getTime();
                    }
                    
                    if (withMeasurements)
                    {
                        measurements.add(new Measurement(measurementUUID, mSetUUID, timeStampDate, value));
                    }
                } catch (Exception ex) {
                    log.error("Unable to process measurement for the report (skipping): " + ex.getMessage(), ex);
                }
                numRows++;
            } while (rs.next());
            
            fromDate = new Date(timeStampFrom);
            toDate = new Date(timeStampTo);
            numberOfMeasurements = numRows;
            
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while creating report from measurement data: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while creating report from measurement data: " + ex.getMessage(), ex);
        }
        
        report.setReportDate(reportDate);
        report.setFromDate(fromDate);
        report.setToDate(toDate);
        report.setNumberOfMeasurements(numberOfMeasurements);
        report.setMeasurementSet(mSet);
        
        if (withMeasurements)
        {
            mSet.setMeasurements(measurements);
        }
        
        return report;
    }
    
    
    public static Report getReportForUnsyncedMeasurementsAfterDate(UUID measurementSetUUID, Date fromDate, int numMeasurements, boolean withMeasurements, Connection connection) throws IllegalArgumentException, NoDataException, Exception
    {
        if (measurementSetUUID == null)
        {
            log.error("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
        }
        
        if (fromDate == null)
        {
            log.error("Cannot generate a Report object for the MeasurementSet from the given date because it is NULL!");
            throw new IllegalArgumentException("Cannot generate a Report object for the MeasurementSet from the given date because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Report because the connection to the DB is closed");
        }
        
        Report report = null;
        try {
            String query = "SELECT * FROM Measurement WHERE mSetUUID = ?"
                    + " AND synchronised = ? AND timeStamp >= ? ORDER BY timeStamp DESC LIMIT ?";
            
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, measurementSetUUID, java.sql.Types.OTHER);
            pstmt.setBoolean(2, false);
            pstmt.setLong(3, fromDate.getTime());
            pstmt.setInt(4, numMeasurements);
            
            ResultSet rs = pstmt.executeQuery();
            report = getReportFromMeasurementResultSet(measurementSetUUID, rs, withMeasurements);
            
        } catch (NoDataException nde) {
            throw nde;
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        }
        /*
        try {
            ReportHelper.saveReportWithoutMeasurements(report, connection, false);
        } catch (Exception ex) {
            log.error("Caught an exception when trying to save the Report object generated: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        */
        return report;
    }
    
    public static void setReportMeasurementsSyncFlag(UUID reportUUID, boolean syncFlag, Connection connection) throws IllegalArgumentException, Exception
    {
        if (reportUUID == null)
        {
            log.error("Cannot set the sync flag for the Measurement of the Report because the given UUID is NULL");
            throw new IllegalArgumentException("Cannot set the sync flag for the Measurement of the Report because the given UUID is NULL");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot set the sync flag for the Measurement of the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot set the sync flag for the Measurement of the Report because the connection to the DB is closed");
        }
        
        try {
            log.debug("Setting sync flag to " + syncFlag + " for measurements belonging to Report with UUID " + reportUUID.toString());
            String query = "UPDATE Measurement SET synchronised = ? WHERE measurementUUID IN (SELECT measurementUUID FROM Report_Measurement where reportUUID = ?)";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setBoolean(1, syncFlag);
            pstmt.setObject(2, reportUUID, java.sql.Types.OTHER);
            pstmt.executeUpdate();
        } catch (Exception ex) {
            log.error("Cannot set the sync flag for the Measurement of the Report: " + ex.getMessage(), ex);
            throw new RuntimeException("Cannot set the sync flag for the Measurement of the Report: " + ex.getMessage(), ex);
        }
    }

    public static void deleteReport(UUID reportUUID, boolean withMeasurements, Connection connection) throws IllegalArgumentException, Exception
    {
        log.debug("Deleting report");
        
        if (reportUUID == null)
        {
            log.error("Cannot delete Report object with the given UUID because it is NULL!");
            throw new IllegalArgumentException("Cannot delete Report object with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot delete the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot delete the Report because the connection to the DB is closed");
        }
        
        // delete measurements first, if flagged to do so
        if (withMeasurements)
        {
            Set<UUID> measurements = new HashSet<UUID>();
            // get the measurement UUIDs
            try {
                String query = "SELECT measurementUUID FROM Report_Measurement where reportUUID = ?";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setObject(1, reportUUID, java.sql.Types.OTHER);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next())
                {
                    measurements.add(UUID.fromString(rs.getString("measurementUUID")));
                }
            } catch (Exception ex) {
                log.error("Unable to get measurements for report: " + ex.getMessage(), ex);
                throw new RuntimeException("Unable to get measurements for report: " + ex.getMessage(), ex);
            }
            if (!measurements.isEmpty())
            {
                log.info("Deleting measurements associated with the report");
                MeasurementDAOHelper.deleteMeasurements(measurements, connection);
            }
        }
        
        try {
            String query = "DELETE from Report where reportUUID = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setObject(1, reportUUID, java.sql.Types.OTHER);
            pstmt.executeUpdate();
        } catch (Exception ex) {
            log.error("Unable to delete report: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to delete report: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Generates a report for the measurements, which is saved.
     * @param measurements The measurements.
     * @param mSetUUID The UUID of the measurement set for the measurements.
     * @param validateMeasurements A flag to say whether to validate the measurements or not.
     * @param saveMeasurements A flag to say whether the measurements should be saved or not.
     * @param connection A database connection object.
     * @param closeDBcon A flag to say whether the database connection should be closed or not.
     * @throws Exception 
     *
    public static void saveReportForMeasurements(Set<Measurement> measurements, UUID mSetUUID, boolean validateMeasurements, boolean saveMeasurements, Connection connection, boolean closeDBcon) throws Exception
    {
        log.debug("Generating and saving a report for measurements.");
        if (validateMeasurements && saveMeasurements)
        {
            ValidationReturnObject returnObj = MeasurementDAOHelper.areObjectsValidForSave(measurements, mSetUUID, false, connection, false);
            if (!returnObj.valid)
            {
                log.error("Unable to save report for measurements: " + returnObj.exception.getMessage(), returnObj.exception);
                throw returnObj.exception;
            }
        }
        else
        {
            if ((measurements == null) || measurements.isEmpty())
            {
                log.error("Cannot save report for measurements, because the set of measurements is NULL (or empty)");
                throw new RuntimeException("Cannot save report for measurements, because the set of measurements is NULL (or empty)");
            }

            if (mSetUUID == null)
            {
                log.error("Cannot save report for measurements, because the measurement set UUID is NULL");
                throw new RuntimeException("Cannot save report for measurements, because the measurement set UUID is NULL");
            }
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot save the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot save the Report because the connection to the DB is closed");
        }
        
        // need to get the from/to dates from the measurements
        Long timeStampFrom = null;
        Long timeStampTo = null;
        
        try {
            for (Measurement measurement : measurements)
            {
                long timeStamp = measurement.getTimeStamp().getTime();
                if (timeStampFrom == null)
                {
                    timeStampFrom = timeStamp;
                    timeStampTo = timeStamp;
                }
                else
                {
                    if (timeStamp > timeStampTo)
                        timeStampTo = timeStamp;
                    else if (timeStamp < timeStampFrom)
                        timeStampFrom = timeStamp;
                }
            }
        } catch (Exception ex) {
            log.error("Unable to save report for measurements because it was not possible to get the time stamp from one or more measurements (was NULL)", ex);
            throw new RuntimeException("Unable to save report for measurements because it was not possible to get the time stamp from one or more measurements (was NULL)", ex);
        }
        Report report = null;
        
        try {
            report = new Report(UUID.randomUUID(), new MeasurementSet(mSetUUID), new Date(), new Date(timeStampFrom), new Date(timeStampTo), measurements.size());
        } catch (Exception ex) {
            log.error("Unable to create and save a report for the given measurements: " + ex.getMessage(), ex);
            throw new RuntimeException("Unable to create and save a report for the given measurements: " + ex.getMessage(), ex);
        }
        
        if (saveMeasurements)
        {
            report.getMeasurementSet().setMeasurements(measurements);
            saveReport(report, connection, closeDBcon);
        }
        else
        {
            saveReportWithoutMeasurements(report, connection, closeDBcon);
        }
    }*/
}
