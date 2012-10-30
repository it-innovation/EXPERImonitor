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

/**
 *
 * @author Maxim Bashevoy
 */
public class WegovRun_ConfigurationSet extends Dao {
    public static final String TABLE_NAME = "Runs_ConfigurationSets";
    
    public WegovRun_ConfigurationSet() {
        this(1, 1);
    }

    public WegovRun_ConfigurationSet(int runID, int configurationSetID) {
        super(TABLE_NAME);
        properties.add(new Triplet("RunID", "integer", runID));
        properties.add(new Triplet("ConfigurationSetID", "integer", configurationSetID));
    }

    @Override
    public Dao createNew() {
        return new WegovRun_ConfigurationSet();
    }
    
    @Override
    public String returning() {
        return "RunID";
    }
    
    public int getActivityID() {
        return getValueForKeyAsInt("RunID");
    }
    
    public int getConfigurationSetID() {
        return getValueForKeyAsInt("ConfigurationSetID");
    }
    
}
