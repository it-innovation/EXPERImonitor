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
//      Created Date :          06-Feb-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents;

import com.vaadin.event.MouseEvents;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import java.util.UUID;




public class HighlightView extends SimpleView
{
  private VerticalLayout hlViewContents;
  
  private transient UUID dataID;
  private transient String highlightStyle = "eccInfoPanelHighlight";
  private transient String lowlightStyle  = "eccInfoPanel";
  

  public HighlightView()
  {
    super();
    
    createComponents();
  }
  
  public void setHiStyles( String high, String low )
  {
    highlightStyle = high;
    lowlightStyle  = low;
  }
  
  public void setDataID( UUID id )
  { dataID = id; }
  
  public UUID getDataID()
  { return dataID; }
  
  public void setSelected( boolean selected )
  {
    if ( selected )
      hlViewContents.setStyleName( highlightStyle );
    else
      hlViewContents.setStyleName( lowlightStyle );
  }
  
  // Protected methods ---------------------------------------------------------
  @Override
  protected VerticalLayout getViewContents()
  { return hlViewContents; }
  
  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = super.getViewContents();
      
    Panel viewPanel = new Panel();
    viewPanel.setSizeFull();
    viewPanel.addListener( new ViewSelected() );
    viewPanel.addStyleName( "borderless light" );
    viewPanel.addStyleName( "small" );
    vl.addComponent( viewPanel );

    // Set up contents
    hlViewContents = (VerticalLayout) viewPanel.getContent();
    hlViewContents.addStyleName( "eccInfoPanel" );
    hlViewContents.setImmediate( true );
  }
  
  private void onViewClicked()
  {
    Collection<HighlightViewListener> listeners = getListenersByType();
    for( HighlightViewListener listener : listeners )
      listener.onHighlightViewSelected( this );
  }
  
  // Private event handling ----------------------------------------------------
  private class ViewSelected implements MouseEvents.ClickListener
  {
    @Override
    public void click(MouseEvents.ClickEvent event)
    { onViewClicked(); }
  }
}
