package uk.ac.open.kmi.analysis.Buzz;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import uk.ac.open.kmi.analysis.Buzz.Sentiment.ListLoader;
import uk.ac.open.kmi.analysis.Buzz.Sentiment.PolarityDecider;
import uk.ac.open.kmi.analysis.core.Post;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import com.representqueens.lingua.en.Fathom;
import com.representqueens.lingua.en.Fathom.Stats;
import com.representqueens.lingua.en.Readability;
import java.text.DecimalFormat;
import java.util.*;
import uk.ac.open.kmi.analysis.core.Language;

public class BuzzPrediction {

    private Vector<Post> inputPosts;
    private Vector<PostFeatures> inputPostFeatures;
    private HashMap<String, Double> mostBuzzPosts;
    private HashMap<String, Double> mostBuzzUsers;
    private double maxReadability = -1;
    private HashMap<String, Double> pos;
    private HashMap<String, Double> neg;
    private Classifier classifier;
    private double maxIndeg = 1;      //maximum indegree
    private double maxNumlists = 1;   //maximum lists - these two will be used to normalise the alternative buzz 
    //value in the measureBackupValue(PostFeatures pf) 
    
    private String language = Language.ENGLISH;
    
    
    public BuzzPrediction(String language){
        
        if(language.equals(Language.ENGLISH)){
            this.language = language;
            this.pos = ListLoader.getPosEnglish();
            this.neg = ListLoader.getNegEnglish();
            this.classifier = DatSaverLoader.loadClassifier("./data/ClassifierEnglish"); //load the classifier
        }
        else if(language.equals(Language.GERMAN)){
            this.language = language;
            this.pos = ListLoader.getPosGerman(); 
            this.neg = ListLoader.getNegGerman(); 
            this.classifier = DatSaverLoader.loadClassifier("./data/ClassifierGerman"); //load the classifier
        }
        else{
            throw new IllegalArgumentException("only two languages allowed: " + 
                    Language.ENGLISH + " or " + Language.GERMAN);
        }    
    }
    

    public void setInputPosts(Vector<Post> poIn) {
        this.inputPosts = poIn;
        calculate();
    }

    /**
     * Populates the post features and performs the analysis
     */
    private void calculate() {
        makePostFeatures();
        BuzzRankPostAndUsers();
        backUpPlan(); //if the posts that were input were all cut in the seed identification we need an alternative plan to rank posts and users;
    }

    private void backUpPlan() {
        if (mostBuzzPosts.size() == 0) {
            System.out.println("Plan B");
            for (PostFeatures pf : this.inputPostFeatures) { //for all input posts features
                double buzzValue = measureBackupValue(pf); //calculate its value
                this.mostBuzzPosts.put(pf.getPostID(), buzzValue); //set it to the hashmap
                Double authorThere = this.mostBuzzUsers.get(pf.getAuthorID()); //update the authors hashmap
                if (authorThere == null) {
                    this.mostBuzzUsers.put(pf.getAuthorID(), buzzValue);
                } else {
                    authorThere += buzzValue;
                    this.mostBuzzUsers.put(pf.getAuthorID(), authorThere);
                }
            }
        }
    }

    /**
     * Simplistic measure. The most buzz is given to the post whose author is
     * more listed and has the most inDegree and based on the readability of the
     * post.
     *
     * @param pf
     * @return
     */
    private double measureBackupValue(PostFeatures pf) {
        double W_List = 0.3; //more value is given to lists
        double W_Indegree = 0.4;
        double W_Readab = 0.3;

        double result = W_List * ((pf.getAuthorNumlists() != null) ? (pf.getAuthorNumlists() / this.maxNumlists) : 0)
                + W_Indegree * ((pf.getAuthorIndegree() != null) ? (pf.getAuthorIndegree() / this.maxIndeg) : 0)
                + W_Readab * ((pf.getReadability() != null) ? (pf.getReadability() / this.maxReadability) : 0);

        return result;
    }

