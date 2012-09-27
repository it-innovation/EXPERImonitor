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
//      Created Date :          13-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.spec.IAMQPMessageDispatchPump;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMMonitorEntryPoint_ProviderListener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces.EMMonitorEntryPoint;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import java.util.*;






public class EMConnectionManager implements IEMMonitorEntryPoint_ProviderListener
{
  private UUID                    entryPointID;
  private EMMonitorEntryPoint     entryPointInterface;
  private AMQPMessageDispatchPump entryPointPump;
  
  private boolean entryPointOpen = false;
  
  private HashMap<UUID, EMClientEx>   connectedClients;
  private EMConnectionManagerListener connectionListener;
  
  
  public EMConnectionManager()
  {
    connectedClients    = new HashMap<UUID, EMClientEx>();
  }
  
  public boolean initialise( UUID epID, 
                             AMQPBasicChannel channel )
  {
    if ( !entryPointOpen )
    {
      if ( epID != null && channel != null )
      {
        entryPointID    = epID;

        AMQPMessageDispatch dispatch = new AMQPMessageDispatch();

        entryPointPump = new AMQPMessageDispatchPump( "EM Entry Point pump",
                                                      IAMQPMessageDispatchPump.ePumpPriority.MINIMUM );

        entryPointPump.addDispatch( dispatch );

        entryPointPump.startPump();

        entryPointInterface = new EMMonitorEntryPoint( channel,
                                                       dispatch,
                                                       entryPointID,
                                                       true );

        entryPointInterface.setListener( this );

        entryPointOpen = true;
      }
    }
    
    return entryPointOpen;
  }
  
  public void setListener( EMConnectionManagerListener listener )
  { connectionListener = listener; }
  
  public boolean isEntryPointOpen()
  { return entryPointOpen; }
  
  public void disconnectClients()
  { entryPointPump.stopPump(); }
  
  public int getConnectedClientCount()
  { return connectedClients.size(); }
  
  public EMClientEx getClient( UUID clientID )
  { return connectedClients.get( clientID ); }
  
  public void removeClient( UUID clientID )
  { connectedClients.remove( clientID ); }
  
  public Set<EMClientEx> getConnectedClients()
  {
    HashSet<EMClientEx> currClients = new HashSet<EMClientEx>();
    currClients.addAll( connectedClients.values() );
    
    return currClients;
  }
  
  // IEMMonitorEntryPoint_ProviderListener -------------------------------------
  @Override
  public void onRegisterAsEMClient( UUID userID, String userName )
  {
    if ( userID != null && userName != null )
      if ( !connectedClients.containsKey(userID) )
      {
        EMClientEx client = new EMClientEx( userID, userName );
        connectedClients.put( userID, client );
        
        connectionListener.onClientRegistered( client );
      }
  }
}
