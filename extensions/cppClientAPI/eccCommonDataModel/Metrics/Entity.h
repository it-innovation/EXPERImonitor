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

#pragma once

#include "Attribute.h"

#include <boost/uuid/uuid.hpp>

#include <hash_set>
#include <hash_map>




namespace ecc_commonDataModel
{

/**
 * An experiment can consist of many different entities that are monitored, some
 * of which could be system components, virtual resources or human beings. Each of
 * which have attributes that can be monitored, which are specified for each entity.
 * 
 * An entity can be described in terms of a name, description and a unique identifier.
 * It is also described in terms of observable attributes that can be monitored.
 * Any measurements produced by metric generators will refer to a specific attribute
 * of an entity.
 * 
 * Examples of entities include:
 *   - A person
 *   - A system component / service that could be used in many experiments
 *   - A virtual compute resource
 * 
 * @author Simon Crowle
 */
class Entity : ModelBase
{
public:

  typedef boost::shared_ptr<Entity> ptr_t;
  
  typedef boost::unordered_set<Entity::ptr_t> Set;
  
  typedef boost::unordered_map<UUID,Entity::ptr_t> Map;


  /**
    * Default constructor, which creates a random UUID for this object instance
    * and initialises the attributes and experimentUUIDs sets.
    */
  Entity();
    
  /**
    * A copy constructor; takes a deep copy of the attributes and experiment UUIDs.
    * @param e An Entity object from which a copy is made.
    */
  Entity( Entity::ptr_t e );
    
  /**
    * A constructor to set all the fields of the entity class except for the attributes.
    * @param uuid A UUID used to uniquely identify an entity in this framework.
    */
  Entity( const UUID& uuid );
    
  /**
    * A constructor to set all the fields of the entity class except for the attributes.
    * @param uuid A UUID used to uniquely identify an entity in this framework.
    * @param name The name of the Entity.
    * @param description A description of the entity.
    */
  Entity( const UUID&   uuid, 
          const String& name, 
          const String& description );
    
  /**
    * A constructor to set all the fields of the entity class.
    * @param uuid A UUID used to uniquely identify an entity in this framework.
    * @param name The name of the Entity.
    * @param description A description of the entity.
    * @param attributes A set of attributes of the entity, which could be observed to generate metrics.
    */
  Entity( const UUID&           uuid, 
          const String&         name, 
          const String&         description, 
          const Attribute::Set& attributes );
    
  /**
    * A constructor to set all the fields of the entity class except for the attributes.
    * @param uuid A UUID used to uniquely identify an entity in this framework.
    * @param entityID An ID that can be used if the Entity is known by a particular ID outside of the framework.
    * @param name The name of the Entity.
    * @param description A description of the entity.
    */
  Entity( const UUID&   uuid, 
          const String& entityID, 
          const String& name, 
          const String& description );
    
  /**
    * A constructor to set all the fields of the entity class.
    * @param uuid A UUID used to uniquely identify an entity in this framework.
    * @param entityID An ID that can be used if the Entity is known by a particular ID outside of the framework.
    * @param name The name of the Entity.
    * @param description A description of the entity.
    * @param attributes A set of attributes of the entity, which could be observed to generate metrics.
    */
  Entity( const UUID&           uuid, 
          const String&         entityID, 
          const String&         name, 
          const String&         description, 
          const Attribute::Set& attributes );

  virtual ~Entity();

  /**
   * Getter/Setter for the unique ID for this entity
   */
  UUID getUUID();

  void setUUID( const UUID& ID );

  /**
   * Getter/Setter for the human readable ID for this entity
   */
  String getEntityID();

  void setEntityID( const String& ID );

  /**
   * Getter/Setter for the name of this entity
   */
  String getName();

  void setName( const String& name );

  /**
   * Getter/Setter for the description of this entity
   */
  String getDescription();

  void setDescription( const String& desc );

  /**
   * Getter/Setter for the attributes belonging to this entity.
   */
  Attribute::Set getAttributes();
  
  void setAttributes( const Attribute::Set& attributes );
    
  /**
    * @param attribute the attribute to add
    */
  void addAttribute( Attribute::ptr_t attribute );
    
  /**
    * @param attributes the attributes to add
    */
  void addAttributes( const Attribute::Set& attributes );

  // ModelBase -----------------------------------------------------------------
  virtual void toJSON( String& jsonStrOUT );

  virtual void fromJSON( const String& jsonStr );

  virtual String toString();

private:

  UUID           entityUniqueID;
  String         entityID;
  String         entityName;
  String         entityDescription;
  Attribute::Set entityAttributes;

};

} // namespace
