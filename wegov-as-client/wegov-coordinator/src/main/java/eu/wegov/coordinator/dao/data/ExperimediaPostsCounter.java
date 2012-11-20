/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2012
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
//	Created Date :			2012-11-19
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////

package eu.wegov.coordinator.dao.data;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.utils.Triplet;
import java.sql.Timestamp;


public class ExperimediaPostsCounter extends Dao {
    public static final String TABLE_NAME = "ExperimediaPostsCounter";
    
    public ExperimediaPostsCounter() {
        this(0, new Timestamp(System.currentTimeMillis()), 0);
    }

    public ExperimediaPostsCounter(int num_posts,
                                   Timestamp collected_at,
                                   int outputOfRunID) {
        super(TABLE_NAME);
        properties.add(new Triplet("count_ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("num_posts", "integer", num_posts));
        properties.add(new Triplet("Collected_at", "timestamp with time zone", collected_at));
        properties.add(new Triplet("OutputOfRunID", "integer", outputOfRunID));
    }

    @Override
    public Dao createNew() {
        return new ExperimediaPostsCounter();
    }
    
    public Integer getNumPosts() {
        return getValueForKeyAsInt("num_posts");
    }    
    
    public String getTimeCollected() {
        return getValueForKeyAsString("Collected_at");
    }
    
    public Timestamp getTimeCollectedAsTimestamp() {
        return getValueForKeyAsTimestamp("Collected_at");
    }
    
    public Integer getOutputOfRunID() {
        return getValueForKeyAsInt("OutputOfRunID");
    }    

}
