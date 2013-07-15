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
#include "EMDataBatch.h"

using namespace boost::uuids;
using namespace std;



namespace ecc_commonDataModel
{

EMDataBatch::EMDataBatch()
{
}

EMDataBatch::~EMDataBatch()
{
}


UUID EMDataBatch::getID()
{
  return batchID;
}

void EMDataBatch::setID( const UUID& ID )
{
  batchID = ID;
}
    
TimeStamp EMDataBatch::getExpectedStartStamp()
{
  return expectedStartStamp;
}

void EMDataBatch::setExpectedStartStamp( const TimeStamp& time )
{
  expectedStartStamp = time;
}

int EMDataBatch::getExpectedMeasurementCount()
{
  return expectedMeasurementCount;
}

void EMDataBatch::setExpectedMeasurementCount( const int& count )
{
  expectedMeasurementCount = count;
}
  
UUID EMDataBatch::getExpectedMeasurementSetID()
{
  return expectedMeasurementSetID;
}

void EMDataBatch::setExpectedMeasurementSetID( const UUID& ID )
{
  expectedMeasurementSetID = ID;
}
  

Report::ptr_t EMDataBatch::getBatchReport()
{
  return batchReport;
}

void EMDataBatch::setBatchReport( Report::ptr_t report )
{
  batchReport = report;
}

// ModelBase -----------------------------------------------------------------
String EMDataBatch::toJSON()
{
  String json;

  return json;
}

void EMDataBatch::fromJSON( const JSONTree& jsonTree )
{
}

String EMDataBatch::toString()
{
  wstring ts;

  return ts;
}

} // namespace