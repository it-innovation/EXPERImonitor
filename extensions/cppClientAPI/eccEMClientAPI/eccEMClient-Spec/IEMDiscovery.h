/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
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
//      Created Date :          20-June-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#pragma once

#include "IEMDiscovery_UserListener.h"

#include "MetricGenerator.h"
#include "EMPhase.h"




namespace ecc_emClient_spec
{

    /**
     * The IECCMonitor is a 'foundation' interface from which the monitoring process
     * supported by the EM begins.
     * 
     * @author sgc
     */
    class IEMDiscovery
    {
    public:

        typedef boost::shared_ptr<IEMDiscovery> ptr_t;


        // Listeners -----------------------------------------------------------------
        /**
         * Sets the user listener for this interface
         * 
         * @param listener - Implementation of the 'user' side listener
         */
        virtual void setUserListener( IEMDiscovery_UserListener::ptr_t listener ) =0;

        // User methods --------------------------------------------------------------
        /**
        * Notify the provider that the user is ready to initialise (i.e., is ready
        * to specify which activity phases it supports; find metric providers etc)
        */
        virtual void readyToInitialise() =0;

        /**
        * Send the provider with all the supported EM based monitoring phases supported
        * by the user (see EMSupportedPhase enumeration).
        * 
        * @param supportedPhases - a list of the monitoring phases supported.
        */
        virtual void sendActivePhases( const ecc_commonDataModel::EMPhaseSet& supportedPhases ) =0;

        /**
        * Send the provider with the result of the user's search for metric generators.
        */
        virtual void sendDiscoveryResult( const bool discoveredGenerators ) =0;

        /**
        * Sends the provider a model of all the metric generators the user currently
        * has available.
        */
        virtual void sendMetricGeneratorInfo( const ecc_commonDataModel::MetricGenerator::Set& generators ) =0;

        /**
        * Notify the EM that the user is disconnecting.
        */
        virtual void clientDisconnecting() =0;
    };

} // namespace
