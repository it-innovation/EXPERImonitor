package eu.wegov.coordinator.dao.mgt;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.utils.Triplet;

public class WegovWidget extends Dao {
    public static final String TABLE_NAME = "Widgets";

    public WegovWidget() {
        this(0, "", 0, 0, "", "", "", "", "", "", "", 0, "");
    }

    public WegovWidget(int widgetsetid, String columnName, int columnordernum, int policymakerId, String name, String description, String category, String type, String datatype, String dataAsString, String parametersAsString, int isVisible, String labelText) {
        super(TABLE_NAME);
        properties.add(new Triplet("id", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("widgetsetid", "integer", widgetsetid));
        properties.add(new Triplet("columnName", "character varying(256) NOT NULL", columnName));
        properties.add(new Triplet("columnordernum", "integer", columnordernum));
        properties.add(new Triplet("policymakerId", "integer", policymakerId));
        properties.add(new Triplet("name", "character varying(256) NOT NULL", name));
        properties.add(new Triplet("description", "text", description));
        properties.add(new Triplet("widgetCategory", "character varying(256) NOT NULL", category));
        properties.add(new Triplet("type", "character varying(256) NOT NULL", type));
        properties.add(new Triplet("datatype", "character varying(256) NOT NULL", datatype));
        properties.add(new Triplet("dataAsString", "text", dataAsString));
        properties.add(new Triplet("parametersAsString", "text", parametersAsString));
        properties.add(new Triplet("labelText", "text", labelText));

        // visible = 0, not = 1
        properties.add(new Triplet("isVisible", "integer", isVisible));
    }

    @Override
    public Dao createNew() {
        return new WegovWidget();
    }

    public int getId() {
    	return getValueForKeyAsInt("id");
    }

    public int getWidgetsetId() {
    	return getValueForKeyAsInt("widgetsetid");
    }

    public String getColumnName() {
    	return getValueForKeyAsString("columnName");
    }

    public int getColumnOrderNumber() {
    	return getValueForKeyAsInt("columnordernum");
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

    public String getWidgetCategory() {
    	return getValueForKeyAsString("widgetCategory");
    }

    public String getType() {
    	return getValueForKeyAsString("type");
    }

    public String getDatatype() {
    	return getValueForKeyAsString("datatype");
    }

    public String getDataAsString() {
    	return getValueForKeyAsString("dataAsString");
    }

    public String getParametersAsString() {
    	return getValueForKeyAsString("parametersAsString");
    }

    public int isVisible() {
    	return getValueForKeyAsInt("isVisible");
    }

    public String getLabelText() {
    	return getValueForKeyAsString("labelText");
    }


    @Override
    public String returning() {
    	return "id";
    }

}