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
//      Created By :            sgc
//      Created Date :          05-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec;

import java.util.*;




/**
 * Use this listener to listen to in-coming events targeted at a provider instance
 * (EM) of the IECCMonitor interface. This listener notifies of initialisation,
 * activity, discovery and disconnection events.
 * 
 * @author sgc
 */
public interface IECCMonitor_ProviderListener
{
  /**
   * Notification of a user who is now ready to initialise.
   */
  void onReadyToInitialise( UUID senderID );
  
  /**
   * Notification of a user's declaration of which activity phases it supports.
   * 
   * @param supportedPhases - a list of enumerated phases supported by the user.
   */
  void onSendActivityPhases( UUID senderID, 
                             List<IECCMonitor.EMSupportedPhase> supportedPhases );
  
  /**
   * Notification that the user has finished their discovery process and is
   * reporting on which metric generators they currently have available.
   */
  void onSendDiscoveryResult( UUID senderID
                              /* Data model under development*/ );
  
  /**
   * Notification that the user is disconnecting from the EM.
   * 
   */
  void onClientDisconnecting( UUID senderID );
}
