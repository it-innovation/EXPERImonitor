package eu.wegov.coordinator.dao.mgt;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.utils.Triplet;

public class WegovWidgetSet extends Dao {
    public static final String TABLE_NAME = "WidgetSets";
    
    public WegovWidgetSet() {
        this(0, "", "", 0);
    }

    public WegovWidgetSet(int policymakerId, String name, String description, int isDefault) {
        super(TABLE_NAME);
        properties.add(new Triplet("id", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("policymakerId", "integer", policymakerId));
        properties.add(new Triplet("name", "character varying(256) NOT NULL", name));
        properties.add(new Triplet("description", "character varying(256) NOT NULL", description));
        properties.add(new Triplet("isDefault", "integer", isDefault));
    }

    @Override
    public Dao createNew() {
        return new WegovWidgetSet();
    }
    
    public int getId() {
    	return getValueForKeyAsInt("id");
    }           
    
    public int getPolicymakerId() {
    	return getValueForKeyAsInt("policymakerId");
    }
    
    public String getName() {
        return getValueForKeyAsString("name");
    }           
    
    public String getDescription() {
    	return getValueForKeyAsString("description");
    }           
    
    public int isDefault() {
    	return getValueForKeyAsInt("isDefault");
    }
    
    @Override
    public String returning() {
    	// TODO Auto-generated method stub
    	return "id";
    }
   
}
