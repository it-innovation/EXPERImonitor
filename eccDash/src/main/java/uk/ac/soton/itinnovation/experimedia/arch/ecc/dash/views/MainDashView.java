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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.dataExport.DataExportController;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveData.LiveMonitorController;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.system.SystemView;

import com.vaadin.ui.*;
import com.vaadin.ui.TabSheet.Tab;
import org.vaadin.artur.icepush.ICEPush;

import java.net.URL;
import java.util.Collection;




public class MainDashView extends SimpleView
{
  private VerticalLayout dashHeaderView;
  private Panel          dashViewBody;
  private Panel          dashContentContainer;
  private VerticalLayout dashContentView;
  private VerticalLayout dashSideView;

  private MonitorControlView    controlView;
  private ClientConnectionsView connectionsView;
  private ClientInfoView        clientInfoView;
  private SystemView            systemView;
  private NAGIOSView            nagiosView;
  private DashLogView           logView;
  private boolean               subViewsReady;

  private TabSheet subViewsSheet;

  private transient LiveMonitorController liveMonitorController;
  private transient DataExportController  dataExportController;


  public MainDashView()
  {
    super();

    createFrameworkComponents();
  }

  public void initialise( UIPushManager pushMgr )
  {
    createMainDashControls( pushMgr );
  }

  public void resetViews()
  {
    controlView.resetView();
    connectionsView.resetView();
    clientInfoView.resetView();

    liveMonitorController.reset();
    dataExportController.reset();
		
		// Return to System/Connect clients view
		systemView.switchViewFocus( 0 );
		switchViewFocus( 0 );
  }

  public void updateViewport()
  {
    if ( subViewsReady )
    {
      VerticalLayout vl = getViewContents();
      Window mainWindow = vl.getApplication().getMainWindow();

      if ( mainWindow != null )
      {
        final float vMargin = 220;
        final float hMargin = 200;

        VerticalLayout cVL = (VerticalLayout) controlView.getImplContainer();
        float bodyHeight   = mainWindow.getHeight() - cVL.getHeight();
        bodyHeight -= vMargin;

        float bodyWidth = mainWindow.getWidth() - cVL.getWidth();
        bodyWidth -= hMargin;

        subViewsSheet.setWidth( bodyWidth, TabSheet.UNITS_PIXELS );
        dashViewBody.setHeight( bodyHeight, VerticalLayout.UNITS_PIXELS );
      }
    }
  }

  public void shutDownUI()
  {
    if ( liveMonitorController != null ) liveMonitorController.shutDown();
    if ( dataExportController != null  ) dataExportController.shutDown();
  }

  public void addLogMessage( String message )
  { if ( logView != null ) logView.addLogMessage( message ); }

  public void switchViewFocus( int index )
  {
    if ( subViewsSheet != null )
    {
      Tab t = subViewsSheet.getTab( index );
      if ( t != null ) subViewsSheet.setSelectedTab( t );
    }
  }

  public MonitorControlView getMonitorControlView()
  { return controlView; }

  public ClientConnectionsView getConnectionsView()
  { return connectionsView; }

  public ClientInfoView getClientInfoView()
  { return clientInfoView; }

  public LiveMonitorController getLiveMonitorController()
  { return liveMonitorController; }

  public DataExportController getDataExportController()
  { return dataExportController; }

  public void setExperimentPhase( EMPhase phase )
  {
    switch ( phase )
    {
      case eEMUnknownPhase : logView.addLogMessage( "Tried an displaying an unknown phase!" ); break;

      default:
      {
        controlView.setPhase( phase );
        connectionsView.updateClientsInPhase( phase );
      }
    }
  }

  public void pointToNAGIOS( URL fullURL )
  {
    if ( fullURL != null && nagiosView != null )
      nagiosView.pointToNAGIOS( fullURL );
  }

  // Private methods -----------------------------------------------------------
  private void createFrameworkComponents()
  {
    VerticalLayout vl = getViewContents();

    dashHeaderView = new VerticalLayout();
    vl.addComponent( dashHeaderView );

    // Body of the view (will adjust to window size and scroll contents)
    dashViewBody = new Panel();
    dashViewBody.setImmediate( true );
    dashViewBody.setStyleName( "borderless light" );
    vl.addComponent( dashViewBody );
    // Make sure content scrolls
    VerticalLayout bodyVL = (VerticalLayout) dashViewBody.getContent();
    bodyVL.setMargin( false );
    bodyVL.setSizeUndefined();

    // Horizontal: side and main content container
    HorizontalLayout hl = new HorizontalLayout();
    dashViewBody.addComponent( hl );

    dashSideView = new VerticalLayout();
    hl.addComponent( dashSideView );

    // Main content view
    dashContentContainer = new Panel();
    dashContentContainer.addStyleName( "borderless light" );
    VerticalLayout vpl = (VerticalLayout) dashContentContainer.getContent(); // Reduce internal padding here
    vpl.setMargin( false );
    hl.addComponent( dashContentContainer );

    dashContentView = new VerticalLayout();
    dashContentContainer.addComponent( dashContentView );

    // Space
    bodyVL.addComponent( UILayoutUtil.createSpace( "20px", null ) );

    // Footer
    hl = new HorizontalLayout();
    bodyVL.addComponent( hl );
    bodyVL.setComponentAlignment( hl, Alignment.BOTTOM_LEFT );

    // Shutdown button
    Button button = new Button( "Shut down ECC" );
    button.addStyleName( "small" );
    button.addListener( new ShutdownButtonListener() );
    bodyVL.addComponent( button );

    // Space
    bodyVL.addComponent( UILayoutUtil.createSpace( "30px", null, true ) );

    Label label = new Label( "© University of Southampton IT Innovation Centre 2013" );
    label.setStyleName( "small" );
    bodyVL.addComponent( label );
  }

  private void createMainDashControls( UIPushManager pushMgr )
  {
    controlView = new MonitorControlView();
    dashHeaderView.addComponent( (Component) controlView.getImplContainer() );

    connectionsView = new ClientConnectionsView();
    dashSideView.addComponent( (Component) connectionsView.getImplContainer() );

    createSubViews( pushMgr );
  }

  private void createSubViews( UIPushManager pushMgr )
  {
    dashContentView.removeAllComponents();

    // Space
    dashContentView.addComponent( UILayoutUtil.createSpace( "4px", null ) );

    subViewsSheet = new TabSheet();
    subViewsSheet.addStyleName( "borderless" );
    subViewsSheet.addStyleName( "large" );
    subViewsSheet.setSizeFull();
    dashContentView.addComponent( subViewsSheet );

    clientInfoView = new ClientInfoView();
    logView = new DashLogView();
    nagiosView = new NAGIOSView();
    systemView = new SystemView(connectionsView, clientInfoView, nagiosView, logView);
    subViewsSheet.addTab( (Component) systemView.getImplContainer(), "System", null );

    liveMonitorController = new LiveMonitorController( pushMgr );
    subViewsSheet.addTab( (Component) liveMonitorController.getLiveView().getImplContainer(),
                          "Live monitor", null );

    dataExportController = new DataExportController();
    subViewsSheet.addTab( (Component) dataExportController.getExportView().getImplContainer(),
                          "Data export", null );

    subViewsReady = true;

    updateViewport();
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
