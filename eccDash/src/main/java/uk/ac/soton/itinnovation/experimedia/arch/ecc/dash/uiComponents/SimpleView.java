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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents;

import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.mvc.IUFView;
import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.types.UFAbstractEventManager;

import com.vaadin.ui.*;

import java.io.Serializable;




public class SimpleView extends UFAbstractEventManager implements Serializable,
                                                                  IUFView
{
  private VerticalLayout viewContents;
  
  
  public SimpleView()
  {
    viewContents = new VerticalLayout();
  }
  
  public SimpleView( boolean hasBorder )
  {
    viewContents = new VerticalLayout();
    if ( hasBorder ) viewContents.addStyleName( "light" );
  }
  
  // IUFView -------------------------------------------------------------------
  @Override
	public Object getImplContainer()
	{ return viewContents; }
	
	@Override
  public boolean isVisible()
  {
    if ( viewContents != null )
      return viewContents.isVisible();
    
    return false;
  }
  
  @Override
  public void setVisible( boolean visible )
  {
    if ( viewContents != null )
      viewContents.setVisible( visible );
  }
  
  @Override
  public void updateView() {}
  
  @Override
  public void displayMessage( String title, String content )
  {
    Window wdw = viewContents.getApplication().getMainWindow();
    
    if ( wdw != null )
      wdw.showNotification( title, content,
                            com.vaadin.ui.Window.Notification.TYPE_HUMANIZED_MESSAGE );
  }
  
  @Override
  public void displayWarning( String title, String content )
  {
    Window wdw = viewContents.getApplication().getMainWindow();
    
    if ( wdw != null )
      wdw.showNotification( title, content,
                            com.vaadin.ui.Window.Notification.TYPE_WARNING_MESSAGE ); 
  }
  
  // Protected methods ---------------------------------------------------------
  protected VerticalLayout getViewContents()
  { return viewContents; }
}
