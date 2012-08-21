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
//      Created Date :          09-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.*;

import java.util.UUID;




public interface IEMMonitorSetup
{
  // Listeners -----------------------------------------------------------------
  /**
   * If you are acting as a 'provider' of this interface (the EM does this) then
   * use this method to set your listener to incoming methods called by the user.
   * 
   * @param listener - Listener interface for provider actors
   */
  void setProviderListener( IEMSetup_ProviderListener listener );
  
  /**
   * If you are acting as a 'user' of this interface (a client of the EM) then
   * use this method to set your listener to incoming methods called by the provider
   * 
   * @param listener - Listener interface for listener actors
   */
  void setUserListener( IEMSetup_UserListener listener);
  
  // Provider methods ----------------------------------------------------------
  void setupMetricGenerator( UUID genID );
  
  void setupTimeOut( UUID genID );
  
  // User methods --------------------------------------------------------------
  void notifyReadyToSetup();
  
  void notifyMetricGeneratorSetupResult( UUID genID, Boolean success );
}
