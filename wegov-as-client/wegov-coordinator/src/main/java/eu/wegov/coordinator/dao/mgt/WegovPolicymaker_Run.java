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
public class WegovPolicymaker_Run extends Dao {
    public static final String TABLE_NAME = "Policymakers_Runs";
    
    public WegovPolicymaker_Run() {
        this(1, 1);
    }

    public WegovPolicymaker_Run(int policymakerID, int runID) {
        super(TABLE_NAME);
        properties.add(new Triplet("PolicymakerID", "integer", policymakerID));
        properties.add(new Triplet("RunID", "integer", runID));
    }

    @Override
    public Dao createNew() {
        return new WegovPolicymaker_Run();
    }
    
    @Override
    public String returning() {
        return "PolicymakerID";
    }
    
    public int getPolicymakerID() {
        return getValueForKeyAsInt("PolicymakerID");
    }
    
    public int getRunID() {
        return getValueForKeyAsInt("RunID");
    }    
}
