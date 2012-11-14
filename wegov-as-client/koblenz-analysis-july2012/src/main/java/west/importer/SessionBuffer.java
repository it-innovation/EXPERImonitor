package west.importer;

import java.util.*;
//import java.sql.*;
//import oracle.jdbc.driver.*;

/**
 *  Die Klasse dient als Zwischenspeicher f�r diverse Objekte, auf die das
 *  System zur Laufzeit zugreift. Zudem werden systemweit g�ltige Konstanten
 *  definiert. Dar�ber hinaus ist das Objekt zust�ndig f�r den Aufbau und
 *  Abschluss der Oracle-Datenbankverbindung inklusive der Benutzer und
 *  Login-Verwaltung. Da zur Laufzeit nur genau eine Instanz dieses Objekts
 *  existieren darf, ist es als Singelton implementiert.
 */

public class SessionBuffer
{
  public static Hashtable stopWords;
  //public static oracle.jdbc.driver.OracleDriver oracleDriver = null;
  
  //Systemweite Einstellungen
  
  public static int maxProcessingThreads = 10;
  public static int maxStorageThreads = 5;
  public static int maxDownloaderThreads = 10;
  public static int maxOpinionThreads = 1;

  public static int batchSize = 10000;
  public static int chunkSize = 512*1024*1024;
  
  public static boolean dbConnect = false;
  
  public static String sHost;
  public static String sServiceName;
  public static String userDir = null;
  public static String dataDir = null;
  public static String defaultUser;
  public static String defaultUserPasswd;
  public static String adminUser;
  public static String adminUserPasswd;
  public static double relevanceThreshold;
  
  //public static OracleConnection oracleConnection = null;

  // Advanced Settings
  public static boolean useStemmer;
  public static int minFeatures;
  public static boolean removeStopwords;
  public static int minTermLength;
  public static int maxTermLength;
  public static int minFeatureLength;
  public static int maxFeatureLength;
  public static boolean digitsAsChar;
  public static boolean digitsAsTerm;
  public static boolean termsToLower;
  public static boolean makeIndex;
  public static boolean storeTerms;
  public static boolean storeFeatures;
  
  public static int numTermsPerTopic = 5;
  
  public static String language = "en";
  
  // Singelton
  private static SessionBuffer sessionBuffer = null;
    
  public static void setLanguage(String lang)
  {
	  language = lang;
  }
  
  public static void setNumTermsPerTopic(int x)
  {
	  numTermsPerTopic = x;
  }

  public static int getNumTermsPerTopic()
  {
	  return numTermsPerTopic;
  }
  
  public static String getLanguage()
  {
	  return language;
  }
  
  protected SessionBuffer()
  {
      useStemmer = true;
      minFeatures = 3;
      removeStopwords = true;
      minTermLength = 3;
      maxTermLength = 20;
      minFeatureLength = 3;
      maxFeatureLength = 20;
      digitsAsChar = false;
      digitsAsTerm = false;
      termsToLower = true;
      makeIndex = true;
      storeTerms = false;
      storeFeatures = true;
      relevanceThreshold = 0.5;

    String s = System.getProperty( "file.separator" );
    if ( userDir == null )
      this.userDir = System.getProperty( "user.dir" );
    
    this.dataDir = "/data3/wiki-en";
    //this.dataDir = userDir + s + "sources";
  }
  
  public boolean isStopword( String s )
  {
    return ( stopWords.containsKey(s) );
  }
  
  public Enumeration getStopwords()
  {
    return stopWords.elements();
  }
  
