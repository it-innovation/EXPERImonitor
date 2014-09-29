/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2014
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
//      Created By :            Simon Crowle
//      Created Date :          08-Aug-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.ecc.service.utils;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import uk.ac.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;

import java.util.*;
import org.slf4j.*;





public class ExplorerMetricsQueryHelper
{
    private final Logger logger        = LoggerFactory.getLogger(getClass());
    private final String failedRequest = "Explorer metric query helper failed: uninitialied or parameter(s) invalid";
    
    private IMonitoringEDM     expDataManager;
    private IExperimentDAO     experimentDAO;
    private IEntityDAO         entityDAO;
    private IMeasurementSetDAO msDAO;
    private IReportDAO         reportDAO;
    
    private boolean            helperInitialised;
    
    
    public ExplorerMetricsQueryHelper()
    {}
    
    /**
     * Initialises query helper with appropriate database configuration
     * 
     * @param dbConfig      - Non-null database configuration instance
     * @throws Exception    - Throws if configuration is invalid or database unreachable
     */
    public void initialise( DatabaseConfiguration dbConfig ) throws Exception
    {
        helperInitialised = false;
        
        // Safety first
        if ( dbConfig == null )
        {
            String msg = "Could not start Explorer service: database config is null";
            logger.error( msg );
            
            throw new Exception( msg );
        }
        
        expDataManager = null;
        entityDAO      = null;
        
        // Create properties for EDM Factory
        Properties dbProps = new Properties();
        dbProps.put( "dbPassword", dbConfig.getUserPassword() );
        dbProps.put( "dbName",     dbConfig.getDatabaseName() );
        dbProps.put( "dbType",     dbConfig.getDatabaseType() );
        dbProps.put( "dbURL",      dbConfig.getUrl() );
        dbProps.put( "dbUsername", dbConfig.getUserName() );
        
        // Try getting the db accessors
        try
        {
            expDataManager = EDMInterfaceFactory.getMonitoringEDM( dbProps );
            
            if ( expDataManager.isDatabaseSetUpAndAccessible() )
            {
                experimentDAO = expDataManager.getExperimentDAO();
                entityDAO     = expDataManager.getEntityDAO();
                msDAO         = expDataManager.getMeasurementSetDAO();
                reportDAO     = expDataManager.getReportDAO();
            }
            else
            {
                String msg = "Could not start Explorer service: database is not accessible";
                logger.error( msg );
                
                throw new Exception( msg );
            }
        }
        catch ( Exception ex )
        {
            String msg = "Could not start Explorer service: database accessors not available: " + ex.getMessage();
            logger.error( msg, ex );
            
            throw new Exception( msg, ex );
        }
        
        helperInitialised = true;
    }
    
    /**
     * Cleans up database connections
     * 
     */
    public void shutdown()
    {
        reportDAO      = null;
        msDAO          = null;
        entityDAO      = null;
        experimentDAO  = null;
        expDataManager = null;
    }
    
    public Experiment getExperiment( UUID expID )
    {
        Experiment result = null;
        
        if ( helperInitialised && expID != null )
        {
            try
            {
                result = experimentDAO.getExperiment( expID, true );
            }
            catch ( Exception ex )
            { logger.error( "Could not find experiment: ", ex ); }
        }
        
        return result;
    }
    
    public Entity getEntity( UUID expID, UUID entID )
    {
        Entity result = null;
        
        if ( helperInitialised && expID != null && entID != null )
        {
            try
            {
                result = entityDAO.getEntity( entID, true );
            }
            catch ( Exception ex )
            { logger.error( "Could not retrieve entity from database ", ex ); }
        }
        
        return result;
    }
    
    public Attribute getAttribute( UUID expID, UUID attrID )
    {
        Attribute result = null;
        
        if ( helperInitialised && expID != null && attrID != null )
        {
            try
            {
                // Retrieve all entities
                Set<Entity> entities = entityDAO.getEntitiesForExperiment( expID, true );
                
                if ( entities != null ) 
                    result = MetricHelper.getAttributeFromEntities( attrID, entities ); 
            }
            catch ( Exception ex )
            { logger.error( "Could not find attribute in database ",ex ); }
            
        
        }
        else logger.error( failedRequest );
        
        return result;
    }
    
    /**
     * Use this method to retrieve a Metric Entity based on a PROV IRI
     * 
     * @param expID     - Non-null experiment ID
     * @param partIRI   - Non-null PROV IRI
     * @return          - Returns a metric Entity (null if it does not exist)
     */
    public Entity getParticipantEntity( UUID expID, String partIRI )
    {
        Entity result = null;
        
        if ( helperInitialised && expID != null && partIRI != null )
        {
            try
            {
                // Retrieve all entities
                Set<Entity> entities = entityDAO.getEntitiesForExperiment( expID, true );

                // Try find entity
                for ( Entity entity : entities )
                    if ( entity.getEntityID().equals(partIRI) )
                    {
                        result = entity;
                        break;
                    }
            }
            catch ( Exception ex )
            { logger.warn( "Could not retrieve entities from EDM: " + ex.getMessage() ); }
        }
        else logger.error( failedRequest );
        
        return result;        
    }
    
