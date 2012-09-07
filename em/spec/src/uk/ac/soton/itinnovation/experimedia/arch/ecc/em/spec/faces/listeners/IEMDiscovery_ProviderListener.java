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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;

import java.util.*;




/**
 * Use this listener to listen to in-coming events targeted at a provider instance
 * (EM) of the IECCMonitor interface. This listener notifies of initialisation,
 * activity, discovery and disconnection events.
 * 
 * @author sgc
 */
public interface IEMDiscovery_ProviderListener
{
    /**
    * Notification of a user who is now ready to initialise.
    */
    void onReadyToInitialise( UUID senderID );

    /**
    * Notification of a user's declaration of which activity phases it supports.
    * 
    * @param senderID        - ID of the client user
    * @param supportedPhases - A list of enumerated phases supported by the user.
    */
    void onSendActivityPhases( UUID senderID, 
                               EnumSet<EMPhase> supportedPhases );

    /**
    * Notification that the user has finished their discovery process and is
    * reporting on which metric generators they currently have available.
    * 
    * @param senderID             - ID of the client user
    * @param discoveredGenerators - Result of the discovery process by the user client
    */
    void onSendDiscoveryResult( UUID senderID,
                                Boolean discoveredGenerators );

    /**
    * Notification from the user of the MetricGenerators is has available to
    * send metric data during the Live Monitoring process.
    * 
    * @param senderID   - ID of the client user
    * @param generators - Non-empty set of MetricGenerators.
    */
    void onSendMetricGeneratorInfo( UUID senderID,
                                    Set<MetricGenerator> generators );

    /**
    * Notification that the user is disconnecting from the EM.
    * 
    * @param senderID - ID of the user client
    * 
    */
    void onClientDisconnecting( UUID senderID );
}
