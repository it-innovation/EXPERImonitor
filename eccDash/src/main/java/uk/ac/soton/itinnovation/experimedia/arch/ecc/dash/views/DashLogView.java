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

import com.vaadin.ui.Panel;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.SimpleView;

import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import java.util.Iterator;
import java.util.LinkedList;



public class DashLogView extends SimpleView
{
  private TextArea logArea;
  
  private transient LinkedList<String> logText;
  private final int logLimit = 100;
  
  
  public DashLogView()
  {
    super();
    
    logText = new LinkedList<String>();
    
    createComponents();
  }
  
  public void addLogMessage( String text )
  {
    if ( text != null )
    {
      logText.add( text );
      if ( logText.size() > logLimit ) logText.removeFirst();
    }
    
    String reverseLog = "";
    Iterator<String> logIt = logText.descendingIterator();
    while ( logIt.hasNext() )
    { reverseLog += logIt.next() + "\n"; }
    
    logArea.setValue( reverseLog );
  }
  
  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();
    
    Panel panel = new Panel( "ECC log data" );
    panel.addStyleName( "borderless light" );
    panel.setSizeFull();
    vl.addComponent( panel );
    
    logArea = new TextArea();
    logArea.addStyleName( "small" );
    logArea.setSizeFull();
    logArea.setHeight( "400px" );
    logArea.setImmediate( true );
    panel.addComponent( logArea );
  }
}
