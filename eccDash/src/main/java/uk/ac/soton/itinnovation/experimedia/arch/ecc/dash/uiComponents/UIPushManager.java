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

import com.vaadin.ui.Window;
import org.vaadin.artur.icepush.ICEPush;

import java.util.*;




public class UIPushManager
{
  private final Object pushLock = new Object();
	private boolean			 pushRootOK;
  private boolean      pushActive;
  
  private Window  rootWindow;
  private ICEPush icePush;
  private Timer   iceTimer;
  
  public UIPushManager( Window rootWin )
  {
    rootWindow = rootWin;
    
    icePush  = new ICEPush();
    iceTimer = new Timer();
    
    rootWindow.addComponent( icePush );
  }
  
  public void restart()
  {
    synchronized( pushLock )
    {
      rootWindow.addComponent( icePush );
      pushActive = false;
			pushRootOK = false;
    }
  }
  
  public void shutdown()
  {
    synchronized( pushLock )
    {
			pushRootOK = false;
      pushActive = false;
      iceTimer.cancel();
      iceTimer.purge();
      
      rootWindow.removeComponent( icePush );
      icePush = null;
    }
  }
  
  public boolean pushUIUpdates()
  {
    boolean pushedOK = false;
		
		if ( !pushRootOK ) // Don't push if not connected to the root window
			checkPushRoot();
		else
		{
			synchronized( pushLock )
			{
				if ( icePush != null )
				{
					if ( !pushActive )
					{
						pushActive = true;

						icePush.push();

						pushedOK = true;

						// Control the rate at which we push updates to the web client; do not
						// want to overload the browser
						PushCallback pcb = new PushCallback();
						iceTimer.schedule( pcb, 1000 ); // 1 update/second max
					}
				}
			}
		}
    
    return pushedOK;
  }
  
  // Private classes & methods -------------------------------------------------  
  private boolean checkPushRoot()
	{
		// The dashboard may sometimes try to push before the Vaadin root window has
		// completed adding the ICE pusher. We need to check the pusher is linked to
		// the root before pushing
		
		if ( icePush != null )
			pushRootOK = ( icePush.getParent() != null );
		
		return pushRootOK;		
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
