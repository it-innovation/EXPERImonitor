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
//      Created Date :          20-Mar-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.basicAndroidClient;

import android.app.Activity;
import android.view.View;
import android.widget.*;



/**
 * The ClientView class represents the user interface for the Android client.
 * Events from the user interface are communicated by ClientViewListener.
 * 
 */
public class ClientView
{
    private EditText ipText;
    private TextView logText;
    private SeekBar  seekBar;
    private Button   connectionButton;

    private ClientViewListener viewListener;


    /**
     * ClientView constructor.
     * 
     * @param act       - Activity instance of the application - must not be null.
     * @param listener  - Listener for the view - must not be null.
     */
    public ClientView( Activity act, ClientViewListener listener )
    {
        // Only proceed if both parameters are good
        if ( act != null && listener != null )
        {
            // Get UI references
            ipText           = (EditText) act.findViewById( R.id.serverIP );
            logText          = (TextView) act.findViewById( R.id.logView );
            seekBar          = (SeekBar)  act.findViewById( R.id.seekBar );
            connectionButton = (Button)   act.findViewById( R.id.conButton );

            // Set up listeners (external and internal)
            viewListener = listener;

            if ( seekBar != null && connectionButton != null )
            {
                seekBar.setOnSeekBarChangeListener( new SeekEventListener() );
                seekBar.setEnabled( false );

                connectionButton.setOnClickListener( new ConnectClickListener() );
            }
        }
    }

    /**
     * Returns a numeric string representing the IP of the RabbitMQ server.
     * 
     * @return - String representing the IP of the RabbitMQ server.
     */
    public String getServerIPValue()
    {
        String value = null;

        if ( ipText != null ) value = ipText.getText().toString();

        return value;    
    }

    /**
     * Appends text to the logging area of the display.
     * 
     * @param text - Text to append. Null values are ignored.
     */
    public void addLogText( String text )
    {
        if ( text != null && logText != null )
        {
          logText.append( text + "\n" );
        }
    }

    /**
     * Clears the logging area of the display.
     */
    public void clearLogText()
    { 
        if ( logText != null )
          logText.setText( "" ); 
    }

    /**
     * Determines whether the connection button displays 'connect' or 'disconnect'
     * as the next function available to the user.
     * 
     * @param connect - if true, the connect button displays 'Connect'
     */
    public void setConnectionFunction( boolean connect )
    {
        if ( connect )
          connectionButton.setText( "Connect" );
        else
          connectionButton.setText( "Disconnect" );
    }

    /**
     * Enables the slider component for the purposes of sending slider data to
     * the ECC (via the controller).
     * 
     * @param enabled - if true, the slider becomes enabled.
     */
    public void setPushEnabled( boolean enabled )
    { 
        if ( seekBar != null )
          seekBar.setEnabled(enabled); 
    }

    // Private methods ---------------------------------------------------------
    private void onSeekValueChanged( int value )
    { 
        viewListener.onSliderValueChanged( value );
    }

    private void onConnectButtonClicked()
    {
        viewListener.onConnectionButtonClicked();
    }

    /**
     * This private class handles events from the slider component
     */
    private class SeekEventListener implements SeekBar.OnSeekBarChangeListener
    {
        @Override
        public void onProgressChanged( SeekBar bar, int prog, boolean fromTouch  )
        { onSeekValueChanged(prog); }

        @Override
        public void onStartTrackingTouch( SeekBar bar )
        {}

        @Override
        public void onStopTrackingTouch( SeekBar bar )
        {}
    }

    /**
     * This private class handles button clicking events
     */
    private class ConnectClickListener implements Button.OnClickListener
    {
        @Override
        public void onClick(View v)
        { onConnectButtonClicked(); }
    }
}
