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
//      Created Date :          21-Mar-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.basicAndroidClient;



/**
 * ClientViewListener is a simple event abstraction of user interface activities
 * handled by the ClientView.
 * 
 */
public interface ClientViewListener
{
    /**
     * Notifies the listener that the connection button has been clicked.
     */
    void onConnectionButtonClicked();

    /**
     * Notifies the listener than the slider value has changed (should only
     * be called during the Live Monitoring phase).
     * 
     * @param value - value of the slider (between 1 and 100)
     */
    void onSliderValueChanged( int value );
}
