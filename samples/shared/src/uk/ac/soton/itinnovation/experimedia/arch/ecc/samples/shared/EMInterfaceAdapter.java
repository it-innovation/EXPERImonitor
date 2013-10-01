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
//      Created Date :          19-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import java.util.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.PROVStatement;




/**
 * Client writers can use this class as a high-level wrapper for communications with
 * the ECC. The class using an instance of EMInterfaceAdapter should implement the EMIAdapterListener 
 * interface so that it can respond to requests from the ECC. Writers wishing only to respond to V0.9 
 * behaviours should instead provide a EMILegacyAdapterListener implementation.
 * 
 * @author Simon Crowle
 */
public class EMInterfaceAdapter implements IEMDiscovery_UserListener,
                                           IEMSetup_UserListener,
                                           IEMLiveMonitor_UserListener,
                                           IEMPostReport_UserListener,
                                           IEMTearDown_UserListener
{
    protected EMIAdapterListener       emiListener;
    protected EMILegacyAdapterListener emiLegListener;
    
    protected String             clientName;
    protected AMQPBasicChannel   amqpChannel;
    protected UUID               expMonitorID;
    protected UUID               clientID;
    protected Experiment         currentExperiment;

    protected EMInterfaceFactory       interfaceFactory;
    protected IAMQPMessageDispatchPump dispatchPump;

    // EM Interfaces
    protected IEMMonitorEntryPoint entryPointFace;
    protected IEMDiscovery         discoveryFace;
    protected IEMMetricGenSetup    setupFace;
    protected IEMLiveMonitor       liveMonitorFace;
    protected IEMPostReport        postReportFace;
    protected IEMTearDown          tearDownFace;
    
    protected boolean clientRegistered = false;

    // Supported phases & metric Generators
    protected EnumSet<EMPhase>     supportedPhases;

    
    /**
     * Constructor for the EMInterfaceAdapter using the most recent listener to ECC events.
     * 
     * @param listener - ECC event listener (V1.0)
     */
    public EMInterfaceAdapter( EMIAdapterListener listener )
    {
        emiListener     = listener; 
        supportedPhases = EnumSet.noneOf( EMPhase.class );
    }
    
    /**
     * Constructor for the EMInterfaceAdapter using an older listener to ECC events (V0.9).
     * 
     * @param listener - ECC event listener (V0.9)
     */
    public EMInterfaceAdapter ( EMILegacyAdapterListener legListener )
    {
        emiLegListener  = legListener;
        supportedPhases = EnumSet.noneOf( EMPhase.class );
    }

    /**
     * Use this method to try registering with the ECC.
     * 
     * @param name       - Name of the client attempting to register.
     * @param channel    - AMQP channel used to connect to the ECC.
     * @param emID       - The UUID of the ECC's monitoring system with which to connect
     * @param ourID      - The UUID of this client (as a unique instance)
     * @throws Exception - Registration will throw if any of the parameters are NULL.
     */
    public void registerWithEM( String name,
                                AMQPBasicChannel channel, 
                                UUID emID,
                                UUID ourID ) throws Exception
    {
        // Safety first
        if ( name == null ) throw new Exception( "Client name is null" );
        if ( channel == null ) throw new Exception( "AMQP Channel is null" );
        if ( emID == null ) throw new Exception( "Experiment Monitor ID is null" );
        if ( ourID == null ) throw new Exception( "Our client ID is null" );

        clientRegistered = false;
        amqpChannel      = channel;
        clientName       = name;
        expMonitorID     = emID;
        clientID         = ourID;

        // Create interface factory to support interfaces required by the EM
        interfaceFactory = new EMInterfaceFactory( amqpChannel, false );

        // Create dispatch pump (only need to do this once)
        dispatchPump = interfaceFactory.createDispatchPump( "EM Client pump", 
                                                            IAMQPMessageDispatchPump.ePumpPriority.MINIMUM );
        dispatchPump.startPump();

        // Create a dispatch (for entry point interface) and add to the pump
        IAMQPMessageDispatch dispatch = interfaceFactory.createDispatch();
        dispatchPump.addDispatch( dispatch );

        // Create our entry point interface
        entryPointFace = interfaceFactory.createEntryPoint( expMonitorID, dispatch );

        // Create the principal interface (IEMDiscovery ahead of time)
        dispatch = interfaceFactory.createDispatch();
        dispatchPump.addDispatch( dispatch );
        discoveryFace = interfaceFactory.createDiscovery( expMonitorID, 
                                                          clientID, 
                                                          dispatch );

        discoveryFace.setUserListener( this );

        //.. and finally, try registering with the EM!
        entryPointFace.registerAsEMClient( clientID, clientName );
    }
    
    /**
     * Use this method to disconnect from the ECC's monitoring system.
     * 
     * @throws Exception - Disconnection will throw if the client is unable to send a disconnection message.
     */
    public void disconnectFromEM() throws Exception
    {
        boolean sentDisconnectMessage = false;
        
        if ( discoveryFace != null )
        {
            discoveryFace.clientDisconnecting();
            sentDisconnectMessage = true;
        }
     
        // Tidy up anyway
        dispatchPump.stopPump();
        
        entryPointFace  = null;
        discoveryFace   = null;
        setupFace       = null;
        liveMonitorFace = null;
        postReportFace  = null;
        tearDownFace    = null;
        
        amqpChannel      = null;
        clientRegistered = false;
        
        if ( !sentDisconnectMessage )
          throw new Exception( "Could not communicate disconnection with EM/ECC" );
    }

    /**
     * Use this method to get information about the current Experiment being run by
     * the ECC. 
     * 
     * @return - Will return populated Experiment information after registration with the ECC
     *           has been confirmed.
     */
    public Experiment getExperimentInfo()
    { return currentExperiment; }
    
    /**
     * Use this method to set all MetricGenerators known to the client. The EMInterfaceAdapter 
     * will then send these on the ECC.
     * 
     * @param generators 
     */
    public void sendMetricGenerators( Set<MetricGenerator> generators )
    {
        if ( generators != null && discoveryFace != null )
            discoveryFace.sendMetricGeneratorInfo( generators );
    }

    /**
     * Actively push a metric report to the ECC. This method should only be called during 
     * the Live Monitoring process.
     * 
     * @param report - A fully populated instance of a metric report.
     */
    public void pushMetric( Report report )
    {
        if ( report != null && liveMonitorFace != null )
            liveMonitorFace.pushMetric( report );
    }
    
    /**
     * Push a provenance statement to the ECC. This method should only be called during
     * the Live Monitoring process.
     * 
     * @param statement 
     */
    public void pushPROVStatement( PROVStatement statement )
    {
      if ( statement != null && liveMonitorFace != null )
        liveMonitorFace.pushPROVStatement( statement );
    }

    /**
     * Sends a request to the ECC for it to enable or disable metric capture
     * for a specific entity. IMPORTANT NOTE: disabling an entity will mean that
     * the ECC no longer PULLs metrics for that entity or stores PUSHed metrics
     * for that entity.
     * 
     * @param entityID - UUID of the entity to enable or disable.
     * @param enabled  - Enabled or disable.
     */
    public void sendEntityEnabled( UUID entityID, boolean enabled )
    {
      if ( entityID != null && discoveryFace != null )
        discoveryFace.enableEntityMetricCollection( entityID, enabled );
    }
    
    // IEMDiscovery_UserListener -----------------------------------------------
    @Override
    public void onCreateInterface( UUID senderID, EMInterfaceType type )
    {
        if ( senderID != null && senderID.equals(expMonitorID) )
        {      
            switch (type)
            {
                case eEMSetup :
                {
                    if ( setupFace == null )
                    {
                        IAMQPMessageDispatch dispatch = interfaceFactory.createDispatch();
                        dispatchPump.addDispatch( dispatch );

                        setupFace = interfaceFactory.createSetup( expMonitorID, 
                                                                  clientID, 
                                                                  dispatch );

                        setupFace.setUserListener( this );
                        setupFace.notifyReadyToSetup();
                    }

                } break;

                case eEMLiveMonitor :
                {
                    if ( liveMonitorFace == null )
                    {
                        IAMQPMessageDispatch dispatch = interfaceFactory.createDispatch();
                        dispatchPump.addDispatch( dispatch );

                        liveMonitorFace = interfaceFactory.createLiveMonitor( expMonitorID, 
                                                                              clientID, 
                                                                              dispatch );

                        liveMonitorFace.setUserListener( this );

                        // Find out whether we can push or pull, or both
                        Boolean[] pushPull = new Boolean[2];
                        
                        if ( emiListener != null )
                        {
                            emiListener.onDescribePushPullBehaviours( pushPull );
                        
                            // Notify EM of behaviours
                            if ( pushPull[0] ) liveMonitorFace.notifyReadyToPush();
                            if ( pushPull[1] ) liveMonitorFace.notifyReadyForPull();
                            
                            // Tell listener that the Live Monitoring phase has begun
                            emiListener.onLiveMonitoringStarted();
                        }
                        else
                        {
                            // Legacy behaviour: report that we can both push and be pulled
                            liveMonitorFace.notifyReadyToPush();
                            liveMonitorFace.notifyReadyForPull();
                        }
                    }

                } break;

                case eEMPostReport :
                {
                    if ( postReportFace == null )
                    {
                        IAMQPMessageDispatch dispatch = interfaceFactory.createDispatch();
                        dispatchPump.addDispatch( dispatch );

                        postReportFace = interfaceFactory.createPostReport( expMonitorID, 
                                                                            clientID, 
                                                                            dispatch );

                        postReportFace.setUserListener( this );
                        postReportFace.notifyReadyToReport();
                    }

                } break;

                case eEMTearDown :
                {
                    if ( tearDownFace == null )
                    {
                        IAMQPMessageDispatch dispatch = interfaceFactory.createDispatch();
                        dispatchPump.addDispatch( dispatch );

                        tearDownFace = interfaceFactory.createTearDown( expMonitorID, 
                                                                        clientID, 
                                                                        dispatch );

                        tearDownFace.setUserListener( this );
                        tearDownFace.notifyReadyToTearDown();
                    }

                } break;
            }
        }
    }

    @Override
    public void onRegistrationConfirmed( UUID    senderID, 
                                         Boolean confirmed,
                                         UUID    expUniqueID,
                                         String  expNamedID,
                                         String  expName,
                                         String  expDescription,
                                         Date    createTime )
    {
        if ( senderID.equals(expMonitorID) )
        {
            // Create the experiment information for this client
            currentExperiment = new Experiment( expUniqueID,
                                                expNamedID,
                                                expName,
                                                expDescription,
                                                createTime );
          
            if ( emiListener != null )
              emiListener.onEMConnectionResult( confirmed, currentExperiment );
            else
              emiLegListener.onEMConnectionResult( confirmed );
            
            discoveryFace.readyToInitialise();
        }
    }
    
    @Override
    public void onDeregisteringThisClient( UUID senderID, String reason )
    {
        if ( senderID.equals(expMonitorID) )
            if ( emiListener != null )
                emiListener.onEMDeregistration( reason );
    }

    @Override
    public void onRequestActivityPhases( UUID senderID )
    {
        if ( senderID.equals(expMonitorID) )
        {
            if ( emiListener != null )
                emiListener.onDescribeSupportedPhases( supportedPhases );
            else
            {
                // Legacy behaviour: specify full support
                supportedPhases = EnumSet.allOf( EMPhase.class );
            }
            
            discoveryFace.sendActivePhases( supportedPhases );
        }
    }

    @Override
    public void onDiscoverMetricGenerators( UUID senderID )
    {
        // Just assume that all metric generators have been discovered
        if ( senderID.equals(expMonitorID) )
            discoveryFace.sendDiscoveryResult( true );
    }

    @Override
    public void onRequestMetricGeneratorInfo( UUID senderID )
    {      
        if ( senderID.equals(expMonitorID) )
        {
             if ( emiListener != null )
                emiListener.onPopulateMetricGeneratorInfo();
             else
               emiLegListener.onPopulateMetricGeneratorInfo();
            
          
        }
    }

    @Override
    public void onDiscoveryTimeOut( UUID senderID )
    { 
        if ( emiListener != null ) 
            emiListener.onDiscoveryTimeOut();
    }

    @Override
    public void onSetStatusMonitorEndpoint( UUID senderID,
                                            String endPoint )
    { /* Not implemented in this demo */ }

    // IEMMonitorSetup_UserListener --------------------------------------------
    @Override
    public void onSetupMetricGenerator( UUID senderID, UUID genID )
    {
        if ( senderID.equals(expMonitorID) && genID != null && setupFace != null )
        {
            Boolean[] result = new Boolean[1];
            result[0] = false;

            if ( emiListener != null )
                emiListener.onSetupMetricGenerator( genID, result );
            else
                emiLegListener.onSetupMetricGenerator( genID, result );
            
            setupFace.notifyMetricGeneratorSetupResult( genID, result[0] );
        }
    }

    @Override
    public void onSetupTimeOut( UUID senderID, UUID genID )
    { 
        if ( emiListener != null )
            emiListener.onSetupTimeOut( genID );
    }

    // IEMLiveMonitor_UserListener ---------------------------------------------
    @Override
    public void onStartPushing( UUID senderID )
    {
        if ( senderID.equals(expMonitorID) )
        {
            if ( emiListener != null )
                emiListener.onStartPushingMetricData();
            else
              emiLegListener.onStartPushingMetricData();
        }       
    }

    @Override
    public void onReceivedPush( UUID senderID, UUID lastReportID )
    {
        if ( senderID.equals(expMonitorID) )
        {
            if ( emiListener != null )
                emiListener.onPushReportReceived( lastReportID );
            else
                emiLegListener.onLastPushProcessed( lastReportID );
        }
    }
    
    @Override
    public void onReceivedPull( UUID senderID, UUID lastReportID )
    {
        if ( senderID.equals(expMonitorID) )
        {
            if ( emiListener != null )
                emiListener.onPullReportReceived( lastReportID );
        }
    }

    @Override
    public void onStopPushing( UUID senderID )
    {
        if ( senderID.equals(expMonitorID) )
        {
            if ( emiListener != null )
                emiListener.onStopPushingMetricData();
            else
                emiLegListener.onStopPushingMetricData();
              
            liveMonitorFace.notifyPushingCompleted();
        }
    }

    @Override
    public synchronized void onPullMetric( UUID senderID, UUID measurementSetID )
    {
        if ( senderID.equals(expMonitorID) && liveMonitorFace != null )
        {
            // Make sure we indicate which measurement set this report is carrying
            // (up to the client to actually populate it)
            Report reportOUT = new Report( measurementSetID );

            if ( emiListener != null )
                emiListener.onPullMetric( measurementSetID, reportOUT );
            else
                emiLegListener.onPullMetric( measurementSetID, reportOUT );

            liveMonitorFace.sendPulledMetric( reportOUT );
        }
    }

    @Override
    public void onPullMetricTimeOut( UUID senderID, UUID measurementSetID )
    {
        if ( emiListener != null )
            emiListener.onPullMetricTimeOut( measurementSetID );
    }

    @Override
    public void onPullingStopped( UUID senderID )
    {
        if ( senderID.equals(expMonitorID) )
        {
            if ( emiListener != null )
                emiListener.onPullingStopped();
        }
    }

    // IEMPostReport_UserListener ----------------------------------------------
    @Override
    public void onRequestPostReportSummary( UUID senderID )
    {
        if ( senderID.equals(expMonitorID) && postReportFace != null )
        {
            EMPostReportSummary summary = new EMPostReportSummary();
            
            if ( emiListener != null )
                emiListener.onPopulateSummaryReport( summary );
            else
              emiLegListener.onPopulateSummaryReport( summary );

            postReportFace.sendReportSummary( summary );
        }
    }

    @Override
    public void onRequestDataBatch( UUID senderID, EMDataBatch reqBatch )
    {
        if ( senderID.equals(expMonitorID) && reqBatch != null && postReportFace != null )
        {
            if ( emiListener != null )
                 emiListener.onPopulateDataBatch( reqBatch );
            else
            {
                // Probably won't support this request in the legacy version
                // so just return an empty batch
            }

          postReportFace.sendDataBatch( reqBatch );
        }
    }

    @Override
    public void notifyReportBatchTimeOut( UUID senderID, UUID batchID )
    {
        if ( emiListener != null )
            emiListener.onReportBatchTimeOut( batchID );
    }

    // IEMTearDown_UserListener ------------------------------------------------
    @Override
    public void onTearDownMetricGenerators( UUID senderID )       
    {
        if ( senderID.equals(expMonitorID) )
        {
            Boolean[] result = new Boolean[1];
            
            if ( emiListener != null )
                emiListener.onGetTearDownResult( result );
            else
                emiLegListener.onGetTearDownResult( result );

            tearDownFace.sendTearDownResult( result[0] );
        }
    }

    @Override
    public void onTearDownTimeOut( UUID senderID )
    { 
        if ( senderID.equals(expMonitorID) )
        {
            if ( emiListener != null )
                emiListener.onTearDownTimeOut();
        }
    }
}
