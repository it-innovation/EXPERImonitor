package eu.wegov.coordinator.web;

public class Widget {
	private int id = 0;
	private int wsId = 0;
	private String columnName = "";
	private int columnOrderNum = 0;
	private int policymakerId = 0;
	private String name = "";
	private String description = "";
	private String widgetCategory = "";
	private String type = "";
	private String datatype = "";
	private String dataAsString = "";
	private String parametersAsString = "";
	private int isVisible = 0;
	private String labelText = "";

	public Widget(int id, int wsId, String columnName, int columnOrderNum,
			int policymakerId, String name,
      String description, String category, String type,
			String datatype, String dataAsString, String parametersAsString,
			int isVisible, String labelText) {
		super();
		this.id = id;
		this.wsId = wsId;
		this.columnName = columnName;
		this.columnOrderNum = columnOrderNum;
		this.policymakerId = policymakerId;
		this.name = name;
		this.description = description;
		this.widgetCategory = category;
		this.type = type;
		this.datatype = datatype;
		this.dataAsString = dataAsString;
		this.parametersAsString = parametersAsString;
		this.isVisible = isVisible;
		this.labelText = labelText;

  }

  public String getWidgetCategory() {
    return widgetCategory;
  }

  public void setWidgetCategory(String widgetCategory) {
    this.widgetCategory = widgetCategory;
  }

  public String getLabelText() {
    return labelText;
  }

  public void setLabelText(String labelText) {
    this.labelText = labelText;
  }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getWsId() {
		return wsId;
	}

	public void setWsId(int wsId) {
		this.wsId = wsId;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public int getColumnOrderNum() {
		return columnOrderNum;
	}

	public void setColumnOrderNum(int columnOrderNum) {
		this.columnOrderNum = columnOrderNum;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(String datatype) {
		this.datatype = datatype;
	}

	public String getDataAsString() {
		return dataAsString;
	}

	public void setDataAsString(String dataAsString) {
		this.dataAsString = dataAsString;
	}

	public String getParametersAsString() {
		return parametersAsString;
	}

	public void setParametersAsString(String parametersAsString) {
		this.parametersAsString = parametersAsString;
	}

	public int getIsVisible() {
		return isVisible;
	}

	public void setIsVisible(int isVisible) {
		this.isVisible = isVisible;
	}


}
