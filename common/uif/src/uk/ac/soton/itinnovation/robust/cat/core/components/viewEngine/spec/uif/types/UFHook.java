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
//      Created Date :          25 Aug 2011
//      Created for Project :   ROBUST
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.types;

import uk.ac.soton.itinnovation.robust.cat.core.components.viewEngine.spec.uif.mvc.*;

public class UFHook
{
  private IUFNotifier notifier;
  private IUFListener listener;
  
  public UFHook( IUFNotifier note, IUFListener list )
  {
    notifier = note;
    listener = list;
  }
  
  public IUFNotifier getNotifier() { return notifier; }
  public IUFListener getListener() { return listener; }
  
  @Override
  protected void finalize() throws Throwable
  {
    // Throw a wobbler if we have dodgy notifier/listener ----------------------
    if ( notifier == null ) throw new Exception( "CATUFHook: Notifier is NULL" );
    if ( listener == null ) throw new Exception( "CATUFHook: Listener is NULL" );
    // -------------------------------------------------------------------------
    
    // Tell notifier listner has been destroyed
    notifier.onListenerHookDestroyed( this );
    
    // Continue finalizing
    try { super.finalize(); } catch (Throwable t) { throw t; }
  }
}