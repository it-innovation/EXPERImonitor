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

#include "Report.h"

#include <boost/uuid/uuid.hpp>
#include <boost/date_time/posix_time/posix_time_types.hpp>



namespace ecc_commonDataModel
{

    /**
     * EMDataBatch encapsulates metric data between a start and end date for a particular
     * MeasurementSet.
     * 
     * @author sgc
     */
    class EMDataBatch : ModelBase
    {
    public:

      typedef boost::shared_ptr<EMDataBatch> ptr_t;

      EMDataBatch();

      virtual ~EMDataBatch();

      /**
       * Getter/Setter for the ID of this batch
       */
      boost::uuids::uuid getBatchID();

      void setBatchID( const boost::uuids::uuid& ID );
    
      /**
       * Getter/Setter for the start date of the first measurement in this set
       */
      boost::posix_time::ptime getExpectedStartStamp();

      void setExpectedStartStamp( const boost::posix_time::ptime& time );

      /**
       * Getter/Setter for the expected Measurement count
       */
      int getExpectedMeasurementCount();

      void setExpectedMeasurementCount( const int& count );
  
      /**
       * Getter/Setter of the associated MeasurementSet for this batch.
       */
      boost::uuids::uuid getExpectedMeasurementSetID();

      void setExpectedMeasurementSetID( boost::uuids::uuid ID );
  
      /**
        * Getter/Setter reflecting the data that was requested for this batch.
        */
      Report::ptr_t getBatchReport();

      void setBatchReport( Report::ptr_t report );

      // ModelBase -----------------------------------------------------------------
      virtual void toJSON( std::wstring& jsonStrOUT );

      virtual void fromJSON( const std::wstring& jsonStr );

      virtual std::wstring toString();

    private:

      boost::uuids::uuid       batchID; 
      boost::posix_time::ptime expectedStartStamp;
      int                      expectedMeasurementCount;
      boost::uuids::uuid       expectedMeasurementSetID;
      Report::ptr_t            batchReport;
    };

} // namespace