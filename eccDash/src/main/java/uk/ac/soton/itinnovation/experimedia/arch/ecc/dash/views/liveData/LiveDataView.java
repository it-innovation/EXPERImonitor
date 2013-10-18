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
//      Created Date :          17-Oct-2013
//      Created for Project :   ECC Dashboard
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveData;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.*;

import com.vaadin.ui.*;




public class LiveDataView extends SimpleView
{
  private LiveMetricView liveMetricView;
  private LiveProvView   liveProvView;

  public LiveDataView()
  {
    super();
    
    createComponents();
  }
  
  public LiveMetricView getLiveMetricView()
  { return liveMetricView; }
  
  public LiveProvView getLiveProvView()
  { return liveProvView; }
  
  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();
    
    Panel panel = new Panel();
    panel.addStyleName( "borderless" );
    panel.setSizeFull();
    vl.addComponent( panel );
    
    TabSheet ts = new TabSheet();
    ts.addStyleName( "borderless" );
    ts.setSizeFull();
    panel.addComponent( ts );
    
    liveMetricView = new LiveMetricView();
    ts.addTab( (Component) liveMetricView.getImplContainer(), "Live Metrics", null );
    
    liveProvView = new LiveProvView();
    ts.addTab( (Component) liveProvView.getImplContainer(), "Live Provenance", null );
  }
}
