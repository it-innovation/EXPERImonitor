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
//      Created Date :          02-Feb-2013
//      Created for Project :   ECC Dash
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.processors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.IExperimentMonitor;
import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.types.UFAbstractEventManager;




public class LiveMetricScheduler extends UFAbstractEventManager
{
  private final Logger metLogger  = Logger.getLogger( LiveMetricScheduler.class );
  private final int pullSpeed     = 2000;
  private final Object clientLock = new Object();
  
  private IExperimentMonitor            expMonitor;
  private HashMap<UUID, MetricPullTask> liveClients;
  
  private Timer            scheduler;
  private volatile boolean isScheduling;
  
  public LiveMetricScheduler()
  {
    liveClients = new HashMap<UUID, MetricPullTask>();
    scheduler   = new Timer();
  }
  
  public void start( IExperimentMonitor monitor )
  {
    expMonitor = monitor;
    isScheduling = true;
  }
  
  public void stop()
  {
    scheduler.cancel();    
    isScheduling = false;
  }
  
  public void reset()
  {
    liveClients.clear();
    scheduler.purge();
    isScheduling = false;
  }
  
  public void shutDown()
  {
    stop();
    scheduler = null;
  }
  
  public void addClient( EMClient client ) throws Exception
  {
    if ( client == null ) throw new Exception( "Client is null" );
 
    UUID clientID = client.getID();   
    if ( liveClients.containsKey(clientID) ) throw new Exception( "Already scheduling client" );
    
    Set<MetricGenerator> mgSet = client.getCopyOfMetricGenerators();
    if ( mgSet.isEmpty() ) throw new Exception( "Client has no metric generators" );
    
    synchronized( clientLock )
    {
      MetricPullTask mpt = new MetricPullTask(client);
      scheduler.scheduleAtFixedRate( mpt, pullSpeed, pullSpeed );
      
      liveClients.put( clientID, mpt ); 
    }
  }
  
  public void removeClient( EMClient client ) throws Exception
  {
    if ( client == null ) throw new Exception( "Client is null" );
 
    UUID clientID = client.getID();   
    if ( !liveClients.containsKey(clientID) ) throw new Exception( "Client is not scheduled" );
    
    synchronized( clientLock )
    { 
      MetricPullTask mpt = liveClients.get( clientID );
      if ( mpt != null ) mpt.cancel();
      
      liveClients.remove( clientID );
    }
  }
  
  // Private methods -----------------------------------------------------------
  private synchronized void tryPullClientMetrics( EMClient client )
  {
    // Only make the pull if we're scheduling and the client is not busy
    if ( isScheduling && !client.isPullingMetricData() )
      if ( expMonitor != null )
        try
        { 
          expMonitor.pullAllMetrics( client );
          
          // Notify listener
          Collection<LiveMetricSchedulerListener> listeners = getListenersByType();
          for( LiveMetricSchedulerListener listener : listeners )
            listener.onIssuedClientMetricPull( client );
        }
        catch (Exception e )
        {          
          // Notify listener
          Collection<LiveMetricSchedulerListener> listeners = getListenersByType();
          for( LiveMetricSchedulerListener listener : listeners )
            listener.onPullMetricFailed(client, e.getMessage() );
        }
  }
  
  // Private classes -----------------------------------------------------------
  private class MetricPullTask extends TimerTask
  {
    private EMClient taskClient;
    
    public MetricPullTask( EMClient client )
    { 
      super();
      taskClient = client;
    }
    
    @Override
    public void run()
    { tryPullClientMetrics( taskClient ); }
  }
}
