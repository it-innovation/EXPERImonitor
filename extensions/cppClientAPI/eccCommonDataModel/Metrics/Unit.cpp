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
#include "Unit.h"

using namespace std;




namespace ecc_commonDataModel
{

Unit::Unit()
{
}

Unit::Unit( Unit::ptr_t u ) 
{
  if ( u ) unitName = u->getName();
}
    
Unit::Unit( const String& name ) 
{
  unitName = name;
}

Unit::~Unit()
{
}
    
String Unit::getName()
{
  return unitName;
}

void Unit::setName( const String& name )
{
  unitName = name;
}


// ModelBase -----------------------------------------------------------------
String Unit::toJSON()
{
  String json( L"{" );

  json.append( createJSON_Prop( L"name", unitName ) );

  json.append( L"}" );

  return json;
}

void Unit::fromJSON( const ModelBase::JSONTree& jsonTree )
{
  // Client does not require implementation
}

String Unit::toString()
{
  return unitName;
}
    
} // namespace