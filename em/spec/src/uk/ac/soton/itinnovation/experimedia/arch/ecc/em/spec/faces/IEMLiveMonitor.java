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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;

import java.util.UUID;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.PROVStatement;





public interface IEMLiveMonitor
{
    // Listeners -----------------------------------------------------------------
    /**
     * Sets the provider listener for this interface
     * 
     * @param listener - Implementation of the 'provider' side listener
     */
    void setProviderListener( IEMLiveMonitor_ProviderListener listener );

    /**
     * Sets the user listener for this interface
     * 
     * @param listener - Implementation of the 'user' side listener
     */
    void setUserListener( IEMLiveMonitor_UserListener listener );

    // Provider methods ----------------------------------------------------------
    /**
     * Tell the user to start pushing metric data to the EM.
     */
    void startPushing();

    /**
     * Tell the user that a push report has been received by the EM.
     * 
     * @param lastPushID - UUID of the last pushed report/provenance statement received by the EM from
     * this client.
     */
    void notifyPushReceived( UUID lastPushID );

    /**
     * Tells the user to stop pushing data to the EM.
     */
    void stopPushing();

    /**
     * Tells the client to generate a Report with data for the supplied 
     * MeasurementSet ID
     * 
     * @param measurementSetID - ID of the measurement set data is required for.
     */
    void pullMetric( UUID measurementSetID );

    /**
     * Tells the user that time has run out to generate data for the MeasurementSet
     * specified.
     * 
     * @param measurementSetID - ID of the measurement set data is required for.
     */
    void pullMetricTimeOut( UUID measurementSetID );

    /**
     * Tells the user that no more pulling requests will be issued.
     */
    void pullingStopped();
    
    /**
     * Tell the user that a pull report has been received by the EM.
     * 
     * @param lastReportID - UUID of the last pull report received by the EM from
     * this client.
     */
    void notifyPullReceived( UUID lastReportID );

    // User methods --------------------------------------------------------------
    /**
     * Notifies the EM that this user is ready to start pushing metric data.
     */
    void notifyReadyToPush();

    /**
     * Sends a Report instance to the EM. IMPORTANT NOTE: users should pair this
     * method call with the IEMLiveMonitor_UserListener.onReceivedPush(..) event
     * issued to the user by the ECC.
     *
     * @param report - Instance of a Report containing measurement data (for now,
     * for a single time frame).
     */
    void pushMetric( Report report );
    
    /**
     * Sends a PROVenance based statement to the EM. IMPORTANT NOTE: users should
     * pair this method call with the IEMLiveMonitor_UserListener.onReceivedPush(..)
     * event issued to the user by the ECC.
     * 
     * @param statement - Instance of the PROVenance statement containing provenance
     * data objects.
     */
    void pushPROVStatement( PROVStatement statement );

    /**
     * Tells the EM that this user has finished pushing Reports.
     * 
     */
    void notifyPushingCompleted();

    /**
     * Notifies the EM that this user is ready to respond to pull requests from
     * the EM. 
     */
    void notifyReadyForPull();

    /**
     * Sends a pulled Report (in response to a request from the EM) to the EM.
     * 
     * @param report  - Instance of a Report containing measurement data (for now,
     * for a single time frame).
     */
    void sendPulledMetric( Report report );
}
