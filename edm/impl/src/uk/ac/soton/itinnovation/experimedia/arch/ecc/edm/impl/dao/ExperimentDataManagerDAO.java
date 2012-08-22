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
//      Created Date :          2012-08-21
//      Created for Project :   BonFIRE
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.dao;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.EDMUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DBUtil;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DatabaseConnector;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.db.DatabaseType;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMeasurementDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMeasurementSetDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMetricDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IMetricGroupDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.dao.IReportDAO;

/**
 * A DAO class for the Experiment Data Manager.
 * 
 * @author Vegard Engen
 */
public class ExperimentDataManagerDAO implements IExperimentDAO, IEntityDAO, IMetricGeneratorDAO, IMetricGroupDAO, IMeasurementSetDAO, IMetricDAO, IMeasurementDAO, IReportDAO
{
    private Map<String,String> configs;
    private DatabaseConnector dbCon;
    
    static Logger log = Logger.getLogger(ExperimentDataManagerDAO.class);
    
    public ExperimentDataManagerDAO() throws Exception
    {
        try {
            configs = EDMUtil.getConfigs();
        } catch (Exception ex){
            log.error("Caught an exception when reading the config file: " + ex.getMessage());
            throw new RuntimeException("Caught an exception when reading the config file: " + ex.getMessage(), ex);
        }
        
        try {
            dbCon = new DatabaseConnector(configs.get("dbURL"), configs.get("dbName"), configs.get("dbUsername"),configs.get("dbPassword"), DatabaseType.fromValue(configs.get("dbType")));
        } catch (Exception ex) {
            log.error("Failed to create DatabaseConnector: " + ex.getMessage(), ex);
            throw new RuntimeException("Failed to create DatabaseConnector: " + ex.getMessage(), ex);
        }
    }
    
    
    //-------------------------- EXPERIMENT ----------------------------------//
    
    
    @Override
    public void saveExperiment(Experiment exp) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        ValidationReturnObject returnObj = ExperimentHelper.isObjectValidForSave(exp, dbCon);
        if (!returnObj.valid)
        {
            log.error("Cannot save the Experiment object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        try {
            dbCon.connect();
            
            // get the table names and values according to what's available in the
            // object
            List<String> valueNames = new ArrayList<String>();
            List<String> values = new ArrayList<String>();
            ExperimentHelper.getTableNamesAndValues(exp, valueNames, values);
            
            String query = DBUtil.getInsertIntoQuery("Experiment", valueNames, values);
            ResultSet rs = dbCon.executeQuery(query, Statement.RETURN_GENERATED_KEYS);

            // check if the result set got the generated table key
            if (rs.next()) {
                String key = rs.getString(1);
                log.debug("Saved experiment " + exp.getName() + ", with key: " + key);
            } else {
                throw new RuntimeException("No index returned after saving experiment " + exp.getName());
            }//end of debugging
        } catch (Exception ex) {
            log.error("Error while saving experiment: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving experiment: " + ex.getMessage(), ex);
        } finally {
            dbCon.close();
        }
        
        // save any metric generators if not NULL
        if ((exp.getMetricGenerators() != null) && !exp.getMetricGenerators().isEmpty())
        {
            for (MetricGenerator mg : exp.getMetricGenerators())
            {
                if (mg != null)
                    this.saveMetricGenerator(mg, exp.getUUID());
            }
        }
    }

    @Override
    public Experiment getExperiment(UUID expUUID) throws Exception
    {
        if (expUUID == null)
        {
            log.error("Cannot get an Experiment object with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get an Experiment object with the given UUID because it is NULL!");
        }
        
        if (!ExperimentHelper.objectExists(expUUID, dbCon))
        {
            log.error("There is no experiment with the given UUID: " + expUUID.toString());
            throw new RuntimeException("There is no experiment with the given UUID: " + expUUID.toString());
        }
        
        Experiment exp = null;
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT * FROM Experiment WHERE expUUID = '" + expUUID + "'";
            ResultSet rs = dbCon.executeQuery(query);

            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                //UUID expUUID = UUID.fromString(rs.getString("expUUID"));
                String name = rs.getString("name");
				String description = rs.getString("description");
				String startTimeStr = rs.getString("startTime");
                String endTimeStr = rs.getString("endTime");
                String expID = rs.getString("expID");
                
                exp = new Experiment(expUUID, expID, name, description, new Date(Long.parseLong(startTimeStr)), new Date(Long.parseLong(endTimeStr)));
            }
            else // nothing in the result set
            {
                log.error("There is no experiment with the given UUID: " + expUUID.toString());
                throw new RuntimeException("There is no experiment with the given UUID: " + expUUID.toString());
            }

        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            dbCon.close();
        }
        
        // TODO: check if there's any metric generators
        
        return exp;
    }

    @Override
    public Set<Experiment> getExperiments() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    //--------------------------- ENTITY -------------------------------------//
    
    
    @Override
    public void saveEntity(Entity ent) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Entity getEntity(UUID entityUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Entity> getEntities() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Entity> getEntitiesForExperiment(UUID expUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    
    //------------------------- ATTRIBUTE ------------------------------------//
    
    
    @Override
    public void saveAttribute(Attribute attrib) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Attribute getAttribute(UUID attribUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<Attribute> getAttributesForEntity(UUID entityUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    //---------------------- METRIC GENERATOR --------------------------------//
    
    
    @Override
    public void saveMetricGenerator(MetricGenerator metricGen, UUID experimentUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MetricGenerator getMetricGenerator(UUID metricGenUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<MetricGenerator> getMetricGenerators() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<MetricGenerator> getMetricGeneratorsForExperiment(UUID expUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    //------------------------- METRIC GROUP ---------------------------------//
    
    
    @Override
    public void saveMetricGroup(MetricGroup metricGroup) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MetricGroup getMetricGroup(UUID metricGroupUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<MetricGroup> getMetricGroupsForMetricGenerator(UUID metricGenUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    //------------------------ MEASUREMENT SET -------------------------------//
    
    
    @Override
    public void saveMeasurementSet(MeasurementSet measurementSet) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MeasurementSet getMeasurementSet(UUID measurementSetUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<MeasurementSet> getMeasurementSetForMetricGroup(UUID metricGroupUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    //---------------------------- METRIC ------------------------------------//
    
    
    @Override
    public void saveMetric(Metric metric) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Metric getMetric(UUID metricUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Metric getMetricForMeasurementSet(UUID measurementSetUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    //------------------------- MEASUREMENT ----------------------------------//
    
    
    @Override
    public void saveMeasurement(Measurement measurement) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Measurement getMeasurement(UUID measurementUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    //---------------------------- REPORT ------------------------------------//
    
    
    @Override
    public void saveReport(Report report) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Report getReport(UUID reportUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Report getReportWithData(UUID reportUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Report getReportForLatestMeasurement(UUID measurementSetUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Report getReportForLatestMeasurementWithData(UUID measurementSetUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Report getReportForAllMeasurements(UUID measurementSetUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Report getReportForAllMeasurementsWithData(UUID measurementSetUUID) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Report getReportForMeasurementsAfterDate(UUID measurementSetUUID, Date fromDate) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Report getReportForMeasurementsAfterDateWithData(UUID measurementSetUUID, Date fromDate) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Report getReportForMeasurementsForTimePeriod(UUID measurementSetUUID, Date fromDate, Date toDate) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Report getReportForMeasurementsForTimePeriodWithData(UUID measurementSetUUID, Date fromDate, Date toDate) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
