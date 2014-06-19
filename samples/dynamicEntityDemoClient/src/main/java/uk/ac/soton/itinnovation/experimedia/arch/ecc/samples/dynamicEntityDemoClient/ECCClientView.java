/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
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
//      Created By :            Dion Kitchener
//      Created Date :          04-July-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.dynamicEntityDemoClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import javax.swing.JOptionPane;

public class ECCClientView extends javax.swing.JFrame {
    
  private ECCClientViewListener viewListener;
  private ECCNewEntityViewListener newEntityListener;
  private HashMap<UUID, EntityInfo> entityInfoMap;
  private ArrayList<EntityInfo> enabledList;
  private ArrayList<EntityInfo>   disabledList;      
  
  /**
   * Class to define entity information to display and pass between components
   */
  private class EntityInfo
  {
      private UUID entityID;
      private String entityName;
      private boolean enabled;
      
      public EntityInfo( UUID id, String name, boolean enabled )
      {
          this.entityID = id;
          this.entityName = name;
          this.enabled = enabled; 
      }

      
      @Override
      public String toString()
      {
          return entityName;
      }
      
      public UUID getID()
      {
          return entityID;
      }
      
      public String getName()
      {
          return entityName;
      }
      
      public boolean isEnabled()
      {
          return enabled;
      }
      
      public void setEnabled( boolean enabled )
      {
          this.enabled = enabled;
      }
  
  }
   /**
   * Creates new form EMClientView
   */
  public ECCClientView( String clientName,
                        ECCClientViewListener listener,
                        ECCNewEntityViewListener entityListener ) {
    initComponents();
    
    clientNameLabel.setText( clientName );
    viewListener = listener;
    newEntityListener = entityListener;
    
    entityInfoMap = new HashMap<UUID, EntityInfo>();
    
    enabledList = new ArrayList<EntityInfo>();
    disabledList = new ArrayList<EntityInfo>();
  }
  
  //Updates client status
  public synchronized void setStatus( String statusValue )
  { statusLabel.setText( statusValue ); }
  
  //Updates client messages
  public synchronized void addLogMessage( String message )
  { clientMessages.append( message + "\n" ); }
  
  public void enableEntity( UUID entityID, String entityName, boolean enable )
  {
      // Check entityID && name is not null
      
      if ( !entityInfoMap.containsKey(entityID)&& entityName !=null )
      {
          // It's a new entity (needs to create new entity info and add to entityInfoMap )
          EntityInfo info = new EntityInfo( entityID, entityName, enable);
          // add to map
          entityInfoMap.put( entityID, info );
      }
      else
      {
          // We already have this entity info
          EntityInfo targetInfo = entityInfoMap.get( entityID );
          targetInfo.setEnabled(enable);
          // Update targetInfo enable state
          entityInfoMap.put( entityID, targetInfo );
      }
      // Clear both enabled/disable UI lists
      enabledList.clear();
      clientEntitiesList.removeAll();   
      disabledList.clear();
      disabledEntitiesList.removeAll();
      
      // Iterate through hash map and sort enabled and disabled entities into appropriate lists
      for ( UUID key: entityInfoMap.keySet() ) 
      {
          EntityInfo value = entityInfoMap.get( key );
          boolean eStatus = value.enabled;

          if( !eStatus )
          {
              //send to disabled list
              disabledList.add( entityInfoMap.get( key ) );       
          }
          else
          {
              //send to enabled list
              enabledList.add( entityInfoMap.get( key ) );              
          }         
      }
      // Send lists to UI list boxes
       disabledEntitiesList.setListData( disabledList.toArray() );
       clientEntitiesList.setListData( enabledList.toArray() );      
  }
  

  public synchronized void enableAddEntity ( boolean enabled )
  { 
      addEntityButton.setEnabled( enabled );
      disableEntityButton.setEnabled( enabled );
      enableEntityButton.setEnabled( enabled );
  }   

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel2 = new javax.swing.JLabel();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        clientNameLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        clientMessages = new javax.swing.JTextArea();
        addEntityButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        disabledEntitiesList = new javax.swing.JList();
        clientNameLabel1 = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        disableEntityButton = new javax.swing.JButton();
        clientNameLabel2 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        clientEntitiesList = new javax.swing.JList();
        enableEntityButton = new javax.swing.JButton();

