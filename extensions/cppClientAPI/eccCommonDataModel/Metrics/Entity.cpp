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
#include "Entity.h"

using namespace boost::uuids;
using namespace std;




namespace ecc_commonDataModel
{

Entity::Entity()
{
  entityUniqueID = createRandomUUID();
}
    
Entity::Entity( Entity::ptr_t ent )
{
  if ( ent )
  {
    entityUniqueID    = ent->getUUID();
    entityID          = ent->getEntityID();
    entityName        = ent->getName();
    entityDescription = ent->getDescription();

    Attribute::Set srcAtts = ent->getAttributes();
    entityAttributes.insert( srcAtts.begin(), srcAtts.end() );
  }
}
    
Entity::Entity( const UUID& uuid )
{
  entityUniqueID = uuid;
}

Entity::Entity( const UUID&   uuid, 
                const String& name, 
                const String& description )
{
  entityUniqueID    = uuid;
  entityName        = name;
  entityDescription = description;
}
    
Entity::Entity( const UUID&           uuid, 
                const String&         name, 
                const String&         description, 
                const Attribute::Set& atts )
{
  entityUniqueID    = uuid;
  entityName        = name;
  entityDescription = description;

  Attribute::Set::const_iterator attIt = atts.begin();
  while ( attIt != atts.end() )
  {
    Attribute::ptr_t attr = *attIt;

    if ( attr ) entityAttributes.insert( attr );

    ++attIt;
  }
}
    
Entity::Entity( const UUID&   uuid, 
                const String& entID, 
                const String& name, 
                const String& description )
{
  entityUniqueID    = uuid;
  entityID          = entID;
  entityName        = name;
  entityDescription = description;
}
    
Entity::Entity( const UUID&           uuid, 
                const String&         entID, 
                const String&         name, 
                const String&         description, 
                const Attribute::Set& atts )
{
  entityUniqueID    = uuid;
  entityID          = entID;
  entityName        = name;
  entityDescription = description;

  Attribute::Set::const_iterator attIt = atts.begin();
  while ( attIt != atts.end() )
  {
    Attribute::ptr_t attr = *attIt;

    if ( attr ) entityAttributes.insert( attr );

    ++attIt;
  }
}

Entity::~Entity()
{
}

UUID Entity::getUUID()
{
  return entityUniqueID;
}

void Entity::setUUID( const UUID& ID )
{
  entityUniqueID = ID;
}

String Entity::getEntityID()
{
  return entityID;
}

void Entity::setEntityID( const String& ID )
{
  entityID = ID;
}

String Entity::getName()
{
  return entityName;
}

void Entity::setName( const String& name )
{
  entityName = name;
}

String Entity::getDescription()
{
  return entityDescription;
}

void Entity::setDescription( const String& desc )
{
  entityDescription = desc;
}

Attribute::Set Entity::getAttributes()
{
  return entityAttributes;
}
  
void Entity::setAttributes( const Attribute::Set& attributes )
{
  entityAttributes = attributes;
}
    
void Entity::addAttribute( Attribute::ptr_t attribute )
{
  if ( attribute )
    entityAttributes.insert( attribute );
}
    
void Entity::addAttributes( const Attribute::Set& attributes )
{
  Attribute::Set::const_iterator attIt = attributes.begin();

  while ( attIt != attributes.end() )
  {
    Attribute::ptr_t attr = *attIt;

    if ( attr ) entityAttributes.insert( attr );

    ++attIt;
  }
}

// ModelBase -----------------------------------------------------------------
String Entity::toJSON()
{
  String json( L"{" );

  json.append( createJSON_Prop( L"uuid", uuidToWide(entityUniqueID) ) + L"," );

  json.append( createJSON_Prop( L"entityID", entityID ) + L"," );

  json.append( createJSON_Prop( L"name", entityName ) + L"," );

  json.append( createJSON_Prop( L"description", entityDescription ) + L"," );

  // Attributes
  json.append( L"\"attributes\":[" );

  Attribute::Set::const_iterator atIt = entityAttributes.begin();
  while ( atIt != entityAttributes.end() )
  {
    json.append( (*atIt)->toJSON() + L"," );

    ++atIt;
  }

  // Snip off trailing delimiter
  if ( !entityAttributes.empty() )
  {
    unsigned int jLen = json.length();
    json = json.substr( 0, jLen-1 );
  }

  json.append( L"]" );

  json.append( L"}" );

  return json;
}

void Entity::fromJSON( const ModelBase::JSONTree& jsonTree )
{
  // Client does not require implementation
}

String Entity::toString()
{
  return entityName + L" {" + uuidToWide(entityUniqueID) + L"}";
}

} // namespace
