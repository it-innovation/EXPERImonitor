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
//      Created Date :          2012-08-21
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao;

import java.util.Set;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;

/**
 * A DAO to save and get MetricGenerator objects from storage.
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
public interface IMetricGeneratorDAO
{
    /**
     * Saves a metric generator instance, which must have a unique UUID. The 
     * experiment UUID is required, which must refer to an already existing
     * experiment.
     * 
     * Any sub-classes of the metric generator instance will also be saved (if
     * not null).
     * @param metricGen The metric generator instance to be saved.
     * @param experimentUUID The experiment UUID.
     * @throws Exception If there's a technical issue or a metric generator with the same UUID already exists.
     */
    public void saveMetricGenerator(MetricGenerator metricGen, UUID experimentUUID) throws Exception;
    
    /**
     * Get a metric generator instance from a UUID. Will include all sub-classes
     * except for measurements, if available.
     * @param metricGenUUID The metric generator UUID.
     * @return A metric generator instance if it exists.
     * @throws Exception If there's a technical issue or there is no metric generator with the given UUID.
     */
    public MetricGenerator getMetricGenerator(UUID metricGenUUID) throws Exception;
    
    /**
     * Get all metric generators. Will include all sub-classes except for measurements,
     * if available.
     * @return Empty set if there are no metric generators.
     * @throws Exception If there's a technical issue.
     */
    public Set<MetricGenerator> getMetricGenerators() throws Exception;
    
    /**
     * Get all metric generators for an experiment. Will include all sub-classes
     * except for measurements, if available.
     * @param expUUID The experiment UUID.
     * @return Empty set if no metric generators exist for the experiment.
     * @throws Exception If there's a technical issue or there is no experiment with the given UUID.
     */
    public Set<MetricGenerator> getMetricGeneratorsForExperiment(UUID expUUID) throws Exception;
}
