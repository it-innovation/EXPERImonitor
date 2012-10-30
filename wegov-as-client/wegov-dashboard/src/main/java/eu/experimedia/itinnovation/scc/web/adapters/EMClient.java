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
//	Created Date :			2012-09-10
//	Created for Project :           Experimedia
//
/////////////////////////////////////////////////////////////////////////
package eu.experimedia.itinnovation.scc.web.adapters;

import eu.wegov.web.security.WegovLoginService;
import java.util.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPBasicChannel;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.amqpAPI.impl.amqp.AMQPConnectionFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experiment.Experiment;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.*;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMDataBatch;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPhase;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.monitor.EMPostReportSummary;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.EMIAdapterListener;

@Service
public class EMClient implements EMIAdapterListener {

    private static final Logger clientLogger = Logger.getLogger(EMClient.class);
    private AMQPBasicChannel amqpChannel;
    private WeGovEMInterfaceAdapter emiAdapter;
    private String clientName;
    private UUID clientId;
    private Entity entityBeingObserved;
    private Attribute entityAttribute;
    private HashMap<UUID, MetricGenerator> metricGenerators = new HashMap<UUID, MetricGenerator>();
    private HashMap<UUID, Attribute> measurementSetsAndAttributes = new HashMap<UUID, Attribute>();
    private ArrayList<MeasurementSet> allMeasurementSets = new ArrayList<MeasurementSet>();
    private Measurement firstMeasurement;
    private Measurement currentMeasurement;
    private String numWidgetsAttributeUuid = "";
    private boolean dataPushEnabled = false;
    @Autowired
    @Qualifier("wegovLoginService")
    WegovLoginService loginService;

    public EMClient() {
        clientLogger.debug("Creating new EM client...");

        // Have to call this here because it's better if this
        // class is initialized by Spring so all autowiring works
        // TODO: move config etc. to bean definition
        try {
            this.start("127.0.0.1", UUID.fromString("00000000-0000-0000-0000-000000000000"), "WeGov 3.0 EM Client");
        } catch (Throwable ex) {
            throw new RuntimeException("Failed to start EM client", ex);
        }

        clientLogger.debug("Successfully created new EM client");
    }

    public UUID getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public String start(String rabbitServerIP, UUID expMonitorID, String clientName) throws Throwable {

        this.clientName = clientName;
        this.clientId = UUID.randomUUID();

        clientLogger.debug("Starting EM client with rabbitServerIP: " + rabbitServerIP + ", expMonitorID: " + expMonitorID.toString() + ", clientName: " + clientName + ", clientId: " + clientId.toString() + "...");

        AMQPConnectionFactory amqpFactory = new AMQPConnectionFactory();
        amqpFactory.setAMQPHostIPAddress(rabbitServerIP);

        clientLogger.debug("Connecting to AMQPHost...");
        try {
            amqpFactory.connectToAMQPHost();
            amqpChannel = amqpFactory.createNewChannel();
        } catch (Throwable e) {
            clientLogger.error("Failed to connect to AMQP Host and create new channel", e);
            throw e;
        }
        clientLogger.debug("Successfully connected to AMQPHost");

        emiAdapter = new WeGovEMInterfaceAdapter(this);

        clientLogger.debug("Registering with EM...");
        try {
            emiAdapter.registerWithEM(clientName, amqpChannel, expMonitorID, clientId);
        } catch (Throwable e) {
            clientLogger.error("WeGov EM Client failed to register with EM", e);
            throw e;
        }

        clientLogger.debug("Successfully registered with EM");
        clientLogger.debug("Successfully started EM client with clientId: " + clientId.toString());

        return clientId.toString();
    }

    public void onEMConnectionResult(boolean connected) {

        clientLogger.debug("onEMConnectionResult");

        if (connected) {
            clientLogger.debug("Connected to EM");
        } else {
            clientLogger.error("EM connection failure");
            throw new RuntimeException("Failed to connected to EM");
        }
    }

