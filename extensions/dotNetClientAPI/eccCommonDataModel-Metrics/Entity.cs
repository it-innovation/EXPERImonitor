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
//      Created By :            Simon Crowle
//      Created Date :          09-Apr-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using System;
using System.Collections.Generic;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics
{

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
public class Entity
{
    /**
     * Default constructor, which creates a random UUID for this object instance
     * and initialises the attributes and experimentUUIDs sets.
     */
    public Entity()
    {
        this.uuid = Guid.NewGuid();
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
        
        if (e.uuid != null)
            this.uuid = new Guid( e.uuid.ToString() );

        this.name = e.name;
        this.description = e.description;
        this.entityID = e.entityID;

        this.attributes = new HashSet<Attribute>();

        if (e.attributes != null)
        {
            foreach (Attribute a in e.attributes)
            {
                if (a != null)
                    this.attributes.Add(new Attribute(a));
            }
        }
    }
    
    /**
     * A constructor to set all the fields of the entity class except for the attributes.
     * @param uuid A UUID used to uniquely identify an entity in this framework.
     */
    public Entity(Guid uuid) : this()
    {
        this.uuid = uuid;
    }
    
    /**
     * A constructor to set all the fields of the entity class except for the attributes.
     * @param uuid A UUID used to uniquely identify an entity in this framework.
     * @param name The name of the Entity.
     * @param description A description of the entity.
     */
    public Entity(Guid uuid, string name, string description) : this(uuid)
    {
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
    public Entity(Guid uuid, string name, string description, HashSet<Attribute> attributes)
        : this(uuid, name, description)
    {
        this.attributes = attributes;
    }
    
    /**
     * A constructor to set all the fields of the entity class except for the attributes.
     * @param uuid A UUID used to uniquely identify an entity in this framework.
     * @param entityID An ID that can be used if the Entity is known by a particular ID outside of the framework.
     * @param name The name of the Entity.
     * @param description A description of the entity.
     */
    public Entity(Guid uuid, string entityID, string name, string description) : this(uuid)
    {
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
    public Entity(Guid uuid, string entityID, string name, string description, HashSet<Attribute> attributes)
        : this(uuid, entityID, name, description)
    {
        this.attributes = attributes;
    }

    public Guid uuid
    {
        get;
        set;
    }

    public string entityID
    {
        get;
        set;
    }

    public string name
    {
        get;
        set;
    }

    public string description
    {
        get;
        set;
    }

    public HashSet<Attribute> attributes
    {
        get;
        set;
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
        
        this.attributes.Add(attribute);
    }
    
    /**
     * @param attributes the attributes to add
     */
    public void addAttributes(Dictionary<Guid,Attribute> attributes)
    {
        if ((attributes == null) || attributes.Count == 0)
            return;
        
        if (this.attributes == null)
            this.attributes = new HashSet<Attribute>();
        
        foreach( Attribute attr in attributes.Values )
            this.attributes.Add( attr );

    }
    
    public string toString()
    {
        return name;
    }
}

} // namespace
