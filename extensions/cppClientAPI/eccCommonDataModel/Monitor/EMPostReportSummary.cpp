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
#include "EMPostReportSummary.h"


using namespace boost::uuids;
using namespace std;


namespace ecc_commonDataModel
{

EMPostReportSummary::EMPostReportSummary()
  : ModelBase()
{
}

EMPostReportSummary::~EMPostReportSummary()
{
}

hash_set<uuid> EMPostReportSummary::getReportedMeasurementSetIDs()
{
  hash_set<uuid> reportIDs;

  // Copy UUIDs into a collection
  ReportMap::iterator repIt = reportsByMeasurementSetID.begin();
  while ( repIt != reportsByMeasurementSetID.end() )
  {
    reportIDs.insert( repIt->first );
    ++repIt;
  }

  return reportIDs;
}

void EMPostReportSummary::addReport( Report::ptr_t report )
    
{
  if ( report )
  {
    MeasurementSet::ptr_t ms = report->getMeasurementSet();

    if ( ms )
      reportsByMeasurementSetID.insert( make_pair<uuid,Report::ptr_t>( ms->getMSetID(), report ) );
  }
}

void EMPostReportSummary::removeReport( const uuid& measurementSetID )
{
  reportsByMeasurementSetID.erase( measurementSetID );
}

Report::ptr_t EMPostReportSummary::getReport( const uuid& measurementID )
{
  Report::ptr_t report;

  ReportMap::iterator target = reportsByMeasurementSetID.find( measurementID );

  if ( target != reportsByMeasurementSetID.end() )
    report = target->second;

  return report;
}

// ModelBase -----------------------------------------------------------------
void EMPostReportSummary::toJSON( wstring& jsonStrOUT )
{
}

void EMPostReportSummary::fromJSON( const wstring& jsonStr )
{
}

wstring EMPostReportSummary::toString()
{
  wstring ts;

  return ts;
}

} // namespace
