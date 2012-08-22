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
//      Created for Project :   
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IReportDAO;

/**
 * Interface to the Experiment Data Manager :: the Light version, intended to live
 * on the clients that generate metrics.
 * 
 * @author Vegard Engen
 */
public interface IExperimentDataManagerLight
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
     * Get the report DAO, allowing saving and getting Report objects.
     * @return Report DAO interface.
     * @throws Exception 
     */
    public IReportDAO getReportDAO() throws Exception;
}
