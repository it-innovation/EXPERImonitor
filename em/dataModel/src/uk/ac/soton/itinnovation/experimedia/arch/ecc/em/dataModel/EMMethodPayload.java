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
//      Created Date :          29-Jul-2012
//      Created for Project :   experimedia-arch-ecc-em-impl
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.dataModel;

import java.util.List;



/**
 * EMMethodPayload encapsulates message data sent in EM interfaces
 * This class must conform the bean standard (YAML requirement)
 * 
 * @author sgc
 */
public class EMMethodPayload
{
  private int          methodID;
  private List<Object> parameters;
  
  public EMMethodPayload()
  {}
  
  public int getMethodID()
  { return methodID; }
  
  public void setMethodID( int id )
  { methodID = id; }
  
  public List<Object> getParameters()
  { return parameters; }
  
  public void setParameters( List<Object> params )
  { parameters = params; }
}
