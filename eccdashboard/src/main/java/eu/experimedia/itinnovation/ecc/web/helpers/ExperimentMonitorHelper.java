/////////////////////////////////////////////////////////////////////////
//
// ¬© University of Southampton IT Innovation Centre, 2012
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
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
//	Created By :			Maxim Bashevoy
//	Created Date :			2012-09-03
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////
package eu.experimedia.itinnovation.ecc.web.helpers;

import eu.experimedia.itinnovation.ecc.web.adapters.DashboardExperimentMonitor;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.MonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;

@Service
public class ExperimentMonitorHelper {
    private static final Logger logger = Logger.getLogger(ExperimentMonitorHelper.class);

    private static DashboardExperimentMonitor em;
    private IMonitoringEDM expDataMgr;

    public synchronized DashboardExperimentMonitor getExperimentMonitor() throws Throwable {
        return getStaticExperimentMonitor();
    }

    public DashboardExperimentMonitor getStaticExperimentMonitor() throws Throwable {

        if (em == null) {
            logger.debug("Creating new Experiment Monitor");
            
            // Clear database
            expDataMgr = new MonitoringEDM();
            expDataMgr.clearMetricsDatabase();
            
            // Create new Experiment monitor
            em = new DashboardExperimentMonitor();
            em.openEntryPoint("127.0.0.1", UUID.fromString("00000000-0000-0000-0000-000000000000"));
            
        }

        return em;
    }
    
}
