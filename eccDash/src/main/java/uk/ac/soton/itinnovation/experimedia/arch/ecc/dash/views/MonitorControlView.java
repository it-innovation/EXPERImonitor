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
    
    currentPhaseLabel.setValue( "Current phase: " + phase.name() );
    
    if ( phase.nextPhase() != EMPhase.eEMProtocolComplete )
      phaseControlButton.setCaption( "Go to " + phase.nextPhase().toString() );
    else
      phaseControlButton.setCaption( "Start new experiment" );
  }
  
  public void resetView()
  {
    currentPhase = EMPhase.eEMUnknownPhase;
    currentPhaseLabel.setValue( "Waiting for clients" );
    phaseControlButton.setCaption( "Start discovery phase" );
  }
  
  // Private methods -----------------------------------------------------------
  private void createComponents()
  {
    VerticalLayout vl = getViewContents();
    
    // Title
    Label label = new Label( "EXPERIMEDIA Experiment monitor (v1.1)" );
    label.setStyleName( "h1 color" );
    vl.addComponent( label );
    
    // Control panel
    Panel panel = new Panel();
    panel.setStyleName( "light borderless" );
    panel.setCaption( "Monitoring control" );
    panel.setWidth( "100%" );
    vl.addComponent( panel );
    
    HorizontalLayout hl = new HorizontalLayout();
    hl.setWidth( "100%" );
    panel.addComponent(hl);
    
    hl.addComponent( createControlComponents() );
    hl.addComponent( createExperimentInfoComponents() );
  }
  
  private VerticalLayout createControlComponents()
  {
    VerticalLayout vl = new VerticalLayout();
    vl.setWidth( "400px" );
    
    // Experiment entry point
    entryPointLabel = new Label( "Entry point ID: " );
    vl.addComponent( entryPointLabel );
    
    // Phase items
    currentPhaseLabel = new Label( "Waiting for clients" );
    vl.addComponent( currentPhaseLabel );
    
    monitorStatus = new Label( "Status: Monitor UP" );
    vl.addComponent( monitorStatus );
    
    // Space
    vl.addComponent ( UILayoutUtil.createSpace( "10px", null ) );
    
    phaseControlButton = new Button( "Start discovery phase" );
    phaseControlButton.addStyleName( "wide tall big" );
    phaseControlButton.addListener( new PhaseControlClicked() );
    phaseControlButton.setWidth( "250px" );
    vl.addComponent( phaseControlButton );
    vl.setComponentAlignment( phaseControlButton, Alignment.BOTTOM_LEFT );
    
    return vl;
  }
  
  private VerticalLayout createExperimentInfoComponents()
  {
    VerticalLayout vl = new VerticalLayout();
    vl.setWidth( "100%" );
    
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
      
        // Last phase, so go for re-start
      case eEMTearDown :
      {
        Collection<MonitorControlViewListener> listeners = getListenersByType();
        for( MonitorControlViewListener listener : listeners )
          listener.onRestartExperimentClicked();
        
      } break;
        
      default:
      {
        Collection<MonitorControlViewListener> listeners = getListenersByType();
        for( MonitorControlViewListener listener : listeners )
          listener.onNextPhaseClicked();
        
      } break;
    }
  }
  
  private class PhaseControlClicked implements Button.ClickListener
  {
    @Override
    public void buttonClick(Button.ClickEvent ce) { onPhaseControlClicked(); }
  }
}
