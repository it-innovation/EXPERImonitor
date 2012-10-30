package west.importer;

import java.util.*;
import java.io.*;
import javax.swing.tree.*;
import java.text.*;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.*;


public class WegovCollectionProcessor implements Runnable
{
    private WegovImporter parent = null;
    private CollectionArticle task = null;
    private SessionBuffer session;
    private SnowballStemmer stemmer;
    private int currentPos = 0;
    
    public WegovCollectionProcessor (WegovImporter ri)
    {
        parent = ri;
        session = SessionBuffer.getInstance ();
        if (session.getLanguage().equals("de"))
            stemmer = new germanStemmer();
        else
            stemmer = new englishStemmer();
        stemmer.reset ();
    }
    
    public void run ()
    {
        while (true)
        {
            task = null;
            
            if (parent.articles4parsing.size () == 0)
            {
                synchronized(parent.myAssistants)
                {
                    parent.myParsers.remove (this);
                    synchronized (parent.sem_opinions)
		            {parent.sem_opinions.notifyAll();}

                    parent.myAssistants.remove (this);
                    parent.myAssistants.notifyAll ();
                }
                
                return;
            }
            
            synchronized (parent.articles4parsing)
            {
                if (parent.articles4parsing.size () > 0)
                {

                    //System.out.println("fetching task from articles4parsing");
                    task = (CollectionArticle) parent.articles4parsing.get (0);
                     //System.out.println("fetched from articls4parsing " + task.did);
                    parent.articles4parsing.remove (task);
                }
            }
            
            if (task != null)
            {
                process ();
                parent.numparse = parent.numparse + 1.0;
                //parent.timeparse = parent.timeparse + (double)(d2.getTime () - d1.getTime ());
                
               // if (task != null && task.features != null && task.features.size () >= session.minFeatures)
                {
                    synchronized (parent.articles4opinions)
                    {
                        //System.out.println("adding task to articles4storage " + task.did);
                        parent.articles4opinions.add (task);
                    }
                    
                    synchronized (parent.sem_opinions)
                    {
                        parent.sem_opinions.notify ();
                    }
                }
            }
        }
    }
    
    public void process ()
    {
        //System.out.println("processin task = " + task.did);
        if (task.body == null || task.body.length () == 0 || task.did == -1)
            return;
        
        if (!checkUnwantedItems ())
            return;
        
        try
        {
        	processText ();
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            stemmer.reset ();
        }
       
        // We need TASK BODY later on !!!! 
        //task.body = null;
    }
    
    public boolean checkUnwantedItems ()
    {
        /*
        String titleCheck = task.title.toLowerCase ();
        String bodyCheck = task.body.toLowerCase ();
        
        if (titleCheck.indexOf (":") >= 0)
            return false;
        if (titleCheck.indexOf ("(disambiguation)") >= 0)
            return false;
        
        if (bodyCheck.indexOf ("#redirect") >= 0)
            return false;
        */
        return true;
    }
    
    long did =-1, count=0;
    