        jLabel2.setText("jLabel2");

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("jCheckBoxMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 255));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                onWindowClosing(evt);
            }
        });

        clientNameLabel.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        clientNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        clientNameLabel.setText("EM Client");

        jLabel3.setText("Current status:");

        statusLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        statusLabel.setText("UNKNOWN");

        clientMessages.setColumns(20);
        clientMessages.setRows(5);
        jScrollPane1.setViewportView(clientMessages);

        addEntityButton.setText("Add Entity");
        addEntityButton.setActionCommand("");
        addEntityButton.setEnabled(false);
        addEntityButton.setName("btAddEntity"); // NOI18N
        addEntityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addEntityButtononPushDataClicked(evt);
            }
        });

        disabledEntitiesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        disabledEntitiesList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                disabledEntitiesListValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(disabledEntitiesList);

        clientNameLabel1.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        clientNameLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        clientNameLabel1.setText("Disabled Client Entities ");

        disableEntityButton.setText("Disable Entity");
        disableEntityButton.setEnabled(false);
        disableEntityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disableEntityButtonActionPerformed(evt);
            }
        });

        clientNameLabel2.setFont(new java.awt.Font("Verdana", 0, 12)); // NOI18N
        clientNameLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        clientNameLabel2.setText("Enabled Client Entities ");

        clientEntitiesList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        clientEntitiesList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                clientEntitiesListValueChanged(evt);
            }
        });
        jScrollPane3.setViewportView(clientEntitiesList);

        enableEntityButton.setText("Enable Entity");
        enableEntityButton.setEnabled(false);
        enableEntityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableEntityButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(clientNameLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addEntityButton, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(clientNameLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(clientNameLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 300, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(disableEntityButton, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(enableEntityButton, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(jSeparator2, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(clientNameLabel)
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(statusLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(addEntityButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(clientNameLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(disableEntityButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 23, Short.MAX_VALUE)
                .addComponent(clientNameLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(enableEntityButton)
                .addGap(5, 5, 5))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(84, 84, 84)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(508, Short.MAX_VALUE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

  private void onWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_onWindowClosing
    viewListener.onClientViewClosed();
  }//GEN-LAST:event_onWindowClosing

    private void addEntityButtononPushDataClicked(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addEntityButtononPushDataClicked
        // Open new entity form
        ECCNewEntityView newEntity;
        newEntity = new ECCNewEntityView( newEntityListener );
        newEntity.setVisible(true);    
    }//GEN-LAST:event_addEntityButtononPushDataClicked

    private void disabledEntitiesListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_disabledEntitiesListValueChanged
    
    }//GEN-LAST:event_disabledEntitiesListValueChanged

    private void disableEntityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disableEntityButtonActionPerformed

       //Get the id of an selected item and associates it with a false enable status 
        //Then sends the data to another method: onEntityStatusChanged
        int e =clientEntitiesList.getSelectedIndex();
        
        // Check something has been selected before trying to disable it
        if ( e > -1  )
        {
           //find entity here save to a variable
            EntityInfo entityToDisable = ( EntityInfo ) clientEntitiesList.getModel().getElementAt( e );

            //Remove entity from the list
            clientEntitiesList.clearSelection();
            UUID entityToDisableID = entityToDisable.entityID;
            String entityToDisableName = entityToDisable.getName();
            viewListener.onEntityStatusChanged(entityToDisableID, entityToDisableName, false);

            // Show notification of entity disabling 
            String disableWarning = "Entity " + entityToDisable + " has been disabled";
            JOptionPane.showMessageDialog( null, disableWarning ); 
        }
    }//GEN-LAST:event_disableEntityButtonActionPerformed

    private void clientEntitiesListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_clientEntitiesListValueChanged
     
    }//GEN-LAST:event_clientEntitiesListValueChanged

    private void enableEntityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableEntityButtonActionPerformed
         //Get the id of an selected item and associates it with a true enabled state
        //Then sends the data to another method: onEntityStatusChanged
        int e =disabledEntitiesList.getSelectedIndex();
        
        // Check something is actually selected before trying to re-enabled it
        if ( e > -1 )
        {
            //find entity here save to variable code here
            EntityInfo entityToEnable = ( EntityInfo ) disabledEntitiesList.getModel().getElementAt( e );

            //Remove entity from the list
            disabledEntitiesList.clearSelection();
            UUID entityToEnableID = entityToEnable.entityID;
            String entityToEnableName = entityToEnable.getName();
            viewListener.onEntityStatusChanged( entityToEnableID, entityToEnableName, true );

            // Show notification of entity disabling 
            String enableWarning = "Entity " + entityToEnable + " has been enabled";
            JOptionPane.showMessageDialog( null, enableWarning );
        }
    }//GEN-LAST:event_enableEntityButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addEntityButton;
    private javax.swing.JList clientEntitiesList;
    private javax.swing.JTextArea clientMessages;
    private javax.swing.JLabel clientNameLabel;
    private javax.swing.JLabel clientNameLabel1;
    private javax.swing.JLabel clientNameLabel2;
    private javax.swing.JButton disableEntityButton;
    private javax.swing.JList disabledEntitiesList;
    private javax.swing.JButton enableEntityButton;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables

   
}
