/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2014
//
// Copyright in this library belongs to the University of Southampton
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
//	Created By :			Maxim Bashevoy
//                          Simon Crowle
//	Created Date :			2014-04-02
//	Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.co.soton.itinnovation.ecc.service.services;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricHelper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IEntityDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IReportDAO;
import uk.co.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;

/**
 * Provides access to data in the database.
 */
@Service("dataService")
public class DataService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private IMonitoringEDM      expDataManager;
	private IExperimentDAO      experimentDAO;
	private IMetricGeneratorDAO metricGenDAO;
    private IEntityDAO          entityDAO;
    private IReportDAO          expReportDAO;

    private boolean started = false;
    

    public DataService() {
    }

    /**
     * Starts the service (should only be called by
     * {@link ConfigurationService})
     *
     * @param databaseConfiguration
     * @return
     */
    boolean start(DatabaseConfiguration databaseConfiguration) {
        started = false;
        
        if ( databaseConfiguration != null ) {
            
            // Make sure clear any previously created EDM references
            expDataManager = null;
            experimentDAO  = null;
            metricGenDAO   = null;
            entityDAO      = null;
            expReportDAO   = null;
            
            // Create properties for EDM Factory
            Properties props = new Properties();
            props.put( "dbPassword", databaseConfiguration.getUserPassword() );
            props.put( "dbName", databaseConfiguration.getDatabaseName() );
            props.put( "dbType", databaseConfiguration.getDatabaseType() );
            props.put( "dbURL", databaseConfiguration.getUrl() );
            props.put( "dbUsername", databaseConfiguration.getUserName() );
            
            // Try getting data accessors
            try {
                expDataManager = EDMInterfaceFactory.getMonitoringEDM( props );
                experimentDAO  = expDataManager.getExperimentDAO();
                metricGenDAO   = expDataManager.getMetricGeneratorDAO();
                entityDAO      = expDataManager.getEntityDAO();
                expReportDAO   = expDataManager.getReportDAO();
                
                started = true;
            }
            catch ( Exception ex ) {
                logger.error( "Could not start data service: " + ex.getMessage() );
            }
        }
        return started;
    }

    public boolean isStarted() {
        return started;
    }

    /**
     * Use this method to shutdown connections to the databases used by this
     * service.
     *
     */
    public void shutdown() {

        if (!started) {
            logger.error("Failed to shut down data service: not initialised yet");
            
            expReportDAO   = null;
            entityDAO      = null;
            metricGenDAO   = null;
            experimentDAO  = null;
            expDataManager = null;
        }
    }

    /**
     * Use this method to retrieve high-level meta-data relating to all known
     * experiments stored in the data service database. This method will return
     * instances of class Experiment but these will not contain metric models.
     *
     * @return - Returns a set of experiment instances (high-level data only)
     */
    Set<Experiment> getAllKnownExperiments() {
        
        HashSet<Experiment> result = new HashSet<Experiment>();
        
        // Safety first
        if ( started ) {
            try {
                result.addAll( experimentDAO.getExperiments( false ) );
            }
            catch ( Exception ex ) {
                logger.warn( "Could not retrieve experiments: " + ex.getMessage() );
            }
        }
        else
            logger.warn( "Cannot get experiments: service not started" );
        
        return result;
    }

    /**
     * Use this method to retrieve high-level experiment meta-data and also all
     * known MetricGenerators currently associated with the experiment.
     *
     * @param expID - UUID of the experiment required
     * @return - Experiment instance with all currently known metric generators
     * @throws Exception - throws if the service is not initialised or there was
     * a problem with the database
     */
    Experiment getExperimentWithMetricModels(UUID expID) throws Exception {
        
        // Safety first
        if ( !started ) throw new Exception( "Cannot get experiment: service not started" );
        if ( expID == null ) throw new Exception( "Could not get experiment: ID is null" );
        
        Experiment experiment = null;
        
        try {
            experiment = experimentDAO.getExperiment( expID, true );
        }
        catch ( Exception ex ) {
            String msg = "Could not retrieve experiment: " + ex.getMessage();
            logger.warn( msg );
            
            throw new Exception( msg );
        }

        return experiment;
    }

    /**
     * Use this method to quickly extract all known (metric) entities from an
     * experiment.
     *
     * @param expID - ID of the experiment you need entities from.
     * @return - Returns a set of entities for the experiment (if any exist)
     * @throws Exception - throws if the experiment ID is invalid or there is no experiment that ID
     */
    Set<Entity> getEntitiesForExperiment(UUID expID) throws Exception {
        
        // Safety
        if ( !started ) throw new Exception( "Could not get Entities for experiment: service not started" );
        if ( expID == null ) throw new Exception( "Could not get Entities for experiment: ID is null" );
        
        HashSet<Entity> entities = new HashSet<Entity>();
        
        try {
            entities.addAll( entityDAO.getEntitiesForExperiment( expID, true ) );
        }
        catch ( Exception ex ) {
            logger.warn( "Could not get entities for experiment ("+ expID.toString() + "): " + ex.getMessage() );
        }
        
        return entities;
    }

    /**
     * Use this method to retrieve ALL MeasurementSets (WITHOUT measurement data) related to
     * a specific attribute in an experiment. This method is useful if you want to retrieve all
     * measurement data (irrespective of which MetricGenerator created it) for a specific attribute.
     *
     * @param expID - Non-null experiment ID for the experiment the attribute belongs to
     * @param attr - Non-null attribute for which measurements are sought
     * @return - Returns (a possibly empty) set of measurement sets
     * @throws Exception - throws if the input parameters are null or invalid
     */
    Set<MeasurementSet> getAllEmptyMeasurementSetsForAttribute(UUID expID, Attribute attr) throws Exception {
        
        // Safety first
        if ( !started ) throw new Exception( "Cannot get experiments: service not started" );
        if ( expID == null ) throw new Exception( "Could not get Measurement Sets for Attribute: experiment ID is null" );
        if ( attr == null ) throw new Exception( "Could not get Measurement Sets for Attribute: Attribute is null" );
        
        HashSet<MeasurementSet> mSets = new HashSet<MeasurementSet>();
        
        // Get metric generators for this experiment
        try {
            Set<MetricGenerator> metGens = metricGenDAO.getMetricGeneratorsForExperiment( expID, true );
            
            Map<UUID, MeasurementSet> allMemSets = MetricHelper.getMeasurementSetsForAttribute( attr, metGens );
            mSets.addAll( allMemSets.values() );
        }
        catch ( Exception ex ) {
            String msg = "Could not retrieve all measurement sets for Attribute " + attr.getName() + ": " + ex.getMessage();
            
            logger.warn( msg );
            throw new Exception( msg, ex );
        }        

        return mSets;
    }
    
    /**
     * Use this method to retrieve MeasurementSets (WITHOUT measurement data) for
     * a specific attribute from a specific MetricGeneator in an experiment.
     * 
     * @param mgen        - MetricGenerator that may hold MeasurementSets for an Attribute
     * @param attr        - Target Attribute which may have measurements associated with it
     * @return            - returns a set of MeasurementSets for the specific MetricGenerator (if they exist)
     * @throws Exception  - throws if the parameters are invalid or the MetricGenerator/Attribute could not be found
     */
    Set<MeasurementSet> getEmptyMeasurementSetsForAttribute( MetricGenerator mgen, Attribute attr ) throws Exception {
        
        // Safety first
        if ( !started ) throw new Exception( "Cannot get experiments: service not started" );
        if ( mgen == null ) throw new Exception( "Could not get MeasurementSets for Attribute: MetricGenerator is null" );
        if ( attr == null ) throw new Exception( "Could not get MeasurementSets for Attribute: Attribute is null" );
        
        UUID attrID = attr.getUUID();
        if ( attrID == null ) throw new Exception( "Could not get MeasurementSets for Attribute: Attribute ID is null" );
        
        // Create a result set, then try populating
        HashSet<MeasurementSet> resultSet = new HashSet<MeasurementSet>();
        
        try {
            Map<UUID, MeasurementSet> entityMSets = MetricHelper.getAllMeasurementSets( mgen );
            
            // If there are measurement sets associated with this entity, add those linked to the target entity
            for ( MeasurementSet ms : entityMSets.values() ) {
                
                if ( ms != null ) {
                    // Check set is mapped to attribute
                    UUID srcAttrID = ms.getAttributeID();
                    
                    // Add it to the result set if good
                    if ( srcAttrID != null && srcAttrID.equals(attrID) )
                        resultSet.add( ms );
                }
                else
                    logger.warn( "Found NULL measurement set associated with attribute " + attr.getName() );
            }
        }
        catch ( Exception ex ) {
            String msg = "Had problems getting MeasurementSet for attribute " + attr.getName() + ": " + ex.getMessage();
            logger.warn( msg );
            
            throw ex;
        }
        
        return resultSet;
    }

    /**
     * Use this method to retrieve MeasurementSets that are populated with actual 
     * measurements (where they exist). Using this method requires that you specify 
     * a start and end range within which measurements are sought.
     *
     * @param expID - Non-null experiment ID for the experiment the attribute
     * belongs to
     * @param attr - Non-null attribute for which measurements are sought
     * @param start - Non-null start date indicating measurements should have
     * been created on or after this point in time
     * @param end - Non-null end date indicating measurements should have been
     * created on or before this point in time
     * @return - Returns a (possibly empty) set of measurement sets
     * @throws Exception - throws if the input parameters are null or invalid
     */
    Set<MeasurementSet> getMeasurementSetsForAttribute(UUID expID, Attribute attr, Date start, Date end) throws Exception {
        
        // Safety first
        if ( !started ) throw new Exception( "Cannot get experiments: service not started" );
        if ( expID == null ) throw new Exception( "Could not get Measurement Sets for Attribute: experiment ID is null" );
        if ( attr == null ) throw new Exception( "Could not get Measurement Sets for Attribute: Attribute is null" );
        if ( start == null || end == null ) throw new Exception( "Could not get Measurement Sets for Attribute: date(s) is null" );
        
        HashSet<MeasurementSet> resultSet = new HashSet<MeasurementSet>();
        
        try {
            // Get the MeasurementSet model first
            Set<MeasurementSet> msetInfo = getAllEmptyMeasurementSetsForAttribute( expID, attr );
            for ( MeasurementSet ms : msetInfo )
            {
                // Then populate with data
                if ( ms != null ) {
                    
                    Report report = expReportDAO.getReportForMeasurementsForTimePeriod( expID, start, end, true );
                    resultSet.add( report.getMeasurementSet() );
                }
                else {
                    String msg = "Had problems retrieving a measurement set: MS ID is null";
                    logger.warn( msg );
                    throw new Exception( msg );
                }
            }
        }
        catch ( Exception ex ) {
            String msg = "Had problems retrieving measurement set data for Attribute " + attr.getName() + ": " + ex.getMessage();
            logger.warn( msg );
            
            throw new Exception( msg, ex );
        }

        return resultSet;
    }
}
