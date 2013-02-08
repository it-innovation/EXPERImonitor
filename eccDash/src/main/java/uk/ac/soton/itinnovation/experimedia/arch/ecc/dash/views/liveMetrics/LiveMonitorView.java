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
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.HashMap;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.SimpleView;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UILayoutUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics.visualizers.BaseMetricVisual;




public class LiveMonitorView extends SimpleView
{
  private VerticalLayout metricsNavList;
  private VerticalLayout metricsVisualList;
  
  private HashMap<UUID, BaseMetricVisual> visualsByMSID;
  
  
  public LiveMonitorView()
  {
    super();
    
    visualsByMSID = new HashMap<UUID, BaseMetricVisual>();
    
    createComponents();
  }
  
  public void addMetricVisual( UUID msID, BaseMetricVisual visual )
  {
    if ( msID != null && visual != null )
      if ( !visualsByMSID.containsKey(msID) )
      {
        metricsVisualList.addComponent( (Component) visual.getImplContainer() );
        visualsByMSID.put( msID, visual );
        
        // Space
        metricsVisualList.addComponent( UILayoutUtil.createSpace("5px", null) );
      }
  }
  
  public void removeMetricVisual( UUID msID )
  {
    if ( msID != null )
    {
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

    HorizontalLayout hl = new HorizontalLayout();
    hl.setSpacing( true );
    hl.setSizeFull();
    vl.addComponent( hl );
    
    Panel panel = new Panel();
    panel.addStyleName( "borderless light" );
    panel.setCaption( "Current live metrics" );
    hl.addComponent( panel );
    hl.setExpandRatio( panel, 1.0f );
    
    metricsNavList = new VerticalLayout();
    metricsNavList.setImmediate( true );
    panel.addComponent( metricsNavList );
    
    panel = new Panel();
    panel.addStyleName( "borderless light" );
    panel.setCaption( "Metric visuals" );
    panel.setSizeFull();
    hl.addComponent( panel );
    hl.setExpandRatio( panel, 3.0f );
    
    metricsVisualList = new VerticalLayout();
    metricsVisualList.setSizeFull();
    metricsVisualList.setImmediate( true );
    panel.addComponent( metricsVisualList );
  }
}
