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
//      Created Date :          21-June-2013
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

#pragma once

#include "MeasurementSet.h"

#include <boost/uuid/uuid.hpp>
#include <boost/date_time/posix_time/posix_time_types.hpp>




namespace ecc_commonDataModel
{

  /**
    * A report class that provides a means of transferring measurements, which 
    * contains meta-information about a measurement set and the measurements for it 
    * for a given time period
    * 
    * Currently only the number of measurements is supported.
    * 
    * @author Vegard Engen
    */
  class Report : ModelBase
  {
  public:

    typedef boost::shared_ptr<Report> ptr_t;

    Report();

    /**
      * Constructor for the Report class that also creates a MeasurementSet with
      * 'msID' as the identifier.
      */
    Report( const boost::uuids::uuid& msID );

    /**
      * Copy constructor for the Report class, which takes a deep copy of any objects.
      * @param report The report object a copy is made of.
      */
    Report( Report::ptr_t report );

    /**
      * Constructor to set the "administrative" attributes of the class.
      * @param uuid The UUID of the Report, to uniquely identify it.
      * @param measurementSet The measurement set that this is a report for.
      * @param reportDate The time stamp for when the report was made.
      * @param fromDate The time stamp for the start of the report period.
      * @param toDate The time stamp for the end of the report period.
      */
    Report( const boost::uuids::uuid&      uuid, 
            MeasurementSet::ptr_t          measurementSet, 
            const boost::posix_time::ptime reportDate, 
            const boost::posix_time::ptime fromDate, 
            const boost::posix_time::ptime toDate );

    virtual ~Report();
    
    /**
      * Constructor to set all the attributes of the class.
      * @param uuid The UUID of the Report, to uniquely identify it.
      * @param measurementSet The measurement set that this is a report for.
      * @param reportDate The time stamp for when the report was made.
      * @param fromDate The time stamp for the start of the report period.
      * @param toDate The time stamp for the end of the report period.
      * @param numMeasurements The number of measurements in the reporting period.
      */
    Report( const boost::uuids::uuid&       uuid, 
            MeasurementSet                  measurementSet, 
            const boost::posix_time::ptime& reportDate, 
            const boost::posix_time::ptime& fromDate, 
            const boost::posix_time::ptime& toDate, 
            const int&                      numMeasurements );

    /**
      * Getter/Setter for the report ID
      */
    boost::uuids::uuid getReportID();

    void setReportID( const boost::uuids::uuid& ID );

    /**
      * Getter/Setter for the measurement set associated with this report
      */
    MeasurementSet::ptr_t getMeasurementSet();
   
    void setMeasurementSet( MeasurementSet::ptr_t ms );

    /**
      * Getter/Setter for the report date
      */
    boost::posix_time::ptime getReportDate();
        
    void setReportDate( const boost::posix_time::ptime& date ); 

    /**
      * Getter/Setter for start date of the measurements contained in this report
      */
    boost::posix_time::ptime getReportFromDate();
        
    void setReportFromDate( const boost::posix_time::ptime& date ); 

    /**
      * Getter/Setter for the end date of the measurements contained in this report
      */
    boost::posix_time::ptime getReportToDate();
        
    void setReportToDate( const boost::posix_time::ptime& date ); 

    /**
      * Getter/Setter for the number of measurements contained in this report
      */
    int getNumberOfMeasurements();

    void setNumberOfMeasurements( const int& mCount );

    /**
      * Copy the report provided by repIn; optional to also copy the measurements
      */
    void copyReport( Report::ptr_t repIn, bool copyMeasurements );

    // ModelBase -----------------------------------------------------------------
    virtual void toJSON( std::wstring& jsonStrOUT );

    virtual void fromJSON( const std::wstring& jsonStr );

    virtual std::wstring toString();

  private:
    boost::uuids::uuid       reportID;
    MeasurementSet::ptr_t    measurementSet;
    boost::posix_time::ptime reportDate;
    boost::posix_time::ptime fromDate;
    boost::posix_time::ptime toDate;
    int                      numberOfMeasurements;

  };
    
} // namespace