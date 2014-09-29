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
using System.Collections.ObjectModel;
using System.Collections.Generic;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics
{

/**
 * A Helper class for assisting with getting information from the metric data model.
 */
public class MetricHelper
{
    /**
     * Retrieves the attribute identified by the ID if it exists in the metric generator collection.
     * 
     * @param attributeID - ID of the attribute required, must not be null.
     * @param mgenSet     - Collection of metric generators, must not be null
     * @return            - Returned attribute instance, if it exists, otherwise null.
     */
    public static Attribute getAttributeFromID( Guid attributeID,
                                                Collection<MetricGenerator> mgenSet )
    {
        Attribute attr = null;
      
        if ( attributeID != null && mgenSet != null )
        {
            Dictionary<Guid, Attribute> attributes = getAllAttributes(mgenSet);
            attr = attributes[ attributeID ];
        }
      
        return attr;
    }
    
    /**
     * Returns an entity identified by the ID, if it exists, from the metric generator collection.
     * 
     * @param entityID  - ID of the entity required, must not be null.
     * @param mgenSet   - Collection of metric generators, must not be null.
     * @return          - Returned entity instance, if it exists, otherwise null.
     */
    public static Entity getEntityFromID( Guid entityID,
                                          Collection<MetricGenerator> mgenSet )
    {
      Entity entity = null;
      
      if ( entityID != null && mgenSet != null )
      {
        Dictionary<Guid, Entity> entities = MetricHelper.getAllEntities( mgenSet );
        entity = entities[ entityID ];
      }
      
      return entity;
    }
  
    /**
     * Returns a collection of measurement sets (if any exist) associated with a specific attribute
     * from the metric generator collection.
     * 
     * @param attr    - Attribute of interest, must not be null.
     * @param mgenSet - Collection of metric generators to look for measurement sets, must not be null.
     * @return        - Return collection of measurement sets (will be empty if no measurement sets exist)
     */
    public static Dictionary<Guid, MeasurementSet> getMeasurementSetsForAttribute(Attribute attr,
                                                                                  Collection<MetricGenerator> mgenSet )
    {
        Dictionary<Guid, MeasurementSet> mSets = new Dictionary<Guid, MeasurementSet>();
      
        if ( attr != null && mgenSet != null )
        {
            Guid targetAttID                         = attr.uuid;
            Dictionary<Guid, MeasurementSet> allSets = getAllMeasurementSets(mgenSet);

            foreach (MeasurementSet ms in allSets.Values)
            {
                Guid linkedAttrID = ms.attributeID;

                if (linkedAttrID != null && linkedAttrID.Equals(targetAttID))
                    mSets.Add(ms.msetID, ms);
            }
        }
      
        return mSets;
    }
    
    /**
     * Returns a collection of measurement sets (if they exist) associated with a specific attribute
     * from a single metric generator.
     * 
     * @param attr - Attribute of interest, must not be null.
     * @param mgen - Metric generators in which to look for measurement sets, must not be null.
     * @return     - Return collection of measurement sets (will be empty if no measurement sets exist)
     */
    public static MeasurementSet getMeasurementSetForAttribute( Attribute attr,
                                                                MetricGenerator mgen )
    {
        MeasurementSet msTarget = null;
      
        if ( attr != null && mgen != null )
        {
            Dictionary<Guid, MeasurementSet> allSets = getAllMeasurementSets( mgen );

            foreach (MeasurementSet ms in allSets.Values)
            {
                Guid linkedAttrID = ms.attributeID;

                if (linkedAttrID != null && linkedAttrID.Equals(attr.uuid))
                {
                    msTarget = ms;
                    break;
                }
            }
            
        }
      
        return msTarget;
    }
    
    /**
     * Returns all the attributes associated with a collection of metric generators.
     * 
     * @param mgenSet - Metric generator collection in which to search, must not be null.
     * @return        - Return collection of attributes (will be empty if no attributes exist)
     */
    public static Dictionary<Guid, Attribute> getAllAttributes(Collection<MetricGenerator> mgenSet)
    {
        Dictionary<Guid, Attribute> attributes = new Dictionary<Guid, Attribute>();
        Dictionary<Guid, Entity>      entities = getAllEntities( mgenSet );

        foreach (Entity ent in entities.Values)
        {
            foreach (Attribute attr in ent.attributes)
                attributes.Add(attr.uuid, attr);
        }
      
        return attributes;
    }
  
    /**
     * Returns all entities associated with a collection of metric generators.
     * 
     * @param mgenSet - Metric generator collection in which to search, must not be null.
     * @return        - Return collection of entities (will be empty if no entities exist)
     */
    public static Dictionary<Guid, Entity> getAllEntities(Collection<MetricGenerator> mgenSet)
    {
        Dictionary<Guid, Entity> entities = new Dictionary<Guid, Entity>();
      
        if ( mgenSet != null )
        {
            foreach (MetricGenerator mg in mgenSet)
            {
                foreach (Entity ent in getAllEntities(mg).Values)
                    entities.Add(ent.uuid, ent);
            }
        }
        
        return entities;
    }
    
    /**
     * Returns all entities associated with a metric generator.
     * 
     * @param mgent - Metric generator in which to search, must not be null.
     * @return      - Return collection of entities (will be empty if no entities exist)
     */
    public static Dictionary<Guid, Entity> getAllEntities(MetricGenerator mgen)
    {
        Dictionary<Guid, Entity> entities = new Dictionary<Guid, Entity>();
      
        if ( mgen != null )
        {
            foreach (Entity ent in mgen.entities)
                entities.Add(ent.uuid, ent);
        }
      
        return entities;
    }
  
    /**
     * Returns a collection of all measurement sets associated with a collection of metric generators
     * 
     * @param mgenSet - Collection of metric generators in which to search, must not be null
     * @return        - Returned collection of measurement sets (will be empty if non exist in metric generator collection)
     */
    public static Dictionary<Guid, MeasurementSet> getAllMeasurementSets(Collection<MetricGenerator> mgenSet)
    {
        Dictionary<Guid, MeasurementSet> mSets = new Dictionary<Guid, MeasurementSet>();
        
        if ( mgenSet != null )
        {
            foreach (MetricGenerator mGen in mgenSet)
            {
                foreach (MetricGroup mGrp in mGen.metricGroups)
                {
                    // Check measurement set set is not null
                    if ( mGrp.measurementSets != null )
                        foreach (MeasurementSet ms in mGrp.measurementSets)
                            mSets.Add(ms.msetID, ms);
                }
            }
        }
        
        return mSets;        
    }
    
    /**
     * Returns a collection of all measurement sets associated with a metric generator
     * 
     * @param mgen - Metric generator in which to search, must not be null
     * @return     - Returned collection of measurement sets (will be empty if non exist in metric generator)
     */
    public static Dictionary<Guid, MeasurementSet> getAllMeasurementSets(MetricGenerator mgen)
    {
        Dictionary<Guid, MeasurementSet> mSets = new Dictionary<Guid, MeasurementSet>();
      
        if ( mgen != null )
        {
            foreach (MetricGroup mGrp in mgen.metricGroups)
            {
                foreach (MeasurementSet ms in mGrp.measurementSets)
                {
                    Guid msID = ms.msetID;

                    if (msID != null) mSets.Add(msID, ms);
                }
            }
        }
        
        return mSets;
    }
    
    /**
     * Returns the measurement set identified by the ID from a metric generator.
     * 
     * @param mgen              - Metric generator in which to search, must not be null.
     * @param measurementSetID  - MeasurementSet ID of measurement set to find
     * @return                  - Returned measurement set instance (null if it does not exist)
     */
    public static MeasurementSet getMeasurementSet( MetricGenerator mgen,
                                                    Guid measurementSetID )
    {
        MeasurementSet targetSet = null;

        if (mgen != null && measurementSetID != null)
        {
            HashSet<MetricGroup> mGroups = mgen.metricGroups;

            if (mGroups != null)
            {
                foreach (MetricGroup mGrp in mGroups)
                {
                    foreach (MeasurementSet ms in mGrp.measurementSets)
                    {
                        Guid msID = ms.msetID;

                        if (msID != null && msID.Equals(measurementSetID))
                        {
                            targetSet = ms;
                            break;
                        }
                    }

                    if (targetSet != null) break;
                }
            }
        }
      
        return targetSet;
    }
    
    /**
     * Returns the measurement set identified by the ID from a metric generator collection.
     * 
     * @param mgenSet           - Metric generator collection in which to search, must not be null.
     * @param measurementSetID  - MeasurementSet ID of measurement set to find
     * @return                  - Returned measurement set instance (null if it does not exist)
     */
    public static MeasurementSet getMeasurementSet( Collection<MetricGenerator> mgenSet,
                                                    Guid measurementSetID )
    {
        MeasurementSet targetSet = null;
      
        if ( mgenSet != null || measurementSetID != null )
        {
            foreach (MetricGenerator mg in mgenSet)
            {
                targetSet = getMeasurementSet(mg, measurementSetID);
                if (targetSet != null) break;
            }
        }
        
        return targetSet;        
    }
    
    /**
     * Sorts an unordered set of measurements by date.
     * 
     * @param measurements - Measurement set to sort - must not be null.
     * @return             - Returned sorted measurements (will be empty if input set empty)
     */
    public static SortedDictionary<DateTime, Measurement> sortMeasurementsByDate( Collection<Measurement> measurements )
    {
        SortedDictionary<DateTime, Measurement> sortedM = new SortedDictionary<DateTime, Measurement>();
      
        if ( measurements != null )
        {
            foreach (Measurement m in measurements)
            {
                DateTime dt = m.timeStamp;

                if ( dt != null ) sortedM.Add(dt, m);
            }
        }
      
      return sortedM;
    }
    
    /**
     * Creates an attribute with the parameters provided. If a valid entity is provided
     * the attribute and entity will be automatically associated.
     * 
     * @param name    - Name of the attribute to create.
     * @param desc    - Description of the attribute.
     * @param entity  - Entity that the attribute belongs to (can be null)
     * @return        - Attribute instance
     */
    public static Attribute createAttribute(string name, string desc, Entity entity)
    {
      Attribute attribute = new Attribute();
      attribute.name = name ;
      attribute.description = desc;
      
      if ( entity != null )
      {
        attribute.entityUUID = entity.uuid;
        entity.addAttribute( attribute );
      }
      
      return attribute;
    }
    
    /**
     * Searches for an attribute by its name.
     * 
     * @param name   - Name of the attribute to search, must not be null.
     * @param entity - Instance of the entity to query, must not be null.
     * @return       - Returned attribute (will be null if the attribute cannot be found)
     */
    public static Attribute getAttributeByName(string name, Entity entity)
    {
      Attribute targAttr = null;
      
      if ( name != null && entity != null )
      {
          HashSet<Attribute> attrs = entity.attributes;

          foreach (Attribute attr in attrs)
          {
              string attName = attr.name;

              if (attName != null && attName.Equals(name))
              {
                  targAttr = attr;
                  break;
              }
          }
      }
      
      return targAttr;      
    }
    
    /**
     * Creates a metric group with the given name and description. Will automatically
     * associate the group with the metric generator, if one is provided.
     * 
     * @param name - Name of the entity to create.
     * @param desc - Description of the entity.
     * @param mGen - Metric generator to associate the entity with (may be null)
     * @return     - Returns an instance of a MetricGroup.
     */
    public static MetricGroup createMetricGroup(string name, string desc, MetricGenerator mGen)
    {
      MetricGroup mGroup = new MetricGroup();
      mGroup.name = name;
      mGroup.description = desc;
      
      if ( mGen != null )
      {
        mGen.addMetricGroup( mGroup );
        mGroup.metricGeneratorUUID = mGen.uuid;
      }
      
      return mGroup;
    }
    
    /**
     * Creates a measurement set automatically linked to an attribute and metric group.
     * 
     * @param attr  - Attribute associated with the measurement set, must not be null.
     * @param type  - Metric type associated with the measurement set, must not be null.
     * @param unit  - Unit associated with the measurement set, must not be null.
     * @param group - Metric group that will contain the measurement set, must not be null.
     * @return      - Returns a new measurement set (null if parameters are invalid)
     */
    public static MeasurementSet createMeasurementSet( Attribute attr,
                                                       MetricType type,
                                                       Unit unit,
                                                       MetricGroup group )
    {
      MeasurementSet mSet = null;
      
      if ( attr != null && unit != null && group != null )
      {
        mSet = new MeasurementSet();
        Metric metric = new Metric();
        metric.metricType = type;
        metric.unit = unit;
        mSet.metric = metric;
        
        mSet.attributeID = attr.uuid;
        mSet.metricGroupID = group.uuid;
        
        group.addMeasurementSets( mSet );
      }
      
      return mSet;
    }
    
    /**
     * Searches for a metric generator by name.
     * 
     * @param name  - Name of the metric generator to find, must not be null.
     * @param mGens - Collection of metric generators in which to search, must not be null.
     * @return      - Returns the metric generator (null if it does not exist)
     */
    public static MetricGenerator getMetricGeneratorByName( String name, 
                                                            Collection<MetricGenerator> mGens )
    {
        MetricGenerator targetGen = null;

        if (mGens != null)
        {
            foreach (MetricGenerator mg in mGens)
            {
                string genName = mg.name;

                if (genName != null && genName.Equals(name))
                {
                    targetGen = mg;
                    break;
                }
            }
        }
        
        return targetGen;
    }
    
    /**
     * Searches for a metric group by name.
     * 
     * @param groupName - Name of the metric group to find.
     * @param mGroups   - Collection of metric groups in which to search.
     * @return          - Returned metric group, if it exists.
     */
    public static MetricGroup getMetricGroupByName( string groupName,
                                                    Collection<MetricGroup> mGroups )
    {
        MetricGroup targetGroup = null;

        if (groupName != null || mGroups != null)
        {
            foreach (MetricGroup mg in mGroups)
            {
                string gName = mg.name;

                if (gName != null && gName.Equals(groupName))
                {
                    targetGroup = mg;
                    break;
                }
            }
        }
      
        return targetGroup;
    }
    
    /**
     * Creates a pre-initialised Report containing a measurement set based on the
     * instance provided. Use this convenience method for quickly creating a report
     * to send to the ECC.
     * 
     * @param sourceMS  - The measurement set 'template' upon which the report will be based, must not be null.
     * @return          - A new report instance (with new measurement set instance) that can be populated with measurements for the ECC.
     */
    public static Report createEmptyMeasurementReport( MeasurementSet sourceMS )
    {
      Report targetReport = null;
     
      if ( sourceMS != null )
      {
        MeasurementSet emptyMS = new MeasurementSet( sourceMS, false );
        DateTime now = DateTime.Now;
        
        targetReport = new Report( Guid.NewGuid(), emptyMS, now, now, now );
      }
      
      return targetReport;      
    }
    
    /**
     * Use this method to iterate through the contents of a metric generator.
     * 
     * @param mgen - Metric generator to describe - must not be null.
     * @return     - String describing the metric generator.
     */
    public static string describeGenerator(MetricGenerator mgen)
    {
        string desc = "Could not describe generator (null)";

        if (mgen != null)
        {
            desc = mgen.name + "\n";
            desc += "Description: " + mgen.description + "\n";

            Dictionary<Guid, Entity> mGenEntities = MetricHelper.getAllEntities(mgen);
            if (mGenEntities.Count > 0)
            {
                desc += "Generator has " + mGenEntities.Count + " entities" + "\n";

                foreach (Entity ent in mGenEntities.Values)
                {
                    desc += "  Entity: " + ent.name + "\n";
                    desc += "  Description: " + ent.description + "\n";

                    HashSet<Attribute> attributes = ent.attributes;
                    if (attributes.Count > 0)
                    {
                        desc += "  Number of attributes: " + attributes.Count + "\n";

                        foreach (Attribute attr in ent.attributes)
                        {
                            desc += "    Attribute: " + attr.name + "\n";
                            desc += "    Description: " + attr.description + "\n";

                            MeasurementSet ms = getMeasurementSetForAttribute(attr, mgen);
                            if (ms != null)
                            {
                                desc += "      Measurement set ID: " + ms.msetID + "\n";

                                Metric metric = ms.metric;
                                if (metric != null)
                                {
                                    desc += "      Metric unit: " + metric.unit.toString() + "\n";
                                    desc += "      Metric type: " + metric.metricType.ToString() + "\n";
                                }
                                else desc += "      Measurement set has no metric.";
                            }
                            else desc += "    Attribute has no measurement set associated with it.\n";
                        }
                    }
                    else desc += "  Entity has no attributes.\n";
                }

                desc += "\n";
            }
        }

        return desc;
    }
}
    
} // namespace