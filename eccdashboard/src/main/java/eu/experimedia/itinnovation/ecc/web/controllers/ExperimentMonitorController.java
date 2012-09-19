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
import java.util.HashSet;
import java.util.Set;
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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;

@Controller
@RequestMapping("/em")
public class ExperimentMonitorController {

    private static final Logger logger = Logger.getLogger(ExperimentMonitorController.class);

    @Autowired
    @Qualifier("experimentMonitorService")
    ExperimentMonitorService emService;

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
                
                logger.debug("Client " + clientUUID + " has " + resultingMeasurementSets.size() + " measurement sets");
                return resultingMeasurementSets.toArray(new MeasurementSetAsJson[0]);
            }

        } catch (Throwable ex) {
            logger.error("Failed to return measurement sets for the client");
            ex.printStackTrace();
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
                EntityAsJson tempEntityASJson;
                for (MetricGenerator mg : metricGenerators) {
                    tempMetricGeneratorAsJson.setUuid(mg.getUUID().toString());
                    tempMetricGeneratorAsJson.setName(mg.getName());
                    tempMetricGeneratorAsJson.setDescription(mg.getDescription());


                    for (Entity tempEntity : mg.getEntities()) {
                        tempEntityASJson = new EntityAsJson();
                        tempEntityASJson.setName(tempEntity.getName());
                        tempEntityASJson.setDescription(tempEntity.getDescription());
                        tempEntityASJson.setUuid(tempEntity.getUUID().toString());
                        tempEntitiesAsJson.add(tempEntityASJson);
                    }

                    tempMetricGeneratorAsJson.setListOfEntities(tempEntitiesAsJson.toArray(new EntityAsJson[0]));
                    resultingMetricGenerators.add(tempMetricGeneratorAsJson);

                    tempMetricGeneratorAsJson = new MetricGeneratorAsJson();
                    tempEntitiesAsJson = new HashSet<EntityAsJson>();
                }
                
                logger.debug("Client " + clientUUID + " has " + resultingMetricGenerators.size() + " metric generators");
                return resultingMetricGenerators.toArray(new MetricGeneratorAsJson[0]);
            }

        } catch (Throwable ex) {
            logger.error("Failed to return metric generators for the client");
            ex.printStackTrace();
            return null;
        }
    }
}
