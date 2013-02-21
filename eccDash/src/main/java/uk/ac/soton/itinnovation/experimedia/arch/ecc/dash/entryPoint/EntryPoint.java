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
//      Created Date :          01-Feb-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.entryPoint;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.ui.Window;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.DashMainController;




@SuppressWarnings("serial")
public class EntryPoint extends Application
                        implements HttpServletRequestListener
{
  private DashMainController dmc;


  @Override
  public void init()
  {
    if ( dmc == null )
    {
      dmc = new DashMainController();
      ECCDashContextListener.setDMC( dmc );

      setTheme( "eccDash" );

      Window window = new Window("ECC Dashboard");
      setMainWindow( window );

      dmc.initialise( window );
    }
  }
  
  @Override
  public void close()
  {
    if ( dmc != null )
    {
      dmc.shutdown();
      dmc = null;
      ECCDashContextListener.setDMC( null );
    }
    
    super.close();
  }
  
  // HttpServletRequestListener ------------------------------------------------
  @Override
  public void onRequestStart( HttpServletRequest request, 
                              HttpServletResponse response )
  {
    
  }
  
  @Override
  public void onRequestEnd( HttpServletRequest request,
                            HttpServletResponse response)
  {
  }
}
