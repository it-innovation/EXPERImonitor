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
//      Created By :            Stefanie Wiegand
//      Created Date :          10-Jun-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.headlessECCClient.tools;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.ITakeMeasurement;

import java.util.*;




/**
 * A simple metric tool that simulates a random walk, modifying the direction of the
 * walker (as specified using 360 degrees) in a random (but repeatable) fashion.
 * 
 * @author Simon Crowle
 */
public class QoETool implements ITakeMeasurement
{
    private final Random random = new Random( 20121110 ); // Repeatable seed
    
    private Date firstMeasurementDate;
    private Date lastMeasurementDate;
    private int  totalMeasurements;
    
    private QoE currentEmotion;
    

    public QoETool()
    {
        //start with unknown emotion
        currentEmotion = QoE.UNKNOWN;
    }
    
    @Override
    public void takeMeasure( Report reportOUT )
    {

		// get random emotion
		currentEmotion = QoE.fromInt(random.nextInt(7));

        // Create measurement instance
        Measurement measure = new Measurement( currentEmotion.name());
        measure.setMeasurementSetUUID( reportOUT.getMeasurementSet().getID() );
        Date stamp = measure.getTimeStamp();
        
        // Create report
        reportOUT.getMeasurementSet().addMeasurement( measure );
        reportOUT.setNumberOfMeasurements( 1 );
        reportOUT.setFromDate( stamp );
        reportOUT.setToDate( stamp );
        
        // Update stats
        totalMeasurements++;
        
        if ( firstMeasurementDate == null ) {
            firstMeasurementDate = stamp;
		}
        
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
	
	public enum QoE {
		
		UNKNOWN(-1),
		DESPERATE (0),
		DEPRESSED(1),
		SAD(2),
		NEUTRAL(3),
		CONTENT(4),
		HAPPY(5),
		OVERJOYED(6);
		
		private final int value;
		private static QoE[] values = null;
		
		public static QoE fromInt(int i) {
			if(QoE.values == null) {
				QoE.values = QoE.values();
			}
			return QoE.values[i];
		}
		
		private QoE (int value) { 
			if (value<0 || value>6) {
				value = 3;
			}
			this.value = value;
		}
		
		public int getValue() { return value; }
	}
}
