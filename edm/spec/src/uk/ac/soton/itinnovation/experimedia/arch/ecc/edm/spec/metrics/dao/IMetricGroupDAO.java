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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao;

import java.util.Set;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.NoDataException;

/**
 * A DAO to save and get MetricGroup objects from storage.
 * 
 * OBS: the get methods will return all sub-classes except for measurements. To
 * get actual measurements for a measurement set, one of the specific getMeasurement(s)
 * methods need to be called, passing on the UUID of the measurement set (and
 * any other arguments for the respective method).
 * 
 * @author Vegard Engen
 */
public interface IMetricGroupDAO
{
    /**
     * Save a metric group, which must have a unique UUID and refer to an existing
     * metric generator (by its UUID).
     * 
     * Any sub-classes of the metric group will also be saved (if not null).
     * @param metricGroup The metric group instance that should be saved.
     * @throws IllegalArgumentException If the MetricGroup is not valid to be saved, typically due to missing information (e.g., NULL values).
     * @throws Exception If there's a technical issue or a metric group with the same UUID already exists.
     */
    void saveMetricGroup(MetricGroup metricGroup) throws IllegalArgumentException, Exception;
    
    /**
     * Get the a metric group instance from a UUID. Will include all sub-classes
     * except for measurements, if available.
     * @param metricGroupUUID The metric group UUID.
     * @param withSubClasses Flag to say whether to return subclasses too; MeasurementSet and sub-classes below that.
     * @return A metric group instance, if it exists.
     * @throws IllegalArgumentException If metricGroupUUID is not a valid argument (e.g., NULL).
     * @throws NoDataException If there's no metric group with the given UUID.
     * @throws Exception If there's a technical issue.
     */
    MetricGroup getMetricGroup(UUID metricGroupUUID, boolean withSubClasses) throws IllegalArgumentException, NoDataException, Exception;
    
    /**
     * Get all metric groups for a metric generator. Will include any sub-classes,
     * except for measurements, if available.
     * @param metricGenUUID The UUID of the metric generator.
     * @param withSubClasses Flag to say whether to return subclasses too; MeasurementSet and sub-classes below that.
     * @return A metric group instance, if it exists.
     * @throws IllegalArgumentException If metricGenUUID is not a valid argument (e.g., NULL).
     * @throws NoDataException If there's no metric generator with the given UUID or there are no metric groups for it.
     * @throws Exception If there's a technical issue.
     */
    Set<MetricGroup> getMetricGroupsForMetricGenerator(UUID metricGenUUID, boolean withSubClasses) throws IllegalArgumentException, NoDataException, Exception;
}
