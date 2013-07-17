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

#pragma once

#include "MetricGenerator.h"
#include "Attribute.h"
#include "MetricGroup.h"
#include "Measurement.h"
#include "Unit.h"
#include "MetricType.h"
#include "Report.h"

#include <hash_set>
#include <hash_map>


namespace ecc_commonDataModel
{

/**
 * A Helper class for assisting with getting information from the metric data model.
 */
class MetricHelper
{
public:

    /**
     * Retrieves the attribute identified by the ID if it exists in the metric generator collection.
     * 
     * @param attributeID - ID of the attribute required, must not be null.
     * @param mgenSet     - Collection of metric generators, must not be null
     * @return            - Returned attribute instance, if it exists, otherwise null.
     */
    static Attribute::ptr_t getAttributeFromID( const UUID&                 attributeID,
                                                const MetricGenerator::Set& mgenSet );
    
    /**
     * Returns an entity identified by the ID, if it exists, from the metric generator collection.
     * 
     * @param entityID  - ID of the entity required, must not be null.
     * @param mgenSet   - Collection of metric generators, must not be null.
     * @return          - Returned entity instance, if it exists, otherwise null.
     */
    static Entity::ptr_t getEntityFromID( const UUID&                 entityID,
                                          const MetricGenerator::Set& mgenSet );
    
    /**
     * Returns all the attributes associated with a collection of metric generators.
     * 
     * @param mgenSet - Metric generator collection in which to search, must not be null.
     * @return        - Return collection of attributes (will be empty if no attributes exist)
     */
    static Attribute::Map getAllAttributes( const MetricGenerator::Set& mgenSet );
  
    /**
     * Returns all entities associated with a collection of metric generators.
     * 
     * @param mgenSet - Metric generator collection in which to search, must not be null.
     * @return        - Return collection of entities (will be empty if no entities exist)
     */
    static Entity::Map getAllEntities( const MetricGenerator::Set& mgenSet );
    
    /**
     * Returns all entities associated with a metric generator.
     * 
     * @param mgent - Metric generator in which to search, must not be null.
     * @return      - Return collection of entities (will be empty if no entities exist)
     */
    static Entity::Map getAllEntities( MetricGenerator::ptr_t mgen );
  
    /**
     * Returns a collection of all measurement sets associated with a collection of metric generators
     * 
     * @param mgenSet - Collection of metric generators in which to search, must not be null
     * @return        - Returned collection of measurement sets (will be empty if non exist in metric generator collection)
     */
    static MeasurementSet::Map getAllMeasurementSets( const MetricGenerator::Set& mgenSet );
    
    /**
     * Returns a collection of all measurement sets associated with a metric generator
     * 
     * @param mgen - Metric generator in which to search, must not be null
     * @return     - Returned collection of measurement sets (will be empty if non exist in metric generator)
     */
    static MeasurementSet::Map getAllMeasurementSets( MetricGenerator::ptr_t mgen );

    /**
     * Returns a map of all measurement sets associated with the entity identified by UUID.
     * 
     * @param mGens     - Set of metric generators to look in for entity & measurement sets
     * @param entityID  - Identity of the entity
     * @return          - Map of the measurement sets found (will be empty if none found)
     */
    static MeasurementSet::Map getMeasurementSetsForEntity( UUID entityID,
                                                            const MetricGenerator::Set& mgenSet );

    /**
     * Returns a collection of measurement sets (if any exist) associated with a specific attribute
     * from the metric generator collection.
     * 
     * @param attr    - Attribute of interest, must not be null.
     * @param mgenSet - Collection of metric generators to look for measurement sets, must not be null.
     * @return        - Return collection of measurement sets (will be empty if no measurement sets exist)
     */
    static MeasurementSet::Map getMeasurementSetsForAttribute( Attribute::ptr_t            attr, 
                                                               const MetricGenerator::Set& mgenSet );
    
    /**
     * Returns a collection of measurement sets (if they exist) associated with a specific attribute
     * from a single metric generator.
     * 
     * @param attr - Attribute of interest, must not be null.
     * @param mgen - Metric generators in which to look for measurement sets, must not be null.
     * @return     - Return collection of measurement sets (will be empty if no measurement sets exist)
     */
    static MeasurementSet::ptr_t getMeasurementSetForAttribute( Attribute::ptr_t       attr,
                                                                MetricGenerator::ptr_t mgen );
    
