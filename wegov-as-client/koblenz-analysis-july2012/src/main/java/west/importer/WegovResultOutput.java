package west.importer;

import java.util.*;

import west.wegovdemo.*;
import jgibblda.*;

public class WegovResultOutput  
{
   public static int LIWC_K = 10;
	
   public String title;
   public ArrayList body;

   public ArrayList topicTerms;
   public ArrayList topicTermScores;
   
   public static int numRelDocs = 3;

   public ArrayList bestDocs;

   WegovImporter parent;
   
   public static int numCandidates = 20;
   
   public ArrayList getTermsForTopic (int i)
   {
	   return (ArrayList) topicTerms.get(i);  
   }
   
   public double[] getTermScoresForTopic(int i)
   {
	   return (double[]) topicTermScores.get(i);
   }
   
   public int getNumPostingsForTopic (int i)
   {
	   return ((ArrayList)bestDocs.get(i)).size();
   }
   
   public CollectionArticle getBestPostingForTopic (int topic, int pos)
   {
	   return (CollectionArticle)((ArrayList)bestDocs.get(topic)).get(pos);
   }
   
   
   public WegovResultOutput (WegovImporter p)
   {
     parent = p;
   }

   public TopicOpinionOutput prepareOutput()
   {
	  WegovOutput output = new WegovOutput(); 
	   
	  doDictionary(output);
	  doTopics(output);
      doDocs(output); 
      doAllDocs(output);
      doControversity(output);
      doTopicTermScores(output);
      
      return output;
   }
   
   public void doDictionary(WegovOutput output)
   {
	   HashMap aggregate = new HashMap();
	   
	   Iterator it = parent.articles4output.values().iterator();
	   while(it.hasNext())
	   {
		   CollectionArticle ca = (CollectionArticle) it.next();
		   HashMap wordmap = ca.terms;
		   Iterator it1 = wordmap.entrySet().iterator();
		   while(it1.hasNext())
		   {
			   Map.Entry entry = (Map.Entry)it1.next();
			   String stem = (String) entry.getKey();
			   HashMap words = (HashMap) entry.getValue();
			   
			   HashMap featureTerms = (HashMap)aggregate.get(stem);
			   if (featureTerms == null)
			   {
				   HashMap newinputs = new HashMap();
				   Iterator it3 = words.entrySet().iterator();
				   while(it3.hasNext())
				   {
					   Map.Entry e = (Map.Entry) it3.next();
					   String a = (String)e.getKey();
					   myInteger b = (myInteger)e.getValue();
					   newinputs.put(a, new myInteger(b.intValue()));
				   }	   
				   aggregate.put(stem, newinputs);
			   }	   
			   else
			   {
				   Iterator it2 = words.entrySet().iterator();
				   while (it2.hasNext())
				   {
					   Map.Entry ent = (Map.Entry) it2.next();
					   String wrd = (String) ent.getKey();
					   myInteger mi = (myInteger) ent.getValue();
					   
					   myInteger cnt = (myInteger)featureTerms.get(wrd);
					   if (cnt == null)
					   {
						 featureTerms.put(wrd, new myInteger(mi.intValue()));   
					   }	
					   else
					   {
						 cnt.increase(mi.intValue());
					   }	   
				   }	   
			   }
		   }	   
	   }	 
	   
	   output.termDictionary = new HashMap();
	   Iterator it5 = aggregate.entrySet().iterator();
	   while (it5.hasNext())
	   {
		   Map.Entry tmp = (Map.Entry)it5.next();
		   String stem = (String) tmp.getKey();
		   HashMap words = (HashMap)tmp.getValue();
		   
		   Iterator it6 = words.entrySet().iterator();
		   int tf = 0;
		   String bestterm = null;
		   while (it6.hasNext())
		   {
			   Map.Entry tmp1 = (Map.Entry) it6.next();
			   String tempword = (String)tmp1.getKey();
			   int tempval  = ((myInteger)tmp1.getValue()).intValue();
			   if (tempval > tf)
			   {
				   tf = tempval;
				   bestterm = tempword;
			   }	   
		   }	 
		   output.termDictionary.put(stem, bestterm);
	   }
   }

   public void doTopicTermScores(WegovOutput output)
   {
	   output.topicTermScores = parent.trnModel.phi;
   }
   
