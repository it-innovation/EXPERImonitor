package eu.wegov.coordinator.web;

public class PolicymakerLocation {
	private int id;
	private int pmId;
	private String locationName;
	private String locationAddress;
	private String lat;
	private String lon;

	public PolicymakerLocation(int id, int pmId, String locationName,
			String locationAddress, String lat, String lon) {
		super();
		this.id = id;
		this.pmId = pmId;
		this.locationName = locationName;
		this.locationAddress = locationAddress;
		this.lat = lat;
		this.lon = lon;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPmId() {
		return pmId;
	}

	public void setPmId(int pmId) {
		this.pmId = pmId;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public String getLocationAddress() {
		return locationAddress;
	}

	public void setLocationAddress(String locationAddress) {
		this.locationAddress = locationAddress;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

}
