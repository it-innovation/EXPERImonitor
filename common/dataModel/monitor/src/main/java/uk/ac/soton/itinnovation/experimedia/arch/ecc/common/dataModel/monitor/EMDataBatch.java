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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import java.util.*;




/**
 * EMDataBatch encapsulates metric data between a start and end date for a particular
 * MeasurementSet.
 * 
 * @author sgc
 */
public class EMDataBatch
{
  protected UUID batchID;
  protected Date expectedStartStamp;
  protected int  expectedMeasurementCount;
  protected UUID expectedMeasurementSetID;
  
  protected Report batchReport;
  
  
  /**
   * Returns the ID of this batch
   * 
   * @return - ID of the batch.
   */
  public UUID getID()
  { return batchID; }
    
  /**
   * Gets the start date of the first measurement in this set
   * 
   * @return - Start date of the first measurement
   */
  public Date getCopyOfExpectedDataStart()
  { return (Date) expectedStartStamp.clone(); }
  
  public int getExpectedMeasurementCount()
  { return expectedMeasurementCount; }
  
  /**
   * Returns the associated MeasurementSet for this batch. This instance
   * should contain all the measurements within the range indicated by the start
   * and the end date
   * 
   * @return - Instance of the MeasurementSet
   */
  public UUID getExpectedMeasurementSetID()
  { return expectedMeasurementSetID; }
  
  /**
   * Get the report reflecting the data that was requested for this batch. This
   * report may not necessarily contain all the data that was requested by the
   * ECC.
   * 
   * @return - SHould return a fully specified Report instance. 
   */
  public Report getBatchReport()
  { return batchReport; }
  
  /**
   * Set the MeasurementSet associated with this batch. This instance
   * should be fully specified, by not necessarily have to contain all the
   * data that was requested by the parent data batch.
   */
  public void setBatchReport( Report report )
  { batchReport = report; }
  
}
