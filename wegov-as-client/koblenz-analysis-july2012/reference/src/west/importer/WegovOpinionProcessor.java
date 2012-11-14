package west.importer;

import java.util.*;
import java.io.*;
import javax.swing.tree.*;
import java.text.*;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.*;


public class WegovOpinionProcessor implements Runnable
{
    private WegovImporter parent = null;
    private CollectionArticle task = null;
    private SessionBuffer session;
    private SnowballStemmer stemmer;
    private int currentPos = 0;
    
    public WegovOpinionProcessor (WegovImporter ri)
    {
        parent = ri;
        session = SessionBuffer.getInstance ();
        stemmer = new englishStemmer ();
        stemmer.reset ();
    }
    
    public void run ()
    {
        while (true)
        {
            task = null;
            
            if (parent.articles4opinions.size() == 0 && parent.myParsers.size () == 0)
            {
                synchronized(parent.myAssistants)
                {
                	//System.out.println("RAUSJ");
                    parent.myAssistants.remove (this);
                    parent.myAssistants.notifyAll ();
                }
                
                return;
            }
            
            while (parent.articles4opinions.size () == 0 && !(parent.myParsers.size () == 0))
            {
                try
                {
                    synchronized(parent.sem_opinions)
                    {parent.sem_opinions.wait (500);}
                }
                catch (Exception e)
                {e.printStackTrace ();}
            }
            
            synchronized (parent.articles4opinions)
            {
                if (parent.articles4opinions.size () > 0)
                {

                    //System.out.println("fetching task from articles4parsing");
                    task = (CollectionArticle) parent.articles4opinions.get (0);
                     //System.out.println("fetched from articls4parsing " + task.did);
                    parent.articles4opinions.remove (task);
                    
                    synchronized(parent.sem_load)
                    {parent.sem_load.notify ();}
                }
            }
            
            if (task != null)
            {
                process ();
                parent.numopinions = parent.numopinions + 1.0;
                //parent.timeparse = parent.timeparse + (double)(d2.getTime () - d1.getTime ());
                
                if (task != null)
                {
                    synchronized (parent.articles4lda)
                    {
                        //System.out.println("adding task to articles4storage " + task.did);
                        parent.articles4lda.add (task);
                    }
                }
            }
        }
    }
    
    public void process ()
    {
        OpinionMiner.setOpinions(task);
    }
    
}
