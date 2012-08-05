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
//      Created By :            sgc
//      Created Date :          05-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.base.*;

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
  
  public IECCMonitorEntryPoint createEntryPoint( UUID providerID )
  {
    return new ECCMonitorEntryPoint( amqpChannel, providerID, generateProviders );
  }
  
  public IECCMonitor createMonitor( UUID providerID, UUID userID )
  {
    return new ECCMonitor( amqpChannel, providerID, userID, generateProviders );
  }
  
  public IECCTest createTest( UUID providerID, UUID userID )
  {
    return new ECCTest( amqpChannel, providerID, userID, generateProviders );
  }
}
