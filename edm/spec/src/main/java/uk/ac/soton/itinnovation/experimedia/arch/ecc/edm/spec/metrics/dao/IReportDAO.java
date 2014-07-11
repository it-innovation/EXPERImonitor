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
//      Created Date :          2012-08-21
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao;

import java.util.Date;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.NoDataException;

/**
 * A DAO to save and get Report objects from storage, which gives access to 
 * measurement sets and measurements (optional).
 * 
 * OBS: the get methods will return all sub-classes except for measurements. To
 * get actual measurements for a measurement set, one of the specific getMeasurement(s)
 * methods need to be called, passing on the UUID of the measurement set (and
 * any other arguments for the respective method).
 * 
 * @author Vegard Engen
 */
public interface IReportDAO
{
    /**
     * Saves a report, which must have a unique UUID and refer to an existing
     * measurement set (by its UUID). Any measurements already in the database
     * will be ignored.
     * @param report The report object to be saved.
     * @param saveMeasurements Boolean to flag whether measurements should be saved too.
     * @throws IllegalArgumentException If the Report is not valid to be saved, typically due to missing information (e.g., NULL values).
     * @throws Exception If there's a technical issue or a report with the same UUID already exists.
     */
    void saveReport(Report report, boolean saveMeasurements) throws IllegalArgumentException, Exception;
    
    /**
     * Saves the measurements for a report, not the report itself. Note that if one or 
     * more of the measurements already exists, they will simply be ignored.
     * @param report The report object, which should contain a measurement set with measurements.
     * @throws IllegalArgumentException If the Measurement objects are not valid to be saved, typically due to missing information (e.g., NULL values).
     * @throws Exception If there's a technical issue.
     */
    void saveMeasurements(Report report) throws IllegalArgumentException, Exception;
    
    /**
     * Get a report object from its UUID. Will contain a MeasurementSet with
     * the UUID to identify it, and measurements if the respective flag is true.
     * @param reportUUID The report UUID.
     * @param withMeasurements Will include the measurements if this flag is set to true.
     * @return A report object, if it exists.
     * @throws IllegalArgumentException If reportUUID is not a valid argument (e.g., NULL).
     * @throws NoDataException If there's no report with the given UUID.
     * @throws Exception If there's a technical issue.
     */
    Report getReport(UUID reportUUID, boolean withMeasurements) throws IllegalArgumentException, NoDataException, Exception;
    
    /**
     * Get a report for the latest measurement for a particular measurement set.
     * This does not return the last Report saved in the database, but will create
     * a new Report instance based on the last Measurement in the database. This
     * Report instance will not be saved in the database, so you must save it with the
     * saveReport() method if this is desirable.
     * The Report instance will contain a MeasurementSet with the UUID to identify 
     * it, and measurements if the respective flag is true.
     * @param measurementSetUUID The measurement set UUID.
     * @param withMeasurements Will include the measurements if this flag is set to true.
     * @return A report object, if the measurement set exists, and there are measurements.
     * @throws IllegalArgumentException If measurementSetUUID is not a valid argument (e.g., NULL).
     * @throws NoDataException If there is no measurement set with the given UUID or there are no measurements.
     * @throws Exception If there's a technical issue.
     */
    Report getReportForLatestMeasurement(UUID measurementSetUUID, boolean withMeasurements) throws IllegalArgumentException, NoDataException, Exception;
    
    /**
     * Get a report for all measurements for a particular measurement set.
     * This does not return any existing Report saved in the database, but will create
     * a new Report instance based on the last Measurement in the database. This
     * Report instance will not be saved in the database, so you must save it with the
     * saveReport() method if this is desirable.
     * The Report instance will contain a MeasurementSet with the UUID to identify 
     * it, and measurements if the respective flag is true.
     * @param measurementSetUUID The measurement set UUID.
     * @param withMeasurements Will include the measurements if this flag is set to true.
     * @return A report object, if the measurement set exists, and there are measurements.
     * @throws IllegalArgumentException If measurementSetUUID is not a valid argument (e.g., NULL).
     * @throws NoDataException If there is no measurement set with the given UUID or there are no measurements.
     * @throws Exception If there's a technical issue.
     */
    Report getReportForAllMeasurements(UUID measurementSetUUID, boolean withMeasurements) throws IllegalArgumentException, NoDataException, Exception;
    
    /**
     * Use this method to retrieve measurement sets up to a given date. 
     * 
     * @param measurementSetID
     * @param tailDate
     * @param count
     * @param withMeasurements
     * @return
     * @throws IllegalArgumentException
     * @throws NoDataException
     * @throws Exception 
     */
    Report getReportForTailMeasurements(UUID measurementSetID, Date tailDate, int count, boolean withMeasurements) throws IllegalArgumentException, NoDataException, Exception;
    
