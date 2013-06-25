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

#include "MeasurementSet.h"

#include <boost/uuid/uuid.hpp>

#include <hash_map>
#include <hash_set>



namespace ecc_commonDataModel
{

    class MetricGroup
    {
    public:
        typedef boost::shared_ptr<MetricGroup> ptr_t;

        /**
         * Default constructor that sets a random UUID for this object instance.
         */
        MetricGroup();

        /**
         * A copy constructor, which takes a deep copy of the measurement sets.
         * @param mg A metric group object from which a copy is made.
         */
        MetricGroup( MetricGroup::ptr_t mg );

        /**
         * A constructor to set the basic information of a metric group.
         * @param uuid The UUID used to uniquely identify a metric group in this framework.
         * @param metricGeneratorUUID The UUID of the metric generator that's produced this metric group.
         * @param name The name of the metric group.
         * @param description A description of the metric group.
         */
        MetricGroup( const boost::uuids::uuid& uuid, 
                     const boost::uuids::uuid& metricGeneratorUUID, 
                     const std::wstring&       name, 
                     const std::wstring&       description );

        

    
        /**
         * A constructor to set the basic information of a metric group.
         * @param uuid The UUID used to uniquely identify a metric group in this framework.
         * @param metricGeneratorUUID The UUID of the metric generator that's produced this metric group.
         * @param name The name of the metric group.
         * @param description A description of the metric group.
         * @param measurementSets A set of measurement sets.
         */
        MetricGroup( const boost::uuids::uuid&                  uuid, 
                     const boost::uuids::uuid&                  metricGeneratorUUID, 
                     const std::wstring&                        name, 
                     const std::wstring&                        description, 
                     const std::hash_set<MeasurementSet::ptr_t> measurementSets );

        virtual ~MetricGroup();

        /**
         * Getter/Setter for MetricGroup ID
         */
        boost::uuids::uuid getUUID();

        void setUUID( const boost::uuids::uuid& ID );
  

        /**
         * Getter/Setter for the parent metric generator ID
         */
        boost::uuids::uuid getMetricGeneratorUUID();

        void setMetricGeneratorUUID( const boost::uuids::uuid& ID );

        /**
         * Getter/Setter for metric group name
         */
        std::wstring getName();

        void setName( const std::wstring& name );


        /**
         * Getter/Setter for metric group description
         */
        std::wstring getDescription();

        void setDescription( const std::wstring& name );

        /**
         * Getter/Setter for metric group measurement sets
         */
        std::hash_set<MeasurementSet::ptr_t> getMeasurementSets();

        void setMeasurementSets( std::hash_set<MeasurementSet::ptr_t> ms );

        /**
         * @param measurementSet the measurement set to add
         */
        void addMeasurementSets( MeasurementSet::ptr_t measurementSet );
    
        /**
         * @param measurementSets the measurement sets to add
         */
        void addMeasurementSets( std::hash_map<boost::uuids::uuid, MeasurementSet::ptr_t> measurementSets );
    };
    
} // namespace