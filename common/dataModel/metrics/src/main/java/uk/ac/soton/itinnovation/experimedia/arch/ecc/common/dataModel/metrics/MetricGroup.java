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
 * This class provides the mechanism to group metrics, given a name and a description.
 * 
 * Example groups:
 *   - User related metrics
 *     - clicks... errors
 *   - Computational / QoS
 *     - response time.. 
 * @author Vegard Engen
 */
public class MetricGroup implements Serializable
{
    private UUID uuid;
    private UUID metricGeneratorUUID;
    private String name;
    private String description;
    private Set<MeasurementSet> measurementSets;

    /**
     * Default constructor that sets a random UUID for this object instance.
     */
    public MetricGroup()
    {
        this.uuid = UUID.randomUUID();
        this.measurementSets = new HashSet<>();
    }
    
    /**
     * A copy constructor, which takes a deep copy of the measurement sets.
     * @param mg A metric group object from which a copy is made.
     */
    public MetricGroup(MetricGroup mg)
    {
        if (mg == null)
            return;
        
        if (mg.getUUID() != null)
            this.uuid = UUID.fromString(mg.getUUID().toString());
        if (mg.getMetricGeneratorUUID() != null)
            this.metricGeneratorUUID = UUID.fromString(mg.getMetricGeneratorUUID().toString());
        this.name = mg.getName();
        this.description = mg.getDescription();
        
        this.measurementSets = new HashSet<>();
        if (mg.getMeasurementSets() != null)
        {
            for (MeasurementSet ms : mg.getMeasurementSets())
            {
                if (ms != null)
                    this.measurementSets.add(new MeasurementSet(ms, true));
            }
        }
    }
    
    
    /**
     * A constructor to set the basic information of a metric group.
     * @param uuid The UUID used to uniquely identify a metric group in this framework.
     * @param metricGeneratorUUID The UUID of the metric generator that's produced this metric group.
     * @param name The name of the metric group.
     * @param description A description of the metric group.
     */
    public MetricGroup(UUID uuid, UUID metricGeneratorUUID, String name, String description)
    {
        this.uuid = uuid;
        this.metricGeneratorUUID = metricGeneratorUUID;
        this.name = name;
        this.description = description;
        this.measurementSets = new HashSet<>();
    }
    
    /**
     * A constructor to set the basic information of a metric group.
     * @param uuid The UUID used to uniquely identify a metric group in this framework.
     * @param metricGeneratorUUID The UUID of the metric generator that's produced this metric group.
     * @param name The name of the metric group.
     * @param description A description of the metric group.
     * @param measurementSets A set of measurement sets.
     */
    public MetricGroup(UUID uuid, UUID metricGeneratorUUID, String name, String description, Set<MeasurementSet> measurementSets)
    {
        this(uuid, metricGeneratorUUID, name, description);
        
        if ( measurementSets != null)
            this.measurementSets = measurementSets;
        else
            this.measurementSets = new HashSet<>();
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
     * @return the metricGeneratorUUID
     */
    public UUID getMetricGeneratorUUID()
    {
        return metricGeneratorUUID;
    }

    /**
     * @param metricGeneratorUUID the metricGeneratorUUID to set
     */
    public void setMetricGeneratorUUID(UUID metricGeneratorUUID)
    {
        this.metricGeneratorUUID = metricGeneratorUUID;
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
     * @return the measurementSets
     */
    public Set<MeasurementSet> getMeasurementSets()
    {
        return measurementSets;
    }

    /**
     * @param measurementSets the measurementSets to set
     */
    public void setMeasurementSets(Set<MeasurementSet> measurementSets)
    {
        if ( measurementSets != null )
            this.measurementSets = measurementSets;
    }
    
    /**
     * @param measurementSet the measurement set to add
     */
    public void addMeasurementSets(MeasurementSet measurementSet)
    {
        if (measurementSet == null)
            return;
        
        if (this.measurementSets == null)
            this.measurementSets = new HashSet<MeasurementSet>();
        
        this.measurementSets.add(measurementSet);
    }
    
    /**
     * @param measurementSets the measurement sets to add
     */
    public void addMeasurementSets(Set<MeasurementSet> measurementSets)
    {
        if (measurementSets == null || measurementSets.isEmpty())
            return;
        
        if (this.measurementSets == null)
            this.measurementSets = new HashSet<MeasurementSet>();
        
        this.measurementSets.addAll(measurementSets);
    }
    
    @Override
    public String toString()
    {
        return name;
    }
}
