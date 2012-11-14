/////////////////////////////////////////////////////////////////////////
//
// ¬© University of Southampton IT Innovation Centre, 2011
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
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
//	Created By :			Maxim Bashevoy
//	Created Date :			2011-08-22
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator.dao.mgt;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.utils.Triplet;

/**
 *
 * @author Maxim Bashevoy
 */
public class WegovPolicymakerRole extends Dao {
    public static final String TABLE_NAME = "PolicymakerRoles";
    
    public WegovPolicymakerRole() {
        this("", "");
    }

    public WegovPolicymakerRole(String name, String description) {
        super(TABLE_NAME);
        properties.add(new Triplet("ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("Name", "character(256) NOT NULL", name));
        properties.add(new Triplet("Description", "text", description));
    }

    @Override
    public Dao createNew() {
        return new WegovPolicymakerRole();
    }
    
    @Override
    public String returning() {
        return "ID";
    }    
    
    public int getID() {
        return getValueForKeyAsInt("ID");
    }    
    
    public String getName() {
        return getValueForKeyAsString("Name");
    }
    
    public String getDescription() {
        return getValueForKeyAsString("Description");
    }

}
