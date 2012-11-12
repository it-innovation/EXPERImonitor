/////////////////////////////////////////////////////////////////////////
//
// ¬© University of Southampton IT Innovation Centre, 2011
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
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
//	Created By :			Maxim Bashevoy
//	Created Date :			2011-07-26
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator.sql;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.utils.Triplet;
import eu.wegov.coordinator.utils.Util;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
 *
 * @author Maxim Bashevoy
 */
public class PostgresConnector {

    private Connection connection;
    private String url;
    private String username;
    private String password;
    private String databaseInUseName = "";
    private String defaultSchema = null;
    private String schemaInUseName = "";
	private SqlDatabase database;
	private SqlSchema schema;
    private final static Logger logger = Logger.getLogger(PostgresConnector.class.getName());

    public PostgresConnector(String serverUrl, String username, String password, String defaultSchema) throws MalformedURLException, ClassNotFoundException, SQLException {

        this.url = Util.ensureSlash(serverUrl);
        this.username = username;
        this.password = password;
        this.defaultSchema = defaultSchema;

        Class.forName("org.postgresql.Driver");
        this.connection = DriverManager.getConnection(url.toString(), username, password);
        logger.debug("PostgresConnector initialised at: " + serverUrl);

    }

    public String getDefaultSchema() {
    	return defaultSchema;
    }

