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
//      Created Date :          2012-08-13
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl;

import java.util.Date;
import java.util.UUID;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Mass;
import javax.measure.unit.Unit;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;

import javax.measure.Measure;
import javax.measure.unit.*;
import javax.measure.quantity.*;
import static javax.measure.unit.SI.*;
import static javax.measure.unit.NonSI.*;
import static javax.measure.unit.Dimension.*;


import static javax.measure.unit.SystemOfUnits.*;
import static javax.measure.unit.UnitFormat.*;
import org.jscience.physics.amount.Amount;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IExperimentDAO;

/**
 *
 * @author Vegard Engen
 */
public class EDMTest
{
    public static void main(String[] args) throws Exception
    {
        Metric m = new Metric();
        m.setUnit(MINUTE);
        //Unit<Mass> unit = GRAM;
        Unit<Duration> min = MINUTE;//SECONDS.times(60);
        Unit<Duration> sec = SECOND;
        Unit<Duration> ms = MILLI(SECOND);
        
        Unit<Length> metre = METRE;
        Unit<Length> cm = CENTI(METER);
        Unit<Length> FOOT = METER.times(3048).divide(10000); // Exact.
        
        Unit<Volume> cl = CENTI(LITER);
        
        Unit<Velocity> mps = METRES_PER_SECOND;
        
        // Conversion between units.
        System.out.println(KILO(METRE).getConverterTo(MILE).convert(10));
        
        // Dimension checking (allows/disallows conversions)    
        System.out.println(ELECTRON_VOLT.isCompatible(WATT.times(HOUR)));

        // Retrieval of the unit dimension (depends upon the current model).
        System.out.println(ELECTRON_VOLT.getDimension());
        
        Amount<Length> x = Amount.valueOf(100, NonSI.INCH);
        
        
        // tweets per second
        
        
        // cpu load
        
        
        // number of VMs
        
        
        
        
    }
}
