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

#include "MetricType.h"
#include "Unit.h"

#include <boost/uuid/uuid.hpp>


namespace ecc_commonDataModel
{

/**
 * The metric class details the measurement of an attribute (of an entity).
 * 
 * @author Vegard Engen
 */
class Metric : ModelBase
{
public:
    typedef boost::shared_ptr<Metric> ptr_t;

    /**
     * Default constructor, which sets a random UUID for the object instance.
     */
    Metric();

    /**
     * Copy constructor; OBS: does a shallow copy of the unit.
     * @param m The metric object a copy is made of.
     */
    Metric( Metric::ptr_t m );
    
    /**
     * Constructor to set all the fields of the Metric object.
     * @param uuid The UUID used to uniquely identify a metric in this framework.
     * @param metricType The type of metric (e.g., nominal or interval).
     * @param unit The unit of the metric (e.g., meters or miles/second).
     */
    Metric( boost::uuids::uuid uuid, MetricType metricType, Unit unit );

    virtual ~Metric();

    /**
     * Getter/Setter for metric ID
     */
    boost::uuids::uuid getUUID();

    void setUUID( const boost::uuids::uuid& ID );

    /**
     * Getter/Setter for metric type
     */
    MetricType getMetricType();

    void setMetricType( const MetricType& type );

    /**
     * Getter/Setter for Unit related to this metric
     */
    Unit::ptr_t getUnit();

    void setUnit( Unit::ptr_t unit );

    // ModelBase -----------------------------------------------------------------
    virtual void toJSON( std::wstring& jsonStrOUT );

    virtual void fromJSON( const std::wstring& jsonStr );

    virtual std::wstring toString();
};

} // namespace