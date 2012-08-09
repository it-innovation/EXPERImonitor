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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.listeners.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccEntryPoint.ECCMonitorEntryPointTest;

import java.util.*;




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
                                               IECCMonitor_ProviderListener,
                                               IECCMonitor_UserListener,
                                               IECCTest_Listener
        
{
  private AMQPBasicChannel providerChannel;
  private AMQPBasicChannel userChannel;
  
  IAMQPMessageDispatchPump dispatchPump;
  
  private IECCMonitor providerMonitor;
  private IECCMonitor userMonitor;
  
  private IECCTest providerTest;
  private IECCTest userTest;
  
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
  
  // IECCMonitor_ProviderListener ----------------------------------------------
  @Override
  public void onReadyToInitialise( UUID senderID )
  {
    // Make sure event is from the correct sender
    if ( senderID.equals( ECCMonitorEntryPointTest.EMUserUUID) )
    {
      providerGotReadyToInit = true;
    
      // Create a test interface here and get ready for data
      EMInterfaceFactory providerFactory = new EMInterfaceFactory( providerChannel, true );
      
      // Create dispatcher & attach dispatcher to (shared) pump
      IAMQPMessageDispatch providerDispatch = providerFactory.createDispatch();
      dispatchPump.addDispatch( providerDispatch );

      userTest = providerFactory.createTest( ECCMonitorEntryPointTest.EMProviderUUID,
                                             ECCMonitorEntryPointTest.EMUserUUID,
                                             providerDispatch );

      providerTest = providerFactory.createTest( ECCMonitorEntryPointTest.EMProviderUUID,
                                                 ECCMonitorEntryPointTest.EMUserUUID,
                                                 providerDispatch );

      providerTest.setListener( this );

      // Tell user to create a test interface
      providerMonitor.createInterface( IECCMonitor.EMInterfaceType.eECCTestInterface );
    }
  }
  
  @Override
  public void onSendActivityPhases( UUID senderID,
                                    List<IECCMonitor.EMSupportedPhase> supportedPhases )
  { /*Not implemented in this test*/ }
  
  @Override
  public void onSendDiscoveryResult( UUID senderID /* Data model under development*/ )
  { /*Not implemented in this test*/ }
  
  @Override
  public void onClientDisconnecting( UUID senderID )
  {
    // Make sure event is from the correct sender
    if ( senderID.equals( ECCMonitorEntryPointTest.EMUserUUID) )
    {
      if ( !providerGotTestBytes )
        System.out.println( "Disconnecting client before send bytes ended!" );
    
      providerGotDisconnectNotice = true;
    }
  }
  
  // IECCMonitor_UserListener --------------------------------------------------
  @Override
  public void onCreateInterface( UUID senderID, IECCMonitor.EMInterfaceType type )
  {
    // Make sure event is from the correct sender
    if ( senderID.equals( ECCMonitorEntryPointTest.EMProviderUUID) )
    {
      if ( type == IECCMonitor.EMInterfaceType.eECCTestInterface )
      userGotCreateTestFaceCommand = true;
    
      // Create a test interface here
      EMInterfaceFactory userFactory = new EMInterfaceFactory( providerChannel, false );
      
      // Create dispatcher & attach dispatcher to (shared) pump
      IAMQPMessageDispatch userDispatch = userFactory.createDispatch();
      dispatchPump.addDispatch( userDispatch );

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
      userMonitor.clientDisconnecting();
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
      userMonitor.readyToInitialise();
    }
  }
  
  @Override
  public void onRequestActivityPhases( UUID senderID )
  { /*Not implemented in this test*/ }
  
  @Override
  public void onDiscoverMetricGenerators( UUID senderID )
  { /*Not implemented in this test*/ }
  
  @Override
  public void onDiscoveryTimeOut( UUID senderID )
  { /*Not implemented in this test*/ }
  
  @Override
  public void onSetStatusMonitorEndpoint( UUID senderID /* Data model under development */ )
  { /*Not implemented in this test*/ }
  
  // Runnable ------------------------------------------------------------------
  @Override
  public void run()
  {
    // Create interface factories for both provider and user
    EMInterfaceFactory providerFactory = new EMInterfaceFactory( providerChannel, true );
    EMInterfaceFactory userFactory     = new EMInterfaceFactory( userChannel, false );
    
    // Share a dispatch message pump
    dispatchPump = providerFactory.createDispatchPump( "Shared pump", 
                                                       IAMQPMessageDispatchPump.ePumpPriority.MINIMUM );
    
    // Create dispatchers for both provider and user
    IAMQPMessageDispatch providerDispatch = providerFactory.createDispatch();
    IAMQPMessageDispatch userDispatch     = userFactory.createDispatch();
    dispatchPump.addDispatch( providerDispatch );
    dispatchPump.addDispatch( userDispatch );
    dispatchPump.startPump();
    
    providerMonitor = providerFactory.createMonitor( ECCMonitorEntryPointTest.EMProviderUUID,
                                                     ECCMonitorEntryPointTest.EMUserUUID,
                                                     providerDispatch );
    
    userMonitor = userFactory.createMonitor( ECCMonitorEntryPointTest.EMProviderUUID,
                                             ECCMonitorEntryPointTest.EMUserUUID,
                                             userDispatch );
    
    // This class acts as BOTH provider and user
    providerMonitor.setProviderListener( this );
    userMonitor.setUserListener( this );
    
    // Start simulation from point of registration confirmation from provider
    providerMonitor.registrationConfirmed( true );
  }
}
