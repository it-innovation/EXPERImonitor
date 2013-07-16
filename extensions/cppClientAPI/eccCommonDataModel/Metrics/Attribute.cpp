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
#include "Attribute.h"

using namespace boost::uuids;
using namespace std;




namespace ecc_commonDataModel
{

Attribute::Attribute()
{
  attrID = createRandomUUID();
}
    
Attribute::Attribute( Attribute::ptr_t attr )
{
  if ( attr )
  {
    attrID          = attr->getUUID();
    entityID        = attr->getEntityUUID();
    attrName        = attr->getName();
    attrDescription = attr->getDescription();
  }
}
    
Attribute::Attribute( const UUID&   uuid, 
                      const UUID&&  entityUUID, 
                      const String& name, 
                      const String& description )
{
  attrID          = uuid;
  entityID        = entityUUID;
  attrName        = name;
  attrDescription = description;
}

Attribute::~Attribute()
{
}

UUID Attribute::getUUID()
{
  return attrID;
}
  
void Attribute::setUUID( const UUID& ID )
{
  attrID = ID;
}

UUID Attribute::getEntityUUID()
{
  return entityID;
}

void Attribute::setEntityUUID( const UUID& ID )
{
  entityID = ID;
}

String Attribute::getName()
{
  return attrName;
}

void Attribute::setName( const String& name )
{
  attrName = name;
}

String& Attribute::getDescription()
{
  return attrDescription;
}

void Attribute::setDescription( const String& description )
{
  attrDescription = description;
}


// ModelBase -----------------------------------------------------------------
String Attribute::toJSON()
{
  String json( L"{" );

  json.append( createJSON_Prop( L"uuid", uuidToWide(attrID) ) + L"," );

  json.append( createJSON_Prop( L"entityUUID", uuidToWide(entityID) ) + L"," );

  json.append( createJSON_Prop( L"name", attrName ) + L"," );

  json.append( createJSON_Prop( L"description", attrDescription ) );

  json.append( L"}" );

  return json;
}

void Attribute::fromJSON( const ModelBase::JSONTree& jsonTree )
{
}

String Attribute::toString()
{
  wstring ts;

  return ts;
}

} // namespace
