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
//	Created Date :			2011-07-27
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator.dao;

import eu.wegov.coordinator.utils.Triplet;
import java.sql.Timestamp;
import java.util.ArrayList;

/**
 *
 * @author Maxim Bashevoy
 */
public abstract class Dao {
    public String tableName;
    public ArrayList<Triplet<String, String, Object>> properties = new ArrayList<Triplet<String, String, Object>>();
    
    public Dao() {
        this.properties = new ArrayList<Triplet<String, String, Object>>();
        this.tableName = "";        
    }
    
    public Dao(String tableName) {
        this.tableName = tableName;        
    }
    
    
    public Dao(ArrayList<Triplet<String, String, Object>> properties, String tableName) {
        this.properties = properties;
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public ArrayList<Triplet<String, String, Object>> getProperties() {
        return properties;
    }
    
    public String getDescriptionForKey(String keyName) {
        String result = null;
        
        for (Triplet<String, String, ?> entry : properties) {
            if (entry.getKey().equals(keyName)) {
                result = entry.getDescription();
                if (result != null)
                    result = result.trim();
                break;
            }
        }
        
        return result;
    }
    
    public String getValueForKeyAsString(String keyName) {
        String result = null;
        
        for (Triplet<String, String, ?> entry : properties) {
            if (entry.getKey().equals(keyName)) {
                Object value = entry.getValue();

                if (value != null)
                    result = value.toString().trim();
                
                break;
            }
        }
        
        return result;
    }
    
    public int getValueForKeyAsInt(String keyName) {
        int result = -1;
        
        for (Triplet<String, String, ?> entry : properties) {
            if (entry.getKey().equals(keyName)) {
                result = (Integer) entry.getValue();
                break;
            }
        }
        
        return result;
    }
    
    public Timestamp getValueForKeyAsTimestamp(String keyName) {
        Timestamp result = new Timestamp(0);
        
        for (Triplet<String, String, ?> entry : properties) {
            if (entry.getKey().equals(keyName)) {
                result = (Timestamp) entry.getValue();
                break;
            }
        }
        
        return result;
    }
    
    public void updateProperty(String keyName, Object newValue) {
        Triplet<String, String, Object> entry = getPropertyForKey(keyName);
        entry.setValue(newValue);
//        properties.remove(entry);
//        properties.add(new Triplet(keyName, entry.getDescription(), newValue));
    }
    
    public Triplet<String, String, Object> getPropertyForKey(String keyName) {
        int i = -1;
        
        for (int j = 0; j < properties.size(); j++) {
            Triplet<String, String, ?> entry = properties.get(j);
            if (entry.getKey().equals(keyName)) {
                i = j;
                break;
            }
        }
        
        if ( i != -1 ) {
            return properties.get(i);
        } else {
            return null;
        }
    }

    public void setProperties(ArrayList<Triplet<String, String, Object>> properties) {
        this.properties = properties;
    }
    
    public String getAllKeysAsString() {
        String result = "(";
        
        for (Triplet<String, String, ?> entry : properties) {
            result = result + "\"" + entry.getKey() + "\" " + entry.getDescription() + ", ";
        }
        
        // Get rid of the last ", ":
        result = result.substring(0, result.length() - 2) + ")";
        
        return result;
    }
    
    public String getTableSqlSchemaAsString() {
        String result = "";
        
        for (Triplet<String, String, ?> entry : properties) {
            result = result + "\"" + entry.getKey() + "\" " + entry.getDescription() + ", ";
        }
        
        result = result.substring(0, result.length() - 2);
        
        return result;        
    }

    public String returning() {
        return "count_ID";
    }
    
    public String getKeysAsString() {
        String result = "(";
        
        for (Triplet<String, String, ?> entry : properties) {
            if (!entry.getDescription().startsWith("SERIAL"))
                result = result + "\"" + entry.getKey() + "\",";
        }
        
        result = result.substring(0, result.length() - 1) + ")";
        
        return result;        
    }
    
    public ArrayList<String> getAllKeysAsArray() {
        ArrayList<String> result = new ArrayList<String>();
        
        for (Triplet<String, String, ?> entry : properties) {
//            if (!entry.getDescription().startsWith("SERIAL"))
            result.add(entry.getKey());
        }
        
        return result;        
    }    
    
    public String getValuesAsQMString() {
        String result = "(";
        
        for (Triplet<String, String, ?> entry : properties) {
            if (!entry.getDescription().startsWith("SERIAL"))
                result = result + "?,";
        }
        
        // Get rid of the last ", ":
        result = result.substring(0, result.length() - 1) + ")";
        return result;
    }
    
    @Override
    public String toString() {
        String result = "Type: \'";
        result += tableName + "\'\n";
        result += "Entries (" + properties.size() + "):" + "\n";

        for (Triplet<String, String, ?> entry : properties) {
            result += "> \'" + entry.getKey() + "\' - \'" + entry.getDescription() + "\' - \'" + entry.getValue() + "\'" + "\n";
        }

        return result;
    }
    
    public abstract Dao createNew();
    
}
