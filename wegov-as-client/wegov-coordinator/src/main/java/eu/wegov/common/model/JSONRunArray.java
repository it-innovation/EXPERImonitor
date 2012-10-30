package eu.wegov.common.model;

public class JSONRunArray {
	int total;
	JSONRun[] data;

	public JSONRunArray(int total, JSONRun[] data) {
		super();
		this.total = total;
		this.data = data;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public JSONRun[] getData() {
		return data;
	}

	public void setData(JSONRun[] data) {
		this.data = data;
	}

}
