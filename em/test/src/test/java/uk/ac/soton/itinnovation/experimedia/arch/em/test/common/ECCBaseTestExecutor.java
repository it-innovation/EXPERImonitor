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
//      Created Date :          13-Dec-2012
//      Created for Project :   experimedia-arch-ecc-em-test
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.em.test.common;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;




public abstract class ECCBaseTestExecutor
{
  private TestEventListener testListener;
  
  protected IECCLogger exeLogger = Logger.getLogger( ECCBaseTestExecutor.class );
  
  protected EMInterfaceFactory providerFactory;
  protected EMInterfaceFactory userFactory;
  
  protected AMQPBasicChannel providerChannel;
  protected AMQPBasicChannel userChannel;
  
  // Main pumps and dispatches
  protected IAMQPMessageDispatchPump providerPump;
  protected IAMQPMessageDispatch     providerDispatch;
  
  protected IAMQPMessageDispatchPump userPump;
  protected IAMQPMessageDispatch     userDispatch;
  
  
  // Deriving classes to implement ---------------------------------------------
  public abstract boolean getTestResult();
  
  
  // Protected methods ---------------------------------------------------------
  protected synchronized void notifyTestEnds( String testName )
  {
    exeLogger.info( "Test ended: " + testName );
    testListener.onTestCompleted();
  }
  
  protected ECCBaseTestExecutor( TestEventListener listener,
                                 AMQPBasicChannel provider,
                                 AMQPBasicChannel user )
  {
    testListener    = listener;
    providerChannel = provider;
    userChannel     = user;
  }
  
  protected void initialiseDispatches( IAMQPMessageDispatchPump.ePumpPriority priority ) 
                                     throws Exception
  {
    if ( providerChannel == null || userChannel == null )
      throw new Exception( "Provider/user AMQP channels are null" );
    
    // Create interface factories for both provider and user
    providerFactory = new EMInterfaceFactory( providerChannel, true );
    userFactory     = new EMInterfaceFactory( userChannel, false );
    
    // Create pump/dispatchers for provider
    providerPump = providerFactory.createDispatchPump( "Provider pump", 
                                                       IAMQPMessageDispatchPump.ePumpPriority.MINIMUM );
    
    providerDispatch = providerFactory.createDispatch();
    providerPump.addDispatch( providerDispatch );
    providerPump.startPump();
    
    // Create pump/dispatchers for user
    userPump = providerFactory.createDispatchPump( "User pump", 
                                                   IAMQPMessageDispatchPump.ePumpPriority.MINIMUM );
    
    userDispatch = userFactory.createDispatch();
    userPump.addDispatch( userDispatch );
    userPump.startPump();
  }
}
