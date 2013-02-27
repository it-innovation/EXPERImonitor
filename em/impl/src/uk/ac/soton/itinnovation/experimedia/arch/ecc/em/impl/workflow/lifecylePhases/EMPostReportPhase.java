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
//      Created Date :          29-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecylePhases;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.listeners.IEMPostReport_ProviderListener;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.faces.EMPostReport;

import java.util.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMDataBatchEx;




public class EMPostReportPhase extends AbstractEMLCPhase
                               implements IEMPostReport_ProviderListener
{
  private final Object controlledStopLock = new Object();
  private final Object acceleratorLock    = new Object();
  
  private EMPostReportPhaseListener  phaseListener;
  
  private HashMap<UUID, EMClientEx>  stoppingReportClients;
  private TreeMap<Date, Measurement> batchDateTree;
  
  private volatile boolean reportingStopping; // Atomic
  
  
  public EMPostReportPhase( AMQPBasicChannel channel,
                            UUID providerID,
                            EMPostReportPhaseListener listener )
  {
    super( EMPhase.eEMPostMonitoringReport, channel, providerID );
    
    phaseListener = listener;
    
    stoppingReportClients = new HashMap<UUID, EMClientEx>();
    batchDateTree         = new TreeMap<Date, Measurement>();
    
    phaseMsgPump.startPump();
    
    phaseState = "Ready to start post-report process";
  }
  
  // AbstractEMLCPhase ---------------------------------------------------------
  @Override
  public void reset()
  {
    phaseActive       = false;
    reportingStopping = false;
    clearAllClients();
    
    batchDateTree.clear();
  }
  
  @Override
  public void start() throws Exception
  {
    if ( phaseActive ) throw new Exception( "Phase already active" );
    if ( !hasClients() ) throw new Exception( "No clients available for this phase" );
    
    phaseActive       = true;
    reportingStopping = false;
    
    // Create post-reporting interfaces
    synchronized ( acceleratorLock )
    {
      Set<EMClientEx> currClients   = getCopySetOfCurrentClients();
      Iterator<EMClientEx> clientIt = currClients.iterator();
      
      while ( clientIt.hasNext() )
      {
        EMClientEx client = clientIt.next();
        client.setCurrentPhaseActivity( EMPhase.eEMPostMonitoringReport );
        setupClientInterface( client );
      }
      
      // Request clients create post-report interface
      clientIt = currClients.iterator();
      while ( clientIt.hasNext() )
      {
        EMClientEx client = clientIt.next();
        client.getDiscoveryInterface().createInterface( EMInterfaceType.eEMPostReport );
      }
    }
    
    phaseState = "Waiting for client to signal ready to report";
    //... remainder of protocol implemented through events
  }
  
  @Override
  public void controlledStop() throws Exception
  {
    // Only engage client if we're actually active and not trying to stop
    if ( phaseActive && !reportingStopping )
    {
      synchronized ( controlledStopLock )
      {
        phaseActive       = false;
        reportingStopping = true;
        
        // Get a copy of all the clients currently trying to report
        Iterator<EMClientEx> clientIt = getCopySetOfCurrentClients().iterator();
        while ( clientIt.hasNext() )
        {
          EMClientEx client = clientIt.next();
          
          if ( client.isCreatingPostReportBatchData() )
            stoppingReportClients.put( client.getID(), client );
        }
        
        // Check to see if we're actually already ready to stop - and do so if
        // nothing left to tidy up
        if ( stoppingReportClients.isEmpty() )
        {
          reportingStopping = false;
          phaseListener.onPostReportPhaseCompleted();
        }
      }
    }
    else throw new Exception( "Phase already stopped or is inactive" );
     
  }
  
  @Override
  public void hardStop()
  {
    synchronized ( controlledStopLock )
    {
      reportingStopping = false;
      phaseActive       = false;
      phaseMsgPump.stopPump();
    }
  }
  
  @Override
  public void accelerateClient( EMClientEx client ) throws Exception
  {
    if ( client == null ) throw new Exception( "Cannot accelerate client (live monitoring) - client is null" );
   
    synchronized ( acceleratorLock )
    {
      client.setCurrentPhaseActivity( EMPhase.eEMPostMonitoringReport );
      
      // Only engage client if we're actually active
      if ( phaseActive && !reportingStopping )
      {
        // Need to manually add/setup accelerated clients
        addClient( client );
        setupClientInterface( client );
      
        client.getDiscoveryInterface().createInterface( EMInterfaceType.eEMPostReport );
      }
      else // Client is too late.. refer them out of this phase immediately
      {
        phaseListener.onPostReportPhaseCompleted( client );
        phaseLogger.info( "Tried accelerating client (post reporting) but we're not active or we are stopping" );
      }
    }
  }
  
  @Override
  public void timeOutClient( EMClientEx client ) throws Exception
  {
    // Safety first
    if ( client == null ) throw new Exception( "Could not time-out: client is null" );   
    
    // Check this client is registered with this phase first
    if ( isClientRegisteredInPhase(client) )
    {
      if ( client.isNotifiedOfTimeOut(EMPhaseTimeOut.eEMTOPostReportTimeOut) ) 
        throw new Exception( "Time-out already sent to client" );
      
      if ( !client.isCreatingPostReportBatchData() )
        throw new Exception( "Client is not currently generating post-report data" );
      
      client.addTimeOutNotification( EMPhaseTimeOut.eEMTOPostReportTimeOut );
      
      EMDataBatch currentBatch = client.getCurrentExpectedDataBatch();
      if ( currentBatch == null ) throw new Exception( "Expected client batch is NULL" );
      
      client.getPostReportInterface().notifyReportBatchTimeOut( currentBatch.getID() );
    }
    else
      throw new Exception( "This client cannot be timed-out in Post-report phase" );
  }
  
  @Override
  public void onClientHasBeenDeregistered( EMClientEx client )
  {
    if ( client != null )
    {
      synchronized ( controlledStopLock )
      {
        UUID id = client.getID();
        stoppingReportClients.remove( id );
        removeClient( id );
      }
    }
  }
  
  // IEMPostReport_ProviderListener --------------------------------------------
  @Override
  public void onNotifyReadyToReport( UUID senderID )
  {
    EMClientEx client = getClient( senderID );

    if ( client != null )
      client.getPostReportInterface().requestPostReportSummary();
  }
  
  @Override
  public void onSendReportSummary( UUID senderID, EMPostReportSummary summary )
  {
    EMClientEx client = getClient( senderID );

    if ( client != null )
    {
      client.setPostReportSummary( summary );
      phaseListener.onGotSummaryReport( client, summary );
    }
  }
  
  @Override
  public void onSendDataBatch( UUID senderID, EMDataBatch populatedBatch )
  {
    EMClientEx client            = getClient( senderID );
    boolean notifyOfClientMSDone = false;

    if ( client != null && populatedBatch != null )
    {
      EMDataBatchEx currExpectedClientBatch = (EMDataBatchEx) client.getCurrentExpectedDataBatch();

      // Get the basic information from this batch and check it's OK
      UUID popBatchID  = populatedBatch.getID();
      Report popReport = populatedBatch.getBatchReport();
      
      boolean handleReport = ( popBatchID != null && popReport != null );

      // If this is the batch we expected, see if we need any more data
      if ( handleReport && popBatchID.equals(currExpectedClientBatch.getID()) )
      {
        if ( popReport != null ) // Check we have some data
        {    
          // Take a look at the current data to see what we need to do with it;
          // Either a) we've got less (or zero) than we expected, in which case
          // the current MeasurementSet is considered 'complete' or b) we've got
          // as much data as we expected in this batch, so ask for some more
          int receivedCount = popReport.getNumberOfMeasurements();

          if ( receivedCount < currExpectedClientBatch.getExpectedMeasurementCount() )
          {
            // Notify we have some data (if we actually do)
            if ( receivedCount > 0 )
              phaseListener.onGotDataBatch( client, populatedBatch );

            // Flag to notify we've completed a MeasurementSet
            notifyOfClientMSDone = true;
          }
          else 
          {
            // We're not going to send the last measurement - it will be used
            // as the basis for start of the next batch. So modified the report
            // by removing the very last measurement
            Date lastStamp = popReport.getToDate();
            amendReportMinusOne( popReport );

            // Send the data we have (minus the last measure)
            phaseListener.onGotDataBatch( client, populatedBatch );

            // Get the next lot (if we're not stopping the reporting phase)
            if ( !reportingStopping )
            {
              EMDataBatchEx clientDBX = (EMDataBatchEx) currExpectedClientBatch;
              clientDBX.resetStartDate( lastStamp );
              clientDBX.resetReportData();

              client.getPostReportInterface().requestDataBatch( currExpectedClientBatch );
            }
            else notifyOfClientMSDone = true; // Otherwise stop this batching set
          }
        }
        else notifyOfClientMSDone = true; // (Report was NULL, so assume no data)

      }
      else // If this isn't the data we expected, save it (if data exists), but complain as well
      {
        if ( handleReport )
        {
          phaseListener.onGotDataBatch( client, populatedBatch );
          phaseLogger.warn( "Got an unexpected batch report: " + populatedBatch.getID().toString() );
        }
        else
        {
          notifyOfClientMSDone = true; // Have to assume we won't get any more data from this measurement set
          phaseLogger.warn( "Got an unexpected batch report (with null data): " + 
                            senderID.toString() + " MS ID: " + 
                            populatedBatch.getExpectedMeasurementSetID().toString() );
        }
      }
    }

    // Notify of completion events if required
    if ( notifyOfClientMSDone )
    {
      phaseListener.onDataBatchMeasurementSetCompleted( client, 
                                                        populatedBatch.getExpectedMeasurementSetID() );

      // Now try to go for another MeasurementSet, if one is waiting... and
      // we're not trying to stop the phase
      if ( !reportingStopping )
      {
        UUID nextMSID = client.iterateNextMSForBatching();
        if ( nextMSID != null )
        {
          EMDataBatch nextBatch = client.getCurrentExpectedDataBatch();          
          client.getPostReportInterface().requestDataBatch( nextBatch );
        }
        else //... otherwise, notify that we're all done!
          phaseListener.onAllDataBatchesRequestComplete( client );
      }
      else // Reporting phase is closing, so drop remaining batches and finish up
      {
        client.clearAllBatching();
        
        synchronized ( controlledStopLock )
        { stoppingReportClients.remove( client.getID() ); }
        
        // Notify we're done with this client
        phaseListener.onAllDataBatchesRequestComplete( client );
      }
    }
    
    // Finally, check to see if we're doing batch for all clients
    boolean noFurtherBatchingClients;
    synchronized ( controlledStopLock )
    { noFurtherBatchingClients = stoppingReportClients.isEmpty(); }
    
    // Notify the phase has ended
    if ( noFurtherBatchingClients )
    {
      phaseActive = false;
      phaseListener.onPostReportPhaseCompleted();
    }
  }
  
  // Private methods -----------------------------------------------------------
  private void setupClientInterface( EMClientEx client )
  {
    AMQPMessageDispatch dispatch = new AMQPMessageDispatch();
    phaseMsgPump.addDispatch( dispatch );

    EMPostReport face = new EMPostReport( emChannel, dispatch,
                                          emProviderID, client.getID(), true );

    face.setProviderListener( this );
    client.setPostReportInterface( face );
  }
  
  private void amendReportMinusOne( Report popRepOUT )
  {
    // We're going to clip the very last measurement away by first ordering
    // all measurements, then removing the last and finally updating the report
    // to reflect the changes
    batchDateTree.clear();
    
    if ( popRepOUT.getNumberOfMeasurements() > 1 )
    {
      Set<Measurement> popMeasures = popRepOUT.getMeasurementSet().getMeasurements();
      Iterator<Measurement> mIt = popMeasures.iterator();
      while ( mIt.hasNext() )
      {
        Measurement m = mIt.next();
        batchDateTree.put( m.getTimeStamp(), m );
      }

      // Find the last and penultimate measurements
      NavigableSet<Date> dateNav = batchDateTree.navigableKeySet();
      Iterator<Date> lastIt = dateNav.descendingIterator();
      Date lastStamp  = lastIt.next(); // Last date stamp
      Date penulStamp = lastIt.next(); // Penultimate date stamp

      // Remove the very last one
      Measurement lastMeasure = batchDateTree.get( lastStamp );
      popMeasures.remove( lastMeasure ); // Don't send this last measurement

      // Update the report to reflect the decrement
      popRepOUT.setToDate( penulStamp );
      popRepOUT.setNumberOfMeasurements( popRepOUT.getNumberOfMeasurements() -1 );
    }
  }
}