    /**
     * Get a report for all measurements for a particular measurement set from
     * a given date. This is inclusive the given date.
     * This does not return any existing Report saved in the database, but will create
     * a new Report instance based on the last Measurement in the database. This
     * Report instance will not be saved in the database, so you must save it with the
     * saveReport() method if this is desirable.
     * The Report instance will contain a MeasurementSet with the UUID to identify 
     * it, and measurements if the respective flag is true.
     * @param measurementSetUUID The measurement set UUID.
     * @param fromDate The date from which measurements should be given.
     * @param withMeasurements Will include the measurements if this flag is set to true.
     * @return A report object, if the measurement set exists, and there are measurements.
     * @throws IllegalArgumentException If the arguments are not valid (e.g., NULL).
     * @throws NoDataException If there is no measurement set with the given UUID or there are no measurements.
     * @throws Exception If there's a technical issue.
     */
    Report getReportForMeasurementsFromDate(UUID measurementSetUUID, Date fromDate, boolean withMeasurements) throws IllegalArgumentException, NoDataException, Exception;
    
    /**
     * Get a report for the measurements for a particular measurement set within a given time
     * period. 
     * This does not return any existing Report saved in the database, but will create
     * a new Report instance based on the last Measurement in the database. This
     * Report instance will not be saved in the database, so you must save it with the
     * saveReport() method if this is desirable.
     * The Report instance will contain a MeasurementSet with the UUID to identify 
     * it, and measurements if the respective flag is true.
     * @param measurementSetUUID The measurement set UUID.
     * @param fromDate The from date of the time period.
     * @param toDate The to date of the time period.
     * @param withMeasurements Will include the measurements if this flag is set to true.
     * @return A report object, if the measurement set exists, and there are measurements.
     * @throws IllegalArgumentException If the arguments are not valid (e.g., NULL).
     * @throws NoDataException If there is no measurement set with the given UUID or there are no measurements.
     * @throws Exception If there's a technical issue.
     */
    Report getReportForMeasurementsForTimePeriod(UUID measurementSetUUID, Date fromDate, Date toDate, boolean withMeasurements) throws IllegalArgumentException, NoDataException, Exception;
    
    /**
     * Get a report for unsynchronised measurements for a particular measurement 
     * set from the given given date. This is inclusive the date given.
     * This does not return any existing Report saved in the database, but will create
     * a new Report instance based on the last Measurement in the database. This
     * Report instance will not be saved in the database, so you must save it with the
     * saveReport() method if this is desirable.
     * The Report instance will contain a MeasurementSet with the UUID to identify 
     * it, and measurements if the respective flag is true.
     * @param measurementSetUUID The measurement set UUID.
     * @param fromDate The date from which measurements should be given.
     * @param numMeasurements The number of measurements after the date that should be included.
     * @param withMeasurements Will include the measurements if this flag is set to true.
     * @return A report object, if the measurement set exists, and there are measurements.
     * @throws IllegalArgumentException If the arguments are not valid (e.g., NULL).
     * @throws NoDataException If there is no measurement set with the given UUID or there are no measurements.
     * @throws Exception If there's a technical issue.
     */
    Report getReportForUnsyncedMeasurementsFromDate(UUID measurementSetUUID, Date fromDate, int numMeasurements, boolean withMeasurements) throws IllegalArgumentException, NoDataException, Exception;
    
    /**
     * Set the synchronisation flag for measurements of a report.
     * @param reportUUID The UUID of the Report.
     * @param syncFlag Synchronisation flag, set to true if the measurements have
     *                 been synchronised with the ECC EDM; false otherwise.
     * @throws IllegalArgumentException If there Report UUID is not valid (e.g. NULL).
     * @throws Exception If there's a technical issue.
     */
    void setReportMeasurementsSyncFlag(UUID reportUUID, boolean syncFlag) throws IllegalArgumentException, Exception;
    
    /**
     * Delete a report with the given UUID - with the option to also delete any
     * measurements stored with the report originally.
     * @param reportUUID The UUID of the Report.
     * @param withMeasurements Flag to indicate if any measured stored with the
     *                         report should be deleted as well.
     * @throws IllegalArgumentException If there Report UUID is not valid (e.g. NULL).
     * @throws Exception If there's a technical issue.
     */
    void deleteReport(UUID reportUUID, boolean withMeasurements) throws IllegalArgumentException, Exception;
}
