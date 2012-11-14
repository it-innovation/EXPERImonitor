package uk.ac.itinnovation.soton.wegov.hansard;


/**
 * Represents Hansard HeadsUp Forum
 *
 */
public class Forum {
	private int id = 0;
	private String name = "";
	
	public Forum() {
		
	}
	
	public Forum(int id, String name) {
		this.id = id;
		this.name = name;
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
	
	@Override
	public String toString() {
		return "[" + id + "] " + name;
	}
}
