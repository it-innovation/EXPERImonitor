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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMDiscovery_UserListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMDiscovery_ProviderListener;

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
   * If you are acting as a 'provider' of this interface (the EM does this) then
   * use this method to set your listener to incoming methods called by the user.
   * 
   * @param listener - Listener interface for provider actors
   */
  void setProviderListener( IEMDiscovery_ProviderListener listener );
  
  /**
   * If you are acting as a 'user' of this interface (a client of the EM) then
   * use this method to set your listener to incoming methods called by the provider
   * 
   * @param listener - Listener interface for listener actors
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
   * Send confirmation of user registration with the EM.
   * 
   * @param confirmed - Confirmed registration - provider is aware of user (or not)
   */
  void registrationConfirmed( Boolean confirmed );
  
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
  void sendActivePhases( EnumSet<EMPhase> supportedPhases );
  
  /**
   * Send the provider with the result of the user's search for metric generators.
   */
  void sendDiscoveryResult( Boolean discoveredGenerators );
  
  /**
   * Sends the provider a model of all the metric generators the user currently
   * has available.
   */
  void sendMetricGeneratorInfo( Set<MetricGenerator> generators );
  
  /**
   * Notify the EM that the user is disconnecting.
   */
  void clientDisconnecting();
}