    public void onPopulateMetricGeneratorInfo() {

        clientLogger.debug("onPopulateMetricGeneratorInfo");

        MetricGenerator mg = makeWegovMetricGenerator();
        metricGenerators.put(mg.getUUID(), mg);

        HashSet<MetricGenerator> mgSet = new HashSet<MetricGenerator>();
        mgSet.addAll(metricGenerators.values());
        emiAdapter.setMetricGenerators(mgSet);

        clientLogger.debug("Returning generated metric generators:");
        for (MetricGenerator tempMg : mgSet) {
            clientLogger.debug("\t- " + tempMg.getUUID().toString() + ": " + tempMg.getName() + " (" + tempMg.getDescription() + ")");
        }
    }

    public void onSetupMetricGenerator(UUID generatorSetID, Boolean[] resultOUT) {
        clientLogger.debug("onSetupMetricGenerator, generatorSetID: " + generatorSetID.toString());

        // Just signal that the metric generator is ready
        resultOUT[0] = true;
    }

    public void onStartPushingMetricData() {
        clientLogger.debug("onStartPushingMetricData");
        dataPushEnabled = true;
//        pushData("data");
    }

    public void pushData(String value) {
        if (dataPushEnabled) {
            MeasurementSet measurementSet = allMeasurementSets.get(0);
            clientLogger.debug("Starting to push data for measurement set: [" + measurementSet.getUUID().toString() + "] " + measurementSet.getMetric().getUnit().getName() + " (" + measurementSet.getMetric().getMetricType().name() + ")");

            Measurement theMeasurement = new Measurement();

            theMeasurement.setMeasurementSetUUID(measurementSet.getUUID());
            theMeasurement.setTimeStamp(new Date());

            theMeasurement.setValue(value);

            measurementSet.addMeasurement(theMeasurement);

            Date date = new Date();
            Report sampleReport = new Report();

            sampleReport.setMeasurementSet(measurementSet);
            sampleReport.setReportDate(date);
            sampleReport.setFromDate(date);
            sampleReport.setToDate(date);
            sampleReport.setNumberOfMeasurements(1);

            emiAdapter.pushMetric(sampleReport);
            clientLogger.debug("Finished pushing data for measurement set: [" + measurementSet.getUUID().toString() + "] " + measurementSet.getMetric().getUnit().getName() + " (" + measurementSet.getMetric().getMetricType().name() + ")");
        } else {
            clientLogger.error("Data pushing is disabled, please check what phase you are in. Pushing only available in Live Monitoring phase");
        }
    }

    public void onLastPushProcessed(UUID lastReportID) {
        clientLogger.debug("onLastPushProcessed, lastReportID: " + lastReportID.toString());
    }

    public void onStopPushingMetricData() {
        dataPushEnabled = false;
        clientLogger.debug("onStopPushingMetricData");
    }

