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
//      Created By :            Dion Kitchener
//      Created Date :          29-Aug-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.configuration;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.*;

import com.vaadin.ui.*;




public class DashConfigView extends SimpleView
{
  
  public DashConfigView()
  {
    super( true );
    
    createComponents();
  }
  
  
  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();
    
    Panel panel = new Panel();
    panel.setWidth( "400px" );
    panel.setHeight( "500px" );
    vl.addComponent( panel );
    vl.setComponentAlignment( panel, Alignment.MIDDLE_CENTER );
    
    VerticalLayout innerVL = new VerticalLayout();
    panel.addComponent( innerVL );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "40", null ) );
    
    // Title
    HorizontalLayout lhl = new HorizontalLayout();
    innerVL.addComponent( lhl );
    innerVL.setComponentAlignment( lhl, Alignment.MIDDLE_CENTER );
    
    Label label = new Label( "Dashboard configuration" );
    label.addStyleName( "h1 color" );
    lhl.addComponent( label );
  }
}
