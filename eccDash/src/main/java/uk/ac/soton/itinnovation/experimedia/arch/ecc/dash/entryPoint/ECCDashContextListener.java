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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.entryPoint;

import javax.servlet.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.DashMainController;




public class ECCDashContextListener implements ServletContextListener
{
  private static DashMainController mainController;
  
  
  public ECCDashContextListener()
  {
  }
  
  public static void setDMC( DashMainController dmc )
  { mainController = dmc; }
  
  public static DashMainController getDMC()
  { return mainController; }
  
  @Override
  public void contextInitialized( ServletContextEvent sce )
  { }
  
  @Override
  public void contextDestroyed( ServletContextEvent sce )
  { 
    if ( mainController != null )
    {
      mainController.shutdown();
      mainController = null;
    } 
  }
}
