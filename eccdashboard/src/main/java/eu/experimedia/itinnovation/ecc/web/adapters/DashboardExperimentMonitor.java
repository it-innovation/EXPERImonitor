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
import eu.experimedia.itinnovation.ecc.web.data.EccPropertiesAsJson;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.apache.log4j.Logger;
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
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IMetricGeneratorDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.spec.mon.dao.IReportDAO;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.factory.EMInterfaceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.IEMLifecycleListener;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.em.spec.workflow.IExperimentMonitor;

public class DashboardExperimentMonitor implements IEMLifecycleListener {

    private final Logger logger = Logger.getLogger(DashboardExperimentMonitor.class);
    private IExperimentMonitor expMonitor;
    private boolean waitingToStartNextPhase = false;
    private IMonitoringEDM expDataMgr;
    private IMetricGeneratorDAO expMGAccessor;
    private IReportDAO expReportAccessor;
    private Experiment expInstance;
    private HashMap<String, DashboardMeasurementSet> reportedMeasurementSets = new HashMap<String, DashboardMeasurementSet>();
    private HashMap<String, DashboardSummarySet> summarySets = new HashMap<String, DashboardSummarySet>();
    private ArrayList<String> measurementSetsWaitingForData = new ArrayList<String>();
    private Properties eccdashboard;

    public DashboardExperimentMonitor() {

        expMonitor = EMInterfaceFactory.createEM();
        expMonitor.addLifecyleListener(this);

        try {
            // Get ECC Dashboard properties - can do without nagios
            File currentDir = new File(".");
            logger.warn("Initialising DashboardExperimentMonitor, path is: " + currentDir.getAbsolutePath());
            if ( (new File("eccdashboard.properties")).exists() ) {
                eccdashboard = tryGetPropertiesFile("eccdashboard");
                String error = verifyProperties(eccdashboard);
                if (error != null)
                    logger.warn("Error processing eccdashboard.properties: \'" + error + "\', will not connect to nagios ");
            } else {
                logger.warn("Eccdashboard.properties file was not found, will not connect to nagios");
            }
            
            File edmPropertiesFile = new File("edm.properties");
            File emPropertiesFile = new File("em.properties");
            
            if (!edmPropertiesFile.exists())
                throw new RuntimeException("EDM properties file is missing");

            if (!emPropertiesFile.exists())
                throw new RuntimeException("EM properties file is missing");
            
            InputStream edmPropsStream = (InputStream) new FileInputStream(edmPropertiesFile);
            Properties edmProps = new Properties();
            edmProps.load(edmPropsStream);
            
            if (edmProps == null)
                throw new RuntimeException("Failed to read EDM properties, edmProps == null");
            
            logger.debug("Loaded EDM properties:");
            Iterator it = edmProps.keySet().iterator();
            String key, value;
            while(it.hasNext()) {
                key = (String) it.next();
                value = edmProps.getProperty(key);
                logger.debug("\t- " + key + " : " + value);
            }
            
            InputStream emPropsStream = (InputStream) new FileInputStream(emPropertiesFile);
            Properties emProps = new Properties();
            emProps.load(emPropsStream);
            
            logger.debug("Loaded EM properties:");
            it = emProps.keySet().iterator();
            while(it.hasNext()) {
                key = (String) it.next();
                value = emProps.getProperty(key);
                logger.debug("\t- " + key + " : " + value);
            }            
            
            logger.debug("Creating MonitoringEDM");
            expDataMgr = EDMInterfaceFactory.getMonitoringEDM(edmProps);
            
            logger.debug("Connecting to EM");
            start(emProps);
            

            // Try getting the EDM properties from a local file
//            Properties edmProps = tryGetPropertiesFile("edm");

            // If available, use these properties
//            if (edmProps != null) {
//                expDataMgr = EDMInterfaceFactory.getMonitoringEDM(edmProps);
//            }
//            else {
//                expDataMgr = EDMInterfaceFactory.getMonitoringEDM(); //... or go to default
//            }

            // Try starting from a local EM properties file
//            Properties emProps = tryGetPropertiesFile("em");
//            if (emProps != null) {
//                start(emProps);
//            } else {
//                throw new RuntimeException("Failed to find configuration files");
//            }

        } catch (Throwable ex) {
            throw new RuntimeException("Could not create Monitoring EDM", ex);
        }
    }

