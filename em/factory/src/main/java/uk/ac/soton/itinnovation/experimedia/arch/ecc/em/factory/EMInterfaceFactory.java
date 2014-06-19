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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.IExperimentMonitor;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.ExperimentMonitor;

import java.util.UUID;



/**
 * EMInterfaceFactory is a simple factory class that generates EM 
 * client/producer interfaces.
 * 
 * @author sgc
 */
public class EMInterfaceFactory
{
    private AMQPBasicChannel amqpChannel;
    private boolean          generateProviders;
    
    /**
     * Construction of the factory requires a properly constructed AMQPBasicChannel
     * (see the AMQPConnectionFactory) and a flag as to whether the factory will
     * create 'user' or 'provider' interfaces. Only 'createProviders' if you are
     * implementing an EM yourself, otherwise you should act as a user.
     * 
     * @param channel         - A properly configured AMQP channel
     * @param createProviders - Set a 'false' to act as a user
     */
    public EMInterfaceFactory( AMQPBasicChannel channel, boolean createProviders )
    {
      amqpChannel       = channel;
      generateProviders = createProviders;
    }
    
    /**
     * Creates a message dispatch pump that controls AMQP message subscriptions
     * 
     * @param name      - Name of the pump (creates an associated Thread of the same name)
     * @param priority  - The processing resource to be allocated to the pump
     * @return          - Returns an instance of the pump
     */
    public IAMQPMessageDispatchPump createDispatchPump( String name,
                                                        IAMQPMessageDispatchPump.ePumpPriority priority )
    {
        return new AMQPMessageDispatchPump( name, priority );
    }
  
    /**
     * Creates a dispatch that is a) added to a pump for message processing and
     * b) assigned to an AMQP interface to listen for specific message types.
     * (See the interface creation methods)
     * 
     * @return - Returns an instance of a dispatch
     */
    public IAMQPMessageDispatch createDispatch()
    {
        return new AMQPMessageDispatch();
    }
    
    /**
     * Creates an 'Entry-point' interface to the EM. Users should use this interface
     * to initialise a connection to the EM.
     * 
     * @param providerID    - UUID of the EM with which to connect. Must be pre-determined.
     * @param dispatch      - Dispatch used to process the messages of this interface.
     * @return              - Instance of this interface.
     */  
    public IEMMonitorEntryPoint createEntryPoint( UUID providerID,
                                                  IAMQPMessageDispatch dispatch )
    {
        return new EMMonitorEntryPoint( amqpChannel,
                                        (AMQPMessageDispatch) dispatch,
                                        providerID, 
                                        generateProviders );
    }
  
    /**
     * Creates an 'Discovery' interface connection with the EM
     * 
     * Users must use this interface to describe which phases of the monitoring
     * process they support and what MetricGenerators they are able to provide.
     * 
     * @param providerID    - UUID of the EM with which to connect. Must be pre-determined.
     * @param dispatch      - Dispatch used to process the messages of this interface.
     * @return              - Instance of this interface.
     */
    public IEMDiscovery createDiscovery( UUID providerID,
                                         UUID userID,
                                         IAMQPMessageDispatch dispatch )
    {
        return new EMDiscovery( amqpChannel,
                                (AMQPMessageDispatch) dispatch,
                                providerID,
                                userID,
                                generateProviders );
    }
    
    /**
     * Creates an 'Setup' interface connection with the EM
     * 
     * Users can use this interface to coordinate specific setting up processes
     * of their MetricGenerators with the EM.
     * 
     * @param providerID    - UUID of the EM with which to connect. Must be pre-determined.
     * @param dispatch      - Dispatch used to process the messages of this interface.
     * @return              - Instance of this interface.
     */
    public IEMMetricGenSetup createSetup( UUID providerID,
                                            UUID userID,
                                            IAMQPMessageDispatch dispatch )
    {
        return new EMMetricGenSetup( amqpChannel,
                                     (AMQPMessageDispatch) dispatch,
                                     providerID,
                                     userID,
                                     generateProviders );
    }
    
    /**
     * Creates an 'Live Monitor' interface connection with the EM
     * 
     * Users can use this interface to send (through pushing or pulling) live 
     * metric data to the EM.
     * 
     * @param providerID    - UUID of the EM with which to connect. Must be pre-determined.
     * @param dispatch      - Dispatch used to process the messages of this interface.
     * @return              - Instance of this interface.
     */
    public IEMLiveMonitor createLiveMonitor( UUID providerID,
                                             UUID userID,
                                             IAMQPMessageDispatch dispatch )
    {
        return new EMLiveMonitor( amqpChannel,
                                  (AMQPMessageDispatch) dispatch,
                                  providerID,
                                  userID,
                                  generateProviders );
    }
  
    /**
     * Creates an 'Post Report' interface connection with the EM
     * 
     * Users can use this interface to send metric data that could not be sent
     * during the live monitoring process in non-real-time batched form.
     * 
     * @param providerID    - UUID of the EM with which to connect. Must be pre-determined.
     * @param dispatch      - Dispatch used to process the messages of this interface.
     * @return              - Instance of this interface.
     */
    public IEMPostReport createPostReport( UUID providerID,
                                           UUID userID,
                                           IAMQPMessageDispatch dispatch )
    {
        return new EMPostReport( amqpChannel,
                                 (AMQPMessageDispatch) dispatch,
                                 providerID,
                                 userID,
                                 generateProviders );
    }
    
    /**
     * Creates an 'Tear-down' interface connection with the EM
     * 
     * Users can use this interface coordinate and report on any specific
     * tear-down processes associated with the monitoring process.
     * 
     * @param providerID    - UUID of the EM with which to connect. Must be pre-determined.
     * @param dispatch      - Dispatch used to process the messages of this interface.
     * @return              - Instance of this interface.
     */  
    public IEMTearDown createTearDown( UUID providerID,
                                       UUID userID,
                                       IAMQPMessageDispatch dispatch )
    {
        return new EMTearDown( amqpChannel,
                               (AMQPMessageDispatch) dispatch,
                               providerID,
                               userID,
                               generateProviders );
    }
  
    public IEMTest createTest( UUID providerID,
                               UUID userID,
                               IAMQPMessageDispatch dispatch )
    {
        return new EMTest( amqpChannel,
                           (AMQPMessageDispatch) dispatch,
                           providerID, 
                           userID, 
                           generateProviders );
    }
  
    /**
     * Only clients (typically not 'users') wishing to encapsulate an Experiment 
     * Monitor component should use this method to create an instance of the EM.
     * 
     * @return - Returns an instance of the experiment monitor.
     */
    public static IExperimentMonitor createEM()
    { return new ExperimentMonitor(); }
}