    /**
     * This method checks if the posts are going to generate any buzz (via the
     * isSeed(PostFeatures p) and how much buzz they are going to generate (via
     * getBuzzValue(PostFeatures p)). The output is stored in the hashmaps which
     * are then called for output.
     */
    private void BuzzRankPostAndUsers() {

        this.mostBuzzPosts = new HashMap<String, Double>(); //initialise hashmaps
        this.mostBuzzUsers = new HashMap<String, Double>();
        for (PostFeatures pf : this.inputPostFeatures) { //for all input posts features
            if (isSeed(pf)) { //if the post is a seed
                double buzzValue = measureBuzzValue(pf); //calculate its value
                this.mostBuzzPosts.put(pf.getPostID(), buzzValue); //set it to the hashmap
                Double authorThere = this.mostBuzzUsers.get(pf.getAuthorID()); //update the authors hashmap
                if (authorThere == null) {
                    this.mostBuzzUsers.put(pf.getAuthorID(), buzzValue);
                } else {
                    authorThere += buzzValue;
                    this.mostBuzzUsers.put(pf.getAuthorID(), authorThere);
                }
            }
        }
    }

    /**
     * Based on the regression analysis the expected buzz is calculated from the
     * top 3 features see section 6.3.1 of IEEE paper and Table VI for the
     * weights of the features. [Informativeness is not included because it's
     * time consuming to produce]
     *
     * @param pf
     * @return
     */
    private double measureBuzzValue(PostFeatures pf) {
        double buzzValue = 0.0;
        if(Language.isEnglish(this.language)){
            buzzValue =
                + 0.0043 * pf.getReadability()
                - 0.5842 * pf.getReferalCount()
                - 0.0028 * pf.getTimeInTheday();
        }
        else if(Language.isGerman(this.language)){
            buzzValue =      -0.0016 * pf.getAuthorPostRate() +
                              0.0001 * pf.getAuthorNumlists() +
                             -0.0834 * pf.getReferalCount();
      
        }

        return Math.pow(2, buzzValue); //this is done to return a positive value
    }

