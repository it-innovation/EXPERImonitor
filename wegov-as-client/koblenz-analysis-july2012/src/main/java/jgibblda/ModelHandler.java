/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jgibblda;


//import dbconn.*;
//import java.sql.*;
//import oracle.sql.*;
import java.util.*;
//import oracle.jdbc.*;
//import com.aliasi.symbol.*;

/**
 *
 * @author nasir
 */
public class ModelHandler {
        //public static OracleConnection conn;
        
        public static void saveModel(Model model, String modelName)throws Exception
        {
            /*
            conn = (OracleConnection)DBConnect.getInstance ().getConnection ();
            conn.setAutoCommit(false);
            Statement st = conn.createStatement();

            String phi = null ;
            String theta = null;
            String topicWord = null ;
            String docWordTopic = null ;
            String parameters = null ;
            if(modelName.equalsIgnoreCase("Final")){
                phi = modelName + "Phi_" + LDA.trnFeatureTableNAme;
                theta = modelName + "Theta_" + LDA.trnFeatureTableNAme;
                topicWord = modelName + "TopicWord_" + LDA.trnFeatureTableNAme;
                docWordTopic = modelName + "DocWordTopic_" + LDA.trnFeatureTableNAme;
                parameters = modelName + "Parameters_" + LDA.trnFeatureTableNAme;

            }else if(modelName.equalsIgnoreCase("Test")){
                phi = modelName + "Phi_" + LDA.testFeatureTableNAme;
                theta = modelName + "Theta_" + LDA.testFeatureTableNAme;
                topicWord = modelName + "TopicWord_" + LDA.testFeatureTableNAme;
                docWordTopic = modelName + "DocWordTopic_" + LDA.testFeatureTableNAme;
                parameters = modelName + "Parameters_" + LDA.testFeatureTableNAme;
                
            }else
                System.out.println("Can' save modle: unknow table name");
            try
            {
                //System.out.println("Deleting previous tables....");
                st.executeUpdate("drop table " + phi);
                st.executeUpdate("drop table " + theta);
                st.executeUpdate("drop table " + topicWord);
                st.executeUpdate("drop table " + docWordTopic);
                st.executeUpdate("drop table " + parameters);
            }
            catch (Exception e){}
            //System.out.println("Creating new tables....");
            st.executeUpdate("Create table "+ phi + " (topic Number, word Number not null, TWProb double precision)");
            st.executeUpdate("Create table "+ theta +" (doc Number not null, topic Number, DTProb double precision)");
            st.executeUpdate("Create table "+ topicWord +" (topic Number not null, word nvarchar2(200), TWProb double precision)");
            st.executeUpdate("Create table "+ docWordTopic +" (doc Number not null, word Number, Topic Number)");
            st.executeUpdate("Create table "+ parameters +" (alpha double precision not null, beta double precision, ntopics Number, ndocs Number, nwords Number, niter Number )");
            st.close(); st=null;

            OraclePreparedStatement opsPHI = (OraclePreparedStatement)conn.prepareStatement("INSERT INTO "+ phi +" (topic, word, TWProb) VALUES (?,?,?)");
            OraclePreparedStatement opsTHETA = (OraclePreparedStatement)conn.prepareStatement("INSERT INTO "+ theta +" (doc, topic, DTProb) VALUES (?,?,?)");
            OraclePreparedStatement opsTWP = (OraclePreparedStatement)conn.prepareStatement("INSERT INTO "+ topicWord +" (topic, word, TWProb) VALUES (?,?,?)");
            OraclePreparedStatement opsDWT = (OraclePreparedStatement)conn.prepareStatement("INSERT INTO "+ docWordTopic +" (doc, word, Topic) VALUES (?,?,?)");
            OraclePreparedStatement opsPARAM = (OraclePreparedStatement)conn.prepareStatement("INSERT INTO "+ parameters +" (alpha, beta, ntopics, ndocs, nwords, niter) VALUES (?,?,?,?,?,?)");
            
            opsPHI.setExecuteBatch(1000);
            opsTHETA.setExecuteBatch(1000);
            opsTWP.setExecuteBatch(1000);
            opsDWT.setExecuteBatch(1000);
            opsPARAM.setExecuteBatch(1000);

            int numTopics = model.K;
            int numWords = model.V;
            int numDocs = model.M;
            
            
            //Save PHI (topic word probability) to Database
            System.out.println("Saving "+phi+" to database....");
            for (int topic = 0; topic < numTopics; ++topic) {
                for (int word = 0; word < numWords; word++) {
                    //insert data in database
                    opsPHI.setInt(1, topic);
                    opsPHI.setInt(2, word);
                    opsPHI.setDouble(3, model.phi[topic][word]);
                    opsPHI.executeUpdate();
                }//end inner far
            }//end topic for

            opsPHI.sendBatch();
            conn.commit();
            opsPHI.close();
            opsPHI = null;

           
            //Save THETA (document topic probability) to Database
            System.out.println("Saving "+theta+" to database....");
            for (int doc = 0; doc < numDocs; ++doc) {
                for (int topic = 0; topic < numTopics; topic++) {
                    //insert data in database
                    opsTHETA.setInt(1, doc);
                    opsTHETA.setInt(2, topic);
                    opsTHETA.setDouble(3, model.theta[doc][topic]);
                    opsTHETA.executeUpdate();
                }//end inner far
            }//end topic for
            opsTHETA.sendBatch();
            conn.commit();
            opsTHETA.close();
            opsTHETA = null;
            
           
            //Save Topic Words to Database
            System.out.println("Saving "+topicWord+" to database....");
            if (model.twords > model.V){
                    model.twords = model.V;
            }

            for (int topic = 0; topic < numTopics; topic++){
                    List<Pair> wordsProbsList = new ArrayList<Pair>(); 
                    for (int w = 0; w < numWords; w++){
                            Pair p = new Pair(w, model.phi[topic][w], false);

                            wordsProbsList.add(p);
                    }//end foreach word

                    //print topic				
                    //writer.write("Topic " + topic + "th:\n");
                    Collections.sort(wordsProbsList);

                    for (int i = 0; i < model.twords; i++){
                            if (model.data.localDict.contains((Integer)wordsProbsList.get(i).first)){
                                    String word = model.data.localDict.getWord((Integer)wordsProbsList.get(i).first);

                                    opsTWP.setInt(1, topic);
                                    opsTWP.setString(2, word);
                                    opsTWP.setDouble(3, Double.parseDouble(wordsProbsList.get(i).second+""));
                                    
                                    opsTWP.executeUpdate();
                                    //writer.write("\t" + word + " " + wordsProbsList.get(i).second + "\n");
                            }
                    }
            } //end foreach topic			
            opsTWP.sendBatch();
            conn.commit();
            opsTWP.close();
            opsTWP = null;
            
           
            //Save topic assignment to each of the words in documents
            System.out.println("Saving "+docWordTopic+" ....");
            for (int doc = 0; doc < model.data.M; doc++){
                for (int word = 0; word < model.data.docs[doc].length; ++word){
                    opsDWT.setInt(1, doc);
                    opsDWT.setInt(2, model.data.docs[doc].words[word]);
                    opsDWT.setInt(3, (Integer)(model.z[doc].get(word)).intValue());
                    opsDWT.executeUpdate();
                }
            }
            opsDWT.sendBatch();
            conn.commit();
            opsDWT.close();
            opsDWT = null;
            
            
            //Save alpha, beta, number of topics, number of document, 
            //number of words and total iterations
            System.out.println("Saving model "+parameters+" ....");
            
            opsPARAM.setDouble(1, model.alpha);
            opsPARAM.setDouble(2, model.beta);
            opsPARAM.setInt(3, model.K);
            opsPARAM.setInt(4, model.M);
            opsPARAM.setInt(5, model.V);
            opsPARAM.setInt(6, model.liter);
            
            opsPARAM.executeUpdate();
            opsPARAM.sendBatch();
            conn.commit();
            opsPARAM.close();
            opsPARAM = null;
            */
            //conn.close();
        }//end method saveModel
        
        
        
