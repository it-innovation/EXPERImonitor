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
#include "MetricHelper.h"

namespace ecc_commonDataModel
{

Attribute::ptr_t MetricHelper::getAttributeFromID( const UUID& attributeID,
                                                   const MetricGenerator::Set& mgenSet )
{
  Attribute::ptr_t target;

  Attribute::Map attributes = MetricHelper::getAllAttributes( mgenSet );

  Attribute::Map::const_iterator findIt = attributes.find( attributeID );

  if ( findIt != attributes.end() ) target = findIt->second;

  return target;
}
    
Entity::ptr_t MetricHelper::getEntityFromID( const UUID&                 entityID,
                                             const MetricGenerator::Set& mgenSet )
{
  Entity::ptr_t target;

  Entity::Map entities = MetricHelper::getAllEntities( mgenSet );
  
  Entity::Map::const_iterator findIt = entities.find( entityID );

  if ( findIt != entities.end() ) target = findIt->second;

  return target;
}
  
MeasurementSet::Map MetricHelper::getMeasurementSetsForAttribute( Attribute::ptr_t            attr, 
                                                                  const MetricGenerator::Set& mgenSet )
{
  MeasurementSet::Map targetMap;

  if ( attr )
  {
    UUID targetAttID                         = attr->getUUID();
    MeasurementSet::Map allSets              = getAllMeasurementSets( mgenSet );
    MeasurementSet::Map::const_iterator msIt = allSets.begin();

    while ( msIt != allSets.end() )
    {
      MeasurementSet::ptr_t ms = msIt->second;
      UUID linkedAttrID = ms->getAttributeID();

      if ( linkedAttrID == targetAttID )
        targetMap.insert( MeasurementSet::Map::value_type(ms->getID(), ms) );

      ++msIt;
    }

  }

  return targetMap;
}
    
MeasurementSet::ptr_t MetricHelper::getMeasurementSetForAttribute( Attribute::ptr_t       attr,
                                                                   MetricGenerator::ptr_t mgen )
{
  MeasurementSet::ptr_t msTarget;

  if ( attr )
  {
    MeasurementSet::Map allSets = MetricHelper::getAllMeasurementSets( mgen );
    MeasurementSet::Map::const_iterator msIt = allSets.begin();

    while ( msIt != allSets.end() )
    {
      MeasurementSet::ptr_t ms = msIt->second;
      UUID linkedAttrID = ms->getAttributeID();

      if ( linkedAttrID == attr->getUUID() )
      {
        msTarget = ms;
        break;
      }

      ++msIt;
    }
  }

  return msTarget;
}
    
Attribute::Map MetricHelper::getAllAttributes( const MetricGenerator::Set& mgenSet )
{
  Attribute::Map attributes;

  Entity::Map entities = getAllEntities( mgenSet );
  Entity::Map::const_iterator entIt = entities.begin();

  while ( entIt != entities.end() )
  {
    Attribute::Set srcAttrs = entIt->second->getAttributes();
    Attribute::Set::const_iterator attIt = srcAttrs.begin();
    
    while ( attIt != srcAttrs.end() )
    {
      Attribute::ptr_t attr = *attIt;
      attributes.insert( Attribute::Map::value_type(attr->getUUID(), attr) );

      ++attIt;
    }

    ++entIt;
  }

  return attributes;
}
  
Entity::Map MetricHelper::getAllEntities( const MetricGenerator::Set& mgenSet )
{
  Entity::Map entities;

  MetricGenerator::Set::const_iterator mgenIt = mgenSet.begin();
  while ( mgenIt != mgenSet.end() )
  {
    MetricGenerator::ptr_t mg = *mgenIt;

    Entity::Map mgEnts = getAllEntities( mg );
    entities.insert( mgEnts.begin(), mgEnts.end() );

    ++mgenIt;
  }

  return entities;
}
    
Entity::Map MetricHelper::getAllEntities( MetricGenerator::ptr_t mgen )
{
  Entity::Map entities;

  if ( mgen )
  {
    Entity::Set mgEnts = mgen->getEntities();
    Entity::Set::const_iterator mgEntIt = mgEnts.begin();
  
    while ( mgEntIt != mgEnts.end() )
    {
      Entity::ptr_t entity = *mgEntIt;

      entities.insert( Entity::Map::value_type(entity->getUUID(), entity) );
    
      ++mgEntIt;
    }
  }

  return entities;
}
  
MeasurementSet::Map MetricHelper::getAllMeasurementSets( const MetricGenerator::Set& mgenSet )
{
  MeasurementSet::Map mSets;

  MetricGenerator::Set::const_iterator mgenIt = mgenSet.begin();
  while ( mgenIt != mgenSet.end() )
  {
    MetricGroup::Set mGroups = (*mgenIt)->getMetricGroups();
    MetricGroup::Set::const_iterator mgIt = mGroups.begin();

    while( mgIt != mGroups.end() )
    {
      MeasurementSet::Set msSet = (*mgIt)->getMeasurementSets();
      MeasurementSet::Set::const_iterator msIt = msSet.begin();

      while ( msIt != msSet.end() )
      {
        MeasurementSet::ptr_t ms = *msIt;
        mSets.insert( MeasurementSet::Map::value_type(ms->getID(), ms) );

        ++msIt;
      }
      ++mgIt;
    }
    ++mgenIt;
  }

  return mSets;
}
    
MeasurementSet::Map MetricHelper::getAllMeasurementSets( MetricGenerator::ptr_t mgen )
{
  MeasurementSet::Map mSets;

  if ( mgen )
  {
    MetricGroup::Set mGroups = mgen->getMetricGroups();
    MetricGroup::Set::const_iterator groupIt = mGroups.begin();
    
    while ( groupIt != mGroups.end() )
    {
      MeasurementSet::Set msSet = (*groupIt)->getMeasurementSets();
      MeasurementSet::Set::const_iterator msIt = msSet.begin();

      while ( msIt != msSet.end() )
      {
        MeasurementSet::ptr_t ms = *msIt;
        mSets.insert( MeasurementSet::Map::value_type(ms->getID(), ms) );

        ++msIt;
      }

      ++groupIt;
    }
  }

  return mSets;
}
    
MeasurementSet::ptr_t MetricHelper::getMeasurementSet( MetricGenerator::ptr_t mgen,
                                                       const UUID&            measurementSetID )
{
  MeasurementSet::ptr_t targetSet;

  if ( mgen )
  {
    MetricGroup::Set mGroup = mgen->getMetricGroups();
    MetricGroup::Set::const_iterator mgIt = mGroup.begin();

    while ( mgIt != mGroup.end() )
    {
      MeasurementSet::Set msets = (*mgIt)->getMeasurementSets();
      MeasurementSet::Set::const_iterator msIt = msets.begin();

      while ( msIt != msets.end() )
      {
        MeasurementSet::ptr_t ms = *msIt;

        if ( ms->getID() == measurementSetID )
        {
          targetSet = ms;
          break;
        }
        ++msIt;
      }
      ++mgIt;
    }
  }

  return targetSet;
}
   
MeasurementSet::ptr_t MetricHelper::getMeasurementSet( const MetricGenerator::Set& mgenSet,
                                                       const UUID&                 measurementSetID )
{
  MeasurementSet::ptr_t targetSet;

  MetricGenerator::Set::const_iterator mgenIt = mgenSet.begin();
  while ( mgenIt != mgenSet.end() )
  {
    targetSet = getMeasurementSet( *mgenIt, measurementSetID );

    if ( targetSet ) break;

    ++mgenIt;
  }

  return targetSet;
}
    
Measurement::Map_Time MetricHelper::sortMeasurementsByDate( const Measurement::Set& measurements )
{
  Measurement::Map_Time targetMap;

  // TODO: Not actually required by client code

  return targetMap;
}
    
Attribute::ptr_t MetricHelper::createAttribute( const String& name, 
                                                const String& desc, 
                                                Entity::ptr_t entity )
{
  Attribute::ptr_t attribute( new Attribute() );

  attribute->setName( name );
  attribute->setDescription( desc );

  if ( entity )
  {
    attribute->setEntityUUID( entity->getUUID() );
    entity->addAttribute( attribute );
  }

  return attribute;
}
    
Attribute::ptr_t MetricHelper::getAttributeByName( const String& name, 
                                                   Entity::ptr_t entity )
{
  Attribute::ptr_t targAttr;

  if ( entity )
  {
    Attribute::Set attrs = entity->getAttributes();
    Attribute::Set::const_iterator attIt = attrs.begin();

    while ( attIt != attrs.end() )
    {
      Attribute::ptr_t attr = *attIt;
      const String attName = attr->getName();

      if ( attName.compare(name) )
      {
        targAttr = attr;
        break;
      }

      ++attIt;
    }
  }

  return targAttr;
}
    
MetricGroup::ptr_t MetricHelper::createMetricGroup( const String&                name,
                                                    const String&                desc, 
                                                    const MetricGenerator::ptr_t mGen )
{
  MetricGroup::ptr_t mGroup = MetricGroup::ptr_t( new MetricGroup() );

  mGroup->setName( name );
  mGroup->setDescription( desc );

  if ( mGen )
  {
    mGen->addMetricGroup( mGroup );
    mGroup->setMetricGeneratorUUID( mGen->getUUID() );
  }

  return mGroup;
}
    
MeasurementSet::ptr_t MetricHelper::createMeasurementSet( Attribute::ptr_t   attr,
                                                          const MetricType&  type,
                                                          Unit::ptr_t        unit,
                                                          MetricGroup::ptr_t group )
{
  MeasurementSet::ptr_t mSet;

  if ( attr && unit && group )
  {
    Metric::ptr_t metric = Metric::ptr_t( new Metric() );
    metric->setMetricType( type );
    metric->setUnit( unit );
    
    mSet = MeasurementSet::ptr_t( new MeasurementSet() );
    mSet->setMetric( metric );
    mSet->setAttributeID( attr->getUUID() );
    mSet->setMetricGroupID( group->getUUID() );

    group->addMeasurementSet( mSet );
  }

  return mSet;
}
    
MetricGenerator::ptr_t MetricHelper::getMetricGeneratorByName( const String&               name, 
                                                               const MetricGenerator::Set& mGens )
{
  MetricGenerator::ptr_t targetGen;

  MetricGenerator::Set::const_iterator genIt = mGens.begin();
  while ( genIt != mGens.end() )
  {
    MetricGenerator::ptr_t mgen = *genIt;
    const String genName = mgen->getName();

    if ( genName.compare(name) )
    {
      targetGen = mgen;
      break;
    }

    ++genIt;
  }

  return targetGen;
}

MetricGroup::ptr_t MetricHelper::getMetricGroupByName( const String&           groupName,
                                                       const MetricGroup::Set& mGroups )
{
  MetricGroup::ptr_t targetGroup;

  MetricGroup::Set::const_iterator groupIt = mGroups.begin();
  while ( groupIt != mGroups.end() )
  {
    MetricGroup::ptr_t group = *groupIt;
    const String gName = group->getName();

    if ( gName.compare(groupName) )
    {
      targetGroup = group;
      break;
    }

    ++groupIt;
  }


  return targetGroup;
}
    
Report::ptr_t MetricHelper::createEmptyMeasurementReport( MeasurementSet::ptr_t sourceMS )
{
  Report::ptr_t targetReport;

  if ( sourceMS )
  {
    MeasurementSet::ptr_t emptyMS = MeasurementSet::ptr_t( new MeasurementSet(sourceMS, false) );
    TimeStamp now = getCurrentTime();

    targetReport = Report::ptr_t( new Report(createRandomUUID(), emptyMS, now, now, now) );
  }

  return targetReport;
}
    
String MetricHelper::describeGenerator( MetricGenerator::ptr_t mgen )
{
  String desc = L"Could not describe generator (null)";

  if ( mgen )
  {
    desc = mgen->getName() + L"\n";
    desc += L"Description: " + mgen->getDescription() + L"\n";

    Entity::Map mGenEntities = MetricHelper::getAllEntities( mgen );
    if ( !mGenEntities.empty() )
    {
      desc += L"Generator has " + intToString(mGenEntities.size()) + L" entities" + L"\n";

      Entity::Map::const_iterator entIt = mGenEntities.begin();
      while ( entIt != mGenEntities.end() )
      {
        Entity::ptr_t entity = entIt->second;
        desc += L"  Entity: " + entity->getName() + L"\n";
        desc += L"  Description: " + entity->getDescription() + L"\n";

        Attribute::Set attributes = entity->getAttributes();
        if ( !attributes.empty() )
        {
          desc += L"  Number of attributes: " + intToString(attributes.size()) + L"\n";
          
          Attribute::Set::const_iterator attIt = attributes.begin();
          while ( attIt != attributes.end() )
          {
            Attribute::ptr_t att = *attIt;
            desc += L"    Attribute: " + att->getName() + L"\n";
            desc += L"    Description: " + att->getDescription() + L"\n";
                
            MeasurementSet::ptr_t ms = getMeasurementSetForAttribute( att, mgen );
            if ( ms )
            {
              desc += L"      Measurement set ID: " + uuidToWide(ms->getID()) + L"\n";
                  
              Metric::ptr_t metric = ms->getMetric();
              if ( metric )
              {
                desc += L"      Metric unit: " + metric->getUnit()->toString() + L"\n";
                desc += L"      Metric type: " + metric->getMetricTypeLabel() + L"\n";
              }
              else desc += L"      Measurement set has no metric.";
            }
            else desc += L"    Attribute has no measurement set associated with it.\n";
          
            ++attIt;
          }
        
        ++entIt;
        }
        else desc += L"  Entity has no attributes.\n";
      }
    }
    else desc += L"Generator has no entities associated with it.\n";
        
    desc += L"\n";
  }

  return desc;
}

    
} // namespace