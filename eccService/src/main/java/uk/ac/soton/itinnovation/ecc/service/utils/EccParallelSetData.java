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
//      Created Date :          15-Aug-2014
//      Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////

package uk.ac.soton.itinnovation.ecc.service.utils;

import uk.ac.soton.itinnovation.ecc.service.domain.explorer.provenance.*;

import java.util.*;
import org.slf4j.*;




public class EccParallelSetData
{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private UUID                          experimentID;
    private ExplorerProvenanceQueryHelper provHelper;
    private ExplorerMetricsQueryHelper    metricHelper;
    private EccParticipant                participant;
    
    private HashMap<String, HashSet<EccActivity>>    activitiesByLabel = new HashMap<>();
    private HashMap<String, HashSet<EccApplication>> appsByLabel       = new HashMap<>();
    private HashMap<String, String>                  actAppsMap        = new HashMap<>();
    private HashMap<String, EccService>              servsByIRI        = new HashMap<>();
    private HashMap<String, String>                  appServsMap       = new HashMap<>();
    

    public EccParallelSetData( UUID                          expID,
                               ExplorerProvenanceQueryHelper pqh,
                               ExplorerMetricsQueryHelper    mqh,
                               EccParticipant                part )
    {
        experimentID = expID;
        provHelper   = pqh;
        metricHelper = mqh;
        participant  = part;
    }
    
    public boolean createActivitySet()
    {
        try
        {
            EccParticipantActivityResultSet ars 
                = provHelper.getParticipantsActivityInstances( experimentID, 
                                                               participant.getIRI(),
                                                               participant );
            for ( EccActivity act: ars.getActivities() )
            {
                String actLabel = act.getName();
                
                if ( !activitiesByLabel.containsKey(actLabel) )
                {
                    HashSet<EccActivity> actInsts = activitiesByLabel.get( actLabel );
                    actInsts.add( act );
                }
                else
                {
                    HashSet<EccActivity> actInsts = new HashSet<>();
                    actInsts.add( act );
                    
                    activitiesByLabel.put( actLabel, actInsts );
                }
            }
            
            return true;
        }
        catch ( Exception ex )
        { logger.error( "Could not create parallel set activity set", ex ); }
        
        return false;
    }
    
    public boolean createApplicationSet()
    {
        try
        {
            for ( String actLabel : activitiesByLabel.keySet() )
            {
                HashSet<EccActivity> acts = activitiesByLabel.get( actLabel );
                for ( EccActivity act : acts )
                {
                    EccActivityApplicationResultSet ars =
                            provHelper.getApplicationsUsedByActivity( experimentID, act.getIRI() );
                    
                    if ( ars != null )
                    {
                        for (EccApplication app : ars.getApplications() )
                        {
                            String appName = app.getName();

                            // Map applications by label
                            if ( appsByLabel.containsKey(appName) )
                            {
                                HashSet<EccApplication> apps = appsByLabel.get( appName );
                                apps.add( app );
                            }
                            else
                            {
                                HashSet<EccApplication> apps = new HashSet<>();
                                apps.add( app );

                                appsByLabel.put( appName, apps );
                            }

                            // Map activity x application labels
                            actAppsMap.put( actLabel, appName );
                        }
                    } 
                }
            }
            
            return true;
        }
        catch ( Exception ex )
        { logger.error( "Could not create parallel set activity set", ex ); }
        
        return false;
    }
    
    public boolean createServiceSet()
    {
        try
        {
            for ( String appLabel : appsByLabel.keySet() )
            {
                // Get all app instances for the label and query services
                HashSet<EccApplication> appInsts = appsByLabel.get( appLabel );
                
                for ( EccApplication app : appInsts )
                {
                    EccApplicationServiceResultSet srs =
                            provHelper.getServicesUsedByApplication( experimentID, app.getIRI() );
                    
                    if ( srs != null )
                    {
                        // Map services by IRI
                        for ( EccService serv : srs.getServices() )
                            servsByIRI.put( serv.getIRI(), serv );
                        
                        // Map app labels to service IRIs
                        
                        
                    }
                }
            }
        }
        catch ( Exception ex )
        { logger.error( "Could not create parallel set service set", ex ); }
        
        return false;
    }
}
