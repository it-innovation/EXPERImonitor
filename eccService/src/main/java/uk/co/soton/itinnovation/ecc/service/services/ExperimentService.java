/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2014
//
// Copyright in this library belongs to the University of Southampton
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
//	Created By :			Maxim Bashevoy
//	Created Date :			2014-04-02
//	Created for Project :           EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.co.soton.itinnovation.ecc.service.services;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;
import uk.co.soton.itinnovation.ecc.service.domain.EccConfiguration;

/**
 * ExperimentService provides executive control over the ECC and experiment work-flow.
 */
@Service
public class ExperimentService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private boolean eccInitialised;
    

    public ExperimentService() {
    }
    
    /**
     * Use this method to initialise the experiment service. Initialisation requires the service 
     * connect to the RabbitMQ and PostgreSQL databases specified in the configuration
     * file. This method will throw if initialisation fails or the service is
     * already initialised.
     * 
     * @param eccConfig     - Non null ECC configuration instance.
     * @throws Exception    - throws in input parameters are invalid or ECC is already initialised
     */
    @PostConstruct
    public void init(EccConfiguration eccConfig) throws Exception {
        logger.debug("Initialising experiment service");
        
        if ( eccInitialised ) throw new Exception( "Could not initialise ECC: ECC is already initialised" );

        logger.debug("Finished initialising experiment service");
    }
    
    /**
     * Use this to determine if ECC is initialised.
     * 
     * @return - return true if ECC is initialised;
     */
    public boolean isServiceInitialised() {
        return eccInitialised;
    }
    
    /**
     * Use this method to shutdown the experiment service. This method will 
     * throw if the service has not been properly initialised.
     * 
     * @throws Exception - throws if service not initialised.
     */
    public void shutdown() throws Exception {
        
    }
    
    /**
     * Use this method to try to start a new experiment. If the input parameters are
     * null or there is already an active experiment, this method will throw. Under normal 
     * conditions, this method create a new experiment in the database and invite ECC 
     * clients already known to the service to join the new experiment.
     * 
     * @param projectName    - Name of the project associated with the experiment
     * @param experimentName - Name of this specific experiment
     * @param expDesc        - Short description of the experiment
     * @return               - Returns meta-data about the experiment.
     * @throws Exception     - Throws if parameters are invalid or there is an active experiment
     */
    public Experiment startExperiment( String projectName, String experimentName, String expDesc ) throws Exception {
        Experiment newExp = null;
        
        return newExp;
    }
    
    /**
     * Use this method to try to stop an experiment. The service will attempt to
     * save the finish time of the experiment to the database before then issuing 'stop'
     * messages to attached clients, where appropriate.
     * 
     * @throws Exception - throws if it was not possible to finalise the experiment or there
     *                     is not an active experiment running.
     */
    public void stopExperiment() throws Exception {
        
    }
    
    /**
     * Returns experiment meta-data if there is an experiment currently active.
     * 
     * @return - Returns NULL if no experiment is currently active.
     */
    public Experiment getActiveExperiment() {
        Experiment exp = null;
        
        // TO DO
        
        return exp;
    }
    
    /**
     * Returns the current phase of the active experiment. If there is no active
     * experiment, EMPhase.eEMUnknownPhase is returned.
     * 
     * @return 
     */
    public EMPhase getActiveExperimentPhase() {
        
        // TO DO
        
        return EMPhase.eEMUnknownPhase;
    }
    
    /**
     * Use this method to attempt to advance the current phase of the active experiment.
     * 
     * @return              - Returns the next phase
     * @throws Exception    - throws if there is no active experiment or there are no more phases to move on to.
     */
    public EMPhase advanceExperimentPhase() throws Exception {
        
        return EMPhase.eEMUnknownPhase;
    }
    
    /**
     * Use this method to retrieve the currently known connected clients. This call 
     * will return an empty set when there are no clients or no active experiment.
     * IMPORTANT: the state of each client you find in this set will be correct only at 
     * the point of calling.
     * 
     * @return - Set of clients currently connected.
     */
    public Set<EMClient> getCurrentlyConnectedClients() {
        HashSet<EMClient> connectedClients = new HashSet<EMClient>();
        
        
        return connectedClients;        
    }
    
    /**
     * Use this method to get an instance of a client specified by an ID.
     * @param id - UUID of the client required.
     * @return   - Client and its state at the point of calling
     */
    public EMClient getClientByID( UUID id ) {
        EMClient client = null;
        
        
        return client;
    }
    
    /**
     * Use this call to send a 'deregister' message to a connected client. This instruction
     * informs the client that they should send a disconnection message to the ECC and then
     * disconnect from the Rabbit service. 
     * 
     * @param client     - Client to send the deregister message.
     * @throws Exception - throws if the client is not known or is already disconnected from the ECC 
     */
    public void deregisterClient( EMClient client ) throws Exception {
        
    }

}