    public void onPullMetric(UUID measurementSetID, Report reportOUT) {
//        logger.debug("onPullMetric, measurementSetID: " + measurementSetID.toString());

        Iterator it = metricGenerators.keySet().iterator();

        UUID mgUUID;
        MetricGenerator mg;
        MeasurementSet theMeasurementSet = new MeasurementSet();
        boolean foundMatchingMeasurementSet = false;
        while (it.hasNext()) {
            mgUUID = (UUID) it.next();
            mg = metricGenerators.get(mgUUID);
//            logger.debug("\t- Metric generator [" + mg.getUUID().toString() + "] " + mg.getName() + " (" + mg.getDescription() + ")");

            for (MetricGroup mGroup : mg.getMetricGroups()) {
                for (MeasurementSet mSet : mGroup.getMeasurementSets()) {
                    if (mSet.getUUID().toString().equals(measurementSetID.toString())) {
//                        logger.debug("Found requested measurement set: [" + mSet.getUUID().toString() + "] " + mSet.getMetric().getUnit().getName() + " (" + mSet.getMetric().getMetricType().name() + ")");
                        theMeasurementSet = mSet;
                        foundMatchingMeasurementSet = true;
                    }
                }
            }
        }

        if (!foundMatchingMeasurementSet) {
            throw new RuntimeException("Measurement set with ID: " + measurementSetID.toString() + " does not exist for client with ID: " + clientId + " (" + clientName + ")");
        } else {

//            it = measurementSetsAndAttributes.keySet().iterator();
//
//            UUID msUUID;
//            Attribute msAttribute;
//            while(it.hasNext()) {
//                msUUID = (UUID) it.next();
//                msAttribute = measurementSetsAndAttributes.get(mg)
//            }

            Attribute theAttribute = measurementSetsAndAttributes.get(theMeasurementSet.getUUID());

            clientLogger.debug("onPullMetric, matching attribute: [" + theAttribute.getUUID().toString() + "] " + theAttribute.getName() + " (" + theAttribute.getDescription() + ")");

            // Only report the number of widgets for now
            if (numWidgetsAttributeUuid.equals(theAttribute.getUUID().toString())) {

                clientLogger.debug("Reporting the number of widgets...");

                // Get number of widgets from wegov, it's ok if we fail
                int numberOfWidgets = 0;
                try {
//                    ClassPathResource resource = new ClassPathResource("application-services.xml");
//                    BeanFactory factory = new XmlBeanFactory(resource);
//                    WegovLoginService loginService = (WegovLoginService) factory.getBean("wegovLoginService");
                    numberOfWidgets = loginService.getTotalNumberOfWidgets();
                } catch (Throwable ex) {
                    throw new RuntimeException("Failed to return number of widgets from WeGov", ex);
                }

                clientLogger.debug("Found " + numberOfWidgets + " widgets");

                Measurement theMeasurement = new Measurement();

                theMeasurement.setMeasurementSetUUID(theMeasurementSet.getUUID());
                theMeasurement.setTimeStamp(new Date());

                theMeasurement.setValue(Integer.toString(numberOfWidgets));

                theMeasurementSet.addMeasurement(theMeasurement);

                Date date = new Date();
                reportOUT.setReportDate(date);
                reportOUT.setFromDate(date);
                reportOUT.setToDate(date);
                reportOUT.setMeasurementSet(theMeasurementSet);
                reportOUT.setNumberOfMeasurements(1);

                clientLogger.debug("Finished reporting the number of widgets");
            }
        }


    }

    private Measurement createTestMeasurementForMeasurementSet(MeasurementSet measurementSet) {
        clientLogger.debug("Creating new measurement for measurement set with ID: " + measurementSet.getUUID().toString() + " ...");

        Runtime rt = Runtime.getRuntime();
        String memVal = Long.toString(rt.totalMemory() - rt.freeMemory());

        Measurement theMeasurement = new Measurement();

        theMeasurement.setMeasurementSetUUID(measurementSet.getUUID());
        theMeasurement.setTimeStamp(new Date());
        theMeasurement.setValue(memVal);

        clientLogger.debug("Successfully created new measurement for measurement set with ID: " + measurementSet.getUUID().toString() + ", measurement value: " + memVal);

        return theMeasurement;
    }

    public void onPopulateSummaryReport(EMPostReportSummary summaryOUT) {
        clientLogger.debug("onPopulateSummaryReport");
    }

    public void onPopulateDataBatch(EMDataBatch batchOut) {
        clientLogger.debug("onPopulateDataBatch");
    }

    public void onGetTearDownResult(Boolean[] resultOUT) {

        clientLogger.debug("onGetTearDownResult");
        resultOUT[0] = true;
    }

