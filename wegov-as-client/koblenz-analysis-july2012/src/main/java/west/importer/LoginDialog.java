package west.importer;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class LoginDialog extends JDialog implements ActionListener
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
  
  private JTextField hostField;
  private JTextField sidField;
  private JTextField userField;
  private JPasswordField passwordField;
  private JTextField adminuserField;
  private JPasswordField adminpasswordField;
  
  
  private String host;
  private String sid;
  private String user;
  private String adminuser ;
  private String password;
  private String adminpassword ;
  private boolean isOK;
  private JButton ok;
  private JButton cancel;
  
  private static String delim = System.getProperty( "file.separator" );
  private static String sLogoFile = System.getProperty( "user.dir" ) + delim + "importer" + delim + "importer.jpg";
  
  public LoginDialog()
  {
    super( new JFrame(), "WIKI Importer", true );
    setSize( 290, 250 );
    Container content = this.getContentPane();
    content.setLayout( new BorderLayout() );
    
    Container cont = new Container();
    cont.setLayout( new GridLayout( 1, 1, 5, 5 ) );
    
    JPanel panel = createFieldPanel();
    cont.add( panel, BorderLayout.CENTER );
    
    panel = new JPanel();
    panel.setBorder( new EmptyBorder( 6, 0, 6, 12 ) );
    
    ok = new JButton( "Import" );
    ok.addActionListener( this );
    ok.addKeyListener( keyAdapter );
    panel.add( ok );
    cancel = new JButton( "Cancel" );
    cancel.addActionListener( this );
    panel.add( cancel );
    
    content.add( cont, BorderLayout.CENTER );
    content.add( panel, BorderLayout.SOUTH );
    
    show();
    userField.selectAll();
  }
  
  public boolean isOK()
  {
    return isOK;
  }
  
  public String getHost()
  {
    return host;
  }
  
  public String getSID()
  {
    return sid;
  }
  
  public String getUser()
  {
    return user;
  }
  
  public String getAdminUser()
  {
    return adminuser;
  }
  
  public String getPassword()
  {
    return password;
  }
  
  public String getAdminPassword()
  {
    return adminpassword;
  }
  
  public void ok()
  {
    user = userField.getText().trim();
    adminuser = adminuserField.getText().trim();
    password = new String( passwordField.getPassword()).trim();
    adminpassword = new String( adminpasswordField.getPassword() ).trim();
    host = hostField.getText().trim();
    sid = sidField.getText().trim();
    
    boolean allesKlar = true;
    if (user == null || adminuser == null || password == null || adminpassword == null || host == null || sid == null)
      allesKlar = false;
    else if (user.length() * adminuser.length() * password.length() * adminpassword.length() * host.length() * sid.length() == 0)
      allesKlar = false;
    
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
    GridLayout layout = new GridLayout( 6, 2, 10, 10 );
    JPanel panel = new JPanel( layout );
   
    JLabel label = new JLabel( "     User", SwingConstants.LEFT );
    panel.add( label );
    
    userField = new JTextField( "bibsonomy", 30 );
    userField.addKeyListener( keyAdapter );
    panel.add( userField );
    
    label = new JLabel( "     Password", SwingConstants.LEFT );
    panel.add( label );
    
    passwordField = new JPasswordField( "00bibsonomy00", 30 );
    passwordField.addKeyListener( keyAdapter );
    panel.add( passwordField );
    
    label = new JLabel( "     Admin", SwingConstants.LEFT );
    panel.add( label );
    
    adminuserField = new JTextField( "sys", 30 );
    adminuserField.addKeyListener( keyAdapter );
    panel.add( adminuserField );
    
    label = new JLabel( "     Password", SwingConstants.LEFT );
    panel.add( label );
    
    adminpasswordField = new JPasswordField( "ahmad", 30 );
    adminpasswordField.addKeyListener( keyAdapter );
    panel.add( adminpasswordField );
    
    label = new JLabel( "     Host", SwingConstants.LEFT );
    panel.add( label );
    
    hostField = new JTextField( "localhost", 30 );
    hostField.addKeyListener( keyAdapter );
    panel.add( hostField );
    
    label = new JLabel( "     Service Name", SwingConstants.LEFT );
    panel.add( label );
     
    sidField = new JTextField( "orcl", 30 );
    sidField.addKeyListener( keyAdapter );
    panel.add( sidField );

    container.add( panel, BorderLayout.CENTER );
    
    return container;
  }
}