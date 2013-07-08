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
    */
  class Report : public ModelBase
  {
  public:

    typedef boost::shared_ptr<Report> ptr_t;

    typedef boost::container::map<UUID, Report::ptr_t> Map;

    Report();

    /**
      * Constructor for the Report class that also creates a MeasurementSet with
      * 'msID' as the identifier.
      */
    Report( const UUID& msID );

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
    Report( const UUID&           uuid, 
            MeasurementSet::ptr_t measurementSet, 
            const TimeStamp&      reportDate, 
            const TimeStamp&      fromDate, 
            const TimeStamp&      toDate );

    /**
      * Constructor to set all the attributes of the class.
      * @param uuid The UUID of the Report, to uniquely identify it.
      * @param measurementSet The measurement set that this is a report for.
      * @param reportDate The time stamp for when the report was made.
      * @param fromDate The time stamp for the start of the report period.
      * @param toDate The time stamp for the end of the report period.
      * @param numMeasurements The number of measurements in the reporting period.
      */
    Report( const UUID&                 uuid, 
            const MeasurementSet::ptr_t measurementSet, 
            const TimeStamp&            reportDate, 
            const TimeStamp&            fromDate, 
            const TimeStamp&            toDate, 
            const int&                  numMeasurements );

    virtual ~Report();

    /**
      * Getter/Setter for the report ID
      */
    UUID getReportID();

    void setReportID( const UUID& ID );

    /**
      * Getter/Setter for the measurement set associated with this report
      */
    MeasurementSet::ptr_t getMeasurementSet();
   
    void setMeasurementSet( MeasurementSet::ptr_t ms );

    /**
      * Getter/Setter for the report date
      */
    TimeStamp getReportDate();
        
    void setReportDate( const TimeStamp& date ); 

    /**
      * Getter/Setter for start date of the measurements contained in this report
      */
    TimeStamp getReportFromDate();
        
    void setReportFromDate( const TimeStamp& date ); 

    /**
      * Getter/Setter for the end date of the measurements contained in this report
      */
    TimeStamp getReportToDate();
        
    void setReportToDate( const TimeStamp& date ); 

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
    virtual String toJSON();

    virtual void fromJSON( const ModelBase::JSONTree& jsonTree );

    virtual String toString();

  private:
    UUID                  reportID;
    MeasurementSet::ptr_t measurementSet;
    TimeStamp             reportDate;
    TimeStamp             fromDate;
    TimeStamp             toDate;
    int                   numberOfMeasurements;

  };
    
} // namespace