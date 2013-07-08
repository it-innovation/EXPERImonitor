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
#include "MetricGroup.h"

using namespace boost::uuids;
using namespace std;




namespace ecc_commonDataModel
{

MetricGroup::MetricGroup()
{
  groupID = createRandomUUID();
}

MetricGroup::MetricGroup( MetricGroup::ptr_t mg )
{
  if ( mg )
  {
    groupID              = mg->getUUID();
    metricGeneratorUUID  = mg->getMetricGeneratorUUID();
    groupName            = mg->getName();
    groupDescription     = mg->getDescription();
    groupMeasurementSets = mg->getMeasurementSets();
  }
}

MetricGroup::MetricGroup( const UUID&   uuid, 
                          const UUID&   metGenUUID, 
                          const String& name, 
                          const String& description )
{
  groupID             = uuid;
  metricGeneratorUUID = metGenUUID;
  groupName           = name;
  groupDescription    = description;
}

MetricGroup::MetricGroup( const UUID&                uuid, 
                          const UUID&                metGenUUID, 
                          const String&              name, 
                          const String&              description, 
                          const MeasurementSet::Set& measurementSets )
{
  groupID              = uuid;
  metricGeneratorUUID  = metGenUUID;
  groupName            = name;
  groupDescription     = description;
  groupMeasurementSets = measurementSets;
}

MetricGroup::~MetricGroup()
{
}


UUID MetricGroup::getUUID()
{
  return groupID;
}

void MetricGroup::setUUID( const UUID& ID )
{
  groupID = ID;
}
  
UUID MetricGroup::getMetricGeneratorUUID()
{
  return metricGeneratorUUID;
}

void MetricGroup::setMetricGeneratorUUID( const UUID& ID )
{
  metricGeneratorUUID = ID;
}

String MetricGroup::getName()
{
  return groupName;
}

void MetricGroup::setName( const String& name )
{
  groupName = name;
}

String MetricGroup::getDescription()
{
  return groupDescription;
}

void MetricGroup::setDescription( const String& name )
{
  groupDescription = name;
}

MeasurementSet::Set MetricGroup::getMeasurementSets()
{
  return groupMeasurementSets;
}

void MetricGroup::setMeasurementSets( MeasurementSet::Set ms )
{
  groupMeasurementSets = ms;
}

void MetricGroup::addMeasurementSet( MeasurementSet::ptr_t measurementSet )
{
  if ( measurementSet )
    groupMeasurementSets.insert( measurementSet );
}
    
void MetricGroup::addMeasurementSets( const MeasurementSet::Set& measurementSets )
{
  MeasurementSet::Set::const_iterator msIt = measurementSets.begin();
  while ( msIt != measurementSets.end() )
  {
    MeasurementSet::ptr_t ms = *msIt;

    if ( ms ) groupMeasurementSets.insert( ms );

    ++msIt;
  }
}

// ModelBase -----------------------------------------------------------------
String MetricGroup::toJSON()
{
  String json;

  return json;
}

void MetricGroup::fromJSON( const ModelBase::JSONTree& jsonTree )
{
}

String MetricGroup::toString()
{
  wstring ts;

  return ts;
}
    
} // namespace