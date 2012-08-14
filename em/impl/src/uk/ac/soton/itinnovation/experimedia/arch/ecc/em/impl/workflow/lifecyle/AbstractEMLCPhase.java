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
//      Created Date :          14-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecyle;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.IAMQPMessageDispatchPump;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.dataModel.EMClient;

import java.util.*;





abstract class AbstractEMLCPhase
{
  protected String                  phaseName;
  protected String                  phaseState;
  protected HashMap<UUID, EMClient> phaseClients;
  
  protected AMQPBasicChannel        emChannel;
  protected UUID                    emProviderID;
  protected AMQPMessageDispatchPump phaseMsgPump;
  
  protected boolean phaseActive = false;
  
  
  protected AbstractEMLCPhase( String name,
                               AMQPBasicChannel channel,
                               UUID providerID )
  {
    phaseName = name;
    emChannel = channel;
    emProviderID = providerID;
    
    phaseMsgPump =
            new AMQPMessageDispatchPump( name + " message pump",
                                         IAMQPMessageDispatchPump.ePumpPriority.NORMAL );
    
    phaseMsgPump.startPump();
  }
  
  protected String getName()
  { return phaseName; }
  
  protected String getState()
  { return phaseState; }
  
  protected boolean addClient( EMClient client )
  {
    if ( client == null ) return false;
    if ( phaseClients.containsKey(client.getID()) ) return false;
    if ( phaseActive ) return false;
    
    phaseClients.put( client.getID(), client );
    return true;
  }
  
  protected boolean removeClient( UUID id )
  {
    if ( id == null ) return false;
    if ( !phaseClients.containsKey(id) ) return false;
    if ( phaseActive ) return false;
    
    phaseClients.remove( id );
    return true;
  }
  
  protected Set<EMClient> getCopySetOfCurrentClients()
  {
    HashSet<EMClient> clientSet = new HashSet<EMClient>();
    clientSet.addAll( phaseClients.values() );
    
    return clientSet;
  }
  
  // Deriving classes must implement phase start/stopping behaviour ------------
  protected abstract void start() throws Exception;
  
  protected abstract void stop() throws Exception;
}
