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

/**
 * A simple class representing a unit by a name.
 * 
 * This will be extended later.
 * 
 * @author Vegard Engen
 */
//class Unit
//{
//    public Unit() {}
//    
//    /**
//     * Copy constructor for the Unit.
//     * @param u The object to copy from.
//     */
//    public Unit(Unit u)
//    {
//        if (u == null)
//            return;
//        
//        this.name = u.name;
//    }
//    
//    public Unit(string name)
//    {
//        this.name = name;
//    }
//
//    public string name
//    {
//        get;
//        set;
//    }
//    
//    public string toString()
//    {
//        if (this.name == null)
//            return "null";
//
//        return this.name;
//    }
//};


// ModelBase -----------------------------------------------------------------
void Unit::toJSON( wstring& jsonStrOUT )
{
}

void Unit::fromJSON( const wstring& jsonStr )
{
}

wstring Unit::toString()
{
  wstring ts;

  return ts;
  }
    
} // namespace