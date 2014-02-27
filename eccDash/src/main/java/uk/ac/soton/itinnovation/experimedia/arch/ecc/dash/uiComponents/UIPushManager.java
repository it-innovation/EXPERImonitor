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
//      Created Date :          05-Dec-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.dash.uiComponents;

import com.vaadin.Application;
import com.vaadin.ui.Component;
import com.vaadin.ui.Window;
import org.vaadin.artur.icepush.ICEPush;

import java.util.*;




public class UIPushManager
{
  //private final Object pushLock = new Object();
	private final Object pushLock = new Object();
	private boolean	     pushReady;
  private boolean      pushActive;
  
  private Window  rootWindow;
  private ICEPush icePush;
  private Timer   iceTimer;
  
  public UIPushManager( Window rootWin )
  {
    rootWindow = rootWin;
		
		// Add ICE push to root window
		if ( rootWindow != null )
		{
			icePush  = new ICEPush();
			rootWindow.addComponent( icePush );

			iceTimer = new Timer();
		}
  }
  
  public void restart()
  {
		// Re-set state of push manager
		synchronized( pushLock )
		{
			pushReady  = false;
			pushActive = false;			
		}
		
		// Re-insert ICE push (may have been removed)
		if ( rootWindow != null )
		{
			rootWindow.removeComponent( icePush );
			rootWindow.addComponent( icePush );
		}
  }
  
  public void shutdown()
  {
		synchronized( pushLock )
		{
			pushReady  = false;
			pushActive = false;
			iceTimer.cancel();
			iceTimer.purge();
		}
		
		rootWindow.removeComponent( icePush );
		icePush = null;
  }
  
  public boolean pushUIUpdates()
  {
    boolean pushedOK = false;
		
		if ( !pushReady ) // Don't push if not yet fully connected to the root window
			tryReadyPush();
		else
		{
			// See if we need to queue an update
			boolean queuePush = false;
			
			synchronized( pushLock )
			{
				if ( !pushActive )
				{
					pushActive = true;
					queuePush  = true;
				}
			}
			
			// Queue the update if required
			if ( queuePush )
			{
				// Synchronize around application object to reduce chances of race-conditions
				// on Vaadin UI update background thread
				synchronized( rootWindow.getApplication() )
				{ 
					icePush.push();
					pushedOK = true;
				}

				// Control the rate at which we push updates to the web client; do not
				// want to overload the browser
				PushCallback pcb = new PushCallback();
				iceTimer.schedule( pcb, 3000 ); // Update every 3 seconds max
			}
		}
    
    return pushedOK;
  }
  
  // Private classes & methods -------------------------------------------------  
  private void tryReadyPush()
	{
		// The dashboard may sometimes try to push before the Vaadin root window has
		// completed adding the ICE pusher. We need to check the pusher is linked to
		// the root before pushing
		if ( icePush != null )
		{
			Component parent = icePush.getParent();
			
			if ( parent != null ) pushReady = true;
		}
	}
	
	private void finishPush()
  {
		synchronized ( pushLock )
		{ pushActive = false; }
  }
  
  private class PushCallback extends TimerTask
  {
    @Override
    public void run()
    { finishPush(); }
  }
}
