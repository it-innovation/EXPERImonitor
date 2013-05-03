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
//      Created Date :          02-May-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents;

import com.vaadin.terminal.ThemeResource;

import java.util.HashMap;




public class UIResource
{
  private static UIResource appResource;
  
  private HashMap<String, ThemeResource> resources;
  
  public UIResource()
  {
    resources = new HashMap<String, ThemeResource>();
    
    if ( appResource == null ) appResource = this;    
  }
  
  public static ThemeResource getResource( String ID )
  { 
    ThemeResource resource = null;
    
    if ( appResource != null )
      resource = appResource.getThemeResource( ID );
    
    return resource;
  }
  
  public void cleanUp()
  {
    resources.clear();
    appResource = null;
  }
  
  public void createResource( String ID, String resPath )
  {
    resources.remove( ID );
    resources.put( ID, new ThemeResource(resPath) );
  }
  
  public void removeResource( String ID )
  { resources.remove( ID ); }

  // Protected methods -----------------------------------------------------------
  protected ThemeResource getThemeResource( String ID )
  { return resources.get( ID ); }

}
