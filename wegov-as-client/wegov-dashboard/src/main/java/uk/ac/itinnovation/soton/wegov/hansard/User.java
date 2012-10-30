package uk.ac.itinnovation.soton.wegov.hansard;


/**
 * Represents Hansard HeadsUp User
 *
 */
public class User {
	private int id = 0;
	private String name = "";
	private String type = "";
	
	public User() {

	}

	public User(int id, String name, String type) {
		this.id = id;
		this.name = name;
		this.type = type;
		
		if (!name.contains(" "))
			this.type = "moderator";
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "[" + id + "] " + name + " (" + type + ")";
	}

}
