package eu.wegov.common.model;

public class JSONRun {
	private int id;
	private int activityid;
	private String name;
	private String comment;
	private String status;
	private String whenStarted;
	private String whenFinished;
	private String results;

	public JSONRun(int id, int activityid, String name, String comment,
			String status, String whenStarted, String whenFinished, String results) {
		super();
		this.id = id;
		this.activityid = activityid;
		this.name = name;
		this.comment = comment;
		this.status = status;
		this.whenStarted = whenStarted;
		this.whenFinished = whenFinished;
		this.results = results;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getActivityid() {
		return activityid;
	}

	public void setActivityid(int activityid) {
		this.activityid = activityid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getWhenStarted() {
		return whenStarted;
	}

	public void setWhenStarted(String whenStarted) {
		this.whenStarted = whenStarted;
	}

	public String getWhenFinished() {
		return whenFinished;
	}

	public void setWhenFinished(String whenFinished) {
		this.whenFinished = whenFinished;
	}

	public String getResults() {
		return results;
	}

	public void setResults(String results) {
		this.results = results;
	}

}