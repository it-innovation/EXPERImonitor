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

import java.util.ArrayList;
import java.util.Date;
import org.joda.time.format.ISODateTimeFormat;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.co.soton.itinnovation.ecc.service.domain.EccCounterMeasurement;
import uk.co.soton.itinnovation.ecc.service.domain.EccCounterMeasurementSet;
import uk.co.soton.itinnovation.ecc.service.domain.EccExperiment;
import uk.co.soton.itinnovation.ecc.service.domain.EccGenericMeasurement;
import uk.co.soton.itinnovation.ecc.service.domain.EccGenericMeasurementSet;
import uk.co.soton.itinnovation.ecc.service.domain.EccMeasurement;
import uk.co.soton.itinnovation.ecc.service.domain.EccMeasurementSet;

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

    public static EccGenericMeasurementSet eccMeasurementSetToEccGenericMeasurementSet(EccMeasurementSet ms) {
        if (ms == null) {
            return null;
        } else {
            EccGenericMeasurementSet result = new EccGenericMeasurementSet();
            result.setType(ms.getType());
            result.setUnit(ms.getUnit());
            result.setTimestamp(ms.getTimestamp());
            ArrayList<EccGenericMeasurement> data = new ArrayList<EccGenericMeasurement>();

            for (EccMeasurement e : ms.getData()) {
                data.add(new EccGenericMeasurement(ISODateTimeFormat.dateTime().print(e.getTimestamp().getTime()), e.getValue()));
            }

            result.setData(data);

            return result;
        }
    }

    public static EccGenericMeasurementSet eccCounterMeasurementSetToEccGenericMeasurementSet(EccCounterMeasurementSet ms) {
        if (ms == null) {
            return null;
        } else {
            EccGenericMeasurementSet result = new EccGenericMeasurementSet();
            result.setType(ms.getType());
            result.setUnit(ms.getUnit());
            result.setTimestamp(ms.getTimestamp());
            ArrayList<EccGenericMeasurement> data = new ArrayList<EccGenericMeasurement>();

            for (EccCounterMeasurement e : ms.getData()) {
                data.add(new EccGenericMeasurement(e.getName(), Integer.toString(e.getCounter())));
            }

            result.setData(data);

            return result;
        }
    }

}
