package uk.ac.open.kmi.analysis.UserRoles;

public class Feature {
	private String name;
	private double minValue;
	private double maxValue;
	
	public Feature (String in){
		int startMin = in.indexOf('\t');
		int startMax = in.indexOf('\t',startMin+1);
		this.name = in.substring(0,startMin);
		this.minValue = Double.valueOf(in.substring(startMin+1, startMax));
		this.maxValue = Double.valueOf(in.substring(startMax+1));		
	}

	public String getName() {
		return name;
	}

	public double getMinValue() {
		return minValue;
	}

	public double getMaxValue() {
		return maxValue;
	}
}