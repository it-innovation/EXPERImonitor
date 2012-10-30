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
public class WegovPostTag extends Dao {
    public static final String TABLE_NAME = "PostTags";
    
    public WegovPostTag() {
        this("", "", 0);
    }

    public WegovPostTag(String tagID, String postID, int outputOfRunID) {
        super(TABLE_NAME);
        properties.add(new Triplet("count_ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("Tag_Tag_ID", "character varying(60) NOT NULL", tagID));
        properties.add(new Triplet("Post_PostItem_ID", "character varying(60) NOT NULL", postID));
        properties.add(new Triplet("OutputOfRunID", "integer", outputOfRunID));
    }

    @Override
    public Dao createNew() {
        return new WegovPostTag();
    }
    
    public String getCount_ID() {
        return getValueForKeyAsString("count_ID");
    }
    
    public String getTag_Tag_ID() {
        return getValueForKeyAsString("Tag_Tag_ID");
    }    
    
    public String getPost_PostItem_ID() {
        return getValueForKeyAsString("Post_PostItem_ID");
    }          
    
    public int getOutputOfRunID() {
        return getValueForKeyAsInt("OutputOfRunID");
    }    
}
