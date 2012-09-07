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
//      Created Date :          09-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMDataBatch;

import java.util.UUID;



/**
 * IEMPostReport_UserListener is a user client listener that listens to the EM
 * for post-report data generation requests.
 * 
 * @author sgc
 */
public interface IEMPostReport_UserListener
{
    /**
     * Request from the EM to generate a post-report summary
     * 
     * @param senderID - ID of the EM
     */
    void onRequestPostReportSummary( UUID senderID );
  
    /**
     * Request from the EM for the user client to generate metric data based
     * on the specification of the EMDataBatch instance.
     * 
     * @param senderID  - ID of the EM
     * @param reqBatch  - Instance of the data batch required by the EM
     */
    void onRequestDataBatch( UUID senderID, EMDataBatch reqBatch );
  
    /**
     * Notification by the EM that time has run out for the client user to
     * send the batched data requested (identified by the ID) by the EM.
     * 
     * @param senderID  - ID of the EM
     * @param batchID   - ID of the batch data requested
     */
    void notifyReportBatchTimeOut( UUID senderID, UUID batchID );
}
