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
//	Created Date :			2011-07-28
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator.dao.data;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.utils.Triplet;

/**
 *
 * @author Maxim Bashevoy
 */
public class WegovSnsGroup extends Dao {
    public static final String TABLE_NAME = "SnsGroup";
    
    public WegovSnsGroup() {
        this("", "", "", "", "", "", 0);
    }

    public WegovSnsGroup(String groupID, String hostSnsID, String name, String description, String creatorUserID, String category, int outputOfRunID) {
        super(TABLE_NAME);
        properties.add(new Triplet("count_ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("ID", "character varying(60) NOT NULL", groupID));
        properties.add(new Triplet("HostSNS_SNS_ID", "character varying(60) NOT NULL", hostSnsID));
        properties.add(new Triplet("Name", "character varying(256) NOT NULL", name));
        properties.add(new Triplet("Description", "text", description));
        properties.add(new Triplet("Creator_SnsUserAccount_ID", "character varying(60) NOT NULL", creatorUserID));
        properties.add(new Triplet("Category", "character varying(256)", category));
        properties.add(new Triplet("OutputOfRunID", "integer", outputOfRunID));
    }

    @Override
    public Dao createNew() {
        return new WegovSnsGroup();
    }
    
    public String getCount_ID() {
        return getValueForKeyAsString("count_ID");
    }
    
    public String getID() {
        return getValueForKeyAsString("ID");
    }
    
    public String getHostSNS_SNS_ID() {
        return getValueForKeyAsString("HostSNS_SNS_ID");
    }    
    
    public String getName() {
        return getValueForKeyAsString("User_SnsUserAccount_ID");
    }           
    
    public String getDescription() {
        return getValueForKeyAsString("Description");
    }           
    
    public String getCreator_SnsUserAccount_ID() {
        return getValueForKeyAsString("Creator_SnsUserAccount_ID");
    }           
    
    public String getCategory() {
        return getValueForKeyAsString("Category");
    }           
    
    public int getOutputOfRunID() {
        return getValueForKeyAsInt("OutputOfRunID");
    }    
}
