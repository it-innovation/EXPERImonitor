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
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.liveData;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.SimpleView;

import com.vaadin.ui.*;
import com.vaadin.terminal.FileResource;

import java.io.File;




public class LiveProvView extends SimpleView
{
  private Embedded embeddedPROVView;

  
  public LiveProvView()
  {
    super();
    
    createComponents();
  }
  
  public void renderPROVVizFile( String path, String targetName )
  {
    if ( path != null && targetName != null && embeddedPROVView != null )
    {
      File fileTarget = new File( path + "/" + targetName + ".dot" );
      
      if ( fileTarget.exists() && fileTarget.isFile() )
      {
        try
        {
          final String pngTarget = path + targetName + ".png";
          final String cmd = "dot -Tpng " + path + targetName + ".dot -o " + pngTarget;
          
          Process rtProc = Runtime.getRuntime().exec( cmd );
          rtProc.waitFor();
          
          fileTarget = new File( pngTarget );
          
          if ( fileTarget.exists() )
          {
            FileResource rs = new FileResource( fileTarget, embeddedPROVView.getApplication() );
            embeddedPROVView.setSource( rs );
          }
        }
        catch( Exception ex )
        {}
      }
    }
  }
  
  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();
    
    Panel panel = new Panel();
    panel.addStyleName( "borderless" );
    panel.setSizeFull();
    vl.addComponent( panel );
    
    embeddedPROVView = new Embedded();
    embeddedPROVView.setType( Embedded.TYPE_IMAGE );
    
    panel.addComponent( embeddedPROVView );
  }
}
