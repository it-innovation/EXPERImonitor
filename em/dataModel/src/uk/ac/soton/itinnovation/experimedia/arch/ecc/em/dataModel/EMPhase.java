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
//      Created Date :          16-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.dataModel;

import java.util.Comparator;


public enum EMPhase implements Comparator
{
  eEMUnknownPhase             ( "Undefined EM phase"                 , 0 ),
  eEMDiscoverMetricGenerators ( "EM Metric generator discovery phase", 1 ),
  eEMSetUpMetricGenerators    ( "EM Metric generator set-up phase",    2 ),
  eEMMetricEnumeration        ( "EM Metric enumeration phase",         3 ),
  eEMLiveMonitoring           ( "EM Metric live monitoring phase",     4 ),
  eEMPostMonitoringReport     ( "EM Post-monitoring reporting phase",  5 ),
  eEMTearDown                 ( "EM Monitoring tear-down phase",       6 ),
  
  // Always at the end of the protocol
  eEMProtocolComplete         ( "EM Monitoring protocol is complete",  7 );
  
  private final int    phaseIndex;
  private final String phaseDescription;
  
  public int getIndex() { return phaseIndex; }
  
  @Override
  public String toString() { return phaseDescription; }
  
  EMPhase( String desc, int ind )
  {
    phaseDescription = desc;
    phaseIndex       = ind;
  }
  
  public EMPhase nextPhase()
  {
    EMPhase nextPhase = eEMUnknownPhase;
    
    switch ( phaseIndex )
    {
      case 0 : nextPhase = eEMDiscoverMetricGenerators; break;
      case 1 : nextPhase = eEMSetUpMetricGenerators;    break;
      case 2 : nextPhase = eEMMetricEnumeration;        break;
      case 3 : nextPhase = eEMLiveMonitoring;           break;
      case 4 : nextPhase = eEMPostMonitoringReport;     break;
      case 5 : nextPhase = eEMTearDown;                 break;
      case 6 : nextPhase = eEMProtocolComplete;         break;
    }
    
    return nextPhase;
  }
  
  // Comparator ----------------------------------------------------------------
  @Override
  public int compare( Object lhs, Object rhs )
  {
    EMPhase pL = (EMPhase) lhs;
    EMPhase pR = (EMPhase) rhs;
    
    if ( pL != null && pR != null )
      return pR.phaseIndex - pL.phaseIndex;
    
    return 0;
  }
  
  public boolean equals( EMPhase lhs, EMPhase rhs )
  { return ( lhs.phaseIndex == rhs.phaseIndex ); }
}
