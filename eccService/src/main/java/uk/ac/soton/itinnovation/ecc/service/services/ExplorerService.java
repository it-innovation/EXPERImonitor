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

import java.io.IOException;
import uk.ac.soton.itinnovation.ecc.service.utils.MetricCalculator;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.metrics.*;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.provenance.*;
import uk.ac.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;
import uk.ac.soton.itinnovation.ecc.service.utils.*;

import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;

import uk.ac.soton.itinnovation.ecc.service.domain.explorer.distributions.*;

import org.springframework.stereotype.Service;

import javax.annotation.*;
import org.slf4j.*;

import java.util.*;
import uk.ac.soton.itinnovation.ecc.service.domain.explorer.EccExperimentSummary;




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

    // Called from controller [line 63]
    public EccExperimentSummary getExperimentPROVSummary( String expName,
                                                          String expDesc,
                                                          UUID   expID )
    {
        EccExperimentSummary result = null;
        
        // You should receive the experiment name, description and ID as parameters
        // for you to 'fill in' for the result. We need the provenance bits from the helper here
        
        
        return result;
    }
    
    // Called from controller [line 79]    
    public EccParticipantResultSet getParticipants( UUID expID )
    {
        EccParticipantResultSet result = new EccParticipantResultSet();

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

        return result;
    }
    
    // Called from controller [line 94]
    public EccParticipant getParticipant( UUID expID, String partIRI )
    {
        EccParticipant result = null;

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

        return result;
    }
    
    // Called from controller [line 270]
    public EccParticipantActivityResultSet getPartActivities( UUID   expID,
                                                              String partIRI )
    {
        EccParticipantActivityResultSet result = null;
        
        // See above strategy for create EccParticipant
        
        // Then stuff result full of EccActivity instances (don't worry about description if we don't have one)
        
        return result;
    }
    
    // Called from controller [line 286]
    public EccParticipantActivityResultSet getPartActivitiesByName( UUID   expID,
                                                                    String partIRI,
                                                                    String activityLabel )
    {
        EccParticipantActivityResultSet result = null;
        
        // Pretty much the same as the above method, only we are just retrieving those
        // activities with the label specified by activityLabel
        
        return result;
    }
    
    // Called from controller [line 304]
    public EccParticipantActivitySummaryResultSet getPartActivitySummary( UUID   expID,
                                                                          String partIRI )
    {
        EccParticipantActivitySummaryResultSet result = null;

        // See above strategy for create EccParticipant
		Entity metricEntity = metricsQueryHelper.getParticipantEntity(expID, partIRI);
		EccParticipant p = createParticipant(metricEntity);

        // Then stuff result full of EccActivity instances (don't worry about description if we don't have one)
		try {
			result = provenanceQueryHelper.getPartActivitySummary(expID, partIRI, p);
		} catch (Exception e) {
			logger.warn("Could not retrieve participants from PROV store", e);
		}

        return result;
    }
    
    // Call from controller [line 322]
    public EccActivityApplicationResultSet getActApplications( UUID   expID,
                                                               String activityIRI )
    {
        EccActivityApplicationResultSet result = null;
        
        // Here we need to find all the applications associated with a specific activity
        
        return result;
    }
    
    // Called from controller [line 340]
    public EccApplicationServiceResultSet getAppServices( UUID expID,
                                                          String applicationIRI )
    {
        EccApplicationServiceResultSet result = null;
        
        // Here we need to find all the services used by a particular application
        
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
			} catch (Exception e) {

				logger.error(callFail + " could not find experiment participants", e );
			}
		}

        return result;
    }

    public EccParticipantResultSet getPartQoEAttrSelection( UUID              expID,
                                                            String            attrName,
                                                            String            selLabel )
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

    public ArrayList<EccNOMORDStratifiedSummary> getPartQoEStratifiedSummary( UUID expID,
                                                                              ArrayList<String> allPartIRIs )
    {
        ArrayList<EccNOMORDStratifiedSummary> result = new ArrayList<>();

        // Safety
        if ( serviceReady && expID != null && allPartIRIs != null )
        {
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
            String keyPostLabel = " of " + stratifiedSamples.keySet().size();

            for ( int index : stratifiedSamples.keySet() )
            {
                // Create the index label
                String indexLabel = (index == -1) ? "NOMINAL"
                                                  // Add 1 to index to display as non-zero scale
                                                  : Integer.toString( index +1 ) + keyPostLabel;

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
    
    public EccINTRATSummary getINTRATAttrDistributionDiscreteSampling( UUID expID, UUID attrID,
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
}
