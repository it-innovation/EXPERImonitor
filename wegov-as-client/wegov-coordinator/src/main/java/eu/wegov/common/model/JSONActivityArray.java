package eu.wegov.common.model;

public class JSONActivityArray {
	int total;
	JSONActivity[] data;

	public JSONActivityArray(int total, JSONActivity[] data) {
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

	public JSONActivity[] getData() {
		return data;
	}

	public void setData(JSONActivity[] data) {
		this.data = data;
	}
	
	public int getSize() {
		return data.length;
	}

}
