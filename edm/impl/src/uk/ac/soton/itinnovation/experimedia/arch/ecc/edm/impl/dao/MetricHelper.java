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
//      Created Date :          2012-08-23
//      Created for Project :   
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.dao;

import java.util.List;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DatabaseConnector;

/**
 *
 * @author Vegard Engen
 */
public class MetricHelper
{
static Logger log = Logger.getLogger(MetricHelper.class);
    
    public static ValidationReturnObject isObjectValidForSave(Metric metric, DatabaseConnector dbCon) throws Exception
    {
        if (metric == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Metric object is NULL - cannot save that..."));
        }
        
        // check if all the required information is given; uuid, type and unit
        
        if (metric.getUUID() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Metric UUID is NULL"));
        }
        
        if (metric.getMetricType() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Metric type is NULL"));
        }
        
        if (metric.getUnit() == null)
        {
            return new ValidationReturnObject(false, new NullPointerException("The Metric unit is NULL"));
        }
        
        // check if it exists in the DB already
        try {
            if (objectExists(metric.getUUID(), dbCon))
            {
                return new ValidationReturnObject(false, new RuntimeException("The Metric already exists; the UUID is not unique"));
            }
        } catch (Exception ex) {
            throw ex;
        }
        
        return new ValidationReturnObject(true);
    }
    
    public static boolean objectExists(UUID uuid, DatabaseConnector dbCon) throws Exception
    {
        return DBUtil.objectExistsByUUID("Metric", "metricUUID", uuid, dbCon);
    }
}
