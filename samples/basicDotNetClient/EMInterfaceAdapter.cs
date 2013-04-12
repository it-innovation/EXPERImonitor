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
//      Created Date :          10-Apr-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics;
using uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory;

using System;
using System.Collections.Generic;





namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared
{

/**
 * Client writers can use this class as a high-level wrapper for communications with
 * the ECC. The class using an instance of EMInterfaceAdapter should implement the EMIAdapterListener 
 * interface so that it can respond to requests from the ECC. Writers wishing only to respond to V0.9 
 * behaviours should instead provide a EMILegacyAdapterListener implementation.
 * 
 * @author Simon Crowle
 */
public class EMInterfaceAdapter : IEMDiscovery_UserListener,
                                  IEMSetup_UserListener,
                                  IEMLiveMonitor_UserListener,
                                  IEMPostReport_UserListener,
                                  IEMTearDown_UserListener
{
    private readonly Object pullLock = new Object(); 

    protected EMIAdapterListener       emiListener;
    
    protected string             clientName;
    protected AMQPBasicChannel   amqpChannel;
    protected Guid               expMonitorID;
    protected Guid               clientID;
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
    
    protected bool clientRegistered = false;

    // Supported phases & metric Generators
    protected HashSet<EMPhase>         supportedPhases;
    protected HashSet<MetricGenerator> clientGenerators;


    /**
     * Constructor for the EMInterfaceAdapter using the most recent listener to ECC events.
     * 
     * @param listener - ECC event listener (V1.0)
     */
    public EMInterfaceAdapter( EMIAdapterListener listener )
    {
        emiListener      = listener; 
        clientGenerators = new HashSet<MetricGenerator>();
        supportedPhases  = new HashSet<EMPhase>();
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
                                Guid emID,
                                Guid ourID )
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
                                                            ePumpPriority.MINIMUM );
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
    public void disconnectFromEM()
    {
        bool sentDisconnectMessage = false;
        
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
    public void setMetricGenerators( HashSet<MetricGenerator> generators )
    {
        if ( generators != null ) clientGenerators = generators;
    }

    /**
     * Actively push a metric report to the ECC. This method should only be called during 
     * the LiveMonitoring process.
     * 
     * @param report - A fully populated instance of a metric report.
     */
    public void pushMetric( Report report )
    {
        if ( report != null && liveMonitorFace != null )
            liveMonitorFace.pushMetric( report );
    }

    // IEMDiscovery_UserListener -----------------------------------------------
    public void onCreateInterface( Guid senderID, EMInterfaceType type )
    {
        if ( senderID != null && senderID.Equals(expMonitorID) )
        {      
            switch (type)
            {
                case EMInterfaceType.eEMSetup :
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

                case EMInterfaceType.eEMLiveMonitor :
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

                case EMInterfaceType.eEMPostReport :
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

                case EMInterfaceType.eEMTearDown :
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

    public void onRegistrationConfirmed( Guid     senderID, 
                                         bool     confirmed,
                                         Guid     expUniqueID,
                                         String   expNamedID,
                                         String   expName,
                                         String   expDescription,
                                         DateTime createTime )
    {
        if ( senderID.Equals(expMonitorID) )
        {
            // Create the experiment information for this client
            currentExperiment = new Experiment( expUniqueID,
                                                expNamedID,
                                                expName,
                                                expDescription,
                                                createTime );
          
            if ( emiListener != null )
              emiListener.onEMConnectionResult( confirmed, currentExperiment );
            
            discoveryFace.readyToInitialise();
        }
    }
    
    public void onDeregisteringThisClient( Guid senderID, string reason )
    {
        if ( senderID.Equals(expMonitorID) )
            if ( emiListener != null )
                emiListener.onEMDeregistration( reason );
    }

    public void onRequestActivityPhases( Guid senderID )
    {
        if ( senderID.Equals(expMonitorID) )
        {
            if ( emiListener != null )
                emiListener.onDescribeSupportedPhases( supportedPhases );
            
            discoveryFace.sendActivePhases( supportedPhases );
        }
    }

    public void onDiscoverMetricGenerators( Guid senderID )
    {
        // Just assume that all metric generators have been discovered
        if ( senderID.Equals(expMonitorID) )
            discoveryFace.sendDiscoveryResult( true );
    }

    public void onRequestMetricGeneratorInfo( Guid senderID )
    {
        if ( senderID.Equals(expMonitorID) )
        {
             if ( emiListener != null )
                emiListener.onPopulateMetricGeneratorInfo();
             
            discoveryFace.sendMetricGeneratorInfo( clientGenerators );
        }
    }

    public void onDiscoveryTimeOut( Guid senderID )
    { 
        if ( emiListener != null ) 
            emiListener.onDiscoveryTimeOut();
    }

    public void onSetStatusMonitorEndpoint( Guid senderID,
                                            string endPoint )
    { /* Not implemented in this demo */ }

    // IEMMonitorSetup_UserListener --------------------------------------------
    public void onSetupMetricGenerator( Guid senderID, Guid genID )
    {
        if ( senderID.Equals(expMonitorID) && genID != null && setupFace != null )
        {
            Boolean[] result = new Boolean[1];
            result[0] = false;

            if ( emiListener != null )
                emiListener.onSetupMetricGenerator( genID, result );
            
            setupFace.notifyMetricGeneratorSetupResult( genID, result[0] );
        }
    }

    public void onSetupTimeOut( Guid senderID, Guid genID )
    { 
        if ( emiListener != null )
            emiListener.onSetupTimeOut( genID );
    }

    // IEMLiveMonitor_UserListener ---------------------------------------------
    public void onStartPushing( Guid senderID )
    {
        if ( senderID.Equals(expMonitorID) )
        {
            if ( emiListener != null )
                emiListener.onStartPushingMetricData();
        }       
    }

    public void onReceivedPush( Guid senderID, Guid lastReportID )
    {
        if ( senderID.Equals(expMonitorID) )
        {
            if ( emiListener != null )
                emiListener.onPushReportReceived( lastReportID );
        }
    }
    
    public void onReceivedPull( Guid senderID, Guid lastReportID )
    {
        if ( senderID.Equals(expMonitorID) )
        {
            if ( emiListener != null )
                emiListener.onPullReportReceived( lastReportID );
        }
    }

    public void onStopPushing( Guid senderID )
    {
        if ( senderID.Equals(expMonitorID) )
        {
            if ( emiListener != null )
                emiListener.onStopPushingMetricData();
              
            liveMonitorFace.notifyPushingCompleted();
        }
    }

    public void onPullMetric( Guid senderID, Guid measurementSetID )
    {
        if ( senderID.Equals(expMonitorID) && liveMonitorFace != null )
        {
            lock( pullLock )
            {
                // Make sure we indicate which measurement set this report is carrying
                // (up to the client to actually populate it)
                Report reportOUT = new Report( measurementSetID );

                if ( emiListener != null )
                    emiListener.onPullMetric( measurementSetID, reportOUT );

                liveMonitorFace.sendPulledMetric( reportOUT );
            }
        }
    }

    public void onPullMetricTimeOut( Guid senderID, Guid measurementSetID )
    {
        if ( emiListener != null )
            emiListener.onPullMetricTimeOut( measurementSetID );
    }

    public void onPullingStopped( Guid senderID )
    {
        if ( senderID.Equals(expMonitorID) )
        {
            if ( emiListener != null )
                emiListener.onPullingStopped();
        }
    }

    // IEMPostReport_UserListener ----------------------------------------------
    public void onRequestPostReportSummary( Guid senderID )
    {
        if ( senderID.Equals(expMonitorID) && postReportFace != null )
        {
            EMPostReportSummary summary = new EMPostReportSummary();
            
            if ( emiListener != null )
                emiListener.onPopulateSummaryReport( summary );

            postReportFace.sendReportSummary( summary );
        }
    }

    public void onRequestDataBatch( Guid senderID, EMDataBatch reqBatch )
    {
        if ( senderID.Equals(expMonitorID) && reqBatch != null && postReportFace != null )
        {
            if ( emiListener != null )
                 emiListener.onPopulateDataBatch( reqBatch );

          postReportFace.sendDataBatch( reqBatch );
        }
    }

    public void notifyReportBatchTimeOut( Guid senderID, Guid batchID )
    {
        if ( emiListener != null )
            emiListener.onReportBatchTimeOut( batchID );
    }

    // IEMTearDown_UserListener ------------------------------------------------
    public void onTearDownMetricGenerators( Guid senderID )       
    {
        if ( senderID.Equals(expMonitorID) )
        {
            Boolean[] result = new Boolean[1];
            
            if ( emiListener != null )
                emiListener.onGetTearDownResult( result );

            tearDownFace.sendTearDownResult( result[0] );
        }
    }

    public void onTearDownTimeOut( Guid senderID )
    { 
        if ( senderID.Equals(expMonitorID) )
        {
            if ( emiListener != null )
                emiListener.onTearDownTimeOut();
        }
    }
}

} // namespace
