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
//	Created Date :			2012-10-10
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////
package eu.experimedia.itinnovation.ecc.web.data;

public class DataPoint {

    long time;
    String value;
    String measurementUUID;

    public DataPoint() {
    }

    public DataPoint(long time, String value, String measurementUUID) {
        this.time = time;
        this.value = value;
        this.measurementUUID = measurementUUID;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMeasurementUUID() {
        return measurementUUID;
    }

    public void setMeasurementUUID(String measurementUUID) {
        this.measurementUUID = measurementUUID;
    }
}
