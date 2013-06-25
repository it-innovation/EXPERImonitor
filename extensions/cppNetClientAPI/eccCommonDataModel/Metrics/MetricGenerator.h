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

#include <boost/uuid/uuid.hpp>

#include <hash_set>




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
    class MetricGenerator : ModelBase
    {
    public:

        typedef boost::shared_ptr<MetricGenerator> ptr_t;

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
        MetricGenerator( const boost::uuids::uuid& uuid, 
                         const std::wstring&       name, 
                         const std::wstring&       description);
    
        /**
         * Constructor to set all fields of the Metric Generator class.
         * @param uuid The UUID used to uniquely identify a metric generator in this framework.
         * @param name The name of the metric generator.
         * @param description A description of the metric generator.
         * @param metricGroups A set of metric groups.
         * @param entities A set entities being observed.
         */
        MetricGenerator( boost::uuids::uuid                       uuid, 
                         const std::wstring&                      name, 
                         const std::wstring&                      description, 
                         const std::hash_set<MetricGroup::ptr_t>& metricGroups, 
                         const std::hash_set<Entity>&             entities );

        virtual ~MetricGenerator();

        /**
         * Getter/Setter for metric generator ID
         */
        boost::uuids::uuid& getUUID();

        void setUUID( const boost::uuids::uuid& ID );

        /**
         * Getter/Setter for metric generator name
         */
        std::wstring getName();

        void setName( const std::wstring& name );

        /**
         * Getter/Setter for metric generator description
         */
        std::wstring getDescription();

        void setDescription( const std::wstring& desc );

        /**
         * Getter/Setter for metric groups for this metric generator
         */
        std::hash_set<MetricGroup::ptr_t> getMetricGroups();

        void setMetricGroups( const std::hash_set<MetricGroup::ptr_t>& groups );

        /**
         * Getter/Setter for Entites associated with this metric generator
         */
        std::hash_set<Entity::ptr_t> getEntities();

        void setEntities( const std::hash_set<Entity::ptr_t>& entities );
    
        /**
         * @param metricGroup the metric group to add
         */
        void addMetricGroup( MetricGroup::ptr_t metricGroup );
    
        /**
         * @param metricGroups the metric groups to add
         */
        void addMetricGroups( const std::hash_set<MetricGroup::ptr_t>& metricGroups );
    
        /**
         * @param entity the entity to add
         */
        void addEntity( Entity::ptr_t entity );

    
        /**
         * @param entities the entities to add
         */
        void addEntities( const std::hash_set<Entity::ptr_t>& entities );

        // ModelBase -----------------------------------------------------------------
        virtual void toJSON( std::wstring& jsonStrOUT );

        virtual void fromJSON( const std::wstring& jsonStr );

        virtual std::wstring toString();
    
    };

} // namespace