    /**
     * Use this method to retrieve the metric Entities that map to PROV participants.
     * 
     * @param expID     - Non-null experiment ID
     * @param partIRIs  - List of known PROV participants for the experiment
     * @return          - returns a map of metric Entities indexed by their metric IDs
     */
    public Map<UUID,Entity> getParticipantEntities( UUID expID, Collection<String> partIRIs )
    {
        HashMap<UUID,Entity> result = new HashMap<>();
        
        if ( helperInitialised && expID != null && partIRIs != null && !partIRIs.isEmpty() )
        {
            try
            {
                // Retrieve all entities
                Set<Entity> entities = entityDAO.getEntitiesForExperiment( expID, true );

                // Look through targets to see if we can find them in the entity set
                for ( String iri : partIRIs )
                {
                    for ( Entity entity : entities )
                        if ( entity.getEntityID().equals(iri) )
                        {
                            UUID entityID = entity.getUUID();
                            
                            if ( !result.containsKey(entityID) ) 
                                result.put(entityID, entity);
                            break;
                        }
                }
            }
            catch ( Exception ex )
            { logger.warn( "Could not retrieve entities from EDM: " + ex.getMessage() ); }
        }
        else logger.error( failedRequest );
        
        return result;
    }
    
    /**
     * Use this method to retrieve a map of attribute instances indexed by (common)
     * attribute names.
     * 
     * @param expID     - Non-null experiment ID within which to search
     * @param attrNames - Non-null/empty list of attributes names of interest
     * @return          - A map of attribute instances, indexed by the name list
     */
    public Map<String,Set<Attribute>> getAttributeInstancesByName( UUID expID, Collection<String> attrNames )
    {
        HashMap<String,Set<Attribute>> result = new HashMap<>();
        
        // Safety
        if ( helperInitialised && expID != null && attrNames != null && !attrNames.isEmpty() )
        {
            try
            {
                // Retrieve all entities
                Set<Entity> entities = entityDAO.getEntitiesForExperiment( expID, true );
                
                if ( entities != null )
                {
                    // For each matchable attribute, search entities for any instances
                    for ( String targAttrName : attrNames )
                    {
                        HashSet<Attribute> attrInstances = new HashSet<>();
                        
                        // Search entities
                        for ( Entity ent : entities )
                            for ( Attribute entAttr : ent.getAttributes() )
                                if ( entAttr.getName().equals(targAttrName) )
                                    attrInstances.add( entAttr );

                        // Place instances in result
                        result.put( targAttrName, attrInstances );
                    }   
                } 
            }    
            catch ( Exception ex )
            { logger.warn( "Could not retrieve entities from EDM: " + ex.getMessage() ); }
        }
        else logger.error( failedRequest );
        
        return result;
    }
    
    /**
     * Use this method to retrieve an aggregated set of attributes associated with the metric
     * Entities representing the PROV participants
     * 
     * @param expID     - Non-null experiment ID
     * @param partIRIs  - List of known PROV participants for the experiment
     * @return          - Returns a map of metric Attributes indexed by their metric IDs
     */
    public Map<UUID,Attribute> getPartCommonAttributes( UUID expID, Collection<String> partIRIs )
    {
        HashMap<UUID,Attribute> result = new HashMap<>();
        
        // Safety
        if ( helperInitialised && expID != null && partIRIs != null && !partIRIs.isEmpty() )
        {
            // Get participant related entities
            Map<UUID, Entity> partEntities = getParticipantEntities( expID, partIRIs );
            
            HashMap<String, Attribute> commonAttrs = new HashMap<>();
            
            // Assemble common attributes
            for ( Entity entity : partEntities.values() )
                for ( Attribute attr : entity.getAttributes() )
                    commonAttrs.put( attr.getName(), attr );
            
            // Recompile result
            for ( Attribute attr : commonAttrs.values() )
                result.put( attr.getUUID(), attr );
        }
        else logger.error( failedRequest );
        
        return result;
    }
    
    /**
     * Use this method to retrieve ALL the attribute instances associated with a collection
     * of Entities.
     * 
     * @param entities - Non-null collection of Entities to extract attributes
     * @return         - A map of Attributes indexed by their metric ID
     */
    public Map<UUID,Attribute> getAllEntityAttributes( Collection<Entity> entities )
    {
        HashMap<UUID,Attribute> result = new HashMap<>();
        
        if ( helperInitialised && entities != null && !entities.isEmpty() )
        {
            for ( Entity entity : entities )
                for ( Attribute attr : entity.getAttributes() )
                    result.put( attr.getUUID(), attr );
        }
        else logger.error( failedRequest );
        
        return result;
    }
    
