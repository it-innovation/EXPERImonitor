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
//      Created Date :          19-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.samples.basicEMClient;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;

import java.util.*;




public interface EMIAdapterListener
{
  void onEMConnectionResult( boolean connected );
  
  void onPopulateMetricGeneratorInfo();
  
  void onSetupMetricGenerator( UUID generatorSetID, Boolean[] resultOUT );
  
  void onStartPushingMetricData();
  
  void onLastPushProcessed( UUID lastReportID );
  
  void onStopPushingMetricData();
  
  void onPullMetric( UUID measurementSetID, Report reportOUT );
  
  void onGetTearDownResult( Boolean[] resultOUT );
}
