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
//      Created Date :          02-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccMonitor;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccEntryPoint.ECCMonitorEntryPointTest;

import java.util.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;






/**
 * ECCMonitorTestExecutor provides a more complex test for interacting with the 
 * provider of the following interfaces:
 * 
 *  * IECCMonitor (the 'foundation' interface of the EM)
 *  * IECCTest    (a simple data transfer testing interface of the EM)
 * 
 * This test case starts off from where the ECCMonitorEntryPointTest ended: it
 * assumes the entry point has been established and both provider and user are 
 * aware of each other's UUIDs.
 * 
 * As with other test cases, ECCMonitorTestExecutor acts as _both_ the provider 
 * (EM) and the user (client of the EM) - this makes testing across the AMQP
 * bus considerably simpler.
 * 
 * (1) The provider sends the user a registration confirmation message
 * (2) On receipt of (1) the user sends the provider a 'ready to initialise' message
 * (3) On receipt of (2) the provider creates an IECCTest interface
 * (4) The provider then sends the user a message to create a IECCTest interface
 * (5) On receipt of (4) the user creates an IECCTest interface
 * (6) The user creates some dummy byte data
 * (7) The user sends the dummy data to the provider via the IECCTest interface
 * (8) The user sends the provider a 'client disconnecting' message
 * (9) After receipt of (7) and (8) the test is completed and successful
 * 
 * @author sgc
 */
public class ECCMonitorTestExecutor implements Runnable,
                                               IEMDiscovery_ProviderListener,
                                               IEMDiscovery_UserListener,
                                               IEMTest_Listener
        
