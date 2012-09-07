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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;

import java.util.UUID;



/**
 * IEMLiveMonitor_ProviderListener is a provider client listener that EM implementations
 * use to listen for client users sending metric data during the Live Monitoring phase.
 * 
 * @author sgc
 */
public interface IEMLiveMonitor_ProviderListener
{
    /**
     * Notification by the user client that it is ready to starting pushing metric data.
     * 
     * @param senderID - ID of the user client
     */
    void onNotifyReadyToPush( UUID senderID );

    /**
     * Notification of a metric push report by the user client.
     * 
     * @param senderID  - ID of the user client
     * @param report    - Report sent by the client
     */
    void onPushMetric( UUID senderID, Report report );

    /**
     * Notification by the user client that it has stopped pushing metric data to
     * the EM.
     * 
     * @param senderID - ID of the user client.
     */
    void onNotifyPushingCompleted( UUID senderID );

    /**
     * Notification by the user client that it is ready to receive pull requests
     * from the EM.
     * 
     * @param senderID 
     */
    void onNotifyReadyForPull( UUID senderID );

    /**
     * Notification of a pulled metric sent from the user client to the EM.
     * 
     * @param senderID  - ID of the user client.
     * @param report    - Report sent as a result of a pull request from the EM.
     */
    void onSendPulledMetric( UUID senderID, Report report );
}
