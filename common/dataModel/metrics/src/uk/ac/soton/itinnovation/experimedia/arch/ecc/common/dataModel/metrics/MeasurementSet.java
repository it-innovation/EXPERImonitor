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
//      Created By :            Vegard Engen/Simon Crowle
//      Created Date :          2012-08-09
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics;

import java.io.Serializable;
import java.util.*;




/**
 * This class represents a set of measurements for a specific attribute (of an entity),
 * given a specific metric.
 * 
 * @author Vegard Engen
 */
public class MeasurementSet implements Serializable
{
    private UUID             msetID;
    private UUID             attributeID;
    private UUID             metricGroupID;
    private Metric           metric;
    private Set<Measurement> measurements;
    
    private MEASUREMENT_RULE measurementRule = MEASUREMENT_RULE.eINDEFINITE;
    private int              measurementCountMax;
    private long             samplingInterval = MINIMUM_SAMPLE_RATE_MS; // Default rate of 1 sample/second
    
    public enum       MEASUREMENT_RULE { eNO_LIVE_MONITOR, eFIXED_COUNT, eINDEFINITE };
    public static int MINIMUM_SAMPLE_RATE_MS = 1000; // milliseconds
    
    
    /**
     * Default constructor which sets a random UUID for the object instance.
     */
    public MeasurementSet()
    {
        msetID = UUID.randomUUID();
        measurements = new HashSet<Measurement>();
    }
    
    /**
     * Copy constructor; does a deep copy of the attribute, metric and measurements.
     * @param ms The metric set object from which a copy is made.
     * @param copyMeasurements Flag indicating whether or not to copy measurements
     */
    public MeasurementSet( MeasurementSet ms, boolean copyMeasurements )
    {
        this();
        
        if ( ms == null ) return;
        
        msetID              = ms.getID();
        attributeID         = ms.getAttributeID();
        metricGroupID       = ms.getMetricGroupID();
        measurementRule     = ms.getMeasurementRule();
        measurementCountMax = ms.getMeasurementCountMax();
        samplingInterval    = ms.getSamplingInterval();
        
        if ( ms.getMetric() != null ) metric = new Metric( ms.getMetric() );
        
        if ( copyMeasurements && ms.getMeasurements() != null )
        {
            for ( Measurement m : ms.getMeasurements() )
              appendMeasurement( m );
        }
    }
    
    /**
     * Constructor to set the UUID of the MeasurementSet.
     */
    public MeasurementSet( UUID msID )
    {
        this();
        
        msetID = msID;
    }
    
