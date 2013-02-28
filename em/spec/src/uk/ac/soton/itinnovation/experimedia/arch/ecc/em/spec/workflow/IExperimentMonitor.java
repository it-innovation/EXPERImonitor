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
//      Created Date :          13-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;

import java.util.*;





/**
 * IExperimentMonitor is a controlling class that allows client code to direct
 * the progression of the experimental process currently support by the EM. Using
 * this interface, you are able to find out which clients are connected to the
 * EM; control the progression of phases of an experiment; and listen to and request
 * metric data (during compatible phases).
 * 
 * @author sgc
 */
public interface IExperimentMonitor
{
  enum eStatus { NOT_YET_INITIALISED,
                 INITIALISED,
                 ENTRY_POINT_OPEN,
                 LIFECYCLE_STARTED,
                 LIFECYCLE_ENDED };
  
  /**
   * Returns the general status of the monitor
   * 
   * @return - general status of the monitor; see getCurrentPhase() for further
   * information.
   */
  eStatus getStatus();
  
  /**
   * Opens the door to clients wishing to connect to the EM; before this has been
   * executed, the EM will not recognise client's attempts to connect.
   * 
   * @param rabbitServerIP - IP of the RabbitMQ server
   * @param entryPointID   - UUID that will uniquely identify this EM
   * @throws Exception     - throws when parameters are invalid; the EM is already
   * initialised; or there was an AMQP/network related connection problem.
   */
  void openEntryPoint( String rabbitServerIP, UUID entryPointID ) throws Exception;
  
  /**
   * Opens the door to clients wishing to connect to the EM; before this has been
   * executed, the EM will not recognise client's attempts to connect.
   * 
   * @param emProps     - EM configuration properties for connection to the Rabbit Server
   * @throws Exception  - Throws if properties are invalid or there is a connection problem
   */
  void openEntryPoint( Properties emProps ) throws Exception;
  
  /**
   * Explicitly shuts down the monitoring process. Disconnects all clients
   * and tidies up all threads.
   * 
   */
  void shutDown();
  
  /**
   * Use this method to get a (connected) client by UUID - if it exists.
   * 
   * @param id - UUID of the client
   * @return   - EMClient instance (or NULL if it does not exist)
   */
  EMClient getClientByID( UUID id );
  
  /**
   * Use this method to get the set of all known connected clients.
   * 
   * @return - Set of EMClient instances.
   */
  Set<EMClient> getAllConnectedClients();
  
  /**
   * Use this method to find out which clients support the current experimental
   * phase (clients indicate which phases they support during the discovery phase).
   * 
   * @return - Set of EMClient instances for the current phase.
   */
  Set<EMClient> getCurrentPhaseClients();
  
  /**
   * Attempts to de-register client by sending them a de-registration message.
   * Client should then respond with a disconnection message.
   * 
   * @param client     - instance of client to de-register
   * @throws Exception - throws if client is invalid or already in a de-registration
   *                     process.
   */
  void deregisterClient( EMClient client, String reason ) throws Exception;
  
  /**
   * Adds a class the is interested in the life-cycle events specified by the listener.
   * 
   * @param listener - Class instance implementing the listener interface.
   */
  void addLifecyleListener( IEMLifecycleListener listener );
  
  /**
   * Removes the life-cycle listener instance from the notification set.
   * 
   * @param listener 
   */
  void removeLifecycleListener( IEMLifecycleListener listener );
  
  /**
   * Starts the experiment life-cycle - all connected clients are sent a registration
   * message which includes the experiment information supplied here.
   * 
   * @param expInfo - Information describing the experiment.
   * 
   * @return - Returns the first phase of the life-cycle
   * @throws Exception  - throws if the EM has not opened the entry point for clients;
   * there are no clients connected; or the life-cycle has already started.
   */
  EMPhase startLifecycle( Experiment expInfo ) throws Exception;
  
