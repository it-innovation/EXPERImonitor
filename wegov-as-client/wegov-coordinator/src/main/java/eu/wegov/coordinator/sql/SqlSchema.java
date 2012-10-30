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
import eu.wegov.coordinator.dao.data.WegovWidgetDataAsJson;
import eu.wegov.coordinator.dao.data.twitter.FullTweet;
import eu.wegov.coordinator.dao.data.twitter.Hashtag;
import eu.wegov.coordinator.dao.data.twitter.Url;
import eu.wegov.coordinator.dao.data.twitter.UserMention;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Maxim Bashevoy
 */
public class SqlSchema {
    private PostgresConnector connector;
    private String name;
    private SqlDatabase parent;

    //public SqlSchema(String name, SqlDatabase parent) {
    //    this.parent = parent;
    //    this.name = name;
    //    this.connector = parent.getConnector();
    //}

    public SqlSchema(String name, PostgresConnector connector) throws SQLException {
        this.parent = connector.getDatabase();
        this.name = name;
        this.connector = connector;
    }

    public String getName() {
        return name;
    }

    public SqlDatabase getParent() {
        return parent;
    }

    public PostgresConnector getConnector() {
        return connector;
    }
    
    public SqlTable getTable(String tableName, String tableSqlSchemaAsString) throws SQLException {
        return connector.getTable(this.getParent(), this, tableName, tableSqlSchemaAsString);
    } 
    
    public SqlTable getTable(Dao object) throws SQLException {
        String tableName = object.getTableName();
        String tableSqlSchemaAsString = object.getTableSqlSchemaAsString();
        return connector.getTable(this.getParent(), this, tableName, tableSqlSchemaAsString);
    } 
    
    public String insertObject(Dao object) throws SQLException {
        return connector.insertRow(parent, this, object);
    }
    
    public void insertFullTweet(FullTweet fullTweet) throws SQLException {
        insertObject(fullTweet.getTweet());
        insertObject(fullTweet.getUser());
        
        for (UserMention mention : fullTweet.getUserMentions()) {
            insertObject(mention);
        }

        for (Url url : fullTweet.getUrls()) {
            insertObject(url);
        }                    

        for (Hashtag hastag : fullTweet.getHashtags()) {
            insertObject(hastag);
        }        
    }    
    
    public ArrayList getAll(Dao object) throws SQLException {
        return connector.getAllObjects(parent, this, object);
    }
    
    public ArrayList getAllUniqueOn(Dao object, String uniqueColumn) throws SQLException {
        return connector.getAllObjectsUniqueOn(parent, this, object, uniqueColumn);
    }
    
    public ArrayList getAllWhere(Dao object, String whereKey, Object whereValue) throws SQLException {
        return connector.getAllObjectsWhere(parent, this, object, whereKey, whereValue);
    }
    
    public ArrayList getAllUniqueWhere(Dao object, String whereKey, Object whereValue) throws SQLException {
        return connector.getAllUniqueObjectsWhere(parent, this, object, whereKey, whereValue);
    }
    
    public void deleteAllWhere(Dao object, String whereKey, Object whereValue) throws SQLException {
        connector.deleteAllObjectsWhere(parent, this, object, whereKey, whereValue);
    }
    
    public ArrayList getAllWhereNot(Dao object, String whereKey, Object whereValue) throws SQLException {
        return connector.getAllObjectsWhereNot(parent, this, object, whereKey, whereValue);
    }
    
    public ArrayList getAllWhere(Dao object, HashMap<String, Object> map) throws SQLException {
        return connector.getAllObjectsWhere(parent, this, object, map);
    }
    
    public ArrayList getAllWhereSortBy(Dao object, HashMap<String, Object> map, String sortBy) throws SQLException {
    	return connector.getAllObjectsWhereSortBy(parent, this, object, map, sortBy);
    }
    
	public Dao getFirstWhere(Dao object, HashMap<String, Object> map) throws SQLException {
        return connector.getFirstObjectWhere(parent, this, object, map);
	}
	
    public Dao getFirstWhereSortBy(Dao object, HashMap<String, Object> map, String sortBy) throws SQLException {
    	return connector.getFirstObjectWhereSortBy(parent, this, object, map, sortBy);
    }
    
    public ArrayList getLimitedWhereSortBy(Dao object, HashMap<String, Object> map, String sortBy, Integer limit) throws SQLException {
    	return connector.getLimitedObjectsWhereSortBy(parent, this, object, map, sortBy, limit);
    }

    public void deleteAllWhere(Dao object, HashMap<String, Object> map) throws SQLException {
        connector.deleteAllWhere(parent, this, object, map);
    }
    
    public void updateRow(Dao object, String fieldToEdit, Object newValue, String identifyingField, Object identifyingValue) throws SQLException {
        connector.updateRow(parent, this, object, fieldToEdit, newValue, identifyingField, identifyingValue);
    }
    
    public Object getColumnValue(Dao object, String fieldToEdit, String identifyingField, Object identifyingValue) throws SQLException {
        return connector.getColumnValue(parent, this, object, fieldToEdit, identifyingField, identifyingValue);
    }
    
    public ArrayList<Integer> getIDColumnValues(Dao object, String columnName) throws SQLException {
        return connector.getIDColumnValues(parent, this, object, columnName);
    }
    
    public ArrayList<Integer> getIDColumnValuesWhere(Dao object, String columnNameToReturn, String columnName, Object value) throws SQLException {
        return connector.getIDColumnValuesWhere(parent, this, object, columnNameToReturn, columnName, value);
    }
    
    public ArrayList<Integer> getIDColumnValuesWhere(Dao mainObject, String columnNameToReturn, String mainKey, Object mainValue, String mainToLinkedColumnName,
            Dao linkedObject, String linkedKey, Object linkedValue) throws SQLException {
        return connector.getIDColumnValuesWhere(parent, this, mainObject, columnNameToReturn, mainKey, mainValue, mainToLinkedColumnName,
                linkedObject, linkedKey, linkedValue);
    }

}
