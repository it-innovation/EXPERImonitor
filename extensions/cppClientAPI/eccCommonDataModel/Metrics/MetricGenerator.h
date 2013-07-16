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

#include "Entity.h"
#include "MetricGroup.h"


namespace ecc_commonDataModel
{

    /**
     * This class represents something or someone who generates and provides metrics
     * about the attributes of certain entities. This could be, for example, a 
     * computational process or a human being operating a mobile device.
     * 
     * Metrics are organised within metric groups. It is possible to define a hierarchy
     * of metric groups as well, as a metric group can contain a set of metric groups.
     */
    class MetricGenerator : public ModelBase
    {
    public:
      
      typedef boost::shared_ptr<MetricGenerator> ptr_t;

      typedef boost::container::set<MetricGenerator::ptr_t> Set;

      typedef boost::container::map<UUID,MetricGenerator::ptr_t> Map;

      /**
        * Default constructor, which sets a random UUID for the object instance.
        */
      MetricGenerator();
    
      /**
        * A copy constructor; takes a deep copy of the UUID, metric groups and entities.
        * @param mg A metric generator object from which a copy is made.
        */
      MetricGenerator( MetricGenerator::ptr_t mg );
    
      /**
        * Constructor to set all fields of the Metric Generator class.
        * @param uuid The UUID used to uniquely identify a metric generator in this framework.
        * @param name The name of the metric generator.
        * @param description A description of the metric generator.
        */
      MetricGenerator( const UUID&   uuid, 
                       const String& name, 
                       const String& description);
    
      /**
        * Constructor to set all fields of the Metric Generator class.
        * @param uuid The UUID used to uniquely identify a metric generator in this framework.
        * @param name The name of the metric generator.
        * @param description A description of the metric generator.
        * @param metricGroups A set of metric groups.
        * @param entities A set entities being observed.
        */
      MetricGenerator( const UUID&             uuid, 
                       const String&           name, 
                       const String&           description, 
                       const MetricGroup::Set& metricGroups, 
                       const Entity::Set&      entities );

      virtual ~MetricGenerator();

      /**
        * Getter/Setter for metric generator ID
        */
      UUID getUUID();

      void setUUID( const UUID& ID );

      /**
        * Getter/Setter for metric generator name
        */
      std::wstring getName();

      void setName( const String& name );

      /**
        * Getter/Setter for metric generator description
        */
      String getDescription();

      void setDescription( const String& desc );

      /**
        * Getter/Setter for metric groups for this metric generator
        */
      MetricGroup::Set getMetricGroups();

      void setMetricGroups( const MetricGroup::Set& groups );

      /**
        * Getter/Setter for Entites associated with this metric generator
        */
      Entity::Set getEntities();

      void setEntities( const Entity::Set& entities );
    
      /**
        * @param metricGroup the metric group to add
        */
      void addMetricGroup( MetricGroup::ptr_t metricGroup );
    
      /**
        * @param metricGroups the metric groups to add
        */
      void addMetricGroups( const MetricGroup::Set& metricGroups );
    
      /**
        * @param entity the entity to add
        */
      void addEntity( Entity::ptr_t entity );

    
      /**
        * @param entities the entities to add
        */
      void addEntities( const Entity::Set& entities );

      // ModelBase -----------------------------------------------------------------
      virtual String toJSON();

      virtual void fromJSON( const ModelBase::JSONTree& jsonTree );

      virtual String toString();

    private:
      
      UUID             mgID;
      String           mgName;
      String           mgDescription;
      MetricGroup::Set mgMetricGroups;
      Entity::Set      mgEntities;

    };

} // namespace