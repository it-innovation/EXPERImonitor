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
//      Created Date :          05-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.listeners.*;

import java.util.UUID;




/**
 * IECCMonitorEntryPoint is a 'half interface' that allows users wishing to
 * register with the provider of the interface to do so. The provider of 
 * IECCMonitorEntryPoint can listen for connecting users using the
 * IECCMonitorEntryPoint_ProviderListener interface
 * 
 * @author sgc
 */
public interface IEMMonitorEntryPoint
{
  /**
   * Listen to the messages sent to the provider (EM) part of this interface.
   * 
   * @param listener - IECCMonitorEntryPoint_ProviderListener listener
   */
  void setListener( IECCMonitorEntryPoint_ProviderListener listener );
  
  // Provider methods ----------------------------------------------------------
  /**
   * As a user of the interface, use this method to register yourself with the
   * provider (the EM).
   * 
   * @param userID    -- UUID that uniquely identifies the user connecting
   * @param userName  -- Informal label identifying the connecting user
   */
  void registerAsEMClient( UUID userID, String userName );
}
