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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;

import java.util.*;




public class EMPostReportSummary
{
  private HashMap<UUID, Report> reportsByMeasurementSetID;
  
  
  public EMPostReportSummary()
  {
    reportsByMeasurementSetID = new HashMap<UUID, Report>();
  }
  
  public Set<UUID> getReportedMeasurementSetIDs()
  { return reportsByMeasurementSetID.keySet(); }
  
  public void addReport( Report report )
  {
    if ( report != null )
     reportsByMeasurementSetID.put( report.getMeasurementSet().getUUID(), 
                                    report );
  }
  
  public void removeReport( UUID measurementSetID )
  {
    if ( measurementSetID != null )
      reportsByMeasurementSetID.remove( measurementSetID );
  }
  
  public Report getReport( UUID measurementID )
  {
    Report report = null;
    
    if ( measurementID != null )
      report = reportsByMeasurementSetID.get( measurementID );
    
    return report;
  }
}