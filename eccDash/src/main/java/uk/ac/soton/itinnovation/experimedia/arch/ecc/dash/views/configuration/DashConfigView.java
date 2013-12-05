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
import com.vaadin.event.ShortcutAction.KeyCode;




public class DashConfigView extends SimpleView
{
   // View listener
   private transient DashConfigViewListener viewlistener;
       
   // View root components  
   private Panel mainPanel;
   private VerticalLayout innerVL;
    
   // Project name field
   private TextField projectNameText;
   private Button    projectSearchButton;

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
    mainPanel.setWidth( "800px" );
    vl.addComponent( mainPanel );
    vl.setComponentAlignment( mainPanel, Alignment.MIDDLE_CENTER );
    
    // Add inner layout items
    innerVL = (VerticalLayout) mainPanel.getContent();
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "20px", null ) );
    
    // Title
    HorizontalLayout hl = new HorizontalLayout();
    innerVL.addComponent( hl );
    innerVL.setComponentAlignment( hl, Alignment.MIDDLE_CENTER );
    Label title = new Label( "Welcome to the Experimedia ECC Dashboard" );
    title.addStyleName( "h1" );
    hl.addComponent( title );
    
     // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "12px", null ) );
   
    // Prompt
    hl = new HorizontalLayout();
    innerVL.addComponent( hl );
    innerVL.setComponentAlignment( hl, Alignment.MIDDLE_CENTER );
    Label prompt = new Label ( "Please enter your project name and click 'Find' to start configuration" );
    prompt.addStyleName( "small" );
    hl.addComponent( prompt );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "8px", null ) );
    
    // Project configuration search bar 
    hl = new HorizontalLayout();
    innerVL.addComponent( hl );
    innerVL.setComponentAlignment( hl, Alignment.MIDDLE_CENTER );
    
    Label projectNameLabel = new Label ( "Project Name:" );
    hl.addComponent( projectNameLabel );
    hl.addComponent( UILayoutUtil.createSpace("8px" , null, true) );
    
    projectNameText = new TextField();
    projectNameText.setWidth( "300px" );
    hl.addComponent( projectNameText );
    hl.addComponent( UILayoutUtil.createSpace("8px" , null, true));
    
    projectSearchButton = new Button( "Find" );
    projectSearchButton.addStyleName( "small" );
    projectSearchButton.addListener( new FindButtonListener() );
    projectSearchButton.setWidth( "90px" );
    projectSearchButton.setClickShortcut( KeyCode.ENTER );
    hl.addComponent( projectSearchButton );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "16px", null ) );
  }
    
  /**
   * Method shows the configuration footer if the user interface is showing configuration data panels.
   * 
   * @param showingConfigs - A boolean value to indicate if configuration data is being shown.
   */
  public void showConfigFooter( boolean showingConfigs)
  {
    if ( showingConfigs )
    {
      // Space
      innerVL.addComponent( UILayoutUtil.createSpace( "8", null) );

      // Footer section directly below the config panels
      Label message = new Label ( "Values in the fields can be modified.  The online configuration only will be updated" );
      innerVL.addComponent( message );

      innerVL.addComponent(UILayoutUtil.createSpace( "8" , null ));

      // Update configuration button
      Button updateConfigButton = new Button ( "Update Configuration" );
      updateConfigButton.addListener( new UpdateButtonListener() );
      innerVL.addComponent( updateConfigButton );
      innerVL.setComponentAlignment( updateConfigButton, Alignment.BOTTOM_CENTER );
      
      innerVL.addComponent(UILayoutUtil.createSpace( "8" , null ));
    }
  }
  
  /**
   * Method to display configuration data in the user interface in forms that can be
   * modified.  Online and local configuration data will be displayed in separate panels.
   * 
   * @param monitorID - Dashboard ID field.
   * @param rabbitIP    - Rabbit server IP address.
   * @param rabbitPort  - Rabbit server port number.
   * @param rabbitKeystore - Rabbit server username.
   * @param rabbitPassword - Rabbit server password
   * @param useRabbitSSL    - Option to use SSL on the Rabbit server.
   */
  public void showConfig( String monitorID, 
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
                          String nagiosUrl )
  {
    // Remove short cut once config is shown
    projectSearchButton.removeClickShortcut();
    
    // Layout constants
    final String fieldVHeight = "4px";
    final String labelWidth   = "150px";
    final String hSpace       = "80px";
    final String textWidth    = "270px";
    
    // Sets up a config panel  
    Panel configPanel = new Panel();
    configPanel.addStyleName( "light" );
    configPanel.setCaption( "Current " + projectNameText + " configuration" );
    innerVL.addComponent( configPanel );
    innerVL.setComponentAlignment( configPanel, Alignment.TOP_CENTER );
    
    VerticalLayout pVL = (VerticalLayout) configPanel.getContent();
    VerticalLayout configContents = new VerticalLayout();
    configContents.setWidth( "600px" );
    pVL.addComponent( configContents );
    pVL.setComponentAlignment(configContents, Alignment.TOP_CENTER );
    
    // Space
    configContents.addComponent( UILayoutUtil.createSpace( "4", null) );
 
    // Monitor ID label and text box
    HorizontalLayout hl = new HorizontalLayout();
    configContents.addComponent( hl );
    
    Label dashboardIdLabel = new Label ( "Monitor ID:" );
    dashboardIdLabel.setWidth( labelWidth );
    hl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    hl.addComponent( dashboardIdLabel );
    dashboardIdText = new TextField();
    dashboardIdText.setValue( monitorID );
    dashboardIdText.setWidth( textWidth );
    hl.addComponent( dashboardIdText );   
   
    // Space
    configContents.addComponent( UILayoutUtil.createSpace( "4", null) );
    
    // Rabbit IP label and text box
    hl = new HorizontalLayout();
    configContents.addComponent( hl );
    Label rabbitIpLabel = new Label ( "Rabbit IP:" );
    rabbitIpLabel.setWidth( labelWidth );
    hl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    hl.addComponent( rabbitIpLabel);
    rabbitIpText = new TextField();
    rabbitIpText.setValue( rabbitIP );
    rabbitIpText.setWidth( textWidth );
    hl.addComponent( rabbitIpText );

    // Space
    configContents.addComponent( UILayoutUtil.createSpace( fieldVHeight, null) );
    
    // Rabbit Port label and text box
    hl = new HorizontalLayout();
    configContents.addComponent( hl );
    Label rabbitPortLabel = new Label ( "Rabbit Port:" );
    rabbitPortLabel.setWidth( labelWidth );
    hl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    hl.addComponent( rabbitPortLabel );   
    rabbitPortText = new TextField();
    rabbitPortText.setValue( rabbitPort );
    rabbitPortText.setWidth( "75" );
    hl.addComponent( rabbitPortText );

    // Space
    configContents.addComponent( UILayoutUtil.createSpace( fieldVHeight, null) );
    
    // Rabbit username label and text box
    hl = new HorizontalLayout();
    configContents.addComponent( hl );
    Label rabbitUsernameLabel = new Label ( "Rabbit Keystore:" );
    rabbitUsernameLabel.setWidth( labelWidth );
    hl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    hl.addComponent( rabbitUsernameLabel );
    rabbitKeystoreText = new TextField();
    rabbitKeystoreText.setValue( rabbitKeystore );
    rabbitKeystoreText.setWidth( textWidth );
    hl.addComponent( rabbitKeystoreText );
        
    // Space
    configContents.addComponent( UILayoutUtil.createSpace( fieldVHeight, null) );
    
    // Rabbit password label and text box
    hl = new HorizontalLayout();
    configContents.addComponent( hl );
    Label rabbitPasswordLabel  = new Label ( "Rabbit Password:" );
    rabbitPasswordLabel.setWidth( labelWidth );
    hl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    hl.addComponent( rabbitPasswordLabel );
    rabbitPasswordText = new PasswordField();
    rabbitPasswordText.setValue( rabbitPassword );
    rabbitPasswordText.setWidth( textWidth );
    hl.addComponent( rabbitPasswordText );
    
    // Space
    configContents.addComponent( UILayoutUtil.createSpace( fieldVHeight, null) );
    
    // Rabbit SSL checkbox
    hl = new HorizontalLayout();
    configContents.addComponent( hl );
    rabbitSslCheck = new CheckBox("Use Rabbit SSL?");
    rabbitSslCheck.setValue( useRabbitSSL  );
    hl.addComponent( rabbitSslCheck );

    // Space
    configContents.addComponent( UILayoutUtil.createSpace( fieldVHeight, null) );
    
    // Draws a line
    Label line = new Label( "<hr/>", Label.CONTENT_XHTML );
    configContents.addComponent( line );
    
    // Space
    configContents.addComponent( UILayoutUtil.createSpace( fieldVHeight, null) );
    
    // Database config fields
    hl = new HorizontalLayout();
    configContents.addComponent( hl );
    Label dbUrlLabel  = new Label ( "Database URL:" );
    dbUrlLabel.setWidth( labelWidth );
    hl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    hl.addComponent( dbUrlLabel );
    dbUrlText = new TextField();
    dbUrlText.setValue( dbUrl );
    dbUrlText.setWidth( textWidth );
    hl.addComponent( dbUrlText );
    
    // Space
    configContents.addComponent( UILayoutUtil.createSpace( fieldVHeight, null) );
    
    hl = new HorizontalLayout();
    configContents.addComponent( hl );
    Label dbNameLabel  = new Label ( "Database Name:" );
    dbNameLabel.setWidth( labelWidth );
    hl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    hl.addComponent( dbNameLabel );
    dbNameText = new TextField();
    dbNameText.setValue( dbName );
    dbNameText.setWidth( textWidth );
    hl.addComponent( dbNameText );
    
    // Space
    configContents.addComponent( UILayoutUtil.createSpace( fieldVHeight, null) );
    
    hl = new HorizontalLayout();
    configContents.addComponent( hl );
    Label dbUsernameLabel  = new Label ( "Database Username:" );
    dbUsernameLabel.setWidth( labelWidth );
    hl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    hl.addComponent( dbUsernameLabel );
    dbUserameText = new TextField();
    dbUserameText.setValue( dbUsername );
    dbUserameText.setWidth( textWidth );
    hl.addComponent( dbUserameText );
    
    // Space
    configContents.addComponent( UILayoutUtil.createSpace( fieldVHeight, null) );
    
    hl = new HorizontalLayout();
    configContents.addComponent( hl );
    Label dbPasswordLabel  = new Label ( "Database Password:" );
    dbPasswordLabel.setWidth( labelWidth );
    hl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    hl.addComponent( dbPasswordLabel );
    dbPasswordText = new PasswordField();
    dbPasswordText.setValue( dbPassword );
    dbPasswordText.setWidth( textWidth );
    hl.addComponent( dbPasswordText );
    
    // Space
    configContents.addComponent( UILayoutUtil.createSpace( fieldVHeight, null) );
  
    hl = new HorizontalLayout();
    configContents.addComponent( hl );
    Label dbTypeLabel = new Label ( "Database Type:" );
    dbTypeLabel.setWidth( labelWidth );
    hl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    hl.addComponent( dbTypeLabel );
    dbTypeText = new TextField();
    dbTypeText.setValue( dbType );
    dbTypeText.setWidth( textWidth );
    hl.addComponent( dbTypeText );
    
    // Space
    configContents.addComponent( UILayoutUtil.createSpace( fieldVHeight, null) );
    
    // Draws a line
    Label line2 = new Label( "<hr/>", Label.CONTENT_XHTML );
    configContents.addComponent( line2 );
    
    // Space
    configContents.addComponent( UILayoutUtil.createSpace( fieldVHeight, null) );
    
    hl = new HorizontalLayout();
    configContents.addComponent( hl );
    Label snapshotCountLabel   = new Label ( "Snapshot Count:" );
    snapshotCountLabel.setWidth( labelWidth );
    hl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    hl.addComponent( snapshotCountLabel );
    snapshotCountText = new TextField();
    snapshotCountText.setValue( snapshotCount );
    snapshotCountText.setWidth( textWidth );
    hl.addComponent( snapshotCountText );
    
    // Space
    configContents.addComponent( UILayoutUtil.createSpace( fieldVHeight, null) );
    
    hl = new HorizontalLayout();
    configContents.addComponent( hl );
    Label nagiosUrlLabel  = new Label ( "Nagios URL:" );
    nagiosUrlLabel.setWidth( labelWidth );
    hl.addComponent(UILayoutUtil.createSpace( hSpace, null, true));
    hl.addComponent( nagiosUrlLabel );
    nagiosUrlText = new TextField();
    nagiosUrlText.setValue( nagiosUrl );
    nagiosUrlText.setWidth( textWidth );
    hl.addComponent( nagiosUrlText );
    
    // Space
    configContents.addComponent( UILayoutUtil.createSpace( fieldVHeight, null) );
    
    // Draws a line
    Label line3 = new Label( "<hr/>", Label.CONTENT_XHTML );
    configContents.addComponent( line3 );
    
    // Use this configuration checkbox
    useConfigCheck = new CheckBox( "Use this configuration" );
    useConfigCheck.setImmediate( true );
    configContents.addComponent( useConfigCheck );  
    
    // Space
    configContents.addComponent( UILayoutUtil.createSpace( "8", null) );
  }
   
  // Event handlers-------------------------------------------------------------
  /**
   * Method to handle a button click event which sends the project name to the
   * controller in order to find configuration data fro a specified component feature.
   */
  public void onFindClicked()
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
  * A method to handle a button click event which collects input data and sends it 
  * to the controller in order to update the online repository.
  */ 
  public void onUpdateConfigurationClicked()
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

      viewlistener.onUpdateConfiguration( monitorID, rabbitIP, rabbitPort, 
                                          rabbitKeystore, rabbitPassword, useRabbitSSL,
                                          dbUrl, dbName, dbUsername, dbPassword, dbType,
                                          snapshotCount, nagiosUrl );
  }  
  
  /**
   * A button click listener class
   */
  private class FindButtonListener implements Button.ClickListener
  {
     @Override
    public void buttonClick(Button.ClickEvent ce)
    { onFindClicked(); }
 }
  
  /**
   * A button click listener class
   */
  private class UpdateButtonListener implements Button.ClickListener
  {
     @Override
     public void buttonClick( Button.ClickEvent ce ) 
     {
       onUpdateConfigurationClicked();
     }
  }
}