    private void updateConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(url.toString(), username, password);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public Connection getConnection() throws SQLException {

        if (connection == null) {
            updateConnection();
        }

        if (connection.isClosed()) {
            updateConnection();
        }

        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getUrl() {
        logger.debug("Returning server URL: " + url);
        return url;
    }

    public void setUrl(String url) {
        this.url = Util.ensureSlash(url);
        logger.debug("Server URL set: " + this.url);
    }

    public boolean execute(String query) throws SQLException {
        Statement statement = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        logger.debug("Executing: " + query + ";");
        return statement.execute(query + ";");
    }

    public ResultSet executeQuery(String query) throws SQLException {
        Statement statement = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        logger.debug("Executing Query: " + query + ";");
        ResultSet result = statement.executeQuery(query + ";");
        return result;
    }

    public String getCurrentDatabaseName() throws SQLException {
//        String databaseName = null;
//
//        ResultSet rs = executeQuery("SELECT current_database()");
//        while (rs.next()) {
//            databaseName = rs.getString("current_database");
//        }
//
//        logger.debug("Returning current database: " + databaseName);

        return databaseInUseName;
    }

    public String getCurrentSchemaName() throws SQLException {
//        String schemaName = null;
//
//        ResultSet rs = executeQuery("SELECT current_schema()");
//        while (rs.next()) {
//            schemaName = rs.getString("current_schema");
//        }
//
//        logger.debug("Returning current schema: " + schemaName);

        return schemaInUseName;
    }

    public ArrayList<String> getDatabasesNames() throws SQLException {
        ArrayList<String> names = new ArrayList<String>();
        ResultSet rs = executeQuery("SELECT datname FROM pg_database");
        while (rs.next()) {
            String name = rs.getString("datname");
            names.add(name);
            logger.debug("Found database: " + name);
        }
        return names;
    }

    public ArrayList<String> getSchemaNames() throws SQLException {
        ArrayList<String> names = new ArrayList<String>();
        ResultSet rs = executeQuery("SELECT nspname FROM pg_namespace");
        while (rs.next()) {
            String name = rs.getString("nspname");
            names.add(name);
            logger.debug("Found schema: " + name);
        }
        return names;
    }

    public SqlDatabase getDatabase() throws SQLException {
    	return getDatabase(databaseInUseName);
    }

    /*
     * Return database by name or create new one
     */
    public SqlDatabase getDatabase(String databaseName) throws SQLException {
        SqlDatabase database = new SqlDatabase(databaseName, this);

        if (!this.getDatabasesNames().contains(databaseName)) {
            this.execute("CREATE DATABASE \"" + databaseName + "\"");
            logger.debug("Created database: " + databaseName);
        }

        this.useDatabase(databaseName);

        return database;
    }

    /*
    public SqlSchema getSchema(SqlDatabase database, String schemaName) throws SQLException {
        //SqlSchema schema = new SqlSchema(schemaName, database);
        schema = new SqlSchema(schemaName, this);
        String databaseName = database.getName();
        this.useDatabase(databaseName);

        if (!this.getSchemaNames().contains(schemaName)) {
            this.execute("CREATE SCHEMA \"" + schemaName + "\"");
            logger.debug("Created schema: " + schemaName + " in database: " + databaseInUseName);
        }

        this.useSchema(schemaName);

        return schema;

    }
    */


    public SqlSchema getSchema() throws SQLException {
    	if (schema == null) {
            //schema = new SqlSchema(defaultSchema, database);
            logger.debug("Getting schema: " + defaultSchema + " in database: " + databaseInUseName);
            schema = new SqlSchema(defaultSchema, this);
            //String databaseName = database.getName();
            //this.useDatabase(databaseName);

            if (!this.getSchemaNames().contains(defaultSchema)) {
                this.execute("CREATE SCHEMA \"" + defaultSchema + "\"");
                logger.debug("Created schema: " + defaultSchema + " in database: " + databaseInUseName);
            }

            this.useSchema(defaultSchema);
    	}

        return schema;

    }
    public void deleteDatabase(String databaseName) throws SQLException {
        this.execute("DROP DATABASE IF EXISTS \"" + databaseName + "\"");
        logger.debug("Deleted database (if it existed): " + databaseName);
        if (getDatabasesNames().contains(databaseName)) {
            throw new SQLException("Failed to delete database \'" + databaseName + "\'. Try disconnecting all users from that database");
        }
        this.useDatabase("");
    }

    public void deleteSchema(String databaseName, String schemaName) throws SQLException {
        this.useDatabase(databaseName);
        this.execute("DROP SCHEMA IF EXISTS \"" + schemaName + "\" CASCADE");
        logger.debug("Deleted schema (if it existed): " + schemaName + " in database: " + databaseName);
        schema = null;
        //defaultSchema = null;
        //this.useDatabase("");
    }

    public void deleteTable(String databaseName, String schemaName, String tableName) throws SQLException {
        this.useDatabase(databaseName);
        this.useSchema(schemaName);
        this.execute("DROP TABLE IF EXISTS \"" + tableName + "\" CASCADE");
        logger.debug("Deleted table (if it existed): " + tableName + " in schema: " + schemaName + " in database: " + databaseName);
        //this.useDatabase("");
    }

    public void useDatabase(String databaseName) throws SQLException {
        String currentDatabaseName = this.getCurrentDatabaseName();
        if (!currentDatabaseName.equals(databaseName)) {
            if (this.connection != null)
            	this.connection.close();
            this.connection = DriverManager.getConnection(url + databaseName, username, password);
            this.databaseInUseName = databaseName;
            logger.debug("Switching to database: " + databaseName);
        }
    }

    public SqlSchema setSchema(String schemaName) throws SQLException {
		this.schemaInUseName = schemaName;
		//schema = new SqlSchema(schemaName, database);
		schema = new SqlSchema(schemaName, this);
		return schema;
    }

    public SqlSchema useSchema(String schemaName) throws SQLException {
    	/*
    	if (defaultSchema == null) {
    		defaultSchema = schemaName;
    		setSchema(schemaName);
    		this.execute("SET search_path TO \"" + schemaName + "\"");
    	}
    	*/
    	if (schemaInUseName.equals("")) {
    		setSchema(schemaName);
    		this.execute("SET search_path TO \"" + schemaName + "\"");
    	}
    	
        String currentSchemaName = this.getCurrentSchemaName();

        /*
        if (!currentSchemaName.equals(schemaName)) {
            System.out.println(defaultSchema + ": Current schema: " + currentSchemaName);
            this.execute("SET search_path TO \"" + schemaName + "\"");
            setSchema(schemaName);
            logger.debug("Switching to schema: " + schemaName);
            System.out.println(defaultSchema + ": Switching to schema: " + schemaName);
        }
        */

        if (!currentSchemaName.equals(schemaName)) {
        	throw new SQLException("Attempted to change connector schema from " + currentSchemaName + " to " + schemaName);
        }

        return schema;
    }

    public String insertRow(SqlDatabase database, SqlSchema schema, Dao object) throws SQLException {
        String id = null;
        String databaseName = database.getName();
        String schemaName = schema.getName();
        String tableName = object.getTableName();

        this.useDatabase(databaseName);
        this.useSchema(schemaName);

        String preparedCommand = "INSERT INTO \"" + tableName + "\" " + object.getKeysAsString() + " VALUES " + object.getValuesAsQMString();
        PreparedStatement p = getConnection().prepareStatement(preparedCommand + ";", Statement.RETURN_GENERATED_KEYS);

        daoToPreparedStatement(object, p);

        logger.debug("Inserting row into database: \'" + databaseName + "\' in schema: \'" + schemaName + "\' in table: \'" + tableName + "\' using prepared statement: \'" + p.toString() + "\'");

        //System.out.println("Inserting row into database: \'" + databaseName + "\' in schema: \'" + schemaName + "\' in table: \'" + tableName + "\' using prepared statement: \'" + p.toString() + "\'");

        p.execute();
        ResultSet rs = p.getGeneratedKeys();

        if (rs != null) {
            while (rs.next()) {
                id = rs.getString(object.returning());
                logger.debug("Key generated: " + id);
            }
        }

        logger.debug("Returning ID: " + id);

        return id;
    }

    private void daoToPreparedStatement(Dao object, PreparedStatement p) throws SQLException {
        int counter = 1;
        for (Triplet<String, String, ?> property : object.getProperties()) {
            String description = property.getDescription();

            if (description.startsWith("SERIAL")) {
                counter--;
            } else if (description.startsWith("character") | description.startsWith("text")) {
                String value = (String) property.getValue();
                p.setString(counter, value);
            } else if (description.startsWith("time")) {
                Timestamp value = (Timestamp) property.getValue();
                p.setTimestamp(counter, value);
            } else if (description.startsWith("int")) {
                int value = (Integer) property.getValue();
                p.setInt(counter, value);
            } else if (description.startsWith("smallint")) {
                short value = (Short) property.getValue();
                p.setShort(counter, value);
            } else if (description.startsWith("boolean")) {
                boolean value = (Boolean) property.getValue();
                p.setBoolean(counter, value);
            } else {
                logger.error("Description not recognised: " + description);
            }

            counter++;
        }
    }

    public ArrayList resultSetAsDao(ResultSet rs, Dao object) throws SQLException {
        ArrayList list = new ArrayList();

        if (rs != null) {
            while (rs.next()) {

                // Create new object
                Dao newObject = object.createNew();

                // Fill it in
                for (Triplet<String, String, ?> property : newObject.getProperties()) {
                    String key = property.getKey();
                    String description = property.getDescription();

                    if (description.startsWith("SERIAL")) {
                        int newValue = rs.getInt(key.toLowerCase());
                        newObject.updateProperty(key, newValue);
                    } else if (description.startsWith("character") | description.startsWith("text")) {
                        String newValue = rs.getString(key.toLowerCase());
                        if (newValue != null)
                            newValue = newValue.trim();
                        newObject.updateProperty(key, newValue);
                    } else if (description.startsWith("time")) {
                        Timestamp newValue = rs.getTimestamp(key.toLowerCase());
                        newObject.updateProperty(key, newValue);
                    } else if (description.startsWith("int")) {
                        int newValue = rs.getInt(key.toLowerCase());
                        newObject.updateProperty(key, newValue);
                    } else if (description.startsWith("smallint")) {
                        Short newValue = rs.getShort(key.toLowerCase());
                        newObject.updateProperty(key, newValue);
                    } else if (description.startsWith("boolean")) {
                        boolean newValue = rs.getBoolean(key.toLowerCase());
                        newObject.updateProperty(key, newValue);
                    } else {
                        logger.error("Description not recognised: " + description);
                    }
                }

                logger.debug(newObject.toString());
                list.add(newObject);
            }
        }

        return list;
    }

    public ArrayList resultSetAsDaoExcludeColumn(ResultSet rs, Dao object, String excludeKey) throws SQLException {
        ArrayList list = new ArrayList();

        if (rs != null) {
            while (rs.next()) {

                // Create new object
                Dao newObject = object.createNew();

                // Fill it in
                for (Triplet<String, String, ?> property : newObject.getProperties()) {
                    String key = property.getKey();
                    if (key.equals(excludeKey))
                    	continue;
                    
                    String description = property.getDescription();

                    if (description.startsWith("SERIAL")) {
                        int newValue = rs.getInt(key.toLowerCase());
                        newObject.updateProperty(key, newValue);
                    } else if (description.startsWith("character") | description.startsWith("text")) {
                        String newValue = rs.getString(key.toLowerCase());
                        if (newValue != null)
                            newValue = newValue.trim();
                        newObject.updateProperty(key, newValue);
                    } else if (description.startsWith("time")) {
                        Timestamp newValue = rs.getTimestamp(key.toLowerCase());
                        newObject.updateProperty(key, newValue);
                    } else if (description.startsWith("int")) {
                        int newValue = rs.getInt(key.toLowerCase());
                        newObject.updateProperty(key, newValue);
                    } else if (description.startsWith("smallint")) {
                        Short newValue = rs.getShort(key.toLowerCase());
                        newObject.updateProperty(key, newValue);
                    } else if (description.startsWith("boolean")) {
                        boolean newValue = rs.getBoolean(key.toLowerCase());
                        newObject.updateProperty(key, newValue);
                    } else {
                        logger.error("Description not recognised: " + description);
                    }
                }

                logger.debug(newObject.toString());
                list.add(newObject);
            }
        }

        return list;
    }

    public ArrayList getAllObjects(SqlDatabase database, SqlSchema schema, Dao object) throws SQLException {

        String databaseName = database.getName();
        String schemaName = schema.getName();
        String tableName = object.getTableName();

        this.useDatabase(databaseName);
        this.useSchema(schemaName);

        logger.debug("Returning all objects: " + object.getClass().getName() + " from database: " + databaseName + ", schema: " + schemaName
                + ", table: " + tableName);

        String query = "SELECT * FROM \"" + tableName + "\"";

        ResultSet rs = executeQuery(query);

        return resultSetAsDao(rs, object);
    }

    public ArrayList getAllObjectsUniqueOn(SqlDatabase database, SqlSchema schema, Dao object, String uniqueColumnName) throws SQLException {

        String databaseName = database.getName();
        String schemaName = schema.getName();
        String tableName = object.getTableName();

        this.useDatabase(databaseName);
        this.useSchema(schemaName);

        logger.debug("Returning all UNIQUE objects: " + object.getClass().getName() + " from database: " + databaseName + ", schema: " + schemaName
                + ", table: " + tableName);

        String query = "SELECT DISTINCT ON (" + uniqueColumnName + ") * FROM \"" + tableName + "\"";

        ResultSet rs = executeQuery(query);

        return resultSetAsDao(rs, object);
    }

    public void setCorrectValueType(PreparedStatement p, Dao object, String key, Object newValue, int counter) throws SQLException {

        if (!object.getAllKeysAsArray().contains(key))
            throw new SQLException("Object " + object.getClass().getName() + " does not contain key: " + key);

        String description = object.getDescriptionForKey(key);

        if (description.startsWith("SERIAL")) {
            int value = (Integer) newValue;
            p.setInt(counter, value);
        } else if (description.startsWith("character") | description.startsWith("text")) {
            String value = (String) newValue;
            p.setString(counter, value);
        } else if (description.startsWith("time")) {
            Timestamp value = (Timestamp) newValue;
            p.setTimestamp(counter, value);
        } else if (description.startsWith("int")) {
            int value = (Integer) newValue;
            p.setInt(counter, value);
        } else if (description.startsWith("smallint")) {
            short value = (Short) newValue;
            p.setShort(counter, value);
        } else if (description.startsWith("boolean")) {
            boolean value = (Boolean) newValue;
            p.setBoolean(counter, value);
        } else {
            logger.error("Description not recognised: " + description);
        }

    }

    public Object getCorrectValueClass(ResultSet rs, Dao object, String keyName) throws SQLException {
        if (!object.getAllKeysAsArray().contains(keyName))
            throw new SQLException("Object " + object.getClass().getName() + " does not contain key: " + keyName);

        String description = object.getDescriptionForKey(keyName);

        if (rs != null) {
            rs.next();
            if (description.startsWith("SERIAL")) {
                int value = (Integer) rs.getInt(keyName);
                return value;
            } else if (description.startsWith("character") | description.startsWith("text")) {
                String value = (String) rs.getString(keyName);
                return value;
            } else if (description.startsWith("time")) {
                Timestamp value = (Timestamp) rs.getTimestamp(keyName);
                return value;
            } else if (description.startsWith("int")) {
                int value = (Integer) rs.getInt(keyName);
                return value;
            } else if (description.startsWith("smallint")) {
                short value = (Short) rs.getShort(keyName);
                return value;
            } else if (description.startsWith("boolean")) {
                boolean value = (Boolean) rs.getBoolean(keyName);
                return value;
            } else {
                logger.error("Description not recognised: " + description);
                return null;
            }
        } else {
            logger.error("No results returned for key name: " + keyName);
            return null;
        }
    }

    public ArrayList getAllObjectsWhere(SqlDatabase database, SqlSchema schema, Dao object, String key, Object value) throws SQLException {

        String databaseName = database.getName();
        String schemaName = schema.getName();
        String tableName = object.getTableName();

        this.useDatabase(databaseName);
        this.useSchema(schemaName);

        logger.debug("Returning all objects: " + object.getClass().getName() + " from database: " + databaseName + ", schema: " + schemaName
                + ", table: " + tableName + " with column name: " + key + ", column value: " + value.toString());

        String preparedCommand = "SELECT * FROM \"" + tableName + "\" WHERE \"" + key + "\"=?";
        PreparedStatement p = getConnection().prepareStatement(preparedCommand + ";");

        setCorrectValueType(p, object, key, value, 1);

        logger.debug ("Query after setting correct type: " + p.toString());

        ResultSet rs = p.executeQuery();

        return resultSetAsDao(rs, object);
    }


    public ArrayList getAllUniqueObjectsWhere(SqlDatabase database, SqlSchema schema, Dao object, String key, Object value) throws SQLException {

        String databaseName = database.getName();
        String schemaName = schema.getName();
        String tableName = object.getTableName();

        this.useDatabase(databaseName);
        this.useSchema(schemaName);

        logger.debug
                ("Returning all UNIQUE objects: " + object.getClass().getName() + " from database: " + databaseName + ", schema: " + schemaName
                + ", table: " + tableName + " with column name: " + key + ", column value: " + value.toString());

        String preparedCommand = "SELECT DISTINCT ON (\"" + key + "\") * FROM \"" + tableName + "\" WHERE \"" + key + "\"=?";
        PreparedStatement p = getConnection().prepareStatement(preparedCommand + ";");

        setCorrectValueType(p, object, key, value, 1);

        logger.debug ("Query after setting correct type: " + p.toString());

        ResultSet rs = p.executeQuery();

        return resultSetAsDao(rs, object);
    }

    public void deleteAllObjectsWhere(SqlDatabase database, SqlSchema schema, Dao object, String key, Object value) throws SQLException {

        String databaseName = database.getName();
        String schemaName = schema.getName();
        String tableName = object.getTableName();

        this.useDatabase(databaseName);
        this.useSchema(schemaName);

        logger.debug("Deleting all objects: " + object.getClass().getName() + " from database: " + databaseName + ", schema: " + schemaName
                + ", table: " + tableName + " with column name: " + key + ", column value: " + value.toString());

        String preparedCommand = "DELETE FROM \"" + tableName + "\" WHERE \"" + key + "\"=?";
        PreparedStatement p = getConnection().prepareStatement(preparedCommand + ";");

        setCorrectValueType(p, object, key, value, 1);

        logger.debug(p.toString());

        p.execute();

    }

    public ArrayList getAllObjectsWhereNot(SqlDatabase database, SqlSchema schema, Dao object, String key, Object value) throws SQLException {

        String databaseName = database.getName();
        String schemaName = schema.getName();
        String tableName = object.getTableName();

        this.useDatabase(databaseName);
        this.useSchema(schemaName);

        logger.debug("Returning all objects: " + object.getClass().getName() + " from database: " + databaseName + ", schema: " + schemaName
                + ", table: " + tableName + " with column name: " + key + ", column value: " + value.toString());

        String preparedCommand = "SELECT * FROM \"" + tableName + "\" WHERE \"" + key + "\" IS DISTINCT FROM ?";
        PreparedStatement p = getConnection().prepareStatement(preparedCommand + ";");

        setCorrectValueType(p, object, key, value, 1);

        logger.debug(p.toString());

        ResultSet rs = p.executeQuery();

        return resultSetAsDao(rs, object);
    }

    public ArrayList getAllObjectsWhere(SqlDatabase database, SqlSchema schema, Dao object, HashMap<String, Object> map) throws SQLException {

        String databaseName = database.getName();
        String schemaName = schema.getName();
        String tableName = object.getTableName();

        this.useDatabase(databaseName);
        this.useSchema(schemaName);

        Iterator<String> keys = map.keySet().iterator();
        String names = "";
        String values = "";

        String preparedCommand = "SELECT * FROM \"" + tableName + "\" WHERE ";

        while(keys.hasNext()) {
            String key = keys.next();
            names += key + ", ";
            Object value = map.get(key);
            values += value.toString() + ", ";
            if (keys.hasNext())
                preparedCommand += " \"" + key + "\"=? AND";
            else
                preparedCommand += " \"" + key + "\"=?";
        }

        names = names.substring(0, names.length() - 2);
        values = values.substring(0, values.length() - 2);

        logger.debug("Returning all objects: " + object.getClass().getName() + " from database: " + databaseName + ", schema: " + schemaName
                + ", table: " + tableName + " with column names: " + names + " and column values: " + values);

        PreparedStatement p = getConnection().prepareStatement(preparedCommand + ";");

        logger.debug(p.toString());

        keys = map.keySet().iterator();
        int counter = 1;
        while(keys.hasNext()) {
            String key = keys.next();
            Object value = map.get(key);
            setCorrectValueType(p, object, key, value, counter);
            counter++;
        }

        logger.debug(p.toString());

        ResultSet rs = p.executeQuery();

        return resultSetAsDao(rs, object);
    }

    public ArrayList getAllObjectsWhereSortBy(SqlDatabase database, SqlSchema schema, Dao object, HashMap<String, Object> map, String sortBy) throws SQLException {
    	return getLimitedObjectsWhereSortBy(database, schema, object, map, sortBy, null);
    }

	public Dao getFirstObjectWhere(SqlDatabase database, SqlSchema schema, Dao object, HashMap<String, Object> map) throws SQLException {
		return getFirstObjectWhereSortBy(database, schema, object, map, null);
	}

	public Dao getFirstObjectWhereSortBy(SqlDatabase database, SqlSchema schema, Dao object, HashMap<String, Object> map, String sortBy) throws SQLException {
		//System.out.println("getFirstObjectWhereSortBy");
    	ArrayList array = getLimitedObjectsWhereSortBy(database, schema, object, map, sortBy, 1);
    	//System.out.println("getFirstObjectWhereSortBy: array.size() = " + array.size());
    	if (array.size() == 1)
    		return (Dao) array.get(0);
    	else
    		return null;
    }

    public ArrayList getLimitedObjectsWhereSortBy(SqlDatabase database, SqlSchema schema, Dao object, HashMap<String, Object> map, String sortBy, Integer limit) throws SQLException {
    	//System.out.println("getLimitedObjectsWhereSortBy");
    	String databaseName = database.getName();
    	String schemaName = schema.getName();
    	String tableName = object.getTableName();

    	this.useDatabase(databaseName);
    	this.useSchema(schemaName);

    	Iterator<String> keys = map.keySet().iterator();
    	String names = "";
    	String values = "";

    	String preparedCommand = "SELECT * FROM \"" + tableName + "\" WHERE ";

    	while(keys.hasNext()) {
    		String key = keys.next();
    		names += key + ", ";
    		Object value = map.get(key);
    		values += value.toString() + ", ";
    		if (keys.hasNext())
    			preparedCommand += " \"" + key + "\"=? AND";
    		else
    			preparedCommand += " \"" + key + "\"=?";
    	}

    	names = names.substring(0, names.length() - 2);
    	values = values.substring(0, values.length() - 2);

    	logger.debug("Returning all objects: " + object.getClass().getName() + " from database: " + databaseName + ", schema: " + schemaName
    			+ ", table: " + tableName + " with column names: " + names + " and column values: " + values);

    	if (sortBy != null) preparedCommand += " ORDER BY " + sortBy + " DESC";

    	if (limit != null)
    		preparedCommand += " LIMIT " + limit;

    	preparedCommand += ";";

    	PreparedStatement p = getConnection().prepareStatement(preparedCommand);

    	logger.debug(p.toString());
    	//System.out.println(p.toString());

    	keys = map.keySet().iterator();
    	int counter = 1;
    	while(keys.hasNext()) {
    		String key = keys.next();
    		Object value = map.get(key);
    		setCorrectValueType(p, object, key, value, counter);
    		counter++;
    	}

    	logger.debug(p.toString());

    	ResultSet rs = p.executeQuery();

    	return resultSetAsDao(rs, object);
    }

    public void deleteAllWhere(SqlDatabase database, SqlSchema schema, Dao object, HashMap<String, Object> map) throws SQLException {

        String databaseName = database.getName();
        String schemaName = schema.getName();
        String tableName = object.getTableName();

        this.useDatabase(databaseName);
        this.useSchema(schemaName);

        Iterator<String> keys = map.keySet().iterator();
        String names = "";
        String values = "";

        String preparedCommand = "DELETE FROM \"" + tableName + "\" WHERE ";

        while(keys.hasNext()) {
            String key = keys.next();
            names += key + ", ";
            Object value = map.get(key);
            values += value.toString() + ", ";
            if (keys.hasNext())
                preparedCommand += " \"" + key + "\"=? AND";
            else
                preparedCommand += " \"" + key + "\"=?";
        }

        names = names.substring(0, names.length() - 2);
        values = values.substring(0, values.length() - 2);

        logger.debug("Deleting all objects: " + object.getClass().getName() + " from database: " + databaseName + ", schema: " + schemaName
                + ", table: " + tableName + " with column names: " + names + " and column values: " + values);

        PreparedStatement p = getConnection().prepareStatement(preparedCommand + ";");

        logger.debug(p.toString());

        keys = map.keySet().iterator();
        int counter = 1;
        while(keys.hasNext()) {
            String key = keys.next();
            Object value = map.get(key);
            setCorrectValueType(p, object, key, value, counter);
            counter++;
        }

        logger.debug(p.toString());

        p.execute();

    }
    
    public void deleteAll(SqlDatabase database, SqlSchema schema, Dao object) throws SQLException {

        String databaseName = database.getName();
        String schemaName = schema.getName();
        String tableName = object.getTableName();

        this.useDatabase(databaseName);
        this.useSchema(schemaName);

        String preparedCommand = "DELETE FROM \"" + tableName + "\"";

        logger.debug("Deleting all objects: " + object.getClass().getName() + " from database: " + databaseName + ", schema: " + schemaName
                + ", table: " + tableName);

        PreparedStatement p = getConnection().prepareStatement(preparedCommand + ";");

        logger.debug(p.toString());

        p.execute();

    }    

    public void updateRow(SqlDatabase database, SqlSchema schema, Dao object, String fieldToEdit, Object newValue, String identifyingField, Object identifyingValue) throws SQLException {
        String databaseName = database.getName();
        String schemaName = schema.getName();
        String tableName = object.getTableName();

        this.useDatabase(databaseName);
        this.useSchema(schemaName);

        logger.debug("Updating object: " + object.getClass().getName() + " from database: " + databaseName + ", schema: " + schemaName
                + ", table: " + tableName + " with column name to edit: " + fieldToEdit);

        String preparedCommand = "UPDATE \"" + tableName + "\" SET \"" + fieldToEdit + "\" = ? WHERE \"" + identifyingField + "\" = ?";
        PreparedStatement p = getConnection().prepareStatement(preparedCommand + ";");

        setCorrectValueType(p, object, fieldToEdit, newValue, 1);
        setCorrectValueType(p, object, identifyingField, identifyingValue, 2);

        logger.debug(p.toString());

        p.executeUpdate();
        p.close();

    }

    public Object getColumnValue(SqlDatabase database, SqlSchema schema, Dao object, String columnName, String identifyingField, Object identifyingValue) throws SQLException {

        String databaseName = database.getName();
        String schemaName = schema.getName();
        String tableName = object.getTableName();

        this.useDatabase(databaseName);
        this.useSchema(schemaName);

        logger.debug("Returning column value for: " + object.getClass().getName() + " from database: " + databaseName + ", schema: " + schemaName
                + ", table: " + tableName + " with column name: " + columnName);

        String preparedCommand = "SELECT \"" + columnName + "\" FROM \"" + tableName + "\" WHERE \"" + identifyingField + "\"=?";
        PreparedStatement p = getConnection().prepareStatement(preparedCommand + ";");

        setCorrectValueType(p, object, identifyingField, identifyingValue, 1);

        logger.debug(p.toString());

        ResultSet rs = p.executeQuery();

        return getCorrectValueClass(rs, object, columnName);

    }

    public ArrayList<Integer> getIDColumnValues(SqlDatabase database, SqlSchema schema, Dao object, String columnName) throws SQLException {

        ArrayList<Integer> values = new ArrayList<Integer>();

        String databaseName = database.getName();
        String schemaName = schema.getName();
        String tableName = object.getTableName();

        this.useDatabase(databaseName);
        this.useSchema(schemaName);

        logger.debug("Returning column value for: " + object.getClass().getName() + " from database: " + databaseName + ", schema: " + schemaName
                + ", table: " + tableName + " with column name: " + columnName);

        String preparedCommand = "SELECT \"" + columnName + "\" FROM \"" + tableName + "\"";
        PreparedStatement p = getConnection().prepareStatement(preparedCommand + ";");

        logger.debug(p.toString());

        ResultSet rs = p.executeQuery();

        if (rs != null) {
            while (rs.next()) {
               values.add(rs.getInt(columnName));
            }
        }

        return values;

    }

    public ArrayList<Integer> getIDColumnValuesWhere(SqlDatabase database, SqlSchema schema, Dao object, String columnNameToReturn, String key, Object value) throws SQLException {

        ArrayList<Integer> values = new ArrayList<Integer>();

        String databaseName = database.getName();
        String schemaName = schema.getName();
        String tableName = object.getTableName();

        this.useDatabase(databaseName);
        this.useSchema(schemaName);

        logger.debug("Returning column value for: " + object.getClass().getName() + " from database: " + databaseName + ", schema: " + schemaName
                + ", table: " + tableName + " with column name: " + key);

        String preparedCommand = "SELECT \"" + columnNameToReturn + "\" FROM \"" + tableName + "\" WHERE \"" + key + "\"=?";
        PreparedStatement p = getConnection().prepareStatement(preparedCommand + ";");

        setCorrectValueType(p, object, key, value, 1);

        logger.debug(p.toString());

        ResultSet rs = p.executeQuery();

        if (rs != null) {
            while (rs.next()) {
               values.add(rs.getInt(columnNameToReturn));
            }
        }

        return values;

    }

    public ArrayList<Integer> getIDColumnValuesWhere(SqlDatabase database, SqlSchema schema,
            Dao mainObject, String columnNameToReturn, String mainKey, Object mainValue, String mainToLinkedColumnName,
            Dao linkedObject, String linkedKey, Object linkedValue) throws SQLException {

        ArrayList<Integer> values = new ArrayList<Integer>();

        String databaseName = database.getName();
        String schemaName = schema.getName();
        String mainTableName = mainObject.getTableName();
        String linkedTableName = linkedObject.getTableName();

        this.useDatabase(databaseName);
        this.useSchema(schemaName);

//        logger.debug("Returning column value for: " + linkedObject.getClass().getName() + " from database: " + databaseName + ", schema: " + schemaName
//                + ", table: " + mainTableName + " with column name: " + linkedKey);

        String preparedCommand = "SELECT main.\"" + columnNameToReturn + "\" FROM \"" + mainTableName + "\" main, \"" + linkedTableName + "\" linked"
                + " WHERE main.\"" + mainKey + "\" = ?"
                + " AND linked.\"" + linkedKey + "\" = ?"
                + " AND linked.\"" + mainToLinkedColumnName + "\" = main.\"" + columnNameToReturn + "\"";
        PreparedStatement p = getConnection().prepareStatement(preparedCommand + ";");

        setCorrectValueType(p, mainObject, mainKey, mainValue, 1);
        setCorrectValueType(p, linkedObject, linkedKey, linkedValue, 2);

        logger.debug(p.toString());

        ResultSet rs = p.executeQuery();

        if (rs != null) {
            while (rs.next()) {
               values.add(rs.getInt(columnNameToReturn));
            }
        }

        return values;

    }

    public String insertRow(SqlDatabase database, SqlSchema schema, SqlTable table, Insertable object) throws SQLException {
        String id = null;
        this.useDatabase(database.getName());
        this.useSchema(schema.getName());

        String preparedCommand = "INSERT INTO \"" + table.getName() + "\" " + object.columnNamesAsString() + " VALUES " + object.columnValuesForPreparedStatement();
        PreparedStatement p = getConnection().prepareStatement(preparedCommand + ";");

        int i = 1;
        for (String value : object.valuesAsArray()) {
            p.setString(i, value);
            i++;
        }

//        System.out.println(p.toString());

        p.execute();
        ResultSet rs = p.getGeneratedKeys();

        if (rs != null) {
            while (rs.next()) {
                id = rs.getString(object.returning());
//                System.out.println("Got " + object.returning() + ": " + id);
            }
        }

//        String command = "INSERT INTO \"" + table.getName() + "\" " + object.getKeysAsString() + " VALUES " + object.columnValuesAsString() + " RETURNING \"" + object.returning() + "\"";
//        System.out.println(command);
//        ResultSet result = this.executeQuery(command);
//        if (result != null) {
//            while (result.next()) {
//                id = result.getString(object.returning());
//                System.out.println("Got " + object.returning() + ": " + id);
//            }
//        }

        p.close();
        return id;
    }

    public String insertRowGetCountId(SqlDatabase database, SqlSchema schema, SqlTable table, Insertable object) throws SQLException {
        String count_id = null;
        String countIdName = "count_ID";
        this.useDatabase(database.getName());
        this.useSchema(schema.getName());

        String preparedCommand = "INSERT INTO \"" + table.getName() + "\" " + object.columnNamesAsString() + " VALUES " + object.columnValuesForPreparedStatement();
        PreparedStatement p = getConnection().prepareStatement(preparedCommand + ";");

        int i = 1;
        for (String value : object.valuesAsArray()) {
            p.setString(i, value);
            i++;
        }

        p.execute();
        ResultSet rs = p.getGeneratedKeys();

        if (rs != null) {
            while (rs.next()) {
                count_id = rs.getString(countIdName);
//                System.out.println("Got " + countIdName + ": " + id);
            }
        }

        p.close();
        return count_id;
    }

    public void updateRow(SqlDatabase database, SqlSchema schema, SqlTable table, String fieldToEdit, String newValue, String identifyingField, String identifyingValue) throws SQLException {
        this.useDatabase(database.getName());
        this.useSchema(schema.getName());

        String command = "UPDATE \"" + table.getName() + "\" SET \"" + fieldToEdit + "\" = \'" + newValue + "\' WHERE \"" + identifyingField + "\" = \'" + identifyingValue + "\'";
//        System.out.println(command);
        this.execute(command);
    }

    public ResultSet getEntries(SqlDatabase database, SqlSchema schema, SqlTable table) throws SQLException {
        this.useDatabase(database.getName());
        this.useSchema(schema.getName());

        return this.executeQuery("SELECT * FROM \"" + table.getName() + "\"");
    }

    public ResultSet getEntriesWith(SqlDatabase database, SqlSchema schema, SqlTable table, String columnName, String columnValue) throws SQLException {
        this.useDatabase(database.getName());
        this.useSchema(schema.getName());
        String command = "SELECT * FROM \"" + table.getName() + "\" WHERE \"" + columnName + "\"=\'" + columnValue + "\'";
//        System.out.println(command);
        return this.executeQuery(command);
    }

    public ResultSet getEntriesWith(SqlDatabase database, SqlSchema schema, SqlTable table, HashMap<String, String> fieldAndValue) throws SQLException {
        this.useDatabase(database.getName());
        this.useSchema(schema.getName());
        String where = "\" WHERE ";

        int size = fieldAndValue.keySet().size();
        int counter = 0;
        for (Iterator i = fieldAndValue.keySet().iterator(); i.hasNext();) {
            ++counter;
            String key = (String) i.next();
            String value = (String) fieldAndValue.get(key);
            where = where + "\"" + key + "\"=\'" + value + "\'";
            if (counter < size) {
                where = where + " AND ";
            }
        }

        String command = "SELECT * FROM \"" + table.getName() + where;
//        System.out.println(command);
        return this.executeQuery(command);
//        return null;
    }

    public ResultSet getEntriesBetween(SqlDatabase database, SqlSchema schema, SqlTable table, String columnName, String columnValueStart, String columnValueEnd) throws SQLException {
        this.useDatabase(database.getName());
        this.useSchema(schema.getName());
        String command = "";

        if (columnValueStart.equals("") & !(columnValueEnd.equals(""))) {
            command = "SELECT * FROM \"" + table.getName() + "\" WHERE \"" + columnName + "\" <= " + columnValueEnd;
        } else if (columnValueEnd.equals("") & !(columnValueStart.equals(""))) {
            command = "SELECT * FROM \"" + table.getName() + "\" WHERE \"" + columnName + "\" >= " + columnValueStart;
        } else if (columnValueStart.equals("") & columnValueEnd.equals("")) {
            command = "SELECT * FROM \"" + table.getName() + "\"";
        } else {
            command = "SELECT * FROM \"" + table.getName() + "\" WHERE \"" + columnName + "\" BETWEEN " + columnValueStart + " AND " + columnValueEnd;
        }

//        System.out.println(command);
        return this.executeQuery(command);
    }

    public ArrayList<String> getTablesNames(SqlSchema schema) throws SQLException {
        this.useDatabase(schema.getParent().getName());
        this.useSchema(schema.getName());
        ArrayList<String> names = new ArrayList<String>();
        ResultSet rs = executeQuery("SELECT tablename FROM pg_tables where schemaname=\'" + schema.getName() + "\' ");

        while (rs.next()) {
            names.add(rs.getString("tablename"));
        }

        return names;
    }

    public SqlTable getTable(SqlDatabase database, SqlSchema schema, String tableName, String tableColumns) throws SQLException {
        this.useDatabase(database.getName());
        this.useSchema(schema.getName());

        if (!this.getTablesNames(schema).contains(tableName)) {
            this.execute("CREATE TABLE \"" + tableName + "\" (" + tableColumns + ")");
        }

        return new SqlTable(tableName, schema);

    }

}
