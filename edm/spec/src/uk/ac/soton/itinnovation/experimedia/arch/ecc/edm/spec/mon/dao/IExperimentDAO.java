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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao;

import java.util.Set;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.NoDataException;

/**
 * A DAO to save and get Experiment objects from storage.
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
public interface IExperimentDAO
{
    /**
     * Save an experiment (must have a unique UUID).
     * @param exp The experiment instance to be saved (must have a unique UUID).
     * @throws IllegalArgumentException If the Experiment is not valid to be saved, typically due to missing information.
     * @throws Exception If there's a technical issue or an experiment with the same UUID already exists.
     */
    public void saveExperiment(Experiment exp) throws IllegalArgumentException, Exception;
    
    /**
     * Get an experiment instance according to an experiment UUID.
     * @param expUUID The UUID of the experiment.
     * @param withSubClasses Flag to say whether to return subclasses too; MetricGenerator and sub-classes below that.
     * @return An experiment instance with all sub-classes except for measurements.
     * @throws IllegalArgumentException If expUUID is not a valid argument (e.g., NULL).
     * @throws NoDataException If there's no experiment with the given UUID.
     * @throws Exception If there's a technical issue or there is no experiment with the given UUID.
     */
    public Experiment getExperiment(UUID expUUID, boolean withSubClasses) throws NoDataException, IllegalArgumentException, Exception;
    
    /**
     * Get all existing experiments.
     * @param withSubClasses Flag to say whether to return subclasses too; MetricGenerator and sub-classes below that.
     * @return A set of Experiment objects, if any exist.
     * @throws NoDataException If there are no experiments.
     * @throws Exception If there's a technical issue.
     */
    public Set<Experiment> getExperiments(boolean withSubClasses) throws NoDataException, Exception;
}
