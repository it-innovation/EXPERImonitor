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
//      Created Date :          09-Apr-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using System;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners
{

/**
 * IEMLiveMonitor_UserListener is a user client listener that responds to metric
 * monitoring activity carried out in the Live Monitoring phase.
 * 
 * @author sgc
 */
public interface IEMLiveMonitor_UserListener
{
    /**
     * Notification from the EM to the client to start pushing data (in a controlled
     * fashion, see also 'onReceivedPush(..)'.
     * 
     * @param senderID - ID of the EM
     */
    void onStartPushing(Guid senderID);

    /**
     * Notification from the EM that the pushed report identified by the report
     * ID has been received by the EM. The user client should take this as a signal
     * for it to push another report if it wishes to do so.
     * 
     * @param senderID      - ID of the EM
     * @param lastReportID  - ID of the last push report received by the EM
     */
    void onReceivedPush(Guid senderID, Guid lastReportID);

    /**
     * Notification by the EM that the client user should stop pushing metric data.
     * 
     * @param senderID - ID of the EM
     */
    void onStopPushing(Guid senderID);

    /**
     * A request by the EM that the user client should send the latest metric data
     * for the MeasurementSet ID specified.
     * 
     * @param senderID          - ID of the EM
     * @param measurementSetID  - ID of the MeasurementSet for which the latest metric data should be sent.
     */
    void onPullMetric(Guid senderID, Guid measurementSetID);

    /**
     * Notification by the EM that time has run out for the user client to send 'pulled' metric data
     * for the MeasurementSet specified by the ID.
     * 
     * @param senderID          - ID of the EM
     * @param measurementSetID  - ID of the MeasurementSet for which the pull was requested.
     */
    void onPullMetricTimeOut(Guid senderID, Guid measurementSetID);

    /**
     * Notification by the EM that pulling has stopped.
     * 
     * @param senderID 
     */
    void onPullingStopped(Guid senderID);
    
    /**
     * Notification from the EM that the pulled report identified by the report
     * ID has been received by the EM.
     * 
     * @param senderID      - ID of the EM
     * @param lastReportID  - ID of the last pull report received by the EM
     */
    void onReceivedPull(Guid senderID, Guid lastReportID);
}

} // namespace