    public IMonitoringEDM getExpDataMgr() {
        return expDataMgr;
    }

    private String verifyProperties(Properties properties) {
        String result = null;

        String[] mustHaveProperties = new String[]{
          "nagios.fullurl"};

        for (String mustHaveproperty : mustHaveProperties) {
            if (!properties.containsKey(mustHaveproperty)) {
                result = "Missing key: " + mustHaveproperty;
                break;
            } else {
                if (properties.getProperty(mustHaveproperty).equals("")) {
                    result = "Missing property for key: " + mustHaveproperty;
                    break;
                }
            }
        }

        return result;

    }
    
    public EccPropertiesAsJson getEccProperties() {
        if (eccdashboard != null)
            return new EccPropertiesAsJson(eccdashboard.getProperty("nagios.fullurl"));
        else
            return null;
    }

    // Private methods -----------------------------------------------------------
    private Properties tryGetPropertiesFile(String configName) {
        Properties props = null;
        InputStream propsStream = null;

        // Try find the properties file
        File propFile = new File(configName + ".properties");
        if (propFile.exists()) {
            try {
                propsStream = (InputStream) new FileInputStream(propFile);
            } catch (IOException ioe) {
                logger.error("Could not open " + configName + " configuration file");
            }
        }

        // Try load the property stream
        if (propsStream != null) {
            props = new Properties();
            try {
                props.load(propsStream);
            } catch (IOException ioe) {
                logger.error("Could not load " + configName + " configuration");
                props = null;
            }
        }

        // Tidy up
        if (propsStream != null) {
            try {
                propsStream.close();
            } catch (IOException ioe) {
                logger.error("Could not close " + configName + " config file");
            }
        }

        return props;
    }

    private boolean clearECCEDM() {
        boolean clearedOK = false;

        if (expDataMgr != null) {
            try {
                expDataMgr.clearMetricsDatabase();
                clearedOK = true;
            } catch (Exception e) {
                logger.error("Could not clear EDM database: " + e.getLocalizedMessage());
            }
        }

        return clearedOK;
    }

    private void start(Properties emProps) throws Exception {
        logger.info("Trying to connect to Rabbit server");

        try {
            expMonitor.openEntryPoint(emProps);

        } catch (Exception e) {
            logger.error("Could not open entry point on Rabbit server");
            throw e;
        }

        boolean dmOK = createExperiment();

        if (!dmOK) {
            logger.error("Had problems setting up the EDM");
            throw new Exception("Could not set up EDM");
        }
    }

    private boolean createExperiment() {
        boolean result = false;

        if (expDataMgr == null) {
            logger.error("EDM not created");
            return false;
        }

        Date expDate = new Date();
        expInstance = new Experiment();
        expInstance.setName(UUID.randomUUID().toString());
        expInstance.setDescription("Sample ExperimentMonitor based experiment");
        expInstance.setStartTime(expDate);
        expInstance.setExperimentID(expDate.toString());

        // If we have a working EDM, set up the EDM interfaces
        if (expDataMgr.isDatabaseSetUpAndAccessible()) {
            try {
                expMGAccessor = expDataMgr.getMetricGeneratorDAO();
                expReportAccessor = expDataMgr.getReportDAO();

                IExperimentDAO expDAO = expDataMgr.getExperimentDAO();
                expDAO.saveExperiment(expInstance);
                result = true;
            } catch (Exception e) {
                logger.error("Could not initialise experiment");
            }
        } else {
            logger.error("Could not access EDM database");
        }

        return result;
    }

    public EMPhase getCurrentPhase() {
        EMPhase currentPhase = expMonitor.getCurrentPhase();
        logger.debug("Returning current phase: [" + currentPhase.getIndex() + "] " + currentPhase.name());
        EMPhase nextPhase = expMonitor.getNextPhase();
        logger.debug("The next phase will be: [" + nextPhase.getIndex() + "] " + nextPhase.name());
        return currentPhase;
    }

    public void goToNextPhase() throws Exception {
        expMonitor.goToNextPhase();
    }

