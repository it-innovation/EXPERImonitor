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
    @Qualifier("dataService")
    DataService dataService;
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/summary" )
    @ResponseBody
    public EccExperimentSummary getExperimentSummary( @PathVariable String expID )
    {
        return demoData.expSummary;
    }
    
    // Participant based queries -----------------------------------------------
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/participants" )
    @ResponseBody
    public EccParticipantResultSet getParticipants( @PathVariable String expID )
    {
        return demoData.eccParticipants;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/participants/iri")
    @ResponseBody
    public EccParticipant getParticipantByIRI( @PathVariable String expID,
                                               @RequestParam(value="IRI", defaultValue="") String IRI )
    {        
        EccParticipant part = demoData.getParticipant( IRI );
        
        return part;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/participants/groupAttributes" )
    @ResponseBody
    public EccParticipantAttributeResultSet getParticipantAttributes( @PathVariable UUID expID )
    {
        return demoData.partAttrInfoSet;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/participants/iri/attributes" )
    @ResponseBody
    public EccParticipantAttributeResultSet getParticipantAttributes( @PathVariable UUID expID,
                                                                      @RequestParam(value="IRI", defaultValue="") String IRI )
    {    
        return demoData.partAttrInfoSet;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/participants/attributes/select" )
    @ResponseBody
    public EccParticipantResultSet getParticipantsByAttributeNomOrdValue( @PathVariable UUID expID,
                                                                          @RequestParam(value="attrName", defaultValue = "")  String attrName,
                                                                          @RequestParam(value="nomOrdLabel", defaultValue="") String nomOrdLabel )
    {        
        return demoData.getParticipantsByAttributeScaleLabel( attrName, nomOrdLabel );
    }
    
    // Metric distribution based queries ---------------------------------------
    // -------------------------------------------------------------------------
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/participants/iri/distribution/qoe" )
    @ResponseBody
    public EccNOMORDParticipantSummary getNOMORDParticipantDistribution( @PathVariable UUID expID,
                                                                         @RequestParam(value="IRI", defaultValue="") String partIRI )
    {
        return demoData.qoeParticipantSummaryData.get( partIRI );
    }
    
    @RequestMapping(method = RequestMethod.GET, value="/{expID}/participants/distribution/stratified" )
    @ResponseBody
    public ArrayList<EccNOMORDStratifiedSummary> getNOMORDStratifiedParticipantDistribution( @PathVariable UUID expID )
    {
        return demoData.qoeStratifiedSummaryDistribData;
    }
    
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/attributes/distribution/qoe" )
    @ResponseBody
    public ArrayList<EccNOMORDAttributeSummary> getNOMORDAttributeDistributionDataByName( @PathVariable UUID   expID,
                                                                                          @RequestParam(value="attrName", defaultValue="") String attrName )
    {
        ArrayList<EccNOMORDAttributeSummary> result = new ArrayList<>();
        result.add( demoData.qoeSummaryDistribData.get( attrName ) );
        
        return result;
    }
    
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/attributes/distribution/qos" )
    @ResponseBody
    public EccINTRATSummary getINTRATAttributeDistributionDataByID( @PathVariable UUID                                         expID,
                                                                             @RequestParam(value="attrID", defaultValue="")    UUID attrID,
                                                                             @RequestParam(value="startTime", defaultValue="") long startTime,
                                                                             @RequestParam(value="endTime", defaultValue="")   long endTime )
    {
        return demoData.getINTRATDistData( attrID, startTime, endTime );
    }
    
    // Activity based queries --------------------------------------------------
    // -------------------------------------------------------------------------
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/participants/iri/activities" )
    @ResponseBody
    public EccParticipantActivityResultSet getParticipantActivities( @PathVariable UUID expID,
                                                                     @RequestParam(value="IRI", defaultValue = "") String IRI )
    {        
        return demoData.getActivitiesByParticipant( IRI );
    }
    
    // Application based queries -----------------------------------------------
    // -------------------------------------------------------------------------
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/activities/iri/applications" )
    @ResponseBody
    public EccActivityApplicationResultSet getActivityApplications( @PathVariable UUID expID,
                                                                    @RequestParam(value="IRI", defaultValue = "") String IRI )
    {        
        return demoData.getApplicationsByActivity( IRI );
    }
    
    // Application based queries -----------------------------------------------
    // -------------------------------------------------------------------------
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/applications/iri/services" )
    @ResponseBody
    public EccApplicationServiceResultSet getApplicationServices( @PathVariable UUID expID,
                                                                  @RequestParam(value="IRI", defaultValue = "") String IRI )
    {        
        return demoData.getServicesByApplication( IRI );
    }
    
    // Service based queries -----------------------------------------------
    // -------------------------------------------------------------------------
    @RequestMapping(method = RequestMethod.GET, value = "/{expID}/services/iri/attributes" )
    @ResponseBody
    public EccAttributeResultSet getServiceAttributes( @PathVariable UUID expID,
                                                       @RequestParam(value="IRI", defaultValue = "") String IRI )
    {
        return demoData.serviceQoSAttributes.get( IRI );
    }
    
}
