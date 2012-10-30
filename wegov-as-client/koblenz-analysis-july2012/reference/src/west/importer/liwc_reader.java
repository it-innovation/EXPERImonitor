package west.importer;

import java.io.*;
import java.util.*;

public class liwc_reader
{
	static String posemo = "13";
	static String negemo = "16";
	static boolean issuffix = false;
	static StringTokenizer tok;

	public static void main (String[] args) throws Exception
	{
		StringBuffer result = new StringBuffer();
	    File file = new File ("/Users/skyhorse/Documents/LIWC/LIWC2007_German.dic");
	    BufferedReader buf = new BufferedReader(new FileReader(file));
	    String line = buf.readLine();

	do
	{
	       line = buf.readLine();
	}
	while (!line.startsWith("%"));
	
	while (line!= null)
	{
		tok = new StringTokenizer(line);
		// System.out.println(line);
		String term = tok.nextToken();
		if (term.endsWith("*"))
			issuffix = true;
		else
			issuffix = false;
		
		if (line.indexOf(posemo) >= 0 && issuffix)
		{
		 result.append("\"");	
	     result.append(term.replaceAll("\\*", ""));
	     result.append("\", ");
	     }
		
		line = buf.readLine();
	}
	
	System.out.println(result.toString());
    }
}
