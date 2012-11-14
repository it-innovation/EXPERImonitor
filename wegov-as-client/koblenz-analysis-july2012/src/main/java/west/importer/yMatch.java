package west.importer;

import java.net.*;

public class yMatch 
{
    static String base = "http://uk.news.yahoo.com/blogs/editors_corner/";
	
    public String pubURL;
    public String imgURL;
    
    public String body;
    public String title;
    
	public void setURL(String link)
	{
		try
		{
			pubURL = new URL (new URL (base), link).toString();
		}
		catch (Exception e)
		{}
	}
}