  // =====================================================================
/*
  public static synchronized OracleConnection getOracleConnection()
  {
    OracleConnection oc = null;
    try
    {
      if ( oracleDriver == null )
      {
        oracleDriver = new oracle.jdbc.driver.OracleDriver();
        DriverManager.registerDriver( oracleDriver );
      }
      
      // DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
      //Connection conn = DriverManager.getConnection("jdbc:oracle:oci8:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=134.96.246.200)(PORT=1521)))(CONNECT_DATA=(service_name=sydney.world)))","bingo", "00bingo00");
      /*
       try
      {
        oc = (OracleConnection)DriverManager.getConnection(
        "jdbc:oracle:oci:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=" + sHost + ")(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=" + sServiceName + ")(server = dedicated)))",
        defaultUser,
        defaultUserPasswd
        );
      }
      catch (Exception e)
      {}
      
      
      if (oc == null)  // thin versuchen
      {
        //System.out.println("Native OCI-Connection failed, use Java Thin instead");
        oc = (OracleConnection)DriverManager.getConnection(
        "jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=" + sHost + ")(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=" + sServiceName + ")(server = dedicated)))",
        defaultUser,
        defaultUserPasswd
        );
      }
      
      oc.setAutoCommit( false );
      //      SessionBuffer.log( "Neue DB-Verbindung: " + sServiceName + ":" + sHost + " aufgebaut." );
      dbConnect = true;
      
    } catch ( SQLException q )
    {
      q.printStackTrace();
      dbConnect = false;
    }
    return oc;
  }
  
  public static synchronized OracleConnection getAdminOracleConnection()
  {
    OracleConnection oc = null;
    try
    {
      if ( oracleDriver == null )
      {
        oracleDriver = new oracle.jdbc.driver.OracleDriver();
        DriverManager.registerDriver( oracleDriver );
      }
      
      Properties prop = new Properties();
      prop.put("user", adminUser);
      prop.put("password", adminUserPasswd);
      prop.put("internal_logon","sysdba");
      
      String url = "jdbc:oracle:thin:";
      String dbString = "(DESCRIPTION = (ADDRESS_LIST = (ADDRESS = "
      + "(PROTOCOL = TCP)(HOST = " + sHost + ")(PORT = 1521)))"
      + "(CONNECT_DATA = (SERVICE_NAME = " + sServiceName + " )(server = dedicated))))";
      
      oc = (OracleConnection)DriverManager.getConnection(url + "@" + dbString, prop);
      oc.setAutoCommit(true);
      
      if (oc != null)
        oc.setAutoCommit( false );
      
    } catch ( SQLException q )
    {
      q.printStackTrace();
      dbConnect = false;
    }
    return oc;
  }
  
  */
  /**
   *  Methode zum Aufruf der (einzigen) Instanz der Klasse. Es wird keine
   *  Datenbankverbindung aufgebaut.
   *
   *@return    Die Instanz.
   */
  public static synchronized SessionBuffer getInstance()
  {
    if ( sessionBuffer == null )
    {
      sessionBuffer = new SessionBuffer();
    }
    return sessionBuffer;
  }
  
  
  /**
   *  Methode zum Aufruf der (einzigen) Instanz der Klasse. Es wird eine
   *  Datenbankverbindung zu dem spezififizierten Oracle-Host aufgebaut. Dieser
   *  Verbindungsaufbau ist nur einmal zur Laufzeit erlaubt.
   *
   *@param  host         Der Host.
   *@param  serviceName  Der ServiceName.
   *@return              Die Instanz.
   */
  public static SessionBuffer getInstance( String user, String pass, String host, String serviceName )
  {
    defaultUser = user;
    defaultUserPasswd = pass;
    sHost = host;
    sServiceName = serviceName;
    
    if ( sessionBuffer == null )
    {
      sessionBuffer = new SessionBuffer();
    }
    //    if ( !dbConnect ) {
    //      oracleConnection = getOracleConnection();
    //      if ( oracleConnection == null ) {
    //        System.exit( 0 );
    //      }
    //      dbinterface = new myDBInterface();
    //    }
    return sessionBuffer;
  }
  
  
  /**
   *  Methode zum Aufruf der (einzigen) Instanz der Klasse. Es wird angegeben,
   *  ob eine Datenbankverbindung zu einem vorher spezififizierten Oracle-Host
   *  aufgebaut werden soll. Dieser Verbindungsaufbau ist nur einmal zur
   *  Laufzeit erlaubt.
   */
  public static SessionBuffer getInstance( boolean connect )
  {
    if ( !dbConnect && connect )
    {
      DBLoginDialog lDialog = new DBLoginDialog();
      if ( lDialog.isOK() )
      {
        defaultUser = lDialog.getUser();
        defaultUserPasswd = lDialog.getPassword();
        adminUser = lDialog.getAdminUser();
        adminUserPasswd = lDialog.getAdminPassword();
         
        sHost = lDialog.getHost();
        sServiceName = lDialog.getSID();
        
        lDialog = null;
      }
      else
      {
        System.exit(0);
      }
    }

    AdvancedDialog advDialog = new AdvancedDialog();
    if ( advDialog.isOK() )
    {
        useStemmer = advDialog.get_useStemmer();
        minFeatures = advDialog.get_minFeatures();
        removeStopwords = advDialog.get_removeStopwords();
        minTermLength = advDialog.get_minTermLength();
        maxTermLength = advDialog.get_maxTermLength();
        minFeatureLength = advDialog.get_minFeatureLength();
        maxFeatureLength = advDialog.get_maxFeatureLength();
        digitsAsChar = advDialog.get_digitsAsChar();
        digitsAsTerm = advDialog.get_digitsAsTerm();
        termsToLower = advDialog.get_termsToLower();
        makeIndex = advDialog.get_makeIndex();
        storeTerms = advDialog.get_storeTerms();
        storeFeatures = advDialog.get_storeFeatures();
    }
    else
    {
      System.exit(0);
    }

    if ( sessionBuffer == null )
    {
      sessionBuffer = new SessionBuffer();
    }
    return sessionBuffer;
  }
  
  public static int getBatchSize()
  {
    return batchSize;
  }
  
  public static int getChunkSize()
  {
    return chunkSize;
  }
}