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

/**
 * This class represents an observable attribute of an entity, which metrics can
 * be generated for.
 * 
 * @author Vegard Engen
 */
//class Attribute
//{
//    /**
//     * Default constructor which sets a random UUID for the object instance.
//     */
//    public Attribute()
//    {
//        this.uuid = Guid.NewGuid();
//    }
//    
//    /**
//     * Copy constructor.
//     * @param a The attribute object from which a copy is made.
//     */
//    public Attribute(Attribute a)
//    {
//        if (a == null)
//            return;
//        
//        if (a.uuid != null)
//            this.uuid = new Guid( a.uuid.ToString() );
//
//        if (a.entityUUID != null)
//            this.entityUUID = new Guid( a.entityUUID.ToString() );
//
//        this.name = a.name;
//        this.description = a.description;
//    }
//    
//    /**
//     * Constructor to set all the fields of the Attribute class.
//     * @param uuid UUID used to uniquely identify an attribute in this framework.
//     * @param entityUUID The UUID of the entity that this attribute is a part of.
//     * @param name The name of the attribute.
//     * @param description A description of the attribute.
//     */
//    public Attribute(Guid uuid, Guid entityUUID, string name, string description)
//    {
//        this.uuid = uuid;
//        this.entityUUID = entityUUID;
//        this.name = name;
//        this.description = description;
//    }
//
//    public Guid uuid
//    {
//        get;
//        set;
//    }
//
//    public Guid entityUUID
//    {
//        get;
//        set;
//    }
//
//    public string name
//    {
//        get;
//        set;
//    }
//
//    public string description
//    {
//        get;
//        set;
//    }
//
//    public string toString()
//    {
//        return name;
//    }
//};

// ModelBase -----------------------------------------------------------------
void Attribute::toJSON( wstring& jsonStrOUT )
{
}

void Attribute::fromJSON( const wstring& jsonStr )
{
}

wstring Attribute::toString()
{
  wstring ts;

  return ts;
}

} // namespace
