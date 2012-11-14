package eu.wegov.coordinator.web;

public class PolicymakerSetting {
	private String name;
	private String value;
	
	public PolicymakerSetting() {
		super();
	}

	public PolicymakerSetting(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
