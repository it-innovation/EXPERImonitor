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
//      Created Date :          15-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.samples.basicEMClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import java.util.*;





public class EMClientController implements EMIAdapterListener
{
  private AMQPBasicChannel   amqpChannel;
  private EMInterfaceAdapter emiAdapter;
  private EMClientView       clientView;
  
  public EMClientController()
  {
  }
  
  public void start( String rabbitServerIP,
                     UUID expMonitorID,
                     UUID clientID ) throws Exception
  {
    if ( rabbitServerIP != null &&
         expMonitorID   != null &&
         clientID       != null )
    {
      // Create connection to Rabbit server ------------------------------------
      AMQPConnectionFactory amqpFactory = new AMQPConnectionFactory();
      amqpFactory.setAMQPHostIPAddress( rabbitServerIP );
      try
      {
        amqpFactory.connectToAMQPHost();
        amqpChannel = amqpFactory.createNewChannel();
      }
      catch (Exception e ) { throw e; }
      
      // Set up a simple view --------------------------------------------------
      clientView = new EMClientView();
      clientView.setVisible( true );
      
      // Create EM interface adapter, listen to it...
      emiAdapter = new EMInterfaceAdapter( this );
      
      // ... and try registering with the EM.
      Date date = new Date();
      try { emiAdapter.registerWithEM( "Test Client (" + date.toString() + ")",
                                       amqpChannel, 
                                       expMonitorID, clientID ); }
      catch ( Exception e ) 
      { throw e; }
    }
  }
  
  // EMIAdapterListener --------------------------------------------------------
  @Override
  public void onEMConnectionResult( boolean connected )
  {
    if ( connected )
      clientView.setStatus( "Connected to EM" );
    else
      clientView.setStatus( "Refused connection to EM" );
  }
  
  @Override
  public void populateMetricGeneratorInfo( Set<MetricGenerator> genSetOUT )
  {
    clientView.setStatus( "Sending metric meta-data to EM" );
    
    // Mock up some metric generators
    MetricGenerator mg = new MetricGenerator();
    mg.setName( "Demo metric generator" );
    mg.setDescription( "Metric generator demonstration" );
    genSetOUT.add( mg );
    
    //TODO: Entities & Attributes
    
    //TODO: Metric sets, metric types & units 
  }
  
  @Override
  public void setupMetricGenerator( MetricGenerator genOut, Boolean[] resultOUT )
  {
    // Just signal that the metric generator is ready
    resultOUT[0] = true;
  }
}
