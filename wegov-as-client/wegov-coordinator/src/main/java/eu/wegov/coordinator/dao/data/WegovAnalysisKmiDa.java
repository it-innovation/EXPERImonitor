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
//	Created Date :			2011-12-19
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
public class WegovAnalysisKmiDa extends Dao {
    public static final String TABLE_NAME = "AnalysisKmiDiscussionActivity";
    
    public WegovAnalysisKmiDa() {
        this(new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "", "", "", 0);
    }

    public WegovAnalysisKmiDa(Timestamp startTime, Timestamp endTime, String step, String posts, String values, int outputOfRunID) {
        super(TABLE_NAME);
        properties.add(new Triplet("count_ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("StartTime", "timestamp with time zone", startTime));
        properties.add(new Triplet("EndTime", "timestamp with time zone", endTime));
        properties.add(new Triplet("Step", "character varying(60)", step));
        properties.add(new Triplet("NumPosts", "character varying(60)", posts));
        properties.add(new Triplet("Values", "text", values));
        properties.add(new Triplet("OutputOfRunID", "integer NOT NULL", outputOfRunID));
    }

    @Override
    public Dao createNew() {
        return new WegovAnalysisKmiDa();
    }
    
    public String getCount_ID() {
        return getValueForKeyAsString("count_ID");
    }
    
    public Timestamp getStartTime() {
        return getValueForKeyAsTimestamp("StartTime");
    }          
    
    public Timestamp getEndTime() {
        return getValueForKeyAsTimestamp("EndTime");
    }          
    
    public String getStep() {
        return getValueForKeyAsString("Step");
    }           
    
    public String getNumPosts() {
        return getValueForKeyAsString("NumPosts");
    }           
    
    public String getValues() {
        return getValueForKeyAsString("Values");
    }
    
    public int getOutputOfRunID() {
        return getValueForKeyAsInt("OutputOfRunID");
    }    
}
