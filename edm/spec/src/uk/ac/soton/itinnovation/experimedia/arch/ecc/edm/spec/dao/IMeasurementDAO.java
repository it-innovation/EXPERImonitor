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

import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;

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
     * @throws Exception If there's a technical issue or a measurement with the same UUID already exists.
     */
    public void saveMeasurement(Measurement measurement) throws Exception;
    
    /**
     * Get a measurement according to its UUID.
     * @param measurementUUID The measurement UUID.
     * @return A measurement object, if it exists.
     * @throws Exception If there's a technical issue or there is no measurement with the given UUID.
     */
    public Measurement getMeasurement(UUID measurementUUID) throws Exception;
}
