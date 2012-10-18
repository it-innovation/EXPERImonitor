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
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMeasurementDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMeasurementSetDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMetricDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMetricGroupDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IReportDAO;

/**
 * Interface for the Experiment Data Manager for Monitoring data.
 * 
 * @author Vegard Engen
 */
public interface IMonitoringEDM
{
    /**
     * Get the experiment DAO, allowing saving and getting Experiment objects.
     * @return Experiment DAO interface.
     * @throws Exception 
     */
    public IExperimentDAO getExperimentDAO() throws Exception;
    
    /**
     * Get the entity DAO, allowing saving and getting Entity objects.
     * @return Entity DAO interface.
     * @throws Exception 
     */
    public IEntityDAO getEntityDAO() throws Exception;
    
    /**
     * Get the metric generator DAO, allowing saving and getting MetricGenerator objects.
     * @return MetricGenerator DAO interface.
     * @throws Exception 
     */
    public IMetricGeneratorDAO getMetricGeneratorDAO() throws Exception;
    
    /**
     * Get the metric group DAO, allowing saving and getting MetricGroup objects.
     * @return MetricGroup DAO interface.
     * @throws Exception 
     */
    public IMetricGroupDAO getMetricGroupDAO() throws Exception;
    
    /**
     * Get the measurement set DAO, allowing saving and getting MeasurementSet objects.
     * @return MeasurementSet DAO interface.
     * @throws Exception 
     */
    public IMeasurementSetDAO getMeasurementSetDAO() throws Exception;
    
    /**
     * Get the metric DAO, allowing saving and getting Metric objects.
     * @return Metric DAO interface.
     * @throws Exception 
     */
    public IMetricDAO getMetricDAO() throws Exception;
    
    /**
     * Get the report DAO, allowing saving and getting Report objects.
     * @return Report DAO interface.
     * @throws Exception 
     */
    public IReportDAO getReportDAO() throws Exception;
    
    /**
     * Get the measurement DAO, allowing saving and getting Measurement objects.
     * @return Measurement DAO interface.
     * @throws Exception 
     */
    public IMeasurementDAO getMeasurementDAO() throws Exception;
    
    /**
     * Clear the metrics database.
     * @throws Exception 
     */
    public void clearMetricsDatabase() throws Exception;
    
    
}
