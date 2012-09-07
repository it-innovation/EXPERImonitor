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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMTearDown_UserListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMTearDown_ProviderListener;



/**
 * IEMTearDown supports a basic tear-down control and reporting procedure controlled
 * by the EM.
 * 
 * @author sgc
 */
public interface IEMTearDown
{
    // Listeners -----------------------------------------------------------------
    /**
     * Sets the provider listener for this interface
     * 
     * @param listener - Implementation of the 'provider' side listener
     */
    void setProviderListener( IEMTearDown_ProviderListener listener );

    /**
     * Sets the user listener for this interface
     * 
     * @param listener - Implementation of the 'user' side listener
     */
    void setUserListener( IEMTearDown_UserListener listener );

    // Provider methods ----------------------------------------------------------
    /**
     * Requests that the user of this interface start tearing down their monitoring
     * resources.
     */
    void tearDownMetricGenerators();

    /**
     * Tell user that it has run out of time to report on its tear-down process.
     */
    void tearDownTimeOut();

    // User methods --------------------------------------------------------------
    /**
     * Notify the EM that this client is ready to start its tear-down process.
     */
    void notifyReadyToTearDown();

    /**
     * Send the EM the result of this user's tear-down process.
     * 
     * @param success 
     */
    void sendTearDownResult( Boolean success );
}
