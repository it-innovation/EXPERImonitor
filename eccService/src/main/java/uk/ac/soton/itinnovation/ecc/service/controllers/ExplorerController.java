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

package uk.ac.soton.itinnovation.ecc.service.controllers;

import uk.ac.soton.itinnovation.ecc.service.domain.explorer.metrics.*;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.provenance.*;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.distributions.*;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.*;
import uk.ac.soton.itinnovation.ecc.service.services.*;
import uk.ac.soton.itinnovation.ecc.service.utils.ExplorerDemoData;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import org.slf4j.*;


/**
 * Exposes the EXPERIMonitor Explorer service endpoints
 * 
 */
@Controller
@RequestMapping("/explorer")
public class ExplorerController {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    // REMOVE THIS ONCE CONTROLLER IS COMPLETE ---------------------------------
    private final ExplorerDemoData demoData = new ExplorerDemoData();
    // --------------------------------- REMOVE THIS ONCE CONTROLLER IS COMPLETE
    
    @Autowired
    @Qualifier("explorerService")
    ExplorerService explorerService;
    
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/summary" )
    @ResponseBody
    public EccExperimentSummary getExperimentSummary( @PathVariable String expID )
    {
        EccExperimentSummary result = null;
        
        if ( explorerService != null && explorerService.isReady() )
        {
            result = demoData.expSummary;
        }
        else logger.error( "Could not execute explorer service: service is not ready" );
        
        return result;
    }
    
