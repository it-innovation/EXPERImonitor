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
//	Created Date :			2011-09-05
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
public class WegovRun extends Dao {
    public static final String TABLE_NAME = "Runs";
    
    public WegovRun() {
        this("", "", "", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
    }

    public WegovRun(String name, String comment, String status, Timestamp whenStarted, Timestamp whenFinished) {
        super(TABLE_NAME);
        properties.add(new Triplet("ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("Name", "text", name));
        properties.add(new Triplet("Comment", "text", comment));
        properties.add(new Triplet("Status", "character varying(60)", status));
        properties.add(new Triplet("Started", "timestamp with time zone", whenStarted));
        properties.add(new Triplet("Finished", "timestamp with time zone", whenFinished));
    }

    @Override
    public Dao createNew() {
        return new WegovRun();
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
    
    public String getComment() {
        return getValueForKeyAsString("Comment");
    }
    
    public String getStatus() {
        return getValueForKeyAsString("Status");
    }
    
    public Timestamp getWhenStarted() {
        return getValueForKeyAsTimestamp("Started");
    }
    
    public Timestamp getWhenFinished() {
        return getValueForKeyAsTimestamp("Finished");
    }
}
