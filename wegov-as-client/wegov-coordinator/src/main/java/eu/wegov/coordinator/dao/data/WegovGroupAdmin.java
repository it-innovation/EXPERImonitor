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
import java.sql.Timestamp;

/**
 *
 * @author Maxim Bashevoy
 */
public class WegovGroupAdmin extends Dao {
    public static final String TABLE_NAME = "GroupAdmins";
    
    public WegovGroupAdmin() {
        this("", "", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), 0);
    }

    public WegovGroupAdmin(String groupID, String adminUserID, Timestamp startDate, Timestamp endDate, int outputOfRunID) {
        super(TABLE_NAME);
        properties.add(new Triplet("count_ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("Group_SnsGroup_ID", "character varying(60) NOT NULL", groupID));
        properties.add(new Triplet("Admin_SnsUserAccount_ID", "character varying(60) NOT NULL", adminUserID));
        properties.add(new Triplet("StartDate", "timestamp with time zone", startDate));
        properties.add(new Triplet("EndDate", "timestamp with time zone", endDate));
        properties.add(new Triplet("OutputOfRunID", "integer", outputOfRunID));
    }

    @Override
    public Dao createNew() {
        return new WegovGroupAdmin();
    }
    
    public String getCount_ID() {
        return getValueForKeyAsString("count_ID");
    }
    
    public String getGroup_SnsGroup_ID() {
        return getValueForKeyAsString("Group_SnsGroup_ID");
    }    
    
    public String getAdmin_SnsUserAccount_ID() {
        return getValueForKeyAsString("Admin_SnsUserAccount_ID");
    }    
    
    public Timestamp getStartDate() {
        return getValueForKeyAsTimestamp("StartDate");
    }    
    
    public Timestamp getEndDate() {
        return getValueForKeyAsTimestamp("EndDate");
    }       
    
    public int getOutputOfRunID() {
        return getValueForKeyAsInt("OutputOfRunID");
    }     
}
