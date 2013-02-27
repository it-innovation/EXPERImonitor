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
//      Created for Project :   ECC Dash
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics.visualizers;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.SimpleView;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UILayoutUtil;




public abstract class BaseMetricVisual extends SimpleView
{
  protected Label visualTitle;
  protected Label visualUnit;
  protected Label visualType;
  private VerticalLayout vizContainer;
  
  private transient boolean visualVisible;
  
  
  public BaseMetricVisual()
  {
    super();
    
    createComponents();
  }
  
  public void setTitle( String title )
  { if ( title != null ) visualTitle.setValue( title ); }
  
  public void setMetricInfo( String unit, String type )
  {
    if ( unit != null && type != null )
    {      
      visualUnit.setValue( unit );
      visualType.setValue( "(" + type + ")" );
    }
  }
  
  // Sub-classes to implement --------------------------------------------------
  public abstract void addMeasurementData( MeasurementSet ms );
  
  // Protected methods ---------------------------------------------------------
  @Override
  protected VerticalLayout getViewContents()
  { return vizContainer; }

  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = super.getViewContents();
    
    // Header
    HorizontalLayout hl = new HorizontalLayout();
    hl.setSpacing( true );
    hl.setStyleName( "eccInfoPanelHeader" );
    vl.addComponent( hl );
    
    // Info part
    HorizontalLayout innerHL = new HorizontalLayout();
    hl.addComponent( innerHL );
    hl.setExpandRatio( innerHL, 1.0f );
    
    // Space
    innerHL.addComponent( UILayoutUtil.createSpace( "5px", null, true ) );
    
    // Title
    visualTitle = new Label();
    visualTitle.addStyleName( "h3" );
    innerHL.addComponent( visualTitle );
    
    // Space
    innerHL.addComponent( UILayoutUtil.createSpace( "20px", null, true ) );
    
    // Other info
    visualUnit = new Label();
    visualUnit.addStyleName( "h4" );
    innerHL.addComponent( visualUnit );
    
    // Space
    innerHL.addComponent( UILayoutUtil.createSpace( "5px", null, true ) );
    
    visualType = new Label();
    visualType.addStyleName( "h4" );
    innerHL.addComponent( visualType );
    
    // Control part
    innerHL = new HorizontalLayout();
    hl.addComponent( innerHL );
    hl.setExpandRatio( innerHL, 3.0f );
    
    // Expand/hide button
    Button button = new Button( "hide" );
    button.addStyleName( "small" );
    button.addListener( new HideButtonListener() );
    innerHL.addComponent( button );
    innerHL.setComponentAlignment( button, Alignment.MIDDLE_RIGHT );
    
    // Space
    innerHL.addComponent( UILayoutUtil.createSpace( "5px", null, true ) );
    
    // Close button
    button = new Button( "close" );
    button.addStyleName( "small" );
    button.setData( true );
    innerHL.addComponent( button );
    innerHL.setComponentAlignment( button, Alignment.MIDDLE_RIGHT );
    
    // Space
    vl.addComponent( UILayoutUtil.createSpace( "5px", null, true ) );
  
    // Graph area
    vizContainer  = new VerticalLayout();
    visualVisible = true;
    vl.addComponent( vizContainer );
  }
  
  // Event handling ------------------------------------------------------------
  private void onHideButtonClicked( Button button )
  {
    if ( button != null )
    {
      if ( visualVisible )
      {
        visualVisible = false;
        
        vizContainer.setVisible( visualVisible );
        button.setCaption( "show" );
      }
      else
      {
        visualVisible = true;
        
        vizContainer.setVisible( visualVisible );
        button.setCaption( "hide" );
      } 
    }
  }
  
  private void onCloseButtonClicked()
  {
    
  }
  
  private class HideButtonListener implements Button.ClickListener
  {
    @Override
    public void buttonClick(Button.ClickEvent ce) 
    { onHideButtonClicked( ce.getButton() ); }
  }
}