        public static void loadModel(Model model, String modelName)throws Exception
        {
            /*
            conn = (OracleConnection)DBConnect.getInstance ().getConnection ();
            Statement stmt = conn.createStatement();
  
            String docWordTopic = modelName + "DocWordTopic_" + LDA.trnFeatureTableNAme;
            String parameters = modelName + "Parameters_"+LDA.trnFeatureTableNAme;

        
            System.out.println("Loading model paramteres....");
            //Load alpha, beta, ntopics, ndocs, nwords, niter            
            String queryParam = "select * from " + parameters;
            ResultSet rs = stmt.executeQuery(queryParam);
            while(rs.next()){
                model.alpha = rs.getDouble(1);
                model.beta = rs.getDouble(2);
                model.K = rs.getInt(3);
                model.M = rs.getInt(4);
                model.V = rs.getInt(5);
                model.liter = rs.getInt(6);
            }
            rs.close(); rs=null;
            
        
            System.out.println("Loading Document word topic assignments....");
            String queryDWT = "select * from "+docWordTopic+" order by doc asc";
            ResultSet rsDWT = stmt.executeQuery(queryDWT);
            
            model.z = new Vector[model.M]; //K
            model.data = new LDADataset(model.M);
            model.data.V = model.V;			
            
            int current_id = -1;
            int c_id = -1;
            int i = 0;
            int count = 0;
            int wordID;
            int topicID;
            int docID = 0;
            
            Vector<Integer> words = null;
            Vector<Integer> topics = null;
            Document doc =null;
            
            while (rsDWT.next ()){
                c_id = rsDWT.getInt(1);
                wordID = rsDWT.getInt(2);
                topicID = rsDWT.getInt(3);
                
                if (c_id != current_id){
                    if (words != null){
                        //allocate and add new document to the corpus
                        doc = new Document(words);
                        //System.out.println("adding document " + current_id);
                        model.data.setDoc(doc, current_id);
                        //assign values for z
                        model.z[current_id] = new Vector<Integer>();
			for (int j = 0; j < topics.size(); j++)
                            model.z[current_id].add(topics.get(j));
                    }
                    words = new Vector<Integer>();
                    topics = new Vector<Integer>();
                    current_id = c_id;
                }
                words.add(wordID);
                topics.add(topicID);

                i++;
              if (i%100000 == 0)
                System.out.println (i + " records read");
                
        }//end rs.next while    
            doc = new Document(words);
            model.data.setDoc(doc, current_id);
            //assign values for z
            model.z[current_id] = new Vector<Integer>();
            for (int j = 0; j < topics.size(); j++)
                model.z[current_id].add(topics.get(j));
          
            rsDWT.close(); rsDWT = null;
            stmt.close(); stmt = null;
          */
          /*
			for (i = 0; i < M; i++){
				line = reader.readLine();
				StringTokenizer tknr = new StringTokenizer(line, " \t\r\n");
				
				int length = tknr.countTokens();
				
				Vector<Integer> words = new Vector<Integer>();
				Vector<Integer> topics = new Vector<Integer>();
				
				for (j = 0; j < length; j++){
					String token = tknr.nextToken();
					
					StringTokenizer tknr2 = new StringTokenizer(token, ":");
					if (tknr2.countTokens() != 2){
						System.out.println("Invalid word-topic assignment line\n");
						return false;
					}
					
					words.add(Integer.parseInt(tknr2.nextToken()));
					topics.add(Integer.parseInt(tknr2.nextToken()));
				}//end for each topic assignment
				
				//allocate and add new document to the corpus
				Document doc = new Document(words);
				this.data.setDoc(doc, i);
				
				//assign values for z
				this.z[i] = new Vector<Integer>();
				for (j = 0; j < topics.size(); j++){
					this.z[i].add(topics.get(j));
				}
				
			}//end for each doc
           
           */  
            
        }//end loadModel()
        
