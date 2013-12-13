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
//      Created By :            Simon Crowle
//      Created Date :          10-Dec-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UILayoutUtil;

import com.vaadin.ui.*;




public class CreateExperimentView
{
  private Window    expView;
  private TextField expID;
  private TextField expName;
  private TextField expDesc;
  
  private transient CreateExperimentViewListener viewListener;
  
  
  public CreateExperimentView( String projectName,
                               CreateExperimentViewListener cevl )
  {
    viewListener = cevl;
    
    createComponents( projectName );
  }
  
  public Window getWindow()
  { return expView; }
  
  // Private methods -----------------------------------------------------------
  private void createComponents( String projectName )
  {
    expView = new Window();
    expView.setResizable( false );
    expView.setModal( true );
    expView.setClosable( false );
    
    VerticalLayout vl = new VerticalLayout();
    vl.setWidth( "400px" );
    expView.setContent( vl );
    
    // Space
    vl.addComponent( UILayoutUtil.createSpace( "20px", null ) );
    
    // Title spacing
    HorizontalLayout hl = new HorizontalLayout();
    vl.addComponent( hl );
    hl.addComponent( UILayoutUtil.createSpace( "20px", null, true ) );
    
    Label label = new Label( "Create new experiment" );
    label.addStyleName( "h2" );
    hl.addComponent( label );
    
    // Space
    vl.addComponent( UILayoutUtil.createSpace( "20px", null ) );
    
    // Left margin and then components
    hl = new HorizontalLayout();
    vl.addComponent( hl );
    VerticalLayout innerVL = new VerticalLayout();
    innerVL.setWidth( "40px" );
    hl.addComponent( innerVL );
    
    // Main components
    innerVL = new VerticalLayout();
    innerVL.setWidth( "320px" );
    hl.addComponent( innerVL );
    
    // Experiment ID
    expID = new TextField();
    innerVL.addComponent( createInputField( "Project:", 
                                            projectName, 
                                            expID, true, null ) );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "8px", null ) );
    
    // Experiment name
    expName = new TextField();
    innerVL.addComponent( createInputField( "Experiment name:",
                                            "<Your experiment instance>",
                                             expName, false, null) );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "8px", null ) );
    
    // Experiment description
    expDesc = new TextField();
    innerVL.addComponent( createInputField( "Experiment description:",
                                            "<A description of your experiment here>",
                                            expDesc, false, "200px") );
    
    // Space
    vl.addComponent( UILayoutUtil.createSpace( "20px", null ) );
    
    // Button container
    HorizontalLayout bHL = new HorizontalLayout();
    vl.addComponent( bHL );
    vl.setComponentAlignment( bHL, Alignment.BOTTOM_CENTER );
    
    // Start button
    Button button = new Button( "Start experiment" );
    button.addListener( new StartButtonListener() );
    bHL.addComponent( button );
    
    // Space
    bHL.addComponent( UILayoutUtil.createSpace( "8px", null, true ) );
    
    // Cancel button
    button = new Button( "Cancel" );
    button.addListener( new CancelButtonListener() );
    bHL.addComponent( button );
    
    // Space
    vl.addComponent( UILayoutUtil.createSpace( "20px", null ) );
  }
  
  private HorizontalLayout createInputField( String    name,
                                             String    defaultVal,
                                             TextField tf,
                                             boolean   readonly,
                                             String    height )
  {
    HorizontalLayout hl = new HorizontalLayout();
    
    Label label = new Label( name );
    label.addStyleName( "small" );
    label.setWidth( "140px" );
    hl.addComponent( label );
    
    // Space
    hl.addComponent( UILayoutUtil.createSpace( "8px", null, true ) );
    
    hl.addComponent( tf );
    tf.setValue( defaultVal );
    tf.setReadOnly( readonly );
    tf.addStyleName( "small" );
    tf.setWidth( "160px" );
    
    if ( height != null ) tf.setHeight( height );
    
    return hl;
  }
  
  private void onStartClicked()
  {
    viewListener.onStartExperiment( (String) expID.getValue(), 
                                    (String) expName.getValue(), 
                                    (String) expDesc.getValue() );
  }
  
  private void onCancelClicked()
  {
    viewListener.onCancelStartExperiment();
  }
  
  // Listeners
  class StartButtonListener implements Button.ClickListener
  {
    @Override
    public void buttonClick( Button.ClickEvent ce )
    { onStartClicked(); }
  }
  
  class CancelButtonListener implements Button.ClickListener
  {
    @Override
    public void buttonClick( Button.ClickEvent ce )
    { onCancelClicked(); }
  }
}
