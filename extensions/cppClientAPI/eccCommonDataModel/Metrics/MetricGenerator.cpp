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
#include "MetricGenerator.h"

using namespace boost::uuids;
using namespace std;




namespace ecc_commonDataModel
{

MetricGenerator::MetricGenerator()
{
  mgID = createRandomUUID();
}
    
MetricGenerator::MetricGenerator( MetricGenerator::ptr_t mg )
{
  if ( mg )
  {
    mgID           = mg->getUUID();
    mgName         = mg->getName();
    mgDescription  = mg->getDescription();
    mgMetricGroups = mg->getMetricGroups();
    mgEntities     = mg->getEntities();
  }
}
    
MetricGenerator::MetricGenerator( const UUID&   uuid, 
                                  const String& name, 
                                  const String& description )
{
  mgID          = uuid;
  mgName        = name;
  mgDescription = description;
}
    
MetricGenerator::MetricGenerator( const UUID&             uuid, 
                                  const String&           name, 
                                  const String&           description, 
                                  const MetricGroup::Set& metricGroups, 
                                  const Entity::Set&      entities )
{
  mgID           = uuid;
  mgName         = name;
  mgDescription  = description;
  mgMetricGroups = metricGroups;
  mgEntities     = entities;
}

MetricGenerator::~MetricGenerator()
{
}

UUID MetricGenerator::getUUID()
{
  return mgID;
}

void MetricGenerator::setUUID( const UUID& ID )
{
  mgID = ID;
}

String MetricGenerator::getName()
{
  return mgName;
}

void MetricGenerator::setName( const String& name )
{
  mgName = name;
}

String MetricGenerator::getDescription()
{
  return mgDescription;
}

void MetricGenerator::setDescription( const String& desc )
{
  mgDescription = desc;
}

MetricGroup::Set MetricGenerator::getMetricGroups()
{
  return mgMetricGroups;
}

void MetricGenerator::setMetricGroups( const MetricGroup::Set& groups )
{
  mgMetricGroups = groups;
}

Entity::Set MetricGenerator::getEntities()
{
  return mgEntities;
}

void MetricGenerator::setEntities( const Entity::Set& entities )
{
  mgEntities = entities;
}
    
void MetricGenerator::addMetricGroup( MetricGroup::ptr_t metricGroup )
{
  if ( metricGroup ) mgMetricGroups.insert( metricGroup );
}
    
void MetricGenerator::addMetricGroups( const MetricGroup::Set& metricGroups )
{
  MetricGroup::Set::const_iterator mgIt = metricGroups.begin();
  while ( mgIt != metricGroups.end() )
  {
    MetricGroup::ptr_t mg = *mgIt;

    if ( mg ) mgMetricGroups.insert( mg );

    ++mgIt;
  }
}
    
void MetricGenerator::addEntity( Entity::ptr_t entity )
{
  if ( entity ) mgEntities.insert( entity );
}

void MetricGenerator::addEntities( const Entity::Set& entities )
{
  Entity::Set::const_iterator entIt = entities.begin();
  while ( entIt != entities.end() )
  {
    Entity::ptr_t entity = *entIt;

    if ( entity ) mgEntities.insert( entity );

    ++entIt;
  }
}

// ModelBase -----------------------------------------------------------------
String MetricGenerator::toJSON()
{
  String json( L"{" );

  json.append( createJSON_Prop( L"uuid", uuidToWide(mgID) ) + L"," );

  json.append( createJSON_Prop( L"name", mgName ) + L"," );

  json.append( createJSON_Prop( L"description", mgDescription ) + L"," );

  // Metric Groups
  json.append( L"\"metricGroups\":[" );

  MetricGroup::Set::const_iterator mgIt = mgMetricGroups.begin();
  while ( mgIt != mgMetricGroups.end() )
  {
    json.append( (*mgIt)->toJSON() + L"," );

    ++mgIt;
  }

  // Snip off trailing delimiter
  if ( !mgMetricGroups.empty() )
  {
    unsigned int jLen = json.length();
    json = json.substr( 0, jLen-1 );
  }

  json.append( L"]," );

  // Entities
  json.append( L"\"entities\":[" );

  Entity::Set::const_iterator entIt = mgEntities.begin();
  while ( entIt != mgEntities.end() )
  {
    json.append( (*entIt)->toJSON() + L"," );

    ++entIt;
  }

  // Snip off trailing delimiter
  if ( !mgEntities.empty() )
  {
    int jLen = json.length();
    json = json.substr( 0, jLen-1 );
  }

  json.append( L"]" );

  json.append( L"}" );

  return json;
}

void MetricGenerator::fromJSON( const ModelBase::JSONTree& jsonTree )
{
  // Client does not require implementation
}

String MetricGenerator::toString()
{
  return mgName + L" {" + uuidToWide(mgID) + L"} " +
         mgDescription;
}

} // namespace