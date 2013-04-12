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
//      Created Date :          09-Apr-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

using System;
using Newtonsoft.Json;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics
{

/**
 * This class represents a measurement taken at a particular point in time.
 * The measurement is a part of a measurement set for an attribute (of an entity).
 * 
 * @author Vegard Engen
 */
public class Measurement
{    
    /**
     * Default constructor, which sets a random UUID for the object instance and
     * sets the synchronised flag to false.
     */
    public Measurement()
    {
        this.uuid         = Guid.NewGuid();
        this.timeStamp    = DateTime.Now;
        this.synchronised = false;
    }
    
    /**
     * Measurement value constructor; creates an instance and automatically time-stamps 
     * the construction date.
     * 
     * @param value - Value of the measurement to be stored
     */
    public Measurement(string value) : this()
    {
        this.value = value;
    }
    
    /**
     * Copy constructor; takes a deep copy of all objects.
     * @param m The measurement object a copy is made from.
     */
    public Measurement(Measurement m)
    {
        if (m == null) {
            return;
        }
        
        if (m.uuid != null) {
            this.uuid = new Guid( m.uuid.ToString() );
        }
        if (m.measurementSetUUID != null) {
            this.measurementSetUUID = new Guid( m.measurementSetUUID.ToString() );
        }
        if (m.timeStamp != null) {
            this.timeStamp = new DateTime( m.timeStamp.Ticks );
        }
        
        this.value = m.value;
        this.synchronised = m.synchronised;
    }
    
    /**
     * Constructor to set all the fields of the Measurement object.
     * @param uuid The UUID used to uniquely identify a measurement in this framework.
     * @param measurementSetUUID The UUID of the measurement set that this measurement is a part of.
     * @param timeStamp The time stamp when the measurement was taken.
     * @param value The measurement value
     */
    public Measurement(Guid uuid, Guid measurementSetUUID, DateTime timeStamp, string value)
    {
        this.uuid = uuid;
        this.measurementSetUUID = measurementSetUUID;
        this.timeStamp = timeStamp;
        this.value = value;

        this.synchronised = false;
    }
    
    /**
     * Constructor to set all the fields of the Measurement object.
     * @param uuid The UUID used to uniquely identify a measurement in this framework.
     * @param measurementSetUUID The UUID of the measurement set that this measurement is a part of.
     * @param timeStamp The time stamp when the measurement was taken.
     * @param value The measurement value
     */
    public Measurement(Guid uuid, Guid measurementSetUUID, DateTime timeStamp, string value, bool synchronised)
        : this(uuid, measurementSetUUID, timeStamp, value)
    {
        this.synchronised = synchronised;
    }

    public Guid uuid
    {
        get;
        set;
    }

    public Guid measurementSetUUID
    {
        get;
        set;
    }

    [JsonConverter(typeof(ECCDateTimeJSONConverter))]
    public DateTime timeStamp
    {
        get;
        set;
    }

    public string value
    {
        get;
        set;
    }

    public bool synchronised
    {
        get;
        set;
    }
}
    
} // namespace