   public void doControversity(WegovOutput output)
   {
	     Model model = parent.trnModel;
	     
	     double[] topic_liwc = new double[model.K];
	     double[] topic_controversity = new double[model.K]; 
	   
	     output.opinionHistograms = new ArrayList(model.K);
	     
	     for (int i = 0; i < model.K; ++i)
	     {	    
	            double[] doctopic = new double[model.M];
	            for (int j=0; j<model.M; j++)
	            	doctopic[j] = model.theta[j][i];
	            
	            int[] topDocIdx = WestUtil.topThreshold(doctopic, SessionBuffer.relevanceThreshold);
	            
	            double[] posemo = new double[topDocIdx.length];
	            double[] negemo = new double[topDocIdx.length];
	            double[] sumemo = new double[topDocIdx.length];
	            
	            
	        
	            ArrayList[] histogram = new ArrayList[WegovOutput.opinionHistogramLabels.length];
	            for (int d=0; d< histogram.length; d++)
	            	histogram[d] = new ArrayList();
	            
	            for (int j=0; j<topDocIdx.length; j++)
	            {
	            	CollectionArticle c = (CollectionArticle)parent.articles4output.get(new Integer(topDocIdx[j]));
	            	if (c!= null)
	            	{
	            		posemo[j]=c.liwc_pos;
	            		negemo[j]=c.liwc_neg;
	            		sumemo[j]=c.liwc_sum;
	            		
	            		int m = 0;
	            		while (m < WegovOutput.opinionHistogramBorders.length -1)
	            		{
	            			if (c.liwc_sum > WegovOutput.opinionHistogramBorders[m])
	            				m++;
	            			else
	            				break;
	            		}	
	            		histogram[m].add(new Integer(topDocIdx[j]));
	            	}
	            	else
	            	{
	            		posemo[j]=-1.0;
	            		negemo[j]=-1.0;
	            		sumemo[j]=-1.0;
	            	}

	            }
	            
	            int[][] histogramInt = new int[histogram.length][];
            	for (int r = 0; r<histogram.length; r++)
            	{
            		int[] tmp = new int [histogram[r].size()];
            		for (int w = 0; w< histogram[r].size(); w++)
            			tmp[w] = ((Integer)histogram[r].get(w)).intValue();
            		histogramInt [r] = tmp;
            	}
            	output.opinionHistograms.add(i, histogramInt);
	            
	            double avg_posemo = 0.0;
	            double avg_negemo = 0.0;
	            double avg_sumemo = 0.0;
	            double count = 0.0;
	            
	            for (int q=0; q< posemo.length; q++)
	            {
	            	if (posemo[q] >= 0)
	            	{	
	            		avg_posemo += posemo[q];
	            		avg_negemo += negemo[q];
	            		avg_sumemo += sumemo[q];
	            		count = count + 1.0;
	            	}
	            }
	            if (count > 0)
	            {	
	            	avg_posemo = avg_posemo / count;
	            	avg_negemo = avg_negemo / count;
	            	avg_sumemo = avg_sumemo / count;
	            }
	            
	            // now compute variance = controversity
	            
	            double contr = 0.0;
	            if (count > 1.0)
	            {
	            	double sum1 = 0.0;
	            	double sum2 = 0.0;
	            	for (int q=0; q< posemo.length; q++)
		            {
	            		if (sumemo[q] >= 0)
	            		{
	            			sum1 += sumemo[q];
	            			sum2 += sumemo[q] * sumemo[q];
	            		}
		            }
	            	contr = 1.0 / (count - 1.0) * (sum2 - 1.0 / count * sum1 * sum1); // 1-step variance computation
	            }
	            
	            topic_controversity[i] = contr;
	            topic_liwc [i] = avg_sumemo;          
	     }
	     output.topicControversity = topic_controversity;
	     output.topicValence = topic_liwc;
   }
   
   public void doTopics(WegovOutput output)
   {
	 int numTermsPerTopic = SessionBuffer.getNumTermsPerTopic();  
	   
     Model model = parent.trnModel;
     topicTerms = new ArrayList();
     output.topicTermProbs = new double[model.K][];
     
     output.topicTerms = new String[model.K][numTermsPerTopic];
     
     for (int i = 0; i < model.K; ++i)
     {
       int[] topWordIdx = WestUtil.topK(model.phi[i], numTermsPerTopic);
       double[] topWordProbs = WestUtil.topK_prob(model.phi[i], numTermsPerTopic);
       
       ArrayList result = new ArrayList();
       
       for (int j = 0; j < numTermsPerTopic; ++j)
       {
    	   String stem = model.data.localDict.id2word.get(topWordIdx[j]);
    	   String word = (String)output.termDictionary.get(stem);
    	   if (word != null)
    	      output.topicTerms[i][j] = word;
    	   else
     	      output.topicTerms[i][j] = stem;
       }
       output.topicTermProbs[i] = topWordProbs;
     }
   }

