/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2012
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
#include "Report.h"

using namespace boost::uuids;
using namespace std;




namespace ecc_commonDataModel
{

Report::Report()
{
  reportID             = createRandomUUID();
  reportDate           = getCurrentTime();
  numberOfMeasurements = 0;
}

Report::Report( const UUID& msID )
{
  reportID             = createRandomUUID();
  reportDate           = getCurrentTime();
  numberOfMeasurements = 0;
  
  measurementSet = MeasurementSet::ptr_t( new MeasurementSet( msID ) );
}

Report::Report( Report::ptr_t report )
{
  if ( report )
  {
    reportID = report->getReportID();

    MeasurementSet::ptr_t ms = report->getMeasurementSet();
    if ( ms ) measurementSet = MeasurementSet::ptr_t( new MeasurementSet(ms, true) );

    reportDate           = report->getReportDate();
    fromDate             = report->getReportFromDate();
    toDate               = report->getReportToDate();
    numberOfMeasurements = report->getNumberOfMeasurements();
  }
}

Report::Report( const UUID&           uuid, 
                MeasurementSet::ptr_t mSet, 
                const TimeStamp&      rD, 
                const TimeStamp&      fD, 
                const TimeStamp&      tD )
{
  reportID       = uuid;
  measurementSet = mSet;
  reportDate     = rD;
  fromDate       = fD;
  toDate         = tD;
  
  if ( mSet )
    numberOfMeasurements = mSet->getMeasurements().size();
  else
    numberOfMeasurements = 0;
}

Report::Report( const UUID&                 uuid, 
                const MeasurementSet::ptr_t mSet, 
                const TimeStamp&            rD, 
                const TimeStamp&            fD, 
                const TimeStamp&            tD, 
                const int&                  numMeasures )
{
  reportID       = uuid;
  measurementSet = mSet;
  reportDate     = rD;
  fromDate       = fD;
  toDate         = tD;
  numberOfMeasurements = numMeasures;
}

Report::~Report()
{
}

UUID Report::getReportID()
{
  return reportID;
}

void Report::setReportID( const UUID& ID )
{
  reportID = ID;
}

MeasurementSet::ptr_t Report::getMeasurementSet()
{
  return measurementSet;
}
   
void Report::setMeasurementSet( MeasurementSet::ptr_t ms )
{
  measurementSet = ms;
}

TimeStamp Report::getReportDate()
{
  return reportDate;
}
        
void Report::setReportDate( const TimeStamp& date )
{
  reportDate = date;
} 

TimeStamp Report::getReportFromDate()
{
  return fromDate;
}
        
void Report::setReportFromDate( const TimeStamp& date )
{
  fromDate = date;
} 

TimeStamp Report::getReportToDate()
{
  return toDate;
}
        
void Report::setReportToDate( const TimeStamp& date )
{
  toDate = date;
} 

int Report::getNumberOfMeasurements()
{
  return numberOfMeasurements;
}

void Report::setNumberOfMeasurements( const int& mCount )
{
  numberOfMeasurements = mCount;
}

void Report::copyReport( Report::ptr_t repIn, bool copyMeasurements )
{
  if ( repIn )
  {
    reportID = repIn->getReportID();

    measurementSet = MeasurementSet::ptr_t( new MeasurementSet( repIn->getMeasurementSet(), 
                                                                copyMeasurements) );

    reportDate           = repIn->getReportDate();
    fromDate             = repIn->getReportFromDate();
    toDate               = repIn->getReportToDate();
    numberOfMeasurements = repIn->getNumberOfMeasurements();
  }
}

// ModelBase -----------------------------------------------------------------
String Report::toJSON()
{
  String json( L"{" );

  json.append( createJSON_Prop( L"reportID", uuidToWide(reportID) ) + L"," );

  // Measurement Set
  if ( measurementSet )
  {
    json.append( L"\"measurementSet\":" );
    json.append( measurementSet->toJSON() );

    json.append( L"," );
  }

  json.append( createJSON_Prop( L"reportDate", timeStampToString(reportDate) ) + L"," );

  json.append( createJSON_Prop( L"fromDate", timeStampToString(fromDate) ) + L"," );

  json.append( createJSON_Prop( L"toDate", timeStampToString(toDate) ) + L"," );

  json.append( createJSON_Prop( L"numberOfMeasurements", numberOfMeasurements ) );

  json.append( L"}" );

  return json;
}
void Report::fromJSON( const ModelBase::JSONTree& jsonTree )
{
}

String Report::toString()
{
  wstring ts;

  return ts;
}

} // namespace