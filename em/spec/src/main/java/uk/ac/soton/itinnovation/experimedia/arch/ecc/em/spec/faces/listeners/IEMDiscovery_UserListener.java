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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMInterfaceType;

import java.util.*;




/**
 * This listener provides access to methods executed by the provider (the EM)
 * which are sent to the user. This includes requests to generate interfaces; 
 * confirmation of registration; request for activity phase support etc.
 * 
 * @author sgc
 */
public interface IEMDiscovery_UserListener
{
  /**
   * Request by the provider for the user to create a (user) interface instance
   * of the type EMInterfaceType.
   * 
   * @param senderID - ID of the EM sending this event
   * @param type     - type (EMInterfaceType) of interface required
   */
  void onCreateInterface( UUID senderID, EMInterfaceType type );
  
  /**
   * Provider (EM) notifies the user that they have successfully (or not)
   * registered.
   * 
   * @param senderID       - ID of the EM sending this event
   * @param confirmed      - Confirmed registration - provider is aware of user (or not)
   * @param experimentID   - Unique ID for the experiment being run by the EM
   * @param expName        - Name of the experiment being run by the EM
   * @param expDescription - Short description of the experiment being run by the EM
   * @param createTime     - Creation time of the experiment
   */
  void onRegistrationConfirmed( UUID    senderID, 
                                Boolean confirmed,
                                UUID    expUniqueID,
                                String  expNamedID,
                                String  expName,
                                String  expDescription,
                                Date    createTime );
  
  /**
   * Provider (ECC) has notified the user that they are being de-registered. Clients
   * should reply by using the clientDisconnecting() method on the discovery interface.
   * 
   * @param senderID  - ID of the ECC sending this event
   * @param reason    - Brief description for the reason for de-registering
   */
  void onDeregisteringThisClient( UUID senderID,
                                  String reason );
  
  /**
   * ECC requests a list of supported activities by the user.
   * 
   * @param senderID - ID of the ECC sending this event
   */
  void onRequestActivityPhases( UUID senderID );
  
  /**
   * ECC requests the user discovers any metric generators that are available
   * to generate metric data to send to the ECC.
   * 
   * @param senderID - ID of the ECC sending this event
   */
  void onDiscoverMetricGenerators( UUID senderID );
  
  /**
   * ECC requests the user sends it a model of the generators that it has available
   * to it
   * 
   * @param senderID - ID of the ECC sending this event
   */
  void onRequestMetricGeneratorInfo( UUID senderID );
  
  /**
   * ECC notifies the user that time has run out to discover any more metric
   * generators - the user should stop this process if it is continuing.
   * 
   * @param senderID - ID of the ECC sending this event
   */
  void onDiscoveryTimeOut( UUID senderID );
  
  /**
   * The ECC has sent a status monitoring end-point for the user to (optionally)
   * use to provide active reporting of the systems it is currently using.
   * 
   * @param senderID - ID of the ECC sending this event
   */
  void onSetStatusMonitorEndpoint( UUID senderID,
                                   String endPoint );
}
