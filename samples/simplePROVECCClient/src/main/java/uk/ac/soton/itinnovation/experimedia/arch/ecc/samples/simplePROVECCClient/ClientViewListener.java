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
//      Created By :            Simon Crowle
//      Created Date :          29-Sep-2013
//      Created for Project :   experimedia-arch-ecc-samples-simplePROVECCClient
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.simplePROVECCClient;




public interface ClientViewListener
{
    /*
     * Notify controller that the UI has been closed
     * 
     */
    void onClientViewClosed();

    /**
     * Notify controller of the name of the agent selected by the user
     * 
     * @param agent - Name of agent selected
     */
    void onAgentSelected( String agent );

    /**
     * Notify the controller of the activity selected by the user
     * 
     * @param activity - Name of activity selected
     */
    void onActivitySelected( String activity );

    /**
     * Notify the controller of the entity selected by the user
     * 
     * @param entity - Name of entity selected
     */
    void onEntitySelected( String entity );

    /**
     * Notify the controller that the user wishes to send the current provenance statement
     * 
     */
    void onSendECCData();
}
