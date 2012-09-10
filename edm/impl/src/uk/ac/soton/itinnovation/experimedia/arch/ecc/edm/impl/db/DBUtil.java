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
//      Created for Project :   
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;

/**
 *
 * @author Vegard Engen
 */
public class DBUtil
{
    static Logger log = Logger.getLogger(DBUtil.class);
    
    public static String getInsertIntoQuery(String tableName, List<String> valueNames, List<String> values)
    {
        String query = "INSERT INTO " + tableName + " (";
        
        for (int i = 0; i < valueNames.size(); i++)
        {
            query += valueNames.get(i);
            
            if ((i+1) < valueNames.size())
                query += ", ";
        }
        
        query += ") VALUES (";
        
        for (int i = 0; i < values.size(); i++)
        {
            query += values.get(i);
            
            if ((i+1) < values.size())
                query += ", ";
        }
        
        query += ")";
        
        return query;
    }
    
    // check if a object exists in the DB
    public static boolean objectExistsByUUID(String tableName, String keyName, UUID uuid, DatabaseConnector dbCon, boolean closeDBcon) throws Exception
    {
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT " + keyName + " FROM " + tableName + " WHERE " + keyName + " = '" + uuid + "'";
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
            if (closeDBcon)
                dbCon.close();
        }
    }
}
