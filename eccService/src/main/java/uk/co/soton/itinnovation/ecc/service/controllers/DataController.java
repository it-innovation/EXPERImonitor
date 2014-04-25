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
package uk.co.soton.itinnovation.ecc.service.controllers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricHelper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.co.soton.itinnovation.ecc.service.domain.EccAttribute;
import uk.co.soton.itinnovation.ecc.service.domain.EccEntity;
import uk.co.soton.itinnovation.ecc.service.domain.EccMeasurement;
import uk.co.soton.itinnovation.ecc.service.domain.EccMeasurementSet;
import uk.co.soton.itinnovation.ecc.service.services.ConfigurationService;
import uk.co.soton.itinnovation.ecc.service.services.DataService;
import uk.co.soton.itinnovation.ecc.service.services.ExperimentService;
import uk.co.soton.itinnovation.ecc.service.utils.AttributeComparator;
import uk.co.soton.itinnovation.ecc.service.utils.EccAttributeComparator;
import uk.co.soton.itinnovation.ecc.service.utils.EccEntityComparator;

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

    /**
     * Returns 10 latest data points for an attribute.
     *
     * @param attributeId attribute to return data for.
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "/attribute/{attributeId}")
    @ResponseBody
    public EccMeasurementSet getLatestDataForAttribute(@PathVariable String attributeId) {
        logger.debug("Returning data for attribute '" + attributeId + "'");

        EccMeasurementSet result = dataService.getLatestMeasurementsForAttribute(attributeId);
        for (EccMeasurement em : result.getData()) {
            logger.debug("Reporting: " + em.getTimestamp().getTime() + ": " + em.getValue());
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
    public EccMeasurementSet getLatestDataForAttributeSince(@PathVariable String attributeId, @PathVariable Long timestampMsec) {
        logger.debug("Returning 10 latest data items for attribute '" + attributeId + "' since '" + timestampMsec + "' (" + new Date(timestampMsec) + ")");
        EccMeasurementSet result = new EccMeasurementSet();
        EccMeasurementSet tempResult = dataService.getLatestSinceMeasurementsForAttribute(attributeId, timestampMsec, 10);
        result.setType(tempResult.getType());
        result.setUnit(tempResult.getUnit());

        // TODO: this does not work when there is no data to start with
        ArrayList<EccMeasurement> measurements = new ArrayList<EccMeasurement>();
        for (EccMeasurement em : tempResult.getData()) {
            if (em.getTimestamp().after(new Date(timestampMsec))) {
                measurements.add(em);
                logger.debug("Added: " + em.getTimestamp().getTime() + ": " + em.getValue());
            } else {
                logger.debug("Ignored: " + em.getTimestamp().getTime() + ": " + em.getValue());
            }
        }
        result.setData(measurements);

        return result;
    }

    /**
     * Returns attribute data as a download file.
     *
     * @param attributeId attribute to return data for.
     * @param response
     */
    @RequestMapping(method = RequestMethod.GET, value = "/export/attribute/{attributeId}")
    @ResponseBody
    public void exportDataForAttribute(@PathVariable String attributeId, HttpServletResponse response) {
        logger.debug("Exporting data for attribute '" + attributeId + "'");

        // TODO: add error reporting
        Attribute attribute = dataService.getAttribute(attributeId);

        if (attribute == null) {
            logger.error("Failed to create file for attribute [" + "]: data service returned NULL value");
        } else {

            Entity entity = dataService.getEntity(attribute.getEntityUUID().toString(), false);

            if (entity == null) {
                logger.error("Failed to create file for attribute [" + "]: data service returned NULL entity");
            } else {
                String fileName = attribute.getName() + " - " + entity.getName() + ".csv";
                response.setContentType("text/csv");
                response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");

                try {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
                    writer.write("Timestamp, Value");

                    for (EccMeasurement m : dataService.getAllMeasurementsForAttribute(attributeId).getData()) {
                        writer.newLine();
                        writer.write(ISODateTimeFormat.dateTime().print(m.getTimestamp().getTime()) + ", " + m.getValue());
                    }

                    writer.flush();
                    writer.close();
                    response.flushBuffer();
                } catch (IOException e) {
                    logger.error("Failed to write data to output response stream for attribute [" + attributeId + "]", e);
                }
            }
        }
    }

    /**
     * Returns entity data as a download file.
     *
     * @param entityId
     * @param response
     */
    @RequestMapping(method = RequestMethod.GET, value = "/export/entity/{entityId}")
    @ResponseBody
    public void exportDataForEntity(@PathVariable String entityId, HttpServletResponse response) {
        logger.debug("Exporting data for entity '" + entityId + "'");

        // TODO: add error reporting
        Entity entity = dataService.getEntity(entityId, true);

        if (entity == null) {
            logger.error("Failed to create file for entity [" + "]: data service returned NULL value");
        } else {
            // get attributes and sort alphabetically
            ArrayList<Attribute> attributes = new ArrayList<Attribute>(entity.getAttributes());
            logger.debug("Entity [" + entityId + "] has " + attributes.size() + " attributes");
            Collections.sort(attributes, new AttributeComparator());
            String fileName = entity.getName() + ".csv";
            response.setContentType("text/csv");
            response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");

            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
//                writer.write("Attribute UUID, Attribute Name, Timestamp, Value, Metric type, Metric unit");
                writer.write("Attribute Name, Attribute UUID, Timestamp, Value");
                for (Attribute a : attributes) {
                    logger.debug("Writing attribute [" + a.getUUID().toString() + "] " + a.getName());
                    for (EccMeasurement m : dataService.getAllMeasurementsForAttribute(a.getUUID().toString()).getData()) {
                        writer.newLine();
                        writer.write(a.getName() + ", " + a.getUUID().toString() + ", ");
                        writer.write(ISODateTimeFormat.dateTime().print(m.getTimestamp().getTime()) + ", " + m.getValue());
                        // TODO: add metric type and unit as difficult
                    }
                }

                writer.flush();
                writer.close();
                response.flushBuffer();
            } catch (IOException e) {
                logger.error("Failed to write data to output response stream for entity [" + entityId + "]", e);
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
        logger.debug("Exporting data for client '" + clientId + "'");

        // TODO: add error reporting
        try {
            EMClient theClient = experimentService.getClientByID(UUID.fromString(clientId));
            Iterator<MetricGenerator> it = theClient.getCopyOfMetricGenerators().iterator();
            ArrayList<EccEntity> entities = new ArrayList<EccEntity>();
            MetricGenerator mg;
            EccEntity tempEntity;
            ArrayList<EccAttribute> attributes;
            MeasurementSet ms;
            while (it.hasNext()) {
                mg = it.next();
                for (Entity e : mg.getEntities()) {
                    tempEntity = new EccEntity();
                    attributes = new ArrayList<EccAttribute>();
                    tempEntity.setName(e.getName());
                    tempEntity.setDescription(e.getDescription());
                    tempEntity.setUuid(e.getUUID());
                    for (Attribute a : e.getAttributes()) {
                        ms = MetricHelper.getMeasurementSetForAttribute(a, mg);
                        attributes.add(new EccAttribute(a.getName(), a.getDescription(), a.getUUID(), a.getEntityUUID(), ms.getMetric().getMetricType().name(), ms.getMetric().getUnit().getName()));
                    }
                    Collections.sort(attributes, new EccAttributeComparator());
                    tempEntity.setAttributes(attributes);
                    entities.add(tempEntity);
                }
            }

            if (entities.size() > 1) {
                Collections.sort(entities, new EccEntityComparator());
            }

            String fileName = theClient.getName() + ".csv";
            response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
            response.setContentType("text/csv");

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
            writer.write("Entity Name, Entity UUID, Attribute Name, Attribute UUID, Timestamp, Value");
            for (EccEntity e : entities) {
                attributes = e.getAttributes();
                Collections.sort(attributes, new EccAttributeComparator());

                for (EccAttribute a : attributes) {
                    logger.debug("Writing attribute [" + a.getUuid().toString() + "] " + a.getName());
                    for (EccMeasurement m : dataService.getAllMeasurementsForAttribute(a.getUuid().toString()).getData()) {
                        writer.newLine();
                        writer.write(e.getName() + ", " + e.getUuid().toString() + ", ");
                        writer.write(a.getName() + ", " + a.getUuid().toString() + ", ");
                        writer.write(ISODateTimeFormat.dateTime().print(m.getTimestamp().getTime()) + ", " + m.getValue());
                        // TODO: add metric type and unit as difficult
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
}
