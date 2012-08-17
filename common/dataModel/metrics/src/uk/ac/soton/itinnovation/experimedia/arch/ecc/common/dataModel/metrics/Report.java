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

import java.util.UUID;

/**
 * A report class, which contains derived information about a measurement set.
 * 
 * @author Vegard Engen
 */
public class Report
{
    private UUID uuid;
    private UUID measurementSetUUID;
    private int numberOfMeasurements;
    
    /**
     * Default constructor which generates a random UUID for the Report object.
     */
    public Report()
    {
        this.uuid = UUID.randomUUID();
    }
    
    /**
     * Copy constructor for the Report class.
     * @param report The report object a copy is made of.
     */
    public Report(Report report)
    {
        if (report == null)
            return;
        
        this.uuid = report.getUUID();
        this.measurementSetUUID = report.getMeasurementSetUUID();
        this.numberOfMeasurements = report.getNumberOfMeasurements();
    }
    
    /**
     * Constructor to set all the attributes of the class.
     * @param uuid The UUID of the Report, to uniquely identify it.
     * @param measurementSetUUID The UUID of the measurement set that this is a report for.
     * @param numMeasurements The number of measurements in the measurement set this is a report for.
     */
    public Report(UUID uuid, UUID measurementSetUUID, int numMeasurements)
    {
        this.uuid = uuid;
        this.measurementSetUUID = measurementSetUUID;
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
     * @return the measurementSetUUID
     */
    public UUID getMeasurementSetUUID()
    {
        return measurementSetUUID;
    }

    /**
     * @param measurementSetUUID the measurementSetUUID to set
     */
    public void setMeasurementSetUUID(UUID measurementSetUUID)
    {
        this.measurementSetUUID = measurementSetUUID;
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
