package uk.ac.itinnovation.soton.wegov.hansard;


/**
 * Represents Hansard HeadsUp Topic of a Forum
 *
 */
public class Thread {
	private int id = 0;
	private int forumId = 0;
	private String name = "";
	
	public Thread() {
		
	}
	
	public Thread(int id, int forumId, String name) {
		this.id = id;
		this.forumId = forumId;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getForumId() {
		return forumId;
	}

	public void setForumId(int forumId) {
		this.forumId = forumId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return "[" + id + "] from forum [" + forumId + "] " + name;
	}	
		
}