{
  private AMQPBasicChannel providerChannel;
  private AMQPBasicChannel userChannel;
  
  IAMQPMessageDispatchPump providerPump;
  IAMQPMessageDispatchPump userPump;
  
  private IEMDiscovery providerDiscovery;
  private IEMDiscovery userDiscovery;
  
  private IEMTest providerTest;
  private IEMTest userTest;
  
  private int    testDataSize;
  private byte[] testDataBody;
  
  private boolean userGotRegistrationConfirmed = false;
  private boolean providerGotReadyToInit       = false;
  private boolean userGotCreateTestFaceCommand = false;
  private boolean providerGotTestBytes         = false;
  private boolean providerGotDisconnectNotice  = false;
  
  
  public ECCMonitorTestExecutor( AMQPBasicChannel provider,
                                 AMQPBasicChannel user )
  {
    providerChannel = provider;
    userChannel     = user;
  }
  
  public boolean getTestResult()
  {
    if ( userGotRegistrationConfirmed == true &&
         providerGotReadyToInit       == true && 
         userGotCreateTestFaceCommand == true &&
         providerGotTestBytes         == true &&
         providerGotDisconnectNotice  == true )
    {
      System.out.println( "ECCMonitorTest is GOOD." );
      return true;
    }
      
    
    return false;
  }
  
  // IECCTest_Listener ---------------------------------------------------------
  @Override
  public void onReceivedData( int byteCount, byte[] dataBody )
  {
    // Check data size
    if ( byteCount == testDataSize )
    {
      // Check data integrity
      boolean dataOK = true;
      
      for ( int i=0; i < byteCount; i++ )
        if ( dataBody[i] != testDataBody[i] )
        {
          dataOK = false;
          break;
        }
      
      if ( dataOK ) providerGotTestBytes = true;
    }
  }
  
  // IEMDiscovery_ProviderListener ---------------------------------------------
  @Override
  public void onReadyToInitialise( UUID senderID )
  {    
    // Make sure event is from the correct sender
    if ( senderID.equals(ECCMonitorEntryPointTest.EMUserUUID) )
    {
      providerGotReadyToInit = true;
    
      // Create a test interface here and get ready for data
      EMInterfaceFactory providerFactory = new EMInterfaceFactory( providerChannel, true );
      
      // Create dispatcher & attach dispatcher to (shared) pump
      IAMQPMessageDispatch providerDispatch = providerFactory.createDispatch();
      providerPump.addDispatch( providerDispatch );

      userTest = providerFactory.createTest( ECCMonitorEntryPointTest.EMProviderUUID,
                                             ECCMonitorEntryPointTest.EMUserUUID,
                                             providerDispatch );

      providerTest = providerFactory.createTest( ECCMonitorEntryPointTest.EMProviderUUID,
                                                 ECCMonitorEntryPointTest.EMUserUUID,
                                                 providerDispatch );

      providerTest.setListener( this );

      // Tell user to create a test interface
      providerDiscovery.createInterface( EMInterfaceType.eEMTestInterface );
    }
  }
  
  @Override
  public void onSendActivityPhases( UUID senderID,
                                    EnumSet<EMPhase> supportedPhases )
  { /*Not implemented in this test*/ }
  
  @Override
  public void onSendDiscoveryResult( UUID senderID,
                                     Boolean discoveredGenerators )
  { /*Not implemented in this test*/ }
  
  @Override
  public void onSendMetricGeneratorInfo( UUID senderID,
                                         Set<MetricGenerator> generators )
  { /*Not implemented in this test*/ }
  
  @Override
  public void onClientDisconnecting( UUID senderID )
  {
    // Make sure event is from the correct sender
    if ( senderID.equals( ECCMonitorEntryPointTest.EMUserUUID) )
    {
      if ( !providerGotTestBytes )
        System.out.println( "Client disconnect message received; waiting to receive bytes" );
    
      providerGotDisconnectNotice = true;
    }
  }
  
  // IEMDiscovery_UserListener -------------------------------------------------
  @Override
  public void onCreateInterface( UUID senderID, EMInterfaceType type )
  {
    // Make sure event is from the correct sender
    if ( senderID.equals( ECCMonitorEntryPointTest.EMProviderUUID) )
    {
      if ( type == EMInterfaceType.eEMTestInterface )
      userGotCreateTestFaceCommand = true;
    
      // Create a test interface here
      EMInterfaceFactory userFactory = new EMInterfaceFactory( providerChannel, false );
      
      // Create dispatcher & attach dispatcher to (shared) pump
      IAMQPMessageDispatch userDispatch = userFactory.createDispatch();
      providerPump.addDispatch( userDispatch );

      userTest = userFactory.createTest( ECCMonitorEntryPointTest.EMProviderUUID,
                                         ECCMonitorEntryPointTest.EMUserUUID,
                                         userDispatch );

      // Create some test data
      testDataSize = 2048;
      testDataBody = new byte[testDataSize];
      Random rand = new Random();
      rand.nextBytes( testDataBody );

      // Send the data to the provider
      userTest.sendData( testDataSize, testDataBody );

      // And immediately send a dis-connection notice (will it arrive before end of data?)
      userDiscovery.clientDisconnecting();
    }
  }
  
  @Override
  public void onRegistrationConfirmed( UUID senderID, Boolean confirmed )
  {
    // Make sure event is from the correct sender
    if ( senderID.equals( ECCMonitorEntryPointTest.EMProviderUUID) )
    {
      userGotRegistrationConfirmed = true; // Don't really care about the result
    
      // Send provider notice that user is ready to initialise
      userDiscovery.readyToInitialise();
    }
  }
  
  @Override
  public void onRequestActivityPhases( UUID senderID )
  { /*Not implemented in this test*/ }
  
  @Override
  public void onDiscoverMetricGenerators( UUID senderID )
  { /*Not implemented in this test*/ }
  
  @Override
  public void onRequestMetricGeneratorInfo( UUID senderID )
  { /*Not implemented in this test*/ }
  
  @Override
  public void onDiscoveryTimeOut( UUID senderID )
  { /*Not implemented in this test*/ }
  
  @Override
  public void onSetStatusMonitorEndpoint( UUID senderID, String endPoint )
  { /*Not implemented in this test*/ }
  
  // Runnable ------------------------------------------------------------------
  @Override
  public void run()
  {
    // Create interface factories for both provider and user
    EMInterfaceFactory providerFactory = new EMInterfaceFactory( providerChannel, true );
    EMInterfaceFactory userFactory     = new EMInterfaceFactory( userChannel, false );
    
    // Create pump/dispatchers for provider
    providerPump = providerFactory.createDispatchPump( "Provider pump", 
                                                       IAMQPMessageDispatchPump.ePumpPriority.MINIMUM );
    
    IAMQPMessageDispatch providerDispatch = providerFactory.createDispatch();
    providerPump.addDispatch( providerDispatch );
    providerPump.startPump();
    
    // Create pump/dispatchers for user
    userPump = providerFactory.createDispatchPump( "User pump", 
                                                   IAMQPMessageDispatchPump.ePumpPriority.MINIMUM );
    
    IAMQPMessageDispatch userDispatch = userFactory.createDispatch();
    
    userPump.addDispatch( userDispatch );
    userPump.startPump();
    
    // Create discovery interfaces for both provider and user
    providerDiscovery = providerFactory.createDiscovery( ECCMonitorEntryPointTest.EMProviderUUID,
                                                         ECCMonitorEntryPointTest.EMUserUUID,
                                                         providerDispatch );
    
    userDiscovery = userFactory.createDiscovery( ECCMonitorEntryPointTest.EMProviderUUID,
                                                 ECCMonitorEntryPointTest.EMUserUUID,
                                                 userDispatch );
    
    // This class acts as BOTH provider and user
    providerDiscovery.setProviderListener( this );
    userDiscovery.setUserListener( this );
    
    // Start simulation from point of registration confirmation from provider
    providerDiscovery.registrationConfirmed( true );
  }
}
