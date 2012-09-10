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
//      Created By :            Vegard Engen
//      Created Date :          2012-09-10
//      Created for Project :   
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics;

/**
 * A simple class representing a unit by a name.
 * 
 * This will be extended later.
 * 
 * @author Vegard Engen
 */
public class Unit
{
    private String name;
    
    public Unit() {}
    
    /**
     * Copy constructor for the Unit.
     * @param u The object to copy from.
     */
    public Unit(Unit u)
    {
        if (u == null)
            return;
        
        this.name = u.getName();
    }
    
    public Unit(String name)
    {
        this.name = name;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    @Override
    public String toString()
    {
        if (this.name == null)
            return "null";
        return this.name;
    }
}
