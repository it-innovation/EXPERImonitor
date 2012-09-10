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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * A helper class to connect to a database and execute queries on it.
 * 
 * @author Vegard Engen
 */
public class DatabaseConnector
{
    static Logger logger = Logger.getLogger(DatabaseConnector.class);
    
    // connection reference to the db
    private Connection connection = null;
    
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
     */
    public DatabaseConnector(String dbURL, String dbName, String userName, String password, DatabaseType type)
    {
        this();
        
        this.dbURL = dbURL;
        this.dbName = dbName;
        this.userName = userName;
        this.password = password;
        this.dbType = type;
        
        if ((this.dbURL != null) && (this.dbName != null) && (this.userName != null) && (this.password != null) && (this.dbType != null)) {
            initialised = true;
        }
        else {
            initialised = false;
            logger.error("DatabaseConnector not initialised properly, one of the config parameters were NULL");
        }
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
    public void initialise (String dbURL, String dbName, String userName, String password, DatabaseType type)
    {
        this.dbURL = dbURL;
        this.dbName = dbName;
        this.userName = userName;
        this.password = password;
        this.dbType = type;
        
        if ((this.dbURL != null) && (this.dbName != null) && (this.userName != null) && (this.password != null) && (this.dbType != null)) {
            initialised = true;
        }
        else {
            initialised = false;
            logger.error("DatabaseConnector not initialised properly, one of the config parameters were NULL");
        }
    }

    /**
     * Connect to the data base, as per parameters given either in the overloaded
     * constructor, via the initialise method, or if the overloaded connect method
     * has been called successfully once before.
     * 
     * @throws Exception If any errors occur is establishing a connection to the database.
     */
    public void connect() throws Exception
    {
        if (initialised) {
            connect(dbURL, dbName, userName, password, dbType);
        } else {
            logger.error("Cannot connect to the database, because connection details have not been given. Try the overloaded connect method, or use the overloaded constructor when creating this DatabaseConnector object!");
            throw new RuntimeException("Cannot connect to the database, because connection details have not been given. Try the overloaded connect method, or use the overloaded constructor when creating this DatabaseConnector object!");
        }
    }

    /**
     * Connect to the database as per the parameters given. If a connection is
     * successfully made, the parameters are saved, so the connect() method can
     * be called without passing on the parameters again.
     * 
     * If there is already a connection with the database, a new one is not made.
     * 
     * @param dbURL The URL of the database.
     * @param dbName The name of the database.
     * @param userName The username used to connect to the database.
     * @param password The password used to connect to the database.
     * @param type The type of database.
     * @throws Exception If any errors occur is establishing a connection to the database.
     */
    public void connect(String dbURL, String dbName, String userName, String password, DatabaseType type) throws Exception
    {
        // check if already connected
        if (isConnected())
        {
            logger.debug("Call to connect, but already connected...");
            return;
        }
        
        if ((this.dbURL == null) || (this.dbName == null) || (this.userName == null) || (this.password == null) || (this.dbType == null))
        {
            logger.error("One or more of the parameters specifying the DB connection are NULL, so cannot connect!");
            throw new NullPointerException("One or more of the parameters specifying the DB connection are NULL, so cannot connect!");
        }
        
        try {
            if (!drivers.containsKey(type)) {
                logger.error("The database type (" + type + ") is not supported");
                throw new RuntimeException("The database type (" + type + ") is not supported");
            }

            Class.forName(drivers.get(type));
        } catch (ClassNotFoundException e) {
            logger.error("Did not find the JDBC Driver for " + type, e);
            throw new RuntimeException("Did not find the JDBC Driver for " + type, e);
        }

        //connect to the database
        try {
            Properties props = new Properties();
            props.setProperty("user", userName);
            props.setProperty("password", password);
            props.setProperty("allowMultiQueries", "true"); //this will allow running scripts from files
            connection = DriverManager.getConnection("jdbc:" + type.toString().toLowerCase() + "://" + dbURL + "/" + dbName, props);
        } catch (SQLException e) {
            logger.error("Failed to connect to the database: " + e.getMessage(), e);
            throw new RuntimeException("Failed to connect to the database " + dbName + ": " + e.getMessage(), e);
        }

        if (connection == null) {
            logger.error("Failed to connect to the database: " + dbName);
            throw new RuntimeException("Failed to connect to the database: " + dbName);
        }

        // saving the connection details for future connections, so that the 'connect()' method can be used later
        this.dbURL = dbURL;
        this.dbName = dbName;
        this.userName = userName;
        this.password = password;
        this.dbType = type;
        
        initialised = true;
    }

    /**
     * Close the connection to the database.
     * Any SQL exception caused by this operation is caught within this method and logged.
     */
    public void close()
    {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                logger.error("SQLException caught when trying to close the DB connection", ex);
            }
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
    public boolean isConnected()
    {
        if (connection == null)
            return false;
        
        try {
            return !connection.isClosed();
        } catch (SQLException ex) {
            logger.error("SQLException caught when check if the connection was open: " + ex.getMessage(), ex);
            return false;
        }
    }
    
    /**
     * Check if the connection with the database is closed.
     * OBS: will return true if a connection object has not been created (via
     * calling the connect method at least once), OR if an SQL exception is caught
     * when checking of the connection is closed.
     * 
     * @return True if closed; false otherwise.
     */
    public boolean isClosed()
    {
        if (connection == null)
            return true;
        
        try {
            return connection.isClosed();
        } catch (SQLException ex) {
            logger.error("SQLException caught when check if the connection was open: " + ex.getMessage(), ex);
            return true;
        }
    }
    
    /**
     * Get a connection object to the database.
     * @return A Connection object if connected to the database.
     * @throws Exception Throws an exception if not connected to the database, or any other technical exception.
     */
    public Connection getConnection() throws Exception
    {
        if (isClosed())
            throw new RuntimeException("Cannot give a connection that's not been established yet - use the connect method first!");
        
        return this.connection;
    }

    /**
     * Execute a SQL query, as defined in the String parameter. This will execute
     * the query with setting Statement.NO_GENERATED_KEYS.
     * @param query The SQL query.
     * @return ResultSet object.
     * @throws Exception If a connection has not been made, if the query parameter is NULL or any SQLException occurring when executing the query.
     */
    public ResultSet executeQuery(String query) throws Exception
    {
        return executeQuery(query, Statement.NO_GENERATED_KEYS);
    }
    
    /**
     * Execute a SQL query, as defined in the String parameter, which will
     * @param query The SQL query.
     * @param autoGeneratedKeys a constant indicating whether auto-generated keys should be made available for retrieval using the method getGeneratedKeys; one of the following constants: Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS
     * @return ResultSet object.
     * @throws Exception If a connection has not been made, if the query parameter is NULL or any SQLException occurring when executing the query.
     */
    public ResultSet executeQuery(String query, int autoGeneratedKeys) throws Exception
    {
        if (connection == null) {
            logger.error("Cannot execute the query because no connection has been made");
            throw new RuntimeException("Cannot execute the query because no connection has been made");
        }
        
        if (query == null) {
            logger.error("Cannot execute the query because the query is NULL");
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
            logger.error("Error while executing query: " + query, ex);
            throw ex;
        }

        return rs;
    }

    @Override
    public void finalize()
    {
        try {
            close();
            super.finalize();
        } catch (Throwable t) {
            logger.debug("Error caught when trying to close the DB connection", t);
        }
    }
}
