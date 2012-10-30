package eu.wegov.coordinator.dao.data;

import java.sql.Timestamp;

import eu.wegov.coordinator.dao.Dao;
import eu.wegov.coordinator.sql.PostgresConnector;
import eu.wegov.coordinator.utils.Triplet;
import org.apache.log4j.Logger;

public class WegovWidgetDataAsJson extends Dao {
    public static final String TABLE_NAME = "WidgetDataAsJson";
    private final static Logger logger = Logger.getLogger(WegovWidgetDataAsJson.class.getName());

    public WegovWidgetDataAsJson() {
        this(0, 0, 0, 0, "", "", "", 0, "", "", null, null, "", new Timestamp(System.currentTimeMillis()));
    }

    public WegovWidgetDataAsJson(int widgetid, int pmid, int activityid, int runid, String type, String name, String location, int nResults, String minId, String maxId, Timestamp minTs, Timestamp maxTs, String dataAsJson, Timestamp collected_at) {
        super(TABLE_NAME);
        properties.add(new Triplet("id", "SERIAL PRIMARY KEY", ""));
        properties.add(new Triplet("widgetid", "integer", widgetid));
        properties.add(new Triplet("pmid", "integer", pmid));
        properties.add(new Triplet("activityid", "integer", activityid));
        properties.add(new Triplet("runid", "integer", runid));
        properties.add(new Triplet("type", "text", type));
        properties.add(new Triplet("name", "text", name));
        properties.add(new Triplet("location", "text", location));
        properties.add(new Triplet("nResults", "integer", nResults));
        properties.add(new Triplet("minId", "character varying(60)", minId));
        properties.add(new Triplet("maxId", "character varying(60)", maxId));
        properties.add(new Triplet("minTs", "timestamp with time zone", minTs));
        properties.add(new Triplet("maxTs", "timestamp with time zone", maxTs));
        properties.add(new Triplet("dataAsJson", "text", dataAsJson));
        properties.add(new Triplet("collected_at", "timestamp with time zone", collected_at));

        logger.debug("Widget Data as JSON data = " + dataAsJson);

        //System.out.println ("Widget Data as JSON data = " + dataAsJson);
    }

    @Override
    public Dao createNew() {
        return new WegovWidgetDataAsJson();
    }

    public int getId() {
    	return getValueForKeyAsInt("id");
    }

    public int getWidgetId() {
    	return getValueForKeyAsInt("widgetid");
    }

    public int getPmId() {
    	return getValueForKeyAsInt("pmid");
    }

    public int getActivityId() {
    	return getValueForKeyAsInt("activityid");
    }

    public int getRunId() {
    	return getValueForKeyAsInt("runid");
    }

    public String getType() {
    	return getValueForKeyAsString("type");
    }

    public String getName() {
    	return getValueForKeyAsString("name");
    }

    public String getLocation() {
    	return getValueForKeyAsString("location");
    }

    public int getNumResults() {
    	return getValueForKeyAsInt("nResults");
    }

    public String getMinId() {
    	return getValueForKeyAsString("minId");
    }

    public String getMaxId() {
    	return getValueForKeyAsString("maxId");
    }

    public Timestamp getMinTimestamp() {
        return getValueForKeyAsTimestamp("minTs");
    }

    public Timestamp getMaxTimestamp() {
        return getValueForKeyAsTimestamp("maxTs");
    }

    public String getDataAsJson() {
    	return getValueForKeyAsString("dataAsJson");
    }

    public Timestamp getTimeCollected() {
        return getValueForKeyAsTimestamp("collected_at");
    }

    @Override
    public String returning() {
    	return "id";
    }

}