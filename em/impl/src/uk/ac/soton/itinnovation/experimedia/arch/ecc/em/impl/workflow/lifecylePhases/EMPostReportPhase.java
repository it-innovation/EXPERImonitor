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
  public void reset()
  {
    clearAllClients();
    
    batchDateTree.clear();
  }
  
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
      EMClientEx client            = getClient( senderID );
      boolean notifyOfClientMSDone = false;
      
      if ( client != null && populatedBatch != null )
      {
        EMDataBatchEx currExpectedClientBatch = (EMDataBatchEx) client.getCurrentExpectedDataBatch();
        
        // Get the basic information from this batch and check it's OK
        UUID popBatchID  = populatedBatch.getID();
        Report popReport = populatedBatch.getBatchReport();
                
        // If this is the batch we expected, see if we need any more data
        if ( popBatchID.equals(currExpectedClientBatch.getID()) )
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

              // Get the next lot
              EMDataBatchEx clientDBX = (EMDataBatchEx) currExpectedClientBatch;
              clientDBX.resetStartDate( lastStamp );
              clientDBX.resetReportData();
              
              client.getPostReportInterface().requestDataBatch( currExpectedClientBatch );
            }
          }
          else
            notifyOfClientMSDone = true; // (Report was NULL, so assume no data)
          
        }
        else // If this isn't the data we expected, save it (if data exists), but complain as well
        {
          if ( popReport != null )
          {
            phaseListener.onGotDataBatch( client, populatedBatch );
            phaseLogger.warn( "Got an unexpected batch report: " + populatedBatch.getID().toString() );
          }
          else
            phaseLogger.warn( "Got an unexpected batch report (with no data): " + populatedBatch.getID().toString() );
        }
      }
      
      // Notify of completion events if required
      if ( notifyOfClientMSDone )
      {
        phaseListener.onDataBatchMeasurementSetCompleted( client, 
                                                          populatedBatch.getExpectedMeasurementSetID() );
        
        // Now try to go for another MeasurementSet, if one is waiting...
        UUID nextMSID = client.iterateNextMSForBatching();
        if ( nextMSID != null )
        {
          EMDataBatch nextBatch = client.getCurrentExpectedDataBatch();          
          client.getPostReportInterface().requestDataBatch( nextBatch );
        }
        else //... otherwise, notify that we're all done!
          phaseListener.onAllDataBatchesRequestComplete( client );
      }
    }
  }
  
  // Private methods -----------------------------------------------------------
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
