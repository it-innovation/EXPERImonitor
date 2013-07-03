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

#include "ModelBase.h"




namespace ecc_commonDataModel
{

/**
 * This class represents an observable attribute of an entity, which metrics can
 * be generated for.
 * 
 * @author Vegard Engen
 */
class Attribute : ModelBase
{
public:

  typedef boost::shared_ptr<Attribute> ptr_t;

  typedef boost::unordered_set<Attribute::ptr_t> Set;

  typedef boost::unordered_map<UUID,Attribute::ptr_t> Map;

  /**
    * Default constructor which sets a random UUID for the object instance.
    */
  Attribute();
    
  /**
    * Copy constructor.
    * @param attr The attribute object from which a copy is made.
    */
  Attribute( Attribute::ptr_t attr );
    
  /**
    * Constructor to set all the fields of the Attribute class.
    * @param uuid UUID used to uniquely identify an attribute in this framework.
    * @param entityUUID The UUID of the entity that this attribute is a part of.
    * @param name The name of the attribute.
    * @param description A description of the attribute.
    */
  Attribute( const UUID&   uuid, 
             const UUID&&  entityUUID, 
             const String& name, 
             const String& description );

  virtual ~Attribute();

  /**
   * Getter/Setter for the unique ID of this attribute
   */
  UUID getUUID();
  
  void setUUID( const UUID& ID );

  /**
   * Getter/Setter for the entity ID related to this attribute
   */
  UUID getEntityUUID();

  void setEntityUUID( const UUID& ID );

  /**
   * Getter/Setter for the name of this attribute
   */
  String getName();

  void setName( const String& name );

  /**
   * Getter/Setter for the description of this attribute
   */
  String& getDescription();

  void setDescription( const String& description );

  // ModelBase -----------------------------------------------------------------
  virtual void toJSON( String& jsonStrOUT );

  virtual void fromJSON( const String& jsonStr );

  virtual String toString();

private:

  UUID   attrID;
  UUID   entityID;
  String attrName;
  String attrDescription;

};

} // namespace
