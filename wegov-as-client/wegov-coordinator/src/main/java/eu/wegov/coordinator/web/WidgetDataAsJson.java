package eu.wegov.coordinator.web;


public class WidgetDataAsJson {
	private int id = 0;
	private int wId = 0;
	private int pmid = 0;
	private int activityid = 0;
	private int runid = 0;
	private String type = "";
	private String name = "";
	private String location = "";
	private int nResults = 0;
	private String minId = null;
	private String maxId = null;
	private String dataAsJson = "";
	private String collected_at;

	public WidgetDataAsJson(int id, int wId, String type, String name, String location, String dataAsJson, String collected_at) {
		this(id, wId, 0, 0, 0, type, name, location, 0, null, null, dataAsJson, collected_at);
	}

	public WidgetDataAsJson(int id, int wId, int pmid, int activityid, int runid, String type, String name,
			String location, int nResults, String minId, String maxId, String dataAsJson, String collected_at) {
		super();
		this.id = id;
		this.wId = wId;
		this.pmid = pmid;
		this.activityid = activityid;
		this.runid = runid;
		this.type = type;
		this.name = name;
		this.location = location;
		this.nResults = nResults;
		this.minId = minId;
		this.maxId = maxId;
		this.dataAsJson = dataAsJson;
		this.collected_at = collected_at;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getwId() {
		return wId;
	}

	public void setwId(int wId) {
		this.wId = wId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDataAsJson() {
		return dataAsJson;
	}

	public void setDataAsJson(String dataAsJson) {
		this.dataAsJson = dataAsJson;
	}

	public String getCollected_at() {
		return collected_at;
	}

	public void setCollected_at(String collected_at) {
		this.collected_at = collected_at;
	}

	public int getPmid() {
		return pmid;
	}

	public void setPmid(int pmid) {
		this.pmid = pmid;
	}

	public int getActivityid() {
		return activityid;
	}

	public void setActivityid(int activityid) {
		this.activityid = activityid;
	}

	public int getRunid() {
		return runid;
	}

	public void setRunid(int runid) {
		this.runid = runid;
	}

	public int getnResults() {
		return nResults;
	}

	public void setnResults(int nResults) {
		this.nResults = nResults;
	}

	public String getMinId() {
		return minId;
	}

	public void setMinId(String minId) {
		this.minId = minId;
	}

	public String getMaxId() {
		return maxId;
	}

	public void setMaxId(String maxId) {
		this.maxId = maxId;
	}

  @Override
  public String toString() {
    return "WidgetDataAsJson{" + "id=" + id + ", wId=" + wId + ", pmid=" + pmid + ", activityid=" + activityid + ", runid=" + runid + ", type=" + type + ", name=" + name + ", location=" + location + ", nResults=" + nResults + ", minId=" + minId + ", maxId=" + maxId + ", dataAsJson=" + dataAsJson + ", collected_at=" + collected_at + '}';
  }


  
}
