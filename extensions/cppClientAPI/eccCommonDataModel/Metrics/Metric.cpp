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

Metric::Metric()
{
  metricUUID = createRandomUUID();
}

Metric::Metric( Metric::ptr_t m )
{
  if ( m )
  {
    metricUUID = m->getUUID();
    metricType = m->getMetricType();
    metricUnit = m->getUnit();
  }
}
    
Metric::Metric( const UUID&       uuid, 
                const MetricType& mT, 
                Unit::ptr_t       unit )
{
  metricUUID = uuid;
  metricType = mT;
  metricUnit = unit;
}

Metric::~Metric()
{
}

UUID Metric::getUUID()
{
  return metricUUID;
}

void Metric::setUUID( const UUID& ID )
{
  metricUUID = ID;
}

MetricType Metric::getMetricType()
{
  return metricType;
}

void Metric::setMetricType( const MetricType& type )
{
  metricType = type;
}

Unit::ptr_t Metric::getUnit()
{
  return metricUnit;
}

void Metric::setUnit( Unit::ptr_t unit )
{
  metricUnit = unit;
}

// ModelBase -----------------------------------------------------------------
void Metric::toJSON( String& jsonStrOUT )
{
}

void Metric::fromJSON( const String& jsonStr )
{
}

String Metric::toString()
{
  wstring ts;

  return ts;
}

} // namespace