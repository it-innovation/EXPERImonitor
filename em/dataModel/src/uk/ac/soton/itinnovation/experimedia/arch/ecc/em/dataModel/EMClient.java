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
//      Created Date :          13-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.dataModel;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.faces.IEMMonitor;

import java.util.UUID;




public class EMClient
{
  private UUID   clientID;
  private String clientName;
  
  IEMMonitor monitorFace;
  
  public EMClient( UUID id, String name )
  {
    clientID = id;
    clientName = name;
  }
  
  public UUID getID()
  { return clientID; }
  
  public String getName()
  { return clientName; }
  
  public IEMMonitor getEMMonitorInterface()
  { return monitorFace; }
  
  public void setEMMonitorInterface( IEMMonitor face )
  { monitorFace = face; }
  
}
