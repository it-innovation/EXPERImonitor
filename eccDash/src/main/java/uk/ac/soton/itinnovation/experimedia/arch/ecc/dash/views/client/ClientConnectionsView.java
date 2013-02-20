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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views;

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
import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.types.UFAbstractEventManager;




public class ClientConnectionsView extends SimpleView
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
        clientList.addComponent( (Component) view.getImplContainer() );
        
        connectedClients.put( id, view );
        
        // High-light first added
        if ( currSelectedClient == null ) onClientSelected( id );
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
  
  public void updateClientsInPhase( EMPhase phase )
  {
    Iterator<ClientView> cvIt = connectedClients.values().iterator();
    while ( cvIt.hasNext() )
    {
      ClientView view = cvIt.next();
      if ( !view.isClientAccelerating() ) view.setCurrentPhase( phase ); 
    }
  }

  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();
    
    Panel panel = new Panel();
    panel.addStyleName( "borderless light" );
    panel.setCaption( "Connected clients" );
    panel.setSizeFull();
    vl.addComponent( panel );
    
    clientList = new VerticalLayout();
    clientList.setWidth( "450px" );
    clientList.setImmediate( true );
    panel.addComponent( clientList );
  }
  
  private void onClientSelected( UUID targetID )
  {
    if ( targetID != null )
    {
      boolean notify = false;
      
      // If no currently selected client, make this one the selected
      if ( currSelectedClient == null )
      {
        ClientView view = connectedClients.get( targetID );
        if ( view != null )
        {
          currSelectedClient = targetID; 
          notify = true;
        }
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
              listener.onClientSelected( currSelectedClient );
        }
      }
    }
  }
  
  // Private classes -----------------------------------------------------------
  private class ClientView extends HighlightView
  {    
    private transient EMClient client;
    
    private Label clientName;
    private Label currentPhase;
    
    
    public ClientView( EMClient emc )
    {
      super();
      
      client = emc;
      createComponents();
    }
    
    public boolean isClientAccelerating()
    { return client.isPhaseAccelerating(); }
    
    public void setCurrentPhase( EMPhase phase )
    { currentPhase.setValue( "Phase: " + phase.toString() ); }
    
    @Override
    public UUID getDataID()
    { return client.getID(); }
    
    // Private methods ---------------------------------------------------------
    private void createComponents()
    {
      VerticalLayout vl = getViewContents();
      
      clientName = new Label( client.getName() );
      vl.addComponent( clientName );
      
      // Space
      vl.addComponent( UILayoutUtil.createSpace( "2px", null ) );
      
      currentPhase = new Label( "Connected" );
      currentPhase.setImmediate( true );
      vl.addComponent( currentPhase );
      
      addListener( new ClientViewSelected() );
    }
  }
  
  // Private event handling ----------------------------------------------------
  private class ClientViewSelected extends UFAbstractEventManager
                                   implements HighlightViewListener
  {
    @Override
    public void onHighlightViewSelected( HighlightView view )
    { onClientSelected( view.getDataID() ); }
  }
}
