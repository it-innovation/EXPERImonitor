/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
//
// Copyright in this software belongs to University of Southampton
// IT Innovation Centre of Gamma House, Enterprise Road, 
// Chilworth Science Park, Southampton, SO16 7NS, UK.
//
// This software may not be used, sold, licensed, transferred, copied
// or reproduced in whole or in part in any manner or form or in or
// on any media by any person other than in accordance with the terms
// of the Licence Agreement supplied with the software, or otherwise
// without the prior written consent of the copyright owners.
//
// This software is distributed WITHOUT ANY WARRANTY, without even the
// implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
// PURPOSE, except where stated in the Licence Agreement supplied with
// the software.
//
//      Created By :            Dion Kitchener
//      Created Date :          29-Aug-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.configuration;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.*;

import com.vaadin.ui.*;
import java.util.logging.Level;
import java.util.logging.Logger;



public class DashConfigView extends SimpleView
{
    
   // View listener
   private DashConfigViewListener viewlistener;
    
    
   // View root components  
   private Panel mainPanel;
   private VerticalLayout innerVL;
   private HorizontalLayout lhl;
   private HorizontalLayout chl;
    
   // Project name field
   private TextField projectNameText;

   
    
   // Fields for rabbit configuration data
   private TextField dashboardIdText;
   private TextField rabbitIpText;
   private TextField rabbitPortText;
   private TextField rabbitKeystoreText;
   private PasswordField rabbitPasswordText;
   private CheckBox rabbitSslCheck;
   
   // Fields for database configuration
   private TextField dbUrlText;
   private TextField dbNameText;
   private TextField dbUserameText;
   private PasswordField dbPasswordText;
   private TextField dbTypeText;

   private CheckBox useConfigCheck;
   
   // Fields for dashboard configuration
   private TextField snapshotCountText;
   private TextField nagiosUrlText;
   
