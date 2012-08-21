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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.samples.basicEMContainer;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;

import java.awt.event.*;

import java.util.*;




public class EMController implements IEMLifecycleListener
{
  private IExperimentMonitor expMonitor;
  private EMView             mainView;
  
  public EMController()
  {    
    expMonitor = EMInterfaceFactory.createEM();
    expMonitor.addLifecyleListener( this );
  }
  
  public void start( String rabbitIP, UUID emID )
  {
    mainView = new EMView( new MonitorViewListener() );
    mainView.setVisible( true );
    mainView.addWindowListener( new ViewWindowListener() );
    
    try
    { expMonitor.openEntryPoint( rabbitIP, emID ); }
    catch (Exception e) {}
  }
  
  // IEMLifecycleListener ------------------------------------------------------
  @Override
  public void onClientConnected( EMClient client )
  {
    if ( mainView != null )
      mainView.addConnectedClient( client.getID(), client.getName() );
  }
  
  @Override
  public void onClientDisconnected( EMClient client )
  {

  }
  
  @Override
  public void onFoundClientWithMetricGenerators( EMClient client )
  {
    if ( client != null )
    {
      Set<MetricGenerator> generators = client.getCopyOfMetricGenerators();
      Iterator<MetricGenerator> mgIt = generators.iterator();
      
      while ( mgIt.hasNext() )
      {
        MetricGenerator mg = mgIt.next();
        
        mainView.addLogText( client.getName() + " has metric generator: " + mg.getName() );
      }
    }
  }
  
  @Override
  public void onDiscoveryPhaseCompleted()
  {

  }
  
  // Private methods -----------------------------------------------------------
  private void onViewClosed()
  {
    try { expMonitor.endLifecycle(); }
    catch ( Exception e ) {}
  }
  
  private void startMonitoringProcess()
  {
    if ( expMonitor != null )
      try
      { 
        expMonitor.startLifecycle();
        mainView.setMonitoringPhaseValue( "Discovering metric providers" );
      }
      catch ( Exception e ) {}
  }
  
  // Internal event handling ---------------------------------------------------
  private class ViewWindowListener extends WindowAdapter
  {
    @Override
    public void windowClosed( WindowEvent we )
    { onViewClosed(); }
  }
  
  private class MonitorViewListener implements EMViewListener
  {
    @Override
    public void onStartPhasesButtonClicked()
    { startMonitoringProcess(); }
  }
}
