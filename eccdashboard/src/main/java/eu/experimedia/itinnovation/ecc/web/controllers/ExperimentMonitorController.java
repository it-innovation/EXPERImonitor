/////////////////////////////////////////////////////////////////////////
//
// ¬© University of Southampton IT Innovation Centre, 2012
//
// Copyright in this library belongs to the University of Southampton
// University Road, Highfield, Southampton, UK, SO17 1BJ
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
//	Created Date :			2012-09-03
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////

package eu.experimedia.itinnovation.ecc.web.controllers;

import eu.experimedia.itinnovation.ecc.web.data.*;
import eu.experimedia.itinnovation.ecc.web.services.ExperimentMonitorService;
import java.text.SimpleDateFormat;
import java.util.*;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;

@Controller
@RequestMapping("/em")
public class ExperimentMonitorController {

    private static final Logger logger = Logger.getLogger(ExperimentMonitorController.class);

    @Autowired
    @Qualifier("experimentMonitorService")
    ExperimentMonitorService emService;
    
//    protected SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    

    
    @RequestMapping(method = RequestMethod.GET, value = "/getclients/do.json")
    public @ResponseBody EMClientAsJson[] getConnectedClients() throws Throwable {
        logger.debug("Returning list of connected clients");
        try {
            EMClient[] clients = emService.getAllConnectedClients();
            int numClients = clients.length;

            EMClientAsJson tempClient = new EMClientAsJson();
            EMClientAsJson[] resultingListOfClients = new EMClientAsJson[numClients];

            int i = 0;
            for (EMClient e : clients) {
                tempClient.setUuid(e.getID().toString());
                tempClient.setName(e.getName());
                resultingListOfClients[i] = tempClient;

                tempClient = new EMClientAsJson();
                i++;
            }

            return resultingListOfClients;
        } catch (Throwable ex) {
            logger.error("Failed to return a list of connected clients");
            ex.printStackTrace();
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getcurrentphaseclients/do.json")
    public @ResponseBody EMClientAsJson[] getCurrentPhaseClients() throws Throwable {
        logger.debug("Returning list of connected clients");
        try {
            EMClient[] clients = emService.getCurrentPhaseClients();
            int numClients = clients.length;

            EMClientAsJson tempClient = new EMClientAsJson();
            EMClientAsJson[] resultingListOfClients = new EMClientAsJson[numClients];

            int i = 0; Iterator it; EMPhase phase; Set<String> phases = new HashSet<String>();
            for (EMClient e : clients) {
                tempClient.setUuid(e.getID().toString());
                tempClient.setName(e.getName());
                
                // Extra: get supported phases
                it = e.getCopyOfSupportedPhases().iterator();
                while(it.hasNext()) {
                    phase = (EMPhase) it.next();
                    phases.add("[" + phase.getIndex() + "] " + phase.name());
                }
                
                tempClient.setSupportedPhases(phases.toArray(new String[0]));
                
                resultingListOfClients[i] = tempClient;

                tempClient = new EMClientAsJson();
                i++;
                phases.clear();
            }

            return resultingListOfClients;
        } catch (Throwable ex) {
            logger.error("Failed to return a list of connected clients");
            ex.printStackTrace();
            return null;
        }
    }
    
    /**
     * Returns a list of metric generators for clients in the current phase only
     * 
     * For Live Metric View
     * 
     * @throws Throwable 
     */
    @RequestMapping(method = RequestMethod.GET, value = "/getmetricgenerators/do.json")
    public @ResponseBody MetricGeneratorAsJson[] getMetricGenerators() throws Throwable {
        
        // Should be simpler - just return a list of metric generators with minimum data
        // Separate method should load selected metric generator!
        
        logger.debug("Returning list of metric generators for current phase");
        try {
            
            // Faster to get them from the database at this point?
            // Ping clients to see if they are alive?
            EMClient[] clients = emService.getCurrentPhaseClients();
            
            
            if (clients.length < 1) {
                logger.error("No clients currently connected, method problably called by mistake in a wrong phase.");
                return new MetricGeneratorAsJson[0];
            } else {
                logger.debug("Returning metric generators for " + clients.length + " client(s)");
                
                Set<MetricGeneratorAsJson> resultingMgSet = new HashSet<MetricGeneratorAsJson>();
                
                MetricGeneratorAsJson tempMetricGeneratorAsJson = new MetricGeneratorAsJson();
                String name, uuid, desc;
                EntityAsJson tempEntityAsJson = new EntityAsJson();
                MetricGroupAsJson tempMetricGroupAsJson = new MetricGroupAsJson();
                MeasurementSetAsJson tempMeasurementSetAsJson = new MeasurementSetAsJson();
                Set<EntityAsJson> tempEntityAsJsonSet = new HashSet<EntityAsJson>();
                Set<MetricGroupAsJson> tempMetricGroupAsJsonSet = new HashSet<MetricGroupAsJson>();
                Set<MeasurementSetAsJson> tempMeasurementSetAsJsonSet = new HashSet<MeasurementSetAsJson>();
                
                Set<Attribute> allAttributes = new HashSet<Attribute>();
                for (EMClient client : clients) {
                    
                    for (MetricGenerator metricGenerator : client.getCopyOfMetricGenerators()) {
                        
                        uuid = metricGenerator.getUUID().toString();
                        name = metricGenerator.getName();
                        desc = metricGenerator.getDescription();
                        
                        // Entities
                        for (Entity entity : metricGenerator.getEntities()) {
                            
                            tempEntityAsJson.setUuid(entity.getUUID().toString());
                            tempEntityAsJson.setName(entity.getName());
                            tempEntityAsJson.setDescription(entity.getDescription());
                            
                            tempEntityAsJsonSet.add(tempEntityAsJson);
                            tempEntityAsJson = new EntityAsJson();
                            
                            allAttributes.addAll(entity.getAttributes());
                        }
                        
                        // Metric groups
                        for (MetricGroup metricGroup : metricGenerator.getMetricGroups()) {
                            tempMetricGroupAsJson.setUuid(metricGroup.getUUID().toString());
                            tempMetricGroupAsJson.setName(metricGroup.getName());
                            tempMetricGroupAsJson.setDescription(metricGroup.getDescription());
                            
                            for (MeasurementSet measurementSet : metricGroup.getMeasurementSets()) {
                                
                                tempMeasurementSetAsJson.setUuid(measurementSet.getUUID().toString());
                                tempMeasurementSetAsJson.setAttribute(this.getAttributeNameForMeasurementSet(measurementSet, allAttributes.toArray(new Attribute[0])));
//                                tempMeasurementSetAsJson.setAttribute(measurementSet.getAttributeUUID().toString());
                                tempMeasurementSetAsJson.setMetricUUID(measurementSet.getMetric().getUUID().toString());
                                tempMeasurementSetAsJson.setMetricType(measurementSet.getMetric().getMetricType().name());
                                tempMeasurementSetAsJson.setMetricUnit(measurementSet.getMetric().getUnit().getName());
                                        
                                tempMeasurementSetAsJsonSet.add(tempMeasurementSetAsJson);
                                tempMeasurementSetAsJson = new MeasurementSetAsJson();
                            }
                            
                            tempMetricGroupAsJson.setMeasurementSets(tempMeasurementSetAsJsonSet.toArray(new MeasurementSetAsJson[0]));
                            
                            tempMetricGroupAsJsonSet.add(tempMetricGroupAsJson);
                            
                            tempMeasurementSetAsJsonSet = new HashSet<MeasurementSetAsJson>();
                            tempMetricGroupAsJson = new MetricGroupAsJson();
                        }
                        
                        logger.debug("Found metric generator: [" + uuid + "] " + name + " (" + desc + ") with " + tempEntityAsJsonSet.size() + " entity(ies) and " + tempMetricGroupAsJsonSet.size() + " metric group(s)");
                        
                        tempMetricGeneratorAsJson.setUuid(uuid);
                        tempMetricGeneratorAsJson.setName(name);
                        tempMetricGeneratorAsJson.setDescription(desc);
                        tempMetricGeneratorAsJson.setListOfEntities(tempEntityAsJsonSet.toArray(new EntityAsJson[0]));
                        tempMetricGeneratorAsJson.setListOfMetricGroups(tempMetricGroupAsJsonSet.toArray(new MetricGroupAsJson[0]));
                        
                        resultingMgSet.add(tempMetricGeneratorAsJson);
                        
                        allAttributes = new HashSet<Attribute>();
                        tempEntityAsJsonSet = new HashSet<EntityAsJson>();
                        tempMetricGroupAsJsonSet = new HashSet<MetricGroupAsJson>();
                        tempMetricGeneratorAsJson = new MetricGeneratorAsJson();
                    }
                }
                
                return resultingMgSet.toArray(new MetricGeneratorAsJson[0]);
            }

        } catch (Throwable ex) {
            logger.error("Failed to return a list of metric generators");
            ex.printStackTrace();
            return null;
        }
    }  
    
    private String getAttributeNameForMeasurementSet(MeasurementSet measurementSet, Attribute[] allAttributes) {
        String attributeSetUUID = measurementSet.getAttributeUUID().toString();
        String attributeName = "unknown attribute";
        
        for (Attribute attribute : allAttributes) {
            if (attribute.getUUID().toString().equals(attributeSetUUID)) {
                attributeName = attribute.getName();
                break;
            }
        }
        
        return attributeName;
    }    

    @RequestMapping(method = RequestMethod.GET, value = "/getcurrentphase/do.json")
    public @ResponseBody EMPhaseAsJson getCurrentPhase() throws Throwable {
        logger.debug("Returning current experiment phase");
        try {

            EMPhase currentPhase = emService.getCurrentPhase();
            if (currentPhase == null) {
                logger.error("Current phase is NULL");
                return null;
            } else {
                logger.debug("Current phase is: " + currentPhase.toString() + " [" + currentPhase.getIndex() + "]");
                return new EMPhaseAsJson(currentPhase.getIndex(), currentPhase.toString());
            }
        } catch (Throwable ex) {
            logger.error("Failed to return current experiment phase");
            ex.printStackTrace();
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/startlifecycle/do.json")
    public @ResponseBody EMPhaseAsJson startLifeCycle() throws Throwable {
        logger.debug("Starting experiment lifecycle");
        try {
            EMPhase currentPhase = emService.startLifeCycle();

            return new EMPhaseAsJson(currentPhase.getIndex(), currentPhase.toString());
        } catch (Throwable ex) {
            logger.error("Failed to start experiment lifecycle");
            ex.printStackTrace();
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.GET, value = "/gotonextphase/do.json")
    public @ResponseBody EMPhaseAsJson goToNextPhase() throws Throwable {
        logger.debug("Going to the next phase");
        try {
            emService.goToNextPhase();
            EMPhase currentPhase = emService.getCurrentPhase();
            if (currentPhase == null) {
                logger.error("The next phase is NULL");
                return null;
            } else {
                logger.debug("The next phase is: " + currentPhase.toString() + " [" + currentPhase.getIndex() + "]");
                return new EMPhaseAsJson(currentPhase.getIndex(), currentPhase.toString());
            }
        } catch (Throwable ex) {
            logger.error("Failed to get to the next phase");
            ex.printStackTrace();
            return null;
        }
    }

    
    @RequestMapping(method = RequestMethod.POST, value = "/getmeasurementsformeasurementset/do.json")
    public @ResponseBody DataPoint[] getMeasurementsForMeasurementSet(@RequestBody final String inputData) throws Throwable {
        
        logger.debug("Returning data for a measurement set. Input data: " + inputData);
        
        try {
            
            JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(inputData);
            String measurementSetUuid = inputDataAsJSON.getString("measurementSetUuid");
            
            // The data has to come out sorted by time, hence the TreeMap
            Map<Date, String> data = new TreeMap<Date, String>(emService.getMeasurementsForMeasurementSet(measurementSetUuid));
            logger.debug("Measurement set [" + measurementSetUuid + "] has " + data.size() + " data point(s)");
            
            DataPoint[] result = new DataPoint[data.keySet().size()];
            
            Iterator it = data.keySet().iterator();
            Date time; long timeAsLong; String value;
            int counter = 0;
            while(it.hasNext()) {
                time = (Date) it.next();                
                value = data.get(time);
                timeAsLong = time.getTime();
                result[counter] = new DataPoint(timeAsLong, value);
                counter++;
            }

            return result;
        } catch (Throwable ex) {
            logger.error("Failed to return data for a measurement set.");
            ex.printStackTrace();
            return null;
        }        
    }    

    @RequestMapping(method = RequestMethod.POST, value = "/getmeasurementsetsforclient/do.json")
    public @ResponseBody MeasurementSetAsJson[] getMeasurementSetsForClient(@RequestBody final String inputData) throws Throwable {
        logger.debug("Getting measurement sets for a client. Input data: " + inputData);
        try {
            JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(inputData);
            String clientUUID = inputDataAsJSON.getString("clientUUID");

            Set<MeasurementSetAsJson> resultingMeasurementSets = new HashSet<MeasurementSetAsJson>();

            MeasurementSet[] measurementSets = emService.getMeasurementSetsForClient(clientUUID);

            if (measurementSets == null) {
                logger.debug("Client " + clientUUID + " does not have any measurement sets");
                return null;
            } else {
                MeasurementSetAsJson tempMeasurementSetAsJson = new MeasurementSetAsJson();

                for (MeasurementSet ms : measurementSets) {
//                    tempMeasurementSetAsJson.setUuid(inputData);

                    tempMeasurementSetAsJson.setUuid(ms.getUUID().toString());
//                    tempMeasurementSetAsJson.setAttribute(this.getAttributeNameForMeasurementSet(ms, allAttributes));
                    tempMeasurementSetAsJson.setMetricUUID(ms.getMetric().getUUID().toString());
                    tempMeasurementSetAsJson.setMetricType(ms.getMetric().getMetricType().name());
                    tempMeasurementSetAsJson.setMetricUnit(ms.getMetric().getUnit().toString());

                    resultingMeasurementSets.add(tempMeasurementSetAsJson);

                    tempMeasurementSetAsJson = new MeasurementSetAsJson();
                }
                
                logger.debug("Client " + clientUUID + " has " + resultingMeasurementSets.size() + " measurement set(s)");
                return resultingMeasurementSets.toArray(new MeasurementSetAsJson[0]);
            }

        } catch (Throwable ex) {
            logger.error("Failed to return measurement sets for the client", ex);
            return null;
        }
    }

    @RequestMapping(method = RequestMethod.POST, value = "/getmmetricgeneratorsforclient/do.json")
    public @ResponseBody MetricGeneratorAsJson[] getMetricGeneratorsForClient(@RequestBody final String inputData) throws Throwable {
        logger.debug("Getting metric generators for a client. Input data: " + inputData);
        try {

            JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(inputData);
            String clientUUID = inputDataAsJSON.getString("clientUUID");

            Set<MetricGeneratorAsJson> resultingMetricGenerators = new HashSet<MetricGeneratorAsJson>();

            MetricGenerator[] metricGenerators = emService.getMetricGeneratorsForClient(clientUUID);

            if (metricGenerators == null) {
                logger.debug("Client " + clientUUID + " does not have any metric generators");
                return null;
                
            } else {
                
                MetricGeneratorAsJson tempMetricGeneratorAsJson = new MetricGeneratorAsJson();
                
                Set<EntityAsJson> tempEntitiesAsJson = new HashSet<EntityAsJson>();
                EntityAsJson tempEntityAsJson;
                
                Set<AttributeAsJson> tempAttributesAsJson = new HashSet<AttributeAsJson>();
                AttributeAsJson tempAttributeAsJson;
                
                for (MetricGenerator mg : metricGenerators) {
                    tempMetricGeneratorAsJson.setUuid(mg.getUUID().toString());
                    tempMetricGeneratorAsJson.setName(mg.getName());
                    tempMetricGeneratorAsJson.setDescription(mg.getDescription());

                    for (Entity tempEntity : mg.getEntities()) {
                        tempEntityAsJson = new EntityAsJson();
                        tempEntityAsJson.setName(tempEntity.getName());
                        tempEntityAsJson.setDescription(tempEntity.getDescription());
                        tempEntityAsJson.setUuid(tempEntity.getUUID().toString());
                        
                        for (Attribute tempAttribute : tempEntity.getAttributes()) {
                            tempAttributeAsJson = new AttributeAsJson();
                            tempAttributeAsJson.setUuid(tempAttribute.getUUID().toString());
                            tempAttributeAsJson.setName(tempAttribute.getName());
                            tempAttributeAsJson.setDescription(tempAttribute.getDescription());
                            
                            tempAttributesAsJson.add(tempAttributeAsJson);
                        }
                        
                        tempEntityAsJson.setAttributes(tempAttributesAsJson.toArray(new AttributeAsJson[0]));
                        tempEntitiesAsJson.add(tempEntityAsJson);
                        
                        tempAttributesAsJson = new HashSet<AttributeAsJson>();
                    }

                    tempMetricGeneratorAsJson.setListOfEntities(tempEntitiesAsJson.toArray(new EntityAsJson[0]));
                    resultingMetricGenerators.add(tempMetricGeneratorAsJson);

                    tempMetricGeneratorAsJson = new MetricGeneratorAsJson();
                    tempEntitiesAsJson = new HashSet<EntityAsJson>();
                }
                
                logger.debug("Client " + clientUUID + " has " + resultingMetricGenerators.size() + " metric generator(s)");
                return resultingMetricGenerators.toArray(new MetricGeneratorAsJson[0]);
            }

        } catch (Throwable ex) {
            logger.error("Failed to return metric generators for the client", ex);
            return null;
        }
    }
        
}
