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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;

import java.util.*;




/**
 * The IECCMonitor is a 'foundation' interface from which the monitoring process
 * supported by the EM begins.
 * 
 * @author sgc
 */
public interface IEMDiscovery
{  
    // Listeners -----------------------------------------------------------------
    /**
     * Sets the provider listener for this interface
     * 
     * @param listener - Implementation of the 'provider' side listener
     */
    void setProviderListener( IEMDiscovery_ProviderListener listener );

    /**
     * Sets the user listener for this interface
     * 
     * @param listener - Implementation of the 'user' side listener
     */
    void setUserListener( IEMDiscovery_UserListener listener);

    // Provider methods ----------------------------------------------------------
    /**
    * Requests the user of the IECCMonitor interface to create another interface
    * supported by the EM.
    * 
    * @param type - Type of interface supported by the EM.
    */
    void createInterface( EMInterfaceType type );

    /**
    * Send confirmation of user registration with the EM (includes experiment details)
    * 
    * @param confirmed      - Confirmed registration - provider is aware of user (or not)
    * @param expUniqueID    - Unique ID for the experiment being run by the EM
    * @param expNamedID     - More meaningful identity of the experiment being run by the EM
    * @param expName        - Name of the experiment being run by the EM
    * @param expDescription - Short description of the experiment being run by the EM
    * @param createTime     - Creation time of the experiment
    */
    void registrationConfirmed( Boolean confirmed,
                                UUID    expUniqueID,
                                String  expNamedID,
                                String  expName,
                                String  expDescription,
                                Date    createTime );
    
    /**
     * Notifies the client that they are being de-registered from the EM for the
     * reason specified. Clients should reply using the 'clientDisconnecting()'
     * method and then terminate their AMQP connection.
     * 
     * @param reason - A short description of why the client has been de-registered.
     */
    void deregisteringThisClient( String reason );

    /**
    * Request a list of supported activity phases from the user that it supports.
    * This includes a metric enumeration phase, set-up metrics phase, (live)
    * metric monitoring phase and post-reporting phase.
    */
    void requestActivityPhases();

    /**
    * Request the user discovers all metric generators currently available for it
    * to use to generate metrics. 
    */
    void discoverMetricGenerators();

    /**
    * Request the user sends information about all metric generators it has discovered
    */
    void requestMetricGeneratorInfo();

    /**
    * Notify the user that it has taken too long to discover its metric providers 
    * and that it should stop.
    */
    void discoveryTimeOut();

    /**
    * Notify the user of a status monitor end-point (if one exists) that will
    * allow them to send general status information about the user's technology
    * to a dashboard view.
    */
    void setStatusMonitorEndpoint( String endPoint );
    
    /**
     * Confirms with the client that the ECC has enabled/disabled metric data 
     * collection for a specific entity
     * 
     * @param entityID - ID of the Entity that was enabled
     * @param enabled  - Enabled/disabled status
     */
    void notifyEntityMetricCollectionEnabled( UUID entityID, boolean enabled );

    // User methods ------------------------------------------------------------
    /**
    * Notify the ECC that the user is ready to initialise (i.e., is ready
    * to specify which activity phases it supports; find metric providers etc)
    */
    void readyToInitialise();

    /**
    * Send the ECC with all the supported EM based monitoring phases supported
    * by the user (see EMSupportedPhase enumeration).
    * 
    * @param supportedPhases - a list of the monitoring phases supported.
    */
    void sendActivePhases( EnumSet<EMPhase> supportedPhases );

    /**
    * Send the ECC with the result of the user's search for metric generators.
    */
    void sendDiscoveryResult( Boolean discoveredGenerators );

    /**
    * Sends the ECC a model of all the metric generators the user currently
    * has available.
    */
    void sendMetricGeneratorInfo( Set<MetricGenerator> generators );
    
    /**
     * Tells the ECC to enable or disable metric collection, from this client,
     * for the Entity specified.
     * 
     * @param entityID  - ID of the entity the client has declared to the ECC
     * @param enabled   - Enable or disable metric collection
     */
    void enableEntityMetricCollection( UUID entityID, boolean enabled );

    /**
    * Notify the ECC that the user is disconnecting.
    */
    void clientDisconnecting();
}
