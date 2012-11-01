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
//      Created Date :          27-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecylePhases;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.IEMLiveMonitor;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMLiveMonitor_ProviderListener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces.EMLiveMonitor;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import java.util.*;




public class EMLiveMonitorPhase extends AbstractEMLCPhase
                                implements IEMLiveMonitor_ProviderListener
{
  private EMLiveMonitorPhaseListener phaseListener;
  
  private HashSet<UUID> clientPushGroup;
  private HashSet<UUID> clientPullGroup;
  
  private boolean       monitorStopping = false;
  private final Object  controlledStopLock = new Object();
  
  
  public EMLiveMonitorPhase( AMQPBasicChannel channel,
                             UUID providerID,
                             EMLiveMonitorPhaseListener listener )
  {
    super( EMPhase.eEMLiveMonitoring, channel, providerID );
    
    phaseListener = listener;
    
    clientPushGroup = new HashSet<UUID>();
    clientPullGroup = new HashSet<UUID>();
    
    phaseState = "Ready to start live monitor";
  }
  
  // AbstractEMLCPhase ---------------------------------------------------------
  @Override
  public void start() throws Exception
  {
    if ( phaseActive ) throw new Exception( "Phase already active" );
    if ( !hasClients() ) throw new Exception( "No clients available for this phase" );
    
    // Create the live monitor interface
    for ( EMClientEx client : getCopySetOfCurrentClients() )
    {
      AMQPMessageDispatch dispatch = new AMQPMessageDispatch();
      phaseMsgPump.addDispatch( dispatch );
      
      EMLiveMonitor face = new EMLiveMonitor( emChannel, dispatch,
                                              emProviderID, client.getID(), true );
      
      face.setProviderListener( this );
      client.setLiveMonitorInterface( face );
    }
    
    phaseMsgPump.startPump();
    phaseActive = true;
    
    // Request clients do the same
    for ( EMClientEx client: getCopySetOfCurrentClients() )
      client.getDiscoveryInterface().createInterface( EMInterfaceType.eEMLiveMonitor );
    
    phaseState = "Waiting for clients to signal ready to start monitoring";
  }
  
  @Override
  public void controlledStop() throws Exception
  {
    if ( phaseActive && !monitorStopping )
    {
      monitorStopping = true;
    
      // Get a copy of pushers and pullers
      HashSet<UUID> copyOfPushers = new HashSet<UUID>();
      HashSet<UUID> copyOfPullers = new HashSet<UUID>();
      synchronized ( controlledStopLock )
      {
        copyOfPushers.addAll( clientPushGroup );
        copyOfPullers.addAll( clientPullGroup );
      }

      // Stop pushing clients
      if ( !copyOfPushers.isEmpty() )
      {
        Iterator<UUID> pushIt = copyOfPushers.iterator();
        while ( pushIt.hasNext() )
        {
          EMClientEx client = getClient( pushIt.next() );
          client.getLiveMonitorInterface().stopPushing();
        }

        // We'll need to wait on confirmation of clients finishing their push
      }

      // Stop pulling clients
      if ( !copyOfPullers.isEmpty() )
      {
        Iterator<UUID> pullIt = copyOfPullers.iterator();
        while ( pullIt.hasNext() )
        {
          EMClientEx client = getClient( pullIt.next() );
          client.getLiveMonitorInterface().pullingStopped();
        }

        // We can clear the pulling list immediately
        synchronized( controlledStopLock )
        { clientPullGroup.clear(); }
      }
      
      // Now call a hard-stop
      hardStop();
      monitorStopping = false;
    }
    else throw new Exception( "Phase already stopped or inactive" );
  }
  
  @Override
  public void hardStop()
  {
    phaseMsgPump.stopPump();
    phaseActive = false;
    
    if ( phaseListener != null ) phaseListener.onLiveMonitorPhaseCompleted();
  }
  
  @Override
  public void timeOutClient( EMClientEx client ) throws Exception
  {
    // Safety first
    if ( client == null ) throw new Exception( "Could not time-out: client is null" );
    if ( !phaseActive )   throw new Exception( "Could not time-out: phase not active" );     
    
    // Check this client is registered with this phase first
    if ( isClientRegisteredInPhase(client) )
    {
      if ( client.isNotifiedOfTimeOut(EMPhaseTimeOut.eEMTOLiveMonitorTimeOut) ) 
        throw new Exception( "Time-out already sent to client" );
      
      if ( !client.isPullingMetricData() )
        throw new Exception( "Client is not currently generating pulled metric data" );
      
      // Time-out all currently pulling measurement sets
      IEMLiveMonitor monitor = client.getLiveMonitorInterface();
      
      Iterator<UUID> msIt = client.getCopyOfCurrentMeasurementSetPullIDs().iterator();
      while ( msIt.hasNext() )
      {
        UUID msID = msIt.next();
        client.removePullingMeasurementSetID( msID );
        monitor.pullMetricTimeOut( msID );
      }
      
      client.addTimeOutNotification( EMPhaseTimeOut.eEMTOLiveMonitorTimeOut );
    }
    else
      throw new Exception( "This client cannot be timed-out in Post-report phase" );
  }
  
  @Override
  public void onClientUnexpectedlyRemoved( EMClientEx client )
  {
    if ( client != null )
    {
      UUID clientID = client.getID();
      
      clientPushGroup.remove( clientID );
      clientPullGroup.remove( clientID );
      removeClient( clientID );
    }
  }
  
  // IEMLiveMonitor_ProviderListener -------------------------------------------
  @Override
  public void onNotifyReadyToPush( UUID senderID )
  {
    if ( phaseActive && !monitorStopping )
    {
      EMClientEx client = getClient( senderID );
      
      if ( client != null )
      {
        clientPushGroup.add( client.getID() );
        client.getLiveMonitorInterface().startPushing();
      }
    }
  }
  
  @Override
  public void onPushMetric( UUID senderID, Report report )
  {
    if ( phaseActive )
    {
      EMClientEx client = getClient( senderID );
      
      // Only allow clients who have declared they are going to push
      if ( client != null && clientPushGroup.contains( client.getID() ) )
      {
        if ( phaseListener != null )
          phaseListener.onGotMetricData( client, report );
        
        // Let client know we've received the data
        client.getLiveMonitorInterface().notifyPushReceived( report.getUUID() );
      }
    }
  }
  
  @Override
  public void onNotifyPushingCompleted( UUID senderID )
  {
    if ( phaseActive )
    {
      EMClientEx client = getClient( senderID );
      
      if ( client != null )
        clientPushGroup.remove( client.getID() );
    }
  }
  
  @Override
  public void onNotifyReadyForPull( UUID senderID )
  {
    if ( phaseActive && !monitorStopping )
    {
      EMClientEx client = getClient( senderID );
      
      if ( client != null )
        clientPullGroup.add( client.getID() );
    }
  }
  
  @Override
  public void onSendPulledMetric( UUID senderID, Report report )
  {
    if ( phaseActive )
    {
      EMClientEx client = getClient( senderID );
      
      // Only allow clients who have declared they are going to be pulled
      if ( client != null && report != null )
      {
        MeasurementSet mSet = report.getMeasurementSet();
        
        // Try to get data from report
        if ( mSet != null )
        {
          // Remove ID from current pulling set
          client.removePullingMeasurementSetID( mSet.getUUID() );

          // Notify listeners
          if ( phaseListener != null ) 
            phaseListener.onGotMetricData( client, report );
        }
        else phaseLogger.error( "Pulled report contained NULL MeasurementSet" );
        
        // Tell the client we got the report
        IEMLiveMonitor monitor = client.getLiveMonitorInterface();
        monitor.notifyPullReceived( report.getUUID() );
        
        // If there are outstanding metrics to pull, make another request (if we're not stopping)
        if ( !monitorStopping )
        {
          UUID nextMSID = client.getNextMeasurementSetIDToPull();
        
          if ( nextMSID != null ) monitor.pullMetric( nextMSID );
        }
        
      }
      else phaseLogger.error( "Could not process pulled metric: NULL client or report" );
    }
  }
}
