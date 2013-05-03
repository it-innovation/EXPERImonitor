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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics.visualizers;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.SimpleView;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UILayoutUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UIResource;




public abstract class BaseMetricVisual extends SimpleView
{
  protected final String defaultChartWidth  = "512px";
  protected final String defaultChartHeight = "342px";
  
  protected Label visualTitle;
  protected Label visualUnit;
  protected Label visualType;
  
  protected transient LinkedList<Measurement> cachedMeasurements;
  protected transient int                     maxCachedMeasurements = 20;
  
  private VerticalLayout    vizContainer;
  private transient UUID    visualID;
  private transient boolean visualVisible;
  
 
  public BaseMetricVisual( UUID id )
  {
    super();
    
    visualID = id;
    cachedMeasurements = new LinkedList<Measurement>();
    
    createComponents();
  }
  
  public UUID getID()
  { return visualID; }
  
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
  
  public void clearMeasurements()
  {
    cachedMeasurements.clear();
    updateView();
  }
  
  public void addMeasurementData( MeasurementSet ms )
  {
    if ( ms != null )
    {
      // Gather limited measurements internally
      Set<Measurement> measurements = ms.getMeasurements();
      if ( measurements != null && !measurements.isEmpty() )
      {
        Iterator<Measurement> mIt = ms.getMeasurements().iterator();
        while ( mIt.hasNext() )
        {
          Measurement m = mIt.next();
          cachedMeasurements.add( m );
          
          if ( cachedMeasurements.size() > maxCachedMeasurements )
            cachedMeasurements.removeFirst();
        }
      }
      
      updateView();
    }
  }
  
  public void setMaxMeasurements( int max )
  {
    if ( max > 0 )
    {
      while ( cachedMeasurements.size() > max )
      { cachedMeasurements.removeFirst(); } 
      
      maxCachedMeasurements = max;
      updateView();
    }
  }
  
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
    hl.setWidth( defaultChartWidth );
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
    visualUnit.addStyleName( "small" );
    innerHL.addComponent( visualUnit );
    innerHL.setComponentAlignment( visualUnit, Alignment.BOTTOM_LEFT );
    
    // Space
    innerHL.addComponent( UILayoutUtil.createSpace( "5px", null, true ) );
    
    visualType = new Label();
    visualType.addStyleName( "small" );
    innerHL.addComponent( visualType );
    innerHL.setComponentAlignment( visualType, Alignment.BOTTOM_LEFT );
    
    // Control part
    innerHL = new HorizontalLayout();
    hl.addComponent( innerHL );
    hl.setComponentAlignment( innerHL, Alignment.TOP_RIGHT );
    
    // Expand/hide button
    Button button = new Button();
    button.setIcon( UIResource.getResource("minimiseIcon") );
    button.addStyleName( "borderless icon-on-top" );
    button.addListener( new HideButtonListener() );
    innerHL.addComponent( button );
    
    // Space
    innerHL.addComponent( UILayoutUtil.createSpace( "4px", null, true ) );
    
    // Close button
    button = new Button();
    button.setIcon( UIResource.getResource("closeIcon") );
    button.addStyleName( "borderless icon-on-top" );
    button.setData( true );
    button.addListener( new CloseButtonListener() );
    innerHL.addComponent( button );
    
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
        button.setIcon( UIResource.getResource("maximiseIcon") );
      }
      else
      {
        visualVisible = true;
        
        vizContainer.setVisible( visualVisible );
        button.setIcon( UIResource.getResource("minimiseIcon") );
      } 
    }
  }
  
  private void onCloseButtonClicked()
  {
    Collection<BaseMetricVisualListener> listeners = getListenersByType();
    for( BaseMetricVisualListener listener : listeners )
      listener.onVisualClosed( visualID );
  }
  
  private class HideButtonListener implements Button.ClickListener
  {
    @Override
    public void buttonClick(Button.ClickEvent ce) 
    { onHideButtonClicked( ce.getButton() ); }
  }
  
  private class CloseButtonListener implements Button.ClickListener
  {
    @Override
    public void buttonClick(Button.ClickEvent ce) 
    { onCloseButtonClicked(); }
  }
}
