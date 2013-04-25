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
import org.vaadin.artur.icepush.ICEPush;
import org.vaadin.artur.icepush.JavascriptProvider;

import java.io.BufferedWriter;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.icepush.servlet.MainServlet;



/**
 * ECCDashServlet is a modification of Artur Signell's ICEPush Vaadin ApplicationServlet.
 * This class has been modified to support ECC dashboard development. See ICEPush V0.2.1 
 * Add-on: https://vaadin.com/directory#addon/icepush:vaadin.
 * 
 * @author Simon Crowle
 */
public class ECCDashServlet extends ApplicationServlet
{
  private MainServlet ICEPushServlet;
  private JavascriptProvider javascriptProvider;

  @Override
  public void init(ServletConfig servletConfig) throws ServletException
  {
    try { super.init( servletConfig ); }
    catch ( ServletException e )
    {
      if ( e.getMessage().equals( "Application not specified in servlet parameters") ) 
      {
        // Ignore if application is not specified to allow the same
        // servlet to be used for only push in portals
      } 
      else { throw e; }
    }
    
    ICEPushServlet = new MainServlet(servletConfig.getServletContext());
    
    try
    {
      javascriptProvider = new JavascriptProvider(getServletContext() .getContextPath());
      
      ICEPush.setCodeJavascriptLocation( javascriptProvider.getCodeLocation() );
    } 
    catch (IOException e)
    { throw new ServletException("Error initializing JavascriptProvider", e); }
  }

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

  @Override
  protected void service( HttpServletRequest request,
                          HttpServletResponse response ) 
                          throws ServletException, IOException
  {
    String pathInfo = request.getPathInfo();
    
    if ( pathInfo != null && pathInfo.equals("/" + javascriptProvider.getCodeName()) )
    {
      // Serve icepush.js
      serveIcePushCode(request, response);
      return;
    }
    
    if ( request.getRequestURI().endsWith(".icepush") )
    {
      // Push request
      try { ICEPushServlet.service(request, response); } 
      
      catch ( ServletException e ) 
      { throw e; } 
      
      catch ( IOException e )
      { throw e; } 
      
      catch (Exception e)
      { throw new RuntimeException(e); }
    } 
    else
    {
      // Vaadin request
      super.service(request, response);
    }
  }

  private void serveIcePushCode( HttpServletRequest request,
                                 HttpServletResponse response ) throws IOException
  {
    String icepushJavscript = javascriptProvider.getJavaScript();
    
    response.setHeader("Content-Type", "text/javascript");
    response.getOutputStream().write(icepushJavscript.getBytes());
  }

  @Override
  public void destroy()
  {
    super.destroy();
    ICEPushServlet.shutdown();
  }
}