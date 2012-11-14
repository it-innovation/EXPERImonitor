package uk.ac.open.kmi.analysis.UserRoles;

import java.sql.Timestamp;


public class UserRole {
	private String userID;
	private String roleLabel;
	private Timestamp date;


	public Timestamp getDate() {
		return date;
	}

	public UserRole(UserFeatures user) {
		this.userID=user.getUserID();
	}

	public String getRoleLabel() {
		return roleLabel;
	}

	public void setRoleLabel(String roleLabel) {
		this.roleLabel = roleLabel;
	}
	
	public String getUserID() {
		return userID;
	}

	public void setDate(Timestamp date) {
		this.date = date;
		
	}

}