   /**
    * Creates a dashboard configuration view
    * 
    * @param dcl - Dashboard configuration view listener 
    */
  public DashConfigView( DashConfigViewListener dcl )
  {  
    super( true );
    viewlistener = dcl;
    createComponents(); 
    
  }
  
  
  // Private methods -----------------------------------------------------------
  /**
   * Method to build and display the initial user interface components
   */
  private void createComponents()
  {
    // Creates a new layout
    VerticalLayout vl = getViewContents();
    mainPanel = new Panel();
    innerVL = new VerticalLayout();
    lhl = new HorizontalLayout();  
    
    // Sets a up the main panel
    mainPanel.setWidth( "800px" );
    mainPanel.setHeight( "1050px" );
    vl.addComponent( mainPanel );
    vl.setComponentAlignment( mainPanel, Alignment.MIDDLE_CENTER );
    
    // Adds the inner vertical layout to the main panel
     mainPanel.addComponent( innerVL );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "15", null ) );
    
    // Title
    innerVL.addComponent( lhl );
    innerVL.setComponentAlignment( lhl, Alignment.MIDDLE_CENTER );
    Label title = new Label( "Welcome to the Experimedia ECC Dashboard" );
    title.addStyleName( "h1 color" );
    lhl.addComponent(title);
    
     // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "15", null ) );
   
      // Prompt
    lhl = new HorizontalLayout();
    innerVL.addComponent( lhl );
    innerVL.setComponentAlignment( lhl , Alignment.MIDDLE_CENTER );
    Label prompt = new Label ( "Please enter your project name and click 'Find' to start configuration" );
    lhl.addComponent( prompt );
    
     // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "15", null ) );
    
    // Project configuration search bar 
    lhl = new HorizontalLayout();
    Label projectNameLabel = new Label ( "Project Name:" );
    lhl.addComponent( projectNameLabel );
    lhl.addComponent( UILayoutUtil.createSpace( "35" , null, true));
    projectNameText = new TextField();
    projectNameText.setWidth( "300" );
    lhl.addComponent( projectNameText );
    lhl.addComponent( UILayoutUtil.createSpace("25" , null, true));
    Button findConfigButton = new Button( "Find" );
    findConfigButton.addListener(new FindButtonListener());
    findConfigButton.setWidth( "90" );
    lhl.addComponent(findConfigButton);
    innerVL.addComponent(lhl);
    innerVL.setComponentAlignment(lhl, Alignment.MIDDLE_CENTER);
 

    
  }
  
  /**
   * Method that will display a prompt to select a configuration if any exist.
   * 
   * @param configsToShow - A boolean value to indicate if configuration data is available for display. 
   */
  public void foundConfigs( boolean configsToShow )
  {
      if( configsToShow )
      {
            // Space
            innerVL.addComponent(UILayoutUtil.createSpace( "15", null));
           
            
            // Sets up a horizontal layout
            lhl = new HorizontalLayout();
            innerVL.addComponent( lhl );
            innerVL.setComponentAlignment( lhl, Alignment.TOP_LEFT );

            // Select config prompt
            Label selectConfigLabel = new Label ( "Please select a configuration" );
            selectConfigLabel.addStyleName( "h2 color" );
            lhl.addComponent(UILayoutUtil.createSpace( "100", null, true));
            lhl.addComponent( selectConfigLabel );
      }
      
  }
  
  /**
   * Method shows the configuration footer if the user interface is showing configuration data panels.
   * 
   * @param showingConfigs - A boolean value to indicate if configuration data is being shown.
   */
  public void showConfigFooter( boolean showingConfigs)
  {
      if( showingConfigs )
      {
            // Space
            innerVL.addComponent(UILayoutUtil.createSpace( "10", null));
            
            // Set a horizonatal layout
            lhl = new HorizontalLayout();
            innerVL.addComponent( lhl );
            innerVL.setComponentAlignment( lhl, Alignment.TOP_LEFT );


            // Footer section directly below the config panels
            Label message = new Label ( "Values in the fields can be modified.  The online configuration only will be updated" );
            lhl.addComponent(UILayoutUtil.createSpace( "100", null, true));
            lhl.addComponent( message );
            lhl = new HorizontalLayout();
            innerVL.addComponent(UILayoutUtil.createSpace( "15" , null ));
            innerVL.addComponent( lhl );
            innerVL.setComponentAlignment( lhl, Alignment.TOP_LEFT);

            // Update configuration button
            Button updateConfigButton = new Button ( "Update Configuration" );
            updateConfigButton.addListener( new UpdateButtonListener() );
            lhl.addComponent(UILayoutUtil.createSpace( "100", null, true));
            lhl.addComponent( updateConfigButton ); 
      }
      
  }
  
  /**
   * Method to display configuration data in the user interface in forms that can be
   * modified.  Online and local configuration data will be displayed in seperate panels.
   * 
   * @param monitorID - Dashboard ID field.
   * @param rabbitIP    - Rabbit server IP address.
   * @param rabbitPort  - Rabbit server port number.
   * @param rabbitKeystore - Rabbit server username.
   * @param rabbitPassword - Rabbit server password
   * @param useRabbitSSL    - Option to use SSL on the Rabbit server.
   */
  public void showConfig ( String monitorID, 
                           String rabbitIP, 
                           String rabbitPort,
                           String rabbitKeystore,
                           String rabbitPassword,
                           boolean useRabbitSSL,
                           String dbUrl,
                           String dbName,
                           String dbUsername,
                           String dbPassword,
                           String dbType,
                           String snapshotCount,
                           String nagiosUrl)
  {
      
    // Sets up a config panel  
    Panel configPanel = new Panel();
    configPanel.setWidth( "600" );
    configPanel.setHeight( "650" );
    
    // set background colour
    configPanel.getStyleName( );
    
    
     // Sets up vertical layout, adds it to the main panel
    VerticalLayout innerConfigVL  = new VerticalLayout();
    configPanel.addComponent( innerConfigVL );
      
    // Sets label widths
    String labelWidth = "130";
    
    // Sets horizantol space width
    String hSpace =  "80";
    
    // Set textbox widths
    String textWidth = "270";
    
    
    // Monitor ID label and text box
    chl = new HorizontalLayout();
    innerConfigVL.addComponent( chl );
    innerConfigVL.setComponentAlignment( chl, Alignment.TOP_LEFT );
    Label dashboardIdLabel = new Label ( "Monitor ID:" );
    dashboardIdLabel.setWidth( labelWidth );
    chl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    chl.addComponent( dashboardIdLabel );
    dashboardIdText = new TextField();
    dashboardIdText.setValue( monitorID );
    dashboardIdText.setWidth( textWidth );
    chl.addComponent( dashboardIdText);   
   
    
    
    // Space
    innerConfigVL.addComponent(UILayoutUtil.createSpace( "15", null));
    
    // Rabbit IP label and text box
    chl = new HorizontalLayout();
    innerConfigVL.addComponent( chl );
    innerConfigVL.setComponentAlignment( chl, Alignment.TOP_LEFT );
    Label rabbitIpLabel = new Label ( "Rabbit IP:" );
    rabbitIpLabel.setWidth( labelWidth );
    chl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    chl.addComponent( rabbitIpLabel);
    rabbitIpText = new TextField();
    rabbitIpText.setValue( rabbitIP );
    rabbitIpText.setWidth( textWidth );
    chl.addComponent( rabbitIpText );

    
    // Space
    innerConfigVL.addComponent(UILayoutUtil.createSpace( "15", null));
    
    // Rabbit Port label and text box
    chl = new HorizontalLayout();
    innerConfigVL.addComponent( chl );
    innerConfigVL.setComponentAlignment( chl, Alignment.TOP_LEFT );
    Label rabbitPortLabel = new Label ( "Rabbit Port:" );
    rabbitPortLabel.setWidth( labelWidth );
    chl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    chl.addComponent( rabbitPortLabel );   
    rabbitPortText = new TextField();
    rabbitPortText.setValue( rabbitPort );
    rabbitPortText.setWidth( "75" );
    chl.addComponent( rabbitPortText );

  
    // Space
    innerConfigVL.addComponent(UILayoutUtil.createSpace( "15", null));
    
    // Rabbit username label and text box
    chl = new HorizontalLayout();
    innerConfigVL.addComponent( chl );
    innerConfigVL.setComponentAlignment( chl, Alignment.TOP_LEFT );
    Label rabbitUsernameLabel = new Label ( "Rabbit Keystore" );
    rabbitUsernameLabel.setWidth( labelWidth );
    chl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    chl.addComponent( rabbitUsernameLabel );
    rabbitKeystoreText = new TextField();
    rabbitKeystoreText.setValue( rabbitKeystore );
    rabbitKeystoreText.setWidth( textWidth );
    chl.addComponent( rabbitKeystoreText );
        
    
    innerConfigVL.addComponent(UILayoutUtil.createSpace( "15", null));
    
    // Rabbit password label and text box
    chl = new HorizontalLayout();
    innerConfigVL.addComponent( chl );
    innerConfigVL.setComponentAlignment( chl, Alignment.TOP_LEFT );
    Label rabbitPasswordLabel  = new Label ( "Rabbit Password" );
    rabbitPasswordLabel.setWidth( labelWidth );
    chl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    chl.addComponent( rabbitPasswordLabel );
    rabbitPasswordText = new PasswordField();
    rabbitPasswordText.setValue( rabbitPassword );
    rabbitPasswordText.setWidth( textWidth );
    chl.addComponent( rabbitPasswordText );
        
    
    
    // Space
    innerConfigVL.addComponent(UILayoutUtil.createSpace( "15", null));
    
    // Rabbit SSL checkbox
    chl = new HorizontalLayout();
    innerConfigVL.addComponent( chl );
    innerConfigVL.setComponentAlignment( chl, Alignment.TOP_LEFT );
    

    rabbitSslCheck = new CheckBox("Use Rabbit SSL?");
    rabbitSslCheck.setValue( useRabbitSSL  );
    chl.addComponent(UILayoutUtil.createSpace( "210" , null, true ));
    chl.addComponent( rabbitSslCheck );

    
    // Space
    innerConfigVL.addComponent(UILayoutUtil.createSpace( "25", null ));
    
    // Draws a line
    Label line = new Label( "<hr/>", Label.CONTENT_XHTML );
    innerConfigVL.addComponent( line );
    
    // Space
    innerConfigVL.addComponent(UILayoutUtil.createSpace( "15", null));
    
    
    // Database config fields
    chl = new HorizontalLayout();
    innerConfigVL.addComponent( chl );
    innerConfigVL.setComponentAlignment( chl, Alignment.TOP_LEFT );
    Label dbUrlLabel  = new Label ( "Database URL:" );
    dbUrlLabel.setWidth( labelWidth );
    chl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    chl.addComponent( dbUrlLabel );
    dbUrlText = new TextField();
    dbUrlText.setValue( dbUrl );
    dbUrlText.setWidth( textWidth );
    chl.addComponent( dbUrlText );
    
     // Space
    innerConfigVL.addComponent(UILayoutUtil.createSpace( "15", null));
    
    chl = new HorizontalLayout();
    innerConfigVL.addComponent( chl );
    innerConfigVL.setComponentAlignment( chl, Alignment.TOP_LEFT );
    Label dbNameLabel  = new Label ( "Database Name:" );
    dbNameLabel.setWidth( labelWidth );
    chl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    chl.addComponent( dbNameLabel );
    dbNameText = new TextField();
    dbNameText.setValue( dbName );
    dbNameText.setWidth( textWidth );
    chl.addComponent( dbNameText );
    
     // Space
    innerConfigVL.addComponent(UILayoutUtil.createSpace( "15", null));
    
    chl = new HorizontalLayout();
    innerConfigVL.addComponent( chl );
    innerConfigVL.setComponentAlignment( chl, Alignment.TOP_LEFT );
    Label dbUsernameLabel  = new Label ( "Database Username:" );
    dbUsernameLabel.setWidth( labelWidth );
    chl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    chl.addComponent( dbUsernameLabel );
    dbUserameText = new TextField();
    dbUserameText.setValue( dbUsername );
    dbUserameText.setWidth( textWidth );
    chl.addComponent( dbUserameText );
    
      // Space
    innerConfigVL.addComponent(UILayoutUtil.createSpace( "15", null));
    
    chl = new HorizontalLayout();
    innerConfigVL.addComponent( chl );
    innerConfigVL.setComponentAlignment( chl, Alignment.TOP_LEFT );
    Label dbPasswordLabel  = new Label ( "Database Password:" );
    dbPasswordLabel.setWidth( labelWidth );
    chl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    chl.addComponent( dbPasswordLabel );
    dbPasswordText = new PasswordField();
    dbPasswordText.setValue( dbPassword );
    dbPasswordText.setWidth( textWidth );
    chl.addComponent( dbPasswordText );
    
       // Space
    innerConfigVL.addComponent(UILayoutUtil.createSpace( "15", null));
   
    
    chl = new HorizontalLayout();
    innerConfigVL.addComponent( chl );
    innerConfigVL.setComponentAlignment( chl, Alignment.TOP_LEFT );
    Label dbTypeLabel = new Label ( "Database Type:" );
    dbTypeLabel.setWidth( labelWidth );
    chl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    chl.addComponent( dbTypeLabel );
    dbTypeText = new TextField();
    dbTypeText.setValue( dbType );
    dbTypeText.setWidth( textWidth );
    chl.addComponent( dbTypeText );
    
       // Space
    innerConfigVL.addComponent(UILayoutUtil.createSpace( "15", null));
    
    // Draws a line
    Label line2 = new Label( "<hr/>", Label.CONTENT_XHTML );
    innerConfigVL.addComponent( line2 );
    
      // Space
    innerConfigVL.addComponent(UILayoutUtil.createSpace( "15", null));
    
    chl = new HorizontalLayout();
    innerConfigVL.addComponent( chl );
    innerConfigVL.setComponentAlignment( chl, Alignment.TOP_LEFT );
    Label snapshotCountLabel   = new Label ( "Snapshot Count:" );
    snapshotCountLabel.setWidth( labelWidth );
    chl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    chl.addComponent( snapshotCountLabel );
    snapshotCountText = new TextField();
    snapshotCountText.setValue( snapshotCount );
    snapshotCountText.setWidth( textWidth );
    chl.addComponent( snapshotCountText );
    
    
    
      // Space
    innerConfigVL.addComponent(UILayoutUtil.createSpace( "15", null));
    
    chl = new HorizontalLayout();
    innerConfigVL.addComponent( chl );
    innerConfigVL.setComponentAlignment( chl, Alignment.TOP_LEFT );
    Label nagiosUrlLabel  = new Label ( "Nagios URL:" );
    nagiosUrlLabel.setWidth( labelWidth );
    chl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    chl.addComponent( nagiosUrlLabel );
    nagiosUrlText = new TextField();
    nagiosUrlText.setValue( nagiosUrl );
    nagiosUrlText.setWidth( textWidth );
    chl.addComponent( nagiosUrlText );
    
    
         // Space
    innerConfigVL.addComponent(UILayoutUtil.createSpace( "15", null));
    
    // Draws a line
    Label line3 = new Label( "<hr/>", Label.CONTENT_XHTML );
    innerConfigVL.addComponent( line3 );

    
    // Use this configuration checkbox
    chl = new HorizontalLayout();
    innerConfigVL.addComponent( chl );
    innerConfigVL.setComponentAlignment( chl, Alignment.BOTTOM_LEFT );
    useConfigCheck = new CheckBox( "Use this configuration" );
    useConfigCheck.setImmediate( true );
    chl.addComponent( useConfigCheck );  
    
    // Adds the config panel to the main layout
    innerVL.addComponent(configPanel);
    
    // Space
    innerVL.addComponent(UILayoutUtil.createSpace( "20", null));
    
    // Sets the config panel alignment
    innerVL.setComponentAlignment( configPanel, Alignment.TOP_CENTER );
    
  }
   
  // Event handlers-------------------------------------------------------------
  /**
   * Method to handle a button click event which sends the project name to the
   * controller in order to find configuration data fro a specified component feature.
   * 
   * @throws Exception 
   */
  public void onFindClicked() throws Exception
  {
      // check the project text field is not null
      String projectName = projectNameText.getValue().toString();
  
      if( !projectName.isEmpty() )
      {
          // send the project name to the controller to find configurations
          viewlistener.onFindConfigurations( projectName );
            
      }
      else
      {
          // message:  "The project name is not valid"
         this.displayWarning( "Configuration error" , "The project name is not valid" );
      }

  }
  
 /**
  *A method to handle a button click event which collects input data and sends it 
  * to the controller in order to update the online repository.
  * 
  * @throws Exception - Throws if the data cannot be updated.
  */ 
  public void onUpdateConfigurationClicked() throws Exception 
  {       
      // collect data from online config text boxes
      // send data to the controller

      // Rabbit configuration
      String monitorID = dashboardIdText.getValue().toString();
      String rabbitIP = rabbitIpText.getValue().toString();
      String rabbitPort = rabbitPortText.getValue().toString();
      String rabbitKeystore = rabbitKeystoreText.getValue().toString();
      String rabbitPassword = rabbitPasswordText.getValue().toString();
      boolean useRabbitSSL = rabbitSslCheck.booleanValue();

      // Database configuration
      String dbUrl = dbUrlText.getValue().toString();
      String dbName = dbNameText.getValue().toString();
      String dbUsername = dbUserameText.getValue().toString();
      String dbPassword = dbPasswordText.getValue().toString();
      String dbType = dbTypeText.getValue().toString();

      // Dashboard configuration
      String snapshotCount = snapshotCountText.getValue().toString();
      String nagiosUrl = nagiosUrlText.getValue().toString();


    try
    {
          viewlistener.onUpdateConfiguration( monitorID, 
                                            rabbitIP, 
                                            rabbitPort, 
                                            rabbitKeystore, 
                                            rabbitPassword, 
                                            useRabbitSSL,
                                            dbUrl,
                                            dbName,
                                            dbUsername,
                                            dbPassword,
                                            dbType,
                                            snapshotCount,
                                            nagiosUrl                                   
                                            );
          
    }
   catch ( Exception ex )
   {
        String error = "Could not update the configuration because : " + ex.getMessage();
        throw new Exception( error, ex );
    }
}  

  /**
   * A button click listener class
   */
  private class FindButtonListener implements Button.ClickListener
  {
       @Override
    public void buttonClick(Button.ClickEvent ce) 
       {   
           try 
           {
               onFindClicked();
           } 
           catch (Exception ex) 
           {
               Logger.getLogger(DashConfigView.class.getName()).log(Level.SEVERE, null, ex);
           }
        }
 }
  
  /**
   * A button click listener class
   */
  private class UpdateButtonListener implements Button.ClickListener
  {
       @Override
        public void buttonClick( Button.ClickEvent ce ) 
       {
           try 
           {
               onUpdateConfigurationClicked();
           } 
           catch (Exception ex) 
           {
               Logger.getLogger(DashConfigView.class.getName()).log(Level.SEVERE, null, ex);
           }
        }
  }
  
  
}
