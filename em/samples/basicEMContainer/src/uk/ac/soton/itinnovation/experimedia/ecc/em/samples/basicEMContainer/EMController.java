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

package uk.ac.soton.itinnovation.experimedia.ecc.em.samples.basicEMContainer;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.dataModel.EMClient;

import java.awt.event.*;

import java.util.UUID;




public class EMController implements IEMClientListener
{
  private IExperimentMonitor expMonitor;
  private EMView             mainView;
  
  public EMController()
  {    
    expMonitor = EMInterfaceFactory.createEM();
    expMonitor.setClientListener( this );
  }
  
  public void start( String rabbitIP, UUID emID )
  {
    mainView = new EMView();
    mainView.setVisible( true );
    mainView.addWindowListener( new ViewWindowListener() );
    
    try
    {
      expMonitor.openEntryPoint( rabbitIP, emID );
    }
    catch (Exception e) {}
  }
  
  // IEMClientListener ---------------------------------------------------------
  @Override
  public void onClientRegistered( EMClient client )
  {
    if ( mainView != null )
      mainView.addConnectedClient( client.getID(), client.getName() );
  }
  
  // Private methods -----------------------------------------------------------
  private void onViewClosed()
  {
    try { expMonitor.endLifecycle(); }
    catch ( Exception e ) {}
  }
  
  // Internal event handling ---------------------------------------------------
  private class ViewWindowListener extends WindowAdapter
  {
    @Override
    public void windowClosed( WindowEvent we )
    { onViewClosed(); }
  }
}
