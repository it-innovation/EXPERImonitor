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
//      Created Date :          04-Feb-2013
//      Created for Project :   ECC Dash
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.client;

import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricHelper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.SimpleView;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UILayoutUtil;




public class ClientInfoView extends SimpleView
{
  private Label          clientNameLabel;
  private Label          clientStatusLabel;
  private Label          clientIDLabel;
  private VerticalLayout clientInfoHolder;
  
  private transient UUID currClientID;
  
  
  public ClientInfoView()
  {
    super( true );
    
    createComponents();
  }
  
  public UUID getCurrentClientID()
  { return currClientID; }
  
  public void writeClientInfo( EMClient client )
  {
    if ( client != null )
    {
      Set<MetricGenerator> mGens = client.getCopyOfMetricGenerators();
      
      writeMainInfo( client, mGens );
      writeMetricMetaData( mGens );
      
      currClientID = client.getID();
    }
  }
  
  public void updateClientConnectivityStatus( EMClient client, boolean connected )
  {
    if ( client != null && client.getID().equals(currClientID) )
    {
      if ( connected )
        clientStatusLabel.setValue( "(connected)" );
      else
      {
        clientStatusLabel.setValue( "(disconnected)" );
        clientInfoHolder.removeAllComponents();
      }
    }
  }
  
  public void resetView()
  {
    clientNameLabel.setValue( "No client information yet" );
    clientStatusLabel.setValue( "" );
    clientIDLabel.setValue( "" );
    clientInfoHolder.removeAllComponents();
  }
  
  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();
    
    Panel panel = new Panel();
    panel.addStyleName( "borderless light" );
    panel.setSizeFull();
    vl.addComponent( panel );
    
    HorizontalLayout hl = new HorizontalLayout();
    hl.setWidth( "100%" );
    hl.setStyleName( "eccInfoPanelHeader" );
    panel.addComponent( hl );
    
    // Space
    hl.addComponent( UILayoutUtil.createSpace( "5px", null, true ) );
    
    // Main info
    HorizontalLayout innerHL = new HorizontalLayout();
    hl.addComponent( innerHL );
    
    clientNameLabel = new Label( "No client information yet" );
    clientNameLabel.addStyleName( "h2" );
    clientNameLabel.setImmediate( true );
    innerHL.addComponent( clientNameLabel );
    innerHL.setComponentAlignment( clientNameLabel, Alignment.TOP_LEFT );
    
    // Space
    innerHL.addComponent( UILayoutUtil.createSpace( "50px", null, true ) );
    
    // Status
    clientStatusLabel = new Label();
    clientStatusLabel.setStyleName( "h3" );
    clientStatusLabel.setImmediate( true );
    innerHL.addComponent( clientStatusLabel );
    innerHL.setComponentAlignment( clientStatusLabel, Alignment.TOP_RIGHT );
    
    // Space
    innerHL.addComponent( UILayoutUtil.createSpace( "100%", null, true ) );
    
    // Space
    panel.addComponent( UILayoutUtil.createSpace( "5px", null ) );
    
    // ID
    clientIDLabel = new Label();
    clientIDLabel.addStyleName( "small" );
    clientIDLabel.setImmediate( true );
    panel.addComponent( clientIDLabel );
    
    // Space
    panel.addComponent( UILayoutUtil.createSpace( "5px", null ) );
    
    // Main info area
    clientInfoHolder = new VerticalLayout();
    clientInfoHolder.setStyleName( "eccDashDefault" );
    clientInfoHolder.setImmediate( true );
    clientInfoHolder.setSizeFull();
    panel.addComponent( clientInfoHolder );
    
