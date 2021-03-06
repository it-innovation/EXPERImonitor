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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;

import java.util.*;




public class EMLiveMonitorPhase extends AbstractEMLCPhase
                                implements IEMLiveMonitor_ProviderListener
{
  private final Object controlledStopLock = new Object();
  private final Object acceleratorLock    = new Object();
  
  private EMLiveMonitorPhaseListener phaseListener;

  private HashSet<UUID> clientPushGroup;
  private HashSet<UUID> clientPullGroup;
  
  private volatile boolean monitorStopping; // Atomic
  
  
  public EMLiveMonitorPhase( AMQPBasicChannel channel,
                             UUID providerID,
                             EMLiveMonitorPhaseListener listener )
  {
    super( EMPhase.eEMLiveMonitoring, channel, providerID );
    
    phaseListener   = listener;
    clientPushGroup = new HashSet<UUID>();
    clientPullGroup = new HashSet<UUID>();
    phaseMsgPump.startPump();
    
    phaseState = "Ready to start live monitor";
  }
  
  // AbstractEMLCPhase ---------------------------------------------------------
  @Override
  public void reset()
  {
    synchronized ( controlledStopLock )
    {
      clientPushGroup.clear();
      clientPullGroup.clear();
    }
    
    clearAllClients();
    
    phaseActive     = false;
    monitorStopping = false;
  }
  
  @Override
  public void start() throws Exception
  {
    if ( phaseActive ) throw new Exception( "Phase already active" );
    
    phaseActive     = true;
    monitorStopping = false;
    
    // Set up live monitoring clients
    synchronized ( acceleratorLock )
    {
      Set<EMClientEx> currClients   = getCopySetOfCurrentClients();
      Iterator<EMClientEx> clientIt = currClients.iterator();
      
      // Create the live monitoring interface
      while ( clientIt.hasNext() )
      {
        EMClientEx client = clientIt.next();
        client.setCurrentPhaseActivity( EMPhase.eEMLiveMonitoring );
        setupClientInterface( client );
      }
      
      // Request clients do the same
      for ( EMClientEx client: getCopySetOfCurrentClients() )
        client.getDiscoveryInterface().createInterface( EMInterfaceType.eEMLiveMonitor );
    } 
    
    phaseState = "Waiting for clients to signal ready to start monitoring";
    //... remainder of protocol implemented through events
  }
  
  @Override
  public void controlledStop()
  {
    if ( phaseActive && !monitorStopping )
    {
      // Do not de-active phase as clients will be stopping asynchronously
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
        synchronized ( controlledStopLock )
        { clientPullGroup.clear(); }
      }
    }
  }
  
  @Override
  public void hardStop()
  {
    monitorStopping = false;
    phaseActive     = false;
    phaseMsgPump.stopPump();
  }
  
  @Override
  public void setupClientInterface( EMClientEx client )
  {
    if ( client.getLiveMonitorInterface() == null )
    {
      AMQPMessageDispatch dispatch = new AMQPMessageDispatch();
      phaseMsgPump.addDispatch( dispatch );
      
      EMLiveMonitor face = new EMLiveMonitor( emChannel, dispatch,
                                              emProviderID, client.getID(), true );
      
      face.setProviderListener( this );
      client.setLiveMonitorInterface( face );
    }
  }
  
  @Override
  public void accelerateClient( EMClientEx client ) throws Exception
  {
    if ( !phaseActive ) throw new Exception( "Cannot accelerate client (live monitoring) - phase is not active" );
    if ( client == null ) throw new Exception( "Cannot accelerate client (live monitoring) - client is null" );
   
    synchronized ( acceleratorLock )
    {
      // Always update the client's current phase
      client.setCurrentPhaseActivity( EMPhase.eEMLiveMonitoring );
      
      // Only engage client if we're in focus and not trying to stop
      if ( phaseInFocus && !monitorStopping ) 
      {
        // Need to manually add/setup accelerated clients
        addClient( client );
        setupClientInterface( client );
      
        client.getDiscoveryInterface().createInterface( EMInterfaceType.eEMLiveMonitor );
      }
      else // Otherwise client is too late.. refer them out of this phase immediately
      {
        phaseListener.onLiveMonitorPhaseCompleted( client );
        phaseLogger.info( "Tried accelerating client (live monitoring) but phase is stopping/no longer in focus" );
      }
    }
  }
  
  @Override
  public void timeOutClient( EMClientEx client ) throws Exception
  {
    // Safety first
    if ( client == null ) throw new Exception( "Could not time-out: client is null" );
    
    if ( monitorStopping ) throw new Exception( "Could not time-out: monitoring phase is already stopping" );
    
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
  public void onClientHasBeenDeregistered( EMClientEx client )
  {
    if ( client != null )
    {
      UUID clientID = client.getID();
     
      synchronized ( controlledStopLock )
      {
				client.clearAllLiveMeasurementSetPulls();
				
        clientPushGroup.remove( clientID );
        clientPullGroup.remove( clientID );
				
        removeClient( clientID );
      }
    }
  }
  
  // IEMLiveMonitor_ProviderListener -------------------------------------------
  @Override
  public void onNotifyReadyToPush( UUID senderID )
  {
    if ( !monitorStopping )
    {
      EMClientEx client = getClient( senderID );
      
      if ( client != null )
      {
        client.setIsPushCapable( true );
        
        synchronized ( controlledStopLock )
        { clientPushGroup.add( client.getID() ); }
        
        // Notify of client behaviour
        phaseListener.onClientDeclaredCanPush( client );
        
        // Tell client to start pushing
        client.getLiveMonitorInterface().startPushing();
      }
    }
  }
  
  @Override
  public void onPushMetric( UUID senderID, Report report )
  {
    EMClientEx client = getClient( senderID );

    if ( client != null && report != null )
    {
      // Make sure we don't have a null measurement set
      MeasurementSet mSet = report.getMeasurementSet();
      if ( mSet != null )
      {
        // Only allow clients who have declared they are going to push
        boolean clientInPushGroup;

        synchronized ( controlledStopLock )
        { clientInPushGroup = clientPushGroup.contains( client.getID() ); }

        if ( clientInPushGroup )
        {
          // Make sure we have measurement set info for this client
          EMMeasurementSetInfo msInfo = client.getMSInfo( report.getMeasurementSet().getID() );
          if ( msInfo != null )
          {
            // Pass on data if 'LIVE' (ignore 'Enabled' status - the client is pushing this data, so their responsibility)
            if ( msInfo.isLiveEnabled() )
            {
              // Fill-in MS semantics & notify listeners if all well
              if ( fillInMSSemantics(client, mSet) )
              {
                if ( phaseListener != null ) 
                  phaseListener.onGotMetricData( client, report );
              }
              else
                phaseLogger.error( "Could not find MeasurementSet meta-data for client: " + client.getName() );

            }
            // Warn that we have dropped this data
            else phaseLogger.warn( "Got metric push from client, but dropped the data - Entity is not LIVE" );
          }
          else phaseLogger.error( "Got metric push from client, but cannot find measurement set info" );
        }
        else phaseLogger.warn( "Got metric push from client that has not declared pushing: data not recorded" );
      }
      else
        phaseLogger.error( "Got push from client but MeasurementSet is null" );
     
      // Let client know we've received the data (whatever the outcome was this side)
      client.getLiveMonitorInterface().notifyPushReceived( report.getUUID() );
    }
    else phaseLogger.error( "Could not process pushed metric: NULL client or report" ); 
  }
  
  @Override
  public void onPushPROVStatement( UUID senderID, EDMProvReport statement )
  {
    EMClientEx client = getClient( senderID );
    
    if ( client != null && statement != null )
    {
      // Only allow clients who have declared they are going to push
      boolean clientInPushGroup;
    
      synchronized ( controlledStopLock )
      { clientInPushGroup = clientPushGroup.contains( client.getID() ); }
    
      if ( clientInPushGroup )
      {
        // Check we're not sending rubbish towards the EDM
        if ( statement != null )
          phaseListener.onGotPROVData( client, statement );
      }
      else phaseLogger.error( "Got PROV push from client, but client has not declared as pushable" );
      
      // Let client know we've received the data (whatever the outcome was this side)
      client.getLiveMonitorInterface().notifyPushReceived( statement.getID() );
    }
  }
  
  @Override
  public void onNotifyPushingCompleted( UUID senderID )
  {
    EMClientEx client = getClient( senderID );

    if ( client != null )
      synchronized ( controlledStopLock )
      { clientPushGroup.remove( client.getID() ); }
  }
  
  @Override
  public void onNotifyReadyForPull( UUID senderID )
  {
    if ( !monitorStopping )
    {
      EMClientEx client = getClient( senderID );
      
      if ( client != null )
      {
        client.setIsPullCapable( true );
        
        synchronized ( controlledStopLock )
        { clientPullGroup.add( client.getID() ); }
        
        // Notify of client behaviour
        phaseListener.onClientDeclaredCanBePulled( client );
      }
    }
  }
  
  @Override
  public void onSendPulledMetric( UUID senderID, Report report )
  {
    EMClientEx client = getClient( senderID );

    // Only allow clients who have declared they are going to be pulled
    if ( client != null && report != null )
    {
      // Make sure we don't have a null measurement set
      MeasurementSet mSet = report.getMeasurementSet();
      if ( mSet != null )
      {
        // Make sure we have measurement set info for this client
        EMMeasurementSetInfo msInfo = client.getMSInfo( report.getMeasurementSet().getID() );
        if ( msInfo != null )
        { 
          // Pass on data if 'LIVE' and 'ENABLED'
          if ( msInfo.isLiveEnabled() && msInfo.isEntityEnabled() )
          {
            // Fill-in MS semantics & notify listeners if all well
            if ( fillInMSSemantics(client, mSet) )
            {
              if ( phaseListener != null ) 
                phaseListener.onGotMetricData( client, report );
            }
            else
              phaseLogger.error( "Could not find MeasurementSet meta-data for client: " + client.getName() );

            // Update the pull status for this client, irrespective of good/bad data
            UUID msID = mSet.getID();

            client.updatePullReceivedForMS( msID );
            client.removePullingMeasurementSetID( msID );
            
            // If there are outstanding metrics to pull, make another request (if we're not stopping)
            if ( !monitorStopping )
            {
              IEMLiveMonitor monitor = client.getLiveMonitorInterface();
              
              UUID nextMSID = client.iterateNextValidMSToBePulled();
              if ( nextMSID != null ) monitor.pullMetric( nextMSID );
            }
          }
          // Warn that we have dropped this data
          else phaseLogger.warn( "Got metric pull from client, but dropped the data - Entity is no longer LIVE" );
        }
        else phaseLogger.error( "Got metric pull from client, but cannot find measurement set info" ); 
      }
      else phaseLogger.error( "Got metric pull, but MeasurementSet is null" );
      
      // Tell the client we got the report (whatever the outcome was on this side)
      IEMLiveMonitor monitor = client.getLiveMonitorInterface();
      monitor.notifyPullReceived( report.getUUID() );
    }
    else phaseLogger.error( "Could not process pulled metric: NULL client or report" ); 
  }
  
  // Private methods -----------------------------------------------------------
  private boolean fillInMSSemantics( EMClientEx client, MeasurementSet msOUT )
  {
    boolean result = false;
    
    final UUID msID               = msOUT.getID();
    final MeasurementSet clientMS = client.getMSInfo( msID ).getMeasurementSet();
    
    if ( clientMS != null )
    {
      msOUT.setAttributeUUID( clientMS.getAttributeID() );
      msOUT.setMetricGroupUUID( clientMS.getMetricGroupID() );
      msOUT.setMetric( clientMS.getMetric() );
      
      result = true;
    }
    
    return result;
  }
}
