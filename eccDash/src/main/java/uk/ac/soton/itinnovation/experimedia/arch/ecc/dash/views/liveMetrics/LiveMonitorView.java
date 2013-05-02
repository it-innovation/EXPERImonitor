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
//      Created for Project :   EXPERIMEDIA
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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics.visualizers.BaseMetricVisualListener;
import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.types.UFAbstractEventManager;




public class LiveMonitorView extends SimpleView
                             implements BaseMetricVisualListener
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
        
        visual.addListener( this );
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
        
        // Do not listen to this visual any more
        unhookNotifier( visual );
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
  
  // BaseMetricVisualListener --------------------------------------------------
  @Override
  public void onVisualClosed( UUID visualID )
  {
    if ( visualID != null )
      onNavViewRemovedClicked( visualID );
  }
  
  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();
    
    Panel vizBody = new Panel();
    vl.addComponent( vizBody );
    vizBody.setStyleName( "borderless light" );
  
    HorizontalLayout hl = new HorizontalLayout();
    hl.setStyleName( "eccDashDefault" );
    vizBody.addComponent( hl );
   
    metricsNavList = new VerticalLayout();
    metricsNavList.setWidth( "250px" );
    hl.addComponent( metricsNavList );
    
    // Space
    hl.addComponent( UILayoutUtil.createSpace( "4px", null, true ) );
    
    Panel visualPanel = new Panel();
    visualPanel.setStyleName( "borderless light" );
    hl.addComponent( visualPanel );
    metricsVisualList = (VerticalLayout) visualPanel.getContent();
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
      
      // Space
      vl.addComponent( UILayoutUtil.createSpace( "4px", null ) );
      
      // Indent
      HorizontalLayout hl = new HorizontalLayout();
      vl.addComponent( hl );
      hl.addComponent( UILayoutUtil.createSpace( "4px", null, true ) );
      VerticalLayout innerVL = new VerticalLayout();
      hl.addComponent( innerVL );
      
      Label label = new Label( clientName );
      label.addStyleName( "small" );
      innerVL.addComponent( label );
      
      // Space
      innerVL.addComponent( UILayoutUtil.createSpace( "2px", null ) );
      
      label = new Label( "Entity: " + entityName );
      label.addStyleName( "tiny" );
      innerVL.addComponent( label );
      
      label = new Label( "Attribute: " + attributeName );
      label.addStyleName( "tiny" );
      innerVL.addComponent( label );
      
      // Space
      vl.addComponent( UILayoutUtil.createSpace( "4px", null ) );
      
      // Remove button      
      Button button = new Button( "Remove" );
      button.setStyleName( "small" );
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
