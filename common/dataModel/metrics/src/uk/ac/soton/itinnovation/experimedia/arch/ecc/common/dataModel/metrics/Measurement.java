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
//      Created By :            Vegard Engen
//      Created Date :          2012-08-09
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * This class represents a measurement taken at a particular point in time.
 * The measurement is a part of a measurement set for an attribute (of an entity).
 * 
 * @author Vegard Engen
 */
public class Measurement implements Serializable
{
    private UUID uuid;
    private UUID measurementSetUUID; // the measurement is a part of a set, in which the metric is defined
    private Date timeStamp; // the time the measurement was taken
    private String value; // could consider encoding this differently in the future
    
    // other things?
    //   - range?
    
    /**
     * Default constructor, which sets a random UUID for the object instance.
     */
    public Measurement()
    {
        this.uuid = UUID.randomUUID();
    }
    
    /**
     * Copy constructor; takes a deep copy of all objects.
     * @param m The measurement object a copy is made from.
     */
    public Measurement(Measurement m)
    {
        if (m == null)
            return;
        
        this.uuid = UUID.fromString(m.getUUID().toString());
        this.measurementSetUUID = UUID.fromString(m.getMeasurementSetUUID().toString());
        this.timeStamp = new Date(m.getTimeStamp().getTime());
        this.value = m.getValue();
    }
    
    /**
     * Constructor to set all the fields of the Measurement object.
     * @param uuid The UUID used to uniquely identify a measurement in this framework.
     * @param measurementSetUUID The UUID of the measurement set that this measurement is a part of.
     * @param timeStamp The time stamp when the measurement was taken.
     * @param value The measurement value
     */
    public Measurement(UUID uuid, UUID measurementSetUUID, Date timeStamp, String value)
    {
        this.uuid = uuid;
        this.measurementSetUUID = measurementSetUUID;
        this.timeStamp = timeStamp;
        this.value = value;
    }

    /**
     * @return the uuid
     */
    public UUID getUUID()
    {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUUID(UUID uuid)
    {
        this.uuid = uuid;
    }
    
    /**
     * @return the measurementSetUUID
     */
    public UUID getMeasurementSetUUID()
    {
        return measurementSetUUID;
    }

    /**
     * @param measurementSetUUID the measurementSetUUID to set
     */
    public void setMeasurementSetUUID(UUID measurementSetUUID)
    {
        this.measurementSetUUID = measurementSetUUID;
    }

    /**
     * @return the timeStamp
     */
    public Date getTimeStamp()
    {
        return timeStamp;
    }

    /**
     * @param timeStamp the timeStamp to set
     */
    public void setTimeStamp(Date timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    /**
     * @return the value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value)
    {
        this.value = value;
    }    
}