        public static void main(String arg[]){
            
            Model model = new Model();
            try{
                loadModel(model, "Final");
            }catch(Exception e){
                e.getMessage();e.printStackTrace();
            }
            System.out.println("Loading Dict....");
            Dictionary dict = new Dictionary();
            dict.readWordMap(null);
	System.out.println("Dict Size = " + dict.word2id.size());
            Iterator<String> it = dict.word2id.keySet().iterator();
			while (it.hasNext()){
				String key = it.next();
				Integer value = dict.word2id.get(key);				
				System.out.println(key + " " + value);

			}
            
            System.out.println("alpha= " + model.alpha );
            System.out.println("beta= " + model.beta );
            System.out.println("numtopics= " + model.K );
            System.out.println("numdocs= " + model.M );
            System.out.println("liter= " + model.liter );
            System.out.println("numwords= " + model.V );
            
            System.out.println("Doc word topcis .....  "  );
            for (int doc = 0; doc < model.data.M; doc++){
                System.out.print("Doc " + doc);
                for (int word = 0; word < model.data.docs[doc].length; ++word){
                    
                    System.out.print(" " + model.data.docs[doc].words[word]+":"+ (Integer)(model.z[doc].get(word)).intValue());
                }
                System.out.println();
            }
            
            
        }//end main
}