   public void doDocs(WegovOutput output)
   {
     Model model = parent.trnModel;
     
	 output.topicRelevantDocIds = new int[model.K][];
	 output.topicRelevantDocScores = new double[model.K][];

	 output.topicOpinionDocIds = new int[model.K][];
	 output.topicOpinionDocScores = new double[model.K][];
	 output.topicOpinionDocReasons = new String[model.K][];
	 
	 output.topicUsers = new String[model.K][];
	 
     for (int i = 0; i < model.K; ++i)
     {	    
            double[] doctopic = new double[model.M];
            for (int j=0; j<model.M; j++)
            	doctopic[j] = model.theta[j][i];
            
            int[] topDocIdx = WestUtil.topThreshold(doctopic, SessionBuffer.relevanceThreshold);
            
            //int[] topDocIdx = WestUtil.topK(doctopic, numCandidates);

            ArrayList candidates = new ArrayList();
            
            HashSet users = new HashSet();
            
            for (int j=0;j<topDocIdx.length;j++)
            {
            	CollectionArticle c = (CollectionArticle)parent.articles4output.get(new Integer(topDocIdx[j]));
            	if (c!= null)
            	{
            	  c.score = doctopic[topDocIdx[j]];
            	  candidates.add(c);
            	}
            	  
            }

            ArrayList finalists = new ArrayList();
            for (int m=0; m<numRelDocs && m<candidates.size(); m++)
            {
              CollectionArticle tmp = (CollectionArticle) candidates.get(m);
              tmp.reason = "relevance";
              finalists.add(tmp);

              //OPTIONAL !!!!!!!!
              //candidates.remove(tmp);
              users.add(tmp.user);
            }

            int size = finalists.size();
            int[] rowIDs = new int[size];
            String[] rowReasons = new String[size];
            double[] rowScores = new double[size];
            
            for (int q=0; q<size;q++)
            {
         	   CollectionArticle ca = (CollectionArticle)finalists.get(q);
         	   rowIDs[q] = (int)ca.did;
         	   rowScores[q] = ca.score;
         	   rowReasons[q] = ca.reason;
            }
            
            output.topicRelevantDocIds[i] = rowIDs;
            output.topicRelevantDocScores[i] = rowScores;

            size = users.size();
            String[] usernames = new String[size];
            int x = 0;
            Iterator it = users.iterator();
            while (it.hasNext())
            {
            	usernames[x] = (String)it.next();
            	x++;
            }	
            it=null;
           
            output.topicUsers [i] = usernames;
            
            //=====================================================================================

            finalists = new ArrayList();
            
            it = OpinionMiner.termLists.keySet().iterator();
            while (it.hasNext())
            {
               String opinion = (String) it.next();
               double[] scores = new double[candidates.size()];
               for (int m=0; m<candidates.size(); m++)
               {
                 CollectionArticle a = (CollectionArticle)candidates.get(m);
                 scores[m] = ((Double)(a.opinions.get(opinion))).doubleValue();
               }

               int[] res = WestUtil.topK(scores, 1);
               if (res.length > 0)
               {
                   int pos = res[0];
                   CollectionArticle ca = (CollectionArticle)candidates.get(pos);
                   if (!finalists.contains(ca))
                   {
                     ca.reason = opinion;
                     ca.score = scores[pos];
                     finalists.add(ca);
  
                     //OPTIONAL !!!!!!!!
                     candidates.remove(ca);
                   }
               }
               
               size = finalists.size();
               rowIDs = new int[size];
               rowReasons = new String[size];
               rowScores = new double[size];
               
               for (int q=0; q<size;q++)
               {
            	   CollectionArticle ca = (CollectionArticle)finalists.get(q);
            	   rowIDs[q] = (int)ca.did;
            	   rowScores[q] = ca.score;
            	   rowReasons[q] = ca.reason;
               }
               
               output.topicOpinionDocIds[i] = rowIDs;
               output.topicOpinionDocScores[i] = rowScores;               
               output.topicOpinionDocReasons[i] = rowReasons;
            }
         }
   }

   public void doAllDocs(WegovOutput output)
   {
     Model model = parent.trnModel;
     
	 ArrayList allDocs = new ArrayList();
     for (int i=0; i<model.M; i++)
     {
    	 TopicOpinionDocument doc = new TopicOpinionDocument();
    	 doc.ID = i;
    	     	 
    	 double[] docscores = new double[model.K];
    	 for (int j=0; j<model.K; j++)
         	docscores[j] = model.theta[i][j];
    	 doc.topicScores = docscores;
    	 
      	CollectionArticle c = (CollectionArticle)parent.articles4output.get(new Integer(i));
      	if (c== null)
      		continue;

    	 HashMap docOpinions = new HashMap();
    	 Iterator it = OpinionMiner.termLists.keySet().iterator();
         while (it.hasNext())
         {
            String opinion = (String) it.next();
            Double value = (Double)(c.opinions.get(opinion));
            docOpinions.put(opinion, value);
         }   
         doc.opinions = docOpinions;
         
         doc.valence = c.liwc_sum;
         
         allDocs.add(doc);
     }
     output.allDocs = allDocs;
   }
}
