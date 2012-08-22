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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.samples.basicEMClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;

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
  private HashMap<UUID, MetricGenerator> metricGenerators;
  

  public EMInterfaceAdapter( EMIAdapterListener listener )
  {
    emiListener = listener;
    
    metricGenerators = new HashMap<UUID, MetricGenerator>();
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
    IAMQPMessageDispatch epDispatch = interfaceFactory.createDispatch();
    dispatchPump.addDispatch( epDispatch );
      
    // Crate our entry point interface
    entryPointFace = interfaceFactory.createEntryPoint( expMonitorID, epDispatch );
    
    // Create the principal interface (IEMDiscovery ahead of time)
    IAMQPMessageDispatch monDispatch = interfaceFactory.createDispatch();
    dispatchPump.addDispatch( monDispatch );
    discoveryFace = interfaceFactory.createDiscovery( expMonitorID, 
                                                      clientID, 
                                                      monDispatch );
    
    discoveryFace.setUserListener( this );
      
    //.. and finally, try registering with the EM!
    entryPointFace.registerAsEMClient( clientID, "Simple EM Client" );
  }
  
  // IEMMonitor_UserListener ---------------------------------------------------
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
            IAMQPMessageDispatch monDispatch = interfaceFactory.createDispatch();
            dispatchPump.addDispatch( monDispatch );
            
            setupFace = interfaceFactory.createSetup( expMonitorID, 
                                                      clientID, 
                                                      monDispatch );
            
            setupFace.setUserListener( this );
            setupFace.notifyReadyToSetup();
          }
          
        } break;
          
        case eEMLiveMonitor :
        {
          if ( setupFace == null )
          {
            IAMQPMessageDispatch monDispatch = interfaceFactory.createDispatch();
            dispatchPump.addDispatch( monDispatch );
            
            liveMonitorFace = interfaceFactory.createLiveMonitor( expMonitorID, 
                                                                  clientID, 
                                                                  monDispatch );
            
            liveMonitorFace.setUserListener( this );
            //TODO: Decide on push/pull
          }
          
        } break;
          
        case eEMPostReport :
        {
          if ( setupFace == null )
          {
            IAMQPMessageDispatch monDispatch = interfaceFactory.createDispatch();
            dispatchPump.addDispatch( monDispatch );
            
            postReportFace = interfaceFactory.createPostReport( expMonitorID, 
                                                                clientID, 
                                                                monDispatch );
            
            postReportFace.setUserListener( this );
            postReportFace.notifyReadyToReport();
          }
          
        } break;
          
        case eEMTearDown :
        {
          if ( setupFace == null )
          {
            IAMQPMessageDispatch monDispatch = interfaceFactory.createDispatch();
            dispatchPump.addDispatch( monDispatch );
            
            tearDownFace = interfaceFactory.createTearDown( expMonitorID, 
                                                            clientID, 
                                                            monDispatch );
            
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
    if ( senderID.equals(expMonitorID) && emiListener != null )
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
      // Notify EM of all the phases supported by this adapter
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
    if ( senderID.equals(expMonitorID) && emiListener != null )
    {
      HashSet<MetricGenerator> genSet = new HashSet<MetricGenerator>();
      genSet.addAll( metricGenerators.values() );
      
      emiListener.populateMetricGeneratorInfo( genSet );
      discoveryFace.sendMetricGeneratorInfo( genSet );
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
    if ( senderID.equals(expMonitorID) && setupFace != null )
    {
      MetricGenerator mg = metricGenerators.get( genID );
      if ( mg != null )
      {
        Boolean[] result = new Boolean[1];
        result[0] = false;
        emiListener.setupMetricGenerator( mg, result );
        
        setupFace.notifyMetricGeneratorSetupResult( genID, result[0] );
      }
    }
  }
  
  @Override
  public void onSetupTimeOut( UUID senderID, UUID genID )
  { /* Not imeplemented in this demo */ }
  
  // IEMLiveMonitor_UserListener -----------------------------------------------
  @Override
  public void onStartPushing( UUID senderID )
  {
    
  }
  
  @Override
  public void onReceivedPush( UUID senderID, UUID lastReportID )
  {
    
  }
  
  @Override
  public void onStopPushing( UUID senderID )
  {
    
  }
  
  @Override
  public void onPullMetric( UUID senderID, UUID measurementSetID )
  {
    
  }
  
  @Override
  public void onPullMetricTimeOut( UUID senderID, UUID measurementSetID )
  {
    
  }
  
  @Override
  public void onPullingStopped( UUID senderID )
  {
    
  }
  
  // IEMPostReport_UserListener ------------------------------------------------
  @Override
  public void onRequestPostReportSummary( UUID senderID )
  {
    
  }
  
  @Override
  public void onRequestDataBatch( UUID senderID, EMDataBatch reqBatch )
  {
    
  }
  
  @Override
  public void notifyReportBatchTimeOut( UUID senderID, UUID batchID )
  {
    
  }
  
  // IEMTearDown_UserListener --------------------------------------------------
  @Override
  public void onTearDownMetricGenerators( UUID senderID )       
  {
    
  }
  
  @Override
  public void onTearDownTimeOut( UUID senderID )
  {
    
  }
}
