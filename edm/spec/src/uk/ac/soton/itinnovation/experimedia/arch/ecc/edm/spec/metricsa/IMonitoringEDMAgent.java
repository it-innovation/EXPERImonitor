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
//      Created Date :          2012-08-22
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IReportDAO;

/**
 * Interface to the Experiment Data Manager for Monitoring data.
 * This is the Light version, intended to be used on the clients that generate metrics.
 * 
 * @author Vegard Engen
 */
public interface IMonitoringEDMAgent
{
    /**
     * Get the experiment DAO, allowing saving and getting Experiment objects.
     * @return Experiment DAO interface.
     * @throws Exception 
     */
    IExperimentDAO getExperimentDAO() throws Exception;
    
    /**
     * Get the entity DAO, allowing saving and getting Entity objects.
     * @return Entity DAO interface.
     * @throws Exception 
     */
    IEntityDAO getEntityDAO() throws Exception;
    
    /**
     * Get the metric generator DAO, allowing saving and getting MetricGenerator objects.
     * @return MetricGenerator DAO interface.
     * @throws Exception 
     */
    IMetricGeneratorDAO getMetricGeneratorDAO() throws Exception;
    
    /**
     * Get the report DAO, allowing saving and getting Report objects.
     * @return Report DAO interface.
     * @throws Exception 
     */
    IReportDAO getReportDAO() throws Exception;
    
    /**
     * Check if the database has been set up correctly and is accessible.
     * Will try to connect to the database and check that the schema is in place.
     * @return True if set up and accessible; false otherwise.
     */
    boolean isDatabaseSetUpAndAccessible();
    
    /**
     * Clear the data in the metrics database.
     * @throws Exception 
     */
    void clearMetricsDatabase() throws Exception;
}