    public void processText ()
    {
        task.features = new HashMap ();
        task.terms = new HashMap();
        
        String toParse = filterLine (task.body);

        //System.out.println("processing task = " + task.did);
        
        if (toParse==null || toParse.length () <= 0)
            return;
        
        StringTokenizer it = new StringTokenizer (toParse);
        did = task.did;
        
        while (it.hasMoreTokens ())
        {
            String term = it.nextToken ();
            
            
            if (term.length () > session.maxTermLength || term.length () < session.minTermLength || term == null)
                continue;
            
            term = term.toLowerCase ().trim ();
            
            if (session.removeStopwords && parent.stopwords.contains (term))
               continue;
            
            String word = null;
            if (term != null && term.length () >= session.minTermLength && term.length () <= session.maxTermLength)
            {
            	word = term;
            }           
            
            if (session.useStemmer)
            {
                stemmer.setCurrent (term);
                stemmer.stem ();
                term = filterToken(stemmer.getCurrent ());   // nur normale buchstaben bleiben
                stemmer.reset ();
            } 

            if (term != null && term.length () >= session.minFeatureLength && term.length () <= session.maxFeatureLength)
            {	
                addFeature (term);
                if (word != null)
                	addTerm(word,term);
            }
            term = null;
            word = null;
        }
        //System.out.print("Doc Id " + task.did);
        //System.out.println("   Features " + count);
        count=0;
        //System.out.println(">>>>>>>>>>>>>>>");
        
        it = null; toParse=null;
    }
    
   
    public void addTerm (String term, String feature)
    {
    	myInteger myinteger = null;
    	
    	HashMap termmap = (HashMap) task.terms.get (feature);
    	if (termmap == null)
    	{
    		myinteger = new myInteger(1);
    		termmap = new HashMap();
    		termmap.put(term, myinteger);
    		task.terms.put(feature,termmap);
    	}	
    	else
    	{
    		myinteger = (myInteger)termmap.get(term);
    		if (myinteger == null)
    		{
    			myinteger = new myInteger(1);
    			termmap.put(term, myinteger);
    		}	
    		else
    		{
    			myinteger.increase();
    		}
    	}	
        myinteger = null;
    }
 
    
    public void addFeature (String term)
    {
        //if(term.length()<= 3)
           
        
        myInteger myinteger = (myInteger)task.features.get (term);
        if (myinteger == null){
             //System.out.println("adding feature " + term);
            task.features.put (term, new myInteger (1));
            //System.out.print(task.did +" " );
            //System.out.println(term + " ");
            
                count++;
        }
        else
            myinteger.increase ();
        
        myinteger = null;
    }
    
    
    public static String filterToken (String _token)
    {
        String toParse = _token;
        toParse = toParse.replaceAll("\\W","");
        toParse = toParse.replaceAll("\\d", "");
        return toParse;
    }
    
