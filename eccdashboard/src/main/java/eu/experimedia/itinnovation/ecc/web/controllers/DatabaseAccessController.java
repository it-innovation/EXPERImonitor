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

import eu.experimedia.itinnovation.ecc.web.data.ExperimentAsJson;
import eu.experimedia.itinnovation.ecc.web.data.MeasurementSetAsJson;
import eu.experimedia.itinnovation.ecc.web.data.MetricGeneratorAsJson;
import eu.experimedia.itinnovation.ecc.web.data.MetricGroupAsJson;
import eu.experimedia.itinnovation.ecc.web.services.DatabaseAccessService;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;

@Controller
@RequestMapping("/da")
public class DatabaseAccessController {
    @Autowired
    @Qualifier("databaseAccessService")
    DatabaseAccessService daService;
    
    @RequestMapping(method = RequestMethod.GET, value = "/getexperiments/do.json")
    public @ResponseBody ExperimentAsJson[] getExperiments() throws Throwable {
        try {
            
            Experiment[] experiments = daService.getExperiments();
            int numExperiments = experiments.length;
            
            ExperimentAsJson tempExperiment = new ExperimentAsJson();
            ExperimentAsJson[] result = new ExperimentAsJson[numExperiments];
            
            int i = 0;
            for (Experiment e : experiments) {
                tempExperiment.setUuid(e.getUUID().toString());
                tempExperiment.setExperimentID(e.getExperimentID());
                tempExperiment.setName(e.getName());
                tempExperiment.setDescription(e.getDescription());
                tempExperiment.setStartTime(e.getStartTime().getTime());
                tempExperiment.setEndTime(e.getEndTime().getTime());
                result[i] = tempExperiment;
                tempExperiment = new ExperimentAsJson();
                i++;
            }
            
            return result;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    @RequestMapping(method = RequestMethod.POST, value = "/getmetricgenerators/do.json")
    public @ResponseBody MetricGeneratorAsJson[] getMetricGenerators(@RequestBody final String inputData) throws Throwable {
        try {
            
            JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(inputData);
            String experimentUUID = inputDataAsJSON.getString("experimentUUID");
            MetricGenerator[] mgs = daService.getMetricGeneratorsForExperiment(experimentUUID);
            int numMetricGenerators = mgs.length;
            
            MetricGeneratorAsJson tempMg = new MetricGeneratorAsJson();
            MetricGeneratorAsJson[] result = new MetricGeneratorAsJson[numMetricGenerators];
            
            int i = 0;
            StringBuffer listOfEntities = new StringBuffer();
            String listOfEntitiesAsString = "";
            for (MetricGenerator mg : mgs) {
                tempMg.setUuid(mg.getUUID().toString());
                tempMg.setName(mg.getName());
                tempMg.setDescription(mg.getDescription());
                
                for (Entity entity : mg.getEntities().toArray(new Entity[0])) {
                    listOfEntities.append(entity.getName());
                    listOfEntities.append(" (");
                    listOfEntities.append(entity.getDescription());
                    listOfEntities.append("), ");
                }
                
                listOfEntitiesAsString = listOfEntities.toString();
                
                if (listOfEntitiesAsString.length() > 2) {
                    listOfEntitiesAsString = listOfEntitiesAsString.substring(0, listOfEntitiesAsString.length() - 2);
                    tempMg.setListOfEntities(listOfEntitiesAsString);
                } else {
                    tempMg.setListOfEntities("unknown entity");
                }

                result[i] = tempMg;
                
                listOfEntities = new StringBuffer();
                tempMg = new MetricGeneratorAsJson();
                i++;
            }
            
            return result;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    @RequestMapping(method = RequestMethod.POST, value = "/getmetricgroups/do.json")
    public @ResponseBody MetricGroupAsJson[] getMetricGroups(@RequestBody final String inputData) throws Throwable {
        try {
            
            JSONObject inputDataAsJSON = (JSONObject) JSONSerializer.toJSON(inputData);
            String experimentUUID = inputDataAsJSON.getString("metricGeneratorUUID");
            MetricGroup[] mgs = daService.getMetricGroupsForMetricGenerator(experimentUUID);
            Attribute[] allAttributes = daService.getAllAttributes();
            int numMetricGroups = mgs.length;
            
            MeasurementSetAsJson tempMeasurementSet = new MeasurementSetAsJson();
            MeasurementSetAsJson[] tempMeasurementSetArray;
            MetricGroupAsJson tempMetricgroup = new MetricGroupAsJson();
            MetricGroupAsJson[] resultingMetricGroupArray = new MetricGroupAsJson[numMetricGroups];
            
            int i = 0, j = 0;
            int numMeasurementSets;
            for (MetricGroup mg : mgs) {
                tempMetricgroup.setUuid(mg.getUUID().toString());
                tempMetricgroup.setName(mg.getName());
                tempMetricgroup.setDescription(mg.getDescription());
                
                numMeasurementSets = mg.getMeasurementSets().size();
                tempMeasurementSetArray = new MeasurementSetAsJson[numMeasurementSets];
                
                for (MeasurementSet ms : mg.getMeasurementSets()) {
                    tempMeasurementSet.setUuid(ms.getUUID().toString());
                    tempMeasurementSet.setAttribute(this.getAttributeNameForMeasurementSet(ms, allAttributes));
//                    tempMeasurementSet.setAttribute(ms.getAttributeUUID().toString());
                    tempMeasurementSet.setMetricUUID(ms.getMetric().getUUID().toString());
                    tempMeasurementSet.setMetricType(ms.getMetric().getMetricType().name());
                    tempMeasurementSet.setMetricUnit(ms.getMetric().getUnit().toString());
                    
                    tempMeasurementSetArray[j] = tempMeasurementSet;
                    
                    tempMeasurementSet = new MeasurementSetAsJson();
                    j++;
                }

                tempMetricgroup.setMeasurementSets(tempMeasurementSetArray);                
                resultingMetricGroupArray[i] = tempMetricgroup;
                
                tempMetricgroup = new MetricGroupAsJson();
                j = 0;
                i++;
            }
            
            return resultingMetricGroupArray;
        } catch (Throwable ex) {
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
}
