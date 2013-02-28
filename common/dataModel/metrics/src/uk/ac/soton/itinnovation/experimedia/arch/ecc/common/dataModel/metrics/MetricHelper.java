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
//      Created Date :          26-Oct-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics;

import java.util.*;



/**
 * A Helper class for assisting with getting information from the metric data model.
 * @author Simon Crowle
 */
public class MetricHelper
{
    public static Attribute getAttributeFromID( UUID attributeID,
                                                Collection<MetricGenerator> mgenSet )
    {
      Attribute attr = null;
      
      if ( attributeID != null && mgenSet != null )
      {
        Map<UUID, Attribute> attributes = getAllAttributes( mgenSet );
        attr = attributes.get( attributeID );
      }
      
      return attr;
    }
    
    public static Entity getEntityFromID( UUID entityID,
                                          Collection<MetricGenerator> mgenSet )
    {
      Entity entity = null;
      
      if ( entityID != null && mgenSet != null )
      {
        Map<UUID, Entity> entities = MetricHelper.getAllEntities( mgenSet );
        entity = entities.get( entityID );
      }
      
      return entity;
    }
  
    public static Map<UUID, MeasurementSet> getMeasurementSetsForAttribute( Attribute attr,
                                                                            Collection<MetricGenerator> mgenSet )
    {
      HashMap<UUID, MeasurementSet> mSets = new HashMap<UUID, MeasurementSet>();
      
      if ( attr != null && mgenSet != null )
      {
        UUID targetAttID              = attr.getUUID();
        Set<MeasurementSet> allSets   = getAllMeasurementSets( mgenSet );
        Iterator<MeasurementSet> msIt = allSets.iterator();
        
        while ( msIt.hasNext() )
        {
          MeasurementSet ms = msIt.next();
          UUID linkedAttrID = ms.getAttributeUUID();
          
          if ( linkedAttrID != null && linkedAttrID.equals(targetAttID) )
            mSets.put( ms.getUUID(), ms );
        }
      }
      
      return mSets;
    }
    
    public static MeasurementSet getMeasurementSetForAttribute( Attribute attr,
                                                                MetricGenerator mgen )
    {
      MeasurementSet msTarget = null;
      
      if ( attr != null && mgen != null )
      {
        Map<UUID, MeasurementSet> allSets = getAllMeasurementSets( mgen );
        Iterator<MeasurementSet> msIt = allSets.values().iterator();
        
        while ( msIt.hasNext() )
        {
          MeasurementSet ms = msIt.next();
          UUID linkedAttrID = ms.getAttributeUUID();
          
          if ( linkedAttrID != null && linkedAttrID.equals( attr.getUUID()) )
          {
            msTarget = ms;
            break;
          }
        }
      }
      
      return msTarget;
    }
    
    public static Map<UUID, Attribute> getAllAttributes( Collection<MetricGenerator> mgenSet )
    {
      HashMap<UUID, Attribute> attributes = new HashMap<UUID, Attribute>();
      
      Map<UUID, Entity> entities = getAllEntities( mgenSet );
      Iterator<Entity> entIt = entities.values().iterator();
      
      while ( entIt.hasNext() )
      {
        Iterator<Attribute> attIt = entIt.next().getAttributes().iterator();
        while ( attIt.hasNext() )
        {
          Attribute attr = attIt.next();
          attributes.put( attr.getUUID(), attr );
        }
      }
      
      return attributes;
    }
  
    public static Map<UUID, Entity> getAllEntities( Collection<MetricGenerator> mgenSet )
    {
      HashMap<UUID, Entity> entities = new HashMap<UUID, Entity>();
      
      if ( mgenSet != null )
      {
        Iterator<MetricGenerator> mgenIt = mgenSet.iterator();
        while ( mgenIt.hasNext() )
        {
          MetricGenerator mg = mgenIt.next();
          
          Map<UUID, Entity> mgEnts = getAllEntities( mg );
          entities.putAll( mgEnts );
        }
      }
      
      return entities;
    }
    
