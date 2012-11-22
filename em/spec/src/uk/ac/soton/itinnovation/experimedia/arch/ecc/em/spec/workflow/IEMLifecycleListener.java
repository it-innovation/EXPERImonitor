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
//      Created Date :          14-Aug-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import java.util.UUID;




public interface IEMLifecycleListener
{
  void onClientConnected( EMClient client );
  
  void onClientDisconnected( EMClient client );
  
  void onLifecyclePhaseStarted( EMPhase phase );
  
  void onLifecyclePhaseCompleted( EMPhase phase );
  
  void onNoFurtherLifecyclePhases();
  
  void onLifecycleReset();
  
  void onFoundClientWithMetricGenerators( EMClient client );
  
  void onClientSetupResult( EMClient client, boolean success );
  
  void onClientDeclaredCanPush( EMClient client );
  
  void onClientDeclaredCanBePulled( EMClient client );
  
  void onGotMetricData( EMClient client, Report report );
  
  void onGotSummaryReport( EMClient client, EMPostReportSummary summary );
  
  void onGotDataBatch( EMClient client, EMDataBatch batch );
  
  void onDataBatchMeasurementSetCompleted( EMClient client, UUID measurementSetID );
  
  void onAllDataBatchesRequestComplete( EMClient client );
  
  void onClientTearDownResult( EMClient client, boolean success );
}
