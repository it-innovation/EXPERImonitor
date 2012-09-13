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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;

/**
 * A utility class for database operations, such as checking if a connection
 * to the database is open or closed, and executing queries.
 * 
 * @author Vegard Engen
 */
public class DBUtil
{
    static Logger log = Logger.getLogger(DBUtil.class);
    
    /**
     * Check if the connection with the database is closed.
     * OBS: will return true if a connection object is NULL OR if an SQL exception
     * is caught when checking of the connection is closed.
     * 
     * @return True if closed; false otherwise.
     */
    public static boolean isClosed(Connection connection)
    {
        if (connection == null)
            return true;
        
        try {
            return connection.isClosed();
        } catch (SQLException ex) {
            log.debug("SQLException caught when check if the connection was closed: " + ex.getMessage(), ex);
            return true;
        }
    }
    
    /**
     * Check if the connection with the database is open.
     * OBS: will return false if a connection object has not been created (via
     * calling the connect method at least once), OR if an SQL exception is caught
     * when checking of the connection is closed.
     * 
     * @return True if connected; false otherwise.
     */
    public static boolean isConnected(Connection connection)
    {
        if (connection == null)
            return false;
        
        try {
            return !connection.isClosed();
        } catch (SQLException ex) {
            log.debug("SQLException caught when check if the connection was open: " + ex.getMessage(), ex);
            return false;
        }
    }
    
    /**
     * Execute a SQL query, as defined in the String parameter. This will execute
     * the query with setting Statement.NO_GENERATED_KEYS.
     * @param query The SQL query.
     * @return ResultSet object.
     * @throws Exception If a connection has not been made, if the query parameter is NULL or any SQLException occurring when executing the query.
     */
    public static ResultSet executeQuery(Connection connection, String query) throws Exception
    {
        return executeQuery(connection, query, Statement.NO_GENERATED_KEYS);
    }
    
    /**
     * Execute a SQL query, as defined in the String parameter, which will
     * @param query The SQL query.
     * @param autoGeneratedKeys a constant indicating whether auto-generated keys should be made available for retrieval using the method getGeneratedKeys; one of the following constants: Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
     * @return ResultSet object.
     * @throws Exception If a connection has not been made, if the query parameter is NULL or any SQLException occurring when executing the query.
     */
    public static ResultSet executeQuery(Connection connection, String query, int autoGeneratedKeys) throws Exception
    {
        if (connection == null) {
            log.error("Cannot execute the query because no connection has been made");
            throw new RuntimeException("Cannot execute the query because no connection has been made");
        }
        
        if (query == null) {
            log.error("Cannot execute the query because the query is NULL");
            throw new NullPointerException("Cannot execute the query because the query is NULL");
        }
        
        ResultSet rs = null;
        try {
            Statement s = connection.createStatement();
            if (s.execute(query, autoGeneratedKeys)) {
                rs = s.getResultSet();
            } else {
                rs = s.getGeneratedKeys();
            }
        } catch (Exception ex) {
            log.error("Error while executing query: " + query, ex);
            throw ex;
        }

        return rs;
    }
    
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
    public static boolean objectExistsByUUID(String tableName, String keyName, UUID uuid, Connection connection, boolean closeDBcon) throws Exception
    {
        if (connection == null)
        {
            log.error("Cannot check that the object exists in the database, because the connection is NULL");
            throw new RuntimeException("Cannot check that the object exists in the database, because the connection is NULL");
        }
        
        if (isClosed(connection))
        {
            log.error("Cannot check that the object exists in the database, because the connection is closed");
            throw new RuntimeException("Cannot check that the object exists in the database, because the connection is closed");
        }
        
        try {
            String query = "SELECT " + keyName + " FROM " + tableName + " WHERE " + keyName + " = '" + uuid + "'";
            ResultSet rs = executeQuery(connection, query);

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
                connection.close();
        }
    }
}
