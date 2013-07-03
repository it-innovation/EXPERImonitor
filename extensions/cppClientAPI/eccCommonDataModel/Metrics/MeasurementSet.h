/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2013
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
//      Created Date :          21-June-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#pragma once

#include "MeasurementSet.h"
#include "Measurement.h"
#include "Metric.h"




namespace ecc_commonDataModel
{

  /**
   * This class represents a set of measurements for a specific attribute (of an entity),
   * given a specific metric.
   * 
   * @author Vegard Engen
   */
  class MeasurementSet : ModelBase
  {
  public:

    typedef boost::shared_ptr<MeasurementSet> ptr_t;

    typedef boost::unordered_set<MeasurementSet::ptr_t> Set;

    typedef boost::unordered_map<UUID,MeasurementSet::ptr_t> Map;

    enum MEASUREMENT_RULE { eNO_LIVE_MONITOR, eFIXED_COUNT, eINDEFINITE };

    static const int MINIMUM_SAMPLE_RATE_MS = 1000; // milliseconds

    /**
     * Default constructor which sets a random UUID for the object instance.
     */
     MeasurementSet();
    
    /**
      * Copy constructor; does a deep copy of the attribute, metric and measurements.
      * @param ms The metric set object from which a copy is made.
      * @param copyMeasurements Flag indicating whether or not to copy measurements
      */
    MeasurementSet( MeasurementSet::ptr_t ms, const bool copyMeasurements );

    
    /**
      * Constructor to set the UUID of the MeasurementSet.
      */
    MeasurementSet( const UUID& msID );
 
    
    /**
      * Constructor to set the attributes of the Measurement Set except for measurements.
      * @param msID The UUID used to uniquely identify a measurement set in this framework.
      * @param attrID The UUID of the attribute this measurement set defines measurements of.
      * @param metGroupID The UUID of the metric group this measurement set is a part of.
      * @param met The metric of the measurements.
      */
    MeasurementSet( const UUID&   msID, 
                    const UUID&   attrID, 
                    const UUID&   metGroupID, 
                    Metric::ptr_t met );
    
    /**
      * Constructor to set all the attributes of the Measurement Set.
      * @param msID The UUID used to uniquely identify a measurement set in this framework.
      * @param attrID The UUID of the attribute this measurement set defines measurements of.
      * @param metGroupID The UUID of the metric group this measurement set is a part of.
      * @param metric The metric of the measurements.
      * @param measurements The set of measurements.
      */
    MeasurementSet( const UUID&             msID, 
                    const UUID&             attrID, 
                    const UUID&             metGroupID, 
                    Metric::ptr_t           metric, 
                    const Measurement::Set& measures );

    virtual ~MeasurementSet();
    
    /**
      * Getter/Setter for the measurement set's ID
      */
    UUID getID();

    void setID( const UUID& ID );

    /**
      * Getter/Setter for the attribute ID of this measurement set
      */
    UUID getAttributeID();

    void setAttributeID( const UUID& ID );

    /**
      * Getter/Setter of the metric group ID for this measurement set
      */
    UUID getMetricGroupID();

    void setMetricGroupID( const UUID& ID );

    /**
      * Getter/Setter for the Metric related to this measurement set
      */
    Metric::ptr_t getMetric();

    void setMetric( Metric::ptr_t metric );

    /**
    * Getter/setter for the measurements for this set. If the measurement rule for this set is 
    * eFIXED_COUNT then the size of the set must be less than the measurement max count
    * 
    * @param measurements the measurements to set (null values are ignored)
    * 
    * @return returns true if measurements were set
    */
    Measurement::Set getMeasurements();

    bool setMeasurements( const Measurement::Set& measurements );
    
    /**
      * Getter/Setter the measurement rule for this set:
      *   - eNO_LIVE_MONITOR : measurements for this set will not be gathered during Live Monitoring phase
      *   - eFIXED_COUNT     : a finite number of measurements may be added to this set.
      *   - eINDEFINITE      : an indefinite/unlimited number of measurements may be added to this set (default)
      * 
      * @return  - rule for this set.
      */
    MEASUREMENT_RULE getMeasurementRule();

    void setMeasurementRule( const MEASUREMENT_RULE& rule );

    /**
      * Getter/Setter for the measurement count max. Returns a positive integer, depending on the 
      * Measurement Rule for this set:
      *   - eNO_LIVE_MONITOR : 
      *   - eFIXED_COUNT     : the number of measurements in this set
      *   or
      *   - eFIXED_COUNT     : the maximum number of measurements allowed for this set
      * 
      * @return - the number of measurements, depending on Measurement Rule
      */
    unsigned int getMeasurementCountMax();

    bool setMeasurementCountMax( const unsigned int& max );
    
    /**
      * Getter/Setter for the minimum time (in milliseconds) that must elapse before the ECC 
      * should query this measurement set for new data.
      * 
      * @return - interval time, in milliseconds
      */
    long getSampleInterval();

    void setSampleInterval( const long& interval );

    /**
      * Add a measurement to the set. If the measurement rule for this set is eFIXED_COUNT
      * the measurement will only be added if the maximum is not already reached.
      * 
      * @param measurement the measurement to add.
      * 
      * @return returns true if measurement was added
      */
    bool addMeasurement( Measurement::ptr_t measurement );

    /**
      * Add measurements to the set.
      * 
      * @param measurements the set of measurements to add.
      * 
      * @return returns true if all measurements were added
      */
    bool addMeasurements( const Measurement::Set& measurements );

    // ModelBase -----------------------------------------------------------------
    virtual void toJSON( String& jsonStrOUT );

    virtual void fromJSON( const String& jsonStr );

    virtual String toString();

  private:
    
      bool appendMeasurement( Measurement::ptr_t m );

      UUID             msetID;
      UUID             attributeID;
      UUID             metricGroupID;
      Metric::ptr_t    metric;
      Measurement::Set measurements;
    
      MEASUREMENT_RULE measurementRule; 
      unsigned int     measurementCountMax;
      long             samplingInterval;
  };

} // namespace