    public static Map<UUID, Entity> getAllEntities( MetricGenerator mgen )
    {
      HashMap<UUID, Entity> entities = new HashMap<UUID, Entity>();
      
      if ( mgen != null )
      {
        Set<Entity> mgEnts = mgen.getEntities();
        if ( mgEnts != null )
        {
          Iterator<Entity> mgEntIt = mgEnts.iterator();
          while ( mgEntIt.hasNext() )
          {
            Entity entity = mgEntIt.next();
            entities.put( entity.getUUID(), entity );
          }
        }
      }
      
      return entities;
    }
  
    public static Set<MeasurementSet> getAllMeasurementSets( Collection<MetricGenerator> mgenSet )
    {
        HashSet<MeasurementSet> mSets = new HashSet<MeasurementSet>();
        
        if ( mgenSet != null )
        {
            Iterator<MetricGenerator> mgenIt = mgenSet.iterator();
            while ( mgenIt.hasNext() )
            {
                Iterator<MetricGroup> mgIt = mgenIt.next().getMetricGroups().iterator();
                while ( mgIt.hasNext() )
                {
                    Iterator<MeasurementSet> msIt = mgIt.next().getMeasurementSets().iterator();
                    while ( msIt.hasNext() )
                    { mSets.add( msIt.next() ); }
                }
            }
        }
        
        return mSets;        
    }
    
    public static Map<UUID, MeasurementSet> getAllMeasurementSets( MetricGenerator mgen )
    {
      HashMap<UUID, MeasurementSet> mSets = new HashMap<UUID, MeasurementSet>();
      
      if ( mgen != null )
      {
        Iterator<MetricGroup> groupIt = mgen.getMetricGroups().iterator();
        while ( groupIt.hasNext() )
        {
          Iterator<MeasurementSet> msIt = groupIt.next().getMeasurementSets().iterator();
          while ( msIt.hasNext() )
          {
            MeasurementSet ms = msIt.next();
            UUID msID = ms.getUUID();
            
            if ( msID != null ) mSets.put( ms.getUUID(), ms );
          }
        }
      }
      
      return mSets;
    }
    
    public static MeasurementSet getMeasurementSet( Collection<MetricGenerator> mgenSet,
                                                    UUID measurementSetID )
    {
        MeasurementSet targetSet = null;
      
        if ( mgenSet != null || measurementSetID != null )
        {
            Iterator<MetricGenerator> mgenIt = mgenSet.iterator();
            while ( mgenIt.hasNext() )
            {
                Iterator<MetricGroup> mgIt = mgenIt.next().getMetricGroups().iterator();
                while ( mgIt.hasNext() )
                {
                    Iterator<MeasurementSet> msIt = mgIt.next().getMeasurementSets().iterator();
                    while ( msIt.hasNext() )
                    { 
                        MeasurementSet ms = msIt.next();
                        UUID msID = ms.getUUID();
                        
                        if ( msID != null &&  msID.equals( measurementSetID) )
                        {
                            targetSet = ms;
                            break;
                        }
                    }
                }
            }
        }
        
        return targetSet;        
    }
    
    public static TreeMap<Date, Measurement> sortMeasurementsByDate( Set<Measurement> measurements )
    {
      TreeMap<Date, Measurement> sortedM = new TreeMap<Date, Measurement>();
      
      if ( measurements != null )
      {
        Iterator<Measurement> mIt = measurements.iterator();
        while ( mIt.hasNext() )
        {
          Measurement m = mIt.next();
          Date stamp = m.getTimeStamp();
          
          if ( stamp != null ) sortedM.put( stamp, m );
        }
      }
      
      return sortedM;
    }
    
    public static Attribute createAttribute( String name, String desc, Entity entity )
    {
      Attribute attribute = new Attribute();
      attribute.setName( name );
      attribute.setDescription( desc );
      
      if ( entity != null )
      {
        attribute.setEntityUUID( entity.getUUID() );
        entity.addAttribute( attribute );
      }
      
      return attribute;
    }
    
    public static Attribute getAttributeByName( String name, Entity entity )
    {
      Attribute targAttr = null;
      
      if ( name != null && entity != null )
      {
        Set<Attribute> attrs = entity.getAttributes();
        if ( attrs != null )
        {
          Iterator<Attribute> attIt = attrs.iterator();
          while ( attIt.hasNext() )
          {
            Attribute attr = attIt.next();
            String attName = attr.getName();
            
            if ( attName != null && attName.equals(name) )
            {
              targAttr = attr;
              break;
            }
          }
        }
      }
      
      return targAttr;      
    }
    