    /**
     * Use this method to retrieve all measurement sets associated with an Attribute (by ID)
     * 
     * @param expID            - Non-null ID of the experiment
     * @param attrID           - Non-null ID of the attribute
     * @param withMeasurements - Set true if actual measurements are required for this set
     * @return                 - returns a map of measurement sets indexed by their metric IDs
     */
    public Map<UUID,MeasurementSet> getMeasurementSetsForAttribute( UUID expID, UUID attrID, boolean withMeasurements )
    {
        HashMap<UUID,MeasurementSet> result = new HashMap<>();
        
        if ( helperInitialised && expID != null && attrID != null )
        {
            try
            {
                Set<MeasurementSet> sets = msDAO.getMeasurementSetsForAttribute(attrID, expID, true);
                
                for ( MeasurementSet ms : sets )
                    result.put( ms.getID(), ms );
                
                // Collect data if required
                if ( withMeasurements )
                    for ( MeasurementSet ms : sets )
                    {
                        try
                        {
                            Report report = 
                                reportDAO.getReportForAllMeasurements(ms.getID(), true);
                        
                            ms.addMeasurements( report.getMeasurementSet().getMeasurements() );
                        }
                        catch ( Exception ex )
                        { logger.warn( "Did not find measurements for MS: " + ms.getID().toString() );
                            
                        }
                    }
            }
            catch ( Exception ex )
            {
                logger.warn( "Could not retrieve measurement sets for attribute: " + ex );
            }
        }
        else logger.error( failedRequest );
        
        return result;
    }
    
    /**
     * Use this method to combined multiple measurement sets belonging to a set of
     * common attributes. Important note: all the input attributes must have the exact same
     * Metric type and semantics
     * 
     * @param expID         - Non-null ID of experiment
     * @param attributes    - Non-null/empty set of attributes
     * @return              - Single measurement set representing all valid measurement data
     */
    public MeasurementSet getCombinedMeasurementSetDataForAttributes( UUID expID, Collection<Attribute> attributes )
    {
        MeasurementSet result = null;
        
        if ( helperInitialised && attributes != null && !attributes.isEmpty() )
        {
            // Create super set of measurement sets
            HashSet<MeasurementSet> superMSSet = new HashSet<>();
            for ( Attribute attr: attributes )
            {
                Collection<MeasurementSet> sets = 
                        getMeasurementSetsForAttribute( expID, attr.getUUID(), true ).values();
                
                superMSSet.addAll( sets );
            }
            
            // Try to reduce to a single set
            try
            {
                result = MetricHelper.combineMeasurementSets( superMSSet );
            }
            catch ( Exception ex )
            { logger.error( "Could not combine measurement sets for attribute", ex ); }
        }
        else logger.error( failedRequest );
        
        return result;
    }
    
    /**
     * Use this method to extract a Metric instance from an attribute. Note that it is possible
     * for an Attribute to have more than one metric associated with it (this is not current common);
     * this method retrieves the first available metric from the database.
     * 
     * @param expID     - Non-null Experiment ID
     * @param attrID    - Non-null Attribute ID
     * @return          - Returns the Metric instance for the attribute (may be null)
     */
    public Metric getAttributeMetric( UUID expID, UUID attrID )
    {
        Metric result = null;
        
        // Safety
        if ( helperInitialised && expID != null && attrID != null )
        {
            try
            {
                Set<MeasurementSet> mSets = msDAO.getMeasurementSetsForAttribute( attrID, expID, true );
                
                // Just use the first measurement set's metric information
                if ( !mSets.isEmpty() )
                    result = mSets.iterator().next().getMetric();
            }
            catch ( Exception ex )
            {
                String msg = "Could not retrieve metric for attribute: " + ex.getMessage();
                logger.warn( msg );
            }
        }
        else logger.error( failedRequest );
        
        return result;
    }
    
    public boolean isQoSAttribute( UUID expID, UUID attrID ) throws Exception
    {
        Metric metric = getAttributeMetric( expID, attrID );
        
        if ( metric != null )
        {
            MetricType mt = metric.getMetricType();
            
            if ( mt == MetricType.INTERVAL || mt == MetricType.RATIO )
                return true;
        }
        else throw new Exception( "Cannot determine metric type: no metric data" );
        
        return false;
    }
    
    public boolean isQoEAttribute( UUID expID, UUID attrID ) throws Exception
    {
        Metric metric = getAttributeMetric( expID, attrID );
        
        if ( metric != null )
        {
            MetricType mt = metric.getMetricType();
            
            if ( mt == MetricType.NOMINAL || mt == MetricType.ORDINAL )
                return true;
        }
        else throw new Exception( "Cannot determine metric type: no metric data" );
        
        return false;
    }
    
    
}
