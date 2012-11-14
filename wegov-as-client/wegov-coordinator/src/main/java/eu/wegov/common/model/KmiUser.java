package eu.wegov.common.model;

import net.sf.json.JSONObject;

public class KmiUser {
	private String createdAt;
	private String description;
	private Double numFavorities;
	private Double numFollowers;
	private Double numFriends;
	private String id;
	private Double numListed;
	private String location;
	private String name;
	private String profileImageUrl;
	private String screenName;
	private Double numStatuses;
	private String url;
	private String timeZone;
	private String role;
	private String buzzScore;

	public KmiUser(String createdAt, String description, Double numFavorities,
			Double numFollowers, Double numFriends, String id,
			Double numListed, String location, String name,
			String profileImageUrl, String screenName, Double numStatuses,
			String url, String timeZone, String role, String buzzScore) {
		super();
		this.createdAt = createdAt;
		this.description = description;
		this.numFavorities = numFavorities;
		this.numFollowers = numFollowers;
		this.numFriends = numFriends;
		this.id = id;
		this.numListed = numListed;
		this.location = location;
		this.name = name;
		this.profileImageUrl = profileImageUrl;
		this.screenName = screenName;
		this.numStatuses = numStatuses;
		this.url = url;
		this.timeZone = timeZone;
		this.role = role;
		this.buzzScore = buzzScore;
	}

	public KmiUser(JSONObject userJSON) {
		//super();
		this.createdAt = userJSON.getString("createdAt");
		this.description = userJSON.getString("description");
		this.numFavorities = userJSON.getDouble("numFavorities");
		this.numFollowers = userJSON.getDouble("numFollowers");
		this.numFriends = userJSON.getDouble("numFriends");
		this.id = userJSON.getString("id");
		this.numListed = userJSON.getDouble("numListed");
		this.location = userJSON.getString("location");
		this.name = userJSON.getString("name");
		this.profileImageUrl = userJSON.getString("profileImageUrl");
		this.screenName = userJSON.getString("screenName");
		this.numStatuses = userJSON.getDouble("numStatuses");
		this.url = userJSON.getString("url");
		this.timeZone = userJSON.getString("timeZone");
		this.role = userJSON.getString("role");
		this.buzzScore = userJSON.getString("buzzScore");
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Double getNumFavorities() {
		return numFavorities;
	}

	public void setNumFavorities(Double numFavorities) {
		this.numFavorities = numFavorities;
	}

	public Double getNumFollowers() {
		return numFollowers;
	}

	public void setNumFollowers(Double numFollowers) {
		this.numFollowers = numFollowers;
	}

	public Double getNumFriends() {
		return numFriends;
	}

	public void setNumFriends(Double numFriends) {
		this.numFriends = numFriends;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Double getNumListed() {
		return numListed;
	}

	public void setNumListed(Double numListed) {
		this.numListed = numListed;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public String getScreenName() {
		return screenName;
	}

	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}

	public Double getNumStatuses() {
		return numStatuses;
	}

	public void setNumStatuses(Double numStatuses) {
		this.numStatuses = numStatuses;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getBuzzScore() {
		return buzzScore;
	}

	public void setBuzzScore(String buzzScore) {
		this.buzzScore = buzzScore;
	}

}
