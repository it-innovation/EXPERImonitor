package uk.ac.open.kmi.analysis.Buzz.Sentiment;

import java.util.HashMap;

/**
 * User: mcr266
 * Date: Nov 29, 2010
 * Time: 5:14:43 PM
 */
public class PolarityDecider {
    public static double getStatusPolarity(String text, String language, HashMap<String,Double> posList, HashMap<String,Double> negList) {

        // remove stop words
        text = StopWords.removeStopWords(text, language);

        // tokenise the text
        String[] tks = text.split(" ");


        // initialise the polarity
        double polarity = 0;
        int poleWordCount = 0;
        for (String tk : tks) {

            // get the pos weight of the token
            if(posList.containsKey(tk)) {
                polarity += posList.get(tk);
                poleWordCount++;
            }


            // get the neg weight of the token
            if(negList.containsKey(tk)) {
                polarity -= negList.get(tk);
                if(!posList.containsKey(tk))
                    poleWordCount++;
            }
        }

        // normalise the polarity score
        if(poleWordCount > 0)
            polarity = polarity / poleWordCount;

        return polarity;
    }
}
