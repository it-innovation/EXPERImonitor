/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
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
//      Created Date :          21-June-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#pragma once

#include "ECCUtils.h"


namespace ecc_commonDataModel
{

enum EMPhase
{
    eEMUnknownPhase                  = 0,
    eEMDiscoverMetricGeneratorsPhase = 1,
    eEMSetUpMetricGeneratorsPhase    = 2,
    eEMLiveMonitoringPhase           = 3,
    eEMPostMonitoringReportPhase     = 4,
    eEMTearDownPhase                 = 5,
    
    // Always at the end of the protocol
    eEMProtocolComplete         = 6
};

typedef boost::container::set<EMPhase> EMPhaseSet;

} // namespace