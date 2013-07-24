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
  : metricType(RATIO)
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

String Metric::getMetricTypeLabel()
{
  String result = L"unknown";

  switch ( metricType )
  {
  case NOMINAL  : result = L"NOMINAL";  break;
  case ORDINAL  : result = L"ORDINAL";  break;
  case INTERVAL : result = L"INTERVAL"; break;
  case RATIO    : result = L"RATIO";    break;
  }

  return result;
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
String Metric::toJSON()
{
  String json( L"{" );

  json.append( createJSON_Prop( L"uuid", uuidToWide(metricUUID) ) + L"," );

  // MetricType
  String mt;
  switch ( metricType )
  {
  case NOMINAL: mt = L"NOMINAL"; break;

  case ORDINAL: mt = L"ORDINAL"; break;

  case INTERVAL: mt = L"INTERVAL"; break;

  case RATIO: mt = L"RATIO"; break;
  }

  json.append( createJSON_Prop( L"metricType", mt ) + L"," );

  // Unit
  json.append( L"\"unit\":" );
  json.append( metricUnit->toJSON() );

  json.append( L"}" );

  return json;
}

void Metric::fromJSON( const ModelBase::JSONTree& jsonTree )
{
  // Client does not require implementation
}

String Metric::toString()
{
  String ts;

  if ( metricUnit ) ts.append( metricUnit->toString() );

  ts.append( uuidToWide(metricUUID) );

  return ts;
}

} // namespace