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
//      Created Date :          21-Feb-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.dataExport;

import com.vaadin.terminal.FileResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.SimpleView;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UILayoutUtil;




public class DataExportView extends SimpleView
{
  private Table  exportTable;
  private Button downloadMetricsButton;
  private Label  metaStatusLabel;
  private Link   metainfoLink;
  private Label  mainStatusLabel;
  private Link   mainDataLink;
  
  // Table column ids
  private final String tClientName    = "Client";
  private final String tEntityName    = "Entity";
  private final String tAttributeName = "Attribute";
  private final String tMSID          = "Measurement set ID";
  private final String tMSUnit        = "Unit";
  private final String tMeasureCount  = "Total measurements";
  
  
  public DataExportView()
  {
    super();
    
    createComponents();
  }
  
  public void resetView()
  {
    clearExportItems();
    
    metaStatusLabel.setValue( "Data not ready" );
    mainStatusLabel.setValue( "Data not ready" );
    metainfoLink.setEnabled( false );
    mainDataLink.setEnabled( false );
    downloadMetricsButton.setEnabled( false );
  }
  
  public void clearExportItems()
  {
   exportTable.removeAllItems();
   
   downloadMetricsButton.setEnabled( false );
   
   metainfoLink.setResource( null );
   metainfoLink.setEnabled( false );
   
   mainDataLink.setResource( null );
   mainDataLink.setEnabled( false );
  }
  
  public void setDownloadEnabled( boolean enable )
  { downloadMetricsButton.setEnabled( true ); }
  
  public boolean addExportItem( String clientName,    String entityName,
                                String attributeName, UUID msID,
                                String unit,          String metricTotal )
  {
    // Safety first
    if ( clientName == null || entityName == null || attributeName == null || 
         msID == null       || unit == null       || metricTotal == null )
      return false;
    
    exportTable.addItem(
            new Object[] { clientName, entityName, attributeName, unit,
                           msID.toString(), metricTotal}, msID );
    
    return true;
  }
  
  public void setMetaInfoDownloadResource( FileResource resource )
  {
    if ( resource != null )
    {
      metaStatusLabel.removeStyleName( "loading" );
      metaStatusLabel.setValue( "Data ready" );
      
      metainfoLink.setResource( resource );
      metainfoLink.setEnabled( true );
    }
  }
  
  public void setMetricDataDownloadResource( FileResource resource )
  {
    if ( resource != null )
    {
      mainStatusLabel.removeStyleName( "loading" );
      mainStatusLabel.setValue( "Data ready" );
      
      mainDataLink.setResource( resource );
      mainDataLink.setEnabled( true );
			
			displayMessage( "Metric data ready", "Please click on download links" );
    }
  }
  
  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();
    
    Panel panel = new Panel();
    panel.addStyleName( "borderless light" );
    panel.setSizeFull();
    vl.addComponent( panel );
    
    // Header
    HorizontalLayout hl = new HorizontalLayout();
    hl.setWidth( "100%" );
    hl.setStyleName( "eccInfoPanelHeader" );
    panel.addComponent( hl );
    
    Label label = new Label( "  Current exports " );
    label.addStyleName( "h3" );
    hl.addComponent( label );
    hl.setComponentAlignment( label, Alignment.MIDDLE_LEFT );
    
    // Space
    panel.addComponent( UILayoutUtil.createSpace( "5px", null, true ) );
    
    // Table (to be developed further)
    exportTable = new Table( "Metrics to export" );
    exportTable.setSizeFull();
    exportTable.addContainerProperty( tClientName   , String.class, null );
    exportTable.addContainerProperty( tEntityName   , String.class, null );
    exportTable.addContainerProperty( tAttributeName, String.class, null );
    exportTable.addContainerProperty( tMSUnit       , String.class, null );
    exportTable.addContainerProperty( tMSID         , String.class, null );
    exportTable.addContainerProperty( tMeasureCount , String.class, null );
    exportTable.setImmediate( true );
    panel.addComponent( exportTable );
    
    // Space
    panel.addComponent( UILayoutUtil.createSpace( "10px", null ) );
    
    // Export controls
    hl = new HorizontalLayout();
    hl.setStyleName( "eccInfoSubPanel" );
    panel.addComponent( hl );
    
