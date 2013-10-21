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
import java.util.*;





public class LiveProvView extends SimpleView
{
  private HashSet<UUID> knownPROVObjects;
  private HashSet<UUID> knownTriples;
  
  private Table         provElementView;
  private Table         provDataView;
  private Embedded      embeddedPROVView;

  
  public LiveProvView()
  {
    super();
    
    knownPROVObjects = new HashSet<UUID>();
    knownTriples     = new HashSet<UUID>();
    
    createComponents();
  }
  
  public void echoPROVData( EDMProvReport statement )
  {
    if ( statement != null )
    {
      HashMap<String, EDMProvBaseElement> pEls = statement.getProvElements();
      for ( EDMProvBaseElement el : pEls.values() )
      {
        // Echo new PROV objects
        UUID elInstID = el.getInstanceID();
        if ( !knownPROVObjects.contains(elInstID) )
        {
          switch ( el.getProvType() )
          {
            case ePROV_ENTITY :
              provElementView.addItem( new Object[] { "Entity", el.getIri() }, elInstID ); break;
              
            case ePROV_AGENT :
              provElementView.addItem( new Object[] { "Agent", el.getIri() }, elInstID ); break;
                
            case ePROV_ACTIVITY :
              provElementView.addItem( new Object[] { "Activity", el.getIri() }, elInstID ); break;
          }
          
          knownPROVObjects.add( elInstID );
        }
        
        // Echo new triples
        LinkedList<EDMProvTriple> triples = el.getTriples();
        
        if ( triples != null && !triples.isEmpty() )
          for ( EDMProvTriple triple : triples )
          {
            UUID tripleID = triple.getID();
            
            if ( !knownTriples.contains(tripleID) )
            {
              provDataView.addItem( new Object[]{ triple.getSubject(),
                                                  triple.getPredicate(),
                                                  triple.getObject() },
                                                  tripleID );
              
              knownTriples.add( tripleID );
            }
          }
      }
    }
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
    
    vl = (VerticalLayout) panel.getContent();
    
    HorizontalLayout hl = new HorizontalLayout();
    vl.addComponent( hl );
    
    provElementView = new Table( "PROV Elements" );
    provElementView.addStyleName( "striped" );
    provElementView.setWidth( "300px" );
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
    
    embeddedPROVView = new Embedded();
    embeddedPROVView.setType( Embedded.TYPE_IMAGE );
    
    vl.addComponent( embeddedPROVView );
  }
}