    // Participant based queries -----------------------------------------------
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/participants" )
    @ResponseBody
    public EccParticipantResultSet getParticipants( @PathVariable String expID )
    {
        EccParticipantResultSet result = null;
        
        if ( explorerService != null && explorerService.isReady() )
        {
            result = demoData.eccParticipants;            
        }
        else logger.error( "Could not execute explorer service: service is null" );
        
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/participants/iri")
    @ResponseBody
    public EccParticipant getParticipantByIRI( @PathVariable String expID,
                                               @RequestParam(value="IRI", defaultValue="") String IRI )
    {
        EccParticipant result = null;
        
        if ( explorerService != null && explorerService.isReady() )
        {
            result = demoData.getParticipant( IRI );
        }
        else logger.error( "Could not execute explorer service: service is null" );
        
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/participants/iri/attributes" )
    @ResponseBody
    public EccParticipantAttributeResultSet getParticipantAttributes( @PathVariable UUID expID,
                                                                      @RequestParam(value="IRI", defaultValue="") String IRI )
    {
        EccParticipantAttributeResultSet result = null;
        
        if ( explorerService != null && explorerService.isReady() )
        {
            // Will be using: explorerService.getPartCommonAttrResultSet(..)
            result = demoData.partAttrInfoSet;
        }
        else logger.error( "Could not execute explorer service: service is null" );
        
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/participants/groupAttributes" )
    @ResponseBody
    public EccParticipantAttributeResultSet getParticipantAttributes( @PathVariable UUID expID )
    {
        EccParticipantAttributeResultSet result = null;
        
        if ( explorerService != null && explorerService.isReady() )
        {
            // Will be using: explorerService.getPartCommonAttrResultSet(..)
            result = demoData.partAttrInfoSet;
        }
        else logger.error( "Could not execute explorer service: service is null" );
        
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/participants/attributes/select" )
    @ResponseBody
    public EccParticipantResultSet getParticipantsQoEAttributeSelection( @PathVariable UUID expID,
                                                                         @RequestParam(value="attrName", defaultValue = "")  String attrName,
                                                                         @RequestParam(value="nomOrdLabel", defaultValue="") String nomOrdLabel )
    {
        EccParticipantResultSet result = null;
        
        if ( explorerService != null && explorerService.isReady() )
        {
            // Will be using: explorerService.getPartQoEAttrSelection(..)
            result = demoData.getParticipantsByAttributeScaleLabel( attrName, nomOrdLabel );
        }
        else logger.error( "Could not execute explorer service: service is null" );
        
        return result;
    }
    
    // Metric distribution based queries ---------------------------------------
    // -------------------------------------------------------------------------
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/participants/iri/distribution/qoe" )
    @ResponseBody
    public EccNOMORDParticipantSummary getNOMORDParticipantDistribution( @PathVariable UUID expID,
                                                                         @RequestParam(value="IRI", defaultValue="") String partIRI )
    {
        EccNOMORDParticipantSummary result = null;
        
        if ( explorerService != null && explorerService.isReady() )
        {
            // Will be using: getPartQoEDistribution(...)
            result = demoData.qoeParticipantSummaryData.get( partIRI );
        }
        else logger.error( "Could not execute explorer service: service is null" );
        
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET, value="/{expID}/participants/distribution/stratified" )
    @ResponseBody
    public ArrayList<EccNOMORDStratifiedSummary> getNOMORDStratifiedParticipantDistribution( @PathVariable UUID expID )
    {
        ArrayList<EccNOMORDStratifiedSummary> result = null;
        
        if ( explorerService != null && explorerService.isReady() )
        {
            // Will be using: getPartQoEStratifiedSummary(...)
            result = demoData.qoeStratifiedSummaryDistribData;
        }
        else logger.error( "Could not execute explorer service: service is null" );
        
        return result;
    }
    
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/attributes/distribution/qoe" )
    @ResponseBody
    public ArrayList<EccNOMORDAttributeSummary> getNOMORDAttributeDistributionDataByName( @PathVariable UUID expID,
                                                                                          @RequestParam(value="attrName", defaultValue="") String attrName )
    {
        ArrayList<EccNOMORDAttributeSummary> result = null;
        
        if ( explorerService != null && explorerService.isReady() )
        {
            result = new ArrayList<>();
            result.add( demoData.qoeSummaryDistribData.get( attrName ) );
        }
        else logger.error( "Could not execute explorer service: service is null" );
        
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/attributes/distribution/qos/discrete" )
    @ResponseBody
    public EccINTRATSummary getINTRATAttributeDistributionDataByID( @PathVariable UUID expID,
                                                                    @RequestParam(value="attrID", defaultValue="")     UUID attrID,
                                                                    @RequestParam(value="timeStamps", defaultValue="") String timeStamps )
    {
        EccINTRATSummary result = null;
        
        if ( explorerService != null && explorerService.isReady() )
        {
            // Process timeStams as a comman delimited list of long values
            String[] stamps = timeStamps.split( "," );
            ArrayList<Long> validTimeStamps = new ArrayList<>();

            for ( String stamp : stamps )
            {
                try
                {
                    validTimeStamps.add( Long.parseLong( stamp ) );
                }
                catch ( NumberFormatException nfe )
                { logger.warn( "Caught bad time stamp parameter for QoS discrete distribution query"); }
            }
            
            result = demoData.getINTRATDistDataDiscrete( attrID, validTimeStamps );
            
        }
        else logger.error( "Could not execute explorer service: service is null" );
        
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/attributes/series/qos/highlight/activities" )
    @ResponseBody
    public EccINTRATSeriesSet getINTRATAttributeSeriesHighlightActivities( @PathVariable UUID expID,
                                                                           @RequestParam(value="attrID", defaultValue="")    UUID   attrID,
                                                                           @RequestParam(value="IRI",       defaultValue="") String IRI,
                                                                           @RequestParam(value="actLabel",  defaultValue="") String actLabel )
    {
        EccINTRATSeriesSet result = null;
        
        if ( explorerService != null && explorerService.isReady() )
        {
            return demoData.getINTRATSeriesHighlightActivities( attrID, IRI, actLabel );
        }
        else logger.error( "Could not execute explorer service: service is not ready" );
        
        return result;
    }
     
    // Activity based queries --------------------------------------------------
    // -------------------------------------------------------------------------
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/participants/iri/activities" )
    @ResponseBody
    public EccParticipantActivityResultSet getParticipantActivities( @PathVariable UUID expID,
                                                                     @RequestParam(value="IRI", defaultValue = "") String IRI )
    {
        EccParticipantActivityResultSet result = null;
                
        if ( explorerService != null && explorerService.isReady() )
        {
            result = demoData.getActivitiesByParticipant( IRI );
        }
        else logger.error( "Could not execute explorer service: service is null" );
        
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/participants/iri/activities/select" )
    @ResponseBody
    public EccParticipantActivityResultSet getParticipantActivitiesByName( @PathVariable UUID expID,
                                                                           @RequestParam(value="IRI", defaultValue = "") String IRI,
                                                                           @RequestParam(value="actLabel", defaultValue ="") String actLabel )
    {
        EccParticipantActivityResultSet result = null;
                
        if ( explorerService != null && explorerService.isReady() )
        {
            // For now just return the same activities - we've only got one set in the demo
            result = demoData.getActivitiesByParticipant( IRI );
        }
        else logger.error( "Could not execute explorer service: service is null" );
        
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/participants/iri/activities/summary" )
    @ResponseBody
    public EccParticipantActivitySummaryResultSet getParticipantActivitiesSummary( @PathVariable UUID expID,
                                                                                   @RequestParam(value="IRI", defaultValue = "") String IRI )
    {
        EccParticipantActivitySummaryResultSet result = null;
        
        if ( explorerService != null && explorerService.isReady() )
        {
            result = demoData.getPROVSummaryByParticipant( IRI );
        }
        else logger.error( "Could not execute explorer service: service is null" );
        
        return result;
    }
    
    // Application based queries -----------------------------------------------
    // -------------------------------------------------------------------------
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/activities/iri/applications" )
    @ResponseBody
    public EccActivityApplicationResultSet getActivityApplications( @PathVariable UUID expID,
                                                                    @RequestParam(value="IRI", defaultValue = "") String IRI )
    {
        EccActivityApplicationResultSet result = null;
        
        if ( explorerService != null && explorerService.isReady() )
        {
            result = demoData.getApplicationsByActivity( IRI );
        }
        else logger.error( "Could not execute explorer service: service is null" );
        
        return result;
    }
    
    // Application based queries -----------------------------------------------
    // -------------------------------------------------------------------------
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/applications/iri/services" )
    @ResponseBody
    public EccApplicationServiceResultSet getApplicationServices( @PathVariable UUID expID,
                                                                  @RequestParam(value="IRI", defaultValue = "") String IRI )
    {
        EccApplicationServiceResultSet result = null;
        
        if ( explorerService != null && explorerService.isReady() )
        {
            result = demoData.getServicesByApplication( IRI );
        }
        else logger.error( "Could not execute explorer service: service is null" );
        
        return result;
    }
    
    // Service based queries -----------------------------------------------
    // -------------------------------------------------------------------------
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/services/iri/attributes" )
    @ResponseBody
    public EccAttributeResultSet getServiceAttributes( @PathVariable UUID expID,
                                                       @RequestParam(value="IRI", defaultValue = "") String IRI )
    {
        EccAttributeResultSet result = null;
        
        if ( explorerService != null && explorerService.isReady() )
        {
            
            result = demoData.serviceQoSAttributes.get( IRI );            
        }
        else logger.error( "Could not execute explorer service: service is null" );
        
        return result;
    }
    
}
