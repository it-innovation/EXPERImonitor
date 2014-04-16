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
//	Created By :			Simon Crowle
//                          Maxim Bashevoy
//	Created Date :			2014-04-02
//	Created for Project :   EXPERIMEDIA
//
/////////////////////////////////////////////////////////////////////////
package uk.co.soton.itinnovation.ecc.service.services;

import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Measurement;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGenerator;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricHelper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricType;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Report;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMClient;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMDataBatch;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPostReportSummary;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvReport;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.IMonitoringEDM;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IExperimentDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.metrics.dao.IReportDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.IEMLifecycleListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.IExperimentMonitor;
import uk.co.soton.itinnovation.ecc.service.domain.DatabaseConfiguration;
import uk.co.soton.itinnovation.ecc.service.domain.RabbitConfiguration;
import uk.co.soton.itinnovation.ecc.service.process.ExperimentStateModel;
import uk.co.soton.itinnovation.ecc.service.process.LiveMetricScheduler;
import uk.co.soton.itinnovation.ecc.service.process.LiveMetricSchedulerListener;

/**
 * ExperimentService provides executive control over the ECC and experiment
 * work-flow.
 */
@Service("experimentService")
public class ExperimentService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final static String DEFAULT_EXPERIMENT_NAME = "EXPERIMEDIA Experiment";
    private final static String DEFAULT_EXPERIMENT_DESCRIPTION = "Sample experiment description";

    private IExperimentMonitor expMonitor;
    private IMonitoringEDM expDataManager;
    private IMetricGeneratorDAO expMetGeneratorDAO;
    private IReportDAO expReportAccessor;

    private ExperimentStateModel expStateModel;
    private LiveMetricScheduler liveMetricScheduler;
    private boolean started = false;
    private boolean experimentInProgress = false;

    public ExperimentService() {
    }

    /**
     * Initialises the service (empty).
     */
    @PostConstruct
    public void init() {
    }

    /**
     * Ensures the service is shut down properly.
     */
    @PreDestroy
    public void shutdown() {

        logger.debug("Shutting down experiment service");

        if (started) {
            // Metrics sheduling shutdown
            if (liveMetricScheduler != null) {
                liveMetricScheduler.shutDown();
                liveMetricScheduler = null;
            }

            // Experiment monitor shutdown
            if (expMonitor != null) {
                expMonitor.shutDown();
                expMonitor = null;
            }

            // Experiment data manager tidy up
            expReportAccessor = null;
            expMetGeneratorDAO = null;
            expDataManager = null;
        }

        logger.debug("Experiment service shut down");
    }

    /**
     * Starts the service (should only be called by
     * {@link ConfigurationService})
     *
     * @param databaseConfiguration
     * @param rabbitConfiguration
     * @return true if everything worked.
     */
    boolean start(DatabaseConfiguration databaseConfiguration, RabbitConfiguration rabbitConfiguration) {

        started = false;
        logger.debug("Starting experiment service");

        // Try setting up the EDM ----------------------------------------------
        if (databaseConfiguration == null) {
            logger.error("Failed to start experiment service: database configuration is NULL");
            return false;
        } else {

            Properties props = new Properties();
            props.put("dbPassword", databaseConfiguration.getUserPassword());
            props.put("dbName", databaseConfiguration.getDatabaseName());
            props.put("dbType", databaseConfiguration.getDatabaseType());
            props.put("dbURL", databaseConfiguration.getUrl());
            props.put("dbUsername", databaseConfiguration.getUserName());

            try {
                expDataManager = EDMInterfaceFactory.getMonitoringEDM(props);
            } catch (Exception e) {
                logger.error("Failed to get monitoring EDM", e);
                return false;
            }

            if (!expDataManager.isDatabaseSetUpAndAccessible()) {
                logger.error("Failed to initialise database for experiment service: could not access EDM database");
                return false;
            } else {
                // Create data accessors
                try {
                    expMetGeneratorDAO = expDataManager.getMetricGeneratorDAO();
                    expReportAccessor = expDataManager.getReportDAO();
                } catch (Exception e) {
                    logger.error("Failed to create data accessors", e);
                    return false;
                }

                logger.info("EDM initialisation completed OK");

                // Try initialising the state model ------------------------------------
                logger.info("Attempting to initialise experiment state");

                expStateModel = new ExperimentStateModel();
                try {
                    expStateModel.initialise(props);
                } catch (Exception e) {
                    logger.error("Failed to initialise experiment state", e);
                    return false;
                }

                logger.info("State model initialised");

                // Try setting up the Experiment monitor -------------------------------
                logger.info("Attempting to connect to RabbitMQ server");

                expMonitor = EMInterfaceFactory.createEM();
                expMonitor.addLifecyleListener(new ExpLifecycleListener());

                // Configure EM properties
                if (rabbitConfiguration == null) {
                    logger.error("Failed to initialised experiment service: Rabbit configuration is NULL");
                    return false;
                } else {

                    props = new Properties();
                    props.put("Rabbit_Port", rabbitConfiguration.getPort());
                    props.put("Rabbit_Use_SSL", (rabbitConfiguration.isUseSsl() ? "true" : "false"));
                    props.put("Rabbit_IP", rabbitConfiguration.getIp());
                    props.put("Monitor_ID", rabbitConfiguration.getMonitorId().toString());
                    props.put("Rabbit_Password", rabbitConfiguration.getUserPassword());
                    props.put("Rabbit_Username", rabbitConfiguration.getUserName());

                    try {
                        // Try opening the RabbitMQ entry point for this service
                        expMonitor.openEntryPoint(props);
                    } catch (Exception e) {
                        logger.error("Failed to the RabbitMQ entry point", e);
                        return false;
                    }

                    logger.info("EM initialisation completed OK");

                    liveMetricScheduler = new LiveMetricScheduler(new LiveMetricsScheduleListener());

                    started = true;
                    return true;
                }
            }
        }
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isExperimentInProgress() {
        return experimentInProgress;
    }

    /**
     * Use this method to try to start a new experiment. If the input parameters
     * are null or there is already an active experiment, this method will
     * throw. Under normal conditions, this method create a new experiment in
     * the database and invite ECC clients already known to the service to join
     * the new experiment.
     *
     * @param projName - Name of the project associated with the experiment
     * @param expName - Name of this specific experiment
     * @param expDesc - Short description of the experiment
     * @return - Returns meta-data about the experiment, null if failed to
     * create.
     */
    public Experiment startExperiment(String projName, String expName, String expDesc) {

        String name = expName;
        String description = expDesc;
        // Safety first
        if (!started) {
            throw new IllegalStateException("Cannot start experiment: service not initialised");
        }
        if (projName == null || projName.isEmpty()) {
            throw new IllegalArgumentException("Cannot start experiment: project name is NULL or empty");
        }
        if (expName == null || expName.isEmpty()) {
            name = DEFAULT_EXPERIMENT_NAME;
        }
        if (expDesc == null || expDesc.isEmpty()) {
            description = DEFAULT_EXPERIMENT_DESCRIPTION;
        }
        if (expStateModel.isExperimentActive()) {
            throw new IllegalStateException("Cannot start experiment: an experiment is already active");
        }

        // Create new experiment instance
        Experiment newExp = new Experiment();
        newExp.setExperimentID(projName);
        newExp.setName(name);
        newExp.setDescription(description);
        newExp.setStartTime(new Date());

        try {

            // Prepare metrics database
            IExperimentDAO expDAO = expDataManager.getExperimentDAO();
            expDAO.saveExperiment(newExp);

            // Saved OK, so set as the active experiment in state model
            expStateModel.setActiveExperiment(newExp);

            // Prepare PROV logging (TO DO)
            //liveMonitorController.createPROVLog( currentExperiment.getUUID(), eccBasePath );
            // Go straight into live monitoring
            expMonitor.startLifecycle(newExp, EMPhase.eEMLiveMonitoring);

            experimentInProgress = true;
            return newExp;
        } catch (Exception ex) {
            String problem = "Could not start experiment because: " + ex.getMessage();
            logger.error(problem);
            return null;
        }
    }

    /**
     * Use this method to try to stop an experiment. The service will attempt to
     * save the finish time of the experiment to the database before then
     * issuing 'stop' messages to attached clients, where appropriate.
     *
     * @throws Exception - throws if it was not possible to finalise the
     * experiment or there is not an active experiment running.
     */
    public void stopExperiment() throws Exception {

        // Safety first
        if (!started) {
            throw new Exception("Cannot stop experiment: service not initialised");
        }

        Experiment exp = expStateModel.getActiveExperiment();
        if (exp != null) {

            try {

                // Finish up the experiment lifecycle
                expMonitor.endLifecycle();

                // Finish up the experiment on the database
                exp.setEndTime(new Date());
                IExperimentDAO expDAO = expDataManager.getExperimentDAO();
                expDAO.finaliseExperiment(exp);

                expStateModel.setActiveExperiment(null);

                experimentInProgress = false;

                // TODO: Clean up PROV?
            } catch (Exception ex) {

                String problem = "Could not stop experiment because: " + ex.getMessage();

                logger.error(problem);
                throw new Exception(problem, ex);
            }
        } else {
            throw new Exception("Could not stop experiment: no experiment currently active");
        }
    }

    /**
     * Returns experiment meta-data if there is an experiment currently active.
     *
     * @return - Returns NULL if no experiment is currently active.
     */
    public Experiment getActiveExperiment() {

        Experiment activeExp = null;

        if (started) {
            activeExp = expStateModel.getActiveExperiment();
        }

        return activeExp;
    }

    /**
     * Returns the current phase of the active experiment. If there is no active
     * experiment, EMPhase.eEMUnknownPhase is returned.
     *
     * @return
     */
    public EMPhase getActiveExperimentPhase() {

        EMPhase currentPhase = EMPhase.eEMUnknownPhase;

        if (started && expStateModel.isExperimentActive()) {
            currentPhase = expStateModel.getCurrentPhase();
        }

        return currentPhase;
    }

    /**
     * Use this method to attempt to advance the current phase of the active
     * experiment.
     *
     * @throws Exception - throws if there is no active experiment or there are
     * no more phases to move on to.
     */
    public void advanceExperimentPhase() throws Exception {

        // Safety first
        if (!started) {
            throw new Exception("Could not advance experiment phase: service not initialised");
        }
        if (!expStateModel.isExperimentActive()) {
            throw new Exception("Could not advance experiment phase: no active experiment");
        }

        try {

            expMonitor.goToNextPhase();
        } catch (Exception ex) {

            String problem = "Could not advance experiment phase: " + ex.getMessage();
            logger.error(problem);

            throw new Exception(problem, ex);
        }
    }

    /**
     * Use this method to retrieve the currently known connected clients. This
     * call will return an empty set when there are no clients or no active
     * experiment. IMPORTANT: the state of each client you find in this set will
     * be correct only at the point of calling.
     *
     * @return - Set of clients currently connected.
     */
    public Set<EMClient> getCurrentlyConnectedClients() {

        HashSet<EMClient> connectedClients = new HashSet<EMClient>();

        if (started) {

            Set<EMClient> clients = expMonitor.getAllConnectedClients();
            connectedClients.addAll(clients);
        }

        return connectedClients;
    }

    /**
     * Use this method to get an instance of a client specified by an ID.
     *
     * @param id - UUID of the client required.
     * @return - Client and its state at the point of calling
     * @throws Exception - throws if the client ID is invalid or service not
     * ready
     */
    public EMClient getClientByID(UUID id) throws Exception {

        if (!started) {
            throw new Exception("Cannot get client - service not initialised");
        }
        if (id == null) {
            throw new Exception("Cannot get client - ID is null");
        }

        return expMonitor.getClientByID(id);
    }

    /**
     * Use this call to send a 'deregister' message to a connected client. This
     * instruction informs the client that they should send a disconnection
     * message to the ECC and then disconnect from the Rabbit service.
     *
     * @param client - Client to send the deregister message.
     * @throws Exception - throws if the client is not known or is already
     * disconnected from the ECC
     */
    public void deregisterClient(EMClient client) throws Exception {

        if (!started) {
            throw new Exception("Cannot deregister client: service not initialised");
        }
        if (client == null) {
            throw new Exception("Could not deregister client:  client is null");
        }

        try {
            expMonitor.deregisterClient(client, "ECC service has requested de-registration");
        } catch (Exception ex) {

            String problem = "Had problems deregistering client " + client.getName() + ": " + ex.getMessage();
            logger.error(problem);

            throw new Exception(problem, ex);
        }
    }

    /**
     * Use this method to forcibly remove a client from the service. Note that
     * the experiment service cannot actually force a client's process to close
     * or disconnect from the RabbitMQ server. This action will only clean up
     * references to the client on the service side.
     *
     * @param client
     * @throws Exception
     */
    public void forceClientDisconnect(EMClient client) throws Exception {

        // Safety first
        if (!started) {
            throw new Exception("Cannot force client disconnection: service not initialised");
        }
        if (client == null) {
            throw new Exception("Cannot forcibly disconnect client: client is null");
        }

        try {
            logger.info("Trying to forcibly remove client " + client.getName() + " from service");

            expMonitor.forceClientDisconnection(client);
            liveMetricScheduler.removeClient(client);
        } catch (Exception ex) {
            logger.error("Could not forcibly remove client " + client.getName() + " because: " + ex.getMessage());
        }
    }

    // Private methods ---------------------------------------------------------
    private void processLiveMetricData(Report report) throws Exception {

        // Safety first
        if (!started) {
            throw new Exception("Cannot process live metric data: experiment service not initialised");
        }
        if (!expStateModel.isExperimentActive()) {
            throw new Exception("Cannot process live metric data: no currently active experiment");
        }
        if (report == null) {
            throw new Exception("Live monitoring metric: report is null");
        }

        // Check to see if we have anything useful store, and try store
        if (sanitiseMetricReport(report)) {
            // First get the EDM to save the measurements
            try {
                expReportAccessor.saveMeasurements(report);
            } catch (Exception ex) {
                throw ex;
            }
        }
    }

    private boolean sanitiseMetricReport(Report reportOUT) {

        // Check that we apparently have data
        Integer nom = reportOUT.getNumberOfMeasurements();

        if (nom == null || nom == 0) {
            logger.warn("Did not process metric report: measurement count = 0");
            return false;
        }

        // Make sure we have a valid measurement set
        MeasurementSet clientMS = reportOUT.getMeasurementSet();
        if (clientMS == null) {
            logger.warn("Did not process metric report: Measurement set is null");
            return false;
        }

        Metric metric = clientMS.getMetric();
        if (metric == null) {
            logger.warn("Did not process metric report: Metric is null");
            return false;
        }

        MetricType mt = metric.getMetricType();

        // Sanitise data based on full semantic info
        MeasurementSet cleanSet = new MeasurementSet(clientMS, false);

        // Run through each measurement checking that it is sane
        for (Measurement m : clientMS.getMeasurements()) {

            String val = m.getValue();

            switch (mt) {
                case NOMINAL:
                case ORDINAL:
                    if (val != null && !val.isEmpty()) {
                        cleanSet.addMeasurement(m);
                    }
                    break;

                case INTERVAL:
                case RATIO: {

                    if (val != null) {
                        try {
                            // Make sure we have a sensible number
                            Double dVal = Double.parseDouble(val);

                            if (!dVal.isNaN() && !dVal.isInfinite()) {
                                cleanSet.addMeasurement(m);
                            }
                        } catch (Exception ex) {
                            logger.warn("Caught NaN value in measurement: dropping");
                        }
                    }
                }
                break;
            }
        }

        // Use update report with clean measurement set
        reportOUT.setMeasurementSet(cleanSet);
        reportOUT.setNumberOfMeasurements(cleanSet.getMeasurements().size());

        return true;
    }

    private void processLivePROVData(EDMProvReport report) throws Exception {

        if (report == null) {
            throw new Exception("Could not process PROV report: report is null");
        }

        logger.debug("PROV data processing yet to be implemented");

    }

    // Private classes ---------------------------------------------------------
    private class ExpLifecycleListener implements IEMLifecycleListener {

        public ExpLifecycleListener() {
        }

        // IEMLifecycleListener ------------------------------------------------
        @Override
        public void onClientConnected(EMClient client, boolean reconnected) {

            logger.info("Client connected: " + client.getName() + (reconnected ? "[reconnection]" : "."));

            if (!client.isReRegistering()) {
                expStateModel.setClientConnectedState(client, true);
            }
        }

        @Override
        public void onClientDisconnected(UUID clientID) {

            EMClient client = expMonitor.getClientByID(clientID);

            if (client != null) {

                logger.info("Client " + client.getName() + " disconnected");

                // Stop scheduling metrics from this client
                if (liveMetricScheduler != null) {
                    try {
                        liveMetricScheduler.removeClient(client);
                    } catch (Exception ex) {
                        logger.warn("Client disconencted; metric scheduler says: " + ex.getMessage());
                    }
                }
            } else {
                logger.warn("Got a disconnection message from an unknown client");
            }
        }

        @Override
        public void onClientStartedPhase(EMClient client, EMPhase phase) {

            logger.info("Client " + client.getName() + " started phase " + phase.name());
        }

        @Override
        public void onLifecyclePhaseStarted(EMPhase phase) {

            logger.info("Experiment lifecycle phase " + phase.name() + " started");

            expStateModel.setCurrentPhase(phase);

            // Perform starting actions, as required
            switch (phase) {
                case eEMLiveMonitoring: {
                    liveMetricScheduler.start(expMonitor);
                }
                break;

                case eEMPostMonitoringReport: {
                    liveMetricScheduler.stop();
                }
                break;
            }
        }

        @Override
        public void onLifecyclePhaseCompleted(EMPhase phase) {

            logger.info("Experiment lifecycle phase " + phase.name() + " completed");
        }

        @Override
        public void onNoFurtherLifecyclePhases() {

            logger.info("No further experiment lifecycle phases");

            expStateModel.setCurrentPhase(EMPhase.eEMProtocolComplete);
        }

        @Override
        public void onLifecycleEnded() {

            logger.info("Experiment lifecycle has ended");

            liveMetricScheduler.stop();
            liveMetricScheduler.reset();

            try {
                expMonitor.resetLifecycle();
            } catch (Exception ex) {
                logger.error("Could not reset experiment lifecycle: " + ex.getMessage());
            }
        }

        @Override
        public void onFoundClientWithMetricGenerators(EMClient client, Set<MetricGenerator> newGens) {

            if (client != null && newGens != null) {

                // If client is re-registering, add the client to the connected view now
                // (we've definitely got a client that is still connected)
                if (client.isReRegistering()) {
                    logger.info("Known client connected: " + client.getID() + " (\"" + client.getName() + "\")");
                }

                // Pass on metric generators to the EDM for storage
                UUID expID = expStateModel.getActiveExperiment().getUUID();

                for (MetricGenerator mg : newGens) {

                    // Check metric generator has at least one entity
                    if (!MetricHelper.getAllEntities(mg).isEmpty()) {
                        try {

                            expMetGeneratorDAO.saveMetricGenerator(mg, expID);
                        } catch (Exception ex) {
                            logger.error("Failed to save metric generators for client " + client.getName() + ": " + ex.getMessage());
                        }
                    }
                }
            } else {
                logger.error("Received invalid metric generator event");
            }
        }

        @Override
        public void onClientEnabledMetricCollection(EMClient client, UUID entityID, boolean enabled) {

            if (client != null && entityID != null) {
                String msg = "Client " + client + " has " + (enabled ? "enabled" : "disabled")
                        + " metric collection for Entity ID: " + entityID.toString();

                logger.info(msg);
            } else {
                logger.error("Received invalid metric collection enabling message");
            }
        }

        @Override
        public void onClientSetupResult(EMClient client, boolean success) {

            if (client != null) {
                logger.info("Client " + client.getName() + " has completed set up");
            }

        }

        @Override
        public void onClientDeclaredCanPush(EMClient client) {

            if (client != null) {
                logger.info("Client " + client.getName() + " can push");
            }

        }

        @Override
        public void onClientDeclaredCanBePulled(EMClient client) {

            if (client != null) {

                if (expStateModel.getCurrentPhase().equals(EMPhase.eEMLiveMonitoring)) {

                    try {
                        liveMetricScheduler.addClient(client);
                    } catch (Exception ex) {
                        logger.error("Could not add pulling client to live monitoring: " + ex.getMessage());
                    }
                } else {
                    logger.warn("Client " + client.getName() + " trying to start pull process whilst not in Live monitoring");
                }
            } else {
                logger.warn("Got pull semantics from unknown client");
            }

        }

        @Override
        public void onGotMetricData(EMClient client, Report report) {

            if (client != null && report != null) {

                try {
                    processLiveMetricData(report);
                } catch (Exception ex) {

                    String problem = "Could not save measurements for client: "
                            + client.getName() + " because: " + ex.getMessage();

                    logger.error(problem);
                }
            }
        }

        @Override
        public void onGotPROVData(EMClient client, EDMProvReport report) {

            if (report != null) {
                try {
                    processLivePROVData(report);
                } catch (Exception ex) {

                    String problem = "Could not save provenance statement for client "
                            + client.getName() + " because: " + ex.getMessage();

                    logger.error(problem);
                }
            }
        }

        @Override
        public void onGotSummaryReport(EMClient client, EMPostReportSummary summary) {

            if (client != null && summary != null) {

                try {

                    expMonitor.getAllDataBatches(client);
                    logger.info("Requested missing metric data from " + client.getName());
                } catch (Exception ex) {
                    String problem = "Could not request missing metric data from "
                            + client + " because: " + ex.getMessage();

                    logger.error(problem);
                }
            } else {
                logger.error("Client " + client.getName() + " provided an empty summary report");
            }
        }

        @Override
        public void onGotDataBatch(EMClient client, EMDataBatch batch) {

            if (client != null && batch != null) {

                try {
                    expReportAccessor.saveReport(batch.getBatchReport(), true);
                } catch (Exception e) {
                    logger.error("Could not save batch data report: " + e.getMessage());
                }
            }
        }

        @Override
        public void onDataBatchMeasurementSetCompleted(EMClient client, UUID measurementSetID) {

            if (client != null && measurementSetID != null) {
                logger.info("Client " + client.getName() + " finished batching for measurement set: " + measurementSetID.toString());
            }

        }

        @Override
        public void onAllDataBatchesRequestComplete(EMClient client) {

            if (client != null) {
                logger.info("Client " + client.getName() + " has finished batching missing data");
            }
        }

        @Override
        public void onClientTearDownResult(EMClient client, boolean success) {

            if (client != null) {
                logger.info("Client " + client.getName() + " has finished tearing down");
            }
        }
    }

    private class LiveMetricsScheduleListener implements LiveMetricSchedulerListener {

        public LiveMetricsScheduleListener() {
        }

        // LiveMetricSchedulerListener -----------------------------------------
        @Override
        public void onIssuedClientMetricPull(EMClient client) {

            if (client != null) {
                logger.debug("Issued metric pull on client: " + client.getName());
            }
        }

        @Override
        public void onPullMetricFailed(EMClient client, String reason) {

            if (client != null) {
                if (reason == null) {
                    reason = "Unknown reason";
                }
                logger.debug("Did not pull client" + client.getName() + ": " + reason);
            }
        }
    }
}
