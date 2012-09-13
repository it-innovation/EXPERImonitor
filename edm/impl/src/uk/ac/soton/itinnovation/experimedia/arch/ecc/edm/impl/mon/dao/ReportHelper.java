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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;

/**
 * A helper class for validating and executing queries for the Reports.
 * 
 * @author Vegard Engen
 */
public class ReportHelper
{
    static Logger log = Logger.getLogger(ReportHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(Report report, Connection connection, boolean closeDBcon) throws Exception
    {
        if (report == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Report object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, mSetUUID, from timestamp, to timestamp, numMeasurements
        
        if (report.getUUID() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Report UUID is NULL"));
        }
        
        if (report.getMeasurementSet() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Report's MeasurementSet is NULL"));
        }
        
        if (report.getReportDate() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Report's reporting date is NULL"));
        }
        
        if (report.getFromDate() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Report's from date is NULL"));
        }
        
        if (report.getToDate() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Report's to date is NULL"));
        }
        
        if (report.getNumberOfMeasurements() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Report's number of measurements value is NULL"));
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
    
    public static String getSqlInsertQuery(Report report)
    {
        String query = "INSERT INTO Report (reportUUID, mSetUUID, reportTimeStamp, fromDateTimeStamp, toDateTimeStamp, numMeasurements) VALUES ("
                    + "'" + report.getUUID().toString() + "', "
                    + "'" + report.getMeasurementSet().getUUID().toString() + "', "
                    + String.valueOf(report.getReportDate().getTime()) + ", "
                    + String.valueOf(report.getFromDate().getTime()) + ", "
                    + String.valueOf(report.getToDate().getTime()) + ", "
                    + String.valueOf(report.getNumberOfMeasurements()) + ")";
        
        return query;
    }
    
    public static boolean objectExists(UUID uuid, Connection connection, boolean closeDBcon) throws Exception
    {
        return DBUtil.objectExistsByUUID("Report", "reportUUID", uuid, connection, closeDBcon);
    }
    
    public static void saveReport(Report report, Connection connection, boolean closeDBcon) throws Exception
    {
        // should save any measurements that may be included
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        // also checks that the MeasurementSet exists (by it's UUID)
        ValidationReturnObject returnObj = ReportHelper.isObjectValidForSave(report, connection, false);
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
        
        boolean exception = false;
        try {
            if (closeDBcon)
            {
                log.debug("Starting transaction");
                connection.setAutoCommit(false);
            }
            
            String query = ReportHelper.getSqlInsertQuery(report);
            ResultSet rs = DBUtil.executeQuery(connection, query, Statement.RETURN_GENERATED_KEYS);
            
            // check if the result set got the generated table key
            if (rs.next()) {
                String key = rs.getString(1);
                log.debug("Saved Report with key: " + key);
            } else {
                exception = true;
                throw new RuntimeException("No index returned after saving Report");
            }
        } catch (Exception ex) {
            exception = true;
            log.error("Error while saving Report: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving Report: " + ex.getMessage(), ex);
        } finally {
            if (exception && closeDBcon)
            {
                log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                connection.rollback();
                connection.close();
            }
        }
        
        try {
            // save any measurements if not NULL
            if ((report.getMeasurementSet().getMeasurements() != null) && !report.getMeasurementSet().getMeasurements().isEmpty())
            {
                log.debug("Saving " + report.getMeasurementSet().getMeasurements().size() + " measurements for the report");
                MeasurementHelper.saveMeasurementsForSet(report.getMeasurementSet().getMeasurements(), report.getMeasurementSet().getUUID(), connection, false); // flag not to close the DB connection
            }
        } catch (Exception ex) {
            exception = true;
            throw ex;
        } finally {
            if (closeDBcon)
            {
                if (exception) {
                    log.debug("Exception thrown, so rolling back the transaction and closing the connection");
                    connection.rollback();
                }
                else {
                    log.debug("Committing the transaction and closing the connection");
                    connection.commit();
                }
                connection.close();
            }
        }
    }
    
    // used when a report is generated from existing measurements in the DB
    public static void saveReportWithoutMeasurements(Report report, Connection connection, boolean closeDBcon) throws Exception
    {
        // should save any measurements that may be included
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        // also checks that the MeasurementSet exists (by it's UUID)
        ValidationReturnObject returnObj = ReportHelper.isObjectValidForSave(report, connection, false);
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
            String query = ReportHelper.getSqlInsertQuery(report);
            ResultSet rs = DBUtil.executeQuery(connection, query, Statement.RETURN_GENERATED_KEYS);
            
            // check if the result set got the generated table key
            if (rs.next()) {
                String key = rs.getString(1);
                log.debug("Saved Report with key: " + key);
            } else {
                throw new RuntimeException("No index returned after saving Report");
            }
        } catch (Exception ex) {
            log.error("Error while saving Report: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving Report: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
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
     */
    public static void saveReportForMeasurements(Set<Measurement> measurements, UUID mSetUUID, boolean validateMeasurements, boolean saveMeasurements, Connection connection, boolean closeDBcon) throws Exception
    {
        log.debug("Generating and saving a report for measurements.");
        if (validateMeasurements && saveMeasurements)
        {
            ValidationReturnObject returnObj = MeasurementHelper.areObjectsValidForSave(measurements, mSetUUID, false, connection, false);
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
    }

    public static Report getReport(UUID reportUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        if (reportUUID == null)
        {
            log.error("Cannot get a Report object with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get a Report object with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Report because the connection to the DB is closed");
        }
        
        /*if (!ReportHelper.objectExists(reportUUID, connection))
        {
            log.error("There is no Report with the given UUID: " + reportUUID.toString());
            throw new RuntimeException("There is no Report with the given UUID: " + reportUUID.toString());
        }*/
        
        Report report = null;
        try {
            String query = "SELECT * FROM Report WHERE reportUUID = '" + reportUUID + "'";
            ResultSet rs = DBUtil.executeQuery(connection, query);
            
            // check if anything got returned (connection closed in finalise method)
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
                throw new RuntimeException("There is no Report with the given UUID: " + reportUUID.toString());
            }
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        
        return report;
    }

    public static Report getReportWithData(UUID reportUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        // get the report without data; may throw an exception
        Report report = ReportHelper.getReport(reportUUID, connection, false);
        
        // get the measurements
        try {
            report.getMeasurementSet().setMeasurements(MeasurementHelper.getMeasurementsForTimePeriod(report.getMeasurementSet().getUUID(), report.getFromDate(), report.getToDate(), connection, false));
        } catch (Exception ex) {
            log.error("Caught an exception when getting measurements for report: " + ex.getMessage());
            throw new RuntimeException("Failed to get the report when getting the measurements: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        
        return report;
    }

    public static Report getReportForLatestMeasurement(UUID mSetUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        if (mSetUUID == null)
        {
            log.error("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Report because the connection to the DB is closed");
        }
        
        /*if (!MeasurementSetHelper.objectExists(mSetUUID, connection))
        {
            log.error("There is no MeasurementSet with the given UUID: " + mSetUUID.toString());
            throw new RuntimeException("There is no MeasurementSet with the given UUID: " + mSetUUID.toString());
        }*/
        
        Report report = new Report();
        Date reportDate = new Date();
        Date fromDate = null;
        Date toDate = null;
        Integer numberOfMeasurements = null;
        
        MeasurementSet mSet = new MeasurementSet(mSetUUID);
        /* get the measurement set (won't have values)
        MeasurementSet mSet = null;
        try {
            mSet = MeasurementSetHelper.getMeasurementSet(mSetUUID, false, connection, false);
        } catch (Exception ex) {
            log.error("Failed to generate report, because the MeasurementSet couldn't be retrieved from the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Failed to generate report, because the MeasurementSet couldn't be retrieved from the database: " + ex.getMessage(), ex);
        }*/
        report.setMeasurementSet(mSet);
        
        // get measurements, which will allow the calculation of the other values for the Report object
        boolean exception = false;
        try {
            String query = "SELECT * FROM Measurement WHERE mSetUUID = '" + mSetUUID + "'"
                    + " ORDER BY timeStamp DESC LIMIT 1";
            ResultSet rs = DBUtil.executeQuery(connection, query);
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
				String timeStampStr = rs.getString("timeStamp");
                
                try {
                    fromDate = new Date(Long.parseLong(timeStampStr));
                    toDate = new Date(Long.parseLong(timeStampStr));
                    numberOfMeasurements = 1;
                } catch (Exception ex) {
                    exception = true;
                    log.error("Unable to process measurement(s), so cannot generate the Report: " + ex.getMessage(), ex);
                    throw ex;
                }
            }
            else // nothing in the result set
            {
                exception = true;
                log.error("There are no Measurements for the MeasurementSet with the given UUID: " + mSetUUID.toString());
                throw new RuntimeException("There are no Measurements for the MeasurementSet with the given UUID: " + mSetUUID.toString());
            }
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (exception && closeDBcon)
                connection.close();
        }
        
        report.setReportDate(reportDate);
        report.setFromDate(fromDate);
        report.setToDate(toDate);
        report.setNumberOfMeasurements(numberOfMeasurements);
        
        try {
            ReportHelper.saveReportWithoutMeasurements(report, connection, closeDBcon);
        } catch (Exception ex) {
            log.error("Caught an exception when trying to save the Report object generated: " + ex.getMessage(), ex);
        }
        
        return report;
    }
    
    public static Report getReportForLatestMeasurementWithData(UUID mSetUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        if (mSetUUID == null)
        {
            log.error("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Report because the connection to the DB is closed");
        }
        
        /*if (!MeasurementSetHelper.objectExists(mSetUUID, connection))
        {
            log.error("There is no MeasurementSet with the given UUID: " + mSetUUID.toString());
            throw new RuntimeException("There is no MeasurementSet with the given UUID: " + mSetUUID.toString());
        }*/
        
        Report report = new Report();
        Date reportDate = new Date();
        Date fromDate = null;
        Date toDate = null;
        Integer numberOfMeasurements = null;
        
        MeasurementSet mSet = new MeasurementSet(mSetUUID);
        /* get the measurement set (won't have values)
        MeasurementSet mSet = null;
        try {
            mSet = MeasurementSetHelper.getMeasurementSet(mSetUUID, false, connection, false);
        } catch (Exception ex) {
            log.error("Failed to generate report, because the MeasurementSet couldn't be retrieved from the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Failed to generate report, because the MeasurementSet couldn't be retrieved from the database: " + ex.getMessage(), ex);
        }*/
        report.setMeasurementSet(mSet);
        
        // get measurements, which will allow the calculation of the other values for the Report object
        boolean exception = false;
        try {
            String query = "SELECT * FROM Measurement WHERE mSetUUID = '" + mSetUUID + "'"
                    + " ORDER BY timeStamp DESC LIMIT 1";
            ResultSet rs = DBUtil.executeQuery(connection, query);
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String measurementUUIDstr = rs.getString("measurementUUID");
				String timeStampStr = rs.getString("timeStamp");
                String value = rs.getString("value");
                
                try {
                    UUID measurementUUID = UUID.fromString(measurementUUIDstr);
                    Date timeStamp = new Date(Long.parseLong(timeStampStr));
                    mSet.addMeasurement(new Measurement(measurementUUID, mSetUUID, timeStamp, value));
                    
                    fromDate = new Date(Long.parseLong(timeStampStr));
                    toDate = new Date(Long.parseLong(timeStampStr));
                    numberOfMeasurements = 1;
                } catch (Exception ex) {
                    exception = true;
                    log.error("Unable to process measurement, so cannot generate the Report: " + ex.getMessage(), ex);
                    throw ex;
                }
            }
            else // nothing in the result set
            {
                exception = true;
                log.error("There are no Measurements for the MeasurementSet with the given UUID: " + mSetUUID.toString());
                throw new RuntimeException("There are no Measurements for the MeasurementSet with the given UUID: " + mSetUUID.toString());
            }
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (exception && closeDBcon)
                connection.close();
        }
        
        report.setReportDate(reportDate);
        report.setFromDate(fromDate);
        report.setToDate(toDate);
        report.setNumberOfMeasurements(numberOfMeasurements);
        
        try {
            ReportHelper.saveReportWithoutMeasurements(report, connection, closeDBcon);
        } catch (Exception ex) {
            log.error("Caught an exception when trying to save the Report object generated: " + ex.getMessage(), ex);
        }
        
        return report;
    }

    public static Report getReportForAllMeasurements(UUID mSetUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        if (mSetUUID == null)
        {
            log.error("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Report because the connection to the DB is closed");
        }
        
        /*if (!MeasurementSetHelper.objectExists(mSetUUID, connection))
        {
            log.error("There is no MeasurementSet with the given UUID: " + mSetUUID.toString());
            throw new RuntimeException("There is no MeasurementSet with the given UUID: " + mSetUUID.toString());
        }*/
        
        Report report = new Report();
        Date reportDate = new Date();
        Date fromDate = null;
        Date toDate = null;
        Integer numberOfMeasurements = null;
        
        MeasurementSet mSet = new MeasurementSet(mSetUUID);
        /* get the measurement set (won't have values)
        MeasurementSet mSet = null;
        try {
            mSet = MeasurementSetHelper.getMeasurementSet(mSetUUID, false, connection, false);
        } catch (Exception ex) {
            log.error("Failed to generate report, because the MeasurementSet couldn't be retrieved from the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Failed to generate report, because the MeasurementSet couldn't be retrieved from the database: " + ex.getMessage(), ex);
        }*/
        report.setMeasurementSet(mSet);
        
        // get measurements, which will allow the calculation of the other values for the Report object
        boolean exception = false;
        try {
            String query = "SELECT * FROM Measurement WHERE mSetUUID = '" + mSetUUID + "'";
            ResultSet rs = DBUtil.executeQuery(connection, query);
            int numRows = 0;
            Long timeStampFrom = null;
            Long timeStampTo = null;
            
            // check if anything got returned (connection closed in finalise method)
            while (rs.next())
            {
				String timeStampStr = rs.getString("timeStamp");
                
                try {
                    long timeStamp = Long.parseLong(timeStampStr);
                    if (numRows == 0)
                    {
                        timeStampFrom = timeStamp;
                        timeStampTo = timeStamp;
                    }
                    else if (timeStamp > timeStampTo)
                    {
                        timeStampTo = timeStamp;
                    }
                    else if (timeStamp < timeStampFrom)
                    {
                        timeStampFrom = timeStamp;
                    }
                } catch (Exception ex) {
                    log.debug("Unable to process measurement for the report (skipping): " + ex.getMessage(), ex);
                }
                numRows++;
            }
            
            if (numRows == 0)
            {
                exception = true;
                throw new RuntimeException("There are no measurements in the database for the given measurement set to generate a report from");
            }
            
            fromDate = new Date(timeStampFrom);
            toDate = new Date(timeStampTo);
            numberOfMeasurements = numRows;
            
        } catch (Exception ex) {
            exception = true;
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (exception && closeDBcon)
                connection.close();
        }
        
        report.setReportDate(reportDate);
        report.setFromDate(fromDate);
        report.setToDate(toDate);
        report.setNumberOfMeasurements(numberOfMeasurements);
        
        try {
            ReportHelper.saveReportWithoutMeasurements(report, connection, false);
        } catch (Exception ex) {
            log.error("Caught an exception when trying to save the Report object generated: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        
        return report;
    }

    public static Report getReportForAllMeasurementsWithData(UUID mSetUUID, Connection connection, boolean closeDBcon) throws Exception
    {
        // get the report without data; may throw an exception
        Report report = ReportHelper.getReportForAllMeasurements(mSetUUID, connection, false);
        
        // get the actual measurements for the report
        try {
            report.getMeasurementSet().setMeasurements(MeasurementHelper.getMeasurementsForTimePeriod(report.getMeasurementSet().getUUID(), report.getFromDate(), report.getToDate(), connection, false));
        } catch (Exception ex) {
            log.error("Caught an exception when getting measurements for report: " + ex.getMessage());
            throw new RuntimeException("Failed to get the report when getting the measurements: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        
        return report;
    }
    
    public static Report getReportForMeasurementsAfterDate(UUID mSetUUID, Date fromDate, Connection connection, boolean closeDBcon) throws Exception
    {
        if (mSetUUID == null)
        {
            log.error("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
        }
        
        if (fromDate == null)
        {
            log.error("Cannot generate a Report object for the MeasurementSet from the given date because it is NULL!");
            throw new NullPointerException("Cannot generate a Report object for the MeasurementSet from the given date because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Report because the connection to the DB is closed");
        }
        
        /*if (!MeasurementSetHelper.objectExists(mSetUUID, connection))
        {
            log.error("There is no MeasurementSet with the given UUID: " + mSetUUID.toString());
            throw new RuntimeException("There is no MeasurementSet with the given UUID: " + mSetUUID.toString());
        }*/
        
        Report report = new Report();
        Date reportDate = new Date();
        Date toDate = null;
        Integer numberOfMeasurements = null;
        
        MeasurementSet mSet = new MeasurementSet(mSetUUID);
        /* get the measurement set (won't have values)
        MeasurementSet mSet = null;
        try {
            mSet = MeasurementSetHelper.getMeasurementSet(mSetUUID, false, connection, false);
        } catch (Exception ex) {
            log.error("Failed to generate report, because the MeasurementSet couldn't be retrieved from the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Failed to generate report, because the MeasurementSet couldn't be retrieved from the database: " + ex.getMessage(), ex);
        }*/
        report.setMeasurementSet(mSet);
        
        // get measurements, which will allow the calculation of the other values for the Report object
        boolean exception = false;
        try {
            String query = "SELECT * FROM Measurement WHERE mSetUUID = '" + mSetUUID + "'"
                    + " AND timeStamp >= " + fromDate.getTime();
            ResultSet rs = DBUtil.executeQuery(connection, query);
            int numRows = 0;
            Long timeStampTo = null;
            
            // check if anything got returned (connection closed in finalise method)
            while (rs.next())
            {
				String timeStampStr = rs.getString("timeStamp");   
                try {
                    long timeStamp = Long.parseLong(timeStampStr);
                    if (numRows == 0)
                    {
                        timeStampTo = timeStamp;
                    }
                    else if (timeStampTo < timeStamp)
                    {
                        timeStampTo = timeStamp;
                    }
                } catch (Exception ex) {
                    log.error("Unable to process measurement for the report (skipping): " + ex.getMessage(), ex);
                }
                numRows++;
            }
            
            if (numRows == 0)
            {
                exception = true;
                throw new RuntimeException("There are no measurements in the database for the given time period of the measurement set to generate a report from");
            }
            
            toDate = new Date(timeStampTo);
            numberOfMeasurements = numRows;
            
        } catch (Exception ex) {
            exception = true;
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (exception && closeDBcon)
                connection.close();
        }
        
        report.setReportDate(reportDate);
        report.setFromDate(fromDate);
        report.setToDate(toDate);
        report.setNumberOfMeasurements(numberOfMeasurements);
        
        try {
            ReportHelper.saveReportWithoutMeasurements(report, connection, false);
        } catch (Exception ex) {
            log.error("Caught an exception when trying to save the Report object generated: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        
        return report;
    }
    
    public static Report getReportForMeasurementsAfterDateWithData(UUID mSetUUID, Date fromDate, Connection connection, boolean closeDBcon) throws Exception
    {
        // get the report without data; may throw an exception
        Report report = ReportHelper.getReportForMeasurementsAfterDate(mSetUUID, fromDate, connection, false);
        
        // get the actual measurements for the report
        try {
            report.getMeasurementSet().setMeasurements(MeasurementHelper.getMeasurementsForTimePeriod(report.getMeasurementSet().getUUID(), report.getFromDate(), report.getToDate(), connection, false));
        } catch (Exception ex) {
            log.error("Caught an exception when getting measurements for report: " + ex.getMessage());
            throw new RuntimeException("Failed to get the report when getting the measurements: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        
        return report;
    }

    public static Report getReportForMeasurementsForTimePeriod(UUID mSetUUID, Date fromDate, Date toDate, Connection connection, boolean closeDBcon) throws Exception
    {
        if (mSetUUID == null)
        {
            log.error("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot generate a Report object for the MeasurementSet with the given UUID because it is NULL!");
        }
        
        if (fromDate == null)
        {
            log.error("Cannot generate a Report object for the MeasurementSet from the given date because it is NULL!");
            throw new NullPointerException("Cannot generate a Report object for the MeasurementSet from the given date because it is NULL!");
        }
        
        if (DBUtil.isClosed(connection))
        {
            log.error("Cannot get the Report because the connection to the DB is closed");
            throw new RuntimeException("Cannot get the Report because the connection to the DB is closed");
        }
        
        /*if (!MeasurementSetHelper.objectExists(mSetUUID, connection))
        {
            log.error("There is no MeasurementSet with the given UUID: " + mSetUUID.toString());
            throw new RuntimeException("There is no MeasurementSet with the given UUID: " + mSetUUID.toString());
        }*/
        
        Report report = new Report();
        Date reportDate = new Date();
        Integer numberOfMeasurements = null;
        
        MeasurementSet mSet = new MeasurementSet(mSetUUID);
        /* get the measurement set (won't have values)
        MeasurementSet mSet = null;
        try {
            mSet = MeasurementSetHelper.getMeasurementSet(mSetUUID, false, connection, false);
        } catch (Exception ex) {
            log.error("Failed to generate report, because the MeasurementSet couldn't be retrieved from the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Failed to generate report, because the MeasurementSet couldn't be retrieved from the database: " + ex.getMessage(), ex);
        }*/
        report.setMeasurementSet(mSet);
        
        // get measurements, which will allow the calculation of the other values for the Report object
        boolean exception = false;
        try {
            //String query = "SELECT * FROM Measurement WHERE mSetUUID = '" + mSetUUID + "'"
            //        + " AND timeStamp BETWEEN " + fromDate.getTime() + " AND " + toDate.getTime();
            String query = "SELECT COUNT(*) FROM Measurement WHERE mSetUUID = '" + mSetUUID + "'"
                    + " AND timeStamp BETWEEN " + fromDate.getTime() + " AND " + toDate.getTime();
            ResultSet rs = DBUtil.executeQuery(connection, query);
            int numRows = 0;
            
            // count how many measurements we've got, if any
            if (rs.next())
            {
                //numRows++;
                numRows = rs.getInt("count");
            }
            
            if (numRows == 0)
            {
                exception = true;
                throw new RuntimeException("There are no measurements in the database for the given time period of the measurement set to generate a report from");
            }
            numberOfMeasurements = numRows;
            
        } catch (Exception ex) {
            exception = true;
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (exception && closeDBcon)
                connection.close();
        }
        
        report.setReportDate(reportDate);
        report.setFromDate(fromDate);
        report.setToDate(toDate);
        report.setNumberOfMeasurements(numberOfMeasurements);
        
        try {
            ReportHelper.saveReportWithoutMeasurements(report, connection, false);
        } catch (Exception ex) {
            log.error("Caught an exception when trying to save the Report object generated: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        
        return report;
    }

    public static Report getReportForMeasurementsForTimePeriodWithData(UUID mSetUUID, Date fromDate, Date toDate, Connection connection, boolean closeDBcon) throws Exception
    {
        // get the report without data; may throw an exception
        Report report = ReportHelper.getReportForMeasurementsForTimePeriod(mSetUUID, fromDate, toDate, connection, false);
        
        // get the actual measurements for the report
        try {
            report.getMeasurementSet().setMeasurements(MeasurementHelper.getMeasurementsForTimePeriod(report.getMeasurementSet().getUUID(), report.getFromDate(), report.getToDate(), connection, false));
        } catch (Exception ex) {
            log.error("Caught an exception when getting measurements for report: " + ex.getMessage());
            throw new RuntimeException("Failed to get the report when getting the measurements: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                connection.close();
        }
        
        return report;
    }
}