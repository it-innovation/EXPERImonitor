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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;

import uk.ac.soton.itinnovation.experimedia.arch.em.test.common.*;
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
 * (8) After receipt of (7) the test is completed and successful
 * 
 * @author sgc
 */
public class ECCMonitorTestExecutor extends ECCBaseTestExecutor
                                    implements Runnable,
                                               IEMDiscovery_ProviderListener,
                                               IEMDiscovery_UserListener,
                                               IEMTest_Listener
        
{
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
  
  
  public ECCMonitorTestExecutor( TestEventListener listener,
                                 AMQPBasicChannel provider,
                                 AMQPBasicChannel user )
  { super( listener, provider, user ); }
  
  @Override
  public boolean getTestResult()
  {
    return ( userGotRegistrationConfirmed == true &&
             providerGotReadyToInit       == true && 
             userGotCreateTestFaceCommand == true &&
             providerGotTestBytes         == true );
  }
  
  // IECCTest_Listener ---------------------------------------------------------
  @Override
  public void onReceivedData( UUID senderID, int byteCount, byte[] dataBody )
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
    
    notifyTestEnds( "ECC Monitor Test Execution" );
  }
  
  // IEMDiscovery_ProviderListener ---------------------------------------------
  @Override
  public void onReadyToInitialise( UUID senderID )
  {    
    // Make sure event is from the correct sender
    if ( senderID.equals(ECCMonitorEntryPointTest.EMUserUUID) )
    {
      providerGotReadyToInit = true;
    
      // Create dispatcher & attach dispatcher to (shared) pump
      IAMQPMessageDispatch providerTestDispatch = providerFactory.createDispatch();
      providerPump.addDispatch( providerTestDispatch );

      providerTest = providerFactory.createTest( ECCMonitorEntryPointTest.EMProviderUUID,
                                                 ECCMonitorEntryPointTest.EMUserUUID,
                                                 providerTestDispatch );

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
  public void onEnableEntityMetricCollection( UUID senderID,
                                              UUID entityID, 
                                              boolean enabled )
  { /*Not implemented in this test*/ }
  
  @Override
  public void onClientDisconnecting( UUID senderID )
  { /*Not implemented in this test*/ }
  
  // IEMDiscovery_UserListener -------------------------------------------------
  @Override
  public void onCreateInterface( UUID senderID, EMInterfaceType type )
  {
    // Make sure event is from the correct sender
    if ( senderID.equals(ECCMonitorEntryPointTest.EMProviderUUID) )
    {
      if ( type == EMInterfaceType.eEMTestInterface )
      userGotCreateTestFaceCommand = true;
      
      // Create dispatcher & attach dispatcher to (shared) pump
      IAMQPMessageDispatch userTestDispatch = userFactory.createDispatch();
      userPump.addDispatch( userTestDispatch );

      userTest = userFactory.createTest( ECCMonitorEntryPointTest.EMProviderUUID,
                                         ECCMonitorEntryPointTest.EMUserUUID,
                                         userTestDispatch );

      // Create some test data
      testDataSize = 2048;
      testDataBody = new byte[testDataSize];
      Random rand = new Random();
      rand.nextBytes( testDataBody );

      // Send the data to the provider
      userTest.sendData( ECCMonitorEntryPointTest.EMUserUUID,
                         testDataSize, 
                         testDataBody );
    }
  }
  
  @Override
  public void onRegistrationConfirmed( UUID    senderID,
                                       Boolean confirmed,
                                       UUID    expUniqueID,
                                       String  expNamedID,
                                       String  expName,
                                       String  expDescription,
                                       Date    expCreateTime )
  {
    // Check the experiment info is correct
    boolean expInfoOK = ( expUniqueID.equals(ECCMonitorEntryPointTest.EMExperimentUUID)    &&
                          expNamedID.equals(ECCMonitorEntryPointTest.EMExperimentNamedID)  &&
                          expName.equals(ECCMonitorEntryPointTest.EMExperimentName)        &&
                          expDescription.equals(ECCMonitorEntryPointTest.EMExperimentDesc) &&
                          expCreateTime.equals(ECCMonitorEntryPointTest.EMStartDate) );
    
    // Make sure event is from the correct sender
    if ( senderID.equals( ECCMonitorEntryPointTest.EMProviderUUID) && expInfoOK )
    {
      userGotRegistrationConfirmed = true; // Don't really care about the result
    
      // Send provider notice that user is ready to initialise
      userDiscovery.readyToInitialise();
    }
  }
  
  @Override
  public void onDeregisteringThisClient( UUID senderID, String reason )
  { /* Not implemented in this test */ }
  
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
    boolean pumpsOK = false;
    try
    { 
      initialiseDispatches( IAMQPMessageDispatchPump.ePumpPriority.MINIMUM );
      pumpsOK = true;
    }
    catch ( Exception e )
    { exeLogger.error( "Test initialisation problem: " + e.getMessage() ); }
    
    if ( pumpsOK )
    {
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
      providerDiscovery.registrationConfirmed( true,
                                               ECCMonitorEntryPointTest.EMExperimentUUID,
                                               ECCMonitorEntryPointTest.EMExperimentNamedID,
                                               ECCMonitorEntryPointTest.EMExperimentName,
                                               ECCMonitorEntryPointTest.EMExperimentDesc,
                                               ECCMonitorEntryPointTest.EMStartDate );
    }
  }
}
