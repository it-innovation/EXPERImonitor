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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao;

import java.util.Set;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.NoDataException;

/**
 * A DAO to save and get Entity and Attribute objects from storage.
 * 
 * OBS: the get methods will return all sub-classes except for measurements. To
 * get actual measurements for a measurement set, one of the specific getMeasurement(s)
 * methods need to be called, passing on the UUID of the measurement set (and
 * any other arguments for the respective method).
 * 
 * @author Vegard Engen
 */
public interface IEntityDAO
{
    /**
     * Saves an entity instance, which must have a unique UUID. Any attributes that
     * the entity may have will also be saved.
     * @param ent The Entity instance to be saved (must have a unique UUID).
     * @throws IllegalArgumentException If the Entity is not valid to be saved, typically due to missing information.
     * @throws Exception If there's a technical issue or an entity with the same UUID already exists.
     */
    void saveEntity(Entity ent) throws IllegalArgumentException, Exception;
    
    /**
     * Get an entity instance from a UUID, which will include any attributes the
     * entity may have.
     * @param entityUUID The UUID of the entity.
     * @param withAttributes Flag to say whether to return attributes too.
     * @return An Entity instance.
     * @throws IllegalArgumentException If entityUUID is not a valid argument (e.g., NULL).
     * @throws NoDataException If there's no entity with the given UUID.
     * @throws Exception If there's a technical issue.
     */
    Entity getEntity(UUID entityUUID, boolean withAttributes) throws IllegalArgumentException, NoDataException, Exception;
    
    /**
     * Get all the existing entities, if any.
     * @param withAttributes Flag to say whether to return attributes too.
     * @return A set of Entity objects, if any exist.
     * @throws NoDataException If there are no entities.
     * @throws Exception If there's a technical issue.
     */
    Set<Entity> getEntities(boolean withAttributes) throws NoDataException, Exception;
    
    /**
     * Get all the entities for a specific experiment, if any.
     * @param expUUID The experiment UUID.
     * @param withSubClasses Flag to say whether to return attributes too.
     * @return A set of Entity objects, if any are being monitored within the given experiment.
     * @throws IllegalArgumentException If expUUID is not a valid argument (e.g., NULL).
     * @throws NoDataException If there's no entity/entities for the given experiment UUID.
     * @throws Exception If there's a technical issue.
     */
    Set<Entity> getEntitiesForExperiment(UUID expUUID, boolean withAttributes) throws IllegalArgumentException, NoDataException, Exception;
    
    /**
     * Get all the entities for a specific metric generator, if any.
     * @param mGenUUID The metric generator UUID.
     * @param withAttributes Flag to say whether to return attributes too.
     * @return A set of Entity objects, if any are monitored by the given metric generator.
     * @throws IllegalArgumentException If mGenUUID is not a valid argument (e.g., NULL).
     * @throws NoDataException If there's no entity/entities for the given metric generator UUID, or there's no metric generator with the given UUID.
     * @throws Exception If there's a technical issue.
     */
    Set<Entity> getEntitiesForMetricGenerator(UUID mGenUUID, boolean withAttributes) throws IllegalArgumentException, NoDataException, Exception;
    
    
    //--------------------------- ATTRIBUTE ----------------------------------//
    
    
    /**
     * Saves an attribute instance, which must have the UUID of an already saved
     * Entity, and must have a unique UUID itself.
     * @param attrib The attribute instance that should be saved.
     * @throws IllegalArgumentException If the Attribute is not valid to be saved, typically due to missing information.
     * @throws Exception If there's a technical issue or an attribute with the same UUID already exists.
     */
    void saveAttribute(Attribute attrib) throws IllegalArgumentException, Exception;
    
    /**
     * Get an attribute instance according to the UUID, if it exists.
     * @param attribUUID The attribute UUID.
     * @return An attribute instance, if it exists.
     * @throws IllegalArgumentException If attribUUID is not a valid argument (e.g., NULL).
     * @throws NoDataException If there's no attribute with the given UUID.
     * @throws Exception If there's a technical issue or there is no attribute with the given UUID.
     */
    Attribute getAttribute(UUID attribUUID) throws IllegalArgumentException, NoDataException, Exception;
    
    /**
     * Get all attribute instances for an entity, according to the entity UUID.
     * @param entityUUID The entity UUID.
     * @return A set of Attribute objects, if any exist for the given entity.
     * @throws IllegalArgumentException If entityUUID is not a valid argument (e.g., NULL).
     * @throws NoDataException If there's no entity with the given UUID, or it has no attributes.
     * @throws Exception If there's a technical issue.
     */
    Set<Attribute> getAttributesForEntity(UUID entityUUID) throws IllegalArgumentException, NoDataException, Exception;
}
