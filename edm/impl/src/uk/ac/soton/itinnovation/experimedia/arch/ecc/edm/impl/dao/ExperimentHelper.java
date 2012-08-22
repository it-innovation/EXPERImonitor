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
//      Created Date :          2012-08-22
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.dao;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DatabaseConnector;

/**
 *
 * @author Vegard Engen
 */
public class ExperimentHelper
{
    static Logger log = Logger.getLogger(ExperimentHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(Experiment exp, DatabaseConnector dbCon) throws Exception
    {
        if (exp == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Experiment object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, name
        
        if (exp.getUUID() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Experiment UUID is NULL"));
        }
        
        if (exp.getName() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Experiment name is NULL"));
        }
        
        // check if it exists in the DB already
        try {
            if (objectExists(exp.getUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Experiment already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
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
     * @param exp
     * @param valueNames
     * @param values 
     */
    public static void getTableNamesAndValues(Experiment exp, List<String> valueNames, List<String> values)
    {
        if (exp == null)
            return;
        
        if ((valueNames == null) || (values == null))
            return;
        
        valueNames.add("expUUID");
        values.add("'" + exp.getUUID().toString() + "'");
        
        valueNames.add("name");
        values.add("'" + exp.getName() + "'");
        
        if (exp.getDescription() != null)
        {
            valueNames.add("description");
            values.add("'" + exp.getDescription() + "'");
        }
        
        if (exp.getStartTime() != null)
        {
            valueNames.add("startTime");
            values.add(String.valueOf(exp.getStartTime().getTime()));
        }
        
        if (exp.getEndTime() != null)
        {
            valueNames.add("endTime");
            values.add(String.valueOf(exp.getEndTime().getTime()));
        }
        
        if (exp.getExperimentID() != null)
        {
            valueNames.add("expID");
            values.add("'" + exp.getExperimentID() + "'");
        }
    }
    
    public static boolean objectExists(UUID uuid, DatabaseConnector dbCon) throws Exception
    {
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT expUUID FROM Experiment WHERE expUUID = '" + uuid + "'";
            ResultSet rs = dbCon.executeQuery(query);

            // check if anything got returned (connection closed in finalise method)
            if (rs.next()) {
                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            dbCon.close();
        }
    }
}