    /**
     * Constructor to set the attributes of the Measurement Set except for measurements.
     * @param msID The UUID used to uniquely identify a measurement set in this framework.
     * @param attrID The UUID of the attribute this measurement set defines measurements of.
     * @param metGroupID The UUID of the metric group this measurement set is a part of.
     * @param met The metric of the measurements.
     */
    public MeasurementSet( UUID msID, UUID attrID, UUID metGroupID, Metric met )
    {
        this( msID );
                
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
    public MeasurementSet( UUID msID, UUID attrID, UUID metGroupID, 
                           Metric metric, Set<Measurement> measures )
    {
        this( msID, attrID, metGroupID, metric );
        
        if ( measures != null )
            for ( Measurement m : measures )
                appendMeasurement( m );
    }
    
    /**
     * @return the MeasurementSet ID
     */
    public UUID getID()
    {   return msetID;    }

    /**
     * @param msID Set the MeasurementSet ID
     */
    public void setID( UUID msID )
    {    msetID = msID;    }

    /**
     * @return the associated attribute ID
     */
    public UUID getAttributeID()
    {   return attributeID;   }

    /**
     * @param id the associated attribute ID
     */
    public void setAttributeUUID( UUID id )
    {   attributeID = id;   }
    
    /**
     * @return the associated metric group ID
     */
    public UUID getMetricGroupID()
    {   return metricGroupID;   }

    /**
     * @param metricGroupUUID the associated metric group ID to set
     */
    public void setMetricGroupUUID( UUID id )
    {   metricGroupID = id;   }
    
    /**
     * @return the metric
     */
    public Metric getMetric()
    {   return metric;    }

    /**
     * @param met the metric to set
     */
    public void setMetric( Metric met )
    {   metric = met;   }

    /**
     * @return the measurements
     */
    public Set<Measurement> getMeasurements()
    {   return measurements;    }
    
    /**
     * Returns the measurement rule for this set:
     *   - eNO_LIVE_MONITOR : measurements for this set will not be gathered during Live Monitoring phase
     *   - eFIXED_COUNT     : a finite number of measurements may be added to this set.
     *   - eINDEFINITE      : an indefinite/unlimited number of measurements may be added to this set (default)
     * 
     * @return  - rule for this set.
     */
    public MEASUREMENT_RULE getMeasurementRule()
    {   return measurementRule;   }
    
    /**
     * Sets the measurement rule of this set:
     *   - eNO_LIVE_MONITOR : measurements for this set will not be gathered during Live Monitoring phase
     *   - eFIXED_COUNT     : a finite number of measurements may be added to this set.
     *   - eINDEFINITE      : an indefinite/unlimited number of measurements may be added to this set (default)
     * 
     * @param per - rule for this set.
     */
    public void setMeasurementRule( MEASUREMENT_RULE per )
    {   measurementRule = per;    }
    
    /**
     * Returns a positive integer, depending on the Measurement Rule for this set:
     *   - eNO_LIVE_MONITOR : 
     *   - eFIXED_COUNT     : the number of measurements in this set
     *   or
     *   - eFIXED_COUNT     : the maximum number of measurements allowed for this set
     * 
     * @return - the number of measurements, depending on Measurement Rule
     */
    public int getMeasurementCountMax()
    {
        if ( measurementRule == MEASUREMENT_RULE.eFIXED_COUNT )
            return measurementCountMax;
          
        return measurements.size();
    }
    
    /**
     * Set the measurement count maximum only if:
     *   * The measurement rule is of type eFIXED_COUNT
     *   * The current number of measurements is less than 'max'
     *   * 'max' is a number greater than zero
     * 
     * @param max - the maximum number of measurements allowed for this set
     * 
     * @return returns true if maximum was set 
     */
    public boolean setMeasurementCountMax( int max )
    {
        // Safety first
        if ( measurementRule != MEASUREMENT_RULE.eFIXED_COUNT ) return false;
        if ( max < 1 || measurements.size() > max )             return false;
        
        measurementCountMax = max;
        
        return true;
    }

    /**
     * Sets the measurements for this set. If the measurement rule for this set is 
     * eFIXED_COUNT then the size of the set must be less than the measurement max count
     * 
     * @param measurements the measurements to set (null values are ignored)
     * 
     * @return returns true if measurements were set
     */
    public boolean setMeasurements( Set<Measurement> measures )
    {
        boolean setMeasures = false;
        
        if ( measures != null )
        {
            if ( measurementRule != MEASUREMENT_RULE.eFIXED_COUNT )
                setMeasures = true;
            else
                if ( measures != null && measures.size() < measurementCountMax )
                    setMeasures = true;
        }
        
        if ( setMeasures ) measurements = measures;
        
        return setMeasures;
    }
    
    /**
     * Return this minimum time (in milliseconds) that must elapse before the ECC 
     * should query this measurement set for new data.
     * 
     * @return - interval time, in milliseconds
     */
    public long getSamplingInterval()
    {   return samplingInterval;    }
    
    /**
     * Sets the minimum time (in milliseconds) that must elapse before the ECC 
     * should query this measurement set for new data
     * 
     * @param sampInt - minimum time elapsed (in milliseconds), cannot drop below MINIMUM_SAMPLE_RATE_MS
     */
    public void setSamplingInternval( long sampInt )
    {
      if ( sampInt >= MINIMUM_SAMPLE_RATE_MS ) samplingInterval = sampInt;
    }
    
    /**
     * Add a measurement to the set. If the measurement rule for this set is eFIXED_COUNT
     * the measurement will only be added if the maximum is not already reached.
     * 
     * @param measurement the measurement to add.
     * 
     * @return returns true if measurement was added
     */
    public boolean addMeasurement( Measurement measurement )
    {
        if ( measurement == null ) return false;
        
        if ( measurements == null ) measurements = new HashSet<Measurement>();
        
        return appendMeasurement( measurement );
    }
    
    /**
     * Add measurements to the set.
     * 
     * @param measurements the set of measurements to add.
     * 
     * @return returns true if all measurements were added
     */
    public boolean addMeasurements( Set<Measurement> measurements )
    {
        if ( (measurements == null) || measurements.isEmpty() ) return false;
        
        if ( measurements == null ) measurements = new HashSet<Measurement>();
        
        Iterator<Measurement> measureIt = measurements.iterator();
        while ( measureIt.hasNext() )
        { appendMeasurement( measureIt.next() ); }
        
        if ( measureIt.hasNext() ) return false; // Did not add all measurements
        
        return false;
    }
    
    // Private methods ---------------------------------------------------------
    private boolean appendMeasurement( Measurement m )
    {
        boolean addMeasure = false;
        
        if ( measurementRule != MEASUREMENT_RULE.eFIXED_COUNT )
          addMeasure = true;
        else
            if ( measurements.size() < measurementCountMax ) addMeasure = true;
        
        if ( addMeasure )
        {
            m.setMeasurementSetUUID( msetID );
            measurements.add( m );
        }
        
        return addMeasure;
    }
}
