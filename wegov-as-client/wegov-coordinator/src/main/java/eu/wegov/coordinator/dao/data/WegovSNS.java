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
public class WegovSNS extends Dao {
    public static final String TABLE_NAME = "SNS";
    
    public WegovSNS() {
        this("", "", "", "", 0);
    }

    public WegovSNS(String snsID, String snsName, String apiUrl, String favicon, int outputOfRunID) {
        super(TABLE_NAME);
        properties.add(new Triplet("count_ID", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("ID", "character varying(60) NOT NULL", snsID));
        properties.add(new Triplet("SNS", "character varying(60) NOT NULL", snsName));
        properties.add(new Triplet("ApiUrl", "text", apiUrl));
        properties.add(new Triplet("Favicon", "text", favicon));
        properties.add(new Triplet("OutputOfRunID", "integer", outputOfRunID));
    }

    @Override
    public Dao createNew() {
        return new WegovSNS();
    }
    
    public String getCount_ID() {
        return getValueForKeyAsString("count_ID");
    }
    
    public String getID() {
        return getValueForKeyAsString("ID");
    }
    
    public String getSNS() {
        return getValueForKeyAsString("SNS");
    }    
    
    public String getApiUrl() {
        return getValueForKeyAsString("ApiUrl");
    }           
    
    public String getFavicon() {
        return getValueForKeyAsString("Favicon");
    }           
    
    public int getOutputOfRunID() {
        return getValueForKeyAsInt("OutputOfRunID");
    }    
}