    public Set<EMClient> getAllConnectedClients() {
        return expMonitor.getAllConnectedClients();
    }

    public Set<EMClient> getCurrentPhaseClients() {
        return expMonitor.getCurrentPhaseClients();
    }

    public EMPhase startLifecycle(Experiment expInfo) throws Exception {
        EMPhase currentPhase = expMonitor.startLifecycle(expInfo);
        logger.debug("Started the lifecycle with the phase: [" + currentPhase.getIndex() + "] " + currentPhase.name());
        EMPhase nextPhase = expMonitor.getNextPhase();
        logger.debug("The next phase will be: [" + nextPhase.getIndex() + "] " + nextPhase.name());
        return currentPhase;
    }

    public LinkedHashMap<String, DataPoint> getMeasurementsForMeasurementSet(String measurementSetUuid) {
        if (reportedMeasurementSets.containsKey(measurementSetUuid)) {

            DashboardMeasurementSet theMeasurementSet = reportedMeasurementSets.get(measurementSetUuid);

            EMPhase thePhase = expMonitor.getCurrentPhase();

            // If it's live monitoring phase, start pulling data from clients
            if (thePhase.getIndex() == 3) {

                // Get all clients for the live monitoring phase
                Set<EMClient> currentPhaseClients = expMonitor.getCurrentPhaseClients();

                Iterator<EMClient> currentPhaseClientsIterator = currentPhaseClients.iterator();

                EMClient currentPhaseClient;
                while (currentPhaseClientsIterator.hasNext()) {
                    currentPhaseClient = currentPhaseClientsIterator.next();

                    // Get only ones that are not busy generating data
                    if (!currentPhaseClient.isPullingMetricData()) {
                        try {
                            if (!measurementSetsWaitingForData.contains(measurementSetUuid)) {
                                logger.debug("Pulling metrics from client: [" + currentPhaseClient.getID() + "], measurement set [" + measurementSetUuid + "]");
                                expMonitor.pullMetric(currentPhaseClient, UUID.fromString(measurementSetUuid));
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

            EMPhase thePhase = expMonitor.getCurrentPhase();

            // If it's live monitoring phase, start pulling data from clients
            if (thePhase.getIndex() == 3) {

                // Get all clients for the live monitoring phase
                Set<EMClient> currentPhaseClients = expMonitor.getCurrentPhaseClients();

                Iterator<EMClient> currentPhaseClientsIterator = currentPhaseClients.iterator();

                EMClient currentPhaseClient;
                while (currentPhaseClientsIterator.hasNext()) {
                    currentPhaseClient = currentPhaseClientsIterator.next();

                    // Get only ones that are not busy generating data
                    if (!currentPhaseClient.isPullingMetricData()) {
                        try {
                            logger.debug("Pulling metrics from client: [" + currentPhaseClient.getID() + "] " + currentPhaseClient.getName());
                            expMonitor.pullAllMetrics(currentPhaseClient);

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

    // IEMLifecycleListener ------------------------------------------------------
    @Override
    public void onClientConnected(EMClient client) {
        logger.debug("Client connected: " + client.getName() + " [" + client.getID().toString() + "]");

    }

    @Override
    public void onClientDisconnected(EMClient client) {
        logger.debug("Client disconnected: " + client.getName() + " [" + client.getID().toString() + "]");

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
    public void onClientDeclaredCanPush(EMClient client) {
      logger.debug("onClientDeclaredCanPush");
    }
    
    @Override
    public void onClientDeclaredCanBePulled(EMClient client) {
      logger.debug("onClientDeclaredCanBePulled");
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

                    if (measurementSetsWaitingForData.contains(measurementSet.getUUID().toString())) {
                        measurementSetsWaitingForData.remove(measurementSet.getUUID().toString());
                    }

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
                                while (itCurrent.hasNext()) {
                                    currentMeasurementUUID = (String) itCurrent.next();
                                    currentDatapoint = currentMeasurements.get(currentMeasurementUUID);
                                    if ((currentDatapoint.getTime() == measurementTimestamp.getTime()) && (currentDatapoint.getValue().equals(measurementValue))) {
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
}
