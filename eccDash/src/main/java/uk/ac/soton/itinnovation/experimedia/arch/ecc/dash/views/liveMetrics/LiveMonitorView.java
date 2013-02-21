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
//      Created Date :          02-Feb-2013
//      Created for Project :   ECC Dash
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.HighlightView;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.HighlightViewListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.SimpleView;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UILayoutUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics.visualizers.BaseMetricVisual;
import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.types.UFAbstractEventManager;




public class LiveMonitorView extends SimpleView
{
  private VerticalLayout metricsNavList;
  private VerticalLayout metricsVisualList;
  
  private transient HashMap<UUID, BaseMetricVisual> visualsByMSID;
  private transient HashMap<UUID, VisualNavView>    navViewsByMSID;
  private transient UUID                            currSelectedNavView;
  
  
  public LiveMonitorView()
  {
    super();
    
    visualsByMSID  = new HashMap<UUID, BaseMetricVisual>();
    navViewsByMSID = new HashMap<UUID, VisualNavView>();
    
    createComponents();
  }
  
  public void resetView()
  {
    metricsNavList.removeAllComponents();
    metricsVisualList.removeAllComponents();
    
    currSelectedNavView = null;
    visualsByMSID.clear();
    navViewsByMSID.clear();
  }
  
  public void addMetricVisual( String clientName, String entityName,
                               String attributeName,
                               UUID msID, BaseMetricVisual visual )
  {
    if ( clientName    != null && entityName != null && 
         attributeName != null && msID       != null && visual != null )
      if ( !visualsByMSID.containsKey(msID) )
      {
        VisualNavView navView = new VisualNavView( clientName, entityName,
                                                   attributeName, msID );
        
        metricsNavList.addComponent( (Component) navView.getImplContainer() );
        navViewsByMSID.put( msID, navView );
        
        metricsVisualList.addComponent( (Component) visual.getImplContainer() );
        visualsByMSID.put( msID, visual );
        
        // Space
        metricsVisualList.addComponent( UILayoutUtil.createSpace("5px", null) );
        
        // High-light if first selected
        if ( currSelectedNavView == null ) onNavViewSelected( msID );
      }
  }
  
  public void removeMetricVisual( UUID msID )
  {
    if ( msID != null )
    {
      VisualNavView view = navViewsByMSID.get( msID );
      if ( view != null )
      {
        metricsNavList.removeComponent( (Component) view.getImplContainer() );
        navViewsByMSID.remove( msID );
      }
      
      BaseMetricVisual visual = visualsByMSID.get( msID );
      if ( visual != null )
      {
        metricsVisualList.removeComponent( (Component) visual.getImplContainer() );
        visualsByMSID.remove( msID );
      }
    }
  }
  
  public void updateMetricVisual( UUID msID, MeasurementSet ms )
  {
    if ( msID != null && ms != null )
    {
      BaseMetricVisual visual = visualsByMSID.get( msID );
      
      if ( visual != null ) 
        visual.addMeasurementData( ms );
    }
  }
  
  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();
    vl.setImmediate( true );
    
    Panel panel = new Panel();
    panel.addStyleName( "borderless light" );
    panel.setSizeFull();
    vl.addComponent( panel );

    HorizontalLayout hl = new HorizontalLayout();
    hl.setSpacing( true );
    panel.addComponent( hl );
    
    // Navigation list
    Panel innerPanel = new Panel();
    innerPanel.addStyleName( "borderless light" );
    innerPanel.setCaption( "Current live metrics" );
    hl.addComponent( innerPanel );
    hl.setExpandRatio( innerPanel, 1.0f );
    
    metricsNavList = new VerticalLayout();
    innerPanel.addComponent( metricsNavList );
    
    innerPanel = new Panel();
    innerPanel.addStyleName( "borderless light" );
    innerPanel.setCaption( "Metric visuals" );
    hl.addComponent( innerPanel );
    hl.setExpandRatio( innerPanel, 3.0f );
    
    metricsVisualList = new VerticalLayout();
    innerPanel.addComponent( metricsVisualList );
  }
  
  private void onNavViewSelected( UUID targetID )
  {
    if ( targetID != null )
    {
      // If no currently selected client, make this one the selected
      if ( currSelectedNavView == null )
        currSelectedNavView = targetID;
      else // Otherwise swap
      {
        VisualNavView oldView = navViewsByMSID.get( currSelectedNavView );
        if ( oldView != null ) oldView.setSelected( false );
        
        currSelectedNavView = targetID;
      }
      
      // Select view
      VisualNavView view = navViewsByMSID.get( currSelectedNavView );
      if ( view != null ) view.setSelected( true );
    }
  }
  
  private void onNavViewRemovedClicked( UUID targetID )
  {
    if ( targetID != null )
    {
      // Notify listeners if required
      Collection<LiveMonitorViewListener> listeners = getListenersByType();
      for ( LiveMonitorViewListener listener : listeners )
        listener.onRemoveVisualClicked( targetID );
    }
  }
  
  // Private classes -----------------------------------------------------------
  private class VisualNavView extends HighlightView
  {    
    private transient UUID measurementSetID;
    
    
    public VisualNavView( String clientName, String entityName,
                          String attributeName, UUID msID )
    {
      super();
      
      measurementSetID = msID;
      
      createComponents( clientName, entityName, attributeName );
    }
    
    @Override
    public UUID getDataID()
    { return measurementSetID; }
    
    // Private methods ---------------------------------------------------------
    private void createComponents( String clientName, String entityName,
                                   String attributeName )
    {
      VerticalLayout vl = getViewContents();
      
      Label label = new Label( "Client: " + clientName );
      label.addStyleName( "small" );
      vl.addComponent( label );
      
      // Space
      vl.addComponent( UILayoutUtil.createSpace( "2px", null ) );
      
      // Indent a bit
      HorizontalLayout hl = new HorizontalLayout();
      vl.addComponent( hl );
      
      // Space
      hl.addComponent( UILayoutUtil.createSpace( "2px", null ) );
      VerticalLayout innerVL = new VerticalLayout();
      hl.addComponent( innerVL );
      
      label = new Label( "Entity: " + entityName );
      label.addStyleName( "tiny" );
      innerVL.addComponent( label );
      
      label = new Label( "Attribute: " + attributeName );
      label.addStyleName( "tiny" );
      innerVL.addComponent( label );
      
      // Space
      vl.addComponent( UILayoutUtil.createSpace( "2px", null ) );
      
      // Remove button
      Button button = new Button( "Remove" );
      button.setStyleName( "tiny" );
      button.setData( measurementSetID );
      button.addListener( new RemoveButtonClickedListener() );
      vl.addComponent( button );
      vl.setComponentAlignment( button, Alignment.BOTTOM_RIGHT );
      
      addListener( new NavViewSelected() );
    }
  }
  
  // Private event handling ----------------------------------------------------
  private class NavViewSelected extends UFAbstractEventManager
                                implements HighlightViewListener
  {
    @Override
    public void onHighlightViewSelected( HighlightView view )
    { onNavViewSelected( view.getDataID() ); }
  }
  
  private class RemoveButtonClickedListener implements Button.ClickListener
  {
    @Override
    public void buttonClick(Button.ClickEvent ce) 
    { onNavViewRemovedClicked( (UUID) ce.getButton().getData()); }
  }
}
