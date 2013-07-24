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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.client;

import com.vaadin.ui.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.HighlightView;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.HighlightViewListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.SimpleView;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UILayoutUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UIResource;




public class ClientConnectionsView extends SimpleView
                                   implements HighlightViewListener,
                                              ClientViewListener
{
  private VerticalLayout clientList;
  
  private transient HashMap<UUID, ClientView> connectedClients;
  private transient UUID                      currSelectedClient;
  
  
  public ClientConnectionsView()
  {
    super();
    
    connectedClients = new HashMap<UUID, ClientView>();
    
    createComponents();
  }
  
  public void resetView()
  {
    connectedClients.clear();
    currSelectedClient = null;
    clientList.removeAllComponents();
  }
  
  public void addClient( EMClient client )
  {
    if ( client != null )
    {
      UUID id = client.getID();
      
      if ( !connectedClients.containsKey(id) )
      {
        ClientView view = new ClientView( client );
        view.addListener( this );
        clientList.addComponent( (Component) view.getImplContainer() );
        
        connectedClients.put( id, view );
        
        // Select first added
        if ( currSelectedClient == null ) currSelectedClient = client.getID();
      }
    }
  }
  
  public void removeClient( EMClient client )
  {    
    if ( client != null )
    {
      UUID id = client.getID();
      
      if ( connectedClients.containsKey(id) )
      {
        ClientView view = connectedClients.get( id );
        clientList.removeComponent( (Component) view.getImplContainer() );
        
        connectedClients.remove( id );
        
        // Unselect current as well, if removed
        if ( currSelectedClient != null && currSelectedClient.equals(id) ) 
          currSelectedClient = null;
      }
    }
  }
  
  public UUID getSelectedClientID()
  { return currSelectedClient; }
  
  public void updateClientsInPhase( EMPhase phase )
  {
    Iterator<ClientView> cvIt = connectedClients.values().iterator();
    while ( cvIt.hasNext() )
    {
      ClientView view = cvIt.next();
      if ( !view.isClientAccelerating() ) view.setCurrentPhase( phase ); 
    }
  }
  
  public void updateClientPhase( UUID clientID, EMPhase phase )
  {
    if ( clientID != null && phase != null )
    {
      ClientView cv = connectedClients.get( clientID );
      
      if ( cv != null ) cv.setCurrentPhase( phase );
    }
  }
  
  public void updateClientSummaryInfo( UUID clientID,
                                       int entityCount,
                                       int metricCount )
  {
    if ( clientID != null )
    {
      ClientView cv = connectedClients.get( clientID );
      
      if ( cv != null ) 
      {
          cv.setSummaryInfo( entityCount, metricCount );
          if(entityCount>0)
          {
              cv.statusIcon.setVisible(true);
              cv.newEntitiesLabel.setVisible(true);
              
          }
          
      }
           
    }
  }
  
  // HighlightViewListener -----------------------------------------------------
  @Override
  public void onHighlightViewSelected( HighlightView hv )
  {
    ClientView view = (ClientView) hv;
    
    if ( view != null )
    {
      UUID targetID = view.getDataID();
      
      if ( targetID != null )
      {
        boolean notify;

        // If no currently selected client, make this one the selected
        if ( currSelectedClient == null )
        {
          currSelectedClient = targetID;
          notify = true;
        }
        else // Otherwise swap
        {
          ClientView oldView = connectedClients.get( currSelectedClient );
          if ( oldView != null ) oldView.setSelected( false );

          currSelectedClient = targetID;

          notify = true;
        }

        // Notify listeners if required
        if ( notify )
        {
          ClientView selView = connectedClients.get( currSelectedClient );
          if ( selView != null )
          {
            selView.setSelected( true );
            currSelectedClient = targetID;

            Collection<ClientConnectionsViewListener> listeners = getListenersByType();
              for( ClientConnectionsViewListener listener : listeners )
                listener.onViewClientSelected( currSelectedClient );
          }
        }
      }
    }
  }
  
  // ClientViewListener --------------------------------------------------------
  @Override
  public void onClientDisconnect( UUID clientID, boolean force )
  {
    if ( clientID != null )
    {
      Collection<ClientConnectionsViewListener> listeners = getListenersByType();
        for( ClientConnectionsViewListener listener : listeners )
          listener.onViewClientDisconnect( clientID, force );
    }
  }
  
  @Override
  public void onEntityAdded(UUID clientID)
  {
     if(clientID!=null)
     {
         ClientView cv = connectedClients.get(clientID);
         if ( cv !=null )
         {
             cv.newEntitiesLabel.setVisible(false);
             cv.statusIcon.setVisible(false);
         }
     }
  }
  
  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();
    
    Panel panel = new Panel();
    panel.addStyleName( "borderless light" );
    panel.setCaption( "Connected clients" );
    panel.setWidth( "250px" );
    VerticalLayout vpl = (VerticalLayout) panel.getContent(); // Reduce internal padding here
    vpl.setMargin( false );
    vl.addComponent( panel );
    
    clientList = new VerticalLayout();
    clientList.setImmediate( true );
    clientList.setSizeUndefined();
    panel.addComponent( clientList );
  }
  
  // Private classes -----------------------------------------------------------
  private class ClientView extends HighlightView
  {
    private Label  clientName;
    private Label  currentPhase;
    private Label  entityCountLabel;
    private Label  metricCountLabel;
    private Label newEntitiesLabel;
    private Button disconnectButton;
    private Embedded statusIcon;
    
    
    

            
    private transient EMClient client;
    private transient boolean forceDisconnect = false;
    
    
    public ClientView( EMClient emc )
    {
      super();
      
      client = emc;
      createComponents();
    }
    
    public boolean isClientAccelerating()
    { return client.isPhaseAccelerating(); }
    
    // Set current phase label
    public void setCurrentPhase( EMPhase phase )
    { currentPhase.setValue( "Phase: " + phase.toString() ); }
    
    // Set entity and metric ount labels
    public void setSummaryInfo( int entityCount, int metricCount )
    {
      entityCountLabel.setValue( "Entities: " + entityCount );
      metricCountLabel.setValue( "Metrics: " + metricCount );
      
    }
    
    // HighlightView overrides -------------------------------------------------
    @Override
    public UUID getDataID()
    { return client.getID(); }
    
    @Override
    public void setSelected( boolean selected )
    {
      super.setSelected( selected );
       if(!selected)
        {
           //statusIcon.setVisible(false);
          // newEntitiesLabel.setVisible(false);
      }
   }
    
    // Private methods ---------------------------------------------------------
    private void createComponents()
    {
      VerticalLayout vl = getViewContents();
      vl.setWidth( "250px" );
      
      // Space
      vl.addComponent( UILayoutUtil.createSpace( "4px", null ) );
      
      // Indent
      HorizontalLayout hl = new HorizontalLayout();
      vl.addComponent( hl );
      hl.addComponent( UILayoutUtil.createSpace( "4px", null, true ) );
      VerticalLayout innerVL = new VerticalLayout();
      innerVL.setWidth( "240px" );
      hl.addComponent( innerVL );
      
     
       // New entity warning icon
      statusIcon = new Embedded(null, UIResource.getResource( "alertIcon") );
      statusIcon.setWidth("20px");
      statusIcon.setHeight("20px");
      innerVL.addComponent(statusIcon);
      innerVL.setComponentAlignment(statusIcon, Alignment.TOP_RIGHT);
      statusIcon.setVisible(false);
      
      
      // Client name label
      clientName = new Label( client.getName() );
      clientName.addStyleName( "small" );
      innerVL.addComponent( clientName );
           
      // Space
      innerVL.addComponent( UILayoutUtil.createSpace( "2px", null ) );
      
      // Waiting for entity label
      currentPhase = new Label( "Waiting" );
      currentPhase.addStyleName( "tiny" );
      currentPhase.setImmediate( true );
      innerVL.addComponent( currentPhase );
      
      // Space
      innerVL.addComponent( UILayoutUtil.createSpace( "2px", null ) );
      
      // Summary
      entityCountLabel = new Label( "Entities: unknown" );
      entityCountLabel.addStyleName( "tiny" );
      entityCountLabel.setImmediate( true );
      innerVL.addComponent( entityCountLabel );
      
      // Space
      innerVL.addComponent( UILayoutUtil.createSpace( "2px", null ) );
      
      // Metric count label
      metricCountLabel = new Label( "Metrics: unknown" );
      metricCountLabel.addStyleName( "tiny" );
      metricCountLabel.setImmediate( true );
      innerVL.addComponent( metricCountLabel );
      
      // Label to inform of new entities
      newEntitiesLabel = new Label ( "New Entities Available" );
      newEntitiesLabel.addStyleName( "tiny" );
      // To do: change font colour to red
      innerVL.addComponent(newEntitiesLabel);
      newEntitiesLabel.setVisible(false);
      
      // Space
      vl.addComponent( UILayoutUtil.createSpace( "10px", null ) );
      
      // Force disconnect button
      disconnectButton = new Button( "Disconnect" );
      disconnectButton.addStyleName( "small" );
      disconnectButton.setData( client.getID() );
      disconnectButton.addListener( new DisconnectListener() );
      innerVL.addComponent( disconnectButton );
      innerVL.setComponentAlignment( disconnectButton, Alignment.BOTTOM_RIGHT );
      
  
      
    }
    
    // Private event handling --------------------------------------------------
    private void onDisconnectClient( UUID clientID )
    {
      if ( clientID != null && clientID.equals( client.getID()) )
      {
        // Try a gentle disconnect action first
        Collection<ClientViewListener> listeners = getListenersByType();
          for( ClientViewListener listener : listeners )
            listener.onClientDisconnect( clientID, forceDisconnect );
            
        // Then force for next time
        disconnectButton.setCaption( "force disconnect");
        forceDisconnect = true;
      }
    }
    
    private class DisconnectListener implements Button.ClickListener
    {
      @Override
      public void buttonClick(Button.ClickEvent ce)
      { onDisconnectClient( (UUID) ce.getButton().getData() ); }
    }
  }
}
