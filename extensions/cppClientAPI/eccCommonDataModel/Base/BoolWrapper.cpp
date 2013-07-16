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
//      Created Date :          12-Jul-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#include "StdAfx.h"
#include "BoolWrapper.h"

using namespace ecc_commonDataModel;


BoolWrapper::BoolWrapper( const bool& value )
  : boolValue(value)
{
}

BoolWrapper::~BoolWrapper()
{
}

// ModelBase -----------------------------------------------------------------
String BoolWrapper::toJSON( )
{
  return boolValue ? L"true" : L"false";
}

void BoolWrapper::fromJSON( const JSONTree& jsonTree )
{
  JSONTreeIt tIt = jsonTree.begin(); // Get past method ID first

  String stringValue = getJSON_String( *tIt );

  if ( stringValue.compare( L"true" ) == 0 )
    boolValue = true;
  else
    boolValue = false;
}


String BoolWrapper::toString()
{
  return boolValue ? L"true" : L"false";
}