  /**
   * Use this method to find out which experimental phase the EM is currently running.
   * 
   * @return - the current phase of the EM.
   */
  EMPhase getCurrentPhase();
  
  /**
   * Use this method to find out which phase follows the currently running phase.
   * 
   * @return - the next phase to be run by the EM.
   */
  EMPhase getNextPhase();
  
  /**
   * Use this method to find out whether the current phase has been completed by
   * the EM. The result of this method will vary, depending on:
   *   - Whether the phase is user-controlled (such as the live monitoring phase)
   *   - Whether the EM is waiting on a response of a client (which may need to be timed-out)
   * 
   * @return - returns true if the protocol relating to the phase has been completed.
   */
  boolean isCurrentPhaseActive();
  
  /**
   * Use this method to forcefully stop the currently running phase. This process may 
   * sometimes begin with a 'winding down' process during which time the EM communicates 
   * to clients that the current phase is stopping, so to prepare for the commencement of the 
   * next phase.
   * 
   * @throws Exception - Throws if the current phase is already winding down.
   */
  void stopCurrentPhase() throws Exception;
  
  /**
   * Use this method to progress to the next phase in the experiment life-cycle.
   * This may invoke a winding-down phase (depending on the phase type) after which
   * time the EM will iterate to the next phase.
   * 
   * @throws Exception 
   */
  void goToNextPhase() throws Exception;
  
  /**
   * Forcefully ends the experimental life-cycle.
   * 
   * @throws Exception
   */
  void endLifecycle() throws Exception;
  
  /**
   * Forcefully resets the life-cycle process to the beginning where the EM waits for clients
   * to connect. Any currently connected clients will be sent de-registration
   * messages, but the EM will not wait for a response.
   * 
   * @throws Exception - throws if no clients are currently connected.
   */
  void resetLifecycle() throws Exception;
  
  /**
   * Attempts to pull the latest measurement for a specified MeasurementSet ID.
   * 
   * @param client            - Client from which the metric data is to be pulled.
   * @param measurementSetID  - UUID associated with the specific MeasurementSet
   * @throws Exception        - Throws if the parameters are invalid or the EM is
   * not currently running an appropriate phase where pulling is allowed.
   */
  void pullMetric( EMClient client, UUID measurementSetID ) throws Exception;
  
  /**
   * Progressively pulls all metrics available from the client. After this method
   * has been called, users of IExperimentMonitor will need to use 
   * Client.isPullingMetricData() to determine when it is possible to request
   * another pull (clients will not be asked to pull if they are already doing so).
   * 
   * @param client      - Client to pull data from
   * @throws Exception  - Throws if the parameters are invalid or the client is already pulling.
   */
  void pullAllMetrics( EMClient client ) throws Exception;
  
  /**
   * Requests batch data be gathered from the client for the MeasurementSet
   * specified by the ID supplied.
   * 
   * @param client           - Client from which the batched data is required.
   * @param measurementSetID - ID of the MeasurementSet to retrieve data from
   * @throws Exception       - Throws if the parameters are invalid or if the EM is not
   * currently running a phase that is appropriate for requesting data batches.
   */
  void requestDataBatches( EMClient client, UUID measurementSetID ) throws Exception;
  
  /**
   * Use this method to automate the retrieval of all missing data from the 
   * connected client. Once all of the data has been retrieved, the 
   * IEMLifecycleListener.onAllDataBatchesRequestComplete(..) method will be 
   * called by the EM.
   * 
   * @param client      - Client from which the batches should be retrieved
   * @throws Exception  - Throws if the client is either invalid or already busy
   *                      generating data batches
   */
  void getAllDataBatches( EMClient client ) throws Exception;
  
  /**
   * Notifies the client of a time-out for the currently running phase.
   * 
   * @param client      - Client instance to be notified
   * @throws Exception  - Throws if the parameter is invalid; if the client does
   * not support the current phase; or the phase itself does not support time-outs
   */
  void notifyClientOfTimeOut( EMClient client ) throws Exception;
}