    /**
     * Using the classifier pre calculated we estimate if a post is likely to be
     * seed or not.
     *
     * @param pf
     * @return
     */
    private boolean isSeed(PostFeatures pf) {
        double seed;
        try {
            Instance instance = buildDatasetSkeleton(pf).instance(0);
            seed = classifier.classifyInstance(instance);
            // result= 1 negative
            // result =0 positive is a seed post.
            if (seed == 0) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Transforms the post feature in an instance understandable by the
     * classifier.
     *
     * @param pf
     * @return
     * @throws Exception
     */
    private Instances buildDatasetSkeleton(PostFeatures pf) throws Exception {

        Attribute userNumOfPosts         = new Attribute("user_num_posts");       
        Attribute userNumFollows         = new Attribute("user_num_follows");     
        Attribute userNumOfFollowers     = new Attribute("user_num_followers");
        Attribute userAge                = new Attribute("user_age");
        Attribute userPostRate           = new Attribute("user_post_rate");
        Attribute userNumLists           = new Attribute("user_num_lists");
        Attribute contentLength          = new Attribute("content_length");
        Attribute contentComplexity      = new Attribute("content_complexity");
        Attribute contentReadability     = new Attribute("content_readability");
        Attribute contentReferralCount   = new Attribute("content_referral_count");
        Attribute contentTimeInDay       = new Attribute("content_time_in_day");
        Attribute contentInformativeness = new Attribute("content_informativeness");
        Attribute contentPolarity        = new Attribute("content_polarity");

        // Class Attribute
        FastVector fvClassVal = new FastVector(2);
        fvClassVal.addElement("pos");
        fvClassVal.addElement("neg");
        Attribute ClassAttribute = new Attribute("theClass", fvClassVal);

        // declare the feature vector
        FastVector fvWekaAttributes = new FastVector(14);

        fvWekaAttributes.addElement(userNumOfPosts);
        fvWekaAttributes.addElement(userNumFollows);
        fvWekaAttributes.addElement(userNumOfFollowers);
        fvWekaAttributes.addElement(userAge);
        fvWekaAttributes.addElement(userPostRate);
        fvWekaAttributes.addElement(userNumLists);
        fvWekaAttributes.addElement(contentLength);
        fvWekaAttributes.addElement(contentComplexity);
        fvWekaAttributes.addElement(contentReadability);
        fvWekaAttributes.addElement(contentReferralCount);
        fvWekaAttributes.addElement(contentTimeInDay);
        fvWekaAttributes.addElement(contentInformativeness);
        fvWekaAttributes.addElement(contentPolarity);

        fvWekaAttributes.addElement(ClassAttribute);

        Instances dataset = new Instances("Rel", fvWekaAttributes, 7);
        dataset.setClass(ClassAttribute);

        //System.out.println(dataset);
        Instance inst = new Instance(14);

        inst.setValue(userNumOfPosts, 0);
        inst.setValue(userNumOfFollowers, pf.getAuthorIndegree());
        inst.setValue(userNumFollows, pf.getAuthorOutdegree());
        inst.setValue(userNumLists, pf.getAuthorNumlists());
        inst.setValue(userAge, pf.getAuthorAge());
        inst.setValue(userPostRate, pf.getAuthorPostRate());
        inst.setValue(contentLength, 0);
        inst.setValue(contentComplexity, 0);
        inst.setValue(contentReadability, pf.getReadability());
        inst.setValue(contentReferralCount, pf.getReferalCount());
        inst.setValue(contentTimeInDay, pf.getTimeInTheday());
        inst.setValue(contentInformativeness, 0);
        inst.setValue(contentPolarity, pf.getPolarity());
        inst.setValue(ClassAttribute, "pos");

        dataset.add(inst);

        // remove the atributes that are not top 4 from the dataset
        Remove remove = new Remove();
        
        if(Language.isEnglish(this.language)){
            int[] ids = {0, 2, 3, 4, 5, 6, 7, 9, 11};
            remove.setAttributeIndicesArray(ids);
        }
        else if(Language.isGerman(language)){
            int[] ids = { 0, 6, 7, 11 };
            remove.setAttributeIndicesArray(ids);
        }
        
        remove.setInputFormat(dataset);
        dataset = Filter.useFilter(dataset, remove);

        return dataset;
    }

    /**
     * Create the post features from the posts.
     */
    private void makePostFeatures() {
        this.inputPostFeatures = new Vector<PostFeatures>();

        for (Post p : this.inputPosts) {
            PostFeatures pf = new PostFeatures();
            pf.setPostID(p.getPostID());
            pf.setAuthorID(p.getAuthorID());
            pf.setAuthorOutdegree(calculateAuthorOutdegree(p));
            pf.setAuthorIndegree(calculateAuthorIndegree(p));
            pf.setAuthorNumlists(calculateAuthorNumlists(p));
            pf.setAuthorAge(calculateAuthorAge(p));
            pf.setAuthorPostRate(calculateAuthorPostRate(p));
            pf.setPolarity(calculatePolarity(p));
            pf.setReadability(calculateReadability(p));
            pf.setReferalCount(calculateReferalCount(p));
            pf.setTimeInTheday(calculateTimeInTheDay(p));
            this.inputPostFeatures.add(pf);
        }

    }

    private double calculateTimeInTheDay(Post p) {
        double timeInTheDay = estimateMinutes(p);
        return timeInTheDay;
    }

    private double calculateAuthorIndegree(Post p) {
        double indegree = p.getAuthorInDegree();
        if (this.maxIndeg < indegree) {
            this.maxIndeg = indegree;
        }
        return indegree;
    }

    private java.lang.Double calculateAuthorNumlists(Post p) {
        double lists = p.getAuthorNumLists();
        if (this.maxNumlists < lists) {
            this.maxNumlists = lists;
        }
        return lists;
    }

    private double calculateAuthorAge(Post p) {
        return p.getAuthorAge();
    }

    private double calculateAuthorPostRate(Post p) {
        return p.getAuthorPostRate();
    }

    private double estimateMinutes(Post p) {
        double dayTime = 0;
        String dateCreated = p.getDateCreated().toString();
        String time = dateCreated.substring(dateCreated.indexOf(' ') + 1);
        String hours = time.substring(0, 2);
        String minutes = time.substring(3, 5);
        String seconds = time.substring(6, 7);
        dayTime = Double.valueOf(hours) * 60 + Integer.valueOf(minutes);
        if (Integer.valueOf(seconds) > 30) {
            dayTime += 1;
        }
        return dayTime;
    }

    private double calculateReferalCount(Post p) {
        double referalCnt = 0;
        String textContent = p.getTextContent();
        int firstRef = textContent.indexOf("http");
        if (firstRef > -1) {
            referalCnt++;
            int secondRef = textContent.indexOf("http", firstRef + 4);
            if (secondRef > -1) {
                referalCnt++;
                int thirdRef = textContent.indexOf("http", secondRef + 4);
                if (thirdRef > -1) {
                    referalCnt++;
                }
            }
        }
        return referalCnt;
    }

    private double calculateReadability(Post p) {
        if (Language.isEnglish(this.language)) {
            return calculateReadabilityEnglish(p);
        } else if (Language.isGerman(this.language)) {
            return calculateReadabilityGerman(p);
        }
        return calculateReadabilityEnglish(p);
    }

    private double calculateReadabilityEnglish(Post p) {
        Stats st = Fathom.analyze(p.getTextContent());
        double readability = Readability.calcFog(st);
        if (Double.isInfinite(readability)) {
            readability = this.maxReadability;
        } else {
            if (readability > this.maxReadability) {
                this.maxReadability = readability;
            }
        }
        return readability;
    }

    private double calculateReadabilityGerman(Post p) {
        //Use the LIX formula Lix (text) = count (words) / count (sentences) + count (words>6letters) * 100/count (words) 
        double readability = 0;
        String[] wordList = p.getTextContent().split(" ");
        int numWords = wordList.length;
        int numComplexWords = 0;
        for (int i = 0; i < wordList.length; i++) {
            String word = wordList[i];
            if (word.length() > 6) {
                numComplexWords++;
            }
        }
        int numSentences = getSentenceList(p.getTextContent()).size();
        if (numSentences > 0 && numWords > 0) {
            readability = (1.0 * numWords / numSentences) + (100.0 * numComplexWords / numWords);
        } else {
            readability = -1;
        }

        if (Double.isInfinite(readability)) {
            readability = this.maxReadability;
        } else {
            if (readability > this.maxReadability) {
                this.maxReadability = readability;
            }
        }
        return readability;
    }

    private ArrayList<String> getSentenceList(String text) {
        String PUNCTION = ".;!?;";
        ArrayList<String> sentenceList = new ArrayList<String>();
        int indexBegin = 0;
        int indexEnd = 0;
        String sentence = null;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (PUNCTION.indexOf(c) != -1) {
                indexEnd = i;
                if (indexEnd > indexBegin) {
                    sentence = text.substring(indexBegin, indexEnd);
                    sentenceList.add(sentence.trim());
                }
                indexBegin = indexEnd + 1;
            }
        }
        if (indexBegin < text.length() - 1) {
            sentence = text.substring(indexBegin, text.length());
            sentenceList.add(sentence.trim());
        }
        return sentenceList;
    }

    private double calculatePolarity(Post p) {
        double polarity = PolarityDecider.getStatusPolarity(p.getTextContent(), this.language,
                this.pos, this.neg);
        return polarity;
    }

    private double calculateAuthorOutdegree(Post p) {
        double authorOutDegree = p.getAuthorOutDegree();
        return authorOutDegree;
    }

    /**
     * This is one of the two output functions. Asks this.mostBuzzPosts for the
     * top k posts based on their buzz value previously calculated.
     *
     * @param k
     * @return
     */
    public Vector<Entry<String, Double>> getTopKMostBuzzPosts(int k) {
        Vector<Entry<String, Double>> sortedEntries = new Vector<Entry<String, Double>>();

        int i = 0;
        for (Entry<String, Double> entry : entriesSortedByValues(this.mostBuzzPosts)) {
            if (i < k) {
                sortedEntries.add(entry);
                i++;
            }
        }
        return sortedEntries;
    }

    /**
     * This is one of the two output functions. Asks this.mostBuzzUsers for the
     * top k users based on the buzz value of their posts as previously
     * calculated.
     *
     * @param k
     * @return
     */
    public Vector<Entry<String, Double>> getTopKMostBuzzUsers(int k) {

        Vector<Entry<String, Double>> sortedEntries = new Vector<Entry<String, Double>>();

        int i = 0;
        for (Entry<String, Double> entry : entriesSortedByValues(this.mostBuzzUsers)) {
            if (i < k) {
                sortedEntries.add(entry);
                i++;
            }
        }
        return sortedEntries;
    }

    private static <String, Double extends Comparable<? super Double>> SortedSet<Map.Entry<String, Double>> entriesSortedByValues(
            Map<String, Double> map) {
        SortedSet<Map.Entry<String, Double>> sortedEntries = new TreeSet<Map.Entry<String, Double>>(
                new Comparator<Map.Entry<String, Double>>() {

                    @Override
                    public int compare(Map.Entry<String, Double> e1,
                            Map.Entry<String, Double> e2) {
                        int res = e2.getValue().compareTo(e1.getValue());
                        return res != 0 ? res : 1;
                    }
                });
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }
}