    public MetricGenerator makeWegovMetricGenerator() {
        clientLogger.debug("makeWegovMetricGenerator");

        MetricGenerator theMetricGenerator = new MetricGenerator();
        theMetricGenerator.setName("Wegov Metric Generator");
        theMetricGenerator.setDescription("Metric generator for WeGov Social Analytics Dashboard");

        MetricGroup wegovMetricGroup = new MetricGroup();
        wegovMetricGroup.setName("Wegov Metric Group");
        wegovMetricGroup.setDescription("Metric group for all WeGov Social Analytics Dashboard metrics");
        wegovMetricGroup.setMetricGeneratorUUID(theMetricGenerator.getUUID());
        theMetricGenerator.addMetricGroup(wegovMetricGroup);

        Entity twitterSchladming = new Entity();
        twitterSchladming.setName("Schladming Twitter Group");
        twitterSchladming.setDescription("People found in Schladming Twitter search for keyword Schladming");
        theMetricGenerator.addEntity(twitterSchladming);

        // NUMBER OF PEOPLE
        Attribute twitterSchladmingNumPeople = new Attribute();
        twitterSchladmingNumPeople.setName("Number of people");
        twitterSchladmingNumPeople.setDescription("Number of people who tweeted about Schladming");
        twitterSchladmingNumPeople.setEntityUUID(twitterSchladming.getUUID());
        twitterSchladming.addAttribute(twitterSchladmingNumPeople);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingNumPeople, MetricType.INTERVAL, new Unit("Person"));

