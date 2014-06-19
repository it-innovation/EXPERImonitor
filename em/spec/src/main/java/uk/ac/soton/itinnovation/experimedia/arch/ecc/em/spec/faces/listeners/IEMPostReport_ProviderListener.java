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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import java.util.UUID;



/**
 * IEMPostReport_ProviderListener is a provider client listener for use by the EM
 * implementation to listen to post-reporting user clients.
 * 
 * @author sgc
 */
public interface IEMPostReport_ProviderListener
{
    /**
     * Notifies the EM that a user client is ready to provide a 'post report'.
     * 
     * @param senderID 
     */
    void onNotifyReadyToReport( UUID senderID );
    
    /**
     * Notification to the EM a report summary has been provided by a user client
     * to the EM.
     * 
     * @param senderID  - ID of the user client
     * @param summary   - Summary data for the client. Summary should have at least one report.
     */
    void onSendReportSummary( UUID senderID, EMPostReportSummary summary );
  
    /**
     * Notification to the EM that a data batch has arrived from a user client.
     * 
     * @param senderID          - ID of the user client.
     * @param populatedBatch    - Data batch populated with metric data. Fields must not be null.
     */
    void onSendDataBatch( UUID senderID, EMDataBatch populatedBatch );
}
