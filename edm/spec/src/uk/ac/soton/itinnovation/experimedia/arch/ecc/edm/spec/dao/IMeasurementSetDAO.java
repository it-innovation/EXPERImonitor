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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao;

import java.util.Set;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;

/**
 * A DAO to save and get MeasurementSet objects from storage.
 * 
 * OBS: the get methods will return all sub-classes except for measurements. To
 * get actual measurements for a measurement set, one of the specific getMeasurement(s)
 * methods need to be called, passing on the UUID of the measurement set (and
 * any other arguments for the respective method).
 * 
 * OBS: no delete methods yet.
 * OBS: no update methods yet.
 * 
 * @author Vegard Engen
 */
public interface IMeasurementSetDAO
{
    /**
     * Save a measurement set, which must have a unique UUID and refer to an 
     * existing metric group (by its UUID).
     * 
     * Any sub-classes of the measurement set will also be saved (if not null).
     * If there are no measurements, for example, then the measurement set is still
     * saved without any exception thrown.
     * @param measurementSet The measurement set instance to be saved.
     * @throws Exception If there's a technical issue or a measurement set with the same UUID already exists.
     */
    public void saveMeasurementSet(MeasurementSet measurementSet) throws Exception;
    
    /**
     * Get a measurement set instance according to its UUID. Will not include the
     * measurements. Need to use the Report facility for that.
     * @param measurementSetUUID The UUID of the measurement set.
     * @param withMetric Flag to say whether to return the metric too.
     * @return A measurement set object, if it exists.
     * @throws Exception If there's a technical issue or there is no measurement set with the given UUID.
     */
    public MeasurementSet getMeasurementSet(UUID measurementSetUUID, boolean withMetric) throws Exception;
    
    /**
     * Get all measurement sets for a metric group. Will include any sub-classes
     * except for measurements, if available.
     * @param metricGroupUUID The UUID of the metric group.
     * @param withMetric Flag to say whether to return the metric too.
     * @return A measurement set object, if it exists.
     * @throws Exception If there's a technical issue or there is no metric group with the given UUID.
     */
    public Set<MeasurementSet> getMeasurementSetForMetricGroup(UUID metricGroupUUID, boolean withMetric) throws Exception;
}
