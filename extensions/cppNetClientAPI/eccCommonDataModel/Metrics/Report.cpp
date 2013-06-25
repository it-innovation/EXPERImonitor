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

#include "stdafx.h"
#include "Report.h"

using namespace boost::uuids;
using namespace std;




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
//class Report
//{    
//    /**
//     * Default constructor which generates a random UUID for the Report object.
//     */
//    public Report()
//    {
//        reportID   = Guid.NewGuid();
//        reportDate = DateTime.Now;
//    }
//    
//    /**
//     * Constructor for the Report class that also creates a MeasurementSet with
//     * 'msID' as the identifier.
//     * 
//     * @param msID - MeasurementSet ID to be used for this report
//     */
//    public Report(Guid msID) : this()
//    {
//        measurementSet = new MeasurementSet( msID );
//    }
//    
//    /**
//     * Copy constructor for the Report class, which takes a deep copy of any objects.
//     * @param report The report object a copy is made of.
//     */
//    public Report(Report report)
//    {
//        if (report == null)
//            return;
//        
//        if (report.reportID != null)
//            this.reportID = new Guid(report.reportID.ToString());
//
//        if (report.measurementSet != null)
//            this.measurementSet = new MeasurementSet(report.measurementSet, true);
//
//        if (report.reportDate != null)
//            this.reportDate = new DateTime(report.reportDate.Ticks);
//
//        if (report.fromDate != null)
//            this.fromDate = new DateTime(report.fromDate.Ticks);
//
//        if (report.toDate != null)
//            this.toDate = new DateTime(report.toDate.Ticks);
//        
//        this.numberOfMeasurements = report.numberOfMeasurements;
//    }
//    
//    /**
//     * Constructor to set the "administrative" attributes of the class.
//     * @param uuid The UUID of the Report, to uniquely identify it.
//     * @param measurementSet The measurement set that this is a report for.
//     * @param reportDate The time stamp for when the report was made.
//     * @param fromDate The time stamp for the start of the report period.
//     * @param toDate The time stamp for the end of the report period.
//     */
//    public Report(Guid uuid, MeasurementSet measurementSet, DateTime reportDate, DateTime fromDate, DateTime toDate)
//    {
//        this.reportID = uuid;
//        this.measurementSet = measurementSet;
//        this.reportDate = reportDate;
//        this.fromDate = fromDate;
//        this.toDate = toDate;
//    }
//    
//    /**
//     * Constructor to set all the attributes of the class.
//     * @param uuid The UUID of the Report, to uniquely identify it.
//     * @param measurementSet The measurement set that this is a report for.
//     * @param reportDate The time stamp for when the report was made.
//     * @param fromDate The time stamp for the start of the report period.
//     * @param toDate The time stamp for the end of the report period.
//     * @param numMeasurements The number of measurements in the reporting period.
//     */
//    public Report(Guid uuid, MeasurementSet measurementSet, DateTime reportDate, DateTime fromDate, DateTime toDate, int numMeasurements)
//        : this(uuid, measurementSet, reportDate, fromDate, toDate)
//    {
//        this.numberOfMeasurements = numMeasurements;
//    }
//
//    public Guid reportID
//    {
//        get;
//        set;
//    }
//
//    public MeasurementSet measurementSet
//    {
//        get;
//        set;
//    }
//
//    [JsonConverter(typeof(ECCDateTimeJSONConverter))]
//    public DateTime reportDate
//    {
//        get;
//        set;
//    }
//
//    [JsonConverter(typeof(ECCDateTimeJSONConverter))]
//    public DateTime fromDate
//    {
//        get;
//        set;
//    }
//
//    [JsonConverter(typeof(ECCDateTimeJSONConverter))]
//    public DateTime toDate
//    {
//        get;
//        set;
//    }
//
//    public int numberOfMeasurements
//    {
//        get;
//        set;
//    }
//    
//    public void copyReport(Report repIn, bool copyMeasurements)
//    {
//        if ( repIn != null )
//        {
//            reportID = repIn.reportID;
//            measurementSet = new MeasurementSet(repIn.measurementSet,copyMeasurements);
//            reportDate = new DateTime( repIn.reportDate.Ticks );
//            fromDate = new DateTime( repIn.fromDate.Ticks );
//            toDate = new DateTime( repIn.toDate.Ticks );
//            numberOfMeasurements = repIn.numberOfMeasurements;
//        }
//    }
//};

// ModelBase -----------------------------------------------------------------
void Report::toJSON( wstring& jsonStrOUT )
{
}

void Report::fromJSON( const wstring& jsonStr )
{
}

wstring Report::toString()
{
  wstring ts;

  return ts;
}

} // namespace