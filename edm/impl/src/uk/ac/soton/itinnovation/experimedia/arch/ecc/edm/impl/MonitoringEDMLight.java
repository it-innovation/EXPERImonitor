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
//      Created Date :          2012-09-07
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl;

import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.mon.dao.ExperimentDataManagerDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDMLight;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IReportDAO;

/**
 * The Light version of the Experiment Monitoring Data Manager, which exposes
 * only the essential data access objects needed by clients to store and retrieve
 * monitoring data.
 * 
 * This implementation relies on a PostgreSQL database to persist the data.
 * 
 * @author Vegard Engen
 */
public class MonitoringEDMLight implements IMonitoringEDMLight
{
    private ExperimentDataManagerDAO edmDAO;
    
    static Logger log = Logger.getLogger(MonitoringEDMLight.class);
    
    public MonitoringEDMLight()
    {
        log.info("EDM STARTING UP :)");
        try {
            edmDAO = new ExperimentDataManagerDAO();
        } catch (Exception ex) {
            log.error("Failed to create ExperimentDataManagerDAO object: " + ex.getMessage(), ex);
        }
    }
    
    @Override
    public IExperimentDAO getExperimentDAO() throws Exception
    {
        return edmDAO;
    }

    @Override
    public IEntityDAO getEntityDAO() throws Exception
    {
        return edmDAO;
    }

    @Override
    public IMetricGeneratorDAO getMetricGeneratorDAO() throws Exception
    {
        return edmDAO;
    }

    @Override
    public IReportDAO getReportDAO() throws Exception
    {
        return edmDAO;
    }

}