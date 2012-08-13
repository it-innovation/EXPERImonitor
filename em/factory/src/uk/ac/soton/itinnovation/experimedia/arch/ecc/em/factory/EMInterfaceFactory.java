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
//      Created Date :          05-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces.*;

import java.util.UUID;




public class EMInterfaceFactory
{
  private AMQPBasicChannel amqpChannel;
  private boolean          generateProviders ;
  
  
  public EMInterfaceFactory( AMQPBasicChannel channel, boolean createProviders )
  {
    amqpChannel       = channel;
    generateProviders = createProviders;
  }
  
  public IAMQPMessageDispatchPump createDispatchPump( String name,
                                                      IAMQPMessageDispatchPump.ePumpPriority priority )
  {
    return new AMQPMessageDispatchPump( name, priority );
  }
  
  public IAMQPMessageDispatch createDispatch()
  {
    return new AMQPMessageDispatch();
  }
  
  public IEMMonitorEntryPoint createEntryPoint( UUID providerID,
                                                IAMQPMessageDispatch dispatch )
  {
    return new EMMonitorEntryPoint( amqpChannel,
                                    (AMQPMessageDispatch) dispatch,
                                    providerID, 
                                    generateProviders );
  }
  
  public IEMMonitor createMonitor( UUID providerID,
                                   UUID userID,
                                   IAMQPMessageDispatch dispatch )
  {
    return new EMMonitor( amqpChannel,
                          (AMQPMessageDispatch) dispatch,
                          providerID,
                          userID,
                          generateProviders );
  }
  
  public IEMTest createTest( UUID providerID,
                             UUID userID,
                             IAMQPMessageDispatch dispatch )
  {
    return new EMTest( amqpChannel,
                       (AMQPMessageDispatch) dispatch,
                       providerID, 
                       userID, 
                       generateProviders );
  }
}
