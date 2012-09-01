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
//      Created Date :          01-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.em.test.v1.eccEntryPoint;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;

import java.util.UUID;




/**
 * ECCMonitorEntryPointTestExecutor runs the entry-point tests. This class acts
 * _both_ as the USER (or client of the EM) and as the PROVIDER (or EM itself).
 * This makes testing the behaviour of both sides of the bus considerably easier.
 * 
 * The executor will listen to users of the IECCMonitorEntryPoint interfaces by
 * implementing the IECCMonitorEntryPoint_UserListener interface
 * 
 * @author sgc
 */
public class ECCMonitorEntryPointTestExecutor implements Runnable,
                                                         IEMMonitorEntryPoint_ProviderListener
{
  private AMQPBasicChannel providerChannel;
  private AMQPBasicChannel userChannel;
  
  IAMQPMessageDispatchPump providerPump;
  IAMQPMessageDispatchPump userPump;
  
  IEMMonitorEntryPoint providerEP;
  IEMMonitorEntryPoint userEP;
  
  private boolean gotClientRegistration = false;
  
  
  public ECCMonitorEntryPointTestExecutor( AMQPBasicChannel provider,
                                           AMQPBasicChannel user )
  {
    providerChannel = provider;
    userChannel     = user;
  }
  
  public boolean getTestResult()
  {
    if ( gotClientRegistration ) System.out.println( "ECCMonitorEntryPointTest is GOOD." );
    
    return gotClientRegistration; 
  }
  
  // IECCMonitorEntryPoint_ProviderListener
  @Override
  public void onRegisterAsEMClient( UUID userID, String userName )
  {
    // Check we got a registration event with the correct user ID and name
    if ( userID.equals(ECCMonitorEntryPointTest.EMUserUUID) && 
         userName.equals("EM Entry Point Test User") )
      gotClientRegistration = true;
  }
  
  // Runnable ------------------------------------------------------------------
  @Override
  public void run()
  {
    // Need two separate factories to create PROVIDER and USER interfaces
    // Provider factory
    EMInterfaceFactory providerFactory = new EMInterfaceFactory( providerChannel, true );
    
    // User factory
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
    
    // Set up the provider interface (and listen for in-coming user connections)
    providerEP = providerFactory.createEntryPoint( ECCMonitorEntryPointTest.EMProviderUUID,
                                                   providerDispatch );
    providerEP.setListener( this );
    
    // Create user interface and try to connect to the provider
    userEP = userFactory.createEntryPoint( ECCMonitorEntryPointTest.EMProviderUUID,
                                           userDispatch );
    
    // Start by trying to register as EM client
    userEP.registerAsEMClient( ECCMonitorEntryPointTest.EMUserUUID, "EM Entry Point Test User" );
  }
}
