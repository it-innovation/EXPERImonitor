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
//	Created Date :			2011-08-25
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
public class WegovConfiguration_Parameter extends Dao {
    public static final String TABLE_NAME = "Configurations_Parameters";
    
    public WegovConfiguration_Parameter() {
        this(1, 1);
    }

    public WegovConfiguration_Parameter(int configurationID, int parameterID) {
        super(TABLE_NAME);
        properties.add(new Triplet("ConfigurationID", "integer", configurationID));
        properties.add(new Triplet("ParameterID", "integer", parameterID));
    }

    @Override
    public Dao createNew() {
        return new WegovConfiguration_Parameter();
    }
    
    @Override
    public String returning() {
        return "ConfigurationID";
    }
    
    public int getConfigurationID() {
        return getValueForKeyAsInt("ConfigurationID");
    }
    
    public int getParameterID() {
        return getValueForKeyAsInt("ParameterID");
    }    
}
