package eu.wegov.coordinator.dao.mgt;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.utils.Triplet;

public class WegovPolicymaker_Settings extends Dao {
    public static final String TABLE_NAME = "Policymakers_Settings";
    
    public WegovPolicymaker_Settings() {
        this(0, "", "");
    }

    public WegovPolicymaker_Settings(int policymakerID, String settingName, String settingValue) {
        super(TABLE_NAME);
        properties.add(new Triplet("PolicymakerID", "integer", policymakerID));
        properties.add(new Triplet("SettingName", "text", settingName));
        properties.add(new Triplet("SettingValue", "text", settingValue));
    }

    @Override
    public Dao createNew() {
        return new WegovPolicymaker_Settings();
    }
    
    @Override
    public String returning() {
        return "PolicymakerID";
    }
    
    public int getPolicymakerID() {
        return getValueForKeyAsInt("PolicymakerID");
    }
    
    public String getSettingName() {
        return getValueForKeyAsString("SettingName");
    }    
    
    public String getSettingValue() {
    	return getValueForKeyAsString("SettingValue");
    }    
}
