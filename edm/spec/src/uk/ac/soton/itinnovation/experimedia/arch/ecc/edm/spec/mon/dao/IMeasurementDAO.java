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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.NoDataException;

/**
 * A DAO to save and get Measurement objects from storage.
 * 
 * OBS: no delete methods yet.
 * OBS: no update methods yet.
 * 
 * @author Vegard Engen
 */
public interface IMeasurementDAO
{
    /**
     * Saves a measurement, which must have a unique UUID and refer to an existing
     * measurement set (by its UUID).
     * @param measurement
     * @throws IllegalArgumentException If the Measurement is not valid to be saved, typically due to missing information (e.g., NULL values).
     * @throws Exception If there's a technical issue or a measurement with the same UUID already exists.
     */
    public void saveMeasurement(Measurement measurement) throws IllegalArgumentException, Exception;
    
    /**
     * Saves measurements for an existing measurement set, and will create a
     * Report for them.
     * @param measurements The measurements to save.
     * @param mSetUUID The UUID of the measurement set.
     * @throws IllegalArgumentException If the arguments are invalid, typically due to missing information (e.g., NULL values).
     * @throws Exception If there's a technical issue, if the measurement set does not exist, or a measurement with the same UUID already exists.
     */
    public void saveMeasurementsForSet(Set<Measurement> measurements, UUID mSetUUID) throws IllegalArgumentException, NoDataException, Exception;
    
    /**
     * Get a measurement according to its UUID.
     * @param measurementUUID The measurement UUID.
     * @return A measurement object, if it exists.
     * @throws IllegalArgumentException If measurementUUID is not a valid argument (e.g., NULL).
     * @throws NoDataException If there's no measurement with the given UUID.
     * @throws Exception If there's a technical issue.
     */
    public Measurement getMeasurement(UUID measurementUUID) throws IllegalArgumentException, NoDataException, Exception;
    
    /**
     * Set the synchronisation flag for the given measurement.
     * @param measurementUUID The UUID of the measurement.
     * @param syncFlag The synchronisation flag.
     * @throws IllegalArgumentException If measurementUUID is not a valid argument (e.g., NULL).
     * @throws Exception If there's a technical issue, if the measurement set does not exist, or a measurement with the same UUID already exists.
     */
    public void setSyncFlagForAMeasurement(UUID measurementUUID, boolean syncFlag) throws IllegalArgumentException, Exception;
    
    /**
     * Set the synchronisation flag for all the measurements in the given set.
     * @param measurements The set of UUIDs to set the synchronisation flag for.
     * @param syncFlag The synchronisation flag.
     * @throws IllegalArgumentException If measurement set is not a valid argument (e.g., NULL or empty).
     * @throws Exception If there's a technical issue, if the measurement set does not exist, or a measurement with the same UUID already exists.
     */
    public void setSyncFlagForMeasurements(Set<UUID> measurements, boolean syncFlag) throws IllegalArgumentException, Exception;
}
