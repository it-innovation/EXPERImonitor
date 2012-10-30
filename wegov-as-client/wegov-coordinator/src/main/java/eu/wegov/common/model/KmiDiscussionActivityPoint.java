package eu.wegov.common.model;

import net.sf.json.JSONObject;

public class KmiDiscussionActivityPoint {
	private String time;
	private double value;

	public KmiDiscussionActivityPoint(String time, double value) {
		super();
		this.time = time;
		this.value = value;
	}

	public KmiDiscussionActivityPoint(JSONObject discussJSON) {

		this.time = discussJSON.getString("time");
		this.value = discussJSON.getDouble("value");
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

}
