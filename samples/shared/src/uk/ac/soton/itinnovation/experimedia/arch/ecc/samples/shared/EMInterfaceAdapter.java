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
    protected Set<MetricGenerator> clientGenerators;


    public EMInterfaceAdapter( EMIAdapterListener listener )
    {
        emiListener     = listener;
        
        clientGenerators = new HashSet<MetricGenerator>();
        supportedPhases  = EnumSet.noneOf( EMPhase.class );
    }
    
    public EMInterfaceAdapter ( EMILegacyAdapterListener legListener )
    {
        emiLegListener = legListener;
        
        clientGenerators = new HashSet<MetricGenerator>();
        supportedPhases  = EnumSet.noneOf( EMPhase.class );
    }

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

    public Experiment getExperimentInfo()
    { return currentExperiment; }
    
    public void setMetricGenerators( Set<MetricGenerator> generators )
    {
        if ( generators != null ) clientGenerators = generators;
    }

    public void pushMetric( Report report )
    {
        if ( report != null && liveMonitorFace != null )
            liveMonitorFace.pushMetric( report );
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
             
            discoveryFace.sendMetricGeneratorInfo( clientGenerators );
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
            Report reportOUT = new Report();

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
