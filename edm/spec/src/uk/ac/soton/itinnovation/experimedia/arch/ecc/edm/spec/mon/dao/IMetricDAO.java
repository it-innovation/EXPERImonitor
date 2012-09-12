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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao;

import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;

/**
 * A DAO to save and get Metric objects from storage.
 * 
 * OBS: no delete methods yet.
 * OBS: no update methods yet.
 * 
 * @author Vegard Engen
 */
public interface IMetricDAO
{
    /**
     * Saves a metric, which must have a unique UUID and refer to an existing
     * measurement set (by its UUID).
     * @param metric The metric object to be saved.
     * @throws Exception If there's a technical issue or a metric with the same UUID already exists.
     */
    public void saveMetric(Metric metric) throws Exception;
    
    /**
     * Get a metric object according to its UUID.
     * @param metricUUID The metric UUID.
     * @return A metric object, if it exists.
     * @throws Exception If there's a technical issue or there is no metric with the given UUID.
     */
    public Metric getMetric(UUID metricUUID) throws Exception;
    
    /**
     * Get a metric object for a measurement set.
     * @param measurementSetUUID The measurement set UUID.
     * @return A metric object, if it exists.
     * @throws Exception If there's a technical issue or there is no measurement set with the given UUID.
     */
    public Metric getMetricForMeasurementSet(UUID measurementSetUUID) throws Exception;
}
