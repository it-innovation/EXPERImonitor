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
//	Created Date :			2012-09-19
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////
package eu.experimedia.itinnovation.ecc.web.adapters;

import eu.experimedia.itinnovation.ecc.web.data.DataPoint;
import java.util.*;
import org.apache.log4j.Logger;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPConnectionFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMDataBatch;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPostReportSummary;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMeasurementSetDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IReportDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.dataModelEx.EMClientEx;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.EMConnectionManager;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.impl.workflow.lifecylePhases.EMLifecycleManager;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.IEMLifecycleListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.IExperimentMonitor;

public class DashboardExperimentMonitor implements IExperimentMonitor,
        IEMLifecycleListener {

    private final Logger logger = Logger.getLogger(DashboardExperimentMonitor.class);
    private IExperimentMonitor.eStatus monitorStatus = IExperimentMonitor.eStatus.NOT_YET_INITIALISED;
    private AMQPBasicChannel amqpChannel;
    private EMConnectionManager connectionManager;
    private EMLifecycleManager lifecycleManager;
    private IMonitoringEDM expDataMgr;
    private IMetricGeneratorDAO expMGAccessor;
    private UUID entryPointID;
    private IReportDAO expReportAccessor;
    private IMeasurementSetDAO expMSAccessor;
    private Experiment expInstance;
//    private ArrayList<DashboardMeasurementSet> reportedMeasurementSets = new ArrayList<DashboardMeasurementSet>();
    private HashMap<String, DashboardMeasurementSet> reportedMeasurementSets = new HashMap<String, DashboardMeasurementSet>();
    private HashMap<String, DashboardSummarySet> summarySets = new HashMap<String, DashboardSummarySet>();
    private ArrayList<String> measurementSetsWaitingForData = new ArrayList<String>();

    public DashboardExperimentMonitor() {

        try {
            expDataMgr = EDMInterfaceFactory.getMonitoringEDM();
        } catch (Exception e) {
            logger.error("Could not create EDM");
        }
    }

    public LinkedHashMap<String, DataPoint> getMeasurementsForMeasurementSet(String measurementSetUuid) {
        if (reportedMeasurementSets.containsKey(measurementSetUuid)) {
            
            DashboardMeasurementSet theMeasurementSet = reportedMeasurementSets.get(measurementSetUuid);
            
            EMPhase thePhase = getCurrentPhase();

            // If it's live monitoring phase, start pulling data from clients
            if (thePhase.getIndex() == 3) {

                // Get all clients for the live monitoring phase
                Set<EMClient> currentPhaseClients = getCurrentPhaseClients();

                Iterator<EMClient> currentPhaseClientsIterator = currentPhaseClients.iterator();

                EMClient currentPhaseClient;
                while (currentPhaseClientsIterator.hasNext()) {
                    currentPhaseClient = currentPhaseClientsIterator.next();

                    // Get only ones that are not busy generating data
                    if (!currentPhaseClient.isPullingMetricData()) {
                        try {
                            if (!measurementSetsWaitingForData.contains(measurementSetUuid)) {
                                logger.debug("Pulling metrics from client: [" + currentPhaseClient.getID() + "], measurement set [" + measurementSetUuid + "]");
                                pullMetric(currentPhaseClient, UUID.fromString(measurementSetUuid));
                                measurementSetsWaitingForData.add(measurementSetUuid);
                            } else {
                                logger.debug("Metrics from client: [" + currentPhaseClient.getID() + "], measurement set [ " + measurementSetUuid + "] currently being pulled");
                            }

                        } catch (Exception e) {
                            logger.error("Could not pull metrics from client: "
                                    + currentPhaseClient.getName() + ", because: "
                                    + e.getMessage());
                        }
                    } else {
                        logger.debug("Client " + currentPhaseClient.getName()
                                + " is busy generating metrics");
                    }
                }
            }             
            
            return theMeasurementSet.getMeasurements();
        } else {
            
            DashboardMeasurementSet theMeasurementSet = reportedMeasurementSets.get(measurementSetUuid);
            
            EMPhase thePhase = getCurrentPhase();

            // If it's live monitoring phase, start pulling data from clients
            if (thePhase.getIndex() == 3) {

                // Get all clients for the live monitoring phase
                Set<EMClient> currentPhaseClients = getCurrentPhaseClients();

                Iterator<EMClient> currentPhaseClientsIterator = currentPhaseClients.iterator();

                EMClient currentPhaseClient;
                while (currentPhaseClientsIterator.hasNext()) {
                    currentPhaseClient = currentPhaseClientsIterator.next();

                    // Get only ones that are not busy generating data
                    if (!currentPhaseClient.isPullingMetricData()) {
                        try {
                            logger.debug("Pulling metrics from client: [" + currentPhaseClient.getID() + "] " + currentPhaseClient.getName());
                            pullAllMetrics(currentPhaseClient);

                        } catch (Exception e) {
                            logger.error("Could not pull metrics from client: "
                                    + currentPhaseClient.getName() + ", because: "
                                    + e.getMessage());
                        }
                    } else {
                        logger.debug("Client " + currentPhaseClient.getName()
                                + " is busy generating metrics");
                    }
                }
            }            
            
            return new LinkedHashMap<String, DataPoint>();
        }
        
         
    }

    public LinkedHashMap<String, DataPoint> getMeasurementsForSummarySet(String summarySetUuid) {
        if (summarySets.containsKey(summarySetUuid)) {
            DashboardSummarySet theSummarySet = summarySets.get(summarySetUuid);
            return theSummarySet.getMeasurements();
        } else {
            return new LinkedHashMap<String, DataPoint>();
        }
    }

    // IExperimentMonitor --------------------------------------------------------
    @Override
    public eStatus getStatus() {
        return monitorStatus;
    }

    @Override
    public void openEntryPoint(String rabbitServerIP, UUID epID) throws Exception {
        // Safety first
        if (rabbitServerIP == null || rabbitServerIP.equals("")) {
            throw new Exception("Rabbit server IP is invalid");
        }

        if (epID == null) {
            throw new Exception("Entry point ID is null");
        }

        entryPointID = epID;

        // Try initialising a connection with the Rabbit Server
        try {
            basicInitialise(rabbitServerIP);
            initialiseManagers();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void openEntryPoint(Properties emProps) throws Exception {
        // Safety first
        if (emProps == null) {
            throw new Exception("Configuration properties are NULL");
        }

        String epVal = emProps.getProperty("Monitor_ID");
        entryPointID = UUID.fromString(epVal);

        if (entryPointID == null) {
            throw new Exception("Configuration of entry point ID is invalid");
        }

        // Now try connecting and opening the entry point
        try {
            configInitialise(emProps);
            initialiseManagers();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Set<EMClient> getAllConnectedClients() {
        return getSimpleClientSet(connectionManager.getConnectedClients());
    }

    @Override
    public Set<EMClient> getCurrentPhaseClients() {
        HashSet<EMClient> clients = new HashSet<EMClient>();

        Set<EMClientEx> exClients = lifecycleManager.getCopySetOfCurrentPhaseClients();
        Iterator<EMClientEx> exIt = exClients.iterator();

        while (exIt.hasNext()) {
            clients.add(exIt.next());
        }

        return clients;
    }

    @Override
    public void addLifecyleListener(IEMLifecycleListener listener) {
    }

    @Override
    public void removeLifecycleListener(IEMLifecycleListener listener) {
    }

    @Override
    public EMPhase startLifecycle(Experiment expInfo) throws Exception {

        if (expInfo == null) {
            throw new Exception("Experiment info is NULL");
        }

        if (monitorStatus != IExperimentMonitor.eStatus.ENTRY_POINT_OPEN) {
            throw new Exception("Not in a state ready to start lifecycle");
        }

        if (connectionManager.getConnectedClientCount() == 0) {
            throw new Exception("No clients connected to monitor");
        }

        if (lifecycleManager.isLifecycleStarted()) {
            throw new Exception("Lifecycle has already started");
        }

//        createExperiment();

        // Save a list of connected clients and properties to the database?
        // Then just use that list instead of talking to EM?

        lifecycleManager.setExperimentInfo(expInfo);

        return lifecycleManager.iterateLifecycle();
    }

    private void createExperiment() {
        logger.debug("Creating new experiment in the database");

        try {
            expMGAccessor = expDataMgr.getMetricGeneratorDAO();
            expReportAccessor = expDataMgr.getReportDAO();
            expMSAccessor = expDataMgr.getMeasurementSetDAO();

            Date expDate = new Date();
            expInstance = new Experiment();
            expInstance.setName("Test Experiment");
            expInstance.setDescription("Test Experimedia experiment");
            expInstance.setStartTime(expDate);
            expInstance.setExperimentID("1");

            IExperimentDAO expDAO = expDataMgr.getExperimentDAO();
            expDAO.saveExperiment(expInstance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create new experiment in the database");
        }

    }

    @Override
    public EMPhase getCurrentPhase() {
        EMPhase currentPhase = EMPhase.eEMUnknownPhase;

        if (lifecycleManager != null) {
            currentPhase = lifecycleManager.getCurrentPhase();
        }

        return currentPhase;
    }

    @Override
    public EMPhase getNextPhase() {
        return lifecycleManager.getCurrentPhase().nextPhase();
    }

    @Override
    public boolean isCurrentPhaseActive() {
        return lifecycleManager.isCurrentPhaseActive();
    }

    @Override
    public void stopCurrentPhase() throws Exception {
        if (lifecycleManager.isWindingCurrentPhaseDown()) {
            throw new Exception("Current winding down phase: "
                    + lifecycleManager.getCurrentPhase().toString());
        }
        else
            lifecycleManager.windCurrentPhaseDown();
    }

    @Override
    public void goToNextPhase() throws Exception {
        if (lifecycleManager.isWindingCurrentPhaseDown()) {
            throw new Exception("Current winding down phase: " + lifecycleManager.getCurrentPhase().toString());
        } else {
            lifecycleManager.iterateLifecycle();

            EMPhase currentPhase = lifecycleManager.getCurrentPhase();
            logger.debug("Current phase is: [" + currentPhase.getIndex() + "] " + currentPhase.name());

        }
    }

    @Override
    public void endLifecycle() throws Exception {
        lifecycleManager.endLifecycle();

        if (amqpChannel != null) {
            amqpChannel.close();
        }
    }

    @Override
    public void pullMetric(EMClient client, UUID measurementSetID) throws Exception {
        try {
            lifecycleManager.tryPullMetric(client, measurementSetID);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void pullAllMetrics(EMClient client) throws Exception {
        logger.debug("pullAllMetrics: [" + client.getID() + "] " + client.getName());
        try {
            lifecycleManager.tryPullAllMetrics(client);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void requestDataBatches(EMClient client, UUID measurementSetID) throws Exception {
        try {
            lifecycleManager.tryRequestDataBatch(client, measurementSetID);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void getAllDataBatches(EMClient client) throws Exception {
        try {
            lifecycleManager.tryGetAllDataBatches(client);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void notifyClientOfTimeOut(EMClient client) throws Exception {
        try {
            lifecycleManager.tryClientTimeOut(client);
        } catch (Exception e) {
            throw e;
        }
    }

    // IEMLifecycleListener ------------------------------------------------------
    @Override
    public void onClientConnected(EMClient client) {
        logger.debug("Client connected: " + client.getName() + " [" + client.getID().toString() + "]");

    }

    @Override
    public void onClientDisconnected(EMClient client) {
        logger.debug("Client disconnected: " + client.getName() + " [" + client.getID().toString() + "]");
        connectionManager.removeClient(client.getID());
        
    }

    @Override
    public void onLifecyclePhaseStarted(EMPhase phase) {
        logger.debug("onLifecyclePhaseStarted");
    }

    @Override
    public void onLifecyclePhaseCompleted(EMPhase phase) {
        logger.debug("onLifecyclePhaseCompleted");
    }

    @Override
    public void onNoFurtherLifecyclePhases() {
        logger.debug("onNoFurtherLifecyclePhases");
    }

    @Override
    public void onFoundClientWithMetricGenerators(EMClient client) {
        logger.debug("onFoundClientWithMetricGenerators");
    }

    @Override
    public void onClientSetupResult(EMClient client, boolean success) {
        logger.debug("onClientSetupResult");
    }

    @Override
    public void onGotMetricData(EMClient client, Report report) {
        if (client != null) {
            if (report != null) {

                MeasurementSet measurementSet = report.getMeasurementSet();
                

                if (measurementSet != null) {

                    logger.debug("Received metric data from client: " + client.getName() + " [" + client.getID().toString()
                            + "], report [" + report.getUUID().toString() + "] with " + report.getNumberOfMeasurements()
                            + " measurement(s) for measurement set [" + measurementSet.getUUID().toString() + "]");
                    
                    if (measurementSetsWaitingForData.contains(measurementSet.getUUID().toString()))
                        measurementSetsWaitingForData.remove(measurementSet.getUUID().toString());

                    String measurementSetUuid = measurementSet.getUUID().toString();
                    DashboardMeasurementSet theDashboardMeasurementSet;
                    if (reportedMeasurementSets.containsKey(measurementSetUuid)) {
                        logger.debug("Already tracking measurementSet: [" + measurementSetUuid + "] ");
                        theDashboardMeasurementSet = reportedMeasurementSets.get(measurementSetUuid);
                    } else {
                        logger.debug("Tracking NEW measurementSet: [" + measurementSetUuid + "] ");
                        theDashboardMeasurementSet = new DashboardMeasurementSet(measurementSetUuid);
                        reportedMeasurementSets.put(measurementSetUuid, theDashboardMeasurementSet);
                    }

                    Set<Measurement> measurements = measurementSet.getMeasurements();

                    if (measurements != null) {
                        if (!measurements.isEmpty()) {
                            Iterator it = measurements.iterator();
                            Measurement measurement;
                            int measurementsCounter = 1;
                            String measurementUUID, measurementValue;
                            Date measurementTimestamp;
                            LinkedHashMap<String, DataPoint> currentMeasurements = theDashboardMeasurementSet.getMeasurements();
                            Iterator itCurrent;
                            String currentMeasurementUUID;
                            DataPoint currentDatapoint;
                            boolean pointExists = false;
                            while (it.hasNext()) {
                                measurement = (Measurement) it.next();
                                measurementUUID = measurement.getUUID().toString();
                                measurementTimestamp = measurement.getTimeStamp();
                                measurementValue = measurement.getValue();

                                logger.debug("Measurement " + measurementsCounter + ": [" + measurementUUID + "] " + measurementTimestamp.toString() + " - " + measurementValue);
                                
                                itCurrent = currentMeasurements.keySet().iterator();
                                while(itCurrent.hasNext()) {
                                    currentMeasurementUUID = (String) itCurrent.next();
                                    currentDatapoint = currentMeasurements.get(currentMeasurementUUID);
                                    if ( (currentDatapoint.getTime() == measurementTimestamp.getTime()) && (currentDatapoint.getValue().equals(measurementValue) )) {
                                        pointExists = true;
                                        break;
                                    }
                                }                                
                                
                                if (pointExists) {
                                    logger.debug("Measurement already exists");
                                    pointExists = false;
                                } else {
                                    logger.debug("Measurement is NEW");
                                    theDashboardMeasurementSet.addMeasurement(measurementUUID, new DataPoint(measurementTimestamp.getTime(), measurementValue, measurementUUID));
                                }

                                measurementsCounter++;

                            }
//                            mainView.addLogText(client.getName() + " got metric data: " + measurements.iterator().next().getValue());
                        } else {
                            logger.error("Measurements for measurement set [" + measurementSet.getUUID().toString() + "] are EMPTY");
                        }
                    } else {
                        logger.error("Measurements for measurement set [" + measurementSet.getUUID().toString() + "] are NULL");
                    }

                } else {
                    logger.error("Measurement set for report [" + report.getUUID().toString() + "] is NULL");

                }
            } else {
                logger.error("Received metric data NULL report from client: " + client.getName() + " [" + client.getID().toString() + "]");
            }
        } else {
            logger.error("Received metric data from NULL client!");
        }

//        Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
//
//        while (listIt.hasNext()) {
//            listIt.next().onGotMetricData(client, report);
//        }
    }

    @Override
    public void onGotSummaryReport(EMClient client, EMPostReportSummary summary) {
        if (client != null) {
            if (summary != null) {

                Set<UUID> summaryMeasurementSetIds = summary.getReportedMeasurementSetIDs();

                if (!summaryMeasurementSetIds.isEmpty()) {

                    logger.debug("Received summary report from client: " + client.getName() + " [" + client.getID().toString()
                            + "], with " + summaryMeasurementSetIds.size() + " measurement set(s)");

                    // Go through all reported measurement sets:
                    String summarySetUuid;
                    Report tempSummaryReport;
                    Set<Measurement> measurements;
                    MeasurementSet tempMeasurementSet;
                    for (UUID measurementSetUuid : summaryMeasurementSetIds) {

                        summarySetUuid = measurementSetUuid.toString();

                        DashboardSummarySet theDashboardSummarySet;
                        if (summarySets.containsKey(summarySetUuid)) {
                            logger.debug("Already tracking summarySet: [" + summarySetUuid + "] ");
                            theDashboardSummarySet = summarySets.get(summarySetUuid);
                        } else {
                            logger.debug("Tracking NEW summarySet: [" + summarySetUuid + "] ");
                            theDashboardSummarySet = new DashboardSummarySet(summarySetUuid);
                            summarySets.put(summarySetUuid, theDashboardSummarySet);
                        }

                        tempSummaryReport = summary.getReport(measurementSetUuid);

                        if (tempSummaryReport != null) {

                            tempMeasurementSet = tempSummaryReport.getMeasurementSet();

                            if (tempMeasurementSet != null) {

                                measurements = tempMeasurementSet.getMeasurements();

                                if (measurements != null) {
                                    if (!measurements.isEmpty()) {
                                        Iterator it = measurements.iterator();
                                        Measurement measurement;
                                        int measurementsCounter = 1;
                                        String measurementUUID, measurementValue;
                                        Date measurementTimestamp;
                                        while (it.hasNext()) {
                                            measurement = (Measurement) it.next();
                                            measurementUUID = measurement.getUUID().toString();
                                            measurementTimestamp = measurement.getTimeStamp();
                                            measurementValue = measurement.getValue();

                                            logger.debug("Summary measurement " + measurementsCounter + ": [" + measurementUUID + "] " + measurementTimestamp.toString() + " - " + measurementValue);

                                            theDashboardSummarySet.addMeasurement(measurementUUID, new DataPoint(measurementTimestamp.getTime(), measurementValue, measurementUUID));

                                            measurementsCounter++;

                                        }
                                        //                            mainView.addLogText(client.getName() + " got metric data: " + measurements.iterator().next().getValue());
                                    } else {
                                        logger.error("Measurements for measurement set [" + tempMeasurementSet.getUUID().toString() + "] are EMPTY");
                                    }
                                } else {
                                    logger.error("Measurements for measurement set [" + tempMeasurementSet.getUUID().toString() + "] are NULL");
                                }
                            } else {
                                logger.error("Measurement set for summary report [" + tempSummaryReport.getUUID().toString() + "] is NULL");
                            }
                        } else {
                            logger.error("Summary report for measurement set [" + measurementSetUuid + "] is NULL");
                        }
                    }

                } else {
                    logger.error("Received EMPTY summary report (no measurement set UUIDs) from client: " + client.getName() + " [" + client.getID().toString() + "]");
                }

            } else {
                logger.error("Received NULL summary report from client: " + client.getName() + " [" + client.getID().toString() + "]");
            }
        } else {
            logger.error("Received summary report from NULL client!");
        }


//        Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();
//
//        while (listIt.hasNext()) {
//            listIt.next().onGotSummaryReport(client, summary);
//        }
    }

    @Override
    public void onGotDataBatch(EMClient client, EMDataBatch batch) {
        logger.debug("onGotDataBatch");
    }

    @Override
    public void onDataBatchMeasurementSetCompleted(EMClient client, UUID measurementSetID) {
        logger.debug("onDataBatchMeasurementSetCompleted");
    }

    @Override
    public void onAllDataBatchesRequestComplete(EMClient client) {
        logger.debug("onAllDataBatchesRequestComplete");
    }

    @Override
    public void onClientTearDownResult(EMClient client, boolean success) {
        logger.debug("onClientTearDownResult");
    }

    // Private methods -----------------------------------------------------------
    private void basicInitialise(String rabbitServerIP) throws Exception {
        AMQPConnectionFactory amqpCF = new AMQPConnectionFactory();

        if (!amqpCF.setAMQPHostIPAddress(rabbitServerIP)) {
            throw new Exception("Could not set the server IP correctly");
        }

        amqpCF.connectToAMQPHost();
        if (!amqpCF.isConnectionValid()) {
            throw new Exception("Could not connect to Rabbit server");
        }

        amqpChannel = amqpCF.createNewChannel();
        if (amqpChannel == null) {
            throw new Exception("Could not create AMQP channel");
        }
    }

    private void configInitialise(Properties emProps) throws Exception {
        AMQPConnectionFactory amqpCF = new AMQPConnectionFactory();
        try {
            amqpCF.connectToAMQPHost(emProps);
            if (!amqpCF.isConnectionValid()) {
                throw new Exception("Could not connect to Rabbit server");
            }

            amqpChannel = amqpCF.createNewChannel();
            if (amqpChannel == null) {
                throw new Exception("Could not create AMQP channel");
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private void initialiseManagers() throws Exception {
        connectionManager = new EMConnectionManager();
        lifecycleManager = new EMLifecycleManager();

        monitorStatus = IExperimentMonitor.eStatus.INITIALISED;

        if (monitorStatus != IExperimentMonitor.eStatus.INITIALISED) {
            throw new Exception("Not in a state to open entry point");
        }

        // Initialise connection manager
        if (!connectionManager.initialise(entryPointID, amqpChannel)) {
            throw new Exception("Could not open entry point interface!");
        }

        // Link connection manager to lifecycle manager
        connectionManager.setListener(lifecycleManager);

        // Initialise lifecycle manager
        lifecycleManager.initialise(amqpChannel, entryPointID, this);

        monitorStatus = IExperimentMonitor.eStatus.ENTRY_POINT_OPEN;
    }

    private Set<EMClient> getSimpleClientSet(Set<EMClientEx> exClients) {
        HashSet<EMClient> simpleClients = new HashSet<EMClient>();
        Iterator<EMClientEx> exIt = exClients.iterator();

        while (exIt.hasNext()) {
            simpleClients.add((EMClient) exIt.next());
        }

        return simpleClients;
    }
}
