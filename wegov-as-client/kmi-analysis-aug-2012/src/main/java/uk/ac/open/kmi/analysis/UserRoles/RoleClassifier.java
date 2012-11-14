package uk.ac.open.kmi.analysis.UserRoles;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class RoleClassifier {
	private String label;
	private Vector<Feature> features;

	public RoleClassifier(String file) {
		this.label = file.substring(file.lastIndexOf('/')+1, file.lastIndexOf('.'));
		features = new Vector<Feature>();
		
		for(String featureline:readStringFileToVector(file)){
			Feature f = new Feature(featureline);
			features.add(f);
		}
	}

	public String getLabel() {
		return this.label;
	}

	public void setFeatures(Vector<Feature> features) {
		this.features = features;
	}

	public Vector<Feature> getFeatures() {
		return features;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	private static Vector<String> readStringFileToVector(String path) {
		Vector<String> vec =  new Vector<String>();
		try{
			String str="";
			BufferedReader in = new BufferedReader(new FileReader(path));
			while ((str = in.readLine()) != null) {
				vec.add(str);
			}
			in.close();
		}
		catch (IOException ex){
			ex.printStackTrace();
		}
		return vec;
	}


}
