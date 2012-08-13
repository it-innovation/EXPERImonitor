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
 * This class represents something or someone who generates and provides metrics
 * about the attributes of certain entities. This could be, for example, a 
 * computational process or a human being operating a mobile device.
 * 
 * Metrics are organised within metric groups. It is possible to define a hierarchy
 * of metric groups as well, as a metric group can contain a set of metric groups.
 * 
 * @author Vegard Engen
 */
public class MetricGenerator implements Serializable
{
    private UUID uuid;
    private String name;
    private String description;
    private Set<MetricGroup> metricGroups; // change if the ordering is important
    private Set<UUID> entities; // a set of UUIDs of the entities being observed
    
    // other things needed?
    //   - identifier such as URI?
    //   - need to say anything about what type of generator it is?

    /**
     * Default constructor, which sets a random UUID for the object instance.
     */
    public MetricGenerator()
    {
        this.uuid = UUID.randomUUID();
        this.metricGroups = new HashSet<MetricGroup>();
        this.entities = new HashSet<UUID>();
    }
    
    /**
     * A copy constructor; takes a deep copy of the UUID, metric groups and entities.
     * @param mg A metric generator object from which a copy is made.
     */
    public MetricGenerator(MetricGenerator mg)
    {
        if (mg == null)
            return;
        
        this.uuid = UUID.fromString(mg.getUUID().toString());
        this.name = mg.getName();
        this.description = mg.getDescription();
        
        this.metricGroups = new HashSet<MetricGroup>();
        if (mg.getMetricGroups() != null)
        {
            for (MetricGroup mgroup : mg.getMetricGroups())
                this.metricGroups.add(new MetricGroup(mgroup));
        }
        
        this.entities = new HashSet<UUID>();
        if (mg.getEntities() != null)
        {
            for (UUID e : mg.getEntities())
                this.entities.add(UUID.fromString(e.toString()));
        }
    }
    
    /**
     * Constructor to set all fields of the Metric Generator class.
     * @param uuid The UUID used to uniquely identify a metric generator in this framework.
     * @param name The name of the metric generator.
     * @param description A description of the metric generator.
     */
    public MetricGenerator(UUID uuid, String name, String description)
    {
        this();
        this.uuid = uuid;
        this.name = name;
        this.description = description;
    }
    
    /**
     * Constructor to set all fields of the Metric Generator class.
     * @param uuid The UUID used to uniquely identify a metric generator in this framework.
     * @param name The name of the metric generator.
     * @param description A description of the metric generator.
     * @param metricGroups A set of metric groups.
     * @param entities A set UUIDs of entities being observed.
     */
    public MetricGenerator(UUID uuid, String name, String description, Set<MetricGroup> metricGroups, Set<UUID> entities)
    {
        this(uuid, name, description);
        this.metricGroups = metricGroups;
        this.entities = entities;
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
     * @return the metricGroups
     */
    public Set<MetricGroup> getMetricGroups()
    {
        return metricGroups;
    }

    /**
     * @param metricGroups the metricGroups to set
     */
    public void setMetricGroups(Set<MetricGroup> metricGroups)
    {
        this.metricGroups = metricGroups;
    }
    
    /**
     * @param metricGroup the metric group to add
     */
    public void addMetricGroup(MetricGroup metricGroup)
    {
        if (metricGroup == null)
            return;
        
        if (this.metricGroups == null)
            this.metricGroups = new HashSet<MetricGroup>();
        
        this.metricGroups.add(metricGroup);
    }
    
    /**
     * @param metricGroups the metric groups to add
     */
    public void addMetricGroups(Set<MetricGroup> metricGroups)
    {
        if ((metricGroups == null) || metricGroups.isEmpty())
            return;
        
        if (this.metricGroups == null)
            this.metricGroups = new HashSet<MetricGroup>();
        
        this.metricGroups.addAll(metricGroups);
    }

    /**
     * @return the entities
     */
    public Set<UUID> getEntities()
    {
        return entities;
    }

    /**
     * @param entities the entities to set
     */
    public void setEntities(Set<UUID> entities)
    {
        this.entities = entities;
    }
    
    /**
     * @param entityUUID the entity to add
     */
    public void addEntity(UUID entityUUID)
    {
        if (entityUUID == null)
            return;
        
        if (this.entities == null)
            this.entities = new HashSet<UUID>();
        
        this.entities.add(entityUUID);
    }
    
    /**
     * @param entities the entities to add
     */
    public void addEntities(Set<UUID> entities)
    {
        if ((entities == null) || entities.isEmpty())
            return;
        
        if (this.entities == null)
            this.entities = new HashSet<UUID>();
        
        this.entities.addAll(entities);
    }
}
