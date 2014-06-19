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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.metrics;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.metrics.dao.ExperimentDataManagerDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.IMonitoringEDMAgent;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IReportDAO;

/**
 * The Light version of the Experiment Monitoring Data Manager, which exposes
 * only the essential data access objects needed by clients to store and retrieve
 * monitoring data.
 * 
 * This implementation relies on a PostgreSQL database to persist the data.
 * 
 * @author Vegard Engen
 */
public class MonitoringEDMAgent implements IMonitoringEDMAgent
{
    private ExperimentDataManagerDAO edmDAO;
    private Properties configParams;
    
	private static final Logger log = LoggerFactory.getLogger(MonitoringEDMAgent.class);
    
    /**
     * Monitoring Experiment Data Manager Agent constructor, which reads the
     * configuration file on the class path and sets up a EDM DAO object accordingly.
     * storing and retrieving data.
     */
    public MonitoringEDMAgent()
    {
        log.info("EDM Agent starting up");
        try {
            try {
                log.debug("Reading configuration file on the class path");
                configParams = MonitoringEDMUtil.getConfigs();
            } catch (Exception ex){
                log.error("Caught an exception when reading the config file: " + ex.getMessage());
                throw ex;
            }

            if (!MonitoringEDMUtil.isConfigValid(configParams))
            {
                log.error("The configuration parameters are not valid.");
                throw new RuntimeException("The configuration parameters are not valid.");
            }
            
            edmDAO = new ExperimentDataManagerDAO(configParams);
        } catch (Exception ex) {
            log.error("Failed to create ExperimentDataManagerDAO object: " + ex.getMessage(), ex);
            throw new RuntimeException("Failed to create ExperimentDataManagerDAO object: " + ex.getMessage(), ex);
        }
        log.info("EDM Agent successfully started");
    }
    
    /**
     * Monitoring Experiment Data Manager Agent constructor, which sets up the EDM DAO
     * according to the config parameters provided.
     * @param config Configuration parameters.
     */
    public MonitoringEDMAgent(Properties config) throws Exception
    {
        log.info("EDM Agent starting up");
        configParams = config;
        try {
            if (!MonitoringEDMUtil.isConfigValid(configParams)) {
                throw new RuntimeException("The provided configuration parameters are not valid.");
            }
            
            edmDAO = new ExperimentDataManagerDAO(config);
        } catch (Exception ex) {
            log.error("Failed to create ExperimentDataManagerDAO object: " + ex.getMessage(), ex);
            throw new RuntimeException("Failed to create ExperimentDataManagerDAO object: " + ex.getMessage(), ex);
        }
        log.info("EDM Agent successfully started");
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

    @Override
    public boolean isDatabaseSetUpAndAccessible()
    {
        if (edmDAO == null)
            throw new RuntimeException("Cannot check the metrics database because the EDM DAO is NULL");
        
        return edmDAO.isDatabaseSetUpAndAccessible();
    }

    @Override
    public void clearMetricsDatabase() throws Exception
    {
        if (edmDAO == null)
            throw new RuntimeException("Cannot clear the metrics database because the EDM DAO is NULL");
        
        edmDAO.clearMetricsDatabase();
    }

}
