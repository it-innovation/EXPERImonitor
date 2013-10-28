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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecylePhases;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;

import java.util.*;





public interface EMGeneratorDiscoveryPhaseListener
{
  void onClientPhaseSupportReceived( EMClientEx client );
  
  void onClientDiscoveryResult( EMClientEx client );
  
  void onClientMetricGeneratorsFound( EMClientEx client, Set<MetricGenerator> newGens );
  
  void onClientEnabledMetricCollection( EMClientEx client, UUID entityID, boolean enabled );
  
  void onClientIsDisconnected( EMClientEx client );
  
  void onDiscoveryPhaseCompleted( EMClientEx client );
}
