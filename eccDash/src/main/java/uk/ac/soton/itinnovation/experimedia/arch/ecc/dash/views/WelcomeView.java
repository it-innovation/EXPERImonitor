/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2012
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
//      Created Date :          01-Feb-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views;


import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.SimpleView;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UILayoutUtil;

import com.vaadin.ui.*;
import java.util.Collection;




public class WelcomeView extends SimpleView
{
  private TextArea miniConsole;
  private Button   startButton;

  public WelcomeView()
  {
    super( true );
    
    createComponents();
  }
  
  public void addLogInfo( String info )
  {
    if ( info != null )
    {
      String value = (String) miniConsole.getValue();
      value += info + "\n";
      miniConsole.setValue( value );
    }
  }
  
  public void setReadyToStart( boolean start )
  { startButton.setEnabled( start ); }
  
  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();
    
    Panel panel = new Panel();
    panel.setWidth( "400px" );
    panel.setHeight( "500px" );
    vl.addComponent( panel );
    vl.setComponentAlignment( panel, Alignment.MIDDLE_CENTER );
    
    VerticalLayout innerVL = new VerticalLayout();
    panel.addComponent( innerVL );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "40", null ) );
    
    // Title
    HorizontalLayout lhl = new HorizontalLayout();
    innerVL.addComponent( lhl );
    innerVL.setComponentAlignment( lhl, Alignment.MIDDLE_CENTER );
    
    Label label = new Label( "Experimedia ECC Dashboard" );
    label.addStyleName( "h1 color" );
    lhl.addComponent( label );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "20px", null ) );
   
    // Mini console view
    miniConsole = new TextArea();
    miniConsole.setWidth( "300px" );
    miniConsole.setHeight( "150px" );
    miniConsole.addStyleName( "small color" );
    miniConsole.setImmediate( true );
    innerVL.addComponent( miniConsole );
    innerVL.setComponentAlignment( miniConsole, Alignment.MIDDLE_CENTER );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "40px", null ) );
    
    // Start button
    startButton = new Button( "Open ECC" );
    startButton.addStyleName( "wide tall big" );
    startButton.setEnabled( false );
    startButton.setImmediate( true );
    startButton.addListener( new StartButtonListener() );
    innerVL.addComponent( startButton );
    innerVL.setComponentAlignment( startButton, Alignment.MIDDLE_CENTER );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "40px", null ) );
    
    // Copyright
    lhl = new HorizontalLayout();
    innerVL.addComponent( lhl );
    innerVL.setComponentAlignment( lhl, Alignment.BOTTOM_CENTER );
    Label cr = new Label( "© University of Southampton IT Innovation Centre 2013" );
    cr.addStyleName( "tiny" );
    lhl.addComponent( cr );
  }
 
  // Event handlers ------------------------------------------------------------
  private void onStartClicked()
  {
    Collection<WelcomeViewListener> listeners = getListenersByType();
    for( WelcomeViewListener listener : listeners )
      listener.onStartECCClicked();
  }
  
  private class StartButtonListener implements Button.ClickListener
  {
    @Override
    public void buttonClick(Button.ClickEvent ce) { onStartClicked(); }
  }
}
