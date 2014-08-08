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

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import uk.ac.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;

import java.util.*;
import org.slf4j.*;





public class ExplorerMetricsQueryHelper
{
    private final Logger logger        = LoggerFactory.getLogger(getClass());
    private final String failedRequest = "Explorer metric query helper failed: uninitialied or parameter(s) invalid";
    
    private IMonitoringEDM     expDataManager;
    private IEntityDAO         entityDAO;
    private IMeasurementSetDAO msDAO;
    
    private boolean            helperInitialised;
    
    
    public ExplorerMetricsQueryHelper()
    {}
    
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
                entityDAO = expDataManager.getEntityDAO();
                msDAO     = expDataManager.getMeasurementSetDAO();
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
    
    public void shutdown()
    {
        msDAO          = null;
        entityDAO      = null;
        expDataManager = null;
    }
    
    public Set<Attribute> getPartCommonAttributes( UUID expID, ArrayList<String> partIRIs )
    {
        HashSet<Attribute> result = new HashSet<>();
        
        // Safety
        if ( helperInitialised && expID != null && partIRIs != null && !partIRIs.isEmpty() )
        {
            // Get participant related entities
            Set<Entity> partEntities = getParticipantEntities( expID, partIRIs );
            
            HashMap<String, Attribute> commonAttrs = new HashMap<>();
            
            // Assemble common attributes
            for ( Entity entity : partEntities )
                for ( Attribute attr : entity.getAttributes() )
                    commonAttrs.put( attr.getName(), attr );
            
            // Return result
            result.addAll( commonAttrs.values() );
        }
        else logger.error( failedRequest );
        
        return result;
    }
    
    public Metric getAttributeMetric( UUID expID, UUID attrID )
    {
        Metric result = null;
        
        // Safety
        if ( helperInitialised && attrID != null )
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
    
    // Private methods ---------------------------------------------------------
    private Set<Entity> getParticipantEntities( UUID expID, ArrayList<String> partIRIs )
    {
        HashSet<Entity> result = new HashSet<>();
        
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
                        if ( !result.contains(entity) ) result.add(entity);
                        break;
                    }
            }
        }
        catch ( Exception ex )
        { logger.warn( "Could not retrieve entities from EDM: " + ex.getMessage() ); }
        
        return result;
    }
}