    public static MetricGroup createMetricGroup( String name, String desc, MetricGenerator mGen )
    {
      MetricGroup mGroup = new MetricGroup();
      mGroup.setName( name );
      mGroup.setDescription( desc );
      
      if ( mGen != null ) mGen.addMetricGroup( mGroup );
      
      return mGroup;
    }
    
    public static MeasurementSet createMeasurementSet( Attribute attr,
                                                       MetricType type,
                                                       Unit unit,
                                                       MetricGroup group )
    {
      MeasurementSet mSet = null;
      
      if ( attr != null && type != null && unit != null && group != null )
      {
        mSet = new MeasurementSet();
        Metric metric = new Metric();
        metric.setMetricType( type );
        metric.setUnit( unit );
        mSet.setMetric( metric );
        
        mSet.setAttributeUUID( attr.getUUID() );
        mSet.setMetricGroupUUID( attr.getEntityUUID() );
        
        group.addMeasurementSets( mSet );
      }
      
      return mSet;
    }
    
    public static MetricGenerator getMetricGeneratorByName( String name, 
                                                            Set<MetricGenerator> mGens )
    {
      MetricGenerator targetGen = null;
      
      if ( mGens != null )
      {
        Iterator<MetricGenerator> genIt = mGens.iterator();
        while ( genIt.hasNext() )
        {
          MetricGenerator mgen = genIt.next();
          String genName = mgen.getName();
          
          if ( genName != null && genName.equals(name) )
          {
            targetGen = mgen;
            break;
          }
        }
      }
      
      return targetGen;
    }
    
    public static MetricGroup getMetricGroupByName( String groupName,
                                                    Set<MetricGroup> mGroups )
    {
      MetricGroup targetGroup = null;
      
      if ( groupName != null || mGroups != null )
      {
        Iterator<MetricGroup> groupIt = mGroups.iterator();
        while ( groupIt.hasNext() )
        {
          MetricGroup group = groupIt.next();
          String gName      = group.getName();
          
          if ( gName != null && gName.equals(groupName) )
          {
            targetGroup = group;
            break;
          }
        }
      }
      
      return targetGroup;
    } 
    
    public static String describeGenerator( MetricGenerator mgen )
    {
      String desc = "Could not describe generator (null)";

      if ( mgen != null )
      {
        desc = mgen.getName() + "\n";
        desc += "Description: " + mgen.getDescription() + "\n";

        Collection<Entity> mGenEntities = MetricHelper.getAllEntities(mgen).values();
        if ( !mGenEntities.isEmpty() )
        {
          desc += "Generator has " + mGenEntities.size() + " entities" + "\n";

          Iterator<Entity> entIt = mGenEntities.iterator();
          while ( entIt.hasNext() )
          {
            Entity entity = entIt.next();
            desc += "  Entity: " + entity.getName() + "\n";
            desc += "  Description: " + entity.getDescription() + "\n";

            Set<Attribute> attributes = entity.getAttributes();
            if ( !attributes.isEmpty() )
            {
              desc += "  Number of attributes: " + attributes.size() + "\n";
              Iterator<Attribute> attIt = attributes.iterator();
              while ( attIt.hasNext() )
              {
                Attribute att = attIt.next();
                desc += "    Attribute: " + att.getName() + "\n";
                desc += "    Description: " + att.getDescription() + "\n";
                
                MeasurementSet ms = getMeasurementSetForAttribute( att, mgen );
                if ( ms != null )
                {
                  desc += "      Measurement set ID: " + ms.getUUID() + "\n";
                  
                  Metric metric = ms.getMetric();
                  if ( metric != null )
                  {
                    desc += "      Metric unit: " + metric.getUnit().toString() + "\n";
                    desc += "      Metric type: " + metric.getMetricType().toString() + "\n";
                  }
                  else desc += "      Measurement set has no metric.";
                }
                else desc += "    Attribute has no measurement set associated with it.\n";
              }
            }
            else desc += "  Entity has no attributes.\n";
          }
        }
        else desc += "Generator has no entities associated with it.\n";
        
        desc += "\n";
      }
      
      return desc;
    } 
}
