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

using uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor;

using System;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces
{

public interface IEMPostReport
{
    // Listeners -----------------------------------------------------------------    
    /**
     * Sets the user listener for this interface
     * 
     * @param listener - Implementation of the 'user' side listener
     */
    void setUserListener( IEMPostReport_UserListener listener );

    // User methods --------------------------------------------------------------
    /**
     * Notifies the EM that this user is ready to report.
     */
    void notifyReadyToReport();

    /**
     * Sends a post report summary instance to the EM.
     * 
     * @param summary - summary instance of the report. Fields must not be null.
     */
    void sendReportSummary( EMPostReportSummary summary );

    /**
     * Sends a data batch (based on the specification provided by the EM from the
     * requestDataBatch(..) method.
     * 
     * @param populatedBatch - data batch with metric data. Fields must not be null.
     */
    void sendDataBatch( EMDataBatch populatedBatch );
}

} // namespace