    /**
     * Returns the measurement set identified by the ID from a metric generator.
     * 
     * @param mgen              - Metric generator in which to search, must not be null.
     * @param measurementSetID  - MeasurementSet ID of measurement set to find
     * @return                  - Returned measurement set instance (null if it does not exist)
     */
    static MeasurementSet::ptr_t getMeasurementSet( MetricGenerator::ptr_t mgen,
                                                    const UUID&            measurementSetID );
    
    /**
     * Returns the measurement set identified by the ID from a metric generator collection.
     * 
     * @param mgenSet           - Metric generator collection in which to search, must not be null.
     * @param measurementSetID  - MeasurementSet ID of measurement set to find
     * @return                  - Returned measurement set instance (null if it does not exist)
     */
    static MeasurementSet::ptr_t getMeasurementSet( const MetricGenerator::Set& mgenSet,
                                                    const UUID&                 measurementSetID );
    
    /**
     * Sorts an unordered set of measurements by date.
     * 
     * @param measurements - Measurement set to sort - must not be null.
     * @return             - Returned sorted measurements (will be empty if input set empty)
     */
    static Measurement::Map_Time sortMeasurementsByDate( const Measurement::Set& measurements );
    
    /**
     * Creates an attribute with the parameters provided. If a valid entity is provided
     * the attribute and entity will be automatically associated.
     * 
     * @param name    - Name of the attribute to create.
     * @param desc    - Description of the attribute.
     * @param entity  - Entity that the attribute belongs to (can be null)
     * @return        - Attribute instance
     */
    static Attribute::ptr_t createAttribute( const String& name, 
                                             const String& desc, 
                                             Entity::ptr_t entity );
    
    /**
     * Searches for an attribute by its name.
     * 
     * @param name   - Name of the attribute to search, must not be null.
     * @param entity - Instance of the entity to query, must not be null.
     * @return       - Returned attribute (will be null if the attribute cannot be found)
     */
    static Attribute::ptr_t getAttributeByName( const String& name, 
                                                Entity::ptr_t entity );
    
    /**
     * Creates a metric group with the given name and description. Will automatically
     * associate the group with the metric generator, if one is provided.
     * 
     * @param name - Name of the entity to create.
     * @param desc - Description of the entity.
     * @param mGen - Metric generator to associate the entity with (may be null)
     * @return     - Returns an instance of a MetricGroup.
     */
    static MetricGroup::ptr_t createMetricGroup( const String&                name,
                                                 const String&                desc, 
                                                 const MetricGenerator::ptr_t mGen );
    
    /**
     * Creates a measurement set automatically linked to an attribute and metric group.
     * 
     * @param attr  - Attribute associated with the measurement set, must not be null.
     * @param type  - Metric type associated with the measurement set, must not be null.
     * @param unit  - Unit associated with the measurement set, must not be null.
     * @param group - Metric group that will contain the measurement set, must not be null.
     * @return      - Returns a new measurement set (null if parameters are invalid)
     */
    static MeasurementSet::ptr_t createMeasurementSet( Attribute::ptr_t   attr,
                                                       const MetricType&  type,
                                                       Unit::ptr_t        unit,
                                                       MetricGroup::ptr_t group );
    
    /**
     * Searches for a metric generator by name.
     * 
     * @param name  - Name of the metric generator to find, must not be null.
     * @param mGens - Collection of metric generators in which to search, must not be null.
     * @return      - Returns the metric generator (null if it does not exist)
     */
    static MetricGenerator::ptr_t getMetricGeneratorByName( const String&               name, 
                                                            const MetricGenerator::Set& mGens );
    
    /**
     * Searches for a metric group by name.
     * 
     * @param groupName - Name of the metric group to find.
     * @param mGroups   - Collection of metric groups in which to search.
     * @return          - Returned metric group, if it exists.
     */
    static MetricGroup::ptr_t getMetricGroupByName( const String&                      groupName,
                                                    const MetricGroup::Set& mGroups );
    
    /**
     * Creates a pre-initialised Report containing a measurement set based on the
     * instance provided. Use this convenience method for quickly creating a report
     * to send to the ECC.
     * 
     * @param sourceMS  - The measurement set 'template' upon which the report will be based, must not be null.
     * @return          - A new report instance (with new measurement set instance) that can be populated with measurements for the ECC.
     */
    static Report::ptr_t createEmptyMeasurementReport( MeasurementSet::ptr_t sourceMS );
    
    /**
     * Use this method to iterate through the contents of a metric generator.
     * 
     * @param mgen - Metric generator to describe - must not be null.
     * @return     - String describing the metric generator.
     */
    static String describeGenerator( MetricGenerator::ptr_t mgen );  
  };
    
} // namespace