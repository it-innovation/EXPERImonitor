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
//	Created Date :			2012-10-17
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////
package eu.experimedia.itinnovation.ecc.web.adapters;

import java.util.Date;
import java.util.HashMap;

public class DashboardMeasurementSet {

    private String UUID;
    private HashMap<Date, String> measurements = new HashMap<Date, String>();

    public DashboardMeasurementSet(String UUID) {
        this.UUID = UUID;
    }

    public void addMeasurement(Date timestamp, String value) {
        measurements.put(timestamp, value);
    }

    public HashMap<Date, String> getMeasurements() {
        return measurements;
    }

    public String getUUID() {
        return UUID;
    }
}
