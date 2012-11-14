package west.importer;

import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.*;
import javax.swing.tree.*;
import java.text.*;
import jgibblda.*;
import west.wegovdemo.*;

public class WegovImporter implements TopicOpinionAnalysis
{
    public double numparse = 0.0;
    //public static double timeparse = 0.0;
    
    public double numstore = 0.0;
    //public static double timestore = 0.0;
    
    public double numload = 0.0;
    //public static double timeload = 0.0;

    public double numopinions = 0.0;

    public boolean exhaustedData = false;
    
    public Vector myAssistants = null;
    public Vector myDownloaders = null;
    public Vector myParsers = null;

    public ArrayList articles4parsing;
    public ArrayList articles4lda;
    public ArrayList articles4features;
    public ArrayList articles4opinions;
    public HashMap articles4output;
    
    public HashSet stopwords;
    
    public Counter counter;
    public int postingCounter;
    public SessionBuffer session;
   
    public Boolean sem_load;
    public Boolean sem_process;
    public Boolean sem_opinions;

    public int numPostings;

    public Counter yahooPageCounter;
    public Counter yahooPostingCounter;

    public Model trnModel;
    
    public int numTopics = 0;
     
    //public SchemaManager schemaManager;
    
    /**
     * Set the desired number of topics in the topic-opinion analysis. If the number is not explicitly set before starting analysis, an internal estimate
     * for a suitable number of topics will be applied.
     * @param numTopics The number of topics that topic-opinion module should use for analysis.
     */
    public void setNumTopics(int _numTopics)
    {
    	numTopics = _numTopics;
    }
    
    /**
     * Set the desired number of most relevant terms in the topic-opinion analysis. If the number is not explicitly set before starting analysis, an internal estimate
     * for a suitable number of most relevant terms will be applied.
     * @param numTerms The number of terms that topic-opinion module should return for each topic.
     */
    public void setTermsPerTopic(int numTerms)
    {
    	SessionBuffer.setNumTermsPerTopic(numTerms);
    }
    
    /**
     * Set the language of the analyzed document collection. This setting activates  
     * custom, language-specific morphological reduction of words to word stems for constructing document features. In parallel,
     * it activates the corresponding stopword list for term filtering (removal of language-specific irrelevant, very frequent terms).
     * @param language The language of the dataset. possible values of this parameter are currently "en" (English) and "de" (German). Default value is "en". 
     * Any other strings supplied to this function will be ignored and interpreted as "en".  
     */
    public void setLanguage(String language)
    {
    	session.setLanguage(language);
    }
    
    public TopicOpinionOutput analyzeTopicsOpinions (TopicOpinionInput input)
    {
        Date d1, d2;
        long time;
        d1 = new Date ();
        //schemaManager.createSchema ();
        d2 = new Date ();
        System.out.print ("done - ");
        time = ((d2.getTime () - d1.getTime ()) / 1000);
        System.out.println (time + " seconds");
        
        if (session.removeStopwords)
        {
            System.out.print ("Import stopwords.. ");
            stopwords = StopwordList.getStopwords();
        }
        else
        {
            System.out.println ("Not using stopwords.. ");
        }
        
        // import articles to parse
        String[] inp = input.getDocumentContents();
        String[] usr = input.getDocumentUsers();
        for (int i=0; i<inp.length; i++)
        {
        	CollectionArticle article = new CollectionArticle ();
        	article.body = inp[i];
        	article.user = usr[i];
        	article.did = i;
        	articles4parsing.add(article);
        }
        
        for (int i=1; i<=session.maxProcessingThreads; i++)
        {
            //System.out.println("creating processing threads..");
            WegovCollectionProcessor rp = new WegovCollectionProcessor ( this );
            myAssistants.add (rp);
            myParsers.add(rp);
            new Thread (rp).start ();
        }

        OpinionMiner.init();
        for (int i=1; i<=session.maxOpinionThreads; i++)
        {
            //System.out.println("creating opinion threads..");
            WegovOpinionProcessor rp = new WegovOpinionProcessor ( this );
            myAssistants.add (rp);
            new Thread (rp).start ();
        }
        
        try
        {
            do 
            {
                synchronized (myAssistants)
                {
                    if (myAssistants.size () > 0)
                        myAssistants.wait ();
                }
            }
            while (myAssistants.size () > 0);
        }
        catch (Exception e)
        {e.printStackTrace ();}
        
        
        WegovOpinionSummarizer summarizer = new WegovOpinionSummarizer(this);
        summarizer.run();
        summarizer = null;
        
        WegovTopicAnalyzer analyzer = new WegovTopicAnalyzer ( this );
        if (numTopics > 1)
        {
        	analyzer.setNumTopics(numTopics);
        }	
        analyzer.run();
        //analyzer = null;
        //prepare output
        WegovOutput out = (WegovOutput) new WegovResultOutput(this).prepareOutput();
        
        out.docs = input.getDocumentContents();
        out.users = input.getDocumentUsers();
        
        return out;
    } 

    public WegovImporter ()
    {
        sem_load = new Boolean (true);
        sem_process = new Boolean (true);
        sem_opinions = new Boolean (true);
        
        exhaustedData = false;
        
        session = SessionBuffer.getInstance ();// (true);
        //schemaManager = new SchemaManager (this);
        
        myAssistants = new Vector ();
        myDownloaders = new Vector();
        myParsers = new Vector();
        
        articles4parsing = new ArrayList ();
        articles4lda = new ArrayList ();
        articles4features = new ArrayList ();
        articles4opinions = new ArrayList ();
        
        counter = new Counter ();
        
        System.out.println ();
        System.out.println ("WeGov Topic-Opinion tool Version 7.2");
        System.out.println ();
    }  

}