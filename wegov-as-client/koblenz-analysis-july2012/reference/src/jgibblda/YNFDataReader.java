package jgibblda;
import java.util.Map.*;
import java.util.*;

//import com.aliasi.symbol.*;
import west.importer.*;

import java.io.*;

//import java.util.HashMap;
//import java.util.Map;
//import java.util.Vector;

import west.importer.*;


public class YNFDataReader
{
    //public static MapSymbolTable mapTermId;
    //public static MapSymbolTable mapDocId;
    static java.util.Date d1, d2;
    //public static OracleConnection conn;

    //select freatures from answers
    //static String r = "select f.id, f.term, f.tf from features f, nasir_terms t where f.question=0 and f.term=t.term order by f.id asc";

    //select features from including questions, tags and answers


    public static LDADataset read(Dictionary dict, WegovImporter parent) throws Exception
    {
        if(dict != null)
            return readTestData(dict);
      d1 = new java.util.Date();
      //mapTermId = new MapSymbolTable();
      //mapDocId = new MapSymbolTable();
      int numDocs = parent.articles4lda.size();
      
      LDADataset data = new LDADataset(numDocs);
      
      parent.articles4output = new HashMap();
      
      //System.out.println("Total Docs " + numDocs);
      
      StringBuffer sb = null;
      CollectionArticle task = null;
      HashMap temp = new HashMap ();
      
        outer:
        while (parent.articles4lda.size () > 0)
        {
            sb=null;
            task = null;        
            task = (CollectionArticle) parent.articles4lda.get (0);
            parent.articles4lda.remove (task);

            if (task!=null)
            {
                if (task.features == null || task.features.size () < SessionBuffer.minFeatures)
                {
                	data.setDoc(" " , (int)task.did);
                    continue outer;
                }
/*##########################################################################*/

                double termcount = 0.0;
                Iterator it = task.features.values ().iterator ();
                while (it.hasNext ())
                {
                    myInteger myinteger = (myInteger)it.next ();
                    termcount += myinteger.doubleValue ();
                }

                it = task.features.entrySet ().iterator ();
                int count = 0;
                sb=new StringBuffer();
                while (it.hasNext ())
                {
                    Entry entry = (Entry) it.next ();
                    myInteger myinteger = (myInteger)entry.getValue ();
                    int tf = myinteger.intValue ();
                    double rtf = ((double)tf) / termcount;
                    myinteger = null;

                    for (int j=1; j<=tf; j++)
                    {
                        sb.append((String)entry.getKey ()).append(" ");
                    }

                    entry = null;
                    count++ ;
                }//end while
                //System.out.println(task.did + "  " + count);
                data.setDoc(sb.toString(), (int)task.did);
                
                parent.articles4output.put(new Integer((int)task.did), task);
                
                //System.out.println(task.did +"  "+ sb.toString());
                count=0;
                task = null;

           }//end if

        }//end while outer
      
      System.out.println("Total Docs " + data.M);
      System.out.println("Total Vocabulary " + data.V);
      System.out.println("Total Docs " + data.docs.length);
      System.out.println("Local Dict Size " + data.localDict.id2word.size());

      return data;
    }//end method

    public static LDADataset readTestData(Dictionary dict){
        System.out.println("Not implemented yet...");
        return null;
    }

            public static boolean writeDocumentsFile(){
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("./models/" + System.currentTimeMillis()+".dat"));
			 int limit=1000; //docs.length;
                        System.out.println("Writing file....");
                        writer.write(limit + "\n");
                        int doc=0;
                       /*
                        for(doc=0;doc<limit; doc++){
                            for(int token=0;token<docs[doc].length;token++){
                                //writer.write(mapTermId.idToSymbol(docs[doc][token]) + " ");
                            }
                            writer.write("\n");
                            if(doc==1000)
                                break;
                        }
                        */
                        System.out.println(doc + " Documents written to file!");
			writer.close();
		}
		catch (Exception e){
			System.out.println("Error while saving document file:" + e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

}