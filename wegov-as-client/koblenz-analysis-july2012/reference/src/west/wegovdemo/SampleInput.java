package west.wegovdemo;

import java.util.*;

import west.importer.WegovImporter;

public class SampleInput implements TopicOpinionInput
{

	ArrayList docs;
	ArrayList users;
	
    public SampleInput()
    {
    	docs = new ArrayList();
    	users = new ArrayList();
    }
	
    public void add (String in, String u)
    {
    	docs.add(in);
    	users.add(u);
    }
	
	public String[] getDocumentContents() 
	{
		String[] out = new String[docs.size()];
        for (int i=0; i<docs.size(); i++)
        	out[i] = (String)docs.get(i);
		return out;
	}
	
	public String[] getDocumentUsers()
	{
		String[] out = new String[users.size()];
        for (int i=0; i<users.size(); i++)
        	out[i] = (String)users.get(i);
		return out;		
		
	}
	
	//=============================================================
	
	public static void main (String[] x)
	{
		SampleInput input = new SampleInput();
		input.add("Sample post", "hugo");
        input.add("Sample post 2", "ben");

		TopicOpinionAnalysis analysis = new WegovImporter();
		TopicOpinionOutput output = analysis.analyzeTopicsOpinions(input);
		
		WegovRender.showResults(output);
	}
}
