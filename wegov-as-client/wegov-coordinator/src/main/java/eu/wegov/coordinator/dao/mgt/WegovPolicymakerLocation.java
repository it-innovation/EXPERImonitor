package eu.wegov.coordinator.dao.mgt;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.utils.Triplet;

public class WegovPolicymakerLocation extends Dao {
    public static final String TABLE_NAME = "WegovUserLocations";
    
    public WegovPolicymakerLocation() {
        this(0, "", "", "", "");
    }

    public WegovPolicymakerLocation(int pmId, String locationName, String locationAddress, String lat, String lon) {
        super(TABLE_NAME);
        properties.add(new Triplet("id", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("pmId", "integer", pmId));
        properties.add(new Triplet("locationName", "text", locationName));
        properties.add(new Triplet("locationAddress", "text", locationAddress));
        properties.add(new Triplet("lat", "text", lat));
        properties.add(new Triplet("lon", "text", lon));

    }

    @Override
    public Dao createNew() {
        return new WegovPolicymakerLocation();
    }
    
    public int getId() {
    	return getValueForKeyAsInt("id");
    }           
    
    public int getPolicymakerId() {
    	return getValueForKeyAsInt("pmId");
    }
    
    public String getLocationName() {
    	return getValueForKeyAsString("locationName");
    }     
    
    public String getLocationAddress() {
    	return getValueForKeyAsString("locationAddress");
    }
    
    public String getLat() {
    	return getValueForKeyAsString("lat");
    }
    
    public String getLon() {
    	return getValueForKeyAsString("lon");
    }

    
    @Override
    public String returning() {
    	return "id";
    }
   
}