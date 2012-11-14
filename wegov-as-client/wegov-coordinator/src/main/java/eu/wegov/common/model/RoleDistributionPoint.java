package eu.wegov.common.model;

import net.sf.json.JSONObject;

public class RoleDistributionPoint {
	private String roleName;
	private int numberOfUsers;


	public RoleDistributionPoint(String roleName, int numberOfUsers) {
		super();
		this.roleName = roleName;
		this.numberOfUsers = numberOfUsers;
	}


	public RoleDistributionPoint(JSONObject roleJSON) {

		this.roleName = roleJSON.getString("roleName");
		this.numberOfUsers = roleJSON.getInt("numberOfUsers");
	}

	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public int getNumberOfUsers() {
		return numberOfUsers;
	}
	public void setNumberOfUsers(int numberOfUsers) {
		this.numberOfUsers = numberOfUsers;
	}

}
