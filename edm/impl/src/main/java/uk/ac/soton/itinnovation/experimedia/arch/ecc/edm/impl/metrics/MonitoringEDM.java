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
//      Created By :            Vegard Engen, Simon Crowle
//      Created Date :          2012-08-13
//      Created for Project :   BonFIRE, EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.metrics;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.client.EDMClientPersistence;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.metrics.dao.ExperimentDataManagerDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.client.dao.IClientDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMeasurementDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMeasurementSetDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMetricDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMetricGroupDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IReportDAO;

/**
 * The Experiment Monitoring Data Manager, which exposes all data access objects
 * for storing and retrieving monitoring data.
 * 
 * This implementation relies on a PostgreSQL database to persist the data.
 * 
 * @author Vegard Engen
 */
public class MonitoringEDM implements IMonitoringEDM
{
    private ExperimentDataManagerDAO edmDAO;
		private EDMClientPersistence     edmClientPersist;
    private Properties configParams;
    
	private static final Logger log = LoggerFactory.getLogger(MonitoringEDM.class);
    
    /**
     * Monitoring Experiment Data Manager constructor, which reads the
     * configuration file on the class path and sets up a EDM DAO object accordingly.
     * storing and retrieving data.
     */
    public MonitoringEDM() throws Exception
    {
        log.info("EDM starting up");
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
                throw new IllegalArgumentException("The configuration parameters are not valid.");
            }
            
						// Create metrics data manager
            edmDAO = new ExperimentDataManagerDAO(configParams);
						
						// Create client persistence
						edmClientPersist = new EDMClientPersistence(configParams);
						
        } catch (Exception ex) {
            log.error("Failed to create ExperimentDataManagerDAO object: " + ex.getMessage(), ex);
            throw new RuntimeException("Failed to create ExperimentDataManagerDAO object: " + ex.getMessage(), ex);
        }
        log.info("EDM successfully started");
    }
    
    /**
     * Monitoring Experiment Data Manager constructor, which sets up the EDM DAO
     * according to the config parameters provided.
     * @param config Configuration parameters.
     */
    public MonitoringEDM(Properties config) throws Exception
    {
        log.info("EDM starting up");
        configParams = config;
        try {
            if (!MonitoringEDMUtil.isConfigValid(configParams)) {
                throw new IllegalArgumentException("The configuration parameters provided are not valid.");
            }
            
            // Create metrics data manager
            edmDAO = new ExperimentDataManagerDAO(configParams);
						
						// Create client persistence
						edmClientPersist = new EDMClientPersistence(configParams);
						
        } catch (Exception ex) {
            log.error("Failed to create ExperimentDataManagerDAO object: " + ex.getMessage(), ex);
            throw new IllegalArgumentException("Failed to create ExperimentDataManagerDAO object: " + ex.getMessage(), ex);
        }
        log.info("EDM successfully started");
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
    public IMetricGroupDAO getMetricGroupDAO() throws Exception
    {
        return edmDAO;
    }

    @Override
    public IMeasurementSetDAO getMeasurementSetDAO() throws Exception
    {
        return edmDAO;
    }

    @Override
    public IMetricDAO getMetricDAO() throws Exception
    {
        return edmDAO;
    }
    
    @Override
    public IReportDAO getReportDAO() throws Exception
    {
        return edmDAO;
    }

    @Override
    public IMeasurementDAO getMeasurementDAO() throws Exception
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
		
		@Override
		public IClientDAO getClientDAO() throws Exception
		{
			if (edmClientPersist == null)
					throw new RuntimeException("Cannot create ClientDAO; persistence is not ready");
			
			return edmClientPersist.getClientDAO();
		}
}
