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
//      Created Date :          07-Feb-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.entryPoint;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.ApplicationServlet;
import com.vaadin.ui.Window;
import java.io.BufferedWriter;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;




public class ECCDashServlet extends ApplicationServlet
{
  @Override
  protected void writeAjaxPageHtmlVaadinScripts( Window window,
                                                 String themeName, 
                                                 Application application, 
                                                 BufferedWriter page,
                                                 String appUrl, 
                                                 String themeUri, 
                                                 String appId,
                                                 HttpServletRequest request ) 
                                                 throws ServletException, 
                                                        IOException 
  {
    page.write( "<script type=\"text/javascript\">\n" );
    page.write( "//<![CDATA[\n" );
    
    String contextPath = request.getContextPath();
    
    // Embed scripts
    page.write( "document.write(\"<script language='javascript' src='" + contextPath + "/VAADIN/scripts/jquery/jquery-1.4.4.min.js'><\\/script>\");\n" );
    page.write( "document.write(\"<script language='javascript' src='" + contextPath + "/VAADIN/scripts/js/highcharts.js'><\\/script>\");\n" );
    
    // Embed additional styles
    page.write( "document.write(\"<link rel='stylesheet' type='text/css' href='" + contextPath + "/VAADIN/themes/eccDash/staticStyles.css'>\");\n" );
    
    page.write( "//]]>\n</script>\n" );
        
    super.writeAjaxPageHtmlVaadinScripts( window, themeName, application,
                                          page, appUrl, themeUri, appId,
                                          request );
  }
}
