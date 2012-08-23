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
//      Created for Project :   
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.dao;

import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DatabaseConnector;

/**
 *
 * @author Vegard Engen
 */
public class ReportHelper
{
    static Logger log = Logger.getLogger(ReportHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(Report report, DatabaseConnector dbCon, boolean checkForMeasurementSet) throws Exception
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
        
        // check if it exists in the DB already
        try {
            if (objectExists(report.getUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Report already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }
        
        // if checking for measurement set, if flag is set
        if (checkForMeasurementSet)
        {
            if (!MeasurementSetHelper.objectExists(report.getMeasurementSet().getUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Reports's MeasurementSet does not exist (UUID: " + report.getMeasurementSet().getUUID().toString() + ")"));
            }
        }
        
        return new ValidationReturnObject(true);
    }
    
    /**
     * Checks the available parameters in the object and adds to the lists, the
     * table names and values accordingly.
     * 
     * OBS: it is assumed that the object has been validated to have at least the
     * minimum information.
     * 
     * @param report
     * @param valueNames
     * @param values 
     */
    public static void getTableNamesAndValues(Report report, List<String> valueNames, List<String> values)
    {
        if (report == null)
            return;
        
        if ((valueNames == null) || (values == null))
            return;
        
        valueNames.add("reportUUID");
        values.add("'" + report.getUUID().toString() + "'");
        
        valueNames.add("mSetUUID");
        values.add("'" + report.getMeasurementSet().getUUID().toString() + "'");
        
        valueNames.add("fromDateTimeStamp");
        values.add(String.valueOf(report.getFromDate().getTime()));
        
        valueNames.add("toDateTimeStamp");
        values.add(String.valueOf(report.getToDate().getTime()));
        
        valueNames.add("numMeasurements");
        values.add(String.valueOf(report.getNumberOfMeasurements()));
    }
    
    public static boolean objectExists(UUID uuid, DatabaseConnector dbCon) throws Exception
    {
        return DBUtil.objectExistsByUUID("Report", "reportUUID", uuid, dbCon);
    }
}
