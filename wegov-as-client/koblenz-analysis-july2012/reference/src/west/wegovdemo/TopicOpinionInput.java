package west.wegovdemo;

/**
 * This interface represents inputs passed by WeGov components to the topic-opinion analysis.
 * It contains text documents (e.g. 
 * discussion postings) that shall be automatically analyzed wrt latent semantics and expressed opinions.
 * @author sizov
 *
 */
public interface TopicOpinionInput 
{
   /**
    * This array represents document contents (bodies). Particular document bodies are arbitrary 
    * pieces of text. The currently supported language for text processing is English.
    * Document IDs returned by topic-opinion analysis are document positions in this array.
    */
   public String[] getDocumentContents();

   /**
    * This array represents user names. Particular user names are associated with postings 
    * and appear in same order. User names are currently considered as strings, i.e.  
    * pieces of text.   
    * Document IDs returned by topic-opinion analysis are username positions in this array. 
    */
   public String[] getDocumentUsers();
}
