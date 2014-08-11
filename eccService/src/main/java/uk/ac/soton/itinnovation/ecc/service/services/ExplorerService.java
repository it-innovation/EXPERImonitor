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
//      Created Date :          18-Jul-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.ecc.service.services;

import uk.ac.soton.itinnovation.ecc.service.domain.explorer.metrics.*;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.provenance.*;
import uk.ac.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;
import uk.ac.soton.itinnovation.ecc.service.utils.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import org.springframework.stereotype.Service;

import javax.annotation.*;
import org.slf4j.*;

import java.util.*;




/**
 * The ExplorerService provides access to experiment data using both metric and
 * provenance queries to explorer aspects of the experiment.
 * 
 */
@Service("explorerService")
public class ExplorerService  
{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private ExplorerMetricsQueryHelper    metricsQueryHelper;
    private ExplorerProvenanceQueryHelper provenanceQueryHelper;
 
    private boolean serviceReady;
        
    
    public ExplorerService()
    {   
    }
    
    @PostConstruct
    public void init()
    {
        metricsQueryHelper    = new ExplorerMetricsQueryHelper();
        provenanceQueryHelper = new ExplorerProvenanceQueryHelper();
        
        serviceReady = false;
    }
    
    @PreDestroy
    public void shutdown()
    {
        if ( metricsQueryHelper != null )    metricsQueryHelper.shutdown();
        if ( provenanceQueryHelper != null ) provenanceQueryHelper.shutdown();
        
        serviceReady   = false;        
    }
    
    public boolean start( DatabaseConfiguration dbConfig )
    {
        serviceReady = false;
        
        // Check pre-requisites
        if ( metricsQueryHelper == null || provenanceQueryHelper == null )
        {
            String msg = "Could not start Explorer service: helpers not yet created";
            logger.error( msg );
            
            return false;
        }
        
        // Initialise helpers
        try
        {
            metricsQueryHelper.initialise( dbConfig );
            provenanceQueryHelper.initialise();
        }
        catch ( Exception ex )
        {
            String msg = "Could not start Explorer service: data helpers not initialised: " + ex.getMessage();
            logger.error( msg );
            
            return false;
        }
        
        // All good
        serviceReady = true;
        
        return serviceReady;
    }
    
    public boolean isReady()
    { return serviceReady; }
    
    public EccParticipantAttributeResultSet getPartCommonAttrResultSet( UUID expID, ArrayList<String> allPartIRIs )
    {
        EccParticipantAttributeResultSet result = new EccParticipantAttributeResultSet();
        
        // Safety
        if ( expID != null && allPartIRIs != null && !allPartIRIs.isEmpty() )
        {
            // Get common Attributes
            Map<UUID,Attribute> commonAttributes = metricsQueryHelper.getPartCommonAttributes( expID, allPartIRIs );
            
            // Push each attribute into result appropriately (so long as it has a metric; it should do)
            for ( Attribute attr : commonAttributes.values() )
            {
                Metric metric = metricsQueryHelper.getAttributeMetric( expID, attr.getUUID() );
                
                if ( metric != null )
                {
                    // Create attribute info
                    EccAttributeInfo info = new EccAttributeInfo( attr.getName(),
                                                                  attr.getDescription(),
                                                                  attr.getUUID(),
                                                                  metric.getUnit().getName(),
                                                                  metric.getMetricType().name(),
                                                                  metric.getMetaType(),
                                                                  metric.getMetaContent() );
                    // Place into appropriate list
                    switch ( metric.getMetricType() )
                    {
                        case NOMINAL :
                        case ORDINAL : result.addQoEAttribute( info ); break;
                            
                        case INTERVAL :
                        case RATIO    : result.addOtherAttribute( info ); break;
                    }
                }
                else logger.warn( "Found attribute without metric. Not included in common attribute result set" );
            }
        }
        else logger.error( "Could not retrieve participant common attributes: parameter(s) invalid" );
        
        return result;
    }
    
    public EccParticipantResultSet getPartQoEAttrSelection( UUID              expID, 
                                                            ArrayList<String> allPartIRIs,
                                                            String            attrName,
                                                            String            selLabel )
    {
        EccParticipantResultSet result = new EccParticipantResultSet();
        
        // Safety
        if ( expID != null && allPartIRIs != null && !allPartIRIs.isEmpty() &&
             attrName != null && selLabel != null )
        {
            // Get all entities representing participants
            Map<UUID,Entity> entities = metricsQueryHelper.getParticipantEntities( expID, allPartIRIs );
            
            // Get all the attributes of the given name from the participants
            Map<UUID,Attribute> attrsByEntities = metricsQueryHelper.getAllEntityAttributes( entities.values() );
            
            // Select just those attributes with the target name
            HashSet<Attribute> selAttributes = new HashSet<>();
            
            for ( Attribute attr : attrsByEntities.values() )
                if ( attr.getName().equals(attrName) ) selAttributes.add( attr );
            
            // For select attributes, retrieve measurement set(s) and search for label instance & add Entities
            HashSet<Entity> selectedEntities = new HashSet<>();
            
            for ( Attribute attr : selAttributes )
            {
                Map<UUID,MeasurementSet> msets = metricsQueryHelper.getMeasurementSetsForAttribute( expID, attr.getUUID() );
                
                boolean foundLabel = false;
                
                for ( MeasurementSet ms : msets.values() )
                {
                    // If the instance exists, add participant to result
                    for ( Measurement m : ms.getMeasurements() )
                    {
                        if ( m.getValue().equals(selLabel) )
                        {
                            foundLabel = true;
                            
                            // Find entity and add to result set            
                            Entity targetEntity = entities.get( attr.getEntityUUID() );
                            
                            if ( targetEntity != null )
                            {
                               if ( !selectedEntities.contains(targetEntity) )
                                    selectedEntities.add( targetEntity );
                            }
                            else
                                logger.error( "Could not retrieve QoE label selection: entity not found for attribute");
                            
                            // Stop the search - no need to search again
                            break;
                        }
                    }
                    
                    // If we've already found the label, don't bother searching other measurement sets
                    if ( foundLabel ) break;
                }
            }
            
            // Finally, create the PROV participant information for any metric entities we have found
            // We don't actually need any additional information than that already found in the metric data base for this
            for ( Entity entity : selectedEntities )
                result.addParticipant( new EccParticipant( entity.getName(),
                                                           entity.getDescription(),
                                                           entity.getUUID(),
                                                           entity.getEntityID() ) );
        }
        else logger.error( "Could not retrieve QoE label selection: parameter(s) invalid" ); 
        
        return result;
    }
}
