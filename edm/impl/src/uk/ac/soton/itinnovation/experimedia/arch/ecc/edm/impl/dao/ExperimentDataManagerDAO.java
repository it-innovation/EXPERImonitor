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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import javax.measure.unit.Unit;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricType;
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
            if (dbCon.isClosed())
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
        return getExperiment(expUUID, true);
    }
    
    /**
     * Overloaded method, with the option to set a flag whether to close the
     * DB connection or not.
     * @param expUUID
     * @param closeDBcon
     * @return
     * @throws Exception 
     */
    private Experiment getExperiment(UUID expUUID, boolean closeDBcon) throws Exception
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
        boolean exception = false;
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT * FROM Experiment WHERE expUUID = '" + expUUID + "'";
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String name = rs.getString("name");
				String description = rs.getString("description");
				String startTimeStr = rs.getString("startTime");
                String endTimeStr = rs.getString("endTime");
                String expID = rs.getString("expID");
                
                Date startTime = null;
                Date endTime = null;
                
                if (startTimeStr != null)
                    startTime = new Date(Long.parseLong(startTimeStr));
                
                if (endTimeStr != null)
                    endTime = new Date(Long.parseLong(endTimeStr));
                
                exp = new Experiment(expUUID, expID, name, description, startTime, endTime);
            }
            else // nothing in the result set
            {
                log.error("There is no experiment with the given UUID: " + expUUID.toString());
                throw new RuntimeException("There is no experiment with the given UUID: " + expUUID.toString());
            }
        } catch (Exception ex) {
            exception = true;
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (exception)
                dbCon.close();
        }
        
        // check if there's any metric generators
        Set<MetricGenerator> metricGenerators = null;
        
        try {
            metricGenerators = getMetricGeneratorsForExperiment(expUUID, false);
        } catch (Exception ex) {
            log.error("Caught an exception when getting metric generators for experiment (UUID: " + expUUID.toString() + "): " + ex.getMessage());
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
        
        exp.setMetricGenerators(metricGenerators);
        
        return exp;
    }
    
    @Override
    public Set<Experiment> getExperiments() throws Exception
    {
        Set<Experiment> experiments = new HashSet<Experiment>();
        
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT * FROM Experiment";
            ResultSet rs = dbCon.executeQuery(query);
            
            // iterate over all returned records
            while (rs.next())
            {
                String uuidStr = rs.getString("expUUID");
                String name = rs.getString("name");
				String description = rs.getString("description");
				String startTimeStr = rs.getString("startTime");
                String endTimeStr = rs.getString("endTime");
                String expID = rs.getString("expID");
                
                if (uuidStr == null)
                {
                    log.error("Skipping an experiment, which does not have a UUID in the DB");
                    continue;
                }
                
                Date startTime = null;
                Date endTime = null;
                
                if (startTimeStr != null)
                    startTime = new Date(Long.parseLong(startTimeStr));
                
                if (endTimeStr != null)
                    endTime = new Date(Long.parseLong(endTimeStr));
                
                experiments.add(new Experiment(UUID.fromString(uuidStr), expID, name, description, startTime, endTime));
            }
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            dbCon.close();
        }
        
        return experiments;
    }
    
    
    //--------------------------- ENTITY -------------------------------------//
    
    
    @Override
    public void saveEntity(Entity entity) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        // will validate the attributes too, if any are given
        ValidationReturnObject returnObj = EntityHelper.isObjectValidForSave(entity, dbCon);
        if (!returnObj.valid)
        {
            log.error("Cannot save the Entity object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            // get the table names and values according to what's available in the
            // object
            List<String> valueNames = new ArrayList<String>();
            List<String> values = new ArrayList<String>();
            EntityHelper.getTableNamesAndValues(entity, valueNames, values);
            
            String query = DBUtil.getInsertIntoQuery("Entity", valueNames, values);
            ResultSet rs = dbCon.executeQuery(query, Statement.RETURN_GENERATED_KEYS);
            
            // check if the result set got the generated table key
            if (rs.next()) {
                String key = rs.getString(1);
                log.debug("Saved entity " + entity.getName() + ", with key: " + key);
            } else {
                throw new RuntimeException("No index returned after saving entity " + entity.getName());
            }//end of debugging
        } catch (Exception ex) {
            log.error("Error while saving entity: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving entity: " + ex.getMessage(), ex);
        } finally {
            if ((entity.getAttributes() == null) || entity.getAttributes().isEmpty())
                dbCon.close();
        }
        
        try {
            // save any attributes if not NULL
            if ((entity.getAttributes() != null) && !entity.getAttributes().isEmpty())
            {
                for (Attribute attrib : entity.getAttributes())
                {
                    if (attrib != null)
                        this.saveAttribute(attrib, false); // flag not to close the DB connection
                }
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            dbCon.close();
        }
    }

    @Override
    public Entity getEntity(UUID entityUUID) throws Exception
    {
        return getEntity(entityUUID, true);
    }
    
    private Entity getEntity(UUID entityUUID, boolean closeDBcon) throws Exception
    {
        if (entityUUID == null)
        {
            log.error("Cannot get an Entity object with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get an Entity object with the given UUID because it is NULL!");
        }
        
        if (!EntityHelper.objectExists(entityUUID, dbCon))
        {
            log.error("There is no entity with the given UUID: " + entityUUID.toString());
            throw new RuntimeException("There is no entity with the given UUID: " + entityUUID.toString());
        }
        
        Entity entity = null;
        boolean exception = false;
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT * FROM Entity WHERE entityUUID = '" + entityUUID + "'";
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String name = rs.getString("name");
				String description = rs.getString("description");
                
                entity = new Entity(entityUUID, name, description);
            }
            else // nothing in the result set
            {
                log.error("There is no entity with the given UUID: " + entityUUID.toString());
                throw new RuntimeException("There is no entity with the given UUID: " + entityUUID.toString());
            }
        } catch (Exception ex) {
            exception = true;
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (exception)
                dbCon.close();
        }
        
        // check if there's any metric generators
        Set<Attribute> attributes = null;
        
        try {
            attributes = this.getAttributesForEntity(entityUUID, false); // don't close the connection
        } catch (Exception ex) {
            log.error("Caught an exception when getting attributes for entity (UUID: " + entityUUID.toString() + "): " + ex.getMessage());
            throw new RuntimeException("Unable to get Entity object due to an issue with getting its attributes: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
        
        entity.setAttributes(attributes);
        
        return entity;
    }

    @Override
    public Set<Entity> getEntities() throws Exception
    {
        Set<Entity> entities = new HashSet<Entity>();
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT entityUUID FROM Entity";
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            while (rs.next())
            {
                String uuidStr = rs.getString("entityUUID");
                
                if (uuidStr == null)
                {
                    log.error("Skipping an entity, which does not have a UUID in the DB");
                    continue;
                }
                
                entities.add(getEntity(UUID.fromString(uuidStr), false));
            }

        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            dbCon.close();
        }
        
        return entities;
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
        saveAttribute(attrib, true);
    }
    
    private void saveAttribute(Attribute attrib, boolean closeDBcon) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        ValidationReturnObject returnObj = AttributeHelper.isObjectValidForSave(attrib, dbCon, true);
        if (!returnObj.valid)
        {
            log.error("Cannot save the Attribute object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            // get the table names and values according to what's available in the
            // object
            List<String> valueNames = new ArrayList<String>();
            List<String> values = new ArrayList<String>();
            AttributeHelper.getTableNamesAndValues(attrib, valueNames, values);
            
            String query = DBUtil.getInsertIntoQuery("Attribute", valueNames, values);
            ResultSet rs = dbCon.executeQuery(query, Statement.RETURN_GENERATED_KEYS);
            
            // check if the result set got the generated table key
            if (rs.next()) {
                String key = rs.getString(1);
                log.debug("Saved attribute " + attrib.getName() + ", with key: " + key);
            } else {
                throw new RuntimeException("No index returned after saving attribute " + attrib.getName());
            }//end of debugging
        } catch (Exception ex) {
            log.error("Error while saving entity: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving attribute: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
    }

    @Override
    public Attribute getAttribute(UUID attribUUID) throws Exception
    {
        return getAttribute(attribUUID, true);
    }
    
    private Attribute getAttribute(UUID attribUUID, boolean closeDBcon) throws Exception
    {
        if (attribUUID == null)
        {
            log.error("Cannot get an Attribute object with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get an Attribute object with the given UUID because it is NULL!");
        }
        
        if (!AttributeHelper.objectExists(attribUUID, dbCon))
        {
            log.error("There is no attribute with the given UUID: " + attribUUID.toString());
            throw new RuntimeException("There is no attribute with the given UUID: " + attribUUID.toString());
        }
        
        Attribute attribute = null;
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT * FROM Attribute WHERE attribUUID = '" + attribUUID + "'";
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String name = rs.getString("name");
				String description = rs.getString("description");
                String entityUUIDstr = rs.getString("entityUUID");
                
                if (entityUUIDstr == null)
                {
                    throw new RuntimeException("The attribute instance doesn't have an entity UUID");
                }
                
                attribute = new Attribute(attribUUID, UUID.fromString(entityUUIDstr), name, description);
            }
            else // nothing in the result set
            {
                log.error("There is no attribute with the given UUID: " + attribUUID.toString());
                throw new RuntimeException("There is no attribute with the given UUID: " + attribUUID.toString());
            }
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
        
        return attribute;
    }

    @Override
    public Set<Attribute> getAttributesForEntity(UUID entityUUID) throws Exception
    {
        return getAttributesForEntity(entityUUID, true);
    }
    
    private Set<Attribute> getAttributesForEntity(UUID entityUUID, boolean closeDBcon) throws Exception
    {
        if (entityUUID == null)
        {
            log.error("Cannot get any Attribute objects for an Entity with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get any Attribute objects for an Entity with the given UUID because it is NULL!");
        }
        
        if (!EntityHelper.objectExists(entityUUID, dbCon))
        {
            log.error("There is no entity with the given UUID: " + entityUUID.toString());
            throw new RuntimeException("There is no entity with the given UUID: " + entityUUID.toString());
        }
        
        Set<Attribute> attributes = new HashSet<Attribute>();;
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT * FROM Attribute WHERE entityUUID = '" + entityUUID + "'";
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            while (rs.next())
            {
                String attribUUIDstr = rs.getString("attribUUID");
                String entityUUIDstr = rs.getString("entityUUID");
                String name = rs.getString("name");
				String description = rs.getString("description");
                
                if (attribUUIDstr == null)
                {
                    throw new RuntimeException("The attribute instance doesn't have a UUID");
                }
                
                if (entityUUIDstr == null)
                {
                    throw new RuntimeException("The attribute instance doesn't have an entity UUID");
                }
                
                attributes.add(new Attribute(UUID.fromString(attribUUIDstr), UUID.fromString(entityUUIDstr), name, description));
            }
            
        } catch (Exception ex) {
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
        
        return attributes;
    }
    
    
    //---------------------- METRIC GENERATOR --------------------------------//
    
    
    @Override
    public void saveMetricGenerator(MetricGenerator metricGen, UUID experimentUUID) throws Exception
    {
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        // will validate the attributes too, if any are given
        ValidationReturnObject returnObj = MetricGeneratorHelper.isObjectValidForSave(metricGen, experimentUUID, dbCon);
        if (!returnObj.valid)
        {
            log.error("Cannot save the MetricGenerator object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        boolean exception = false;
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            // get the table names and values according to what's available in the
            // object
            List<String> valueNames = new ArrayList<String>();
            List<String> values = new ArrayList<String>();
            MetricGeneratorHelper.getTableNamesAndValues(metricGen, experimentUUID, valueNames, values);
            
            String query = DBUtil.getInsertIntoQuery("MetricGenerator", valueNames, values);
            ResultSet rs = dbCon.executeQuery(query, Statement.RETURN_GENERATED_KEYS);
            
            // check if the result set got the generated table key
            if (rs.next()) {
                String key = rs.getString(1);
                log.debug("Saved MetricGenerator " + metricGen.getName() + " with key: " + key);
            } else {
                throw new RuntimeException("No index returned after saving MetricGenerator " + metricGen.getName());
            }
        } catch (Exception ex) {
            exception = true;
            log.error("Error while saving MetricGenerator: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving MetricGenerator: " + ex.getMessage(), ex);
        } finally {
            if (exception || (metricGen.getMetricGroups() == null) || metricGen.getMetricGroups().isEmpty())
                dbCon.close();
        }
        
        try {
            // save any metric groups if not NULL
            if ((metricGen.getMetricGroups() != null) || !metricGen.getMetricGroups().isEmpty())
            {
                for (MetricGroup mGrp : metricGen.getMetricGroups())
                {
                    if (mGrp != null)
                        this.saveMetricGroup(mGrp, false); // flag not to close the DB connection
                }
            }
        } catch (Exception ex) {
            exception = true;
            throw ex;
        } finally {
            if (exception)
                dbCon.close();
        }
        
        try {
            // make link between MG and Entity in MetricGenerator_Entity table
            for (UUID entityUUID : metricGen.getEntities())
            {
                linkMetricGeneratorAndEntity(metricGen.getUUID(), entityUUID, false);
            }
        } catch (Exception ex) {
            exception = true;
            throw ex;
        } finally {
            dbCon.close();
        }
    }
    
    private void linkMetricGeneratorAndEntity(UUID mGenUUID, UUID entityUUID, boolean closeDBcon) throws Exception
    {
        if (mGenUUID == null)
        {
            log.error("Cannot link metric generator and entity because the metric generator UUID is NULL!");
            throw new NullPointerException("Cannot link metric generator and entity because the metric generator UUID is NULL!");
        }
        
        if (entityUUID == null)
        {
            log.error("Cannot link metric generator and entity because the entity UUID is NULL!");
            throw new NullPointerException("Cannot link metric generator and entity because the entity UUID is NULL!");
        }
        
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "INSERT INTO MetricGenerator_Entity (mGenUUID, entityUUID) VALUES ("
                    + "'" + mGenUUID.toString() + "', '" + entityUUID.toString() + "')";
            ResultSet rs = dbCon.executeQuery(query, Statement.RETURN_GENERATED_KEYS);
            
            // check if the result set got the generated table key
            if (rs.next()) {
                String key = rs.getString(1);
                log.debug("Saved MetricGenerator - Entity link");
            } else {
                throw new RuntimeException("No index returned after saving MetricGenerator - Entity link");
            }
        } catch (Exception ex) {
            log.error("Error while saving MetricGenerator - Entity link: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving MetricGenerator - Entity link: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
    }

    @Override
    public MetricGenerator getMetricGenerator(UUID metricGenUUID) throws Exception
    {
        return getMetricGenerator(metricGenUUID, true);
    }
    
    private MetricGenerator getMetricGenerator(UUID metricGenUUID, boolean closeDBcon) throws Exception
    {
        if (metricGenUUID == null)
        {
            log.error("Cannot get a MetricGenerator object with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get a MetricGenerator object with the given UUID because it is NULL!");
        }
        
        if (!MetricGeneratorHelper.objectExists(metricGenUUID, dbCon))
        {
            log.error("There is no MetricGenerator with the given UUID: " + metricGenUUID.toString());
            throw new RuntimeException("There is no MetricGenerator with the given UUID: " + metricGenUUID.toString());
        }
        
        MetricGenerator metricGenerator = null;
        boolean exception = false;
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT * FROM MetricGenerator WHERE mGenUUID = '" + metricGenUUID + "'";
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String name = rs.getString("name");
				String description = rs.getString("description");
                
                metricGenerator = new MetricGenerator(metricGenUUID, name, description);
            }
            else // nothing in the result set
            {
                log.error("There is no MetricGenerator with the given UUID: " + metricGenUUID.toString());
                throw new RuntimeException("There is no MetricGenerator with the given UUID: " + metricGenUUID.toString());
            }
        } catch (Exception ex) {
            exception = true;
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            if (exception)
                dbCon.close();
        }
        
        // check if there's any metric groups
        Set<MetricGroup> metricGroups = null;
        
        try {
            metricGroups = this.getMetricGroupsForMetricGenerator(metricGenUUID, false); // don't close the connection
        } catch (Exception ex) {
            log.error("Caught an exception when getting metric groups for MetricGenerator (UUID: " + metricGenUUID.toString() + "): " + ex.getMessage());
            throw new RuntimeException("Unable to get MetricGenerator object due to an issue with getting its metric groups: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
        
        metricGenerator.setMetricGroups(metricGroups);
        
        // get any entityUUID links
        Set<UUID> entities = new HashSet<UUID>();
        
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            String query = "SELECT entityUUID FROM MetricGenerator_Entity WHERE mGenUUID = '" + metricGenUUID + "'";
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String entityUUIDstr = rs.getString("entityUUID");
                if (entityUUIDstr == null)
                {
                    log.error("Unable to get Entity UUID from the DB for the MetricGenerator");
                    throw new RuntimeException("Unable to get Entity UUID from the DB for the MetricGenerator");
                }
                
                entities.add(metricGenUUID);
            }
            else // nothing in the result set
            {
                log.error("There is no Entitis found that are linked to the MetricGenerator with the given UUID: " + metricGenUUID.toString());
                throw new RuntimeException("There is no Entitis found that are linked to the MetricGenerator with the given UUID: " + metricGenUUID.toString());
            }
        } catch (Exception ex) {
            exception = true;
            log.error("Error while quering the database: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while quering the database: " + ex.getMessage(), ex);
        } finally {
            dbCon.close();
        }
        
        metricGenerator.setEntities(entities);
        
        return metricGenerator;
    }

    @Override
    public Set<MetricGenerator> getMetricGenerators() throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<MetricGenerator> getMetricGeneratorsForExperiment(UUID expUUID) throws Exception
    {
        return getMetricGeneratorsForExperiment(expUUID, true);
    }
    
    /**
     * Overloaded method, with the option to set a flag whether to close the
     * DB connection or not.
     * @param expUUID
     * @param closeDBcon
     * @return
     * @throws Exception 
     */
    private Set<MetricGenerator> getMetricGeneratorsForExperiment(UUID expUUID, boolean closeDBcon) throws Exception
    {
        Set<MetricGenerator> generators = new HashSet<MetricGenerator>();
        
        
        return generators;
    }
    
    
    //------------------------- METRIC GROUP ---------------------------------//
    
    
    @Override
    public void saveMetricGroup(MetricGroup metricGroup) throws Exception
    {
        saveMetricGroup(metricGroup, true);
    }
    
    public void saveMetricGroup(MetricGroup metricGroup, boolean closeDBcon) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MetricGroup getMetricGroup(UUID metricGroupUUID) throws Exception
    {
        return getMetricGroup(metricGroupUUID, true);
    }
    
    private MetricGroup getMetricGroup(UUID metricGroupUUID, boolean closeDBcon) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<MetricGroup> getMetricGroupsForMetricGenerator(UUID metricGenUUID) throws Exception
    {
        return getMetricGroupsForMetricGenerator(metricGenUUID, true);
    }
    
    private Set<MetricGroup> getMetricGroupsForMetricGenerator(UUID metricGenUUID, boolean closeDBcon) throws Exception
    {
        Set<MetricGroup> metricGroups = new HashSet<MetricGroup>();
        
        return metricGroups;
    }
    
    
    //------------------------ MEASUREMENT SET -------------------------------//
    
    
    @Override
    public void saveMeasurementSet(MeasurementSet measurementSet) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
        
        // validate object
        //   what to do about metric validation??
        
        // save metric
        //    what to do if metric exists already?
        
        // save measurement set
    }

    @Override
    public MeasurementSet getMeasurementSet(UUID measurementSetUUID) throws Exception
    {
        return getMeasurementSet(measurementSetUUID, true);
    }
    
    private MeasurementSet getMeasurementSet(UUID measurementSetUUID, boolean closeDBcon) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<MeasurementSet> getMeasurementSetForMetricGroup(UUID metricGroupUUID) throws Exception
    {
        return getMeasurementSetForMetricGroup(metricGroupUUID, true);
    }
    
    private Set<MeasurementSet> getMeasurementSetForMetricGroup(UUID metricGroupUUID, boolean closeDBcon) throws Exception
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    
    //---------------------------- METRIC ------------------------------------//
    
    
    @Override
    public void saveMetric(Metric metric) throws Exception
    {
        saveMetric(metric, true);
    }
    
    public void saveMetric(Metric metric, boolean closeDBcon) throws Exception
    {
        log.info("Saving metric");
        
        // this validation will check if all the required parameters are set and if
        // there isn't already a duplicate instance in the DB
        ValidationReturnObject returnObj = MetricHelper.isObjectValidForSave(metric, dbCon);
        if (!returnObj.valid)
        {
            log.error("Cannot save the Metric object: " + returnObj.exception.getMessage(), returnObj.exception);
            throw returnObj.exception;
        }
        
        try {
            if (dbCon.isClosed())
                dbCon.connect();
            
            // serialising unit
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);

            oos.writeObject(metric.getUnit());
            oos.flush();
            oos.close();
            bos.close();

            byte[] unitBytes = bos.toByteArray();
        
            String query = "INSERT INTO Metric (metricUUID, mType, unit) VALUES (?, ?, ?)";
            PreparedStatement pstmt = dbCon.getConnection().prepareStatement(query);
            pstmt.setObject(1, metric.getUUID(), java.sql.Types.OTHER);
            pstmt.setString(2, metric.getMetricType().name());
            pstmt.setObject(3, unitBytes);
            
            int rowCount = pstmt.executeUpdate();
            
            // check if the result set got the generated table key
            if (rowCount > 0) {
                log.debug("Saved metric with uuid: " + metric.getUUID().toString());
            } else {
                throw new RuntimeException("Metric did not get saved in the database (PreparedStatement returned 0 rows)");
            }//end of debugging
        } catch (Exception ex) {
            log.error("Error while saving metric: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while saving metric: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
    }

    @Override
    public Metric getMetric(UUID metricUUID) throws Exception
    {
        return getMetric(metricUUID, true);
    }
    
    private Metric getMetric(UUID metricUUID, boolean closeDBcon) throws Exception
    {
        if (metricUUID == null)
        {
            log.error("Cannot get a Metric object with the given UUID because it is NULL!");
            throw new NullPointerException("Cannot get a Metric object with the given UUID because it is NULL!");
        }
        
        if (!ExperimentHelper.objectExists(metricUUID, dbCon))
        {
            log.error("There is no metric with the given UUID: " + metricUUID.toString());
            throw new RuntimeException("There is no metric with the given UUID: " + metricUUID.toString());
        }
        
        Metric metric = null;
        
        try {
            if (dbCon.isClosed())
                dbCon.connect();
        
            String query = "SELECT * FROM Metric WHERE metricUUID = '" + metricUUID.toString() + "'";
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                ByteArrayInputStream bais;
                ObjectInputStream ins;
                Unit unit;

                try {
                    bais = new ByteArrayInputStream(rs.getBytes("unit"));
                    ins = new ObjectInputStream(bais);
                    unit =(Unit)ins.readObject();
                    ins.close();
                }
                catch (Exception e) {
                    log.error("Unable to read the unit from the database");
                    throw new RuntimeException("Unable to read the unit from the database");
                }
                
                String metricTypeStr = rs.getString("mType");
                if (metricTypeStr == null)
                {
                    log.error("Unable to get the metric type from the database");
                    throw new RuntimeException("Unable to get the metric type from the database");
                }
                
                metric = new Metric(metricUUID, MetricType.fromValue(metricTypeStr), unit);
            }
            else // nothing in the result set
            {
                log.error("There is no metric with the given UUID: " + metricUUID.toString());
                throw new RuntimeException("There is no metric with the given UUID: " + metricUUID.toString());
            }
        } catch (Exception ex) {
            log.error("Error while getting metric: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while getting metric: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
        
        return metric;
    }

    @Override
    public Metric getMetricForMeasurementSet(UUID measurementSetUUID) throws Exception
    {
        return getMetricForMeasurementSet(measurementSetUUID, true);
    }
    
    private Metric getMetricForMeasurementSet(UUID measurementSetUUID, boolean closeDBcon) throws Exception
    {
        if (measurementSetUUID == null)
        {
            log.error("Cannot get a Metric object for the given measurement set, because its given UUID is NULL!");
            throw new NullPointerException("Cannot get a Metric object for the given measurement set, because its given UUID is NULL!");
        }
        
        if (!MeasurementSetHelper.objectExists(measurementSetUUID, dbCon))
        {
            log.error("There is no measurement set with the given UUID: " + measurementSetUUID.toString());
            throw new RuntimeException("There is no measurement set with the given UUID: " + measurementSetUUID.toString());
        }
        
        Metric metric = null;
        
        try {
            if (dbCon.isClosed())
                dbCon.connect();
        
            // TODO: validate that this is correct!
            String query = "SELECT * FROM Metric WHERE metricUUID = (SELECT metricUUID FROM MeasurementSet WHERE mSetUUID = '" + measurementSetUUID + "')";
            ResultSet rs = dbCon.executeQuery(query);
            
            // check if anything got returned (connection closed in finalise method)
            if (rs.next())
            {
                String uuidStr = rs.getString("metricUUID");
                if (uuidStr == null)
                {
                    log.error("Unable to get the metric UUID from the database");
                    throw new RuntimeException("Unable to get the metric UUID from the database");
                }
                
                String metricTypeStr = rs.getString("mType");
                if (metricTypeStr == null)
                {
                    log.error("Unable to get the metric type from the database");
                    throw new RuntimeException("Unable to get the metric type from the database");
                }
                ByteArrayInputStream bais;
                ObjectInputStream ins;
                Unit unit;

                try {
                    bais = new ByteArrayInputStream(rs.getBytes("unit"));
                    ins = new ObjectInputStream(bais);
                    unit =(Unit)ins.readObject();
                    ins.close();
                }
                catch (Exception e) {
                    log.error("Unable to read the unit from the database");
                    throw new RuntimeException("Unable to read the unit from the database");
                }
                
                metric = new Metric(UUID.fromString(uuidStr), MetricType.fromValue(metricTypeStr), unit);
            }
            else // nothing in the result set
            {
                log.error("Didn't find any metric for measurement set with the given UUID: " + measurementSetUUID.toString());
                throw new RuntimeException("Didn't find any metric for measurement set with the given UUID: " + measurementSetUUID.toString());
            }
        } catch (Exception ex) {
            log.error("Error while getting metric for measurement set: " + ex.getMessage(), ex);
            throw new RuntimeException("Error while getting metric for measurement set: " + ex.getMessage(), ex);
        } finally {
            if (closeDBcon)
                dbCon.close();
        }
        
        return metric;
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
        return getMeasurement(measurementUUID, true);
    }
    
    private Measurement getMeasurement(UUID measurementUUID, boolean closeDBcon) throws Exception
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
