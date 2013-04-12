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
 * This class provides the mechanism to group metrics, given a name and a description.
 * 
 * Example groups:
 *   - User related metrics
 *     - clicks... errors
 *   - Computational / QoS
 *     - response time.. 
 * @author Vegard Engen
 */
public class MetricGroup
{
    /**
     * Default constructor that sets a random UUID for this object instance.
     */
    public MetricGroup()
    {
        this.uuid = Guid.NewGuid();
        this.measurementSets = new HashSet<MeasurementSet>();
    }
    
    /**
     * A copy constructor, which takes a deep copy of the measurement sets.
     * @param mg A metric group object from which a copy is made.
     */
    public MetricGroup(MetricGroup mg)
    {
        if (mg == null)
            return;
        
        if (mg.uuid != null)
            this.uuid = new Guid( mg.uuid.ToString() );

        if (mg.metricGeneratorUUID != null)
            this.metricGeneratorUUID = new Guid( mg.metricGeneratorUUID.ToString() );

        this.name = mg.name;
        this.description = mg.description;

        this.measurementSets = new HashSet<MeasurementSet>();
        if (mg.measurementSets != null)
        {
            foreach ( MeasurementSet ms in mg.measurementSets )
            {
                if (ms != null)
                    this.measurementSets.Add(new MeasurementSet(ms, true));
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
    public MetricGroup(Guid uuid, Guid metricGeneratorUUID, string name, string description)
    {
        this.uuid = uuid;
        this.metricGeneratorUUID = metricGeneratorUUID;
        this.name = name;
        this.description = description;
    }
    
    /**
     * A constructor to set the basic information of a metric group.
     * @param uuid The UUID used to uniquely identify a metric group in this framework.
     * @param metricGeneratorUUID The UUID of the metric generator that's produced this metric group.
     * @param name The name of the metric group.
     * @param description A description of the metric group.
     * @param measurementSets A set of measurement sets.
     */
    public MetricGroup(Guid uuid, Guid metricGeneratorUUID, String name, String description, HashSet<MeasurementSet> measurementSets)
        : this(uuid, metricGeneratorUUID, name, description)
    {
        this.measurementSets = measurementSets;
    }

    public Guid uuid
    {
        get;
        set;
    }

    public Guid metricGeneratorUUID
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

    public HashSet<MeasurementSet> measurementSets
    {
        get;
        set;
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
        
        this.measurementSets.Add( measurementSet );
    }
    
    /**
     * @param measurementSets the measurement sets to add
     */
    public void addMeasurementSets(Dictionary<Guid, MeasurementSet> measurementSets)
    {
        if ((measurementSets == null) || measurementSets.Count == 0 )
            return;
        
        if (this.measurementSets == null)
            this.measurementSets = new HashSet<MeasurementSet>();
        
        foreach ( MeasurementSet ms in measurementSets.Values )
            this.measurementSets.Add( ms );
    }
    
    public String toString()
    {
        return name;
    }
}
    
} // namespace