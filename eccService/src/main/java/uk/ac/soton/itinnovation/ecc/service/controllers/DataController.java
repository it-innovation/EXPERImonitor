/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2014
//
// Copyright in this library belongs to the University of Southampton
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
//	Created By :			Maxim Bashevoy
//	Created Date :			2014-04-22
//	Created for Project :           EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.ecc.service.controllers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.ecc.service.domain.EccAttribute;
import uk.ac.soton.itinnovation.ecc.service.domain.EccEntity;
import uk.ac.soton.itinnovation.ecc.service.domain.EccGenericMeasurement;
import uk.ac.soton.itinnovation.ecc.service.domain.EccGenericMeasurementSet;
import uk.ac.soton.itinnovation.ecc.service.domain.EccMeasurement;
import uk.ac.soton.itinnovation.ecc.service.domain.EccMeasurementSet;
import uk.ac.soton.itinnovation.ecc.service.services.ConfigurationService;
import uk.ac.soton.itinnovation.ecc.service.services.DataService;
import uk.ac.soton.itinnovation.ecc.service.services.ExperimentService;
import uk.ac.soton.itinnovation.ecc.service.utils.Convert;
import uk.ac.soton.itinnovation.ecc.service.utils.EccAttributesComparator;

/**
 * Exposes experimental data.
 */