        // NUMBER OF TWEETS
        Attribute twitterSchladmingNumTweets = new Attribute();
        twitterSchladmingNumTweets.setName("Number of tweets");
        twitterSchladmingNumTweets.setDescription("Number of tweets collected about Schladming");
        twitterSchladmingNumTweets.setEntityUUID(twitterSchladming.getUUID());
        twitterSchladming.addAttribute(twitterSchladmingNumTweets);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingNumTweets, MetricType.INTERVAL, new Unit("Tweet"));

        // AVERAGE NUMBER OF TWEETS PER MINUTE
        Attribute twitterSchladmingAverageNumTweetsperMinute = new Attribute();
        twitterSchladmingAverageNumTweetsperMinute.setName("Average number of tweets per minute");
        twitterSchladmingAverageNumTweetsperMinute.setDescription("Average number of tweets collected about Schladming per minute");
        twitterSchladmingAverageNumTweetsperMinute.setEntityUUID(twitterSchladming.getUUID());
        twitterSchladming.addAttribute(twitterSchladmingAverageNumTweetsperMinute);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingAverageNumTweetsperMinute, MetricType.RATIO, new Unit("Tweets per minute"));

        // TOPIC ANALYSIS TOPIC #1
        Attribute twitterSchladmingDiscussionTopic1 = new Attribute();
        twitterSchladmingDiscussionTopic1.setName("Topic analysis #1");
        twitterSchladmingDiscussionTopic1.setDescription("First topic of discussion on Twitter about Schladming");
        twitterSchladmingDiscussionTopic1.setEntityUUID(twitterSchladming.getUUID());
        twitterSchladming.addAttribute(twitterSchladmingDiscussionTopic1);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingDiscussionTopic1, MetricType.NOMINAL, new Unit("Keyword"));

        // TOPIC ANALYSIS TOPIC #2
        Attribute twitterSchladmingDiscussionTopic2 = new Attribute();
        twitterSchladmingDiscussionTopic2.setName("Topic analysis #2");
        twitterSchladmingDiscussionTopic2.setDescription("Second topic of discussion on Twitter about Schladming");
        twitterSchladmingDiscussionTopic2.setEntityUUID(twitterSchladming.getUUID());
        twitterSchladming.addAttribute(twitterSchladmingDiscussionTopic2);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingDiscussionTopic2, MetricType.NOMINAL, new Unit("Keyword"));

        // TOPIC ANALYSIS TOPIC #3
        Attribute twitterSchladmingDiscussionTopic3 = new Attribute();
        twitterSchladmingDiscussionTopic3.setName("Topic analysis #3");
        twitterSchladmingDiscussionTopic3.setDescription("Third topic of discussion on Twitter about Schladming");
        twitterSchladmingDiscussionTopic3.setEntityUUID(twitterSchladming.getUUID());
        twitterSchladming.addAttribute(twitterSchladmingDiscussionTopic3);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingDiscussionTopic3, MetricType.NOMINAL, new Unit("Keyword"));

        // BROADCASTERS
        Attribute twitterSchladmingBroadcasterRoleNumPeople = new Attribute();
        twitterSchladmingBroadcasterRoleNumPeople.setName("Broadcaster role representation");
        twitterSchladmingBroadcasterRoleNumPeople.setDescription("Number of people identified as Broadcaster by Role analysis");
        twitterSchladmingBroadcasterRoleNumPeople.setEntityUUID(twitterSchladming.getUUID());
        twitterSchladming.addAttribute(twitterSchladmingBroadcasterRoleNumPeople);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingBroadcasterRoleNumPeople, MetricType.INTERVAL, new Unit("Person"));

        // DAILY USERS
        Attribute twitterSchladmingDailyUserRoleNumPeople = new Attribute();
        twitterSchladmingDailyUserRoleNumPeople.setName("Daily user role representation");
        twitterSchladmingDailyUserRoleNumPeople.setDescription("Number of people identified as Daily users by Role analysis");
        twitterSchladmingDailyUserRoleNumPeople.setEntityUUID(twitterSchladming.getUUID());
        twitterSchladming.addAttribute(twitterSchladmingDailyUserRoleNumPeople);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingDailyUserRoleNumPeople, MetricType.INTERVAL, new Unit("Person"));

        // INFORMATION SEEKERS
        Attribute twitterSchladmingInformationSeekerRoleNumPeople = new Attribute();
        twitterSchladmingInformationSeekerRoleNumPeople.setName("Information Seeker role representation");
        twitterSchladmingInformationSeekerRoleNumPeople.setDescription("Number of people identified as Information seekers by Role analysis");
        twitterSchladmingInformationSeekerRoleNumPeople.setEntityUUID(twitterSchladming.getUUID());
        twitterSchladming.addAttribute(twitterSchladmingInformationSeekerRoleNumPeople);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingInformationSeekerRoleNumPeople, MetricType.INTERVAL, new Unit("Person"));

        // INFORMATION SOURCES
        Attribute twitterSchladmingInformationSourceRoleNumPeople = new Attribute();
        twitterSchladmingInformationSourceRoleNumPeople.setName("Information Source role representation");
        twitterSchladmingInformationSourceRoleNumPeople.setDescription("Number of people identified as Information sources by Role analysis");
        twitterSchladmingInformationSourceRoleNumPeople.setEntityUUID(twitterSchladming.getUUID());
        twitterSchladming.addAttribute(twitterSchladmingInformationSourceRoleNumPeople);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingInformationSourceRoleNumPeople, MetricType.INTERVAL, new Unit("Person"));

        // RARE POSTERS
        Attribute twitterSchladmingRarePosterRoleNumPeople = new Attribute();
        twitterSchladmingRarePosterRoleNumPeople.setName("Rare Poster role representation");
        twitterSchladmingRarePosterRoleNumPeople.setDescription("Number of people identified as Rare posters by Role analysis");
        twitterSchladmingRarePosterRoleNumPeople.setEntityUUID(twitterSchladming.getUUID());
        twitterSchladming.addAttribute(twitterSchladmingRarePosterRoleNumPeople);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, twitterSchladmingRarePosterRoleNumPeople, MetricType.INTERVAL, new Unit("Person"));


        Entity wegovUsers = new Entity();
        wegovUsers.setName("Users of Wegov Dashboard");
        wegovUsers.setDescription("People using Wegov Dashboard");
        theMetricGenerator.addEntity(wegovUsers);

        // NUMBER OF PEOPLE USING WEGOV
        Attribute wegovUsersNumPeople = new Attribute();
        wegovUsersNumPeople.setName("Number of people");
        wegovUsersNumPeople.setDescription("Number of people who are using Wegov dashboard");
        wegovUsersNumPeople.setEntityUUID(wegovUsers.getUUID());
        wegovUsers.addAttribute(wegovUsersNumPeople);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, wegovUsersNumPeople, MetricType.INTERVAL, new Unit("Person"));

        // TOTAL NUMBER OF WIDGETS
        Attribute wegovUsersTotalNumWidgets = new Attribute();
        wegovUsersTotalNumWidgets.setName("Number of widgets");
        wegovUsersTotalNumWidgets.setDescription("Total number of widgets created by people who are using Wegov dashboard");
        wegovUsersTotalNumWidgets.setEntityUUID(wegovUsers.getUUID());
        wegovUsers.addAttribute(wegovUsersTotalNumWidgets);
        addMetricToAttributeAndMetricGroup(wegovMetricGroup, wegovUsersTotalNumWidgets, MetricType.INTERVAL, new Unit("Widget"));
        numWidgetsAttributeUuid = wegovUsersTotalNumWidgets.getUUID().toString();

        return theMetricGenerator;
    }

    private void addMetricToAttributeAndMetricGroup(MetricGroup metricGroup, Attribute attribute, MetricType metricType, Unit metricUnit) {
        MeasurementSet theMeasuringSet = new MeasurementSet();
        theMeasuringSet.setMetricGroupUUID(metricGroup.getUUID());
        theMeasuringSet.setAttributeUUID(attribute.getUUID());
        metricGroup.addMeasurementSets(theMeasuringSet);

        allMeasurementSets.add(theMeasuringSet);
        measurementSetsAndAttributes.put(theMeasuringSet.getUUID(), attribute);

        Metric theMetric = new Metric();
        theMetric.setMetricType(metricType);
        theMetric.setUnit(metricUnit);
        theMeasuringSet.setMetric(theMetric);
    }

    public void onPushReportReceived(UUID lastReportID) {
        clientLogger.debug("onPushReportReceived, lastReportID: " + lastReportID);
    }

    public void onPullReportReceived(UUID reportID) {
        clientLogger.debug("onPullReportReceived, reportID: " + reportID);
    }

    public void onDiscoveryTimeOut() {
        clientLogger.debug("onDiscoveryTimeOut");
    }

    public void onSetupTimeOut(UUID metricGeneratorID) {
        clientLogger.debug("onSetupTimeOut, metricGeneratorID: " + metricGeneratorID);
    }

    public void onPullMetricTimeOut(UUID measurementSetID) {
        clientLogger.debug("onPullMetricTimeOut, measurementSetID: " + measurementSetID);
    }

    public void onReportBatchTimeOut(UUID batchID) {
        clientLogger.debug("onReportBatchTimeOut, batchID: " + batchID);
    }

    public void onTearDownTimeOut() {
        clientLogger.debug("onTearDownTimeOut");
    }

    public void onEMConnectionResult(boolean connected, Experiment expInfo) {
        clientLogger.debug("onEMConnectionResult");
        if (connected) {
            if (expInfo != null)
                clientLogger.debug("Connected to EM, linked to experiment: " + expInfo.getName());
            else
                clientLogger.error("Connected to EM, linked to NULL experiment");
        } else {
            clientLogger.error("Refused connection to EM");
        }
    }

    @Override
    public void onEMDeregistration(String reason) {
        clientLogger.debug("Got disconnected from EM with reason: " + reason);
        try {
            emiAdapter.disconnectFromEM();
        } catch (Exception e) {
            clientLogger.error("Had problems disconnecting from EM: " + e.getMessage());
        }
    }

    @Override
    public void onDescribePushPullBehaviours(Boolean[] pushPullOUT) {
        clientLogger.debug("onDescribePushPullBehaviours");
        pushPullOUT[0] = true;
        pushPullOUT[1] = true;
    }

    @Override
    public void onDescribeSupportedPhases(EnumSet<EMPhase> phasesOUT) {
        clientLogger.debug("onDescribeSupportedPhases");

        phasesOUT.add(EMPhase.eEMSetUpMetricGenerators);
        phasesOUT.add(EMPhase.eEMLiveMonitoring);
        phasesOUT.add(EMPhase.eEMPostMonitoringReport);
        phasesOUT.add(EMPhase.eEMTearDown);
    }

    @Override
    public void onLiveMonitoringStarted() {
        clientLogger.debug("onLiveMonitoringStarted");
    }

    @Override
    public void onPullingStopped() {
        clientLogger.debug("onPullingStopped");
    }
}
