package eu.wegov.common.model;

public class JSONUserDetails {

	String name;
	String role;
	String username;
	String organisation;
	int id;

	public JSONUserDetails(String name, String role, String username,
			String organisation, int id) {
		super();
		this.name = name;
		this.role = role;
		this.username = username;
		this.organisation = organisation;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getOrganisation() {
		return organisation;
	}

	public void setOrganisation(String organisation) {
		this.organisation = organisation;
	}

}
