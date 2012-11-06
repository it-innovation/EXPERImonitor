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

import eu.experimedia.itinnovation.ecc.web.data.DataPoint;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import org.apache.log4j.Logger;

public class DashboardMeasurementSet {
    
    private final Logger logger = Logger.getLogger(DashboardMeasurementSet.class);
    
    private String UUID;
    private LinkedHashMap<String, DataPoint> measurements = new LinkedHashMap<String, DataPoint>();
//    {
//        @Override
//        protected boolean removeEldestEntry(Entry eldest) {
//                return size() > 5; // This is the map size. New entries remove eldest entries
//        }        
//    };

    public DashboardMeasurementSet(String UUID) {
        this.UUID = UUID;
    }

    public void addMeasurement(String measurementUuid, DataPoint dataPoint) {
        logger.debug("Adding new data point " + dataPoint.getValue() + " to measurement set [" + this.UUID + "]");
        measurements.put(measurementUuid, dataPoint);
    }

    public LinkedHashMap<String, DataPoint> getMeasurements() {
        return measurements;
    }

    public String getUUID() {
        return UUID;
    }
}
