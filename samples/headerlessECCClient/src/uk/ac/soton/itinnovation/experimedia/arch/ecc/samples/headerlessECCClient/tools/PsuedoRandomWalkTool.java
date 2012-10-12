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

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.headerlessECCClient.tools;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.ITakeMeasurement;

import java.util.Random;





public class PsuedoRandomWalkTool implements ITakeMeasurement
{
    private final Random random = new Random( 20121110 ); // Repeatable seed
    
    private int directionDegrees;
    
    public PsuedoRandomWalkTool( int start )
    {
        if ( start < 0 )   start = 0;
        if ( start > 359 ) start = 0;
        
        directionDegrees = start;
    }
    
    @Override
    public Report takeMeasure( MeasurementSet ms )
    {
        // Take a random walk (var
        if ( random.nextBoolean() )
            directionDegrees++;
        else
            directionDegrees--;

        if ( directionDegrees < 0 )   directionDegrees = 359;
        if ( directionDegrees > 359 ) directionDegrees = 0;

        Measurement measure = new Measurement( Integer.toString(directionDegrees) );
        ms.addMeasurement( measure );

        Report report = new Report();
        report.setMeasurementSet( ms );
        report.setFromDate( measure.getTimeStamp() );
        report.setToDate( measure.getTimeStamp() );

        return report;
    }
}
