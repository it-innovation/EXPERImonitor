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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao;

import java.util.Date;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;

/**
 * A DAO to save and get Report objects from storage, which gives access to 
 * measurement sets and measurements (optional).
 * 
 * OBS: the get methods will return all sub-classes except for measurements. To
 * get actual measurements for a measurement set, one of the specific getMeasurement(s)
 * methods need to be called, passing on the UUID of the measurement set (and
 * any other arguments for the respective method).
 * 
 * OBS: no delete methods yet.
 * OBS: no update methods yet.
 * 
 * @author Vegard Engen
 */
public interface IReportDAO
{
    /**
     * Saves a report, which must have a unique UUID and refer to an existing
     * measurement set (by its UUID).
     * @param report The report object to be saved.
     * @throws Exception If there's a technical issue or a report with the same UUID already exists.
     */
    public void saveReport(Report report) throws Exception;
    
    /**
     * Get a report object from its UUID, which will contain sub-classes except
     * for measurements.
     * @param reportUUID The report UUID.
     * @return A report object, if it exists.
     * @throws Exception If there's a technical issue or there is no report with the given UUID.
     */
    public Report getReport(UUID reportUUID) throws Exception;
    
    /**
     * Get a report object from its UUID, with any measurements that may exist.
     * @param reportUUID The report UUID.
     * @return A report object, if it exists.
     * @throws Exception If there's a technical issue or there is no report with the given UUID.
     */
    public Report getReportWithData(UUID reportUUID) throws Exception;
    
    /**
     * Get a report for the latest measurement for a particular measurement set.
     * Will contain sub-classes, except for measurements.
     * @param measurementSetUUID The measurement set UUID.
     * @return A report object, if the measurement set exists, and there are measurements.
     * @throws Exception If there's a technical issue, if there is no measurement set with the given UUID or there are no measurements.
     */
    public Report getReportForLatestMeasurement(UUID measurementSetUUID) throws Exception;
    
    /**
     * Get a report for the latest measurement for a particular measurement set.
     * Will contain the measurements.
     * @param measurementSetUUID The measurement set UUID.
     * @return A report object, if the measurement set exists, and there are measurements.
     * @throws Exception If there's a technical issue, if there is no measurement set with the given UUID or there are no measurements.
     */
    public Report getReportForLatestMeasurementWithData(UUID measurementSetUUID) throws Exception;
    
    /**
     * Get a report for all measurements for a particular measurement set.
     * Will contain sub-classes, except for measurements.
     * @param measurementSetUUID The measurement set UUID.
     * @return Empty set if there are no measurements.
     * @throws Exception If there's a technical issue or there is no measurement set with the given UUID.
     */
    public Report getReportForAllMeasurements(UUID measurementSetUUID) throws Exception;
    
    /**
     * Get a report for all measurements for a particular measurement set.
     * Will contain the measurements.
     * @param measurementSetUUID The measurement set UUID.
     * @return Empty set if there are no measurements.
     * @throws Exception If there's a technical issue or there is no measurement set with the given UUID.
     */
    public Report getReportForAllMeasurementsWithData(UUID measurementSetUUID) throws Exception;
    
    /**
     * Get a report for all measurements for a particular measurement set after a given date.
     * Will contain sub-classes, except for measurements.
     * @param measurementSetUUID The measurement set UUID.
     * @param fromDate The date from which measurements should be given.
     * @return Empty set if there are no measurements.
     * @throws Exception If there's a technical issue; if there is no measurement set with the given UUID; if the date is invalid.
     */
    public Report getReportForMeasurementsAfterDate(UUID measurementSetUUID, Date fromDate) throws Exception;
    
    /**
     * Get a report for all measurements for a particular measurement set after a given date.
     * Will contain the measurements.
     * @param measurementSetUUID The measurement set UUID.
     * @param fromDate The date from which measurements should be given.
     * @return Empty set if there are no measurements.
     * @throws Exception If there's a technical issue; if there is no measurement set with the given UUID; if the date is invalid.
     */
    public Report getReportForMeasurementsAfterDateWithData(UUID measurementSetUUID, Date fromDate) throws Exception;
    
    /**
     * Get a report for the measurements for a particular measurement set within a given time
     * period. Will contain sub-classes, except for measurements.
     * @param measurementSetUUID The measurement set UUID.
     * @param fromDate The from date of the time period.
     * @param toDate The to date of the time period.
     * @return Empty set if there are no measurements.
     * @throws Exception If there's a technical issue; if there is no measurement set with the given UUID; if the dates are invalid.
     */
    public Report getReportForMeasurementsForTimePeriod(UUID measurementSetUUID, Date fromDate, Date toDate) throws Exception;
    
    /**
     * Get a report for the measurements for a particular measurement set within a given time
     * period. Will contain the measurements.
     * @param measurementSetUUID The measurement set UUID.
     * @param fromDate The from date of the time period.
     * @param toDate The to date of the time period.
     * @return Empty set if there are no measurements.
     * @throws Exception If there's a technical issue; if there is no measurement set with the given UUID; if the dates are invalid.
     */
    public Report getReportForMeasurementsForTimePeriodWithData(UUID measurementSetUUID, Date fromDate, Date toDate) throws Exception;
}
