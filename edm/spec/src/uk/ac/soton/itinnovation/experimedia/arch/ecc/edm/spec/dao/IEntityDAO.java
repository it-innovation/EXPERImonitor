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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;

/**
 * A DAO to save and get Entity and Attribute objects from storage.
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
public interface IEntityDAO
{
/**
     * Saves an entity instance, which must have a unique UUID. Any attributes that
     * the entity may have will also be saved.
     * @param ent The Entity instance to be saved (must have a unique UUID).
     * @throws Exception If there's a technical issue or an entity with the same UUID already exists.
     */
    public void saveEntity(Entity ent) throws Exception;
    
    /**
     * Get an entity instance from a UUID, which will include any attributes the
     * entity may have.
     * @param entityUUID The UUID of the entity.
     * @return An Entity instance.
     * @throws Exception If there's a technical issue or there is no entity with the given UUID.
     */
    public Entity getEntity(UUID entityUUID) throws Exception;
    
    /**
     * Get all the existing entities, if any.
     * @return Empty set if there are no entities.
     * @throws Exception If there's a technical issue.
     */
    public Set<Entity> getEntities() throws Exception;
    
    /**
     * Get all the entities for a specific experiment, if any.
     * @param expUUID The experiment UUID.
     * @return An empty set if no entities monitored in the given experiment exist.
     * @throws Exception If there's a technical issue or there is no experiment with the given UUID.
     */
    public Set<Entity> getEntitiesForExperiment(UUID expUUID) throws Exception;
    
    /**
     * Get all the entities for a specific metric generator, if any.
     * @param mGenUUID The metric generator UUID.
     * @return An empty set if no entities monitored by the given metric generator exists.
     * @throws Exception If there's a technical issue or there is no metric generator with the given UUID.
     */
    public Set<Entity> getEntitiesForMetricGenerator(UUID mGenUUID) throws Exception;
    
    
    //--------------------------- ATTRIBUTE ----------------------------------//
    
    
    /**
     * Saves an attribute instance, which must have the UUID of an already saved
     * Entity, and must have a unique UUID itself.
     * @param attrib The attribute instance that should be saved.
     * @throws Exception If there's a technical issue or an attribute with the same UUID already exists.
     */
    public void saveAttribute(Attribute attrib) throws Exception;
    
    /**
     * Get an attribute instance according to the UUID, if it exists.
     * @param attribUUID The attribute UUID.
     * @return An attribute instance, if it exists.
     * @throws Exception If there's a technical issue or there is no attribute with the given UUID.
     */
    public Attribute getAttribute(UUID attribUUID) throws Exception;
    
    /**
     * Get all attribute instances for an entity, according to the entity UUID.
     * @param entityUUID The entity UUID.
     * @return Empty set if no attributes exist for the entity.
     * @throws Exception If there's a technical issue or there is no entity with the given UUID.
     */
    public Set<Attribute> getAttributesForEntity(UUID entityUUID) throws Exception;
}
