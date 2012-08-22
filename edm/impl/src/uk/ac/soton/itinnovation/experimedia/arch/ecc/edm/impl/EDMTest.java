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
import java.util.Set;
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
import org.apache.log4j.Logger;
import org.jscience.physics.amount.Amount;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.dao.ExperimentDataManagerDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IExperimentDAO;

/**
 *
 * @author Vegard Engen
 */
public class EDMTest
{
    static Logger log = Logger.getLogger(EDMTest.class);
    
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
        
        
        
        ExperimentDataManager edm = new ExperimentDataManager();
        IExperimentDAO expDAO = null;
        try {
            expDAO = edm.getExperimentDAO();
        } catch (Exception ex) {
            log.error ("Unable to get Experiment DAO: " + ex.getMessage(), ex);
            System.exit(1);
        }
        
        Experiment exp = new Experiment();
        exp.setUUID(UUID.fromString("bfe4c710-61ba-46f8-a519-be2f7808192e"));
        exp.setName("Strawberry Experiment Extravagansa");
        exp.setDescription("A very boring description...");
        exp.setStartTime(new Date(Long.parseLong("1345642421005")));
        exp.setEndTime(new Date());
        exp.setExperimentID("3543");
        try {
            expDAO.saveExperiment(exp);
        } catch (Exception ex) {
            log.error("Unable to save experiment: " + ex.getMessage());
        }
        
        log.info("Getting experiment object");
        Experiment exp2 = null;
        try {
            //exp2 = expDAO.getExperiment(UUID.fromString("bfe4c710-61ba-46f8-a519-be2f7808192e"));
            //exp2 = expDAO.getExperiment(UUID.fromString("5718cd67-4310-4b2c-aeb9-9b72314630ca"));
            exp2 = expDAO.getExperiment(UUID.fromString("3fe0769d-ffae-4173-9c24-07ff7819b5cb"));
        } catch (Exception ex) {
            log.error("Unable to get experiment: " + ex.getMessage());
        }
        
        log.info("Experiment details:");
        if (exp2.getUUID() != null) log.info("  - UUID:  " + exp2.getUUID());
        if (exp2.getName() != null) log.info("  - Name:  " + exp2.getName());
        if (exp2.getDescription() != null) log.info("  - Desc:  " + exp2.getDescription());
        if (exp2.getStartTime() != null) log.info("  - Start: " + exp2.getStartTime() + " (" + exp2.getStartTime().getTime() + ")");
        if (exp2.getEndTime() != null) log.info("  - End:   " + exp2.getEndTime() + " (" + exp2.getEndTime().getTime() + ")");
        if (exp2.getExperimentID() != null) log.info("  - ID:    " + exp2.getExperimentID());
        
        if ((exp2.getMetricGenerators() == null) || exp2.getMetricGenerators().isEmpty())
            log.info("  - There are NO metric generators");
        else
            log.info("  - There are " + exp.getMetricGenerators().size() + " metric generators");
        
        log.info("Getting all experiments");
        Set<Experiment> experiments = null;
        
        try {
            experiments = expDAO.getExperiments();
            
            if (experiments != null)
            {
                log.info("Got " + experiments.size() + " experiments:");
                for (Experiment exp3 : experiments)
                {
                    log.info(" * Experiment details:");
                    if (exp3.getUUID() != null) log.info("  - UUID:  " + exp3.getUUID());
                    if (exp3.getName() != null) log.info("  - Name:  " + exp3.getName());
                    if (exp3.getDescription() != null) log.info("  - Desc:  " + exp3.getDescription());
                    if (exp3.getStartTime() != null) log.info("  - Start: " + exp3.getStartTime() + " (" + exp3.getStartTime().getTime() + ")");
                    if (exp3.getEndTime() != null) log.info("  - End:   " + exp3.getEndTime() + " (" + exp3.getEndTime().getTime() + ")");
                    if (exp3.getExperimentID() != null) log.info("  - ID:    " + exp3.getExperimentID());
                }
            }
        } catch (Exception ex) {
            log.error("");
        }
    }
}
