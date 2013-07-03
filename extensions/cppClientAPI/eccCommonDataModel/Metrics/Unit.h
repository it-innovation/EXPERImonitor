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

#pragma once

#include "ModelBase.h"


namespace ecc_commonDataModel
{

    /**
     * A simple class representing a unit by a name.
     * 
     * This will be extended later.
     * 
     * @author Vegard Engen
     */
    class Unit : ModelBase
    {
    public:

        typedef boost::shared_ptr<Unit> ptr_t;

        Unit();

        /**
         * Copy constructor for the Unit.
         * @param u The object to copy from.
         */
        Unit( Unit::ptr_t u );
    
        /**
         * Unit constructor with name
         */
        Unit( const String& name );

        virtual ~Unit();
    
        
        /**
         * Getter/Setter for name
         */
        String getName();

        void setName( const String& name );

        // ModelBase -----------------------------------------------------------------
        virtual void toJSON( String& jsonStrOUT );

        virtual void fromJSON( const String& jsonStr );

        virtual String toString();

    private:
      String unitName;

    };
    
} // namespace