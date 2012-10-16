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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.MonitoringEDM;
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
    private HashSet<IEMLifecycleListener> lifecycleListeners;
    private IMonitoringEDM expDataMgr;
    private IMetricGeneratorDAO expMGAccessor;
    private IReportDAO expReportAccessor;
    private IMeasurementSetDAO expMSAccessor;
    private Experiment expInstance;
    private HashMap<Date, String> testMeasurements = new HashMap<Date, String>();

    public DashboardExperimentMonitor() {
        lifecycleListeners = new HashSet<IEMLifecycleListener>();

        expDataMgr = new MonitoringEDM();
    }

    public HashMap<Date, String> getTestMeasurements() {
        return testMeasurements;
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

        // Try initialising a connection with the Rabbit Server
        try {
            initialise(rabbitServerIP);
        } catch (Exception e) {
            throw e;
        }

        if (monitorStatus != IExperimentMonitor.eStatus.INITIALISED) {
            throw new Exception("Not in a state to open entry point");
        }

        // Initialise connection manager
        if (!connectionManager.initialise(epID, amqpChannel)) {
            throw new Exception("Could not open entry point interface!");
        }

        // Link connection manager to lifecycle manager
        connectionManager.setListener(lifecycleManager);

        // Initialise lifecycle manager
        lifecycleManager.initialise(amqpChannel, epID, this);

        monitorStatus = IExperimentMonitor.eStatus.ENTRY_POINT_OPEN;
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
        lifecycleListeners.add(listener);
    }

    @Override
    public void removeLifecycleListener(IEMLifecycleListener listener) {
        lifecycleListeners.remove(listener);
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

        lifecycleManager.windCurrentPhaseDown();
    }

    @Override
    public void goToNextPhase() throws Exception {
        if (lifecycleManager.isWindingCurrentPhaseDown()) {
            throw new Exception("Current winding down phase: "
                    + lifecycleManager.getCurrentPhase().toString());
        }

        lifecycleManager.iterateLifecycle();
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
    public void requestDataBatch(EMClient client, EMDataBatch batch) throws Exception {
        try {
            lifecycleManager.tryRequestDataBatch(client, batch);
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
        Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();

        while (listIt.hasNext()) {
            listIt.next().onClientConnected(client);
        }
    }

    @Override
    public void onClientDisconnected(EMClient client) {
        logger.debug("Client disconnected: " + client.getName() + " [" + client.getID().toString() + "]");
        connectionManager.removeClient(client.getID());

        Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();

        while (listIt.hasNext()) {
            listIt.next().onClientDisconnected(client);
        }
    }

    @Override
    public void onLifecyclePhaseStarted(EMPhase phase) {
        Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();

        while (listIt.hasNext()) {
            listIt.next().onLifecyclePhaseStarted(phase);
        }
    }

    @Override
    public void onLifecyclePhaseCompleted(EMPhase phase) {
        Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();

        while (listIt.hasNext()) {
            listIt.next().onLifecyclePhaseCompleted(phase);
        }
    }

    @Override
    public void onFoundClientWithMetricGenerators(EMClient client) {
        Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();

        while (listIt.hasNext()) {
            listIt.next().onFoundClientWithMetricGenerators(client);
        }

        // get supported phases:
        client.getCopyOfSupportedPhases();
    }

    @Override
    public void onClientSetupResult(EMClient client, boolean success) {
        Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();

        while (listIt.hasNext()) {
            listIt.next().onClientSetupResult(client, success);
        }
    }

    @Override
    public void onGotMetricData(EMClient client, Report report) {
        if (client != null) {
            if (report != null) {
                logger.debug("Received metric data from client: " + client.getName() + " [" + client.getID().toString() + "], report [" + report.getUUID().toString() + "] with " + report.getNumberOfMeasurements() + " measurement(s)");

                MeasurementSet measurementSet = report.getMeasurementSet();

                if (measurementSet != null) {

                    Set<Measurement> measurements = measurementSet.getMeasurements();

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
                                logger.debug(measurementsCounter + ": [" + measurementUUID + "] " + measurementTimestamp.toString() + " - " + measurementValue);
                                testMeasurements.put(measurementTimestamp, measurementValue);
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
        Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();

        while (listIt.hasNext()) {
            listIt.next().onGotSummaryReport(client, summary);
        }
    }

    @Override
    public void onGotDataBatch(EMClient client, EMDataBatch batch) {
        Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();

        while (listIt.hasNext()) {
            listIt.next().onGotDataBatch(client, batch);
        }
    }

    @Override
    public void onClientTearDownResult(EMClient client, boolean success) {
        Iterator<IEMLifecycleListener> listIt = lifecycleListeners.iterator();

        while (listIt.hasNext()) {
            listIt.next().onClientTearDownResult(client, success);
        }
    }

    // Private methods -----------------------------------------------------------
    private void initialise(String rabbitServerIP) throws Exception {
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

        connectionManager = new EMConnectionManager();
        lifecycleManager = new EMLifecycleManager();

        monitorStatus = IExperimentMonitor.eStatus.INITIALISED;
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
