package eu.wegov.coordinator.web;

public class WidgetSet {
	private int id = 0;
	private int policymakerId = 0;
	private String name = "";
	private String description = "";
	private int isDefault = 0;
	
	public WidgetSet(int id, int policymakerId, String name, String description, int isDefault) {
		this.id = id;
		this.policymakerId = policymakerId;
		this.name = name;
		this.description = description;
		this.isDefault = isDefault;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getPolicymakerId() {
		return policymakerId;
	}
	public void setPolicymakerId(int policymakerId) {
		this.policymakerId = policymakerId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public int getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(int isDefault) {
		this.isDefault = isDefault;
	}
}
