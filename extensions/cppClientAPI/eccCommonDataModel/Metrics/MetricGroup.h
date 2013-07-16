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

#include "ModelBase.h"
#include "MeasurementSet.h"




namespace ecc_commonDataModel
{

    class MetricGroup : public ModelBase
    {
    public:
        typedef boost::shared_ptr<MetricGroup> ptr_t;

        typedef boost::container::set<MetricGroup::ptr_t> Set;

        typedef boost::container::map<UUID,MetricGroup::ptr_t> Map;

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
        MetricGroup( const UUID&   uuid, 
                     const UUID&   metricGeneratorUUID, 
                     const String& name, 
                     const String& description );

        

    
        /**
         * A constructor to set the basic information of a metric group.
         * @param uuid The UUID used to uniquely identify a metric group in this framework.
         * @param metricGeneratorUUID The UUID of the metric generator that's produced this metric group.
         * @param name The name of the metric group.
         * @param description A description of the metric group.
         * @param measurementSets A set of measurement sets.
         */
        MetricGroup( const UUID&                uuid, 
                     const UUID&                metricGeneratorUUID, 
                     const String&              name, 
                     const String&              description, 
                     const MeasurementSet::Set& measurementSets );

        virtual ~MetricGroup();

        /**
         * Getter/Setter for MetricGroup ID
         */
        UUID getUUID();

        void setUUID( const UUID& ID );
  
        /**
         * Getter/Setter for the parent metric generator ID
         */
        UUID getMetricGeneratorUUID();

        void setMetricGeneratorUUID( const UUID& ID );

        /**
         * Getter/Setter for metric group name
         */
        String getName();

        void setName( const String& name );

        /**
         * Getter/Setter for metric group description
         */
        String getDescription();

        void setDescription( const String& name );

        /**
         * Getter/Setter for metric group measurement sets
         */
        MeasurementSet::Set getMeasurementSets();

        void setMeasurementSets( MeasurementSet::Set ms );

        /**
         * @param measurementSet the measurement set to add
         */
        void addMeasurementSet( MeasurementSet::ptr_t measurementSet );
    
        /**
         * @param measurementSets the measurement sets to add
         */
        void addMeasurementSets( const MeasurementSet::Set& measurementSets );

        // ModelBase -----------------------------------------------------------------
        virtual String toJSON();

        virtual void fromJSON( const ModelBase::JSONTree& jsonTree );

        virtual String toString();

      private:

        UUID                groupID;
        UUID                metricGeneratorUUID;
        String              groupName;
        String              groupDescription;
        MeasurementSet::Set groupMeasurementSets;
    };
    
} // namespace