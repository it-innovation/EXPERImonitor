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

import uk.ac.soton.itinnovation.ecc.service.utils.MetricCalculator;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.EccExperimentSummary;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.metrics.*;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.provenance.*;
import uk.ac.soton.itinnovation.ecc.service.domain.*;
import uk.ac.soton.itinnovation.ecc.service.utils.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import uk.ac.soton.itinnovation.ecc.service.domain.explorer.distributions.*;

import org.springframework.stereotype.Service;

import javax.annotation.*;
import org.slf4j.*;
import java.io.IOException;
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
    private final String callFail = "Could not execute Explorer service: not ready or parameter(s) invalid";

    private ExplorerMetricsQueryHelper    metricsQueryHelper;
    private ExplorerProvenanceQueryHelper provenanceQueryHelper;

    private boolean serviceReady;

	private Properties props = new Properties();

    public ExplorerService()
    {
    }

    @PostConstruct
    public void init()
    {
		try {
			logger.info("Loading properties file");
			props.load(getClass().getClassLoader().getResourceAsStream("prov.properties"));
		} catch (IOException e) {
			logger.error("Error loading properties file", e);
		}

        metricsQueryHelper    = new ExplorerMetricsQueryHelper();
        provenanceQueryHelper = new ExplorerProvenanceQueryHelper(props);

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

    public EccExperimentSummary getExperimentPROVSummary( UUID expID )
    {
        EccExperimentSummary result = null;
        
        // Safety
        if ( serviceReady && expID != null )
        {
            try
            {
                Experiment experiment = metricsQueryHelper.getExperiment( expID );
                Properties provProps  = provenanceQueryHelper.getExperimentProvSummary( expID );
                
                if ( experiment != null && provProps != null && !provProps.isEmpty() )
                {
                    result = new EccExperimentSummary( experiment.getName(),
                                                       experiment.getDescription(),
                                                       expID.toString(),
                                                       Integer.parseInt( (String) provProps.get("participantCount") ) ,
                                                       Integer.parseInt( (String) provProps.get("activitiesPerformedCount") ) ,
                                                       Integer.parseInt( (String) provProps.get("applicationsUsedCount") ) ,
                                                       Integer.parseInt( (String) provProps.get("servicesUsedCount") ) );
                }
            }
            catch ( Exception ex )
            { logger.error( "Could not get provenance experiment summary", ex); }
        }
        else logger.error( callFail );
        
        return result;
    }
        
    public EccParticipantResultSet getParticipants( UUID expID )
    {
        EccParticipantResultSet result = new EccParticipantResultSet();
        
        if ( serviceReady && expID != null )
        {
            // 1. Get list of participant IRIs
            Set<String> participants = null;
            try {
                participants = provenanceQueryHelper.getParticipantIRIs(expID);
            } catch (Exception e) {
                logger.warn("Could not retrieve participants from PROV store", e);
            }

            // 2. For each IRI, use the metricsQueryHelper to get corresponding Metric Entity
            if (participants!=null) {
                for (String participant: participants) {

                    // 3. Create EccParticipant using createParticipant(..) method (below)
                    Entity metricEntity = metricsQueryHelper.getParticipantEntity(expID, participant);
                    EccParticipant p = createParticipant(metricEntity);
                     // 4. Add to collection
                    result.addParticipant(p);
                }
            }
        }
        else logger.error( callFail );
        
        return result;
    }
    
    public EccParticipant getParticipant( UUID expID, String partIRI )
    {
        EccParticipant result = null;
        
        if ( serviceReady && expID != null && partIRI != null )
        {
            // 1. Get list of participant IRIs
            Set<String> participants = null;
            try {
                participants = provenanceQueryHelper.getParticipantIRIs(expID);
            } catch (Exception e) {
                logger.warn("Could not retrieve participants from PROV store", e);
            }

            // 2. For each IRI, use the metricsQueryHelper to get corresponding Metric Entity
            if (participants!=null) {
                for (String participant: participants) {

                    if (partIRI.equals(participant)) {
                        // 3. Create EccParticipant using createParticipant(..) method (below)
                        Entity metricEntity = metricsQueryHelper.getParticipantEntity(expID, participant);
                        result = createParticipant(metricEntity);
                        break;
                    }
                }
            } 
        }
        else logger.error( callFail );
        
        return result;
    }
    

    public EccParticipantActivityResultSet getPartActivities( UUID   expID,
                                                              String partIRI )
    {
        EccParticipantActivityResultSet result = null;
        
        if ( serviceReady && expID != null && partIRI != null )
        {
            Entity metricEntity = metricsQueryHelper.getParticipantEntity(expID, partIRI);
            EccParticipant p = createParticipant(metricEntity);

            try {
                result = provenanceQueryHelper.getParticipantsActivityInstances(expID, partIRI, p);
            } catch (Exception e) {
                logger.warn("Could not retrieve activities from PROV store", e);
            }
        }
        else logger.error( callFail );

        return result;
    }
    
    // Called from controller [line 286]
    public EccParticipantActivityResultSet getPartActivitiesByName( UUID   expID,
                                                                    String partIRI,
                                                                    String activityLabel )
    {
        EccParticipantActivityResultSet result = null;
        
        if ( serviceReady && expID != null && partIRI != null && activityLabel != null )
        {
            Entity metricEntity = metricsQueryHelper.getParticipantEntity(expID, partIRI);
            EccParticipant p = createParticipant(metricEntity);

            try {
                result = provenanceQueryHelper.getParticipantActivityInstances(expID, partIRI, activityLabel, p);
            } catch (Exception e) {
                logger.warn("Could not retrieve activities from PROV store", e);
            }
        }
        else logger.error( callFail );
        
        return result;
    }
    
    // Called from controller [line 304]
    public EccParticipantActivitySummaryResultSet getPartActivitySummary( UUID   expID,
                                                                          String partIRI )
    {
        EccParticipantActivitySummaryResultSet result = null;
        
        if ( serviceReady && expID != null && partIRI != null )
        {
            // See above strategy for create EccParticipant
            Entity metricEntity = metricsQueryHelper.getParticipantEntity(expID, partIRI);
            EccParticipant p = createParticipant(metricEntity);

            // Then stuff result full of EccActivity instances (don't worry about description if we don't have one)
            try {
                result = provenanceQueryHelper.getPartActivitySummary(expID, partIRI, p);
            } catch (Exception e) {
                logger.warn("Could not retrieve participants from PROV store", e);
            }   
        }
        else logger.error( callFail );
        
        return result;
    }
    
    // Call from controller [line 322]
    public EccActivityApplicationResultSet getActApplications( UUID   expID,
                                                               String activityIRI )
    {
        EccActivityApplicationResultSet result = null;
        
        if ( serviceReady && expID != null && activityIRI != null )
        {
            try
            {
                result = provenanceQueryHelper.getApplicationsUsedByActivity( expID, activityIRI ); 
            }
            catch ( Exception ex )
            { logger.error( "Could not retrieve applications by activity", ex ); }
        }
        else logger.error( callFail );
        
        return result;
    }
    
    // Called from controller [line 340]
    public EccActivityServiceResultSet getActServices( UUID expID, String activityIRI )
    {
        EccActivityServiceResultSet result = null;
        
        if ( serviceReady && expID != null && activityIRI != null )
        {
            try
            {
                result = provenanceQueryHelper.getServicesUsedByActivity( expID, activityIRI ); 
            }
            catch ( Exception ex )
            { logger.error( "Could not retrieve applications by activity", ex ); }
        }
        else logger.error( callFail );
        
        return result;
    }
    
    public EccParticipantAttributeResultSet getPartCommonAttrResultSet( UUID expID )
    {
        EccParticipantAttributeResultSet result = new EccParticipantAttributeResultSet();

        // Safety
        if ( serviceReady && expID != null )
        {
			// Get all participant IRIs for experiment
			Set<String> allPartIRIs = null;

			try
			{
				allPartIRIs = provenanceQueryHelper.getParticipantIRIs( expID );

				// Get common Attributes
				Map<UUID,Attribute> commonAttributes = metricsQueryHelper.getPartCommonAttributes( expID, allPartIRIs );

				// Push each attribute into result appropriately (so long as it has a metric; it should do)
				List<Attribute> sortedAttrs = MetricHelper.sortAttributesByName( commonAttributes.values() );
				for ( Attribute attr : sortedAttrs )
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
			} catch (Exception e) {

				logger.error(callFail + " could not find experiment participants", e );
			}
		}

        return result;
    }
    
    public EccParticipantAttributeResultSet getPartAttrResultSet( UUID expID,
                                                                  String partIRI )
    {
        EccParticipantAttributeResultSet result = new EccParticipantAttributeResultSet();

        // Safety
        if ( serviceReady && expID != null && partIRI != null )
        {
			try
			{
				// Get common attributes for just one participant
                ArrayList<String> iriSet = new ArrayList<>();
                iriSet.add( partIRI );
                
				Map<UUID,Attribute> partAttributes = metricsQueryHelper.getPartCommonAttributes( expID, iriSet );

				// Push each attribute into result appropriately (so long as it has a metric; it should do)
				List<Attribute> sortedAttrs = MetricHelper.sortAttributesByName( partAttributes.values() );
				for ( Attribute attr : sortedAttrs )
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
            catch ( Exception ex )
            { logger.error(callFail + " could not find experiment participants", ex ); }
		}

        return result;
    }

    public EccParticipantResultSet getPartQoEAttrSelection( UUID   expID,
                                                            String attrName,
                                                            String selLabel )
    {
        EccParticipantResultSet result = new EccParticipantResultSet();

        // Safety
        if ( serviceReady && expID != null && attrName != null && selLabel != null )
        {
            // Get all participant IRIs first (get out early if nothing)
            Set<String> allPartIRIs = null;
            try
            {
                allPartIRIs = provenanceQueryHelper.getParticipantIRIs( expID );
            }
            catch ( Exception ex )
            { logger.error("Could not retrieve participants for experiment"); }
            
            // Get out early if we don't have any participants
            if ( allPartIRIs == null || allPartIRIs.isEmpty() ) return result;
            
            // Get all entities representing participants
            Map<UUID,Entity> entities = metricsQueryHelper.getParticipantEntities( expID, allPartIRIs );

            // Get all the attributes of the given name from the participants
            Map<UUID,Attribute> attrsByEntities = metricsQueryHelper.getAllEntityAttributes( entities.values() );

            // Select just those attributes with the target name
            List<Attribute> selAttributes = new ArrayList<>();

            for ( Attribute attr : attrsByEntities.values() )
                if ( attr.getName().equals(attrName) ) selAttributes.add( attr );
            
            // Sort selected (QoE) attributes by name
            selAttributes = extractSortedQoEAttributes( expID, selAttributes );
            
            // For select attributes, retrieve measurement set(s) and search for label instance & add Entities
            HashSet<Entity> selectedEntities = new HashSet<>();

            for ( Attribute attr : selAttributes )
            {
                Map<UUID,MeasurementSet> msets = 
                        metricsQueryHelper.getMeasurementSetsForAttribute( expID, attr.getUUID(), true );

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
                result.addParticipant( createParticipant(entity) );
        }
        else logger.error( callFail );

        return result;
    }

    public ArrayList<EccNOMORDAttributeSummary> getQoEDistributionByName( UUID   expID,
                                                                          String attrName )
    {
        // NOTE: it is only expected that ONE instance end up in this collection;
        // presented as a collection for the purposes of JSONising to web UI
        ArrayList<EccNOMORDAttributeSummary> result = new ArrayList<>();

        // Safety
         if ( serviceReady && expID != null && attrName != null )
         {
             // Get all attribute instances by name
             ArrayList<String> attrNames = new ArrayList<>();
             attrNames.add( attrName );

             Map<String,Set<Attribute>> allAttrs =
                     metricsQueryHelper.getAttributeInstancesByName( expID, attrNames );

             Set<Attribute> attrResult = allAttrs.get( attrName );

             if ( attrResult != null && !attrResult.isEmpty() )
             {
                 // Only use the QoE attributes
                 List<Attribute> qoeResult = extractSortedQoEAttributes( expID, attrResult );

                 // Get measurement set for combined attributes
                 MeasurementSet ms =
                         metricsQueryHelper.getCombinedMeasurementSetDataForAttributes( expID, qoeResult );

                 // If we have data, create the summary distribution
                 if ( ms != null )
                 {
                     Map<String, Integer> distrib =
                             MetricCalculator.countValueFrequencies( ms.getMeasurements() );

                     // Create info based on any attribute instance
                     EccAttributeInfo attrInfo =
                             createAttributeInfo( expID, attrResult.iterator().next() );

                     // Create summary distribution result
                     EccNOMORDAttributeSummary summary =
                             new EccNOMORDAttributeSummary( attrInfo, distrib );

                     result.add( summary );
                 }
                 else logger.error( "Could not find any measurement sets for attributes by name " + attrName );
             }
             else logger.warn( "Could not find any attributes by name " + attrName );

         }
         else logger.error( callFail );

         return result;
    }

    public EccNOMORDParticipantSummary getPartQoEDistribution( UUID expID,
                                                               String partIRI )
    {
        EccNOMORDParticipantSummary result = null;

        // Safety
        if ( serviceReady && expID != null && partIRI != null )
        {
            Entity entity = metricsQueryHelper.getParticipantEntity( expID, partIRI );
            if ( entity != null )
            {
                // Create participant info just from entity data
                result = new EccNOMORDParticipantSummary( createParticipant(entity) );

                // Sort attributes and then...
                List<Attribute> sortedAttrs = extractSortedQoEAttributes( expID, entity.getAttributes() );

                // For each (QoE) attribute, get the measurement set and get the median response in each case
                for ( Attribute attr : sortedAttrs )
                {
                    Map<UUID, MeasurementSet> srcSets =
                            metricsQueryHelper.getMeasurementSetsForAttribute( expID, attr.getUUID(), true );
                    try
                    {
                        MeasurementSet fullSet = MetricHelper.combineMeasurementSets( srcSets.values() );

                        if ( fullSet != null )
                        {
                            Metric metric = fullSet.getMetric();
                            MetricType mt = metric.getMetricType();

                            // Can only be NOMINAL or ORDINAL
                            if ( mt == MetricType.NOMINAL )
                            {
                                // Get most common
                                result.addNOMINALResponse( attr.getName(),
                                                           MetricCalculator.getMostFrequentValue(fullSet.getMeasurements()) );
                            }
                            else
                            {
                                // Get the median
                                float medianPos = MetricCalculator.calcORDINALMedianValuePosition( fullSet );
                                
                                if ( !Float.isNaN(medianPos) )
                                {
                                    String value = MetricHelper.getORDINALLabelFromIndex( metric, medianPos );
                                    result.addORDINALResponse( attr.getName(), value, (int) medianPos );
                                }
                            }
                        }
                        else logger.error( "Failed to combine measurement sets: no measurement sets available" );
                    }
                    catch ( Exception ex )
                    { logger.error( "Failed to combine measurement sets: " + ex.getMessage() ); }
                }
            }
            else logger.warn( "Could not find Entity in metric database based on IRI: " + partIRI );

        } else logger.error( callFail );

        return result;
    }

    public ArrayList<EccNOMORDStratifiedSummary> getPartQoEStratifiedSummary( UUID expID )
    {
        ArrayList<EccNOMORDStratifiedSummary> result = new ArrayList<>();

        // Safety
        if ( serviceReady && expID != null )
        {
            // Try find participants for summary
            Set<String> allPartIRIs = null;
            try
            {
                allPartIRIs = provenanceQueryHelper.getParticipantIRIs( expID );
            }
            catch ( Exception ex )
            { logger.warn( "Could not find any participants for experiment" ); }
            
            // Duck out early if we can't find any participants
            if ( allPartIRIs == null || allPartIRIs.isEmpty() ) return result;
            
            // Get common attributes for participants
            Map<UUID, Attribute> commonAttributes = metricsQueryHelper.getPartCommonAttributes( expID, allPartIRIs );

            // Sort out the QoE attributes
            List<Attribute> sortedAttrs = extractSortedQoEAttributes( expID, commonAttributes.values() );

            // Make a list of attribute names...
            ArrayList<String> attrNames = new ArrayList<>();
            for ( Attribute attr : sortedAttrs )
                attrNames.add( attr.getName() );

            // ... and get the actual attribute instances
            Map<String,Set<Attribute>> attrInstances =
                    metricsQueryHelper.getAttributeInstancesByName( expID, attrNames );

            // For each attribute name, combine measurement sets from all attributes
            HashMap<String, MeasurementSet> superMSSet = new HashMap<>();

            for ( String attrName : attrInstances.keySet() )
            {
                Set<Attribute> instances = attrInstances.get( attrName );

                MeasurementSet allMeasurements =
                        metricsQueryHelper.getCombinedMeasurementSetDataForAttributes( expID, instances );

                superMSSet.put( attrName, allMeasurements );
            }

            // Now get a frequency distribution for each attribute's complete measurement set
            HashMap<String,Map<String,Integer>> attrDistrMap = new HashMap<>();

            for ( String attrName : superMSSet.keySet() )
            {
                MeasurementSet ms = superMSSet.get( attrName );

                Map<String,Integer> freqMap
                        = MetricCalculator.countValueFrequencies( ms.getMeasurements() );

                attrDistrMap.put( attrName, freqMap );
            }

            // Summarise distributions by indexed order
            HashMap<Integer, TreeMap<String, Integer>> stratifiedSamples = new HashMap<>();

            for ( String attrName : attrDistrMap.keySet() )
            {
                // Get associated metric for attribute
                Metric metric = superMSSet.get( attrName ).getMetric();

                // Get distribution of each item for attribute
                Map<String, Integer> attrValueDistr = attrDistrMap.get( attrName );

                for ( String indLabel : attrValueDistr.keySet() )
                {
                    // Convert label to index (an index of -1 means NOMINAL data)
                    int index = MetricHelper.getORDINALIndexFromLabel( metric, indLabel );

                    TreeMap<String,Integer> stratDistr = null;

                    // Get entry for specific index if it exists..
                    if ( stratifiedSamples.containsKey(index) )
                        stratDistr = stratifiedSamples.get(index);
                    else
                    // ... or create one if it does not exist
                    {
                        stratDistr = new TreeMap<>();
                        stratifiedSamples.put( index, stratDistr );
                    }

                    // Add count for this attribute's index
                    stratDistr.put( attrName, attrValueDistr.get(indLabel) );
                }
            }

            // Finally, convert to domain data objects
            for ( int index : stratifiedSamples.keySet() )
            {
                // Create the index label
                String indexLabel = (index == -1) ? "NOMINAL"
                                                  // Add 1 to index to display as non-zero scale
                                                  : "Scale index: " + Integer.toString( index +1 );
                // Create summary
                EccNOMORDStratifiedSummary ss =
                        new EccNOMORDStratifiedSummary( indexLabel );

                // Create summary items
                TreeMap<String, Integer> stratAttrs = stratifiedSamples.get( index );

                for ( String attrName : stratAttrs.keySet() )
                {
                    EccItemCount eic = new EccItemCount( attrName,
                                                        stratAttrs.get(attrName) );

                    ss.addStratifiedItem( eic );
                }

                result.add( ss );
            }
        }
        else logger.error( callFail );
        
        return result;
    }
    
    public EccINTRATSummary getINTRATAttrDistribution( UUID expID,
                                                       UUID attrID )
    {
        EccINTRATSummary result = null;
        
        if ( serviceReady && expID != null && attrID != null )
        {
            try
            {
                // Only operate on INTERVAL or RATIO data
                if ( metricsQueryHelper.isQoSAttribute(expID, attrID) )
                {
                    Attribute attr = metricsQueryHelper.getAttribute( expID, attrID );
                    
                    Map<UUID,MeasurementSet> msSet = 
                            metricsQueryHelper.getMeasurementSetsForAttribute( expID, attrID, true );

                    if ( attr != null && msSet != null )
                    {
                        // Combine data
                        MeasurementSet superMS = MetricHelper.combineMeasurementSets( msSet.values() );
                        
                        // Create summary
                        result = createINTRATSummary( attr, superMS );
                    }
                }
            }
            catch ( Exception ex )
            { logger.error( "Could not create QoS Summary for attribute", ex ); }
        }
        else logger.error( callFail );
        
        return result;
    }
    
    public EccINTRATSummary getINTRATAttrDistributionDiscreteSampling( UUID             expID, 
                                                                       UUID             attrID,
                                                                       Collection<Date> timeStamps )
    {
        EccINTRATSummary result = null;
        
        if ( serviceReady && expID != null && timeStamps != null && 
             !timeStamps.isEmpty() )
        {
            try
            {
                // Only operate on INTERVAL or RATIO data
                if ( metricsQueryHelper.isQoSAttribute(expID, attrID) )
                {
                    Map<UUID,MeasurementSet> msSet = 
                            metricsQueryHelper.getMeasurementSetsForAttribute( expID, attrID, true );

                    // Combine data
                    MeasurementSet superMS = MetricHelper.combineMeasurementSets( msSet.values() );
                    
                    // Get back the discrete measurements nearest our timestamps
                    superMS = MetricCalculator.findNearestMeasurements( superMS, timeStamps );
                    
                    // Get related attribute
                    Attribute attr = metricsQueryHelper.getAttribute( expID, attrID );
 
                    if ( superMS != null && attr != null )
                    {
                        Properties calcResult = MetricCalculator.calcINTRATSummary( superMS );
                        
                        // Create summary if possible
                        if ( !calcResult.isEmpty() )
                        {
                            double floor = (Double) calcResult.get( "floor" );
                            double mean  = (Double) calcResult.get( "mean" );
                            double ceil  = (Double) calcResult.get( "ceiling" );
                            
                            result = new EccINTRATSummary( createAttributeInfo(expID, attr),
                                                           floor, mean, ceil );
                        }
                    }
                }
            }
            catch ( Exception ex )
            {
                String msg = "Could not get discrete distribution for QoS data: " + ex.getMessage();
                logger.error( msg, ex );
            }
        }
        else logger.error( callFail );
        
        return result;
    }

    public EccINTRATSeriesSet getINTRATAttrSeriesHilitePartActivites( UUID   expID,
                                                                      UUID   attrID,
                                                                      String partIRI,
                                                                      String actLabel )
    {
        EccINTRATSeriesSet result = new EccINTRATSeriesSet();
        
        if ( expID != null && attrID != null && partIRI != null && actLabel != null )
        {
            try 
            {
                // Create domain series for attribute and to result
                EccINTRATSeries attrSeries = createDomainSeries( expID, attrID, true );
                
                if ( attrSeries != null ) result.addSeries( attrSeries );
                
                // Now try get participant activities based on label
                Entity ent = metricsQueryHelper.getParticipantEntity( expID, partIRI );
                EccParticipant part = createParticipant( ent );
                ArrayList<EccActivity> partActs = null;
                
                // If we are good, then create a sub-set series based on the attribute data
                if ( part != null && attrSeries != null )
                {
                    EccParticipantActivityResultSet pars = 
                            provenanceQueryHelper.getParticipantActivityInstances( expID, 
                                                                                   partIRI, 
                                                                                   actLabel, 
                                                                                   part );
                    partActs = pars.getActivities();
                    
                    // Copy attribute series and 'turn off' data not relavant to activities
                    EccINTRATSeries subSeries = copySeries( part.getName() + ": " + actLabel, 
                                                            false, attrSeries );
                    
                    ArrayList<EccMeasurement> subMeasures = subSeries.getValues();
                    
                    // 'Turn off' not relevant measurements (use two sample interval
                    // to catch 'single date stamp' activity measurements
                    for ( int i = 0; i < subMeasures.size()-2; i++ )
                    {
                        Date m1Stamp = subMeasures.get( i ).getTimestamp();
                        Date m2Stamp = subMeasures.get( i + 1 ).getTimestamp();
                        
                        boolean switchOff = true;
                        for ( EccActivity act : partActs )
                        {
                            Date actStart = act.getStartTime();
                            Date actEnd   = act.getEndTime();
                            
                            if ( (actStart.equals(m1Stamp) || actStart.after(m1Stamp)) &&
                                 (actEnd.equals(m2Stamp)   || actEnd.before(m2Stamp)) )
                            {
                                switchOff = false;
                                break;
                            }
                        }
                        
                        // Switch 'off' measurement
                        if ( switchOff ) subMeasures.get( i ).setValue( null );
                    }
                    
                    // Add sub-set to result
                    result.addSeries( subSeries );
                }
            }
            catch ( Exception ex )
            { logger.error( "Could not create attribute series highlight", ex ); }
        }
        return result;        
    }
    
    public EccAttributeResultSet getProvenanceAttributeSet( UUID   expID,
                                                            String IRI )
    {
        EccAttributeResultSet result = new EccAttributeResultSet();
        
        if ( serviceReady && expID != null && IRI != null )
        {
            // Try find entity, then return attributes
            Entity entity = metricsQueryHelper.getParticipantEntity( expID, IRI );
            
            if ( entity != null )
            {
                for ( Attribute attr : entity.getAttributes() )
                {
                    Metric metric = metricsQueryHelper.getAttributeMetric( expID, attr.getUUID() );

                    if ( metric != null )
                    {
                        EccAttributeInfo info = 
                                new EccAttributeInfo( attr.getName(),
                                                      attr.getDescription(),
                                                      attr.getUUID(),
                                                      metric.getUnit().getName(),
                                                      metric.getMetricType().name(),
                                                      metric.getMetaType(),
                                                      metric.getMetaContent() );

                        result.addAttributeInfo( info );
                    }
                }
            }
        }
        else logger.error( callFail );
        
        return result;
    }
    
    public EccParallelSet getParallelSet( UUID expID )
    {
        EccParallelSet result = new EccParallelSet();
        
        if ( serviceReady && expID != null )
        {
            try
            {
                // Get all participants for this experiment
                Set<String> partIRIs = provenanceQueryHelper.getParticipantIRIs( expID );
                if ( partIRIs != null && !partIRIs.isEmpty() )
                {
                    // Create tuples (for participants known to metric database)
                    HashMap<String, EccParallelSetData> partTuples = new HashMap<>();   
                    for ( String iri : partIRIs )
                    {
                        Entity ent = metricsQueryHelper.getParticipantEntity( expID, iri );
                        EccParticipant part = createParticipant( ent );
                        
                        if ( part != null )
                        {
                            EccParallelSetData psd 
                                = new EccParallelSetData( expID,
                                                          provenanceQueryHelper,
                                                          metricsQueryHelper,
                                                          part );
                        
                            if ( psd.createActivitySet() )
                                if ( psd.createApplicationSet() )
                                    if ( psd.createServiceSet() );
                        }
                    }
                }
            }
            catch ( Exception ex )
            { logger.error( "Could not create parallel set", ex ); }
        } else logger.error( callFail );
        
        return result;
    }
    
    // Private methods ---------------------------------------------------------
    private EccParticipant createParticipant( Entity ent )
    {
        EccParticipant part = null;
        
        if ( ent != null )
            part = new EccParticipant( ent.getName(),
                                       ent.getDescription(),
                                       ent.getUUID(),
                                       ent.getEntityID() );
        
        return part;
    }

    private EccAttributeInfo createAttributeInfo( UUID expID, Attribute attr )
    {
        EccAttributeInfo result = null;
        
        if ( expID != null && attr != null )
        {
            Metric metric = metricsQueryHelper.getAttributeMetric( expID, attr.getUUID() );

            if ( metric != null )
                result = new EccAttributeInfo( attr.getName(),
                                               attr.getDescription(),
                                               attr.getUUID(),
                                               metric.getUnit().getName(),
                                               metric.getMetricType().name(),
                                               metric.getMetaType(),
                                               metric.getMetaContent() );
            else
                logger.error( "Could not create Attribute Info: metric is unavailable" );   
        }
        
        return result;
    }
    
    private EccAttributeInfo createAttributeInfo( Attribute attr, MeasurementSet ms )
    {
        EccAttributeInfo result = null;
        
        if ( attr != null && ms != null )
        {
            Metric metric = ms.getMetric();
            
            result = new EccAttributeInfo( attr.getName(),
                                           attr.getDescription(),
                                           attr.getUUID(),
                                           metric.getUnit().getName(),
                                           metric.getMetricType().name(),
                                           metric.getMetaType(),
                                           metric.getMetaContent() );
        }
        
        return result;
    }

    private List<Attribute> extractSortedQoEAttributes( UUID expID, Collection<Attribute> attrs )
    {
        HashSet<Attribute> qoeAttrs = new HashSet<>();
        
        for ( Attribute attr : attrs )
        {
            try
            {
                if ( metricsQueryHelper.isQoEAttribute(expID, attr.getUUID()) )
                    qoeAttrs.add( attr );
            }
            catch ( Exception ex )
            { logger.error( "Could not extract QoE attribute", ex);}
        }

        return MetricHelper.sortAttributesByName( qoeAttrs );
    }
    
    private ArrayList<EccMeasurement> createDomainMeasurements( MeasurementSet ms )
    {
        ArrayList<EccMeasurement> result = new ArrayList<>();
        
        // Sort the measurements first
        List<Measurement> sorted = MetricHelper.sortMeasurementsByDateLinearReverse( ms.getMeasurements() );
        
        for ( Measurement m : sorted )
            result.add( new EccMeasurement( m.getTimeStamp(), m.getValue() ));
        
        return result;
    }
    
    private EccINTRATSummary createINTRATSummary( Attribute attr, MeasurementSet ms )
    {
        EccINTRATSummary result = null;
        
        if ( attr != null && ms != null )
        {
            try
            {
                // Check we are dealing with QoS like data
                Metric metric = ms.getMetric();
                MetricType mt = metric.getMetricType();

                if ( mt == MetricType.INTERVAL || mt == MetricType.RATIO )
                {
                    Properties calcResult = MetricCalculator.calcINTRATSummary( ms );

                    // Create summary if possible
                    if ( !calcResult.isEmpty() )
                    {
                        double floor = (Double) calcResult.get( "floor" );
                        double mean  = (Double) calcResult.get( "mean" );
                        double ceil  = (Double) calcResult.get( "ceiling" );

                        result = new EccINTRATSummary( createAttributeInfo(attr,ms),
                                                       floor, mean, ceil );
                    }
                }
                else logger.error( "Could not create INTRAT summary: metric type incorrect" );
            }  
            catch ( Exception ex )
            { logger.error("Could not create INTRAT summary", ex); }
        }
        
        return result;
    }
    
    private EccINTRATSeries createDomainSeries( UUID expID, UUID attrID, boolean enabled )
    {
        EccINTRATSeries result         = null;
        Attribute attr                 = metricsQueryHelper.getAttribute( expID, attrID );
        Map<UUID,MeasurementSet> mSets = metricsQueryHelper.getMeasurementSetsForAttribute( expID, attrID, true );
        
        try
        {
            // Get measurements
            MeasurementSet targMS = MetricHelper.combineMeasurementSets( mSets.values() );
            
            if ( attr != null && targMS != null )
            result = new EccINTRATSeries( attr.getName(), 
                                          !enabled, // flag is actually disabled :(
                                          createDomainMeasurements( targMS ) );
        }
        catch ( Exception ex )
        { logger.error( "Could not create domain data series for attribute", ex ); }
        
        return result;
    }
    
    private EccINTRATSeries copySeries( String key, 
                                        boolean enabled, 
                                        EccINTRATSeries srcSeries )
    {
        ArrayList<EccMeasurement> targMeasures = new ArrayList<>();
        ArrayList<EccMeasurement> srcMeasures  = srcSeries.getValues();
        
        for ( EccMeasurement srcM : srcMeasures )
            targMeasures.add( new EccMeasurement(srcM) );
        
        return new EccINTRATSeries( key, !enabled, targMeasures );
    }
}
