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



public class EMInterfaceAdapter implements IEMMonitor_UserListener,
                                           IEMMonitorSetup_UserListener
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
  private IEMMonitor           monitorFace;
  private IEMMonitorSetup      setupFace;
  private IEMMonitorControl    controlFace;
  private IEMReport            reportFace;
  private IEMTearDown          tearDownFace;
  

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
    IAMQPMessageDispatch epDispatch = interfaceFactory.createDispatch();
    dispatchPump.addDispatch( epDispatch );
      
    // Crate our entry point interface
    entryPointFace = interfaceFactory.createEntryPoint( expMonitorID, epDispatch );
    
    // Create the principal interface (IEMMonitor ahead of time)
    IAMQPMessageDispatch monDispatch = interfaceFactory.createDispatch();
    dispatchPump.addDispatch( monDispatch );
    monitorFace = interfaceFactory.createMonitor( expMonitorID, 
                                                  clientID, 
                                                  monDispatch );
    
    monitorFace.setUserListener( this );
      
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
        case eEMMonitorSetup :
        {
          if ( setupFace == null )
          {
            IAMQPMessageDispatch monDispatch = interfaceFactory.createDispatch();
            dispatchPump.addDispatch( monDispatch );
            
            setupFace = interfaceFactory.createSetup( expMonitorID, 
                                                      clientID, 
                                                      monDispatch );
            
            setupFace.setUserListener( this );
          }
        } break;
          
        case eEMMonitorControl :
        {
          //TODO
        } break;
          
        case eECCReport :
        {
          //TODO
        } break;
          
        case eECCTearDown :
        {
          //TODO
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
      monitorFace.readyToInitialise();
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

      monitorFace.sendActivePhases( phases );
    }
  }
  
  @Override
  public void onDiscoverMetricGenerators( UUID senderID )
  {
    // Just assume that all metric generators have been discovered
    if ( senderID.equals(expMonitorID) )
      monitorFace.sendDiscoveryResult( true );
  }
  
  @Override
  public void onRequestMetricGeneratorInfo( UUID senderID )
  {
    if ( senderID.equals(expMonitorID) && emiListener != null )
    {
      HashSet<MetricGenerator> genSet = new HashSet<MetricGenerator>();
      emiListener.updateMetricGenerators( genSet );
      
      monitorFace.sendMetricGeneratorInfo( genSet );
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
    // Just assume metrics are ready to go for this demo
    if ( senderID.equals(expMonitorID) && setupFace != null )
      setupFace.notifyMetricGeneratorSetupResult(genID, true );
  }
  
  @Override
  public void onSetupTimeOut( UUID senderID, UUID genID )
  { /* Not imeplemented in this demo */ }
}
