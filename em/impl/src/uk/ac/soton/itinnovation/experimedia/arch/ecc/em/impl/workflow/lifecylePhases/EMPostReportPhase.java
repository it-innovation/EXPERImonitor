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
  private EMPostReportPhaseListener  phaseListener;
  private TreeMap<Date, Measurement> batchDateTree;
  
  
  public EMPostReportPhase( AMQPBasicChannel channel,
                            UUID providerID,
                            EMPostReportPhaseListener listener )
  {
    super( EMPhase.eEMPostMonitoringReport, channel, providerID );
    
    phaseListener = listener;
    
    batchDateTree = new TreeMap<Date, Measurement>();
    
    phaseState = "Ready to start post-report process";
  }
  
  // AbstractEMLCPhase ---------------------------------------------------------
  @Override
  public void start() throws Exception
  {
    if ( phaseActive ) throw new Exception( "Phase already active" );
    if ( !hasClients() ) throw new Exception( "No clients available for this phase" );
    
    // Create post-report interface
    for ( EMClientEx client : getCopySetOfCurrentClients() )
    {
      AMQPMessageDispatch dispatch = new AMQPMessageDispatch();
      phaseMsgPump.addDispatch( dispatch );
      
      EMPostReport face = new EMPostReport( emChannel, dispatch,
                                            emProviderID, client.getID(), true );
      
      face.setProviderListener( this );
      client.setPostReportInterface( face );
    }
    
    phaseMsgPump.startPump();
    phaseActive = true;
    
    // Request clients create post-report interface
    for ( EMClientEx client : getCopySetOfCurrentClients() )
      client.getDiscoveryInterface().createInterface( EMInterfaceType.eEMPostReport );
    
    phaseState = "Waiting for client to signal ready to report";
  }
  
  @Override
  public void controlledStop() throws Exception
  {
    //TODO
    throw new Exception( "Not yet supported for this phase");
  }
  
  @Override
  public void hardStop()
  {
    phaseMsgPump.stopPump();
    phaseActive = false;
    
    if ( phaseListener != null ) phaseListener.onPostReportPhaseCompleted();
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
      if ( client.isNotifiedOfTimeOut(EMPhaseTimeOut.eEMTOPostReportTimeOut) ) 
        throw new Exception( "Time-out already sent to client" );
      
      if ( !client.isCreatingPostReportBatchData() )
        throw new Exception( "Client is not currently generating post-report data" );
      
      client.addTimeOutNotification( EMPhaseTimeOut.eEMTOPostReportTimeOut );
      
      EMDataBatch currentBatch = client.getCurrentDataBatch();
      if ( currentBatch == null ) throw new Exception( "Expected client batch is NULL" );
      
      client.getPostReportInterface().notifyReportBatchTimeOut( currentBatch.getID() );
    }
    else
      throw new Exception( "This client cannot be timed-out in Post-report phase" );
  }
  
  @Override
  public void onClientUnexpectedlyRemoved( EMClientEx client )
  {
    if ( client != null ) removeClient( client.getID() );
  }
  
  // IEMPostReport_ProviderListener --------------------------------------------
  @Override
  public void onNotifyReadyToReport( UUID senderID )
  {
    if ( phaseActive )
    {
      EMClientEx client = getClient( senderID );
      
      if ( client != null )
        client.getPostReportInterface().requestPostReportSummary();
    }
  }
  
  @Override
  public void onSendReportSummary( UUID senderID, EMPostReportSummary summary )
  {
    if ( phaseActive )
    {
      EMClientEx client = getClient( senderID );
      
      if ( client != null )
      {
        client.setPostReportSummary( summary );
        
        if ( phaseListener != null )
          phaseListener.onGotSummaryReport( client, summary );
      }
    }
  }
  
  @Override
  public void onSendDataBatch( UUID senderID, EMDataBatch populatedBatch )
  {
    if ( phaseActive )
    {
      EMClientEx client = getClient( senderID );
      boolean notifyClientAllBatchesDone = false;
      
      if ( client != null && populatedBatch != null )
      {
        // Check data ID matches the batch we expected
        UUID popBatchID             = populatedBatch.getID();
        EMDataBatch clientDataBatch = client.getCurrentDataBatch();
        
        // If this is the batch we expected, see if we need any more data
        if ( popBatchID.equals(clientDataBatch.getID()) )
        {
          // Create a copy of the in-coming populated data and find out exactly
          // what measurements we really have
          EMDataBatchEx popBatchEx = new EMDataBatchEx( populatedBatch, true );
          
          // Find out exactly what measurements we actually have...
          MeasurementSet popMS = popBatchEx.getMeasurementSet();
          Set<Measurement> popMeasures = popMS.getMeasurements();
          int receivedCount = popMeasures.size();
          
          batchDateTree.clear();
          Iterator<Measurement> mIt = popMeasures.iterator();
          while ( mIt.hasNext() )
          {
            Measurement m = mIt.next();
            batchDateTree.put( m.getTimeStamp(), m );
          }
          
          // If we received less than a full data batch, assume there is no more
          // data for this MeasurementSet, so send remaining data and then try
          // another MeasurementSet
          if ( receivedCount < clientDataBatch.getExpectedMeasurementCount() )
          {
            // Notify we have some data (if we actually do)
            if ( receivedCount > 0 )
            {
              popBatchEx.setActualMeasureInfo( receivedCount, 
                                               batchDateTree.firstKey(), 
                                               batchDateTree.lastKey() );
              
              phaseListener.onGotDataBatch( client, popBatchEx );
            }
            
            // Notify we've completed a MeasurementSet
            phaseListener.onDataBatchMeasurementSetCompleted( client, popMS );
            
            // Now go for another MeasurementSet, if one is waiting...
            UUID nextMSID = client.iterateNextMSForBatching();
            if ( nextMSID != null )
            {
              EMDataBatch nextBatch = client.getCurrentDataBatch();
              client.getPostReportInterface().requestDataBatch( nextBatch );
            }
            else 
              notifyClientAllBatchesDone = true; //... otherwise, we're all done!
          }
          else 
          {            
            // We're going to ignore the very last measurement, as we'll use it
            // as the basis for the first measurement of the next batch in this series
            NavigableSet<Date> dateNav = batchDateTree.navigableKeySet();
            Iterator<Date> lastIt = dateNav.descendingIterator();
            
            Date lastStamp  = lastIt.next(); // Last date stamp
            Date penulStamp = lastIt.next(); // Penultimate date stamp
            
            Measurement lastMeasure = batchDateTree.get( lastStamp );
            popMeasures.remove( lastMeasure ); // Don't send this last measurement
            
            popBatchEx.setActualMeasureInfo( receivedCount -1, 
                                             batchDateTree.firstKey(), 
                                             penulStamp );
            
            // Send the data we have (minus the last measure)
            phaseListener.onGotDataBatch( client, popBatchEx );

            // Get the next lot
            EMDataBatchEx clientDBX = (EMDataBatchEx) clientDataBatch;
            clientDBX.resetStartDate( lastStamp );
            client.getPostReportInterface().requestDataBatch( clientDataBatch );
          }
        }
        else // If this isn't the data we expected, save it, but complain as well
        {
          phaseListener.onGotDataBatch( client, populatedBatch );
          phaseLogger.warn( "Got an unexpected batch report: " + populatedBatch.getID().toString() );
        }
      }
      
      if ( notifyClientAllBatchesDone ) 
        phaseListener.onAllDataBatchesRequestComplete( client );
    }
  }
}
