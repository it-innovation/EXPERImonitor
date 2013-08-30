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
//      Created By :            Dion Kitchener & Simon Crowle
//      Created Date :          29-Aug-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views.configuration;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.SimpleView;

import java.util.Properties;




public class DashConfigController
{
  private DashConfigView configView;
  
  private Properties emProps;        // em.properties
  private Properties edmProps;       // edm.properties
  private Properties dashboardProps; // dashboard.properties
  
  private boolean configDataOK = false;
  
  //private ECCConfigAPI instanceHere;
  
  
  public DashConfigController()
  {
    initialise();
  }
  
  public SimpleView getConfigView()
  {
    return configView;
  }
  
  public Properties getEMConfig()
  {
    // Client of this class wants the properties originally held in em.properties
    
    return emProps;
  }
  
  public Properties getEDMConfig()
  {
    // Client of this class wants the properties originally held in edm.properties
    return emProps;
  }
  
  public Properties getDashboardConfig()
  {
    // Client of this class wants the properties originally held in dashboard.properties
    return dashboardProps;
  }
  
  // Private methods -----------------------------------------------------------
  private void initialise()
  {
    configView = new DashConfigView();
  }

}
