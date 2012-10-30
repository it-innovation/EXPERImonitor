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

import java.sql.SQLException;
import java.util.HashMap;

/**
 *
 * @author Maxim Bashevoy
 */
public class SqlDatabase {
    //private PostgresConnector connector;
	private HashMap<String, PostgresConnector> connectors;

    private String name;
    
    public SqlDatabase(String name, PostgresConnector connector) {
    	this.connectors = new HashMap<String, PostgresConnector>();
        this.name = name;
        this.connectors.put(connector.getDefaultSchema(), connector);
    }

 	public SqlDatabase(String name, HashMap<String, PostgresConnector> connectors) {
 		this.name = name;
 		this.connectors = connectors;
 	}

    public String getName() {
        return name;
    }

	public PostgresConnector getConnector(String schemaName) {
		return connectors.get(schemaName);
	}

	public SqlSchema getSchema(String schemaName) throws SQLException {
        //return getConnector(schemaName).getSchema(this, schemaName);
        return getConnector(schemaName).getSchema();
    }

    //public PostgresConnector getConnector() {
    //    return connector;
    //}    
}
