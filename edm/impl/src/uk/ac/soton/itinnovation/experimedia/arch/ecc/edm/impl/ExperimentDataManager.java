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

import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.dao.ExperimentDataManagerDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IExperimentDataManager;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMeasurementDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMeasurementSetDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMetricDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMetricGroupDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IReportDAO;

/**
 *
 * @author Vegard Engen
 */
public class ExperimentDataManager implements IExperimentDataManager
{
    private ExperimentDataManagerDAO edmDAO;
    
    static Logger log = Logger.getLogger(ExperimentDataManager.class);
    
    public ExperimentDataManager()
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
}
