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

#include <boost/uuid/uuid.hpp>


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

  /**
    * Default constructor which sets a random UUID for the object instance.
    */
  Attribute();
    
  /**
    * Copy constructor.
    * @param a The attribute object from which a copy is made.
    */
  Attribute( Attribute::ptr_t a );
    
  /**
    * Constructor to set all the fields of the Attribute class.
    * @param uuid UUID used to uniquely identify an attribute in this framework.
    * @param entityUUID The UUID of the entity that this attribute is a part of.
    * @param name The name of the attribute.
    * @param description A description of the attribute.
    */
  Attribute( const boost::uuids::uuid& uuid, 
             const boost::uuids::uuid& entityUUID, 
             const std::wstring&       name, 
             const std::wstring&       description );

  virtual ~Attribute();

  /**
   * Getter/Setter for the unique ID of this attribute
   */
  boost::uuids::uuid getUUID();
  
  void setUUID( const boost::uuids::uuid& ID );

  /**
   * Getter/Setter for the entity ID related to this attribute
   */
  boost::uuids::uuid getEntityUUID();

  void setEntityUUID( const boost::uuids::uuid& ID );

  /**
   * Getter/Setter for the name of this attribute
   */
  std::wstring getName();

  void setName( const std::wstring& name );

  /**
   * Getter/Setter for the description of this attribute
   */
  std::wstring& getDescription();

  void setDescription( const std::wstring& description );

  // ModelBase -----------------------------------------------------------------
  virtual void toJSON( std::wstring& jsonStrOUT );

  virtual void fromJSON( const std::wstring& jsonStr );

  virtual std::wstring toString();

private:

};

} // namespace
