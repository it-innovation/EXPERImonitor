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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.net.URL;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.SimpleView;




public class NAGIOSView extends SimpleView
{
  private Panel    nagiosContainer;
  private Embedded nagiosEmbedded;
  
  
  public NAGIOSView()
  {
    super();
    
    createComponents();
  }
  
  public void pointToNAGIOS( URL fullURL )
  {
    if ( fullURL != null )
    {
      nagiosEmbedded = new Embedded( "", new ExternalResource(fullURL) );
      nagiosEmbedded.setType( Embedded.TYPE_BROWSER );
      nagiosEmbedded.setWidth( "800px" );
      nagiosEmbedded.setHeight( "600px" );
      
      nagiosContainer.removeAllComponents();
      nagiosContainer.addComponent( nagiosEmbedded );
    }
  }
  
  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();
    
    nagiosContainer = new Panel( "NAGIOS systems view" );
    nagiosContainer.addStyleName( "borderless light" );
    nagiosContainer.setSizeFull();
    nagiosContainer.setImmediate( true );
    vl.addComponent( nagiosContainer );
  }
}
