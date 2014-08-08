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

import uk.ac.soton.itinnovation.ecc.service.domain.explorer.metrics.EccParticipantAttributeResultSet;
import uk.ac.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;
import uk.ac.soton.itinnovation.ecc.service.utils.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import org.springframework.stereotype.Service;

import javax.annotation.*;
import org.slf4j.*;

import java.util.*;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.metrics.EccAttributeInfo;






/**
 * The ExplorerService provides access to experiment data using both metric and
 * provenance queries to explorer aspects of the experiment.
 * 
 */
@Service("explorerService")
public class ExplorerService  
{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private ExplorerMetricsQueryHelper    metricsHelper;
    private ExplorerProvenanceQueryHelper provenanceHelper;
 
    private boolean serviceReady;
        
    
    public ExplorerService()
    {   
    }
    
    @PostConstruct
    public void init()
    {
        metricsHelper    = new ExplorerMetricsQueryHelper();
        provenanceHelper = new ExplorerProvenanceQueryHelper();
        
        serviceReady = false;
    }
    
    @PreDestroy
    public void shutdown()
    {
        if ( metricsHelper != null )    metricsHelper.shutdown();
        if ( provenanceHelper != null ) provenanceHelper.shutdown();
        
        serviceReady   = false;        
    }
    
    public boolean start( DatabaseConfiguration dbConfig )
    {
        serviceReady = false;
        
        // Check pre-requisites
        if ( metricsHelper == null || provenanceHelper == null )
        {
            String msg = "Could not start Explorer service: helpers not yet created";
            logger.error( msg );
            
            return false;
        }
        
        // Initialise helpers
        try
        {
            metricsHelper.initialise( dbConfig );
            provenanceHelper.initialise();
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
    
    public EccParticipantAttributeResultSet getPartCommonAttrResultSet( UUID expID, ArrayList<String> partIRIs )
    {
        EccParticipantAttributeResultSet result = new EccParticipantAttributeResultSet();
        
        // Safety
        if ( expID != null && partIRIs != null && !partIRIs.isEmpty() )
        {
            // Get common Attributes
            Set<Attribute> commonAttributes = metricsHelper.getPartCommonAttributes( expID, partIRIs );
            
            // Push each attribute into result appropriately (so long as it has a metric; it should do)
            for ( Attribute attr : commonAttributes )
            {
                Metric metric = metricsHelper.getAttributeMetric( expID, attr.getUUID() );
                
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
        else logger.warn( "Could not retrieve participant common attributes: parameter(s) invalid" );
        
        return result;
    }
}
