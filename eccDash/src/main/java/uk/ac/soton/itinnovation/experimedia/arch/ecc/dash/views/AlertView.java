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
//      Created Date :          28-Mar-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views;

import com.vaadin.ui.*;
import java.util.Collection;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UILayoutUtil;



public class AlertView
{
  private Window            alertWindow;
  private String[]          viewOptions;
  private AlertViewListener alertListener;
  
  
  public AlertView( String title, 
                    String message,
                    String[] options,
                    AlertViewListener listener )
  {
    super();
    
    alertListener = listener;
    
    createComponents( title, message, options );
  }
  

  public Window getWindow()
  { return alertWindow; }
  
  // Private methods -----------------------------------------------------------
  private void createComponents( String title, String message, String[] options )
  {
    alertWindow = new Window();
    alertWindow.setSizeUndefined();
    alertWindow.addStyleName( "opaque" );
    alertWindow.setResizable( false );
    alertWindow.setClosable( false );
    alertWindow.setModal( true );
    
    VerticalLayout vl = new VerticalLayout();
    vl.setWidth( "400px" );
    alertWindow.setContent( vl );
    
    // Space
    vl.addComponent( UILayoutUtil.createSpace( "20px", null ) );
    
    // Title
    if ( title != null )
    {
      HorizontalLayout hl = new HorizontalLayout();
      vl.addComponent( hl );
      
      // Space
      hl.addComponent( UILayoutUtil.createSpace( "10px", null, true ) );
      
      Label label = new Label( title );
      label.addStyleName( "h2 color" );
      hl.addComponent( label );
      hl.setComponentAlignment( label, Alignment.MIDDLE_CENTER );
      
      // Space
      vl.addComponent( UILayoutUtil.createSpace( "10px", null ) );
    }
    
    // Message
    if ( message != null )
    {
      HorizontalLayout hl = new HorizontalLayout();
      hl.setWidth( "360px" );
      vl.addComponent( hl );
      
      // Space
      hl.addComponent( UILayoutUtil.createSpace( "10px", null, true ) );
      
      Label label = new Label( message );
      label.addStyleName( "small" );
      hl.addComponent( label );
      hl.setComponentAlignment( label, Alignment.MIDDLE_CENTER );
      
      // Space
      vl.addComponent( UILayoutUtil.createSpace( "30px", null ) );
    }
    
    // Options
    if ( options != null && options.length > 0 )
    {
      HorizontalLayout hl = new HorizontalLayout();
      vl.addComponent( hl );
      vl.setComponentAlignment( hl, Alignment.MIDDLE_CENTER );
      
      for ( String opt : options )
      {
        Button button = createOptionButton( opt );
        hl.addComponent( button );
        
        // Space
        hl.addComponent( UILayoutUtil.createSpace( "4px", null, true ) );
      }
    }
    else
    {
      Button button = createOptionButton( "OK" );
      vl.addComponent( button );
      vl.setComponentAlignment( button, Alignment.MIDDLE_CENTER );
    }
    
    // Space
    vl.addComponent( UILayoutUtil.createSpace( "40px", null ) );
  }
  
  // Private methods -----------------------------------------------------------
  private Button createOptionButton( String option )
  {
    Button button = new Button ( option );
    button.addStyleName( "small" );
    button.addListener( new ButtonListener() );
    
    return button;
  }
  
  private void onOptionClicked( String option )
  {
    if ( alertListener != null )
      alertListener.onAlertResponse( option );
    
    Window par = alertWindow.getParent();
    par.removeWindow( alertWindow );
    
    alertWindow = null;
  }
  
  private class ButtonListener implements Button.ClickListener
  {
    @Override
    public void buttonClick(Button.ClickEvent ce)
    {
      Button b = ce.getButton();
      if ( b != null )
        onOptionClicked( b.getCaption() );
    }
  }

}
