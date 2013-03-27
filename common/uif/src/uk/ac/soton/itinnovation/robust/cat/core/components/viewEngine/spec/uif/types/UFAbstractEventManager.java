/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2011
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
//      Created Date :          26 Aug 2011
//      Created for Project :   ROBUST
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.types;

import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.mvc.*;

import java.util.*;


/**
 * UFAbstractEventManager is a class that provides general support for notifying
 * and listening to events
 * 
 * @author sgc
 */
public abstract class UFAbstractEventManager implements IUFNotifier, IUFListener
{
  protected HashSet<IUFListener>         ufListeners;
  protected HashMap<IUFNotifier, UFHook> ufHookMap;
  
  // IUFNotifier ---------------------------------------------------------------
  @Override
  public void addListener( IUFListener listener )
  {
    if ( listener != null )
      if ( ufListeners.add(listener) )
        listener.onAcceptHook( new UFHook(this, listener) );
  }
  
  @Override
  public void onListenerHookDestroyed( UFHook hook )
  {
    if ( hook != null )
      ufListeners.remove( hook.getListener() );
  }
  
  // IUFListener ---------------------------------------------------------------
  /**
   * Store reference to notifier so that we can listen for events
   * 
   * @param hook - event listening hook
   */
  @Override
  public void onAcceptHook( UFHook hook )
  {
    if ( hook != null )
      ufHookMap.put( hook.getNotifier(), hook );
  }
  
  // Protected methods ---------------------------------------------------------
  protected UFAbstractEventManager()
  {
    ufListeners = new HashSet<IUFListener>();
    ufHookMap   = new HashMap<IUFNotifier, UFHook>();
  }
  
  /**
   * In cases where you have be accepting more than one listener type, use this
   * method to pull out only the listeners of type T that you wish to notify
   * 
   * @param <T> - the listener type you need
   * @return    - an list of listeners of type T
   */
  protected <T> List<T> getListenersByType()
  {
    ArrayList<T> targListeners = new ArrayList<T>();
    
    // Run through listeners pulling out the type we require
    for ( IUFListener listener : ufListeners )
    {
      T lType = (T) listener;
      if ( lType != null ) targListeners.add( (T) listener );
    }      
      
    return targListeners;    
  }
  
  /**
   * Stop listening to notifier events
   * 
   * @param notifier - the notifier whose events are being listened to
   */
  protected void unhookNotifier( IUFNotifier notifier )
  {
    if ( notifier != null )
      ufHookMap.remove( notifier );
  }
}