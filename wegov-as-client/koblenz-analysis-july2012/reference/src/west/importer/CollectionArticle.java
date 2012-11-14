package west.importer;

import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.tree.*;
import java.text.*;
import java.sql.*;
/*
import oracle.jdbc.dbaccess.*;
import oracle.jdbc.driver.*;
import oracle.sql.*;
*/
public class CollectionArticle
{
    public String reason;

    public String body;
    
    public String user;
    
    //public int isQuestion = 0;

    public long did = -1;
    public long time = -1;
    public long aid = -1;

    public HashMap terms;
    public HashMap features;
    public HashMap opinions;
    
    public double liwc_pos = 0.0;
    public double liwc_neg = 0.0;
    public double liwc_sum = 0.0;
    
    double score;
    
// ***********************************************    
    
    public Reader getReader()
    { 
       if (body != null) 
         return new StringReader(body);
       else
         return null; 
    }
}
