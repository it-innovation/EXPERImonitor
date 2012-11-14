/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package west.importer;

import west.importer.*;
import west.wegovdemo.*;
import jgibblda.*;
import java.util.*;

/**
 *
 * @author nasir
 */
public class WegovTopicAnalyzer 
{
    String[] arg = new String[] {"-est", "-ntopics", "10"};

    WegovImporter parent;

    public WegovTopicAnalyzer (WegovImporter _parent)
    {
      parent = _parent;
      
      int numtopics = (int)Math.max(2, Math.ceil(parent.articles4lda.size() / 30));
      arg = new String[] {"-est", "-ntopics", Integer.toString(numtopics)};
    }
    
    public void setNumTopics(int numTopics)
    {
    	if (numTopics > 1)
    		arg = new String[] {"-est", "-ntopics", Integer.toString(numTopics)};
    }

    public void run()
    {
        if (parent.articles4lda == null || parent.articles4lda.size() == 0)
            return;

        LDA lda = new LDA();

        parent.trnModel = lda.topicAnalyzer(arg, parent);

        /*
    	out.topicTerms = new String[trnModel.K][topWords];
    	out.topicDocIDs = new String[trnModel.K][topDocs];
    	out.topicDocScores = new double[trnModel.K][topDocs];
    	out.topicDocReasons = new String[trnModel.K][topDocs];
    	
        for (int i = 0; i < trnModel.K; ++i)
        {
            int[] topWordIdx = WestUtil.topK(trnModel.phi[i], topWords);
            for (int j = 0; j < topWords; ++j)
            {
            	out.topicTerms[i][j] = trnModel.data.localDict.id2word.get(topWordIdx[j]);
            }
             
            double[] doctopic = new double[trnModel.M];
            for (int j=0; j<trnModel.M; j++)
            	doctopic[j] = trnModel.theta[j][i];
            int[] topDocIdx = WestUtil.topK(doctopic, topDocs);
            for (int j=0;j<topDocIdx.length;j++)
            {
            	CollectionArticle article = (CollectionArticle)CollectionImporter.articles4output.get(new Integer(topDocIdx[j]));
            	out.topicDocIDs[i][j] = Long.toString(article.did);
            	out.topicDocScores[i][j] = trnModel.theta[topDocIdx[j]][i];
            	out.topicDocReasons[i][j] = "relevance";
            }            
        }
    	return out;
        */
    }
 }
