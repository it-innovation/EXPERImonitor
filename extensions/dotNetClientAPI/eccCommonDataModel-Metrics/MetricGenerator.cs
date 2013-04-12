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
 * This class represents something or someone who generates and provides metrics
 * about the attributes of certain entities. This could be, for example, a 
 * computational process or a human being operating a mobile device.
 * 
 * Metrics are organised within metric groups. It is possible to define a hierarchy
 * of metric groups as well, as a metric group can contain a set of metric groups.
 * 
 * @author Vegard Engen
 */
public class MetricGenerator
{
    /**
     * Default constructor, which sets a random UUID for the object instance.
     */
    public MetricGenerator()
    {
        this.uuid = Guid.NewGuid();
        this.metricGroups = new HashSet<MetricGroup>();
        this.entities = new HashSet<Entity>();
    }
    
    /**
     * A copy constructor; takes a deep copy of the UUID, metric groups and entities.
     * @param mg A metric generator object from which a copy is made.
     */
    public MetricGenerator(MetricGenerator mg)
    {
        if (mg == null)
            return;
        
        if (mg.uuid != null)
            this.uuid = new Guid( mg.uuid.ToString() );
        
        this.name = mg.name;
        this.description = mg.description;
        
        this.metricGroups = new HashSet<MetricGroup>();
        if (mg.metricGroups != null)
        {
            foreach ( MetricGroup mgroup in mg.metricGroups )
            {
                if (mgroup != null)
                    this.metricGroups.Add(new MetricGroup(mgroup));
            }
        }
        
        this.entities = new HashSet<Entity>();
        if (mg.entities != null)
        {
            foreach ( Entity e in mg.entities )
            {
                if (e != null)
                    this.entities.Add(new Entity(e));
            }
        }
    }
    
    /**
     * Constructor to set all fields of the Metric Generator class.
     * @param uuid The UUID used to uniquely identify a metric generator in this framework.
     * @param name The name of the metric generator.
     * @param description A description of the metric generator.
     */
    public MetricGenerator(Guid uuid, string name, string description) : this()
    {

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
     * @param entities A set entities being observed.
     */
    public MetricGenerator(Guid uuid, string name, string description, HashSet<MetricGroup> metricGroups, HashSet<Entity> entities)
        : this(uuid, name, description)
    {
        this.metricGroups = metricGroups;
        this.entities = entities;
    }

    public Guid uuid
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

    public HashSet<MetricGroup> metricGroups
    {
        get;
        set;
    }

    public HashSet<Entity> entities
    {
        get;
        set;
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
        
        this.metricGroups.Add(metricGroup);
    }
    
    /**
     * @param metricGroups the metric groups to add
     */
    public void addMetricGroups(HashSet<MetricGroup> metricGroups)
    {
        if ( (metricGroups == null) || metricGroups.Count == 0 )
            return;
        
        if (this.metricGroups == null)
            this.metricGroups = new HashSet<MetricGroup>();
        
        foreach ( MetricGroup mg in metricGroups )
            this.metricGroups.Add( mg );
    }
    
    /**
     * @param entity the entity to add
     */
    public void addEntity(Entity entity)
    {
        if (entity == null)
            return;
        
        if (this.entities == null)
            this.entities = new HashSet<Entity>();
        
        this.entities.Add(entity);
    }
    
    /**
     * @param entities the entities to add
     */
    public void addEntities(HashSet<Entity> entities)
    {
        if ((entities == null) || entities.Count == 0)
            return;
        
        if (this.entities == null)
            this.entities = new HashSet<Entity>();
        
        foreach ( Entity e in entities )
            this.entities.Add( e );
    }
    
    public string toString()
    {
        return name;
    }
}

} // namespace