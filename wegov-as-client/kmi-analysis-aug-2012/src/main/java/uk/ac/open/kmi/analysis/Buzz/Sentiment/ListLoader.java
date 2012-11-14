package uk.ac.open.kmi.analysis.Buzz.Sentiment;

/**
 * User: mcr266
 * Date: Nov 29, 2010
 * Time: 5:12:45 PM
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: Matt
 * Date: Jun 22, 2010
 */
public class ListLoader {
    
    public static String GERMAN_POLARITY_POSITIVE_LIST = "data/lexicons/German/SentiWS_v1.8c_Positive.txt";
    public static String GERMAN_POLARITY_NEGATIVE_LIST = "data/lexicons/German/SentiWS_v1.8c_Negative.txt"; 
    public static String ENGLISH_POLARITY_POSITIVE_LIST = "data/lexicons/English/SentiWordNet.txt";
    public static String ENGLISH_POLARITY_NEGATIVE_LIST = "data/lexicons/English/SentiWordNet.txt";      
    

    public static void main(String[] args) {
        HashMap<String,Double> posWords = getPosEnglish();
        System.out.println("Positive Words");
        for (String s : posWords.keySet()) {
            System.out.println(s);
        }

        HashMap<String,Double> negWords = getNegEnglish();
        System.out.println("Negative Words");
        for (String s : negWords.keySet()) {
            System.out.println(s);
        }
    }

    /*
     * Returns a list of positive sentiment words and their pos scores
     */
    public static HashMap<String,Double> getPosEnglish() {
        HashMap<String,Double> posWords = new HashMap<String,Double>();

        String sentiPath = ENGLISH_POLARITY_POSITIVE_LIST;

        try {
            BufferedReader in = new BufferedReader(new FileReader(sentiPath));
            String str;
            while ((str = in.readLine()) != null) {
                String[] tokens = str.split("\t");

                double posScore = Double.parseDouble(tokens[2]);
                if(posScore > 0.0) {
                    String[] posTokens = tokens[4].split(" ");
                    for (int i = 0; i < posTokens.length; i++) {
                        String posToken = posTokens[i];
                        // clean the token
                        posWords.put(posToken.split("#")[0],posScore);
                    }
                }
            }
            in.close();
        } catch (Exception e) {
        }

        return posWords;
    }

    public static HashMap<String,Double> getNegEnglish() {
        HashMap<String,Double> negWords = new HashMap<String,Double>();
        String sentiPath = ENGLISH_POLARITY_NEGATIVE_LIST;

        try {
            BufferedReader in = new BufferedReader(new FileReader(sentiPath));
            String str;
            while ((str = in.readLine()) != null) {
                String[] tokens = str.split("\t");

                double negScore = Double.parseDouble(tokens[3]);
                if(negScore > 0.0) {
                    String[] posTokens = tokens[4].split(" ");
                    for (int i = 0; i < posTokens.length; i++) {
                        String posToken = posTokens[i];
                        // clean the token
                        negWords.put(posToken.split("#")[0],negScore);
                    }
                }
            }
            in.close();
        } catch (Exception e) {
        }
        return negWords;
    }
    
    
     /*
     * Returns a list of positive sentiment words and their pos scores
     */    
    public static HashMap<String,Double> getPosGerman() {
        return loadPolarityList(GERMAN_POLARITY_POSITIVE_LIST);
    }
    
    /*
     * Returns a list of positive sentiment words and their neg scores
     */     
    public static HashMap<String,Double> getNegGerman() {
        return loadPolarityList(GERMAN_POLARITY_NEGATIVE_LIST);
    }    
    
    //=================================//    
    //  PRIVATE FUNCIONS
    //=================================//    
   
    private static HashMap<String, Double> loadPolarityList(String fileName){
        HashMap<String, Double> termPolarityScores = new HashMap<String, Double>();
        BufferedReader reader = null;
        try {
            
            reader = new BufferedReader(new FileReader(fileName));
            String line;
            //Abbau|NN	-0.058	Abbaus,Abbaues,Abbauen,Abbaue
            //We take every word and add it with the score in lowercase
            while((line=reader.readLine()) !=null){
                String [] elements = line.split("\t");
                Double score = Double.parseDouble(elements[1].trim());
                termPolarityScores.put(elements[0].split("\\|")[0].trim().toLowerCase(), score);
                              
                if (elements.length > 2){
                    String [] terms = elements[2].split(",");                    
                    for (int i = 0; i < terms.length; i++) {
                        termPolarityScores.put(terms[i].trim().toLowerCase(), score);                    
                    }
                }
                
            }
            
        } catch (Exception ex) {
            Logger.getLogger(ListLoader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
                Logger.getLogger(ListLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return termPolarityScores;
    }    
   

}

