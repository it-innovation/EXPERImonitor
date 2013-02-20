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
  private Label          clientMainInfo;
  private VerticalLayout metaDataHolder;
  
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
  
  public void writeClientDisconnected( EMClient client )
  {
    if ( client != null && client.getID().equals(currClientID) )
    {
      String content = "<h1>" + client.getName() + " has been disconnected</h1>";
      clientMainInfo.setValue( content );
      metaDataHolder.removeAllComponents();
    }
  }
  
  public void resetView()
  {
    String content = "<div style=\"\">";
    
    content += "<h1>No client information available</h1>";
    
    content += "</div>";
    
    clientMainInfo.setValue( content );
  }
  
  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();
    
    Panel panel = new Panel();
    panel.setSizeFull();
    panel.addStyleName( "borderless light" );
    vl.addComponent( panel );
    VerticalLayout pVL = (VerticalLayout) panel.getContent();
    pVL.addStyleName( "eccInfoPanel" );
    
    clientMainInfo = new Label();
    clientMainInfo.setSizeFull();
    clientMainInfo.setImmediate( true );
    clientMainInfo.setContentMode( Label.CONTENT_XHTML );
    clientMainInfo.setStyleName( "eccInfoPanel" );
    pVL.addComponent( clientMainInfo );
    
    metaDataHolder = new VerticalLayout();
    metaDataHolder.setSizeFull();
    metaDataHolder.setImmediate( true );
    metaDataHolder.setStyleName( "eccInfoPanel" );
    pVL.addComponent( metaDataHolder );
    
    resetView();
  }
  
  private void writeMainInfo( EMClient client, Set<MetricGenerator> mGens )
  {
    String content = "<h1>" + client.getName() + "</h1>";
    
    if ( mGens.isEmpty() ) content += "<h2> Currently no metric meta-data </h2>";
    
    content += "</div>";
    
    clientMainInfo.setValue( content );
  }
  
  private void writeMetricMetaData( Set<MetricGenerator> mGens )
  {
    metaDataHolder.removeAllComponents();
    
    if ( !mGens.isEmpty() )
    {
      Map<UUID, Entity> knownEntities = MetricHelper.getAllEntities( mGens );
      Iterator<Entity> entIt = knownEntities.values().iterator();
      while ( entIt.hasNext() )
      {
        // Write out entity
        Entity entity = entIt.next();
        
        Label entityLabel = new Label();
        entityLabel.setSizeFull();
        entityLabel.setContentMode( Label.CONTENT_XHTML );
        
        String content = "<div style=\"text-indent:20px;\">";
        
        String val = entity.getName();
        if ( val == null ) val = "No name provided";
        content += "<h2>Entity: " + val + "</h2>";
        
        val = entity.getDescription();
        if ( val == null ) val = "No description provided";
        content += "<h3>Description: " + val+ "</h3>";
        
        content += "</div>";
        entityLabel.setValue( content );
        metaDataHolder.addComponent( entityLabel );
        
        // Write out attributes
        Iterator<Attribute> attIt = entity.getAttributes().iterator();
        while ( attIt.hasNext() )
        {
          Attribute att = attIt.next();
          
          HorizontalLayout hl = new HorizontalLayout();
          metaDataHolder.addComponent( hl );
          
          // Attribute info
          Label attLabel = new Label();
          attLabel.setWidth( "90%" );
          attLabel.setContentMode( Label.CONTENT_XHTML );
          
          String attContent = "<div style=\"eccInfoSubPanel\">";
          attContent       +=  "<div style=\"text-indent:30px;\">";
          
          val = att.getName();
          if ( val == null ) val = "No name provided";
          attContent += "<h2>Attribute: " + val + "</h2>";
          
          val = att.getDescription();
          if ( val == null ) val = "No description provided";
          attContent += "<h3>Description: " + val + "</h2>";
          
          attContent += "</div></div>";
          attLabel.setValue( attContent );
          hl.addComponent( attLabel );
          
          // Space
          hl.addComponent( UILayoutUtil.createSpace( "5px", null, true ) );
          
          // Add to metric view button
          Button addButton = new Button( "Add to live view" );
          addButton.addStyleName( "small" );
          addButton.setData( att.getUUID() );
          addButton.addListener( new AddButtonListener() );
          hl.addComponent( addButton );
          hl.setComponentAlignment( addButton, Alignment.BOTTOM_LEFT );
          
          // Attribute HTML content (TODO)
          VerticalLayout vl = new VerticalLayout();
          hl.addComponent(vl);
          
        }
      }
    }
  }
  
  // Event handlers ------------------------------------------------------------
  private void onAddToLiveViewClicked( UUID attributeID )
  {
    if ( attributeID != null )
    {
      Collection<ClientInfoViewListener> listeners = getListenersByType();
      for( ClientInfoViewListener listener : listeners )
        listener.onAddAttributeToLiveView( attributeID );
    }
  }
  
  private class AddButtonListener implements Button.ClickListener
  {
    @Override
    public void buttonClick(Button.ClickEvent ce) 
    { onAddToLiveViewClicked( (UUID) ce.getButton().getData() ); }
  }
}
