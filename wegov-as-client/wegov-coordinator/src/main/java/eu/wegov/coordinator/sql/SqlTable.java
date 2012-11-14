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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

/**
 *
 * @author Maxim Bashevoy
 */
public class SqlTable {
    private PostgresConnector connector;
    private String name;
    private SqlSchema parent;

    public SqlTable(String name, SqlSchema parent) {
        this.name = name;
        this.parent = parent;
        this.connector = parent.getConnector();
    }

    public String getName() {
        return name;
    }

    public String insertRow(Insertable object) throws SQLException {
        return connector.insertRow(parent.getParent(), parent, this, object);
    }
    
    public String insertRow(Dao object) throws SQLException {
        return connector.insertRow(parent.getParent(), parent, object);
    }    

    public String insertRowGetCountId(Insertable object) throws SQLException {
        return connector.insertRowGetCountId(parent.getParent(), parent, this, object);
    }

    public void updateRow(String fieldToEdit, String newValue, String identifyingField, String identifyingValue) throws SQLException {
        connector.updateRow(parent.getParent(), parent, this, fieldToEdit, newValue, identifyingField, identifyingValue);
    }

    public ResultSet getRows() throws SQLException {
        return connector.getEntries(parent.getParent(), parent, this);
    }

    public ResultSet getRowsWith(String columnName, String columnValue) throws SQLException {
        return connector.getEntriesWith(parent.getParent(), parent, this, columnName, columnValue);
    }

    public ResultSet getRowsWith(HashMap<String, String> fieldAndValue) throws SQLException {
        return connector.getEntriesWith(parent.getParent(), parent, this, fieldAndValue);
    }

    public ResultSet getRowsWithValuesBetween(String columnName, String columnValueStart, String columnValueEnd) throws SQLException {
        return connector.getEntriesBetween(parent.getParent(), parent, this, columnName, columnValueStart, columnValueEnd);
    }    
}
