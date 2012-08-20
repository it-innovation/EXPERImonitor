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
 * This class represents a set of measurements for a specific attribute (of an entity),
 * given a specific metric.
 * 
 * @author Vegard Engen
 */
public class MeasurementSet implements Serializable
{
    private UUID uuid;
    private UUID attributeUUID;
    private UUID metricGroupUUID;
    private Metric metric;
    private Set<Measurement> measurements;
    
    /**
     * Default constructor which sets a random UUID for the object instance.
     */
    public MeasurementSet()
    {
        this.uuid = UUID.randomUUID();
        this.measurements = new HashSet<Measurement>();
    }
    
    /**
     * Copy constructor; does a deep copy of the attribute, metric and measurements.
     * @param ms The metric set object from which a copy is made.
     */
    public MeasurementSet(MeasurementSet ms)
    {
        if (ms == null)
            return;
        
        this.uuid = UUID.fromString(ms.getUUID().toString());
        this.attributeUUID = UUID.fromString(ms.getAttributeUUID().toString());
        this.metricGroupUUID = UUID.fromString(ms.getMetricGroupUUID().toString());
        this.metric = new Metric(ms.getMetric());
        
        this.measurements = new HashSet<Measurement>();
        if (ms.getMeasurements() != null)
        {
            for (Measurement m : ms.getMeasurements())
                this.measurements.add(new Measurement(m));
        }
    }
    
    /**
     * Constructor to set all the attributes of the Measurement Set.
     * @param uuid The UUID used to uniquely identify a measurement set in this framework.
     * @param attributeUUID The UUID of the attribute this measurement set defines measurements of.
     * @param metricGroupUUID The UUID of the metric group this measurement set is a part of.
     * @param metric The metric of the measurements.
     * @param measurements The set of measurements.
     */
    public MeasurementSet(UUID uuid, UUID attributeUUID, UUID metricGroupUUID, Metric metric, Set<Measurement> measurements)
    {
        this();
        this.uuid = uuid;
        this.attributeUUID = attributeUUID;
        this.metricGroupUUID = metricGroupUUID;
        this.metric = metric;
        this.measurements = measurements;
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
     * @return the attributeUUID
     */
    public UUID getAttributeUUID()
    {
        return attributeUUID;
    }

    /**
     * @param attributeUUID the attributeUUID to set
     */
    public void setAttributeUUID(UUID attributeUUID)
    {
        this.attributeUUID = attributeUUID;
    }
    
    /**
     * @return the metricGroupUUID
     */
    public UUID getMetricGroupUUID()
    {
        return metricGroupUUID;
    }

    /**
     * @param metricGroupUUID the metricGroupUUID to set
     */
    public void setMetricGroupUUID(UUID metricGroupUUID)
    {
        this.metricGroupUUID = metricGroupUUID;
    }
    
    /**
     * @return the metric
     */
    public Metric getMetric()
    {
        return metric;
    }

    /**
     * @param metric the metric to set
     */
    public void setMetric(Metric metric)
    {
        this.metric = metric;
    }

    /**
     * @return the measurements
     */
    public Set<Measurement> getMeasurements()
    {
        return measurements;
    }

    /**
     * @param measurements the measurements to set
     */
    public void setMeasurements(Set<Measurement> measurements)
    {
        this.measurements = measurements;
    }
    
    /**
     * Add a measurement to the set.
     * @param measurement the measurement to add.
     */
    public void addMeasurements(Measurement measurement)
    {
        if (measurement == null)
            return;
        
        if (this.measurements == null)
            this.measurements = new HashSet<Measurement>();
        
        this.measurements.add(measurement);
    }
    
    /**
     * Add measurements to the set.
     * @param measurements the set of measurements to add.
     */
    public void addMeasurements(Set<Measurement> measurements)
    {
        if ((measurements == null) || measurements.isEmpty())
            return;
        
        if (this.measurements == null)
            this.measurements = new HashSet<Measurement>();
        
        this.measurements.addAll(measurements);
    }
}
