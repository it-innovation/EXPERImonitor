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

import java.util.List;

/**
 *
 * @author Vegard Engen
 */
public class DBUtil
{
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
}
