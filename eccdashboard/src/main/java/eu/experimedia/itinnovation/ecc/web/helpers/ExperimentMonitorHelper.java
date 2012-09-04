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

import java.util.UUID;
import org.springframework.stereotype.Service;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.IExperimentMonitor;

@Service
public class ExperimentMonitorHelper {

    private static IExperimentMonitor em;

    public synchronized IExperimentMonitor getExperimentMonitor() throws Throwable {
        return getStaticExperimentMonitor();
    }

    public IExperimentMonitor getStaticExperimentMonitor() throws Throwable {

        if (em == null) {
            em = EMInterfaceFactory.createEM();
            em.openEntryPoint("127.0.0.1", UUID.fromString("00000000-0000-0000-0000-000000000000"));

        }

        return em;
    }
}
