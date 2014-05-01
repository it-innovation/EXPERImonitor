/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2014
//
// Copyright in this library belongs to the University of Southampton
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
//	Created By :			Maxim Bashevoy
//	Created Date :			2014-05-01
//	Created for Project :           EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.co.soton.itinnovation.ecc.service.utils;

import java.util.Date;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.co.soton.itinnovation.ecc.service.domain.EccExperiment;

/**
 *
 */
public class Convert {

    public static EccExperiment experimentToEccExperiment(Experiment experiment) {
        EccExperiment result = new EccExperiment();

        if (experiment != null) {
            result.setProjectName(experiment.getExperimentID());
            result.setName(experiment.getName());
            result.setDescription(experiment.getDescription());
            result.setUuid(experiment.getUUID());
            result.setPhase("unknown");

            Date start = experiment.getStartTime();
            Date end = experiment.getEndTime();
            String status = "unknown";
            if (start != null) {
                status = "started";
            }
            if (end != null) {
                status = "finished";
            }

            result.setStatus(status);
            result.setStartTime(start);
            result.setEndTime(end);
        }

        return result;

    }
}
