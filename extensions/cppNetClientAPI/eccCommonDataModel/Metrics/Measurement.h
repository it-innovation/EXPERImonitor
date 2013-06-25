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
    Measurement( const std::wstring& value );
    
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
    Measurement( const boost::uuids::uuid&       uuid, 
                 const boost::uuids::uuid&       measurementSetUUID, 
                 const boost::posix_time::ptime& timeStamp, 
                 const std::wstring&             value );
    
    /**
      * Constructor to set all the fields of the Measurement object.
      * @param uuid The UUID used to uniquely identify a measurement in this framework.
      * @param measurementSetUUID The UUID of the measurement set that this measurement is a part of.
      * @param timeStamp The time stamp when the measurement was taken.
      * @param value The measurement value
      */
    Measurement( const boost::uuids::uuid&       uuid, 
                 const boost::uuids::uuid&       measurementSetUUID, 
                 const boost::posix_time::ptime& timeStamp, 
                 const std::wstring&             value, 
                 const bool                      synchronised );

    virtual ~Measurement();

    /**
     * Getter/Setter for the ID for the Measurement
     */
    boost::uuids::uuid getUUID();

    void setUUID( const boost::uuids::uuid& ID );

    /**
     * Getter/Setter for MeasurementSet ID this measurement belongs to
     */
    boost::uuids::uuid getMeasurementSetUUID();

    void setMeasurementSetUUID( const boost::uuids::uuid& ID );

    /**
     * Getter/Setter for time stamp associated with this measurement
     */
    boost::posix_time::ptime getTimeStamp();

    void setTimeStamp( const boost::posix_time::ptime& stamp );

    /**
     * Getter/Setter for measurement value for this measurement
     */
    std::wstring getValue();

    void setValue( const std::wstring& value );

    /**
     * Getter/Setter for the synchronized status (with the ECC) of this measurement
     */
    bool getSynchronised();

    void setSynchronised( const bool& synchronised );

    // ModelBase -----------------------------------------------------------------
    virtual void toJSON( std::wstring& jsonStrOUT );

    virtual void fromJSON( const std::wstring& jsonStr );

    virtual std::wstring toString();

  private:

  };
    
} // namespace
