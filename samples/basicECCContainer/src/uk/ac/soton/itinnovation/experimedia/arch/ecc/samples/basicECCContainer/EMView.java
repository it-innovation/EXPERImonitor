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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.basicECCContainer;


import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import javax.swing.DefaultListModel;
import java.util.Iterator;




/**
 *
 * @author sgc
 */
public class EMView extends javax.swing.JFrame
{
  private DefaultListModel clientListModel;
  private DefaultListModel entityListModel;
  private DefaultListModel attributeListModel;
  private EMClient         currSelectedClient;
  private Entity           currSelectedEntity;
  
  private EMViewListener   viewListener;
  
  public EMView( EMViewListener listener )
  {
    viewListener = listener;
    
    initComponents();
    
    clientListModel    = new DefaultListModel();
    entityListModel    = new DefaultListModel();
    attributeListModel = new DefaultListModel();
    
    connectedClientMainList.setModel( clientListModel );
    connectedClientMiniList.setModel( clientListModel );
    
    entityList.setModel( entityListModel );
    attributeList.setModel( attributeListModel );
  }
  
  public synchronized void setMonitoringPhaseValue( String phase, String nextPhase )
  {
    phaseLabel.setText( phase );
    setNextPhaseValue( nextPhase );
  }
  
  public synchronized void setNextPhaseValue( String phase )
  {
    if ( phase != null )
    {
      nextPhaseLabel.setText( phase );
      nextPhaseButton.setEnabled( true );
    }
    else
    {
      nextPhaseLabel.setText( "Not yet available" );
      nextPhaseButton.setEnabled( false );
    }
  }
  
  public synchronized void addLogText( String text )
  {
    loggingText.append( text + "\n" );
  }
  
  public synchronized void addConnectedClient( EMClient client )
  {
    if ( client != null )
      clientListModel.addElement( client );
  }
  
  public synchronized void removeClient( EMClient client )
  {
    if ( client != null )
    {
      clientListModel.removeElement( client );
      addLogText( "Client " + client.getName() + " has disconnected" );
    }
  }
  
  public synchronized void updateClient( EMClient client )
  {
    // Update the client if it is currently selected
    if ( client == currSelectedClient )
    {
      clientNameField.setText( currSelectedClient.getName() );
      entityListModel.clear();
      attributeListModel.clear();
        
      updateCurrentEntities();
    }
  }
  
  public synchronized void enablePulling( boolean enabled )
  { pullMetricButton.setEnabled( enabled ); }
  
  public synchronized void enabledPostReportPulling( boolean enabled )
  { postReportButton.setEnabled( enabled ); }
  
  public synchronized void enableTimeOuts( boolean enabled )
  { timeOutButton.setEnabled( enabled ); }

