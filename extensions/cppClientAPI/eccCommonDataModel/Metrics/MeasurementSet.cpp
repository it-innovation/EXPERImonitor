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
#include "MeasurementSet.h"

using namespace boost::uuids;
using namespace std;




namespace ecc_commonDataModel
{

MeasurementSet::MeasurementSet()
  : measurementRule(eINDEFINITE), 
    measurementCountMax(0),
    samplingInterval(MINIMUM_SAMPLE_RATE_MS)
{
  msetID = createRandomUUID();
}
    
MeasurementSet::MeasurementSet( MeasurementSet::ptr_t ms, 
                                const bool copyMeasurements )
  : measurementRule(eINDEFINITE), 
    measurementCountMax(0),
    samplingInterval(MINIMUM_SAMPLE_RATE_MS)
{
  if ( ms )
  {
    msetID           = ms->getID();
    attributeID      = ms->getAttributeID();
    metricGroupID    = ms->getMetricGroupID();
    metric           = ms->getMetric();
    measurementRule  = ms->getMeasurementRule();
    samplingInterval = ms->getSampleInterval();

    if ( copyMeasurements )
    {
      Measurement::Set srcMeasures      = ms->getMeasurements();
      Measurement::Set::iterator srcMIt = srcMeasures.begin();
      
      while ( srcMIt != srcMeasures.end() )
      {
        appendMeasurement( *srcMIt );
        ++srcMIt;
      }
    }
  }
}

MeasurementSet::MeasurementSet( const UUID& msID )
  : measurementRule(eINDEFINITE), 
    measurementCountMax(0),
    samplingInterval(MINIMUM_SAMPLE_RATE_MS)
{
  msetID = msID;
}
 
MeasurementSet::MeasurementSet( const UUID&   msID, 
                                const UUID&   attrID, 
                                const UUID&   metGroupID, 
                                Metric::ptr_t met )
  : measurementRule(eINDEFINITE), 
    measurementCountMax(0),
    samplingInterval(MINIMUM_SAMPLE_RATE_MS)
{
  msetID              = msID;
  attributeID         = attrID;
  metricGroupID       = metGroupID;
  metric              = met;
}
    
MeasurementSet::MeasurementSet( const UUID&             msID, 
                                const UUID&             attrID, 
                                const UUID&             metGroupID, 
                                Metric::ptr_t           metric, 
                                const Measurement::Set& measures )
  : measurementRule(eINDEFINITE), 
    measurementCountMax(0),
    samplingInterval(MINIMUM_SAMPLE_RATE_MS)
{
  msetID        = msID;
  attributeID   = attrID;
  metricGroupID = metGroupID;
  metric        = metric;
 
  // Copy measurements
  Measurement::Set::const_iterator srcMIt = measures.begin();
  while ( srcMIt != measures.end() )
  {
    appendMeasurement( *srcMIt );
    ++srcMIt;
  }
}

MeasurementSet::~MeasurementSet()
{
}   

UUID MeasurementSet::getID()
{
  return msetID;
}

void MeasurementSet::setID( const UUID& ID )
{
  msetID = ID;
}

UUID MeasurementSet::getAttributeID()
{
  return attributeID;
}

void MeasurementSet::setAttributeID( const UUID& ID )
{
  attributeID = ID;
}

UUID MeasurementSet::getMetricGroupID()
{
  return metricGroupID;
}

void MeasurementSet::setMetricGroupID( const UUID& ID )
{
  metricGroupID = ID;
}

Metric::ptr_t MeasurementSet::getMetric()
{
  return metric;
}

void MeasurementSet::setMetric( Metric::ptr_t met )
{
  metric = met;
}

Measurement::Set MeasurementSet::getMeasurements()
{
  return measurements;
}

bool MeasurementSet::setMeasurements( const Measurement::Set& measures )
{
  bool setMeasures = false;

  if ( measurementRule != eFIXED_COUNT )
    setMeasures = true;
  else
    if ( measures.size() < measurementCountMax )
      setMeasures = true;
    
  if ( setMeasures ) measurements = measures;
        
  return setMeasures;
}
    
MeasurementSet::MEASUREMENT_RULE MeasurementSet::getMeasurementRule()
{
  return measurementRule;
}

void MeasurementSet::setMeasurementRule( const MEASUREMENT_RULE& rule )
{
  measurementRule = rule;
}

unsigned int MeasurementSet::getMeasurementCountMax()
{
  if ( measurementRule == eFIXED_COUNT )
    return measurementCountMax;

  return measurements.size();
}

bool MeasurementSet::setMeasurementCountMax( const unsigned int& max )
{
  // Safety first
  if ( measurementRule != eFIXED_COUNT )      return false;
  if ( max < 1 || measurements.size() > max ) return false;

  measurementCountMax = max;

  return true;
}
    
long MeasurementSet::getSampleInterval()
{
  return samplingInterval;
}

void MeasurementSet::setSampleInterval( const long& interval )
{
  if ( interval >= MINIMUM_SAMPLE_RATE_MS ) samplingInterval = interval;
}

bool MeasurementSet::addMeasurement( Measurement::ptr_t measurement )
{
  if ( !measurement ) return false;

  return appendMeasurement( measurement );
}

bool MeasurementSet::addMeasurements( const Measurement::Set& measurements )
{
  if ( measurements.empty() ) return false;

  bool skippedMeasurement = false;

  Measurement::Set::const_iterator msIt = measurements.begin();
  while ( msIt != measurements.end() )
  {
    if ( !appendMeasurement( *msIt ) )
      skippedMeasurement = true;

    ++msIt;
  }

  if ( skippedMeasurement ) return false; // Did not add all measurements

  return true;
}

// ModelBase -----------------------------------------------------------------
String MeasurementSet::toJSON()
{
  String json( L"{" );

  json.append( createJSON_Prop( L"msetID", uuidToWide(msetID) ) + L"," );

  json.append( createJSON_Prop( L"attributeID", uuidToWide(attributeID) ) + L"," );

  json.append( createJSON_Prop( L"metricGroupID", uuidToWide(metricGroupID) ) + L"," );

  // Metric
  if ( metric )
  {
    json.append( L"\"metric\":" );
    json.append( metric->toJSON() + L"," );
  }
  
  // Measurements
  json.append( L"\"measurements\":[" );

  Measurement::Set::const_iterator mIt = measurements.begin();
  while ( mIt != measurements.end() )
  {
    json.append( (*mIt)->toJSON() + L"," );

    ++mIt;
  }

  // Snip off trailing delimiter
  if ( !measurements.empty() )
  {
    unsigned int jLen = json.length();
    json = json.substr( 0, jLen-1 );
  }

  json.append( L"]," );

  // Measurement rule
  String mRule;
  switch ( measurementRule )
  {
  case eNO_LIVE_MONITOR : mRule = L"eNO_LIVE_MONITOR"; break;
  
  case eFIXED_COUNT : mRule = L"eFIXED_COUNT"; break;
  
  case eINDEFINITE : mRule = L"eINDEFINITE"; break;
  }
  json.append( createJSON_Prop( L"measurementRule", mRule ) + L"," );

  // MeasurementCountMax
  json.append( createJSON_Prop( L"measurementCountMax", measurementCountMax ) + L"," );

  // SampleInterval
  json.append( createJSON_Prop( L"samplingInterval", samplingInterval ) );

  json.append( L"}" );

  return json;
}

void MeasurementSet::fromJSON( const ModelBase::JSONTree& jsonTree )
{
}

String MeasurementSet::toString()
{
  wstring ts;

  return ts;
}

// Private methods -----------------------------------------------------------
bool MeasurementSet::appendMeasurement( Measurement::ptr_t m )
{
  bool addMeasure = false;

  if ( m )
  {
    if ( measurementRule != eFIXED_COUNT )
      addMeasure = true;
    else
      if ( measurements.size() < measurementCountMax ) addMeasure = true;

    if ( addMeasure )
    {
      m->setMeasurementSetUUID( msetID );
      measurements.insert( m );
    }
  }

  return addMeasure;
}


} // namespace