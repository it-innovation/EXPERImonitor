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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.*;

import java.util.*;





public interface IExperimentMonitor
{
  enum eStatus { NOT_YET_INITIALISED,
                 INITIALISED,
                 ENTRY_POINT_OPEN,
                 LIFECYCLE_STARTED,
                 LIFECYCLE_ENDED };
    
  eStatus getStatus();
  
  void openEntryPoint( String rabbitServerIP, UUID entryPointID ) throws Exception;
  
  Set<EMClient> getAllConnectedClients();
  
  Set<EMClient> getCurrentPhaseClients();
  
  void addLifecyleListener( IEMLifecycleListener listener );
  
  void removeLifecycleListener( IEMLifecycleListener listener );
  
  EMPhase startLifecycle() throws Exception;
  
  EMPhase getNextPhase();
  
  boolean isCurrentPhaseActive();
  
  void stopCurrentPhase() throws Exception;
  
  void goToNextPhase() throws Exception;
  
  void endLifecycle() throws Exception;
  
  void pullMetric( EMClient client, UUID measurementSetID ) throws Exception;
  
  void requestDataBatch( EMClient client, EMDataBatch batch ) throws Exception;
}
