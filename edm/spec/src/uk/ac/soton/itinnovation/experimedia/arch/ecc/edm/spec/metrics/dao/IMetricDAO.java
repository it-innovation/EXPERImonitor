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
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao;

import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.NoDataException;

/**
 * A DAO to save and get Metric objects from storage.
 * 
 * @author Vegard Engen
 */
public interface IMetricDAO
{
    /**
     * Saves a metric, which must have a unique UUID and refer to an existing
     * measurement set (by its UUID).
     * @param metric The metric object to be saved.
     * @throws IllegalArgumentException If the Metric is not valid to be saved, typically due to missing information (e.g., NULL values).
     * @throws Exception If there's a technical issue or a metric with the same UUID already exists.
     */
    void saveMetric(Metric metric) throws IllegalArgumentException, Exception;
    
    /**
     * Get a metric object according to its UUID.
     * @param metricUUID The metric UUID.
     * @return A metric object, if it exists.
     * @throws IllegalArgumentException If metricUUID is not a valid argument (e.g., NULL).
     * @throws NoDataException If there's no metric with the given UUID.
     * @throws Exception If there's a technical issue.
     */
    Metric getMetric(UUID metricUUID) throws IllegalArgumentException, NoDataException, Exception;
    
    /**
     * Get a metric object for a measurement set.
     * @param measurementSetUUID The measurement set UUID.
     * @return A metric object, if one exist for the given measurement set.
     * @throws NoDataException If there is no measurement set with the given UUID or there is no metric for it.
     * @throws Exception If there's a technical issue.
     */
    Metric getMetricForMeasurementSet(UUID measurementSetUUID) throws IllegalArgumentException, NoDataException, Exception;
}
