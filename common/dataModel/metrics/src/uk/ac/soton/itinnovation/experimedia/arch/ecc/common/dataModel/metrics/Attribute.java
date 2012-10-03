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
import java.util.UUID;

/**
 * This class represents an observable attribute of an entity, which metrics can
 * be generated for.
 * 
 * @author Vegard Engen
 */
public class Attribute implements Serializable
{
    private UUID uuid;
    private UUID entityUUID;
    private String name;
    private String description;
    
    // any other things needed to describe an attribute?
    //   uri?

    /**
     * Default constructor which sets a random UUID for the object instance.
     */
    public Attribute()
    {
        this.uuid = UUID.randomUUID();
    }
    
    /**
     * Copy constructor.
     * @param a The attribute object from which a copy is made.
     */
    public Attribute(Attribute a)
    {
        if (a == null)
            return;
        
        if (a.getUUID() != null)
            this.uuid = UUID.fromString(a.getUUID().toString());
        if (a.getEntityUUID() != null)
            this.entityUUID = UUID.fromString(a.getEntityUUID().toString());
        this.name = a.getName();
        this.description = a.getDescription();
    }
    
    /**
     * Constructor to set all the fields of the Attribute class.
     * @param uuid UUID used to uniquely identify an attribute in this framework.
     * @param entityUUID The UUID of the entity that this attribute is a part of.
     * @param name The name of the attribute.
     * @param description A description of the attribute.
     */
    public Attribute(UUID uuid, UUID entityUUID, String name, String description)
    {
        this.uuid = uuid;
        this.entityUUID = entityUUID;
        this.name = name;
        this.description = description;
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
     * @return the entityUUID
     */
    public UUID getEntityUUID()
    {
        return entityUUID;
    }

    /**
     * @param entityUUID the entityUUID to set
     */
    public void setEntityUUID(UUID entityUUID)
    {
        this.entityUUID = entityUUID;
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
    
    @Override
    public String toString()
    {
        return name;
    }
}
