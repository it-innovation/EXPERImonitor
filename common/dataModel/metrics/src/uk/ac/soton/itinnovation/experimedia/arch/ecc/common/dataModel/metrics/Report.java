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
//      Created By :            Vegard Engen
//      Created Date :          2012-08-17
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics;

import java.util.Date;
import java.util.UUID;

/**
 * A report class, which contains meta-information about a measurement set.
 * 
 * Currently only the number of measurements is supported.
 * 
 * @author Vegard Engen
 */
public class Report
{
    private UUID uuid;
    private MeasurementSet measurementSet;
    private Date fromDate;
    private Date toDate;
    private int numberOfMeasurements;
    
    /**
     * Default constructor which generates a random UUID for the Report object.
     */
    public Report()
    {
        this.uuid = UUID.randomUUID();
    }
    
    /**
     * Copy constructor for the Report class, which takes a deep copy of any objects.
     * @param report The report object a copy is made of.
     */
    public Report(Report report)
    {
        if (report == null)
            return;
        
        if (report.getUUID() != null)
            this.uuid = UUID.fromString(report.getUUID().toString());
        if (report.getMeasurementSet() != null)
            this.measurementSet = new MeasurementSet(report.getMeasurementSet());
        if (report.getFromDate() != null)
            this.fromDate = new Date(report.getFromDate().getTime());
        if (report.getToDate() != null)
            this.toDate = new Date(report.getToDate().getTime());
        this.numberOfMeasurements = report.getNumberOfMeasurements();
    }
    
    /**
     * Constructor to set the "administrative" attributes of the class.
     * @param uuid The UUID of the Report, to uniquely identify it.
     * @param measurementSet The measurement set that this is a report for.
     * @param fromDate The time stamp for the start of the report period.
     * @param toDate The time stamp for the end of the report period.
     */
    public Report(UUID uuid, MeasurementSet measurementSet, Date fromDate, Date toDate)
    {
        this.uuid = uuid;
        this.measurementSet = measurementSet;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }
    
    /**
     * Constructor to set all the attributes of the class.
     * @param uuid The UUID of the Report, to uniquely identify it.
     * @param measurementSet The measurement set that this is a report for.
     * @param fromDate The time stamp for the start of the report period.
     * @param toDate The time stamp for the end of the report period.
     * @param numMeasurements The number of measurements in the reporting period.
     */
    public Report(UUID uuid, MeasurementSet measurementSet, Date fromDate, Date toDate, int numMeasurements)
    {
        this(uuid, measurementSet, fromDate, toDate);
        this.numberOfMeasurements = numMeasurements;
    }
    
    /**
     * @return the uuid
     */
    public UUID getUUID()
    {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUUID(UUID uuid)
    {
        this.uuid = uuid;
    }

    /**
     * @return the measurementSet
     */
    public MeasurementSet getMeasurementSet()
    {
        return measurementSet;
    }

    /**
     * @param measurementSet the measurementSet to set
     */
    public void setMeasurementSet(MeasurementSet measurementSet)
    {
        this.measurementSet = measurementSet;
    }

    /**
     * @return the fromDate
     */
    public Date getFromDate()
    {
        return fromDate;
    }

    /**
     * @param fromDate the fromDate to set
     */
    public void setFromDate(Date fromDate)
    {
        this.fromDate = fromDate;
    }

    /**
     * @return the toDate
     */
    public Date getToDate()
    {
        return toDate;
    }

    /**
     * @param toDate the toDate to set
     */
    public void setToDate(Date toDate)
    {
        this.toDate = toDate;
    }
    
    /**
     * @return the number of measurements
     */
    public int getNumberOfMeasurements()
    {
        return numberOfMeasurements;
    }

    /**
     * @param num the number of measurements to set
     */
    public void setNumberOfMeasurements(int num)
    {
        this.numberOfMeasurements = num;
    }
}