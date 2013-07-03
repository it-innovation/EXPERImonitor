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
#include "Experiment.h"

using namespace boost::uuids;
using namespace std;




namespace ecc_commonDataModel
{

Experiment::Experiment()
  : ModelBase()
{
  expUniqueID = createRandomUUID();
}
    
/**
  * Copy constructor; does a deep copy of the sets of entities and metric generators.
  * @param ex Experiment object a copy should be made from.
  */
Experiment::Experiment( Experiment::ptr_t ex ) 
  : ModelBase()
{
  expUniqueID = createRandomUUID();

  if ( !ex ) return;

  // Try copying experiment UUID
  UUID targetUUID = ex->getUUID();

  if ( !ex->getUUID().is_nil() )
    expUniqueID = targetUUID;

  // Copy other parameters
  experimentID = ex->getExperimentID();
  name         = ex->getName();
  description  = ex->getDescription();
  startTime    = ex->getStartTime();
  endTime      = ex->getEndTime();

  // Copy metric generators
  MetricGenerator::Set srcGenerators = ex->getMetricGenerators();
  if ( !srcGenerators.empty() )
  {
    MetricGenerator::Set::iterator srcGenIt = srcGenerators.begin();
    while ( srcGenIt != srcGenerators.end() )
    {
      metricGenerators.insert( *srcGenIt );
      ++srcGenIt;
    }
  }
}
    
Experiment::Experiment( const UUID&      uuid, 
                        const String&    experimentID, 
                        const String&    name, 
                        const String&    description,
                        const TimeStamp& creationTime )
  : ModelBase()
{
  this->expUniqueID  = uuid;
  this->experimentID = experimentID;
  this->name         = name;
  this->description  = description;
  this->startTime    = creationTime;
}
    
Experiment::Experiment( const UUID&      uuid, 
                        const String&    experimentID, 
                        const String&    name, 
                        const String&    description, 
                        const TimeStamp& creationTime, 
                        const TimeStamp& endTime )
  : ModelBase()
{
  this->expUniqueID  = uuid;
  this->experimentID = experimentID;
  this->name         = name;
  this->description  = description;
  this->startTime    = creationTime;
  this->endTime      = endTime;
}

UUID Experiment::getUUID()
{
  return expUniqueID;
}

void Experiment::setUUID( const UUID& ID )
{
  expUniqueID = ID;
}

String Experiment::getExperimentID()
{
  return experimentID;
}

void Experiment::setExperimentID( const String& ID )
{
  experimentID = ID;
}

String Experiment::getName()
{
  return name;
}

void Experiment::setName( const String& name )
{
  this->name = name;
}

/**
  * Getter/Setter for the experiment description
  */
String Experiment::getDescription()
{
  return description;
}

void Experiment::setDescription( const String& desc )
{
  description = desc;
}

/**
  * Getter/Setter for the experiment's start time
  */
TimeStamp Experiment::getStartTime()
{
  return startTime;
}

void Experiment::setStartTime( const TimeStamp& time )
{
  startTime = time;
}

/**
  * Getter/Setter for the experiment's end time
  */
TimeStamp Experiment::getEndTime()
{
  return endTime;
}

void Experiment::setEndTime( const TimeStamp& time )
{
  endTime = time;
}

/**
  * Getter/Setter for the metric generators associated with this experiment
  */
MetricGenerator::Set Experiment::getMetricGenerators()
{
  return metricGenerators;
}

void Experiment::setMetricGenerators( const MetricGenerator::Set& generators )
{
  metricGenerators = generators;
}
    
/**
  * @param metricGenerator the metric generator to add
  */
void Experiment::addMetricGenerator( MetricGenerator::ptr_t metricGenerator )
{
  if ( metricGenerator ) metricGenerators.insert( metricGenerator );
}
    
/**
  * @param metricGenerators the metric generators to add
  */
void Experiment::addMetricGenerators( const MetricGenerator::Set& generators )
{
  metricGenerators.insert( generators.begin(), generators.end() );
}

// ModelBase -----------------------------------------------------------------
void Experiment::toJSON( String& jsonStrOUT )
{
}

void Experiment::fromJSON( const String& jsonStr )
{
}

String Experiment::toString()
{
  wstring ts;

  return ts;
}

} // namespace
