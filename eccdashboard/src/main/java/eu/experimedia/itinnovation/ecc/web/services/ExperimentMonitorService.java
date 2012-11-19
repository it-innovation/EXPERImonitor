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

package eu.experimedia.itinnovation.ecc.web.services;

import eu.experimedia.itinnovation.ecc.web.data.DataPoint;
import eu.experimedia.itinnovation.ecc.web.data.EccPropertiesAsJson;
import eu.experimedia.itinnovation.ecc.web.helpers.ExperimentMonitorHelper;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.MonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IExperimentDAO;

@Service("experimentMonitorService")
public class ExperimentMonitorService {
    
    private static final Logger logger = Logger.getLogger(ExperimentMonitorService.class);
    
    @Autowired
    @Qualifier("experimentMonitorHelper")
    private transient ExperimentMonitorHelper emHelper;
    
    /**
     * LIVE MONITORING PHASE ONLY. Returns measurements for a measurement set.
     * 
     */
    public LinkedHashMap<String, DataPoint> getMeasurementsForMeasurementSet(String measurementSetUuid) throws Throwable {
        return emHelper.getExperimentMonitor().getMeasurementsForMeasurementSet(measurementSetUuid);
    }    
    
    /**
     * TEAR DOWN PHASE ONLY. Returns measurements for a summary measurement set.
     * 
     */
    public LinkedHashMap<String, DataPoint> getMeasurementsForSummarySet(String measurementSetUuid) throws Throwable {
        return emHelper.getExperimentMonitor().getMeasurementsForSummarySet(measurementSetUuid);
    }    
    
    public EMClient[] getAllConnectedClients() throws Throwable {
        logger.debug("Returning a list of all connected clients");
        
        EMClient[] clients = emHelper.getExperimentMonitor().getAllConnectedClients().toArray(new EMClient[0]);
        
        if (clients != null) {
            if (clients.length > 0) {
                logger.debug("List of connected clients has " + clients.length + " item(s)");
            } else {
                logger.debug("List of connected clients is EMPTY");
            }
        } else {
            logger.debug("List of connected clients is NULL");
        }
        
        return clients;
    }
    
    public EMClient[] getCurrentPhaseClients() throws Throwable {
        logger.debug("Returning a list of current phase clients");
        
        EMClient[] clients = emHelper.getExperimentMonitor().getCurrentPhaseClients().toArray(new EMClient[0]);
        
        if (clients != null) {
            if (clients.length > 0) {
                logger.debug("List of current phase clients has " + clients.length + " item(s)");
            } else {
                logger.debug("List of current phase clients is EMPTY");
            }
        } else {
            logger.debug("List of current phase clients is NULL");
        }
        
        return clients;
    }
    
    public EMPhase getCurrentPhase() throws Throwable {
        return emHelper.getExperimentMonitor().getCurrentPhase();
    }
    
    public EccPropertiesAsJson getEccProperties() throws Throwable {
        return emHelper.getExperimentMonitor().getEccProperties();
    }
    
    public EMPhase startLifeCycle() throws Throwable {
        logger.debug("Starting lifecycle");
        
//        IMonitoringEDM expDataMgr = emHelper.getExperimentMonitor().getExpDataMgr();
//        Date expDate = new Date();
//        Experiment expInstance = new Experiment();
//        expInstance.setName("Test Experiment");
//        expInstance.setDescription("Test Experimedia experiment");
//        expInstance.setStartTime(expDate);
//        expInstance.setExperimentID("1");
//
//        IExperimentDAO expDAO = expDataMgr.getExperimentDAO();
//        expDAO.saveExperiment(expInstance);
        
        return emHelper.getExperimentMonitor().startLifecycle();
    }
    
    public void goToNextPhase() throws Throwable {
        logger.debug("Going to the next phase");
        emHelper.getExperimentMonitor().goToNextPhase();
    }
    
    public MetricGenerator[] getMetricGeneratorsForClient(String clientUUID) throws Throwable {
        logger.debug("Getting metric generators for client " + clientUUID);
        boolean clientIsOnTheList = false;
        EMClient theClient = null;
        Set<MetricGenerator> resultingMetricGeneratorSet = new HashSet<MetricGenerator>();
        
        for (EMClient client : this.getAllConnectedClients()) {
            if (client.getID().toString().equals(clientUUID)) {
                clientIsOnTheList = true;
                theClient = client;
                break;
            }
        } 
        
        if (clientIsOnTheList) {
            for (MetricGenerator metricGenerator : theClient.getCopyOfMetricGenerators()) {    
                logger.debug("Metric generator: " + metricGenerator.getUUID().toString());
                resultingMetricGeneratorSet.add(metricGenerator);
            }
            logger.debug("Returning " + resultingMetricGeneratorSet.size() + " metric generators for client " + clientUUID);
            return resultingMetricGeneratorSet.toArray(new MetricGenerator[0]);
        } else {
            logger.error("Client " + clientUUID + " is not currently connected");
            return null;
        }
    }
    
    public MeasurementSet[] getMeasurementSetsForClient(String clientUUID) throws Throwable {
        logger.debug("Getting measurement sets for client " + clientUUID);
        boolean clientIsOnTheList = false;
        EMClient theClient = null;
        Set<MeasurementSet> resultingMeasurementSet = new HashSet<MeasurementSet>();
        
        for (EMClient client : this.getAllConnectedClients()) {
            if (client.getID().toString().equals(clientUUID)) {
                clientIsOnTheList = true;
                theClient = client;
                break;
            }
        }
        
        if (clientIsOnTheList) {
            for (MetricGenerator metricGenerator : theClient.getCopyOfMetricGenerators()) {
                logger.debug("Metric generator: " + metricGenerator.getUUID().toString());
                for (MetricGroup metricGroup : metricGenerator.getMetricGroups()) {
                    for (MeasurementSet measurementSet : metricGroup.getMeasurementSets()) {
                        resultingMeasurementSet.add(measurementSet);
                    }
                }
            }
            logger.debug("Returning " + resultingMeasurementSet.size() + " mesurement sets for client " + clientUUID);
            return resultingMeasurementSet.toArray(new MeasurementSet[0]);
        } else {
            logger.error("Client " + clientUUID + " is not currently connected");
            return null;
        }
    }

}
