/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2011
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
//      Created By :            sgc
//      Created Date :          25 Aug 2011
//      Created for Project :   ROBUST
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.mvc;

public interface IUFView
{  
  /**
   * Gets the implementing container class for the view
   * 
   * @return - the concrete class that containers the view components, perhaps
   *           an AWT container
   */
	Object getImplContainer();
	
  /**
   * Returns the visibility of the view
   * 
   * @return - true if visible
   */
  boolean isVisible();
  
  /**
   * Visibility of the view
   * 
   * @param visible - determines visibility of the view
   */
  void setVisible( boolean visible );
  
  /**
   * Force the view to update itself
   * 
   */
  void updateView();
  
  void displayMessage( String title, String content );
  
  void displayWarning( String title, String content );
}
