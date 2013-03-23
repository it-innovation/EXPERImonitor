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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.IECCLogger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.Logger;

/**
 * A helper class to connect to a database and execute queries on it.
 * 
 * @author Vegard Engen
 */
public class DatabaseConnector
{
    static IECCLogger log = Logger.getLogger(DatabaseConnector.class);
    
    // database connection details
    private String dbURL = null;
    private String dbName = null;
    private String userName = null;
    private String password = null;
    private DatabaseType dbType = null;
    
    // flag to say if the instance has been initialised with values for the
    // parameters above (to be able to connect to a database)
    private boolean initialised;
    
    // driver details
    private Map<DatabaseType, String> drivers;

    /**
     * Default constructor.
     */
    public DatabaseConnector()
    {
        setUpDrivers();
        initialised = false;
    }

    /**
     * Overloaded constructor, which will store the details required to make a
     * connection to a database. This does not open a connection with the database,
     * which has to be done via the connect method.
     * @param dbURL The URL of the database.
     * @param dbName The name of the database.
     * @param userName The username used to connect to the database.
     * @param password The password used to connect to the database.
     * @param type The type of database.
     * @throws Throwable If there's any issues with the configuration parameters.
     */
    public DatabaseConnector(String dbURL, String dbName, String userName, String password, DatabaseType type) throws Throwable
    {
        this();
        initialise(dbURL, dbName, userName, password, type);
    }

    /**
     * Sets up the drivers required to connect to the supported databases.
     */
    private void setUpDrivers()
    {
        drivers = new EnumMap<DatabaseType, String>(DatabaseType.class);
        drivers.put(DatabaseType.MYSQL, "com.mysql.jdbc.Driver");
        drivers.put(DatabaseType.POSTGRESQL, "org.postgresql.Driver");
    }
    
    /**
     * Initialise the object with the details of the database, which will be stored
     * in memory. This does not open a connection with the database, which has to
     * be done via the connect method.
     * @param dbURL The URL of the database.
     * @param dbName The name of the database.
     * @param userName The username used to connect to the database.
     * @param password The password used to connect to the database.
     * @param type The type of database.
     */
    public final void initialise (String dbURL, String dbName, String userName, String password, DatabaseType type) throws Throwable
    {
        log.debug("Initialising");
        this.dbURL = dbURL;
        this.dbName = dbName;
        this.userName = userName;
        this.password = password;
        this.dbType = type;
        
        log.debug("Checking parameters");
        if (this.dbURL == null) {
            initialised = false;
            log.error("DatabaseConnector not initialised properly, the dbURL is NULL");
        }
        if (this.dbName == null){
            initialised = false;
            log.error("DatabaseConnector not initialised properly, the dbName is NULL");
        }
        if (this.userName == null){
            initialised = false;
            log.error("DatabaseConnector not initialised properly, the userName is NULL");
        }
        if (this.password == null) {
            initialised = false;
            log.error("DatabaseConnector not initialised properly, the password is NULL");
        }
        if (this.dbType == null) {
            initialised = false;
            log.error("DatabaseConnector not initialised properly, the dbType is NULL");
        }
        
        try {
            if (!drivers.containsKey(type)) {
                initialised = false;
                log.error("The database type (" + type + ") is not supported");
                throw new RuntimeException("The database type (" + type + ") is not supported");
            }

            Class.forName(drivers.get(type));
        } catch (ClassNotFoundException e) {
            initialised = false;
            log.error("Did not find the JDBC Driver for " + type, e);
            throw new RuntimeException("Did not find the JDBC Driver for " + type, e);
        }
        
        initialised = true;
    }
    
    /**
     * Get a connection, assuming that the DatabaseConnector has been initialised.
     * @return Connection object.
     * @throws Exception If not initialised or if there's any exceptions thrown from creating the connection.
     */
    public Connection getConnection() throws Exception
    {
        log.debug("Establishing and returning a connection to the database");
        if (!initialised)
        {
            log.error("Cannot connect to the database, because the DatabaseConnector object has not been initialised correctly.");
            throw new RuntimeException("Cannot connect to the database, because the DatabaseConnector object has not been initialised correctly.");
        }
        
        Connection connection = null;
        
        try {
            Properties props = new Properties();
            props.setProperty("user", userName);
            props.setProperty("password", password);
            props.setProperty("allowMultiQueries", "true"); //this will allow running scripts from files
            connection = DriverManager.getConnection("jdbc:" + dbType.toString().toLowerCase() + "://" + dbURL + "/" + dbName, props);
        } catch (SQLException e) {
            log.error("Failed to connect to the database " + dbName + ": " + e.getMessage(), e);
            throw new RuntimeException("Failed to connect to the database " + dbName + ": " + e.getMessage(), e);
        }

        if (connection == null) {
            log.error("Failed to connect to the database: " + dbName);
            throw new RuntimeException("Failed to connect to the database: " + dbName);
        }
        
        return connection;
    }
    
    /**
     * Get a connection, with a given transaction isolation level.
     * This assumes that the DatabaseConnector has been initialised.
     * @param transactionIsolationLevel Should be one of the following: Connection.TRANSACTION_READ_UNCOMMITTED, Connection.TRANSACTION_READ_COMMITTED, Connection.TRANSACTION_REPEATABLE_READ, or Connection.TRANSACTION_SERIALIZABLE
     * @return Connection object.
     * @throws Exception If not initialised or if there's any exceptions thrown from creating the connection.
     */
    public Connection getConnection(int transactionIsolationLevel) throws Exception
    {
        log.debug("Establishing and returning a connection to the database");
        if (!initialised)
        {
            log.error("Cannot connect to the database, because the DatabaseConnector object has not been initialised correctly.");
            throw new RuntimeException("Cannot connect to the database, because the DatabaseConnector object has not been initialised correctly.");
        }
        
        Connection connection = null;
        
        try {
            Properties props = new Properties();
            props.setProperty("user", userName);
            props.setProperty("password", password);
            props.setProperty("allowMultiQueries", "true"); //this will allow running scripts from files
            connection = DriverManager.getConnection("jdbc:" + dbType.toString().toLowerCase() + "://" + dbURL + "/" + dbName, props);
        } catch (SQLException e) {
            log.error("Failed to connect to the database " + dbName + ": " + e.getMessage(), e);
            throw new RuntimeException("Failed to connect to the database " + dbName + ": " + e.getMessage(), e);
        }

        if (connection == null) {
            log.error("Failed to connect to the database: " + dbName);
            throw new RuntimeException("Failed to connect to the database: " + dbName);
        }
        
        connection.setTransactionIsolation(transactionIsolationLevel);
        
        return connection;
    }
}