@Controller
@RequestMapping("/data")
public class DataController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    ConfigurationService configurationService;

    @Autowired
    ExperimentService experimentService;

    @Autowired
    DataService dataService;

    @RequestMapping(method = RequestMethod.GET, value = "/entities/{experimentUuid}")
    @ResponseBody
    public ArrayList<EccEntity> getEntitiesForExperiment(@PathVariable String experimentUuid) {
        logger.debug("Returning entities for experiment [" + experimentUuid + "]");
        ArrayList<EccEntity> result = new ArrayList<EccEntity>();
        EccEntity tempEntity;
        ArrayList<EccAttribute> attributes;
        MeasurementSet ms;
        try {
            for (Entity e : dataService.getEntitiesForExperiment(UUID.fromString(experimentUuid))) {
                tempEntity = new EccEntity();
                attributes = new ArrayList<EccAttribute>();
                tempEntity.setName(e.getName());
                tempEntity.setDescription(e.getDescription());
                tempEntity.setUuid(e.getUUID());

                for (Attribute a : e.getAttributes()) {
                    // TODO: tidy up
                    ms = dataService.getAllEmptyMeasurementSetsForAttribute(UUID.fromString(experimentUuid), a).iterator().next();
//                    ms = MetricHelper.getMeasurementSetForAttribute(a, mg);
                    attributes.add(new EccAttribute(a.getName(), a.getDescription(), a.getUUID(), a.getEntityUUID(), ms.getMetric().getMetricType().name(), ms.getMetric().getUnit().getName()));
                }
                Collections.sort(attributes, new EccAttributesComparator());
                tempEntity.setAttributes(attributes);
                result.add(tempEntity);
            }
        } catch (Exception ex) {
            logger.error("Failed to return entities for experiment [" + experimentUuid + "]", ex);
        }
        return result;
    }

    /**
     * Returns 10 latest data points for an attribute from now.
     *
     * @param attributeId attribute to return data for.
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/attribute/{attributeId}")
    @ResponseBody
    public EccGenericMeasurementSet getLatestDataForAttribute(@PathVariable String attributeId) {
        logger.debug("Returning 10 latest data entries for attribute [" + attributeId + "]");

        EccAttribute theAttribute = dataService.getEccAttribute(attributeId);
        EccGenericMeasurementSet result = new EccGenericMeasurementSet();

        if (theAttribute != null) {
            String theType = theAttribute.getType();
            logger.debug("Attribute [" + theAttribute.getUuid().toString() + "] is of type '" + theType + "'");

            if (theType.equals("NOMINAL")) {
                result = Convert.eccCounterMeasurementSetToEccGenericMeasurementSet(dataService.getCounterMeasurementsForAttributeAfter(
                        experimentService.getActiveExperiment().getUUID().toString(),
                        attributeId,
                        (new Date()).getTime(),
                        10));
            } else {
                result = Convert.eccMeasurementSetToEccGenericMeasurementSet(dataService.getLatestMeasurementsForAttribute(
                        attributeId,
                        10));
            }

            for (EccGenericMeasurement em : result.getData()) {
                logger.debug("Reporting: " + em.getKey() + ": " + em.getValue());
            }

        }
        return result;
    }

    /**
     * Returns 10 latest data points for an attribute since time.
     *
     * @param attributeId attribute to return data for.
     * @param timestampMsec
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/attribute/{attributeId}/since/{timestampMsec}")
    @ResponseBody
    public EccGenericMeasurementSet getLatestDataForAttributeSince(@PathVariable String attributeId, @PathVariable Long timestampMsec) {
        logger.debug("Returning 10 latest data items for attribute '" + attributeId + "' since '" + timestampMsec + "' (" + new Date(timestampMsec) + ")");

        EccAttribute theAttribute = dataService.getEccAttribute(attributeId);
        EccGenericMeasurementSet result = new EccGenericMeasurementSet();

        if (theAttribute != null) {
            String theType = theAttribute.getType();
            logger.debug("Attribute [" + theAttribute.getUuid().toString() + "] is of type '" + theType + "'");

            if (theAttribute.getType().equals("NOMINAL")) {
                // TODO: this is bad, but no time to fix:
                if (dataService.getCounterMeasurementsForAttributeBeforeAndExcluding(
                        experimentService.getActiveExperiment().getUUID().toString(),
                        attributeId,
                        timestampMsec,
                        10).getData().size() > 0) { // only return 10 latest if there is new stuff
                    result = Convert.eccCounterMeasurementSetToEccGenericMeasurementSet(dataService.getCounterMeasurementsForAttributeAfter(
                            experimentService.getActiveExperiment().getUUID().toString(),
                            attributeId,
                            (new Date()).getTime(),
                            10));
                } else {
                    result.setData(new ArrayList<EccGenericMeasurement>());
                    result.setType(theType);
                    result.setUnit(theAttribute.getUnit());
                }
            } else {
                result = Convert.eccMeasurementSetToEccGenericMeasurementSet(dataService.getLatestSinceMeasurementsForAttribute(
                        attributeId,
                        timestampMsec,
                        10));
            }

        } else {
            logger.error("Failed to find attribute [" + "] in current experiment");
        }

//        EccMeasurementSet tempResult = dataService.getLatestSinceMeasurementsForAttribute(attributeId, timestampMsec, 10);
//        result.setType(tempResult.getType());
//        result.setUnit(tempResult.getUnit());
//
//        // TODO: this does not work when there is no data to start with
//        ArrayList<EccMeasurement> measurements = new ArrayList<EccMeasurement>();
//        for (EccMeasurement em : tempResult.getData()) {
//            if (em.getTimestamp().after(new Date(timestampMsec))) {
//                measurements.add(em);
//                logger.debug("Added: " + em.getTimestamp().getTime() + ": " + em.getValue());
//            } else {
//                logger.debug("Ignored: " + em.getTimestamp().getTime() + ": " + em.getValue());
//            }
//        }
//        result.setData(measurements);
        return result;
    }

    /**
     * Returns attribute data as a download file.
     *
     * @param experimentId
     * @param attributeId attribute to return data for.
     * @param response
     */
    @RequestMapping(method = RequestMethod.GET, value = "/export/experiment/{experimentId}/attribute/{attributeId}")
    @ResponseBody
    public void exportDataForAttribute(@PathVariable String experimentId, @PathVariable String attributeId, HttpServletResponse response) {
        logger.debug("Exporting data for attribute [" + attributeId + "], experiment [" + experimentId + "]");

        // TODO: should be a database-level query
        EccAttribute attribute = null;
        for (EccAttribute eccAttribute : dataService.getAttributesForExperiment(experimentId)) {
            if (eccAttribute.getUuid().equals(UUID.fromString(attributeId))) {
                attribute = eccAttribute;
                break;
            }
        }

        if (attribute == null) {
            logger.error("Attribute [" + attributeId + "] was not found in experiment [" + experimentId + "]");
        } else {

            String fileName = attribute.getName() + " (attribute) - experiment " + experimentId + ".csv";
            response.setContentType("text/csv");
            response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");

            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
                writer.write("Timestamp, Value");

                for (EccMeasurement m : dataService.getAllMeasurementsForAttribute(experimentId, attributeId).getData()) {
                    writer.newLine();
                    writer.write(ISODateTimeFormat.dateTime().print(m.getTimestamp().getTime()) + ", " + m.getValue());
                }

                writer.flush();
                writer.close();
                response.flushBuffer();
            } catch (IOException e) {
                logger.error("Failed to write data to output response stream for attribute [" + attributeId + "], experiment [" + experimentId + "]", e);
            }
        }
    }

    /**
     * Returns entity data as a download file.
     *
     * @param experimentId
     * @param entityId
     * @param response
     */
    @RequestMapping(method = RequestMethod.GET, value = "/export/experiment/{experimentId}/entity/{entityId}")
    @ResponseBody
    public void exportDataForEntity(@PathVariable String experimentId, @PathVariable String entityId, HttpServletResponse response) {
        logger.debug("Exporting data for entity [" + entityId + "], experiment [" + experimentId + "]");

        // TODO: add error reporting
        EccEntity entity = null;

        for (EccEntity tempEntity : dataService.getEntitiesForExperiment(experimentId, true)) {
            if (tempEntity.getUuid().equals(UUID.fromString(entityId))) {
                entity = tempEntity;
                break;
            }
        }

        if (entity == null) {
            logger.error("Entity [" + entityId + "] was not found in experiment [" + experimentId + "]");
        } else {
            ArrayList<EccAttribute> attributes = entity.getAttributes();
            String fileName = entity.getName() + " (entity) - experiment " + experimentId + ".csv";
            response.setContentType("text/csv");
            response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");

            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
                writer.write("Attribute UUID, Attribute Name, Timestamp, Value, Metric type, Metric unit");
                for (EccAttribute a : attributes) {
                    logger.debug("Writing attribute [" + a.getUuid().toString() + "] " + a.getName());
                    for (EccMeasurement m : dataService.getAllMeasurementsForAttribute(experimentId, a.getUuid().toString()).getData()) {
                        writer.newLine();
                        writer.write(a.getName() + ", " + a.getUuid().toString() + ", ");
                        writer.write(ISODateTimeFormat.dateTime().print(m.getTimestamp().getTime()) + ", " + m.getValue() + ", ");
                        writer.write(a.getType() + ", " + a.getUnit());
                    }
                }

                writer.flush();
                writer.close();
                response.flushBuffer();
            } catch (IOException e) {
                logger.error("Failed to write data to output response stream for entity [" + entityId + "], experiment [" + experimentId + "]", e);
            }
        }
    }

    /**
     * Returns client data as a download file.
     *
     * @param clientId
     * @param response
     */
    @RequestMapping(method = RequestMethod.GET, value = "/export/client/{clientId}")
    @ResponseBody
    public void exportDataForClient(@PathVariable String clientId, HttpServletResponse response) {
        logger.debug("Exporting data for client [" + clientId + "]");

        // TODO: add error reporting
        try {
            EMClient theClient = experimentService.getClientByID(UUID.fromString(clientId));
            Experiment currentExperiment = experimentService.getActiveExperiment();

            String experimentUuid;
            if (currentExperiment == null) {
                // should never happen
                experimentUuid = "unknown";
            } else {
                experimentUuid = currentExperiment.getUUID().toString();
            }

            ArrayList<EccEntity> entities = dataService.getEntitiesForClient(clientId, true);
            ArrayList<EccAttribute> attributes;

            String fileName = theClient.getName() + " (client) - experiment " + experimentUuid + ".csv";
            response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
            response.setContentType("text/csv");

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
            writer.write("Entity Name, Entity UUID, Attribute Name, Attribute UUID, Timestamp, Value, Metric type, Metric unit");
            for (EccEntity e : entities) {
                attributes = e.getAttributes();

                for (EccAttribute a : attributes) {
                    for (EccMeasurement m : dataService.getAllMeasurementsForAttribute(a.getUuid().toString()).getData()) {
                        writer.newLine();
                        writer.write(e.getName() + ", " + e.getUuid().toString() + ", ");
                        writer.write(a.getName() + ", " + a.getUuid().toString() + ", ");
                        writer.write(ISODateTimeFormat.dateTime().print(m.getTimestamp().getTime()) + ", " + m.getValue() + ", ");
                        writer.write(a.getType() + ", " + a.getUnit());

                    }
                }
            }

            writer.flush();
            writer.close();
            response.flushBuffer();
        } catch (Exception e) {
            logger.error("Failed to write data to output response stream for client [" + clientId + "]", e);
        }
    }

    /**
     * Returns experiment data as a download file.
     *
     * @param experimentId
     * @param response
     */
    @RequestMapping(method = RequestMethod.GET, value = "/export/experiment/{experimentId}")
    @ResponseBody
    public void exportDataForTheExperiment(@PathVariable String experimentId, HttpServletResponse response) {
        logger.debug("Exporting data for experiment '" + experimentId + "'");

        Experiment experiment = dataService.getExperiment(experimentId, false);

        // TODO: add error reporting
        if (experiment == null) {
            logger.error("Failed to get data for experiment [" + experimentId + "], data service returned NULL experiment for that ID");
        } else {
            try {
                String fileName = experiment.getName() + " - UUID " + experimentId + ".csv";
                response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
                response.setContentType("text/csv");

                ArrayList<EccEntity> entities = dataService.getEntitiesForExperiment(experimentId, true);

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
                writer.write("Entity Name, Entity UUID, Attribute Name, Attribute UUID, Timestamp, Value, Metric type, Metric unit");
                ArrayList<EccAttribute> attributes;
                for (EccEntity e : entities) {
                    attributes = e.getAttributes();

                    for (EccAttribute a : attributes) {
                        for (EccMeasurement m : dataService.getAllMeasurementsForAttribute(experimentId, a.getUuid().toString()).getData()) {
                            writer.newLine();
                            writer.write(e.getName() + ", " + e.getUuid().toString() + ", ");
                            writer.write(a.getName() + ", " + a.getUuid().toString() + ", ");
                            writer.write(ISODateTimeFormat.dateTime().print(m.getTimestamp().getTime()) + ", " + m.getValue() + ", ");
                            writer.write(a.getType() + ", " + a.getUnit());
                        }
                    }
                }

                writer.flush();
                writer.close();
                response.flushBuffer();
            } catch (IOException e) {
                logger.error("Failed to write data to output response stream for experiment [" + experimentId + "]", e);
            }
        }
    }
}
