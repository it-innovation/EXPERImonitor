package west.importer;

import java.util.*;

public class WegovOpinionSummarizer
{
	   WegovImporter parent;
	
	   public WegovOpinionSummarizer (WegovImporter _parent)
	    {
	      parent = _parent;
	    }
	   
	   public void run()
	   {
		   double maxPos = 0.000001;
		   double maxNeg = 0.000001;
		   
		   double normPos = 0.000001;
		   double normNeg = -0.000001;
		   
		   Iterator it = parent.articles4lda.iterator();
		   while (it.hasNext())
		   {
			   CollectionArticle article = (CollectionArticle) it.next();
			   if (article.liwc_pos > maxPos)
				   maxPos = article.liwc_pos;
			   if (article.liwc_neg > maxNeg)
				   maxNeg = article.liwc_neg;
		   }
		   
		   
		   it = parent.articles4lda.iterator();
		   while (it.hasNext())
		   {
			   CollectionArticle article = (CollectionArticle) it.next();
			   article.liwc_pos = article.liwc_pos / maxPos;
			   article.liwc_neg = article.liwc_neg / maxNeg;
			   
			   article.liwc_sum = (article.liwc_pos - article.liwc_neg);
			   if (article.liwc_sum > normPos)
				   normPos = article.liwc_sum;
			   if (article.liwc_sum < normNeg)
				   normNeg = article.liwc_sum;
		   }
		   
		   it = parent.articles4lda.iterator();
		   while (it.hasNext())
		   {
			   CollectionArticle article = (CollectionArticle) it.next();
			   
			   if (article.liwc_sum > 0)
				   article.liwc_sum = article.liwc_sum / normPos * 10;
			   else
				   article.liwc_sum = article.liwc_sum / normNeg * - 10;
			   
               // System.out.println(article.liwc_sum);
		   }
	   }
}
