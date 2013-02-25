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
        UUID targetAttID = attr.getUUID();
        Set<MeasurementSet> allSets = getAllMeasurementSets( mgenSet );
        Iterator<MeasurementSet> msIt = allSets.iterator();
        
        while ( msIt.hasNext() )
        {
          MeasurementSet ms = msIt.next();
          
          if ( ms.getAttributeUUID().equals(targetAttID) )
            mSets.put( ms.getUUID(), ms );
        }
      }
      
      return mSets;
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
          Set<Entity> mgEnts = mg.getEntities();
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
                        if ( ms.getUUID().equals( measurementSetID) )
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
          sortedM.put( m.getTimeStamp(), m );
        }
      }
      
      return sortedM;
    }
}
