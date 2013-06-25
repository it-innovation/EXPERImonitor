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

#include "stdafx.h"
#include "Metric.h"

using namespace boost::uuids;
using namespace std;




namespace ecc_commonDataModel
{

/**
 * The metric class details the measurement of an attribute (of an entity).
 * 
 * @author Vegard Engen
 */
//class Metric
//{
//    /**
//     * Default constructor, which sets a random UUID for the object instance.
//     */
//    public Metric()
//    {
//        this.uuid = Guid.NewGuid();
//    }
//    
//    /**
//     * Copy constructor; OBS: does a shallow copy of the unit.
//     * @param m The metric object a copy is made of.
//     */
//    public Metric(Metric m)
//    {
//        if (m == null)
//            return;
//        
//        if (m.uuid != null)
//            this.uuid = new Guid( m.uuid.ToString() );
//        
//        this.metricType = m.metricType;
//
//        if (m.unit != null)
//            this.unit = new Unit(m.unit);
//    }
//    
//    /**
//     * Constructor to set all the fields of the Metric object.
//     * @param uuid The UUID used to uniquely identify a metric in this framework.
//     * @param metricType The type of metric (e.g., nominal or interval).
//     * @param unit The unit of the metric (e.g., meters or miles/second).
//     */
//    public Metric(Guid uuid, MetricType metricType, Unit unit)
//    {
//        this.uuid = uuid;
//        this.metricType = metricType;
//        this.unit = unit;
//    }
//
//    public Guid uuid
//    {
//        get;
//        set;
//    }
//
//    public MetricType metricType
//    {
//        get;
//        set;
//    }
//
//    public Unit unit
//    {
//        get;
//        set;
//    }
//};

// ModelBase -----------------------------------------------------------------
void Metric::toJSON( wstring& jsonStrOUT )
{
}

void Metric::fromJSON( const wstring& jsonStr )
{
}

wstring Metric::toString()
{
  wstring ts;

  return ts;
}

} // namespace