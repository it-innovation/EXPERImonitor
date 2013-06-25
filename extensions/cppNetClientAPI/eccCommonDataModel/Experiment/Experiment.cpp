/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
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
//      Created Date :          21-June-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#include "stdafx.h"
#include "Experiment.h"


namespace ecc_commonDataModel
{

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
//class Experiment
//{
//    /**
//     * Default constructor which sets a random UUID for the object instance.
//     */
//    public Experiment()
//    {
//        uuid = Guid.NewGuid();
//        metricGenerators = new HashSet<MetricGenerator>();
//    }
//    
//    /**
//     * Copy constructor; does a deep copy of the sets of entities and metric generators.
//     * @param ex Experiment object a copy should be made from.
//     */
//    public Experiment(Experiment ex)
//    {
//        if (ex == null)
//            return;
//        
//        if (ex.uuid != null)
//            this.uuid = new Guid( ex.uuid.ToString() );
//        
//        experimentID = ex.experimentID;
//        name         = ex.name;
//        description  = ex.description;
//
//        if (ex.startTime != null)
//            startTime = new DateTime(ex.startTime.Ticks);
//
//        if (ex.endTime != null)
//            endTime = new DateTime(ex.endTime.Ticks);
//
//        metricGenerators = new HashSet<MetricGenerator>();
//        
//        if (ex.metricGenerators != null)
//        {
//            foreach (MetricGenerator mg in ex.metricGenerators)
//            {
//                if (mg != null)
//                    metricGenerators.Add(new MetricGenerator(mg));
//            }
//        }
//    }
//    
//    /**
//     * Constructor to set the basic information about the experiment likely known
//     * at the start.
//     * @param uuid UUID used to uniquely identify an experiment in this framework.
//     * @param experimentID An experiment ID, as per the facility the experiment is/was running in.
//     * @param name Name of the experiment.
//     * @param description A description of the experiment.
//     * @param creationTime The time stamp when the experiment was started.
//     */
//    public Experiment(Guid uuid, string experimentID, string name, string description, DateTime creationTime) : this()
//    {
//        this.uuid         = uuid;
//        this.experimentID = experimentID;
//        this.name         = name;
//        this.description  = description;
//        this.startTime    = creationTime;
//    }
//    
//    /**
//     * Constructor to set all the information about the experiment.
//     * @param uuid UUID used to uniquely identify an experiment in this framework.
//     * @param experimentID An experiment ID, as per the facility the experiment is/was running in.
//     * @param name Name of the experiment.
//     * @param description A description of the experiment.
//     * @param creationTime The time stamp when the experiment was started.
//     * @param endTime The time stamp when the experiment ended.
//     */
//    public Experiment(Guid uuid, string experimentID, string name, string description, DateTime creationTime, DateTime endTime)
//        : this( uuid, experimentID, name, description, creationTime )
//    {
//        this.endTime = endTime;
//    }
//
//    public Guid uuid
//    {
//        get;
//        set;
//    }
//
//    public string experimentID
//    {
//        get;
//        set;
//    }
//
//    public string name
//    {
//        get;
//        set;
//    }
//
//    public string description
//    {
//        get;
//        set;
//    }
//
//    [JsonConverter(typeof(ECCDateTimeJSONConverter))]
//    public DateTime startTime
//    {
//        get;
//        set;
//    }
//
//    [JsonConverter(typeof(ECCDateTimeJSONConverter))]
//    public DateTime endTime
//    {
//        get;
//        set;
//    }
//
//    public HashSet<MetricGenerator> metricGenerators
//    {
//        get;
//        set;
//    }
//    
//    /**
//     * @param metricGenerator the metric generator to add
//     */
//    public void addMetricGenerator(MetricGenerator metricGenerator)
//    {
//        if (metricGenerator == null)
//            return;
//        
//        if (this.metricGenerators == null)
//            this.metricGenerators = new HashSet<MetricGenerator>();
//        
//        this.metricGenerators.Add(metricGenerator);
//    }
//    
//    /**
//     * @param metricGenerators the metric generators to add
//     */
//    public void addMetricGenerators(HashSet<MetricGenerator> metricGenerators)
//    {
//        if ((metricGenerators == null) || metricGenerators.Count == 0)
//            return;
//        
//        if (this.metricGenerators == null)
//            this.metricGenerators = new HashSet<MetricGenerator>();
//        
//        foreach( MetricGenerator mg in metricGenerators)
//            this.metricGenerators.Add(mg);
//    }
//};

} // namespace
