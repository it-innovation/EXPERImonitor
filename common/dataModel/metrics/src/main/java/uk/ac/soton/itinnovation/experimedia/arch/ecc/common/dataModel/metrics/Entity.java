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
//      Created Date :          2012-08-09
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * An experiment can consist of many different entities that are monitored, some
 * of which could be system components, virtual resources or human beings. Each of
 * which have attributes that can be monitored, which are specified for each entity.
 * 
 * An entity can be described in terms of a name, description and a unique identifier.
 * It is also described in terms of observable attributes that can be monitored.
 * Any measurements produced by metric generators will refer to a specific attribute
 * of an entity.
 * 
 * Examples of entities include:
 *   - A person
 *   - A system component / service that could be used in many experiments
 *   - A virtual compute resource
 * 
 * @author Vegard Engen
 */
public class Entity implements Serializable
{
    private UUID uuid; // used to uniquely identify an entity in our framework
    private String entityID; // can be used to identify the entity according to 'external' ID
    private String name;
    private String description;
    private Set<Attribute> attributes;
    
    // other attributes needed?
    //   - identifier such as URI? 
    //   - type of entity
    //   - specification of that entity etc (if VM, then instance type etc)
    //   - add a Map of key-value pairs of things for such general spec information?

    /**
     * Default constructor, which creates a random UUID for this object instance
     * and initialises the attributes and experimentUUIDs sets.
     */
    public Entity()
    {
        this.uuid = UUID.randomUUID();
        this.attributes = new HashSet<Attribute>();
    }
    
    /**
     * A copy constructor; takes a deep copy of the attributes and experiment UUIDs.
     * @param e An Entity object from which a copy is made.
     */
    public Entity(Entity e)
    {
        if (e == null)
            return;
        
        if (e.getUUID() != null)
            this.uuid = UUID.fromString(e.getUUID().toString());
        this.name = e.getName();
        this.description = e.getDescription();
        this.entityID = e.getEntityID();
        
        this.attributes = new HashSet<Attribute>();
        if (e.getAttributes() != null)
        {
            for (Attribute a : e.getAttributes())
            {
                if (a != null)
                    this.attributes.add(new Attribute(a));
            }
        }
    }
    
    /**
     * A constructor to set all the fields of the entity class except for the attributes.
     * @param uuid A UUID used to uniquely identify an entity in this framework.
     */
    public Entity(UUID uuid)
    {
        this();
        this.uuid = uuid;
    }
    
    /**
     * A constructor to set all the fields of the entity class except for the attributes.
     * @param uuid A UUID used to uniquely identify an entity in this framework.
     * @param name The name of the Entity.
     * @param description A description of the entity.
     */
    public Entity(UUID uuid, String name, String description)
    {
        this(uuid);
        this.name = name;
        this.description = description;
    }
    
    /**
     * A constructor to set all the fields of the entity class.
     * @param uuid A UUID used to uniquely identify an entity in this framework.
     * @param name The name of the Entity.
     * @param description A description of the entity.
     * @param attributes A set of attributes of the entity, which could be observed to generate metrics.
     */
    public Entity(UUID uuid, String name, String description, Set<Attribute> attributes)
    {
        this(uuid, name, description);
        this.attributes = attributes;
    }
    
    /**
     * A constructor to set all the fields of the entity class except for the attributes.
     * @param uuid A UUID used to uniquely identify an entity in this framework.
     * @param entityID An ID that can be used if the Entity is known by a particular ID outside of the framework.
     * @param name The name of the Entity.
     * @param description A description of the entity.
     */
    public Entity(UUID uuid, String entityID, String name, String description)
    {
        this(uuid);
        this.name = name;
        this.description = description;
        this.entityID = entityID;
    }
    
    /**
     * A constructor to set all the fields of the entity class.
     * @param uuid A UUID used to uniquely identify an entity in this framework.
     * @param entityID An ID that can be used if the Entity is known by a particular ID outside of the framework.
     * @param name The name of the Entity.
     * @param description A description of the entity.
     * @param attributes A set of attributes of the entity, which could be observed to generate metrics.
     */
    public Entity(UUID uuid, String entityID, String name, String description, Set<Attribute> attributes)
    {
        this(uuid, entityID, name, description);
        this.attributes = attributes;
    }
    
    /**
     * @return the uuid
     */
    public UUID getUUID()
    {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUUID(UUID uuid)
    {
        this.uuid = uuid;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the attributes
     */
    public Set<Attribute> getAttributes()
    {
        return attributes;
    }

    /**
     * @param attributes the attributes to set
     */
    public void setAttributes(Set<Attribute> attributes)
    {
        this.attributes = attributes;
    }
    
    /**
     * @param attribute the attribute to add
     */
    public void addAttribute(Attribute attribute)
    {
        if (attribute == null)
            return;
        
        if (this.attributes == null)
            this.attributes = new HashSet<Attribute>();
        
        this.attributes.add(attribute);
    }
    
    /**
     * @param attributes the attributes to add
     */
    public void addAttributes(Set<Attribute> attributes)
    {
        if (attributes == null || attributes.isEmpty())
            return;
        
        if (this.attributes == null)
            this.attributes = new HashSet<Attribute>();
        
        this.attributes.addAll(attributes);
    }
    
    @Override
    public String toString()
    {
        return name;
    }

    /**
     * @return the entityID
     */
    public String getEntityID()
    {
        return entityID;
    }

    /**
     * @param entityID the entityID to set
     */
    public void setEntityID(String entityID)
    {
        this.entityID = entityID;
    }

    /**
     * @return the experimentUUIDs
     *
    public Set<UUID> getExperimentUUIDs()
    {
        return experimentUUIDs;
    }*/

    /**
     * @param experimentUUIDs the experimentUUIDs to set
     *
    public void setExperimentUUIDs(Set<UUID> experimentUUIDs)
    {
        this.experimentUUIDs = experimentUUIDs;
    }*/
    
    /**
     * @param experimentUUID the experimentUUID to add
     *
    public void addExperimentUUID(UUID experimentUUID)
    {
        if (experimentUUID == null)
            return;
        
        if (this.experimentUUIDs == null)
            this.experimentUUIDs = new HashSet<UUID>();
        
        this.experimentUUIDs.add(experimentUUID);
    }*/
    
    /**
     * @param experimentUUIDs the experimentUUIDs to add
     *
    public void addExperimentUUIDs(Set<UUID> experimentUUIDs)
    {
        if ((experimentUUIDs == null) || experimentUUIDs.isEmpty())
            return;
        
        if (this.experimentUUIDs == null)
            this.experimentUUIDs = new HashSet<UUID>();
        
        this.experimentUUIDs.addAll(experimentUUIDs);
    }*/
}