    resetView();
  }
  
  private void writeMainInfo( EMClient client, Set<MetricGenerator> mGens )
  {
    clientNameLabel.setValue( client.getName() );
    clientStatusLabel.setValue( "(connected)" );
    clientIDLabel.setValue( "Unique ID: " + client.getID().toString() );
  }
  
  private void writeMetricMetaData( Set<MetricGenerator> mGens )
  {
    clientInfoHolder.removeAllComponents();
    
    if ( !mGens.isEmpty() )
    {
      Map<UUID, Entity> knownEntities = MetricHelper.getAllEntities( mGens );
      Iterator<Entity> entIt = knownEntities.values().iterator();
      while ( entIt.hasNext() )
      {
        // Write out entity
        Entity entity = entIt.next();
        createEntityInfo( clientInfoHolder, entity );
        
        // Write out attributes
        Iterator<Attribute> attIt = entity.getAttributes().iterator();
        while ( attIt.hasNext() )
        {
          // Indent a little
          HorizontalLayout hl = new HorizontalLayout();
          hl.addComponent( UILayoutUtil.createSpace( "5px", null, true ) );
          clientInfoHolder.addComponent( hl );
          
          VerticalLayout attVL = new VerticalLayout();
          hl.addComponent( attVL );
          
          Attribute att = attIt.next();
          createAttributeInfo( attVL, att );
        }
        
        // Space
        clientInfoHolder.addComponent( UILayoutUtil.createSpace( "5px", null ) );
      }
    }
  }
  
  private void createEntityInfo( AbstractLayout parent, Entity entity )
  {
    VerticalLayout vl = new VerticalLayout();
    parent.addComponent( vl );
    
    // Header
    HorizontalLayout hl = new HorizontalLayout();
    hl.setWidth( "100%" );
    hl.setStyleName( "eccInfoPanelHeader" );
    vl.addComponent( hl );
    
    Label label = new Label( "Entity: " + entity.getName() );
    label.addStyleName( "h3" );
    hl.addComponent( label );
    
    // Two columns; info and controls
    hl = new HorizontalLayout();
    vl.addComponent( hl );
    
    // Info
    VerticalLayout innerVL = new VerticalLayout();
    innerVL.setWidth( "265px" );
    hl.addComponent( innerVL );
    
    label = new Label( entity.getDescription() );
    label.addStyleName( "small" );
    innerVL.addComponent( label );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "2px", null ) );
    
    label = new Label( "Number of attributes: " + entity.getAttributes().size() );
    label.addStyleName( "small" );
    innerVL.addComponent( label );
    
    // Controls
    innerVL = new VerticalLayout();
    innerVL.setStyleName( "eccInfoSubPanel" );
    hl.addComponent( innerVL );
    
    Button button = new Button( "Add to live view" );
    button.setWidth( "120px" );
    button.addStyleName( "small" );
    button.setData( entity.getUUID() );
    button.addListener( new AddEntityToLiveButtonListener() );
    innerVL.addComponent( button );
    innerVL.setComponentAlignment( button, Alignment.MIDDLE_RIGHT );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "2px", null ) );
    
    button = new Button( "Add to data export" );
    button.setWidth( "120px" );
    button.addStyleName( "small" );
    button.setEnabled( false ); // Not ready yet
    innerVL.addComponent( button );
    innerVL.setComponentAlignment( button, Alignment.MIDDLE_RIGHT );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "20px", null ) );
  }
  
  private void createAttributeInfo( AbstractLayout parent, Attribute attribute )
  {
    VerticalLayout vl = new VerticalLayout();
    parent.addComponent( vl );
    
    // Header
    HorizontalLayout hl = new HorizontalLayout();
    hl.setStyleName( "eccInfoPanelHeader" );
    vl.addComponent( hl );
    
    Label label = new Label( "Attribute: " + attribute.getName() );
    label.addStyleName( "h4" );
    hl.addComponent( label );
    
    // Two columns; info and controls
    hl = new HorizontalLayout();
    vl.addComponent( hl );
    
    // Info
    VerticalLayout innerVL = new VerticalLayout();
    innerVL.setWidth( "260px" );
    hl.addComponent( innerVL );
    
    label = new Label( attribute.getDescription() );
    label.addStyleName( "small" );
    innerVL.addComponent( label );
    
    // Controls
    innerVL = new VerticalLayout();
    innerVL.setStyleName( "eccInfoSubPanel" );
    hl.addComponent( innerVL );
    
    Button button = new Button( "Add to live view" );
    button.setWidth( "120px" );
    button.addStyleName( "small" );
    button.setData( attribute.getUUID() );
    button.addListener( new AddAttributeToLiveButtonListener() );
    innerVL.addComponent( button );
    innerVL.setComponentAlignment( button, Alignment.MIDDLE_RIGHT );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "2px", null ) );
    
    button = new Button( "Add to data export" );
    button.setWidth( "120px" );
    button.addStyleName( "small" );
    button.setEnabled( false ); // Not ready yet
    innerVL.addComponent( button );
    innerVL.setComponentAlignment( button, Alignment.MIDDLE_RIGHT );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "20px", null ) );
  }
  
  // Event handlers ------------------------------------------------------------
  private void onAddEntityToLiveView( UUID entityID )
  {
    if ( entityID != null )
    {
      Collection<ClientInfoViewListener> listeners = getListenersByType();
      for( ClientInfoViewListener listener : listeners )
        listener.onAddEntityToLiveView( entityID );
    }
  }
  
  private void onAddAttributeToLiveView( UUID attributeID )
  {
    if ( attributeID != null )
    {
      Collection<ClientInfoViewListener> listeners = getListenersByType();
      for( ClientInfoViewListener listener : listeners )
        listener.onAddAttributeToLiveView( attributeID );
    }
  }
  
  private class AddEntityToLiveButtonListener implements Button.ClickListener
  {
    @Override
    public void buttonClick(Button.ClickEvent ce) 
    { onAddEntityToLiveView( (UUID) ce.getButton().getData() ); }
  }
  
  private class AddAttributeToLiveButtonListener implements Button.ClickListener
  {
    @Override
    public void buttonClick(Button.ClickEvent ce) 
    { onAddAttributeToLiveView( (UUID) ce.getButton().getData() ); }
  }
}
