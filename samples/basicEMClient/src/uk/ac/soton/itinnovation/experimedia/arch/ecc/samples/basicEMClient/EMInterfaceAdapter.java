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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.basicEMClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import java.util.*;




public class EMInterfaceAdapter implements IEMDiscovery_UserListener,
                                           IEMSetup_UserListener,
                                           IEMLiveMonitor_UserListener,
                                           IEMPostReport_UserListener,
                                           IEMTearDown_UserListener
{
    private EMIAdapterListener emiListener;
    private String             clientName;
    private AMQPBasicChannel   amqpChannel;
    private UUID               expMonitorID;
    private UUID               clientID;

    private EMInterfaceFactory       interfaceFactory;
    private IAMQPMessageDispatchPump dispatchPump;

    // EM Interfaces
    private IEMMonitorEntryPoint entryPointFace;
    private IEMDiscovery         discoveryFace;
    private IEMMetricGenSetup    setupFace;
    private IEMLiveMonitor       liveMonitorFace;
    private IEMPostReport        postReportFace;
    private IEMTearDown          tearDownFace;

    // Metric Generators
    private HashSet<MetricGenerator> clientGenerators;


    public EMInterfaceAdapter( EMIAdapterListener listener )
    {
        emiListener = listener;
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

        amqpChannel  = channel;
        clientName   = name;
        expMonitorID = emID;
        clientID     = ourID;

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
    
    public void deregisterWithEM() throws Exception
    {
        if ( discoveryFace == null ) throw new Exception( "Have not registered correctly with EM" );
      
        discoveryFace.clientDisconnecting();
    }

    public void setMetricGenerators( HashSet<MetricGenerator> generators )
    { clientGenerators = generators; }

    public void pushMetric( Report report )
    {
        if ( report != null && liveMonitorFace != null )
            liveMonitorFace.pushMetric( report );
    }

    // IEMDiscovery_UserListener -------------------------------------------------
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

                        // Report that we can both push and be pulled
                        liveMonitorFace.notifyReadyToPush();
                        liveMonitorFace.notifyReadyForPull();
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
    public void onRegistrationConfirmed( UUID senderID, Boolean confirmed )
    {
        if ( senderID.equals(expMonitorID) )
        {
            emiListener.onEMConnectionResult( confirmed );
            discoveryFace.readyToInitialise();
        }
    }

    @Override
    public void onRequestActivityPhases( UUID senderID )
    {
        if ( senderID.equals(expMonitorID) )
        {
            // Notify EM that ALL the phases are supported by this adapter
            EnumSet<EMPhase> phases = EnumSet.noneOf( EMPhase.class );
            phases.add( EMPhase.eEMDiscoverMetricGenerators );
            phases.add( EMPhase.eEMSetUpMetricGenerators );
            phases.add( EMPhase.eEMLiveMonitoring );
            phases.add( EMPhase.eEMPostMonitoringReport );
            phases.add( EMPhase.eEMTearDown );

            discoveryFace.sendActivePhases( phases );
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
            emiListener.onPopulateMetricGeneratorInfo();
            discoveryFace.sendMetricGeneratorInfo( clientGenerators );
        }
    }

    @Override
    public void onDiscoveryTimeOut( UUID senderID )
    { /* Not implemented in this demo */ }

    @Override
    public void onSetStatusMonitorEndpoint( UUID senderID,
                                            String endPoint )
    { /* Not implemented in this demo */ }

    // IEMMonitorSetup_UserListener ----------------------------------------------
    @Override
    public void onSetupMetricGenerator( UUID senderID, UUID genID )
    {
        if ( senderID.equals(expMonitorID) && genID != null && setupFace != null )
        {
            Boolean[] result = new Boolean[1];
            result[0] = false;

            emiListener.onSetupMetricGenerator( genID, result );
            setupFace.notifyMetricGeneratorSetupResult( genID, result[0] );
        }
    }

    @Override
    public void onSetupTimeOut( UUID senderID, UUID genID )
    { /* Not imeplemented in this demo */ }

    // IEMLiveMonitor_UserListener -----------------------------------------------
    @Override
    public void onStartPushing( UUID senderID )
    {
        if ( senderID.equals(expMonitorID) )
            emiListener.onStartPushingMetricData();
    }

    @Override
    public void onReceivedPush( UUID senderID, UUID lastReportID )
    {
        if ( senderID.equals(expMonitorID) )
            emiListener.onPushReportReceived( lastReportID );
    }
    
    @Override
    public void onReceivedPull( UUID senderID, UUID lastReportID )
    {
        if ( senderID.equals(expMonitorID) )
            emiListener.onPullReportReceived( lastReportID );
    }

    @Override
    public void onStopPushing( UUID senderID )
    {
        if ( senderID.equals(expMonitorID) )
        {
            emiListener.onStopPushingMetricData();
            liveMonitorFace.notifyPushingCompleted();
        }
    }

    @Override
    public void onPullMetric( UUID senderID, UUID measurementSetID )
    {
        if ( senderID.equals(expMonitorID) && liveMonitorFace != null )
        {
            Report reportOut = new Report();

            emiListener.onPullMetric( measurementSetID, reportOut );

            liveMonitorFace.sendPulledMetric( reportOut );
        }
    }

    @Override
    public void onPullMetricTimeOut( UUID senderID, UUID measurementSetID )
    {
    
    }

    @Override
    public void onPullingStopped( UUID senderID )
    { /* Not implemented in this demo */ }

    // IEMPostReport_UserListener ------------------------------------------------
    @Override
    public void onRequestPostReportSummary( UUID senderID )
    {
        if ( senderID.equals(expMonitorID) && postReportFace != null )
        {
            EMPostReportSummary summary = new EMPostReportSummary();
            emiListener.onPopulateSummaryReport( summary );

            postReportFace.sendReportSummary( summary );
        }
    }

    @Override
    public void onRequestDataBatch( UUID senderID, EMDataBatch reqBatch )
    {
        if ( senderID.equals(expMonitorID) && reqBatch != null && postReportFace != null )
        {
          emiListener.onPopulateDataBatch( reqBatch );

          postReportFace.sendDataBatch( reqBatch );
        }
    }

    @Override
    public void notifyReportBatchTimeOut( UUID senderID, UUID batchID )
    { /* Not implemented in this demo */ }

    // IEMTearDown_UserListener --------------------------------------------------
    @Override
    public void onTearDownMetricGenerators( UUID senderID )       
    {
        if ( senderID.equals(expMonitorID) )
        {
            Boolean[] result = new Boolean[1];
            emiListener.onGetTearDownResult( result );

            tearDownFace.sendTearDownResult( result[0] );
        }
    }

    @Override
    public void onTearDownTimeOut( UUID senderID )
    { /* Not implemented in this demo */ }
}
