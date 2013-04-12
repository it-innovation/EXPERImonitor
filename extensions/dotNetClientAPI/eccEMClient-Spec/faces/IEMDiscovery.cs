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
using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics;

using System;
using System.Collections.Generic;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces
{

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
         * Sets the user listener for this interface
         * 
         * @param listener - Implementation of the 'user' side listener
         */
        void setUserListener(IEMDiscovery_UserListener listener);

        // User methods --------------------------------------------------------------
        /**
        * Notify the provider that the user is ready to initialise (i.e., is ready
        * to specify which activity phases it supports; find metric providers etc)
        */
        void readyToInitialise();

        /**
        * Send the provider with all the supported EM based monitoring phases supported
        * by the user (see EMSupportedPhase enumeration).
        * 
        * @param supportedPhases - a list of the monitoring phases supported.
        */
        void sendActivePhases(HashSet<EMPhase> supportedPhases);

        /**
        * Send the provider with the result of the user's search for metric generators.
        */
        void sendDiscoveryResult(bool discoveredGenerators);

        /**
        * Sends the provider a model of all the metric generators the user currently
        * has available.
        */
        void sendMetricGeneratorInfo(HashSet<MetricGenerator> generators);

        /**
        * Notify the EM that the user is disconnecting.
        */
        void clientDisconnecting();
    }

} // namespace
