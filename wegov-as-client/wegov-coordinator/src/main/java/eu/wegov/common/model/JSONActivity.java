package eu.wegov.common.model;

public class JSONActivity {
	private int id;
	private String name;
	private String comment;
	private String status;
	private String whenCreated;
	private String nextStartTime;

	public JSONActivity(int id, String name, String comment, String status,
			String whenCreated, String nextStartTime) {
		this.id = id;
		this.name = name;
		this.comment = comment;
		this.status = status;
		this.whenCreated = whenCreated;
		this.nextStartTime = nextStartTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public String getWhenCreated() {
		return whenCreated;
	}

	public void setWhenCreated(String whenCreated) {
		this.whenCreated = whenCreated;
	}

	public String getNextStartTime() {
		return nextStartTime;
	}

	public void setNextStartTime(String nextStartTime) {
		this.nextStartTime = nextStartTime;
	}
	
}