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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecylePhases;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.logging.spec.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.IAMQPMessageDispatchPump;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;

import java.util.*;




public abstract class AbstractEMLCPhase
{
  private HashMap<UUID, EMClientEx> phaseClients;
  private final Object              clientLock = new Object();
  
  protected final IECCLogger phaseLogger = Logger.getLogger( AbstractEMLCPhase.class );
  protected EMPhase          phaseType;
  protected String           phaseState;
  protected boolean          phaseSupportsTimeOuts = true;
 
  protected AMQPBasicChannel        emChannel;
  protected UUID                    emProviderID;
  protected AMQPMessageDispatchPump phaseMsgPump;
  
  protected volatile boolean phaseActive  = false; // Atomics
  protected volatile boolean phaseInFocus = false;
  
  public EMPhase getPhaseType()
  { return phaseType; }
  
  public String getState()
  { return phaseState; }
  
  public boolean isActive()
  { return phaseActive; }
  
  public boolean isInFocus()
  { return phaseInFocus; }
  
  public void setInFocus( boolean focus )
  { phaseInFocus = focus; }
  
  public boolean isTimeOutSupported()
  { return phaseSupportsTimeOuts; }
  
  public EMClientEx getClient( UUID id )
  { return phaseClients.get( id ); }
  
  public boolean hasClients()
  {
    boolean result = false;
    
    synchronized( clientLock )
    { result = !phaseClients.isEmpty(); }
    
    return result;
  }
  
  public Set<EMClientEx> getCopySetOfCurrentClients()
  {
    HashSet<EMClientEx> clientSet = new HashSet<EMClientEx>();
    
    synchronized ( clientLock )
    { clientSet.addAll( phaseClients.values() ); }
    
    return clientSet;
  }
  
  // Deriving classes must implement phase start/stopping behaviour ------------
  public abstract void reset();
  
  public abstract void start() throws Exception;
  
  public abstract void controlledStop();
  
  public abstract void hardStop();
  
  public abstract void setupClientInterface( EMClientEx client );
  
  public abstract void accelerateClient( EMClientEx client ) throws Exception;
  
  public abstract void timeOutClient( EMClientEx client ) throws Exception;
  
  public abstract void onClientHasBeenDeregistered( EMClientEx client );
  
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
  }
  
  protected boolean addClient( EMClientEx client )
  {
    // Safety first
    if ( client == null ) return false;
    
    boolean result = false;
    
    synchronized ( clientLock )
    {
      if ( !phaseClients.containsKey(client.getID()) )
      {
        phaseClients.put( client.getID(), client );
        result = true;
      }
    }
    
    return result;
  }
  
  protected boolean removeClient( UUID id )
  {
    if ( id == null ) return false;
    
    boolean result = false;
    
    synchronized ( clientLock )
    {
      if ( phaseClients.remove( id ) != null )
        result = true;
    }
   
    return result;
  }
  
  protected void clearAllClients()
  {
    synchronized (clientLock)
    { phaseClients.clear(); }
  }
  
  protected boolean isClientRegisteredInPhase( EMClientEx client )
  {
    if ( client == null ) return false;
    if ( getClient(client.getID()) == null ) return false;
    
    return true;
  }
  
  @Override
  protected void finalize() throws Throwable
  {
    phaseMsgPump.stopPump();
  }
}
