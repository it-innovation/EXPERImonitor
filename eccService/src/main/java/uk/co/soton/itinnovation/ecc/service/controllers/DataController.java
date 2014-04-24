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
import java.util.Date;
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
import uk.co.soton.itinnovation.ecc.service.domain.EccMeasurement;
import uk.co.soton.itinnovation.ecc.service.domain.EccMeasurementSet;
import uk.co.soton.itinnovation.ecc.service.services.ConfigurationService;
import uk.co.soton.itinnovation.ecc.service.services.DataService;
import uk.co.soton.itinnovation.ecc.service.services.ExperimentService;

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

            Entity entity = dataService.getEntity(attribute.getEntityUUID().toString());

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
                    logger.error("Failed to write data to output response stream", e);
                }
            }
        }
    }

}
