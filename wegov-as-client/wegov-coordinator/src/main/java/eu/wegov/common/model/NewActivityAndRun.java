package eu.wegov.common.model;

public class NewActivityAndRun {
	private int activityId;
	private int runId;
	private String error = null;
	
	public NewActivityAndRun(int activityId, int runId, String error) {
		this.activityId = activityId;
		this.runId = runId;
		this.error = error;
	}
	public int getActivityId() {
		return activityId;
	}
	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}
	public int getRunId() {
		return runId;
	}
	public void setRunId(int runId) {
		this.runId = runId;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}

}
