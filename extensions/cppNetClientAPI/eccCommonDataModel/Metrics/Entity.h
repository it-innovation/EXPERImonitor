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
class Entity
{
public:

  typedef boost::shared_ptr<Entity> ptr_t;

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
  Entity( const boost::uuids::uuid& uuid );
    
  /**
    * A constructor to set all the fields of the entity class except for the attributes.
    * @param uuid A UUID used to uniquely identify an entity in this framework.
    * @param name The name of the Entity.
    * @param description A description of the entity.
    */
  Entity( const boost::uuids::uuid& uuid, 
          const std::wstring&       name, 
          const std::wstring&       description );
    
  /**
    * A constructor to set all the fields of the entity class.
    * @param uuid A UUID used to uniquely identify an entity in this framework.
    * @param name The name of the Entity.
    * @param description A description of the entity.
    * @param attributes A set of attributes of the entity, which could be observed to generate metrics.
    */
  Entity( const boost::uuids::uuid&              uuid, 
          const std::wstring&                    name, 
          const std::wstring&                    description, 
          const std::hash_set<Attribute::ptr_t>& attributes );
    
  /**
    * A constructor to set all the fields of the entity class except for the attributes.
    * @param uuid A UUID used to uniquely identify an entity in this framework.
    * @param entityID An ID that can be used if the Entity is known by a particular ID outside of the framework.
    * @param name The name of the Entity.
    * @param description A description of the entity.
    */
  Entity( const boost::uuids::uuid& uuid, 
          const std::wstring&       entityID, 
          const std::wstring&       name, 
          const std::wstring&       description );
    
  /**
    * A constructor to set all the fields of the entity class.
    * @param uuid A UUID used to uniquely identify an entity in this framework.
    * @param entityID An ID that can be used if the Entity is known by a particular ID outside of the framework.
    * @param name The name of the Entity.
    * @param description A description of the entity.
    * @param attributes A set of attributes of the entity, which could be observed to generate metrics.
    */
  Entity( const boost::uuids::uuid&             uuid, 
          const std::wstring&                   entityID, 
          const std::wstring&                   name, 
          const std::wstring&                   description, 
          const std::hash_set<Attribute::ptr_t> attributes );

  virtual ~Entity();

  /**
   * Getter/Setter for the unique ID for this entity
   */
  boost::uuids::uuid getUUID();

  void setUUID( const boost::uuids::uuid& ID );

  /**
   * Getter/Setter for the human readable ID for this entity
   */
  std::wstring getEntityID();

  void setEntityID( const std::wstring& ID );

  /**
   * Getter/Setter for the name of this entity
   */
  std::wstring getName();

  void setName( const std::wstring& name );

  /**
   * Getter/Setter for the description of this entity
   */
  std::wstring getDescription();

  void setDescription( const std::wstring& desc );

  /**
   * Getter/Setter for the attributes belonging to this entity.
   */
  std::hash_set<Attribute::ptr_t> getAttributes();
  
  void setAttributes( std::hash_set<Attribute::ptr_t> attributes );
    
  /**
    * @param attribute the attribute to add
    */
  void addAttribute( Attribute::ptr_t attribute );
    
  /**
    * @param attributes the attributes to add
    */
  void addAttributes( std::hash_map<boost::uuids::uuid, Attribute::ptr_t> attributes );

private:

};

} // namespace
