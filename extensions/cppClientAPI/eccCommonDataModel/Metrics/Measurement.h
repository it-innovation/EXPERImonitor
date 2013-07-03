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

#include "ModelBase.h"

#include <boost/uuid/uuid.hpp>
#include <boost/date_time/posix_time/posix_time_types.hpp>



namespace ecc_commonDataModel
{

  /**
   * This class represents a measurement taken at a particular point in time.
   * The measurement is a part of a measurement set for an attribute (of an entity).
   * 
   * @author Vegard Engen
   */
  class Measurement : ModelBase
  {
  public:

    typedef boost::shared_ptr<Measurement> ptr_t;

    typedef boost::unordered_set<Measurement::ptr_t> Set;

    typedef boost::unordered_map<UUID,Measurement::ptr_t> Map_ID;

    typedef boost::unordered_map<TimeStamp,Measurement::ptr_t> Map_Time;

    /**
      * Default constructor, which sets a random UUID for the object instance and
      * sets the synchronised flag to false.
      */
    Measurement();
    
    /**
      * Measurement value constructor; creates an instance and automatically time-stamps 
      * the construction date.
      * 
      * @param value - Value of the measurement to be stored
      */
    Measurement( const String& value );
    
    /**
      * Copy constructor; takes a deep copy of all objects.
      * @param m The measurement object a copy is made from.
      */
    Measurement( Measurement::ptr_t m );
    
    /**
      * Constructor to set all the fields of the Measurement object.
      * @param uuid The UUID used to uniquely identify a measurement in this framework.
      * @param measurementSetUUID The UUID of the measurement set that this measurement is a part of.
      * @param timeStamp The time stamp when the measurement was taken.
      * @param value The measurement value
      */
    Measurement( const UUID&      uuid, 
                 const UUID&      msetUUID, 
                 const TimeStamp& timeStamp, 
                 const String&    value );
    
    /**
      * Constructor to set all the fields of the Measurement object.
      * @param uuid The UUID used to uniquely identify a measurement in this framework.
      * @param msetUUID The UUID of the measurement set that this measurement is a part of.
      * @param timeStamp The time stamp when the measurement was taken.
      * @param value The measurement value
      */
    Measurement( const UUID&      uuid, 
                 const UUID&      msetUUID, 
                 const TimeStamp& timeStamp, 
                 const String&    value, 
                 const bool       synchronised );

    virtual ~Measurement();

    /**
     * Getter/Setter for the ID for the Measurement
     */
    UUID getUUID();

    void setUUID( const UUID& ID );

    /**
     * Getter/Setter for MeasurementSet ID this measurement belongs to
     */
    UUID getMeasurementSetUUID();

    void setMeasurementSetUUID( const UUID& ID );

    /**
     * Getter/Setter for time stamp associated with this measurement
     */
    TimeStamp getTimeStamp();

    void setTimeStamp( const TimeStamp& stamp );

    /**
     * Getter/Setter for measurement value for this measurement
     */
    String getValue();

    void setValue( const String& value );

    /**
     * Getter/Setter for the synchronized status (with the ECC) of this measurement
     */
    bool getSynchronised();

    void setSynchronised( const bool& synchronised );

    // ModelBase -----------------------------------------------------------------
    virtual void toJSON( String& jsonStrOUT );

    virtual void fromJSON( const String& jsonStr );

    virtual String toString();

  private:

    UUID      measurementUUID;
    UUID      measurementSetUUID;
    TimeStamp measurementTimeStamp;
    String    measurementValue;
    bool      measurementSynchronised;

  };
    
} // namespace
