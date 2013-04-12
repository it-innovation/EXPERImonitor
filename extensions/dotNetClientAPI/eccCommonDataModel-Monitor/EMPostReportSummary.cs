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

using uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics;

using System;
using System.Collections.Generic;




namespace uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor
{

    /**
     * EMPostReportSummary encapsulates Reports of MeasurementSets describing the
     * range of the data generated by the user client during the course of the experiment.
     * 
     * @author sgc
     */
    public class EMPostReportSummary
    {
        private Dictionary<Guid, Report> reportsByMeasurementSetID;

        public EMPostReportSummary()
        {
            reportsByMeasurementSetID = new Dictionary<Guid, Report>();
        }

        /**
         * Returns a set of all the MeasurementSet IDs referred to by this summary report.
         * 
         * @return - Set of IDs for the MeasurementSets in this summary
         */
        public ICollection<Guid> getReportedMeasurementSetIDs()
        { return reportsByMeasurementSetID.Keys; }

        /**
         * Adds a report to this summary. The report instance should contain correct
         * references to the MeasurementSet IDs.
         * 
         * @param report - Report instance (fields must not be null)
         */
        public void addReport(Report report)
        {
            if (report != null)
            {
                MeasurementSet ms = report.measurementSet;

                if (ms != null)
                    reportsByMeasurementSetID.Add(ms.msetID, report);
            }
        }

        /**
         * Removes the Report associated with the MeasurementSet ID
         * 
         * @param measurementSetID - ID of the MeasurementSet
         */
        public void removeReport(Guid measurementSetID)
        {
            if (measurementSetID != null)
                reportsByMeasurementSetID.Remove(measurementSetID);
        }

        /**
         * Gets the report associated with the MeasurementSet ID
         * 
         * @param measurementID - MeasurementSet ID
         * @return              - Report instance detailing the metric data for the MeasurementSet ID
         */
        public Report getReport(Guid measurementID)
        {
            Report report = null;

            if (measurementID != null)
                report = reportsByMeasurementSetID[measurementID];

            return report;
        }
    }

} // namespace
