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
    public static Attribute getAttributeFromGenerators( UUID attributeID,
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
    
    /**
     * Use this method to find an attribute from a collection of entities.
     * 
     * @param attrID    - Non-null attribute ID
     * @param entities  - non-null collection of entities
     * @return          - A (possibly null) attribute
     */
    public static Attribute getAttributeFromEntities( UUID attrID, 
                                                      Collection<Entity> entities )
    {
        Attribute result = null;
        
        if ( entities != null && !entities.isEmpty() )
        {
            for( Entity ent : entities )
                for ( Attribute attr : ent.getAttributes() )
                    if ( attr.getUUID().equals(attrID) )
                    {
                        result = attr;
                        break;
                    }
        }
        
        return result;
    }
    
    /**
     * Returns an entity identified by its ID (String, not UUID), if it exists, from the metric generator.
     * 
     * @param entityID - ID of entity (not UUID) - must not be null
     * @param mgenSet  - Metric generator in which to search
     * @return         - Returned entity instance, if it exists, otherwise null
     */
    public static Entity getEntityFromID ( String entityID,
                                           MetricGenerator mgenSet )
    {
      Entity entity = null;
      
      if ( entityID != null && mgenSet != null )
      {
        Map<UUID, Entity> entities = MetricHelper.getAllEntities( mgenSet );
        
        for ( Entity ent : entities.values() )
        {
          if ( ent.getEntityID().equals(entityID) )
          {
            entity = ent;
            break;
          }
        }
      }
      
      return entity;
    }
    
    /**
     * Returns an entity identified by its UNIQUE ID, if it exists, from the metric generator collection.
     * 
     * @param entityID  - ID of the entity required, must not be null.
     * @param mgenSet   - Collection of metric generators, must not be null.
     * @return          - Returned entity instance, if it exists, otherwise null.
     */
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
    
    /**
     * Returns an entity identified by the name, if it exists, from the metric generator
     * 
     * @param name    - Name of the entity required, must not be null
     * @param mgenSet - Collection of metric generators, must not be null
     * @return        - Returned entity instance, if it exists, otherwise null
     */
    public static Entity getEntityFromName( String name,
                                            MetricGenerator mgenSet )
    {
        Entity entity = null;

        if ( name != null && mgenSet != null )
        {
            for ( Entity ent : mgenSet.getEntities() )
              if ( ent.getName().equals(name) )
              {
                  entity = ent;
                  break;
              }
        }

        return entity;
    }
    
    /**
     * Returns an entity identified by the name, if it exists, from the metric generator collection.
     * 
     * @param name      - Name of the entity required, must not be null.
     * @param mgenSet   - Collection of metric generators, must not be null.
     * @return          - Returned entity instance, if it exists, otherwise null.
     */
    public static Entity getEntityFromName( String name,
                                            Collection<MetricGenerator> mgenSet )
    {
       Entity entity = null;
       
       if (name != null && mgenSet != null)
       {
           Iterator<MetricGenerator> mgenIt = mgenSet.iterator();
           
           while ( mgenIt.hasNext() && entity == null )
           {
               MetricGenerator mg = mgenIt.next();
               Set<Entity> mgEnts = mg.getEntities();
               
               Iterator<Entity> entIt = mgEnts.iterator();
               
               while ( entIt.hasNext() && entity == null )
               {
                   Entity e = entIt.next();
                   
                   if( e.getName().equals(name) )
                   {
                       entity = e;
                   }
               }
           }
       }
       
       return entity;
    }

    /**
     * Returns all the attributes associated with a collection of metric generators.
     * 
     * @param mgenSet - Metric generator collection in which to search, must not be null.
     * @return        - Return collection of attributes (will be empty if no attributes exist)
     */
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
  
    /**
     * Returns all entities associated with a collection of metric generators.
     * 
     * @param mgenSet - Metric generator collection in which to search, must not be null.
     * @return        - Return collection of entities (will be empty if no entities exist)
     */
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
    
    /**
     * Returns all entities associated with a metric generator.
     * 
     * @param mgent - Metric generator in which to search, must not be null.
     * @return      - Return collection of entities (will be empty if no entities exist)
     */
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
  
    /**
     * Returns a collection of all measurement sets associated with a collection of metric generators
     * 
     * @param mgenSet - Collection of metric generators in which to search, must not be null
     * @return        - Returned collection of measurement sets (will be empty if non exist in metric generator collection)
     */
    public static Map<UUID, MeasurementSet> getAllMeasurementSets( Collection<MetricGenerator> mgenSet )
    {
        HashMap<UUID, MeasurementSet> mSets = new HashMap<>();
        
        if ( mgenSet != null )
        {
            for ( MetricGenerator metGen : mgenSet )
                for ( MetricGroup metGrp : metGen.getMetricGroups() )
                {
                    // Make sure measurement set set is not null
                    Set<MeasurementSet> msSet = metGrp.getMeasurementSets();
                    
                    if ( msSet != null )
                        for ( MeasurementSet ms : msSet)
                            mSets.put( ms.getID(), ms );
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
            UUID msID = ms.getID();
            
            if ( msID != null ) mSets.put( ms.getID(), ms );
          }
        }
      }
      
      return mSets;
    }
    
    /**
     * Returns a map of all measurement sets associated with the entity identified by UUID.
     * 
     * @param mGens     - Set of metric generators to look in for entity & measurement sets
     * @param entityID  - Identity of the entity
     * @return          - Map of the measurement sets found (will be empty if none found)
     */
    public static Map<UUID, MeasurementSet> getMeasurementSetsForEntity( UUID entityID,
                                                                         Collection<MetricGenerator> mgenSet )
    {
      HashMap<UUID, MeasurementSet> targetMSets = new HashMap<UUID, MeasurementSet>();
      
      if ( !mgenSet.isEmpty() && entityID != null )
      {
        Entity entity = getEntityFromID( entityID, mgenSet );
        
        // Create a set of required measurement set IDs
        HashSet<UUID>       attrMSIDs = new HashSet<UUID>();
        Iterator<Attribute> attIt     = entity.getAttributes().iterator();
        while ( attIt.hasNext() )
        {
          Attribute attr = attIt.next();
          attrMSIDs.add( attr.getUUID() );
        }
        
        // Pull out all measurement sets associated with attributes of entity
        Map<UUID, MeasurementSet> allSets = getAllMeasurementSets( mgenSet );
        Iterator<MeasurementSet>  msIt    = allSets.values().iterator();
        while ( msIt.hasNext() )
        {
          MeasurementSet ms = msIt.next();
          
          if ( attrMSIDs.contains(ms.getAttributeID()) )
            targetMSets.put( ms.getID(), ms );
        }
      }
      
      return targetMSets;
    }
    
    /**
     * Returns a collection of measurement sets (if any exist) associated with a specific attribute
     * from the metric generator collection.
     * 
     * @param attr    - Attribute of interest, must not be null.
     * @param mgenSet - Collection of metric generators to look for measurement sets, must not be null.
     * @return        - Return collection of measurement sets (will be empty if no measurement sets exist)
     */
    public static Map<UUID, MeasurementSet> getMeasurementSetsForAttribute( Attribute attr,
                                                                            Collection<MetricGenerator> mgenSet )
    {
      HashMap<UUID, MeasurementSet> mSets = new HashMap<UUID, MeasurementSet>();
      
      if ( attr != null && mgenSet != null )
      {
        UUID targetAttID                  = attr.getUUID();
        Map<UUID, MeasurementSet> allSets = getAllMeasurementSets( mgenSet );
        Iterator<MeasurementSet> msIt     = allSets.values().iterator();
        
        while ( msIt.hasNext() )
        {
          MeasurementSet ms = msIt.next();
          UUID linkedAttrID = ms.getAttributeID();
          
          if ( linkedAttrID != null && linkedAttrID.equals(targetAttID) )
            mSets.put( ms.getID(), ms );
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
        Map<UUID, MeasurementSet> allSets = getAllMeasurementSets( mgen );
        Iterator<MeasurementSet> msIt = allSets.values().iterator();
        
        while ( msIt.hasNext() )
        {
          MeasurementSet ms = msIt.next();
          UUID linkedAttrID = ms.getAttributeID();
          
          if ( linkedAttrID != null && linkedAttrID.equals( attr.getUUID()) )
          {
            msTarget = ms;
            break;
          }
        }
      }
      
      return msTarget;
    }
    
    /**
     * Returns the measurement set identified by the ID from a metric generator.
     * 
     * @param mgen              - Metric generator in which to search, must not be null.
     * @param measurementSetID  - MeasurementSet ID of measurement set to find
     * @return                  - Returned measurement set instance (null if it does not exist)
     */
    public static MeasurementSet getMeasurementSet( MetricGenerator mgen,
                                                    UUID measurementSetID )
    {
      MeasurementSet targetSet = null;
      
      if ( mgen != null && measurementSetID != null )
      {
        Set<MetricGroup> mGroup = mgen.getMetricGroups();
        
        if ( mGroup != null )
        {
           Iterator<MetricGroup> mgIt = mGroup.iterator();
            while ( mgIt.hasNext() )
            {
              Set<MeasurementSet> msets = mgIt.next().getMeasurementSets();
              if ( msets != null )
              {
                Iterator<MeasurementSet> msIt = msets.iterator();
                while ( msIt.hasNext() )
                { 
                    MeasurementSet ms = msIt.next();
                    UUID msID = ms.getID();

                    if ( msID != null &&  msID.equals( measurementSetID) )
                    {
                        targetSet = ms;
                        break;
                    }
                }
              }
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
                                                    UUID measurementSetID )
    {
        MeasurementSet targetSet = null;
      
        if ( mgenSet != null || measurementSetID != null )
        {
            Iterator<MetricGenerator> mgenIt = mgenSet.iterator();
            while ( mgenIt.hasNext() )
            {
                targetSet = getMeasurementSet( mgenIt.next(), measurementSetID );
                if ( targetSet != null ) break;
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
    public static TreeMap<Date, Measurement> sortMeasurementsByDate( Set<Measurement> measurements )
    {
      TreeMap<Date, Measurement> sortedM = new TreeMap<Date, Measurement>();
      
      if ( measurements != null )
      {
        for ( Measurement m : measurements )
        {
            Date stamp = m.getTimeStamp();
            
            if ( stamp != null ) sortedM.put( stamp, m );
        }
      }
      
      return sortedM;
    }
    
    public static List<Measurement> sortMeasurementsByDateLinear( Set<Measurement> measurements )
    {
        ArrayList<Measurement> resultMeasurementList = new ArrayList<Measurement>();
        
        if ( measurements != null )
        {
            // ..sort them
            TreeMap <Date, Measurement> sortedMeasures = sortMeasurementsByDate(measurements);
            
            // Push into linear list (newest first)
            for ( Date d : sortedMeasures.descendingKeySet() ) 
                resultMeasurementList.add( sortedMeasures.get(d) );   
        }
        
        return resultMeasurementList;        
    }
    
    public static List<Attribute> sortAttributesByName( Collection<Attribute> attributes )
    {
        ArrayList<Attribute> result = new ArrayList<>();
        
        TreeMap<String, ArrayList<Attribute>> sortedAttrs = new TreeMap<>();
        
        if ( attributes != null && !attributes.isEmpty() )
        {
            // Sort (allowing attributes with the same name)
            for ( Attribute attr: attributes )
            {
                String attrName = attr.getName();
                
                if ( sortedAttrs.containsKey(attrName) )
                {
                    ArrayList<Attribute> bucket = sortedAttrs.get( attrName );
                    bucket.add( attr );
                }
                else
                {
                    ArrayList<Attribute> bucket = new ArrayList<>();
                    bucket.add( attr );
                    
                    sortedAttrs.put( attrName, bucket );
                }
            }
            
            // Write out all attributes (ascending order)
            for ( String attrName : sortedAttrs.keySet() )
            {
                ArrayList<Attribute> bucket = sortedAttrs.get( attrName );
                for ( Attribute attr : bucket )
                    result.add( attr );
            }
        }
        
        return result;
    }
    
    public static List<Measurement> truncateMeasurements( List<Measurement> measures,
                                                          int count,
                                                          boolean tail )        
    {
        List<Measurement> truncMeasures = null; // Assigned as a sub-list...
        
        if ( count > 0 )
        {
            if ( tail )
            {
                int maxIndex   = measures.size();
                int startIndex = maxIndex - count;
                
                if ( startIndex < 0 )
                    startIndex = 0;
                
                truncMeasures = measures.subList( startIndex, maxIndex );
            }
            else 
            {
                if ( count > measures.size() ) count = measures.size();
                
                truncMeasures = measures.subList( 0, count );
            }
        }
        else
            truncMeasures = measures; //... or just reassigned as the input
        
        return truncMeasures;
    }
    
    public static Set<Measurement> stripMeasurementsInRange( Set<Measurement> measures,
                                                             Date start,
                                                             Date end )
    {
        HashSet<Measurement> strippedMeasures = new HashSet<Measurement>();
        
        if ( measures != null && start != null && end != null )
        {
            // Make sure we're operating in a sensible range
            if ( end.after(start) || end.equals(start) )
            {
                for ( Measurement m : measures )
                {
                    Date mDate = m.getTimeStamp();
                    
                    if ( mDate.equals(start) || mDate.after(start) )
                        if ( mDate.before(end) || mDate.equals(end) )
                            strippedMeasures.add( m );
                }
            }
        }
        
        return strippedMeasures;
    }
    
    public static Measurement getMiddleMeasurement( List<Measurement> measurements )
    {
        Measurement target = null;
        
        if ( measurements != null && !measurements.isEmpty() )
        {
            if ( measurements.size() < 2 )
                target = measurements.get(0);
            else
                target = measurements.get( (measurements.size() / 2) -1 );
        }
        
        return target;
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
    
    /**
     * Searches for an attribute by its name.
     * 
     * @param name   - Name of the attribute to search, must not be null.
     * @param entity - Instance of the entity to query, must not be null.
     * @return       - Returned attribute (will be null if the attribute cannot be found)
     */
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
    
    public static Attribute getAttributeByID( UUID id, Entity entity )
    {
        Attribute targAttr = null;
        
        if ( id != null && entity != null )
            for ( Attribute attr : entity.getAttributes() )
                if ( attr.getUUID().equals(id) )
                {
                    targAttr = attr;
                    break;
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
    public static MetricGroup createMetricGroup( String name, String desc, MetricGenerator mGen )
    {
      MetricGroup mGroup = new MetricGroup();
      mGroup.setName( name );
      mGroup.setDescription( desc );
      
      if ( mGen != null )
      {
        mGen.addMetricGroup( mGroup );
        mGroup.setMetricGeneratorUUID( mGen.getUUID() );
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
      
      if ( attr != null && type != null && unit != null && group != null )
      {
        mSet = new MeasurementSet();
        Metric metric = new Metric();
        metric.setMetricType( type );
        metric.setUnit( unit );
        mSet.setMetric( metric );
        
        mSet.setAttributeUUID( attr.getUUID() );
        mSet.setMetricGroupUUID( group.getUUID() );
        
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
    
    /**
     * Searches for a metric group by name.
     * 
     * @param groupName - Name of the metric group to find.
     * @param mGroups   - Collection of metric groups in which to search.
     * @return          - Returned metric group, if it exists.
     */
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
        Date now = new Date();
        
        targetReport = new Report( UUID.randomUUID(), emptyMS, now, now, now );
      }
      
      return targetReport;      
    }
    
    public static Report createMeasurementReport( MeasurementSet setWithMeasurements,
                                                  Date fromDate, Date toDate )
    {
        Report targetReport = null;
        
        if ( setWithMeasurements != null && fromDate != null && toDate != null )
        {
            int mCount = setWithMeasurements.getMeasurements().size();
            
            if ( mCount>  0 )
            {
               Date now = new Date();
               targetReport = new Report( UUID.randomUUID(), 
                                          setWithMeasurements, 
                                          now, fromDate, toDate );
               
               
               targetReport.setNumberOfMeasurements( mCount );
            }
        }
        
        return targetReport;
    }
    
    public static Report createMeasurementReport( MeasurementSet sourceMS,
                                                  Measurement newMeasurement )
    {
      Report targetReport = null;
      
      if ( sourceMS != null && newMeasurement != null )
      {
        MeasurementSet newMS = new MeasurementSet( sourceMS, false );
        Date now = new Date();
        
        newMS.addMeasurement( newMeasurement );
        targetReport = new Report( UUID.randomUUID(), newMS, now, now, now );
        targetReport.setNumberOfMeasurements( 1 );
      }
      
      return targetReport;
    }
    
    public static Report createMeasurementReport( MeasurementSet sourceMS,
                                                  Collection<Measurement> measures )
    {
        Report targetReport = null;
      
        if ( sourceMS != null && measures != null )
        {
          MeasurementSet newMS = new MeasurementSet( sourceMS, false );
          Date now = new Date();

          // Take only non-null measurements
          HashSet<Measurement> validMeasures = new HashSet<Measurement>();
          for ( Measurement m : measures )
              if ( m != null )
                  validMeasures.add( m );
          
          newMS.addMeasurements( validMeasures );
          
          targetReport = new Report( UUID.randomUUID(), newMS, now, now, now );
          targetReport.setNumberOfMeasurements( validMeasures.size() );
        }

        return targetReport;
    }
    
    /**
     * Use this method to iterate through the contents of a metric generator.
     * 
     * @param mgen - Metric generator to describe - must not be null.
     * @return     - String describing the metric generator.
     */
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
                  desc += "      Measurement set ID: " + ms.getID() + "\n";
                  
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
    
    /**
     * Use this method to check the consistency of a collection of measurement sets.
     * 
     * @param mSets - Non-null set of MeasuementSets
     * @return      - Returns true if inputs are valid and consistent
     */
    public static boolean areMeasurementSetsConsistent( Collection<MeasurementSet> mSets )
    {
        if ( mSets == null ) return false;
        
        Iterator<MeasurementSet> msIt = mSets.iterator();
        
        MeasurementSet firstMS = msIt.next();
        if ( firstMS == null ) return false;
        
        Metric keyMetric = firstMS.getMetric();
        if ( keyMetric == null ) return false;
        
        // Run through remaining sets checking for consistency
        while ( msIt.hasNext() )
        {
            MeasurementSet nextMS = msIt.next();
            if ( nextMS == null ) return false;
            
            Metric nextMetric = nextMS.getMetric();
            if ( nextMetric == null ) return false;
            
            if ( nextMetric.getMetricType().compareTo(keyMetric.getMetricType()) != 0 ) return false;
            
            if ( !nextMetric.getUnit().getName().equals(keyMetric.getUnit().getName()) ) return false;
            
            if ( !nextMetric.getMetaType().equals(keyMetric.getMetaType()) ) return false;
            
            if ( !nextMetric.getMetaContent().equals(keyMetric.getMetaContent()) ) return false;
        }
        
        return true;        
    }
    
    /**
     * Use this method to combine a number of MeasuementSet instances. This method will
     * iterate through the sets checking that they are all consistent before then combining
     * the measurements found therein. Duplicated measurement instances will be discarded but
     * measurements with the same date stamp will not.
     * 
     * @param mSets         - Non-null collection of measurement sets
     * @return              - Returns a joined set of measurements
     * @throws Exception    - Throws if input parameter is invalid or measurement sets are inconsistent
     */
    public static MeasurementSet combineMeasurementSets( Collection<MeasurementSet> mSets ) throws Exception
    {
        MeasurementSet result = null;
        
        // Safety
        if ( mSets == null )     throw new Exception( "Could not combined measurement sets: invalid input" );
        if ( mSets.isEmpty() )   throw new Exception( "Could not combine measurement sets: zero sets" );
        if ( mSets.size() == 1 ) return mSets.iterator().next();
        
        // Combine if measurement sets are consistent (semantically)
        if ( areMeasurementSetsConsistent(mSets) )
        {
            // First create single super measurement collection
            HashMap<UUID, Measurement> allMeasurements = new HashMap<>();
            
            for ( MeasurementSet ms : mSets )
                for ( Measurement m : ms.getMeasurements() )
                    allMeasurements.put( m.getUUID(), m );
            
            // Then create (empty) result set
            result = new MeasurementSet( mSets.iterator().next(), false );
            
            // Push in super measurement set
            result.addMeasurements( allMeasurements.values() );
        }
        else throw new Exception( "Could not combine measurement sets: sets inconsistent" );
        
        return result;        
    }
    
    public static int getORDINALIndexFromLabel( Metric metric, String label )
    {
        int result = -1;
        
        if ( metric != null && metric.getMetricType() == MetricType.NOMINAL && 
             metric.getMetaContent() != null && label != null )
        {
            String[] items = metric.getMetaContent().split( "," );
            
            int index = 0;
            for ( String item : items )
            {
                if ( item.equals(label) )
                {
                    result = index;
                    break;
                }
                ++index;
            }
        }
        
        return result;
    }
    
    public static String getORDINALLabelFromIndex( Metric metric, float position )
    {
        String result = null;
        
        // Safety before we get the position
        if ( metric != null && metric.getMetricType() == MetricType.ORDINAL &&
             position != Float.NaN && position >= 0.0f )
        {
            String[] items = metric.getMetaContent().split( "," );
            int intPos     = (int) position;
            
            if ( items.length >= intPos )
                result = items[intPos];
        }
        
        return result;        
    }
    
    public static ArrayList<String> getORDINALLabels( Metric metric )
    {
        ArrayList<String> result = new ArrayList<>();
        
        // Safety
        if ( metric != null && metric.getMetricType() == MetricType.ORDINAL )
        {
            String[] labels = metric.getMetaContent().split( "," );
            
            for ( String label : labels )
                result.add( label );
        }
        
        return result;
    }
}
