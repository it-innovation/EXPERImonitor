/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.dynamicEntityDemoClient;

/**
 *
 * @author dmk
 */
public interface ECCNewEntityViewListener {
    
    /**
     * Event called when the user creates a new entity and attribute at the UI
     * 
     */
    void onNewEntityInfoEntered(String entityName, String attName, String attDesc );
    
}
