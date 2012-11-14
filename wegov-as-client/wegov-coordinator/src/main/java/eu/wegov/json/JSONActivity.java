package eu.wegov.json;

import java.sql.Timestamp;

public class JSONActivity {
	private int id;
	private String name;
	private String comment;
	private String status;
	private Timestamp whenCreated;

	public JSONActivity(int id, String name, String comment, String status,
			Timestamp whenCreated) {
		this.id = id;
		this.name = name;
		this.comment = comment;
		this.status = status;
		this.whenCreated = whenCreated;
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

	public Timestamp getWhenCreated() {
		return whenCreated;
	}

	public void setWhenCreated(Timestamp whenCreated) {
		this.whenCreated = whenCreated;
	}

	
}