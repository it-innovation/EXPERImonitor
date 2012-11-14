package eu.wegov.common.model;

import net.sf.json.JSONObject;

public class KmiPost {
	private String id;
	private String userId;
	private String userScreenName;
	private String userFullName;
	private String userProfileImageUrl;
	private String createdAt;
	private String inReplyToID;
	private String textContent;
	private double authorOutDegree; // Number of Users the author follows
	private double authorInDegree; // Number of Users that follow the author
	private double authorNumLists; // Number of lists the author belongs
	private String buzzScore;

	public KmiPost(String id, String userId, String userScreenName,
			String userFullName, String userProfileImageUrl, String createdAt,
			String inReplyToID, String textContent, double authorOutDegree,
			double authorInDegree, double authorNumLists, String buzzScore) {
		super();
		this.id = id;
		this.userId = userId;
		this.userScreenName = userScreenName;
		this.userFullName = userFullName;
		this.userProfileImageUrl = userProfileImageUrl;
		this.createdAt = createdAt;
		this.inReplyToID = inReplyToID;
		this.textContent = textContent;
		this.authorOutDegree = authorOutDegree;
		this.authorInDegree = authorInDegree;
		this.authorNumLists = authorNumLists;
		this.buzzScore = buzzScore;
	}

	public KmiPost(JSONObject postJSON) {
		//super();
		this.id = postJSON.getString("id");
		this.userId = postJSON.getString("userId");
		this.userScreenName = postJSON.getString("userScreenName");
		this.userFullName = postJSON.getString("userFullName");
		this.userProfileImageUrl = postJSON.getString("userProfileImageUrl");
		this.createdAt = postJSON.getString("createdAt");
		this.inReplyToID = postJSON.getString("inReplyToID");
		this.textContent = postJSON.getString("textContent");
		this.authorOutDegree = postJSON.getDouble("authorOutDegree");
		this.authorInDegree = postJSON.getDouble("authorInDegree");
		this.authorNumLists = postJSON.getDouble("authorNumLists");
		this.buzzScore = postJSON.getString("buzzScore");
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserScreenName() {
		return userScreenName;
	}

	public void setUserScreenName(String userScreenName) {
		this.userScreenName = userScreenName;
	}

	public String getUserFullName() {
		return userFullName;
	}

	public void setUserFullName(String userFullName) {
		this.userFullName = userFullName;
	}

	public String getUserProfileImageUrl() {
		return userProfileImageUrl;
	}

	public void setUserProfileImageUrl(String userProfileImageUrl) {
		this.userProfileImageUrl = userProfileImageUrl;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getInReplyToID() {
		return inReplyToID;
	}

	public void setInReplyToID(String inReplyToID) {
		this.inReplyToID = inReplyToID;
	}

	public String getTextContent() {
		return textContent;
	}

	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}

	public double getAuthorOutDegree() {
		return authorOutDegree;
	}

	public void setAuthorOutDegree(double authorOutDegree) {
		this.authorOutDegree = authorOutDegree;
	}

	public double getAuthorInDegree() {
		return authorInDegree;
	}

	public void setAuthorInDegree(double authorInDegree) {
		this.authorInDegree = authorInDegree;
	}

	public double getAuthorNumLists() {
		return authorNumLists;
	}

	public void setAuthorNumLists(double authorNumLists) {
		this.authorNumLists = authorNumLists;
	}

	public String getBuzzScore() {
		return buzzScore;
	}

	public void setBuzzScore(String buzzScore) {
		this.buzzScore = buzzScore;
	}

}
