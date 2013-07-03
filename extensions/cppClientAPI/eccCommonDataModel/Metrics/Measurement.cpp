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
#include "Measurement.h"

using namespace boost::uuids;
using namespace std;




namespace ecc_commonDataModel
{

Measurement::Measurement()
{
  measurementUUID         = createRandomUUID();
  measurementTimeStamp    = getCurrentTime();
  measurementSynchronised = false;
}

Measurement::Measurement( const String& value )
{
  measurementUUID         = createRandomUUID();
  measurementTimeStamp    = getCurrentTime();
  measurementSynchronised = false;
  measurementValue        = value;
}
    
Measurement::Measurement( Measurement::ptr_t srcMeasurement )
{
  if ( srcMeasurement )
  {
    measurementUUID         = srcMeasurement->getUUID();
    measurementTimeStamp    = srcMeasurement->getTimeStamp();
    measurementSynchronised = srcMeasurement->getSynchronised();
    measurementValue        = srcMeasurement->getValue();
  }
}
    
Measurement::Measurement( const UUID&      uuid, 
                          const UUID&      msetUUID, 
                          const TimeStamp& timeStamp, 
                          const String&    value )
{
  measurementUUID         = uuid;
  measurementSetUUID      = msetUUID;
  measurementTimeStamp    = timeStamp;
  measurementValue        = value;
  measurementSynchronised = false;
}
    
Measurement::Measurement( const UUID&      uuid, 
                          const UUID&      msetUUID, 
                          const TimeStamp& timeStamp, 
                          const String&    value, 
                          const bool       synchronised )
{
  measurementUUID         = uuid;
  measurementSetUUID      = msetUUID;
  measurementTimeStamp    = timeStamp;
  measurementValue        = value;
  measurementSynchronised = synchronised;
}

Measurement::~Measurement()
{
}

UUID Measurement::getUUID()
{
  return measurementUUID;
}

void Measurement::setUUID( const UUID& ID )
{
  measurementUUID = ID;
}

UUID Measurement::getMeasurementSetUUID()
{
  return measurementSetUUID;
}

void Measurement::setMeasurementSetUUID( const UUID& ID )
{
  measurementSetUUID = ID;
}

TimeStamp Measurement::getTimeStamp()
{
  return measurementTimeStamp;
}

void Measurement::setTimeStamp( const TimeStamp& stamp )
{
  measurementTimeStamp = stamp;
}

String Measurement::getValue()
{
  return measurementValue;
}

void Measurement::setValue( const String& value )
{
  measurementValue = value;
}

bool Measurement::getSynchronised()
{
  return measurementSynchronised;
}

void Measurement::setSynchronised( const bool& synchronised )
{
  measurementSynchronised = synchronised;
}

// ModelBase -----------------------------------------------------------------
void Measurement::toJSON( String& jsonStrOUT )
{
}

void Measurement::fromJSON( const String& jsonStr )
{
}

String Measurement::toString()
{
  wstring ts;

  return ts;
}
    
} // namespace
