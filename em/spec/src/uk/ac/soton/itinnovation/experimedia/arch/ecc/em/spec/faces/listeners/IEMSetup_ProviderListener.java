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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners;

import java.util.UUID;



/**
 * IEMSetup_ProviderListener is a provider client listener that alerts the EM
 * to user clients capable of coordinating their set-up procedure
 * 
 * @author sgc
 */
public interface IEMSetup_ProviderListener
{
    /**
     * Notifies the EM that a user client (identified by the ID) is ready to
     * set up.
     * 
     * @param senderID - ID of the client user ready to set up
     */
    void onNotifyReadyToSetup( UUID senderID );
  
    /**
     * Notification for the EM that a client user has run their set-up process
     * and returned the result of that process.
     * 
     * @param senderID  - ID of the client user setting up
     * @param genID     - ID of the Metric Generator they have set-up
     * @param success   - Result of the set-up process for the MetricGenerator
     */
    void onNotifyMetricGeneratorSetupResult( UUID senderID,
                                             UUID genID,
                                             Boolean success );
}
