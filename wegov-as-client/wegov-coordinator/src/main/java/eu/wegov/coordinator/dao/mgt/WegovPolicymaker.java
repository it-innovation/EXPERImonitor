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
//	Created Date :			2011-07-26
//	Created for Project :           WeGov
//
/////////////////////////////////////////////////////////////////////////
package eu.wegov.coordinator.dao.mgt;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.utils.Triplet;
import java.sql.Timestamp;

/**
 *
 * @author Maxim Bashevoy
 */
public class WegovPolicymaker extends Dao {
    public static final String TABLE_NAME = "Policymakers";
    
    public WegovPolicymaker() {
        this("", "", "", "123", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
    }  
    
    public WegovPolicymaker(String name, String organisation, String username, String passwordHash) {
        this(name, organisation, username, passwordHash, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
    }    
       
    public WegovPolicymaker(String name, String organisation, String username, String passwordHash, Timestamp startDate, Timestamp endDate) {
        super(TABLE_NAME);
        properties.add(new Triplet("ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("Name", "character varying(256) NOT NULL", name));
        properties.add(new Triplet("Organisation", "character varying(256)", organisation));
        properties.add(new Triplet("Username", "character varying(256) NOT NULL", username));
        properties.add(new Triplet("PasswordHash", "character varying(40)", passwordHash));
        properties.add(new Triplet("StartDate", "timestamp with time zone", startDate));
        properties.add(new Triplet("EndDate", "timestamp with time zone", endDate));
    }

    @Override
    public Dao createNew() {
        return new WegovPolicymaker();
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
    
    public String getUsername() {
        return getValueForKeyAsString("Username");
    }
    
    public String getOrganisation() {
        return getValueForKeyAsString("Organisation");
    }
    
    public String getPasswordHash() {
        return getValueForKeyAsString("PasswordHash");
    }
    
    public Timestamp getStartDate() {
        return getValueForKeyAsTimestamp("StartDate");
    }
    
    public Timestamp getEndDate() {
        return getValueForKeyAsTimestamp("EndDate");
    }
}