  /**
   * This method is called from within the constructor to initialise the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jTabbedPane1 = new javax.swing.JTabbedPane();
    jPanel1 = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane();
    connectedClientMainList = new javax.swing.JList();
    jLabel1 = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    jLabel2 = new javax.swing.JLabel();
    clientNameField = new javax.swing.JLabel();
    jLabel6 = new javax.swing.JLabel();
    jScrollPane2 = new javax.swing.JScrollPane();
    entityList = new javax.swing.JList();
    jLabel7 = new javax.swing.JLabel();
    jScrollPane3 = new javax.swing.JScrollPane();
    attributeList = new javax.swing.JList();
    jButton1 = new javax.swing.JButton();
    jPanel5 = new javax.swing.JPanel();
    jScrollPane4 = new javax.swing.JScrollPane();
    loggingText = new javax.swing.JTextArea();
    jPanel3 = new javax.swing.JPanel();
    jScrollPane5 = new javax.swing.JScrollPane();
    connectedClientMiniList = new javax.swing.JList();
    jLabel3 = new javax.swing.JLabel();
    timeOutButton = new javax.swing.JButton();
    jPanel4 = new javax.swing.JPanel();
    beginMonitoringProcButton = new javax.swing.JButton();
    jLabel8 = new javax.swing.JLabel();
    phaseLabel = new javax.swing.JLabel();
    nextPhaseButton = new javax.swing.JButton();
    nextPhaseLabel = new javax.swing.JLabel();
    pullMetricButton = new javax.swing.JButton();
    postReportButton = new javax.swing.JButton();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setResizable(false);

    jTabbedPane1.setName("EMTabbedContainer"); // NOI18N

    connectedClientMainList.setName("connectedClientMainList"); // NOI18N
    connectedClientMainList.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        onClientListClicked(evt);
      }
    });
    jScrollPane1.setViewportView(connectedClientMainList);

    jLabel1.setFont(new java.awt.Font("Arial", 0, 18)); // NOI18N
    jLabel1.setText("Currently connected clients");

    jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Client info", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

    jLabel2.setText("Name:");

    clientNameField.setText("No information yet");

    jLabel6.setText("Entities under observation:");

    entityList.setModel(new javax.swing.AbstractListModel() {
      String[] strings = { "Currently no entities" };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    entityList.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        onEntityListClicked(evt);
      }
    });
    jScrollPane2.setViewportView(entityList);

    jLabel7.setText("Entity's observable attibutes:");

    attributeList.setModel(new javax.swing.AbstractListModel() {
      String[] strings = { "Currently no attributes" };
      public int getSize() { return strings.length; }
      public Object getElementAt(int i) { return strings[i]; }
    });
    jScrollPane3.setViewportView(attributeList);

    jButton1.setText("Disconnect client");
    jButton1.setEnabled(false);

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
            .addGap(0, 32, Short.MAX_VALUE)
            .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addGap(67, 67, 67))
          .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jScrollPane2)
              .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addGap(0, 0, Short.MAX_VALUE)))
            .addContainerGap())
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addComponent(jLabel2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(clientNameField, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addContainerGap())
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addComponent(jLabel7)
            .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addGroup(jPanel2Layout.createSequentialGroup()
            .addComponent(jScrollPane3)
            .addContainerGap())))
    );
    jPanel2Layout.setVerticalGroup(
      jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel2Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(clientNameField))
        .addGap(33, 33, 33)
        .addComponent(jLabel6)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(18, 18, 18)
        .addComponent(jLabel7)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(212, 212, 212)
        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(jLabel1)
            .addGap(0, 0, Short.MAX_VALUE))
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 435, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        .addContainerGap())
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(jLabel1)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 429, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE))
        .addGap(82, 82, 82))
    );

    jTabbedPane1.addTab("Conntected EM Clients", jPanel1);

    loggingText.setColumns(20);
    loggingText.setRows(5);
    jScrollPane4.setViewportView(loggingText);

    jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Client fine control"));

    jScrollPane5.setViewportView(connectedClientMiniList);

    jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel3.setText("Current connected clients");

    timeOutButton.setText("Send time-out");
    timeOutButton.setEnabled(false);
    timeOutButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        onSendTimeOutClicked(evt);
      }
    });

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(timeOutButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
          .addGroup(jPanel3Layout.createSequentialGroup()
            .addComponent(jLabel3)
            .addGap(0, 0, Short.MAX_VALUE)))
        .addContainerGap())
    );
    jPanel3Layout.setVerticalGroup(
      jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel3Layout.createSequentialGroup()
        .addGap(9, 9, 9)
        .addComponent(jLabel3)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addGap(11, 11, 11)
        .addComponent(timeOutButton)
        .addContainerGap(88, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
    jPanel5.setLayout(jPanel5Layout);
    jPanel5Layout.setHorizontalGroup(
      jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel5Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 550, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
    jPanel5Layout.setVerticalGroup(
      jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
          .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE))
        .addContainerGap())
    );

    jTabbedPane1.addTab("Experiment logging view", jPanel5);

    jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Monitor control"));

    beginMonitoringProcButton.setText("Begin monitoring process");
    beginMonitoringProcButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        onBeginMonitoringButtonClicked(evt);
      }
    });

    jLabel8.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
    jLabel8.setText("Monitoring phase: ");

    phaseLabel.setFont(new java.awt.Font("Arial", 0, 16)); // NOI18N
    phaseLabel.setText("Waiting for clients");

    nextPhaseButton.setText("Next phase");
    nextPhaseButton.setEnabled(false);
    nextPhaseButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        onNextPhaseClicked(evt);
      }
    });

    nextPhaseLabel.setText("Not available");

    pullMetricButton.setText("Pull client metric data");
    pullMetricButton.setEnabled(false);
    pullMetricButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        onPullButtonClicked(evt);
      }
    });

    postReportButton.setText("Pull post-report data");
    postReportButton.setEnabled(false);
    postReportButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        onPostReportButtonClicked(evt);
      }
    });

    javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
    jPanel4.setLayout(jPanel4Layout);
    jPanel4Layout.setHorizontalGroup(
      jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel4Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(beginMonitoringProcButton, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addGroup(jPanel4Layout.createSequentialGroup()
            .addComponent(nextPhaseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(nextPhaseLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 267, javax.swing.GroupLayout.PREFERRED_SIZE))
          .addGroup(jPanel4Layout.createSequentialGroup()
            .addComponent(jLabel8)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(phaseLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(pullMetricButton, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
          .addComponent(postReportButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addContainerGap())
    );
    jPanel4Layout.setVerticalGroup(
      jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel4Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(beginMonitoringProcButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(jPanel4Layout.createSequentialGroup()
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(jLabel8)
              .addComponent(phaseLabel))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
              .addComponent(nextPhaseButton)
              .addComponent(nextPhaseLabel))))
        .addContainerGap())
      .addGroup(jPanel4Layout.createSequentialGroup()
        .addGap(22, 22, 22)
        .addComponent(pullMetricButton)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addComponent(postReportButton))
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
      .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        .addGap(18, 18, 18)
        .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 510, javax.swing.GroupLayout.PREFERRED_SIZE))
    );

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void onBeginMonitoringButtonClicked(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onBeginMonitoringButtonClicked
    
    if ( viewListener != null )
      viewListener.onStartPhasesButtonClicked();
    
    nextPhaseButton.setEnabled( true );
    beginMonitoringProcButton.setEnabled( false );
    
  }//GEN-LAST:event_onBeginMonitoringButtonClicked

  private void onNextPhaseClicked(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onNextPhaseClicked
    if ( viewListener != null )
      viewListener.onNextPhaseButtonClicked();
  }//GEN-LAST:event_onNextPhaseClicked

  private void onPullButtonClicked(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onPullButtonClicked
    if ( viewListener != null )
      viewListener.onPullMetricButtonClicked();
  }//GEN-LAST:event_onPullButtonClicked

  private void onPostReportButtonClicked(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onPostReportButtonClicked
    if ( viewListener != null )
      viewListener.onPullPostReportButtonClicked();
  }//GEN-LAST:event_onPostReportButtonClicked

  private void onClientListClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_onClientListClicked
    
    // Update currently selected client
    currSelectedClient = getSelectedClient( connectedClientMainList );
    
    if ( currSelectedClient != null ) updateClient( currSelectedClient );    
  }//GEN-LAST:event_onClientListClicked

  private EMClient getSelectedClient( javax.swing.JList list )
  {
    EMClient client = null;
    
    // Update currently selected client
    int selectIndex = list.getSelectedIndex();
    
    if ( selectIndex > - 1 && clientListModel.size() > selectIndex )
      client = (EMClient) clientListModel.get( selectIndex );
    
    return client;
  }
  
  private void updateCurrentEntities()
  {
    if ( currSelectedClient != null )
    {      
      Iterator<MetricGenerator> genIt = currSelectedClient.getCopyOfMetricGenerators().iterator();
      while ( genIt.hasNext() )
      {
        Iterator<Entity> entIt = genIt.next().getEntities().iterator();
        while ( entIt.hasNext() )
        {
          Entity ent = entIt.next();
          
          if ( ent != null ) entityListModel.addElement( ent );
        }
      }
    }
  }
  
  private void updateCurrentAttributes()
  {
    attributeListModel.clear();
    
    if ( currSelectedEntity != null )
    {
      Iterator<Attribute> attIt = currSelectedEntity.getAttributes().iterator();
      while ( attIt.hasNext() )
        attributeListModel.addElement( attIt.next() );
    }
  }
  
  private void onEntityListClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_onEntityListClicked
    
    // Update currently selected client's attributes
    int selectIndex = entityList.getSelectedIndex();
    
    if ( selectIndex > -1 && entityListModel.size() > selectIndex )
      currSelectedEntity = (Entity) entityListModel.get( selectIndex );
    
    updateCurrentAttributes();
  }//GEN-LAST:event_onEntityListClicked

  private void onSendTimeOutClicked(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onSendTimeOutClicked
    
    EMClient client = getSelectedClient( connectedClientMiniList );
    
    if ( client != null ) viewListener.onSendTimeOut( client );
  }//GEN-LAST:event_onSendTimeOutClicked

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JList attributeList;
  private javax.swing.JButton beginMonitoringProcButton;
  private javax.swing.JLabel clientNameField;
  private javax.swing.JList connectedClientMainList;
  private javax.swing.JList connectedClientMiniList;
  private javax.swing.JList entityList;
  private javax.swing.JButton jButton1;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLabel7;
  private javax.swing.JLabel jLabel8;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JPanel jPanel2;
  private javax.swing.JPanel jPanel3;
  private javax.swing.JPanel jPanel4;
  private javax.swing.JPanel jPanel5;
  private javax.swing.JScrollPane jScrollPane1;
  private javax.swing.JScrollPane jScrollPane2;
  private javax.swing.JScrollPane jScrollPane3;
  private javax.swing.JScrollPane jScrollPane4;
  private javax.swing.JScrollPane jScrollPane5;
  private javax.swing.JTabbedPane jTabbedPane1;
  private javax.swing.JTextArea loggingText;
  private javax.swing.JButton nextPhaseButton;
  private javax.swing.JLabel nextPhaseLabel;
  private javax.swing.JLabel phaseLabel;
  private javax.swing.JButton postReportButton;
  private javax.swing.JButton pullMetricButton;
  private javax.swing.JButton timeOutButton;
  // End of variables declaration//GEN-END:variables
}
