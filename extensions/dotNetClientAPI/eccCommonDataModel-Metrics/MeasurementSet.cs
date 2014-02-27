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
using System.Collections.Generic;

using Newtonsoft.Json;
using Newtonsoft.Json.Converters;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics
{

/**
 * This class represents a set of measurements for a specific attribute (of an entity),
 * given a specific metric.
 * 
 * @author Simon Crowle
 */
public class MeasurementSet
{
    private MEASUREMENT_RULE msMeasurementRule;
    private int msMeasurementCountMax;

    private HashSet<Measurement> setMeasurements;

    [JsonConverter(typeof(StringEnumConverter))]
    public enum MEASUREMENT_RULE { eNO_LIVE_MONITOR, eFIXED_COUNT, eINDEFINITE };
    
    public static int MINIMUM_SAMPLE_RATE_MS = 1000; // milliseconds
    
    
    /**
     * Default constructor which sets a random UUID for the object instance.
     */
    public MeasurementSet()
    {
        msetID = Guid.NewGuid();
        measurements = new HashSet<Measurement>();

        measurementRule  = MEASUREMENT_RULE.eINDEFINITE;
        samplingInterval = MINIMUM_SAMPLE_RATE_MS;
    }
    
    /**
     * Copy constructor; does a deep copy of the attribute, metric and measurements.
     * @param ms The metric set object from which a copy is made.
     * @param copyMeasurements Flag indicating whether or not to copy measurements
     */
    public MeasurementSet( MeasurementSet ms, bool copyMeasurements ) : this()
    {
        if ( ms == null ) return;

        msetID                  = ms.msetID;
        attributeID         = ms.attributeID;
        metricGroupID       = ms.metricGroupID;
        measurementRule     = ms.measurementRule;
        measurementCountMax = ms.measurementCountMax;
        samplingInterval    = ms.samplingInterval;
        
        if ( ms.metric != null ) metric = new Metric( ms.metric );
        
        if ( copyMeasurements && ms.measurements != null )
        {
            foreach ( Measurement m in ms.measurements )
              appendMeasurement( m );
        }
    }
    
    /**
     * Constructor to set the UUID of the MeasurementSet.
     */
    public MeasurementSet( Guid msID ) : this()
    {
        msetID = msID;
    }
    
    /**
     * Constructor to set the attributes of the Measurement Set except for measurements.
     * @param msID The UUID used to uniquely identify a measurement set in this framework.
     * @param attrID The UUID of the attribute this measurement set defines measurements of.
     * @param metGroupID The UUID of the metric group this measurement set is a part of.
     * @param met The metric of the measurements.
     */
    public MeasurementSet(Guid msID, Guid attrID, Guid metGroupID, Metric met) : this( msID )
    {            
        attributeID   = attrID;
        metricGroupID = metGroupID;
        metric        = met;
    }
    
    /**
     * Constructor to set all the attributes of the Measurement Set.
     * @param msID The UUID used to uniquely identify a measurement set in this framework.
     * @param attrID The UUID of the attribute this measurement set defines measurements of.
     * @param metGroupID The UUID of the metric group this measurement set is a part of.
     * @param metric The metric of the measurements.
     * @param measurements The set of measurements.
     */
    public MeasurementSet( Guid msID, Guid attrID, Guid metGroupID, 
                           Metric metric, HashSet<Measurement> measures )
        : this( msID, attrID, metGroupID, metric )
    {
        if ( measures != null )
            foreach ( Measurement m in measures )
                appendMeasurement( m );
    }

    public Guid msetID
    {
        get;
        set;
    }

    public Guid attributeID
    {
        get;
        set;
    }

    public Guid metricGroupID
    {
        get;
        set;
    }

    public Metric metric
    {
        get;
        set;
    }

    /**
    * Sets the measurements for this set. If the measurement rule for this set is 
    * eFIXED_COUNT then the size of the set must be less than the measurement max count
    * 
    * @param measurements the measurements to set (null values are ignored)
    * 
    * @return returns true if measurements were set
    */
    public HashSet<Measurement> measurements
    {
        get { return setMeasurements; }

        set
        {
            bool setMeasures = false;

            if (value != null)
            {
                if (measurementRule != MEASUREMENT_RULE.eFIXED_COUNT)
                    setMeasures = true;
                else
                    if (value != null && value.Count < measurementCountMax)
                        setMeasures = true;
            }

            if (setMeasures) setMeasurements = value;
        }
    }
    
    /**
     * Returns the measurement rule for this set:
     *   - eNO_LIVE_MONITOR : measurements for this set will not be gathered during Live Monitoring phase
     *   - eFIXED_COUNT     : a finite number of measurements may be added to this set.
     *   - eINDEFINITE      : an indefinite/unlimited number of measurements may be added to this set (default)
     * 
     * @return  - rule for this set.
     */
    public MEASUREMENT_RULE measurementRule
    {
        get
        {
            return msMeasurementRule;
        }

        set
        {
            msMeasurementRule = value;
        }
    }
    
    /**
     * Returns a positive integer, depending on the Measurement Rule for this set:
     *   - eNO_LIVE_MONITOR : 
     *   - eFIXED_COUNT     : the number of measurements in this set
     *   or
     *   - eFIXED_COUNT     : the maximum number of measurements allowed for this set
     * 
     * @return - the number of measurements, depending on Measurement Rule
     */

    public int measurementCountMax
    {
        get
        {
            if ( msMeasurementRule == MEASUREMENT_RULE.eFIXED_COUNT )
                return msMeasurementCountMax;

            return setMeasurements.Count;
        }

        set
        {
            // Safety first
            if ( msMeasurementRule != MEASUREMENT_RULE.eFIXED_COUNT ) return;
            if ( value < 1 || setMeasurements.Count > value ) return;

            msMeasurementCountMax = value;
        }
    }
    
    /**
     * Return this minimum time (in milliseconds) that must elapse before the ECC 
     * should query this measurement set for new data.
     * 
     * @return - interval time, in milliseconds
     */
    public long samplingInterval
    {
        get;
        set;
    }
    
    /**
     * Add a measurement to the set. If the measurement rule for this set is eFIXED_COUNT
     * the measurement will only be added if the maximum is not already reached.
     * 
     * @param measurement the measurement to add.
     * 
     * @return returns true if measurement was added
     */
    public bool addMeasurement( Measurement measurement )
    {
        if ( measurement == null ) return false;

        if (setMeasurements == null) setMeasurements = new HashSet<Measurement>();
        
        return appendMeasurement( measurement );
    }
    
    /**
     * Add measurements to the set.
     * 
     * @param measurements the set of measurements to add.
     * 
     * @return returns true if all measurements were added
     */
    public bool addMeasurements( Dictionary<Guid,Measurement> measurements )
    {
        if ( (measurements == null) || measurements.Count == 0 ) return false;

        if (setMeasurements == null) setMeasurements = new HashSet<Measurement>();

        bool skippedMeasurement = false;

        foreach (Measurement m in measurements.Values)
            if ( !appendMeasurement( m ) ) skippedMeasurement = true;
        
        return (!skippedMeasurement);
    }
    
    // Private methods ---------------------------------------------------------
    private bool appendMeasurement( Measurement m )
    {
        bool addMeasure = false;
        
        if ( measurementRule != MEASUREMENT_RULE.eFIXED_COUNT )
          addMeasure = true;
        else
            if (setMeasurements.Count < measurementCountMax) addMeasure = true;
        
        if ( addMeasure )
        {
            m.measurementSetUUID = msetID;
            setMeasurements.Add(m);
        }
        
        return addMeasure;
    }
}

} // namespace