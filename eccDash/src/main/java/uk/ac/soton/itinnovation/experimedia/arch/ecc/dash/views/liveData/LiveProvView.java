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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.*;

import com.vaadin.ui.*;
import com.vaadin.terminal.FileResource;

import java.io.File;


public class LiveProvView extends SimpleView
{
  private Panel    provViewPanel;
  private Embedded embeddedPROVView;
  
  private Table    provElementView;
  private Table    provDataView;
 

  public LiveProvView()
  {
    super();

    createComponents();
  }
  
  public void resetView()
  {
    provElementView.removeAllItems();
    provDataView.removeAllItems();
    
    provViewPanel.removeAllComponents();
    embeddedPROVView = createEmbeddedView();
    provViewPanel.addComponent( embeddedPROVView );
  }

  public void echoPROVData( EDMProvReport statement )
  {
    if ( statement != null )
    {
      provElementView.removeAllItems();

      //no need to iterate over elements because only new triples have been sent
      for (EDMTriple triple: statement.getTriples().values()) {
	      provDataView.addItem( new Object[]{ triple.getSubject(),
	              triple.getPredicate(),
	              triple.getObject() },
	              triple.getID() );
      }
    }
  }

  public void renderPROVVizFile( String basePath, String targetName )
  {
    if ( basePath != null && targetName != null && embeddedPROVView != null )
    {
      File fileTarget = new File( basePath + "/" + targetName + ".dot" );

      if ( fileTarget.exists() && fileTarget.isFile() )
      {
        try
        {
          final String pngTarget = basePath + "/" + targetName + ".svg";
          final String cmd = "dot -Tsvg " + basePath + "/" + targetName + ".dot -o " + pngTarget;

          Process rtProc = Runtime.getRuntime().exec( cmd );
          rtProc.waitFor();

          fileTarget = new File( pngTarget );

          if ( fileTarget.exists() )
          {
            FileResource rs = new FileResource( fileTarget, embeddedPROVView.getApplication() );
            embeddedPROVView.setMimeType( "image/svg+xml" );
            embeddedPROVView.setSource( rs );
          }
        }
        catch( Exception ex )
        { displayWarning( "Problems rendering PROV", ex.getMessage() ); }
      }
    }
  }

  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();

    // Space
    vl.addComponent( UILayoutUtil.createSpace( "2px", null) );

    provViewPanel = new Panel();
    provViewPanel.addStyleName( "light" );
    provViewPanel.setScrollable( true );
    vl.addComponent( provViewPanel );
    
    embeddedPROVView = createEmbeddedView();
    provViewPanel.addComponent( embeddedPROVView );

    HorizontalLayout hl = new HorizontalLayout();
    vl.addComponent( hl );

    provElementView = new Table( "PROV Elements" );
    provElementView.addStyleName( "striped" );
    provElementView.setWidth( "400px" );
    provElementView.setHeight( "120px" );
    provElementView.addContainerProperty( "PROV Element", String.class, null );
    provElementView.addContainerProperty( "PROV ID", String.class, null );
    hl.addComponent( provElementView );

    hl.addComponent( UILayoutUtil.createSpace( "4px", null, true) );

    provDataView = new Table( "PROV Triple statements" );
    provDataView.addStyleName( "striped" );
    provDataView.setWidth( "600px" );
    provDataView.setHeight( "120px" );
    provDataView.addContainerProperty( "Subject", String.class, null );
    provDataView.addContainerProperty( "Predicate", String.class, null );
    provDataView.addContainerProperty( "Object", String.class, null );
    hl.addComponent( provDataView );
  }
  
  private Embedded createEmbeddedView()
  {
    Embedded embedded = new Embedded();
    embedded.setType( Embedded.TYPE_OBJECT ); // Do not set mime type until write time
    embedded.setWidth( "100%" );
    embedded.setHeight( "400px" );
    
    return embedded;
  }
}