    public static String filterLine (String _line)
    {
        //String toParse = "Sprint {{BegriffsklÃ¤rungshinweis}}  -0.5 +9,3 Als: Klein[[ Sprint]] oder [[Kurz ]]strecke bezeichnet-man in zyklischen [[Sportart ]] en (mit sich http://www.yahoo.de/ wiederholenden BewegungsablÃ¤ufen) die Ãœberwindung einer Strecke mit der grÃ¶ÃŸtmÃ¶glichen Geschwindigkeit, die der menschliche Organismus erlaubt.   In der [[Leichtathletik ]]  sind die Sprintstrecken 50 bis 400 Meter lang. Man unterscheidet im Allgemeinen auch zwischen Kurz- und Langsprint, wobei Strecken Ã¼ber 200 Metern LÃ¤nge als Langsprint betitelt werden. Die Strecken [[100-Meter-Lauf|100 ]] , [[200-Meter-Lauf|200 ]]  und [[400-Meter-Lauf|400 Meter ]] , 100 Meter HÃ¼rden (Frauen), 110 Meter HÃ¼rden (MÃ¤nner) und 400 Meter HÃ¼rden sind olympische Disziplinen. Der Kurzstreckenlauf ist die Ã¤lteste olympische Disziplin. Entscheidend fÃ¼r die Abgrenzung von Mittel- und Langstrecken ist bei den Laufstrecken der Leichtathletik die Energieversorgung: Sie erfolgt beim Sprint ohne Sauerstoffbeteiligung ([[Anaerobie|anaerob ]] ). Mit etwa 5 & nbsp;% ist der Wirkungsgrad dabei sehr gering. Ein Sportler kann diese Geschwindigkeit hÃ¶chstens etwa 40 Sekunden durchhalten.  Beim [[Schwimmsport ]]  gelten als Sprintstrecken 25- bis 200-Meter-Distanzen. Die Strecken 50, 100 und 200 Meter sind olympische Disziplinen. FÃ¼r die Schwimm-Sprintstrecken gilt die Abgrenzung anhand der fehlenden Sauerstoffbeteiligung nicht, bzw. allenfalls fÃ¼r die 25-Meter-Distanz.  Beim [[Bahnradsport|Bahnradfahren ]]  werden im Sprintrennen ein bis zwei Runden in HÃ¶chstgeschwindigkeit gefahren, ca. 250 bis 600 Meter.   Im [[Eisschnelllauf ]]  werden als Sprintstrecken 100, 500 und 1000 Meter gelaufen. Die beiden letztgenannten sind auch olympische Disziplinen.  Im [[American Football ]]  gilt der sogenannte [[40-Yard-Sprint ]]  als gÃ¤ngigster Indikator fÃ¼r Beschleunigung und Geschwindigkeit eines Spielers.  In anderen Sportarten, etwa  Rudern, Kanu oder Skilanglauf, ist gelegentlich in der Umgangssprache von Sprintstrecken die Rede. Allerdings ist der Sportler dabei deutlich lÃ¤nger als eine Minute unterwegs und die Muskeln werden teilweise mit Sauerstoff versorgt. Deshalb handelt es sich genau genommen um Mittel- oder Langstrecken.  ''Siehe auch:'' [[Olympische Spiele ]] , [[Laufsport ]]    [[Kategorie:Sport ]]  [[Kategorie:Leichtathletik ]]  [[Kategorie:Radsport ]]   [[bg:�?¡�?¿Ñ€�?¸�?½Ñ‚ ]]  [[ca:Carrera de velocitat ]]  [[da:Sprint ]]  [[en:Sprint (race) ]]  [[eo:Kurtadistanca kuro ]]  [[es:Atletismo velocidad ]]  [[fr:Sprint (athlÃ©tisme) ]]  [[it:VelocitÃ  (atletica) ]]  [[ja:çŸ­è·�é›¢èµ° ]]  [[ka:áƒ¡áƒžáƒ áƒ˜áƒœáƒ¢áƒ˜ ]]  [[lt:Sprintas ]]  [[no:SprintlÃ¸p ]]  [[ru:�?¡�?¿Ñ€�?¸�?½Ñ‚ ]]  [[simple:Sprint ]]  [[sq:Sprint ]]  [[zh:çŸ­è·‘ ]]";
        
        String toParse = _line;
        
        
        toParse = com.mindprod.entities.StripEntities.stripEntities (toParse);
        toParse = com.mindprod.entities.StripEntities.stripEntities (toParse);
        
       // toParse = toParse.replaceAll ("\\S+?=\".*?\""," ");     // x="xxx"   weg
        
        /*
        toParse=toParse.replaceAll ("\\{\\{[^\\}]*?\\}\\}"," ");
        toParse=toParse.replaceAll ("\\s+?\\]\\]", "\\]\\]");
        toParse=toParse.replaceAll ("\\[\\[\\s+?", "\\[\\[");                   //wiki-klammern und links
        toParse=toParse.replaceAll ("\\[\\[[^\\]]*?\\:[^\\]]*?\\]\\]"," ");
        toParse=toParse.replaceAll ("\\[\\[","");
        toParse=toParse.replaceAll ("\\]\\]","");
        */
        
        toParse=toParse.replaceAll ("(?s)<!\\-\\-.*?\\-\\->", " ");   //comments weg
        toParse=toParse.replaceAll ("[a-zA-Z]+?://\\S*?\\s", " ");    // all links weg
        
        
        
        //toParse=toParse.replaceAll ("(disambiguation)"," ");
        
        
        toParse=toParse.replaceAll ("[\\(\\[\\{\\\\^\\-\\$\\|\\]\\}\\)\\?\\*\\+\\.]", " ");   // meta characters
        
        
        
        toParse=toParse.replaceAll ("'{2,}", "");   // normale anfÃ¼hrungszeichen als Wiki-Formatierung
        
        
        toParse=toParse.replaceAll ("<[a-zA-Z].*?>", " " );   // open tags weg, lineumbruch immer ende
        toParse=toParse.replaceAll ("</[a-zA-Z].*?>", " ");   // close tags weg, lineumbruch immer ende
        
        toParse=toParse.replaceAll ("\\d", " ");       // zahlen weg
        
        
        toParse = toParse.replaceAll ("[`\"â€žâ€œâ€šâ€˜Â«Â»â€¹â€º]","");   //anfÃ¼hrungszeichen
        toParse=toParse.replaceAll ("[+-/*;:,=><Â§&#@]", " ");  //punktuation
        toParse=toParse.replaceAll ("\\p{Punct}", " ");  //punktuation
        toParse = toParse.replaceAll ("\\s", " ");
        
        //toParse=toParse.replaceAll ("\\s[a-zA-Z]{1,2}\\s", " ");            // einzelne Buchstaben + paare
        //toParse=toParse.replaceAll ("^[a-zA-Z]{1,2}\\s", " ");              // einzelne Buchstaben
        //toParse=toParse.replaceAll ("\\s[a-zA-Z]{1,2}$", " ");              // einzelne Buchstaben
        //toParse=toParse.replaceAll ("^[a-zA-Z]{1,2}$", " ");                // einzelne Buchstaben
        
        //toParse=toParse.replaceAll ("\\s+", " ");
        
        return toParse;
    }
}