    Button button = new Button( "Clear all exports" );
    button.addStyleName( "large" );
    button.addListener( new ClearExportDataClicked() );
    hl.addComponent( button );
    hl.setComponentAlignment( button, Alignment.MIDDLE_LEFT );
    
    // Space
    Component space = (Component) UILayoutUtil.createSpace( "5px", null, true ) ;
    hl.addComponent( space );
    hl.setComponentAlignment( space, Alignment.MIDDLE_LEFT );
    
    button = new Button( "Add all client data" );
    button.addStyleName( "large" );
    button.addListener( new AddAllClientDataClicked() );
    hl.addComponent( button );
    hl.setComponentAlignment( button, Alignment.MIDDLE_LEFT );
    
    // Space
    panel.addComponent( UILayoutUtil.createSpace( "10px", null ) );
    
    hl = new HorizontalLayout();
    panel.addComponent( hl );
    
    downloadMetricsButton = new Button( "Generate export data" );
    downloadMetricsButton.addStyleName( "big" );
    downloadMetricsButton.setEnabled( false );
    downloadMetricsButton.addListener( new ExportDataClicked() );
    hl.addComponent( downloadMetricsButton );
    
    // Space
    hl.addComponent( UILayoutUtil.createSpace( "10px", null, true ) );
    
    // Links
    VerticalLayout innerVL = new VerticalLayout();
    innerVL.setStyleName( "eccInfoSubPanel" );
    hl.addComponent( innerVL );
    
    // Meta info
    HorizontalLayout innerHL = new HorizontalLayout();
    innerVL.addComponent( innerHL );
    
    metaStatusLabel = new Label( "Data not ready" );
		metaStatusLabel.setImmediate( true );
    innerHL.addComponent( metaStatusLabel );
    
    // Space
    innerHL.addComponent( UILayoutUtil.createSpace( "5px", null, true ) );
    
    metainfoLink = new Link( "Download metric meta-data", null );
    metainfoLink.setImmediate( true );
    metainfoLink.setEnabled( false );
    innerHL.addComponent( metainfoLink );
    
    // Main data
    innerHL = new HorizontalLayout();
    innerVL.addComponent( innerHL );
    
    mainStatusLabel = new Label( "Data not ready" );
		mainStatusLabel.setImmediate( true );
    innerHL.addComponent( mainStatusLabel );
    
    // Space
    innerHL.addComponent( UILayoutUtil.createSpace( "5px", null, true ) );
    
    mainDataLink = new Link( "Download metric data", null );
    mainDataLink.setImmediate( true );
    mainDataLink.setEnabled( false );
    innerHL.addComponent( mainDataLink );
  }
  
  // Event handlers ------------------------------------------------------------
  private void onAddAllClientData()
  {
    Collection<DataExportViewListener> listeners = getListenersByType();
    for ( DataExportViewListener listener : listeners )
      listener.onAddAllClientData();
  }
  
  private void onClearAllExports()
  {
    Collection<DataExportViewListener> listeners = getListenersByType();
    for ( DataExportViewListener listener : listeners )
      listener.onClearAllExports();
  }
  
  private void onExportData()
  {
    metaStatusLabel.addStyleName( "loading" );
    metaStatusLabel.setValue( "Generating data" );
    
    mainStatusLabel.addStyleName( "loading" );
    mainStatusLabel.setValue( "Generating data" );
		
		displayMessage( "Please wait...", "Generating export data" );
    
    Collection<DataExportViewListener> listeners = getListenersByType();
    for ( DataExportViewListener listener : listeners )
      listener.onExportData();
  }
  
  private class AddAllClientDataClicked implements Button.ClickListener
  {
    @Override
    public void buttonClick(Button.ClickEvent ce)
    { onAddAllClientData(); }
  }
  
  private class ClearExportDataClicked implements Button.ClickListener
  {
    @Override
    public void buttonClick(Button.ClickEvent ce)
    { onClearAllExports(); }
  }
  
  private class ExportDataClicked implements Button.ClickListener
  {
    @Override
    public void buttonClick(Button.ClickEvent ce)
    { onExportData(); }
  }
}
