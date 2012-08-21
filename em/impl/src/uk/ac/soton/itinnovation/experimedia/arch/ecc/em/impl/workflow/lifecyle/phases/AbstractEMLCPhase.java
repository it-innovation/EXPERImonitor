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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecyle.phases;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.IAMQPMessageDispatchPump;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;

import java.util.*;





public abstract class AbstractEMLCPhase
{
  protected EMPhase                   phaseType;
  protected String                    phaseState;
  protected HashMap<UUID, EMClientEx> phaseClients;
  
  protected AMQPBasicChannel        emChannel;
  protected UUID                    emProviderID;
  protected AMQPMessageDispatchPump phaseMsgPump;
  
  protected boolean phaseActive = false;

  
  public EMPhase getPhaseType()
  { return phaseType; }
  
  public String getState()
  { return phaseState; }
  
  public boolean addClient( EMClientEx client )
  {
    if ( client == null ) return false;
    if ( phaseActive    ) return false;
    
    if ( phaseClients.containsKey(client.getID()) ) return true;
    
    phaseClients.put( client.getID(), client );
    return true;
  }
  
  public boolean removeClient( UUID id )
  {
    if ( id == null ) return false;
    if ( !phaseClients.containsKey(id) ) return false;
    if ( phaseActive ) return false;
    
    phaseClients.remove( id );
    return true;
  }
  
  public Set<EMClientEx> getCopySetOfCurrentClients()
  {
    HashSet<EMClientEx> clientSet = new HashSet<EMClientEx>();
    clientSet.addAll( phaseClients.values() );
    
    return clientSet;
  }
  
  // Deriving classes must implement phase start/stopping behaviour ------------
  public abstract void start() throws Exception;
  
  public abstract void stop() throws Exception;
  
  // Protected methods ---------------------------------------------------------
  protected AbstractEMLCPhase( EMPhase phase,
                               AMQPBasicChannel channel,
                               UUID providerID )
  {
    phaseType    = phase;
    emChannel    = channel;
    emProviderID = providerID;
    phaseClients = new HashMap<UUID, EMClientEx>();
    
    phaseMsgPump =
            new AMQPMessageDispatchPump( phaseType + " message pump",
                                         IAMQPMessageDispatchPump.ePumpPriority.MINIMUM );
    
    phaseMsgPump.startPump();
  }
}