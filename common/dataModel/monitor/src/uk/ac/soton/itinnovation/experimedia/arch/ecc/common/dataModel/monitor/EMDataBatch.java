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
//      Created Date :          22-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;

import java.util.*;





public class EMDataBatch
{
  private UUID batchID;
  private Date dataStart;
  private Date dateEnd;
  
  private MeasurementSet measurementSet;
  
  
  public EMDataBatch()
  {
    batchID = UUID.randomUUID();
  }
  
  public UUID getID()
  { return batchID; }
  
  public void setDataRange( Date start, Date end )
  {
    if ( start != null && end != null )
    {
      dataStart = start;
      dateEnd   = end;
    }
  }
  
  public Date getDataStart()
  { return dataStart; }
  
  public Date getDataEnd()
  { return dateEnd; }
  
  public MeasurementSet getMeasurementSet()
  { return measurementSet; }
  
  public void setMeasurementSet( MeasurementSet set )
  { if ( set != null ) measurementSet = set; }
}
