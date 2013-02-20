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


import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.client.ClientInfoView;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.client.ClientConnectionsView;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.SimpleView;

import com.vaadin.ui.*;
import java.util.Collection;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UILayoutUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveMetrics.LiveMonitorController;




public class MainDashView extends SimpleView
{
  private VerticalLayout dashHeaderView;
  private Panel          dashContentContainer;
  private VerticalLayout dashContentView;
  private VerticalLayout dashSideView;
  
  private MonitorControlView    controlView;
  private ClientConnectionsView connectionsView;
  private ClientInfoView        clientInfoView;
  private NAGIOSView            nagiosView;
  private DashLogView           logView;
  
  private ProgressIndicator livePI;
  private TabSheet          subViewsSheet;
  
  private transient LiveMonitorController liveMonitorController;

  
  public MainDashView()
  {
    super();
    
    createFrameworkComponents();
  }
  
  public void initialise()
  {
    createMainDashControls();
  }
  
  public void resetViews()
  {
    controlView.resetView();
    connectionsView.resetView();
    clientInfoView.resetView();
  }
  
  public void shutDownUI()
  {
    if ( livePI != null )
    {
      livePI.setIndeterminate( false );
      VerticalLayout vl = getViewContents();
      vl.removeComponent( livePI );
    }
    
    if ( liveMonitorController != null ) liveMonitorController.shutDown();
  }
  
  public void addLogMessage( String message )
  { if ( logView != null ) logView.addLogMessage( message ); }
  
  public MonitorControlView getMonitorControlView()
  { return controlView; }
  
  public ClientConnectionsView getConnectionsView()
  { return connectionsView; }
  
  public ClientInfoView getClientInfoView()
  { return clientInfoView; }
  
  public LiveMonitorController getLiveMonitorController()
  { return liveMonitorController; }
  
  public void setExperimentPhase( EMPhase phase )
  {
    if ( phase != EMPhase.eEMUnknownPhase )
    {
      controlView.setPhase( phase );
      connectionsView.updateClientsInPhase( phase );
    }
  }
  
  // Private methods -----------------------------------------------------------
  private void createFrameworkComponents()
  {
    VerticalLayout vl = getViewContents();
    
    livePI = new ProgressIndicator();
    livePI.setWidth( "0px" );
    livePI.setHeight( "0px" );
    livePI.setIndeterminate( true );
    vl.addComponent( livePI );
    
    dashHeaderView = new VerticalLayout();
    vl.addComponent( dashHeaderView );
    
    // Horizontal: side and main content container
    HorizontalLayout hl = new HorizontalLayout();
    hl.setSizeFull();
    hl.setSpacing( true );
    vl.addComponent( hl );
    
    dashSideView = new VerticalLayout();
    hl.addComponent( dashSideView );
    hl.setExpandRatio( dashSideView, 1.0f );
    
    // Main content view
    dashContentContainer = new Panel();
    dashContentContainer.addStyleName( "borderless light" );
    dashContentContainer.setSizeFull();
    VerticalLayout vpl = (VerticalLayout) dashContentContainer.getContent(); // Reduce internal padding here
    vpl.setMargin( false );
    hl.addComponent( dashContentContainer );
    hl.setExpandRatio( dashContentContainer, 4.0f );
    
    dashContentView = new VerticalLayout();
    dashContentView.setSizeFull();
    dashContentContainer.addComponent( dashContentView );
    
    // Space
    vl.addComponent( UILayoutUtil.createSpace( "20px", null ) );
    
    // Footer
    hl = new HorizontalLayout();
    vl.addComponent( hl );
    vl.setComponentAlignment( hl, Alignment.BOTTOM_LEFT );
    
    // Shutdown button
    Button button = new Button( "Shut down ECC" );
    button.addStyleName( "small" );
    button.addListener( new ShutdownButtonListener() );
    hl.addComponent( button );
    
    // Space
    hl.addComponent( UILayoutUtil.createSpace( "30px", null, true ) );
    
    Label label = new Label( "© University of Southampton IT Innovation Centre 2013" );
    label.setStyleName( "small" );
    hl.addComponent( label );
  }
  
  private void createMainDashControls()
  {
    controlView = new MonitorControlView();
    dashHeaderView.addComponent( (Component) controlView.getImplContainer() );
    
    connectionsView = new ClientConnectionsView();
    dashSideView.addComponent( (Component) connectionsView.getImplContainer() );
    
    createSubViews();
  }
  
  private void createSubViews()
  {
    dashContentContainer.setCaption( "Monitoring views" );
    dashContentView.removeAllComponents();
    
    // Space
    dashContentView.addComponent( UILayoutUtil.createSpace( "2px", null ) );
    
    subViewsSheet = new TabSheet();
    subViewsSheet.addStyleName( "borderless" );
    subViewsSheet.addStyleName( "large" );
    subViewsSheet.setSizeFull();
    dashContentView.addComponent( subViewsSheet );
    
    clientInfoView = new ClientInfoView();
    subViewsSheet.addTab( (Component) clientInfoView.getImplContainer(), "Client info", null );
    
    liveMonitorController = new LiveMonitorController();
    subViewsSheet.addTab( (Component) liveMonitorController.getLiveView().getImplContainer(), 
                          "Metric monitor", null );
    
    nagiosView = new NAGIOSView();
    subViewsSheet.addTab( (Component) nagiosView.getImplContainer(), "Systems monitor", null );
    
    logView = new DashLogView();
    subViewsSheet.addTab( (Component) logView.getImplContainer(), "ECC log", null );
  }
  
  // Event handlers ------------------------------------------------------------
  private void onShutdownClicked()
  {    
    Collection<MainDashViewListener> listeners = getListenersByType();
    for( MainDashViewListener listener : listeners )
      listener.onShutdownECCClicked();
  }
  
  private class ShutdownButtonListener implements Button.ClickListener
  {
    @Override
    public void buttonClick(Button.ClickEvent ce) { onShutdownClicked(); }
  }
}
