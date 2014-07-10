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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;

/**
 * An experiment is the top-level class that all metric generators (and entities)
 * are part of.
 * 
 * An experiment can consist of many different entities that are monitored, some
 * of which could be system components, virtual resources or human beings. Each of
 * which have attributes that can be monitored, which are specified for each entity.
 * 
 * @author Vegard Engen
 */
public class Experiment implements Serializable
{
    private UUID uuid; // used to uniquely identify an experiment in this framework
    private String experimentID; // as per the facility the experiment is running in
    private String name;
    private String description;
    private Date startTime;
    private Date endTime;
    private Set<MetricGenerator> metricGenerators;

    // other things?
    //   - wall time? <-- an experiment may be ended before the wall time value set..

    /**
     * Default constructor which sets a random UUID for the object instance.
     */
    public Experiment()
    {
        this.uuid = UUID.randomUUID();
        this.metricGenerators = new HashSet<MetricGenerator>();
    }
    
    /**
     * Copy constructor; does a deep copy of the sets of entities and metric generators.
     * @param ex Experiment object a copy should be made from.
     */
    public Experiment(Experiment ex)
    {
        if (ex == null)
            return;
        
        if (ex.getUUID() != null)
            this.uuid = UUID.fromString(ex.getUUID().toString());
        this.experimentID = ex.getExperimentID();
        this.name = ex.getName();
        this.description = ex.getDescription();
        if (ex.getStartTime() != null)
            this.startTime = new Date(ex.getStartTime().getTime());
        if (ex.getEndTime() != null)
            this.endTime = new Date(ex.getEndTime().getTime());
        
        this.metricGenerators = new HashSet<MetricGenerator>();
        if (ex.getMetricGenerators() != null)
        {
            for (MetricGenerator mg: ex.getMetricGenerators())
            {
                if (mg != null)
                    this.metricGenerators.add(new MetricGenerator(mg));
            }
        }
    }
    
    /**
     * Constructor to set the basic information about the experiment likely known
     * at the start.
     * @param uuid UUID used to uniquely identify an experiment in this framework.
     * @param experimentID An experiment ID, as per the facility the experiment is/was running in.
     * @param name Name of the experiment.
     * @param description A description of the experiment.
     * @param creationTime The time stamp when the experiment was started.
     */
    public Experiment(UUID uuid, String experimentID, String name, String description, Date creationTime)
    {
        this();
        this.uuid = uuid;
        this.experimentID = experimentID;
        this.name = name;
        this.description = description;
        this.startTime = creationTime;
    }
    
    /**
     * Constructor to set all the information about the experiment.
     * @param uuid UUID used to uniquely identify an experiment in this framework.
     * @param experimentID An experiment ID, as per the facility the experiment is/was running in.
     * @param name Name of the experiment.
     * @param description A description of the experiment.
     * @param creationTime The time stamp when the experiment was started.
     * @param endTime The time stamp when the experiment ended.
     */
    public Experiment(UUID uuid, String experimentID, String name, String description, Date creationTime, Date endTime)
    {
        this(uuid, experimentID, name, description, creationTime);
        this.endTime = endTime;
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
     * @return the experimentID
     */
    public String getExperimentID()
    {
        return experimentID;
    }

    /**
     * @param experimentID the experimentID to set
     */
    public void setExperimentID(String experimentID)
    {
        this.experimentID = experimentID;
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
     * @return the startTime
     */
    public Date getStartTime()
    {
        return startTime;
    }

    /**
     * @param creationTime the startTime to set
     */
    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    /**
     * @return the endTime
     */
    public Date getEndTime()
    {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }
    
    /**
     * @return the metricGenerators
     */
    public Set<MetricGenerator> getMetricGenerators()
    {
        return metricGenerators;
    }

    /**
     * @param metricGenerators the metricGenerators to set
     */
    public void setMetricGenerators(Set<MetricGenerator> metricGenerators)
    {
        this.metricGenerators = metricGenerators;
    }
    
    /**
     * @param metricGenerator the metric generator to add
     */
    public void addMetricGenerator(MetricGenerator metricGenerator)
    {
        if (metricGenerator == null)
            return;
        
        if (this.metricGenerators == null)
            this.metricGenerators = new HashSet<MetricGenerator>();
        
        this.metricGenerators.add(metricGenerator);
    }
    
    /**
     * @param metricGenerators the metric generators to add
     */
    public void addMetricGenerators(Set<MetricGenerator> metricGenerators)
    {
        if (metricGenerators == null || metricGenerators.isEmpty())
            return;
        
        if (this.metricGenerators == null)
            this.metricGenerators = new HashSet<MetricGenerator>();
        
        this.metricGenerators.addAll(metricGenerators);
    }
}
