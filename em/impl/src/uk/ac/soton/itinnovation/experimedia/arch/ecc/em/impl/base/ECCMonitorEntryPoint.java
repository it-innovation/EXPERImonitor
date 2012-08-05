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
//      Created By :            sgc
//      Created Date :          31-Jul-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.base;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.eccInterface.ECCHalfInterfaceBase;


import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.dataModel.EMMethodPayload;

import java.util.*;





public class ECCMonitorEntryPoint extends ECCBaseInterface
                                  implements IECCMonitorEntryPoint
{
  private IECCMonitorEntryPoint_ProviderListener providerListener;
  
  
  public ECCMonitorEntryPoint( AMQPBasicChannel channel,
                               UUID             providerID,
                               boolean          isProvider )
  {
    super( channel, isProvider );
    interfaceName = "IECCMonitorEntryPoint";
    interfaceVersion = "0.1";
    
    interfaceProviderID = providerID;
    
    ECCHalfInterfaceBase entryPoint = new ECCHalfInterfaceBase( channel );
    initialiseAMQP( entryPoint );
  }
  
  // IECCMonitorEntryPoint -----------------------------------------------------
  @Override
  public void setListener( IECCMonitorEntryPoint_ProviderListener listener )
  { providerListener = listener; }
  
  // Method ID = 1
  @Override
  public void registerAsEMClient( UUID userID, String userName )
  {
    ArrayList<Object> params = new ArrayList<Object>();
    params.add( userID.toString() );
    params.add( userName );
    
    executeMethod( 1, params );
  }
  
  // Protected methods ---------------------------------------------------------
  @Override
  protected void onInterpretMessage( EMMethodPayload payload )
  {
    // 'RegisterAsEMClient' method (ID = 1) ------------------------------------
    if ( payload.getMethodID() == 1 && providerListener != null )
    {
      List<Object> params = payload.getParameters();
      
      UUID userID = UUID.fromString( (String) params.get(0) );
      String userName = (String) params.get(1);
      
      providerListener.onRegisterAsEMClient( userID, userName );
    }
  }
}
