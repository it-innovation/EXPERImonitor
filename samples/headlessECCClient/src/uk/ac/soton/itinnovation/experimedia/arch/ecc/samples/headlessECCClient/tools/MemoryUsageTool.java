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
//      Created Date :          11-Oct-2012
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.headlessECCClient.tools;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.ITakeMeasurement;

import java.util.Date;



/**
 * A simple (and rough) memory measurement tool, implementing ITakeMeasurement.
 * 
 * @author Simon Crowle
 */
public class MemoryUsageTool implements ITakeMeasurement
{
    private final Runtime rt = Runtime.getRuntime();
    
    private Date firstMeasurementDate;
    private Date lastMeasurementDate;
    private int  totalMeasurements;
              
    @Override
    public void takeMeasure( Report reportOUT )
    {
        // Get measurement value
        String memVal = Long.toString( rt.totalMemory() - rt.freeMemory() );
        
        // Create measurement instance
        Measurement measure = new Measurement( memVal );
        measure.setMeasurementSetUUID( reportOUT.getMeasurementSet().getUUID() );
        Date stamp = measure.getTimeStamp();
        
        // Create report
        reportOUT.getMeasurementSet().addMeasurement( measure );
        reportOUT.setNumberOfMeasurements( 1 );
        reportOUT.setFromDate( stamp );
        reportOUT.setToDate( stamp );
        
        // Update stats
        totalMeasurements++;
        
        if ( firstMeasurementDate == null )
            firstMeasurementDate = stamp;
        
        lastMeasurementDate = stamp;
    }
    
    @Override
    public int getMeasurementCount()
    {
        return totalMeasurements;
    }
    
    @Override
    public Date getFirstMeasurementDate()
    { 
        return firstMeasurementDate;
    }
    
    @Override
    public Date getLastMeasurementDate()
    {
        return lastMeasurementDate;
    }
}
