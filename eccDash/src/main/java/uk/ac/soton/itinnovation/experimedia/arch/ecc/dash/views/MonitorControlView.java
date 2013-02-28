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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.views;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.SimpleView;

import com.vaadin.ui.*;
import java.util.Collection;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents.UILayoutUtil;




public class MonitorControlView extends SimpleView
{
  // Control components
  private Label  entryPointLabel;
  private Label  currentPhaseLabel;
  private Label  monitorStatus;
  private Button phaseControlButton;
  private Button experimentStopButton;
  
  // Experiment info components
  private Label experimentNameLabel;
  private Label experimentDescLabel;
  private Label experimentIDLabel;
  private Label experimentStartDateLabel;
  
  private transient EMPhase currentPhase = EMPhase.eEMUnknownPhase;
  

  public MonitorControlView()
  {
    super();
    
    createComponents();
  }
  
  public void setExperimentInfo( String id, Experiment info )
  { 
    if ( id != null && info != null )
    {
      entryPointLabel.setValue( "Entry point ID: " + id );
      
      experimentNameLabel.setValue( "Experiment: " + info.getName() );
      experimentDescLabel.setValue( "Description: " + info.getDescription() );
      experimentIDLabel.setValue( "ID: " + info.getExperimentID() );
      experimentStartDateLabel.setValue( "Started: " + info.getStartTime().toString() );
    } 
  }
  
  public void setPhase( EMPhase phase )
  {
    currentPhase = phase;
    
    if ( currentPhase != EMPhase.eEMProtocolComplete )
    {
      currentPhaseLabel.setValue( "Current phase: " + phase.toString() );
      
      // Update next phase button
      if ( phase.nextPhase() != EMPhase.eEMProtocolComplete )
      {
        phaseControlButton.setCaption( "Go to " + phase.nextPhase().toString() );
        experimentStopButton.setEnabled( true );
      }
      else
        phaseControlButton.setCaption( "Finalize experiment" );
    }
    else
    {
      phaseControlButton.setCaption( "No further phases" );
      phaseControlButton.setEnabled( false );
      currentPhaseLabel.setValue( "Experiment process completed");
    }
  }
  
  public void resetView()
  {
    currentPhase = EMPhase.eEMUnknownPhase;
    currentPhaseLabel.setValue( "Waiting for clients" );
    phaseControlButton.setCaption( "Start discovery phase" );
    phaseControlButton.setEnabled( true );
    experimentStopButton.setEnabled( false );
  }
  
  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();
    vl.setStyleName( "eccControlPanel" );
    
    // Title
    HorizontalLayout hl = new HorizontalLayout();
    vl.addComponent( hl );
    
    Resource res  = new ThemeResource( "img/expLogo.jpg" );
    Embedded img  = new Embedded( null, res );
    hl.addComponent( img );
    
    Label label = new Label( "Dashboard V1.1" );
    label.setStyleName( "tiny" );
    hl.addComponent( label );
    hl.setComponentAlignment( label, Alignment.TOP_LEFT );
    
    VerticalLayout innerVL = new VerticalLayout();
    innerVL.setStyleName( "eccControlInfoPanel" );
    vl.addComponent( innerVL );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "10px", null ) );
    
    hl = new HorizontalLayout();
    innerVL.addComponent(hl);
    hl.addComponent( UILayoutUtil.createSpace( "10px", null, true ) );
    hl.addComponent( createControlComponents() );
    hl.addComponent( createStatusComponents() );
    hl.addComponent( createExperimentInfoComponents() );
    
    // Space
    innerVL.addComponent( UILayoutUtil.createSpace( "10px", null ) );
    
    // Bottom border
    vl.addComponent( UILayoutUtil.createSpace( "1px", null ) );
  }
  
  private VerticalLayout createStatusComponents()
  {
    VerticalLayout vl = new VerticalLayout();
    vl.setWidth( "350px" );
    
    // Experiment entry point
    entryPointLabel = new Label( "Entry point ID: " );
    vl.addComponent( entryPointLabel );
    
    // Status
    monitorStatus = new Label( "Status: Monitor UP" );
    vl.addComponent( monitorStatus );
    
    // Phase items
    currentPhaseLabel = new Label( "Waiting for clients" );
    currentPhaseLabel.addStyleName( "h2" );
    vl.addComponent( currentPhaseLabel );
    
    return vl;
  }
  
  private Component createControlComponents()
  {
    VerticalLayout vl = new VerticalLayout();
    vl.setWidth( "240px" );
    
    // Phase control
    phaseControlButton = new Button( "Start discovery phase" );
    phaseControlButton.addStyleName( "wide tall" );
    phaseControlButton.addListener( new PhaseControlClicked() );
    phaseControlButton.setWidth( "230px" );
    vl.addComponent( phaseControlButton );
    vl.setComponentAlignment( phaseControlButton, Alignment.BOTTOM_LEFT );
    
    // Space
    vl.addComponent ( UILayoutUtil.createSpace( "2px", null ) );
    
    // Experiment stop
    experimentStopButton = new Button( "Stop experiment" );
    experimentStopButton.setStyleName( "small" );
    experimentStopButton.setWidth( "230px" );
    experimentStopButton.setEnabled( false );
    experimentStopButton.addListener( new StopExpClicked() );
    vl.addComponent( experimentStopButton );
    vl.setComponentAlignment( experimentStopButton, Alignment.BOTTOM_LEFT );
    
    return vl;
  }
  
  private VerticalLayout createExperimentInfoComponents()
  {
    VerticalLayout vl = new VerticalLayout();
    vl.setWidth( "350px" );
    
    experimentNameLabel = new Label( "Awaiting experiment info" );
    vl.addComponent( experimentNameLabel );
    
    experimentDescLabel = new Label( "No description yet" );
    vl.addComponent( experimentDescLabel );
    
    experimentIDLabel = new Label( "No experiment ID yet" );
    vl.addComponent( experimentIDLabel );
    
    experimentStartDateLabel = new Label( "No start time yet" );
    vl.addComponent( experimentStartDateLabel );
    
    return vl;
  }
  
  // Event handlers ------------------------------------------------------------
  private void onPhaseControlClicked()
  {
    switch ( currentPhase )
    {
      case eEMUnknownPhase:
      {
        Collection<MonitorControlViewListener> listeners = getListenersByType();
        for( MonitorControlViewListener listener : listeners )
          listener.onStartLifecycleClicked();
        
      } break;
      
      default:
      {
        Collection<MonitorControlViewListener> listeners = getListenersByType();
        for( MonitorControlViewListener listener : listeners )
          listener.onNextPhaseClicked();
        
      } break;
    }
  }
  
  private void onStopExpClicked()
  {
    Collection<MonitorControlViewListener> listeners = getListenersByType();
        for( MonitorControlViewListener listener : listeners )
          listener.onRestartExperimentClicked();
  }
  
  private class PhaseControlClicked implements Button.ClickListener
  {
    @Override
    public void buttonClick(Button.ClickEvent ce) { onPhaseControlClicked(); }
  }
  
  private class StopExpClicked implements Button.ClickListener
  {
    @Override
    public void buttonClick(Button.ClickEvent ce) { onStopExpClicked(); }
  }
}
