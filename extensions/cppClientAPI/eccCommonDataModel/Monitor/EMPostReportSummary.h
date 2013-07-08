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

#include "Report.h"




namespace ecc_commonDataModel
{

/**
  * EMPostReportSummary encapsulates Reports of MeasurementSets describing the
  * range of the data generated by the user client during the course of the experiment.
  * 
  * @author sgc
  */
class EMPostReportSummary : ModelBase
{
public:
        
    typedef boost::shared_ptr<EMPostReportSummary> ptr_t;

    EMPostReportSummary();

    virtual ~EMPostReportSummary();

    /**
      * Returns a set of all the MeasurementSet IDs referred to by this summary report.
      * 
      * @return - Set of IDs for the MeasurementSets in this summary
      */
    UUIDSet getReportedMeasurementSetIDs();

    /**
      * Adds a report to this summary. The report instance should contain correct
      * references to the MeasurementSet IDs.
      * 
      * @param report - Report instance (fields must not be null)
      */
    void addReport( Report::ptr_t report );

    /**
      * Removes the Report associated with the MeasurementSet ID
      * 
      * @param measurementSetID - ID of the MeasurementSet
      */
    void removeReport( const boost::uuids::uuid& measurementSetID );

    /**
      * Gets the report associated with the MeasurementSet ID
      * 
      * @param measurementID - MeasurementSet ID
      * @return              - Report instance detailing the metric data for the MeasurementSet ID
      */
    Report::ptr_t getReport( const boost::uuids::uuid& measurementID );

    // ModelBase -----------------------------------------------------------------
    virtual void toJSON( std::wstring& jsonStrOUT );

    virtual void fromJSON( const std::wstring& jsonStr );

    virtual std::wstring toString();

private:

  Report::Map reportsByMeasurementSetID;

};

} // namespace
