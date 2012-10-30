package west.importer;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class AdvancedDialog extends JDialog implements ActionListener
{
  KeyAdapter keyAdapter =
  new KeyAdapter()
  {
    public void keyPressed( KeyEvent e )
    {
      if ( e.getKeyCode() == KeyEvent.VK_ENTER )
      { 
        ok();
      }
      else if ( e.getKeyCode() == KeyEvent.VK_ESCAPE )
      {
        cancel();
      }
    }
  };
  
  private JCheckBox useStemmer_Field;
  private JTextField minFeatures_Field;
  private JCheckBox removeStopwords_Field;
  private JTextField minTermLength_Field;
  private JTextField maxTermLength_Field;
  private JTextField minFeatureLength_Field;
  private JTextField maxFeatureLength_Field;
  private JCheckBox digitsAsChar_Field;
  private JCheckBox digitsAsTerm_Field;
  private JCheckBox termsToLower_Field;
  private JCheckBox makeIndex_Field;
  private JCheckBox storeTerms_Field;
  private JCheckBox storeFeatures_Field;
  
  
  private boolean useStemmer;
  private int minFeatures;
  private boolean removeStopwords;
  private int minTermLength;
  private int maxTermLength;
  private int minFeatureLength;
  private int maxFeatureLength;
  private boolean digitsAsChar;
  private boolean digitsAsTerm;
  private boolean termsToLower;
  private boolean makeIndex;
  private boolean storeTerms;
  private boolean storeFeatures;
  
  private boolean isOK;
  private JButton ok;
  private JButton cancel;
  
  private static String delim = System.getProperty( "file.separator" );
  private static String sLogoFile = System.getProperty( "user.dir" ) + delim + "importer" + delim + "importer.jpg";
  
  public AdvancedDialog()
  {
    super( new JFrame(), "Advanced Options", true );
    setSize( 290, 450 );
    Container content = this.getContentPane();
    content.setLayout( new BorderLayout() );
    
    Container cont = new Container();
    cont.setLayout( new GridLayout( 1, 1, 5, 5 ) );
    
    JPanel panel = createFieldPanel();
    cont.add( panel, BorderLayout.CENTER );
    
    panel = new JPanel();
    panel.setBorder( new EmptyBorder( 6, 0, 6, 12 ) );
    
    ok = new JButton( "OK" );
    ok.addActionListener( this );
    ok.addKeyListener( keyAdapter );
    panel.add( ok );
    cancel = new JButton( "Cancel" );
    cancel.addActionListener( this );
    panel.add( cancel );
    
    content.add( cont, BorderLayout.CENTER );
    content.add( panel, BorderLayout.SOUTH );
    
    show();
  }
  
  public boolean isOK()
  {
    return isOK;
  }
  

  public boolean get_useStemmer()
  {
      return useStemmer;
  }
  
  public int get_minFeatures()
  {
      return minFeatures;
  }
  
  public boolean get_removeStopwords()
  {
      return removeStopwords;
  }
  
  public int get_minTermLength()
  {
      return minTermLength;
  }
  
  public int get_maxTermLength()
  {
      return maxTermLength;
  }
  
  public int get_minFeatureLength()
  {
      return minFeatureLength;
  }
  
  public int get_maxFeatureLength()
  {
      return maxFeatureLength;
  }

  public boolean get_digitsAsChar()
  {
      return digitsAsChar;
  }
  
  public boolean get_digitsAsTerm()
  {
       return digitsAsTerm;
  }
  
  public boolean get_termsToLower()
  {
      return termsToLower;
  }
  
  public boolean get_makeIndex()
  {
      return makeIndex;
  }
  
  public boolean get_storeTerms()
  {
      return storeTerms;
  }

  public boolean get_storeFeatures()
  {
      return storeFeatures;
  }

  public void ok()
  {
    useStemmer = useStemmer_Field.isSelected();
    minFeatures =  Integer.valueOf( minFeatures_Field.getText().trim() ).intValue();
    removeStopwords = removeStopwords_Field.isSelected();
    minTermLength =  Integer.valueOf( minTermLength_Field.getText().trim() ).intValue();
    maxTermLength =  Integer.valueOf( maxTermLength_Field.getText().trim() ).intValue();
    minFeatureLength =  Integer.valueOf( minFeatureLength_Field.getText().trim() ).intValue();
    maxFeatureLength =  Integer.valueOf( maxFeatureLength_Field.getText().trim() ).intValue();
    digitsAsChar = digitsAsChar_Field.isSelected();
    digitsAsTerm = digitsAsTerm_Field.isSelected();
    termsToLower = termsToLower_Field.isSelected();
    makeIndex = makeIndex_Field.isSelected();
    storeTerms = storeTerms_Field.isSelected();
    storeFeatures = storeFeatures_Field.isSelected();
    
 
/*
    user = userField.getText().trim();
    adminuser = adminuserField.getText().trim();
    password = new String( passwordField.getPassword()).trim();
    adminpassword = new String( adminpasswordField.getPassword() ).trim();
    host = hostField.getText().trim();
    sid = sidField.getText().trim();
*/
 boolean allesKlar = true;
/*
    if (user == null || adminuser == null || password == null || adminpassword == null || host == null || sid == null)
      allesKlar = false;
    else if (user.length() * adminuser.length() * password.length() * adminpassword.length() * host.length() * sid.length() == 0)
      allesKlar = false;
*/  
    
    if (allesKlar)
    {
      isOK = true;
      this.hide();
      this.setEnabled (false);
    }
  } 
  
  public void cancel()
  {
    isOK = false;
    hide();
    System.out.println("CANCEL");
    System.exit(0);
  }
  
  public void actionPerformed( ActionEvent evt )
  {
    Object source = evt.getSource();
    if ( source == ok )
    {
      ok();
    } 
    else if ( source == cancel )
    {
      cancel();
    }
  }

  private JPanel createFieldPanel()
  {
    JPanel container = new JPanel( new BorderLayout() );
    GridLayout layout = new GridLayout( 13, 2, 10, 10 );
    JPanel panel = new JPanel( layout );
   
    JLabel label = new JLabel( "     use Stemmer", SwingConstants.LEFT );
    panel.add( label );
    
    useStemmer_Field = new JCheckBox( "", true );
//    useStemmer_Field.addItemListener( this );
    panel.add( useStemmer_Field );
    
    label = new JLabel( "     min. features", SwingConstants.LEFT );
    panel.add( label );
    
    minFeatures_Field = new JTextField( "2", 2 );
    minFeatures_Field.addKeyListener( keyAdapter );
    panel.add( minFeatures_Field );
    
    label = new JLabel( "     remove stopwords", SwingConstants.LEFT );
    panel.add( label );
    
    removeStopwords_Field = new JCheckBox( "", true );
//    userField.addKeyListener( keyAdapter );
    panel.add( removeStopwords_Field );
    
    label = new JLabel( "     min. term length", SwingConstants.LEFT );
    panel.add( label );
    
    minTermLength_Field = new JTextField( "3", 5 );
    minTermLength_Field.addKeyListener( keyAdapter );
    panel.add( minTermLength_Field );
    
    label = new JLabel( "     max. term length", SwingConstants.LEFT );
    panel.add( label );
    
    maxTermLength_Field = new JTextField( "35", 5 );
    maxTermLength_Field.addKeyListener( keyAdapter );
    panel.add( maxTermLength_Field );
    
    label = new JLabel( "     min. feature length", SwingConstants.LEFT );
    panel.add( label );
    
    minFeatureLength_Field = new JTextField( "3", 5 );
    minFeatureLength_Field.addKeyListener( keyAdapter );
    panel.add( minFeatureLength_Field );
    
    label = new JLabel( "     max. feature length", SwingConstants.LEFT );
    panel.add( label );
    
    maxFeatureLength_Field = new JTextField( "35", 5 );
    maxFeatureLength_Field.addKeyListener( keyAdapter );
    panel.add( maxFeatureLength_Field );
    
    label = new JLabel( "     digits as chars", SwingConstants.LEFT );
    panel.add( label );
    
    digitsAsChar_Field = new JCheckBox( "", false );
//    digitsAsChar_Field.addItemListener( this );
    panel.add( digitsAsChar_Field );
    
    label = new JLabel( "     digits as terms", SwingConstants.LEFT );
    panel.add( label );
    
    digitsAsTerm_Field = new JCheckBox( "", false );
//    digitsAsTerm_Field.addItemListener( this );
    panel.add( digitsAsTerm_Field );
    
    label = new JLabel( "     terms as lowecase", SwingConstants.LEFT );
    panel.add( label );
    
    termsToLower_Field = new JCheckBox( "", true );
//    termsToLower_Field.addItemListener( this );
    panel.add( termsToLower_Field );
    
    label = new JLabel( "     make index", SwingConstants.LEFT );
    panel.add( label );
    
    makeIndex_Field = new JCheckBox( "", true );
//    makeIndex_Field.addItemListener( this );
    panel.add( makeIndex_Field );
    
    label = new JLabel( "     store terms", SwingConstants.LEFT );
    panel.add( label );
    
    storeTerms_Field = new JCheckBox( "", false );
//    storeTerms_Field.addItemListener( this );
    panel.add( storeTerms_Field );
    
    label = new JLabel( "     store features", SwingConstants.LEFT );
    panel.add( label );
    
    storeFeatures_Field = new JCheckBox( "", true );
//    storeFeatures_Field.addItemListener( this );
    panel.add( storeFeatures_Field );
    
    container.add( panel, BorderLayout.CENTER );
    
    return container;
  }
}