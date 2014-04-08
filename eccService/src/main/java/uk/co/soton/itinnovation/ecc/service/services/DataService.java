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
import java.util.Set;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.openprovenance.prov.xml.Attribute;
import org.openprovenance.prov.xml.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.co.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;

/**
 * Provides access to data in the database.
 */
public class DataService {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private boolean dataServiceInitialised;


    public DataService() {
    }

    /**
     * Initialises data service with a configuration profile taken from the 
     * Configuration Server.
     * 
     * @param dc         - Non-null and complete database configuration
     * @throws Exception - Throws if configuration is incomplete or database connection cannot be made
     */
    @PostConstruct
    public void init(DatabaseConfiguration dc) throws Exception {
        logger.info("Initialising data service");
        
        if ( dataServiceInitialised ) throw new Exception( "Could not initialise service: data service is already initialised" );
        
        
        if ( dataServiceInitialised )
            logger.info( "Data service initialised" );
        else
            logger.error( "Failed to initialise data service" );
    }
    
    /**
     * Returns true if the data service has been successfully initialised.
     * 
     * @return - true if service is OK.
     */
    public boolean isServiceInitialised() {
        return dataServiceInitialised;
    }
   
    /**
     * Use this method to shutdown connections to the databases used by this service.
     * 
     * @throws Exception - throws if the service is not properly initialised or service fails to shutdown.
     */
    public void shutdown() throws Exception {
        
        if ( !dataServiceInitialised ) throw new Exception( "Could not shut down service: not initialised" );
    }
    
    /**
     * Use this method to retrieve high-level meta-data relating to all known experiments
     * stored in the data service database. This method will return instances of class Experiment
     * but these will not contain metric models.
     * 
     * @return           - Returns a set of experiment instances (high-level data only)
     * @throws Exception - throws if the service is not initialised or there was a problem with the database
     */
    Set<Experiment> getAllKnownExperiments() throws Exception {
        
        HashSet<Experiment> experiments = new HashSet<Experiment>();
        
        
        return experiments;
    }
    
    /**
     * Use this method to retrieve high-level experiment meta-data and also all known 
     * MetricGenerators currently associated with the experiment.
     * 
     * @param expID      - UUID of the experiment required
     * @return           - Experiment instance with all currently known metric generators
     * @throws Exception - throws if the service is not initialised or there was a problem with the database
     */
    Experiment getExperimentWithMetricModels( UUID expID ) throws Exception {
        Experiment experiment = null;
        
        
        return experiment;                
    }
    
    /**
     * Use this method to quickly extract all known (metric) entities from an experiment.
     * 
     * @param expID         - ID of the experiment you need entities from.
     * @return              - Returns a set of entities for the experiment (if any exist)
     * @throws Exception    - throws if the service is not initialised or there was a problem with the database
     */
    Set<Entity> getEntitiesForExperiment( UUID expID ) throws Exception {
        HashSet<Entity> entities = new HashSet<Entity>();
        
        return entities;
    }
    
    /**
     * Use this method to retrieve all measurement sets related to a specific attribute.
     * In some cases, a client (or clients) may share an attribute and attach more than one
     * set of measurements for that attribute.
     * 
     * @param expID         - Non-null experiment ID for the experiment the attribute belongs to
     * @param attr          - Non-null attribute for which measurements are sought
     * @return              - Returns (a possibly empty) set of measurement sets
     * @throws Exception    - throws if the input parameters are null or invalid
     */
    Set<MeasurementSet> getMeasurementSetsForAttribute( UUID expID, Attribute attr ) throws Exception {
        HashSet<MeasurementSet> mSets = new HashSet<MeasurementSet>();
        
        
        return mSets;
    }
    
    /**
     * This is an overloaded version of the getMeasurementSetsForAttribute(..) method in which it
     * is possible to specific a start and end range within which measurements are sought.
     * 
     * @param expID         - Non-null experiment ID for the experiment the attribute belongs to
     * @param attr          - Non-null attribute for which measurements are sought
     * @param start         - Non-null start date indicating measurements should have been created on or after this point in time
     * @param end           - Non-null end date indicating measurements should have been created on or before this point in time
     * @return              - Returns a (possibly empty) set of measurement sets
     * @throws Exception    - throws if the input parameters are null or invalid
     */
    Set<MeasurementSet> getMeasurementSetsForAttribute( UUID expID, Attribute attr, Date start, Date end ) throws Exception {
        HashSet<MeasurementSet> mSets = new HashSet<MeasurementSet>();
        
        
        return mSets;
    }

}
