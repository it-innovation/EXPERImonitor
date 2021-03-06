/////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2014
//
// Copyright in this software belongs to University of Southampton
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
//      Created By :            Stefanie Wiegand
//      Created Date :          2014-07-17
//      Created for Project :   Experimedia
//
/////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.experimentSimulation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.zip.DataFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experimedia.Application;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experimedia.Content;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experimedia.ExperimediaFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experimedia.Participant;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.experimedia.Service;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Attribute;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MeasurementSet;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Metric;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricGroup;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricHelper;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.MetricType;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Unit;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.samples.shared.ECCSimpleLogger;

public class ExperimentDataGenerator {

    //how many seconds to wait until reading the next log. Set to 0 for max speed
    public static final int SPEED = 1;

    private LinkedList<String> rawlog = new LinkedList<String>();
    private String logname;
    private String logclass;
    private ILog currentLog;

    private final ExperimediaFactory factory = new ExperimediaFactory("eee", "http://experimedia.eu/ontologies/ExperimediaExperimentExplorer#");
    private ECCSimpleLogger eccLogger = new ECCSimpleLogger();

    private ArrayList<String> genericAttributes = new ArrayList<String>();
    private ArrayList<ArrayList<String>> ordinalValues = new ArrayList<ArrayList<String>>();

    //services
    Service twitterService;
    Service weatherService;
    Service lwtService;

    // hacky variables
    Long lastAppEventTime = null;

    //other objects that remain the same for the whole duration of the log
    Random rand = new Random();
    Participant participant;
    Application app;
    String agentName;

    private static final Logger logger = LoggerFactory.getLogger(ExperimentDataGenerator.class);

    public ExperimentDataGenerator() {

        logger.info("Starting ExperimentDataGenerator");

    }

    /**
     * Initialise the generator so it can start parsing logfiles in the next
     * step
     *
     * @param logfile the logfile to read
     * @param logClass the type of log
     * @param eccLogger the link to metric land :)
     */
    public void init(String logfile, String logClass, ECCSimpleLogger eccLogger) {
        this.eccLogger = eccLogger;
        //read log into memory
        readLog(logfile, logClass);

        prepareParsing();

        this.eccLogger = eccLogger;
    }

    /**
     * Reads the logfile and saves contents in memory
     *
     * @param logfile the input logfile; name only as it is read from classpath
     */
    private void readLog(String logfile, String logClass) {
        if (logfile == null) {
            return;
        }
        try {
            String logfilePath = ExperimentDataGenerator.class.getClassLoader().getResource(logfile).getPath();
            FileReader fr = new FileReader(new File(logfilePath));
            BufferedReader br = new BufferedReader(fr);

            String line;
            while ((line = br.readLine()) != null) {
                rawlog.add(line.trim());
            }
            this.logname = logfile.split("\\.")[0];
            this.logclass = logClass;
        } catch (IOException e) {
            logger.error("Error reading from logfile " + logfile, e);
        }
    }

    /**
     * Prepare parsing by creating all the necessary objects that remain the
     * same for the whole log
     */
    private void prepareParsing() {

        try {
            //init factory
            factory.getProvFactory().addOntology("foaf", "http://xmlns.com/foaf/0.1/");
            factory.getProvFactory().addOntology("sioc", "http://rdfs.org/sioc/ns#");
            factory.getProvFactory().addOntology("eee", "http://experimedia.eu/ontologies/ExperimediaExperimentExplorer#");

            //prepare metric attributes
            genericAttributes.ensureCapacity(3);
            genericAttributes.add(0, "Ease of use");
            genericAttributes.add(1, "Usefulness");
            genericAttributes.add(2, "Responsiveness");

            MetricGroup metricGroup = MetricHelper.createMetricGroup("SSG questionnaire group",
                    "Questionnaire data set for SSG experiment", eccLogger.metricGenerator);

            ordinalValues.ensureCapacity(3);

            ordinalValues.add(0, new ArrayList<String>());
            ordinalValues.get(0).add(0, "very difficult");
            ordinalValues.get(0).add(1, "difficult");
            ordinalValues.get(0).add(2, "not easy/difficult");
            ordinalValues.get(0).add(3, "easy");
            ordinalValues.get(0).add(4, "very easy");

            ordinalValues.add(1, new ArrayList<String>());
            ordinalValues.get(1).add(0, "not at all useful");
            ordinalValues.get(1).add(1, "not very useful");
            ordinalValues.get(1).add(2, "sometimes useful");
            ordinalValues.get(1).add(3, "often useful");
            ordinalValues.get(1).add(4, "always useful");

            ordinalValues.add(2, new ArrayList<String>());
            ordinalValues.get(2).add(0, "very unresponsive");
            ordinalValues.get(2).add(1, "not very responsive");
            ordinalValues.get(2).add(2, "moderately responsive");
            ordinalValues.get(2).add(3, "quite responsive");
            ordinalValues.get(2).add(4, "very responsive");

            //create participant
            if (this.logclass.equals("PerfectLog")) {
                //create participant as per name of logfile
                agentName = logname.substring(0, 1).toUpperCase() + logname.substring(1).toLowerCase();
                participant = factory.createParticipant(agentName);
            } else {
                //create randy, the random participant
                agentName = "Randy";
                participant = factory.createParticipant(agentName);
            }

            //link participant to his metric entity representation
            uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity e
                    = new uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity();
            e.setName(agentName);
            e.setEntityID(participant.agent.getIri());
            e.setDescription(participant.agent.getFriendlyName());
            logger.info(eccLogger.metricGenerator.toString());

            eccLogger.metricGenerator.addEntity(e);

            //create specific attributes for agent
            for (int i = 0; i < genericAttributes.size(); i++) {

                Attribute a = MetricHelper.createAttribute(genericAttributes.get(i), "Question " + i, e);

                MeasurementSet ms = MetricHelper.createMeasurementSet(a, MetricType.ORDINAL,
                        new Unit("Scale item"),
                        metricGroup);

                // Add appropriate metric meta-data to measurement set
                Metric metric = ms.getMetric();
                metric.setMetaType("Likert scale");

                ArrayList<String> scaleItems = ordinalValues.get(i);
                String scaleMeta = "";
                for (String item : scaleItems) {
                    scaleMeta += item + ",";
                }

                scaleMeta = scaleMeta.substring(0, scaleMeta.length() - 1);
                metric.setMetaContent(scaleMeta);
            }

            //create services - using static names as they are the same across participants and used by QoS model
            twitterService = factory.createService("http://sad.it-innovation.soton.ac.uk/", "Hot Tweet Service");
            weatherService = factory.createService("http://weather.com/", "Weather Service");
            lwtService = factory.createService("http://vas.joanneum.at/", "Lift Waiting Time Service");

        } catch (Exception e) {
            logger.error("Error filling EDMProvFactory with data", e);
        }
    }

    /**
     * Process the next log from the logfile
     *
     * @return whether to go on parsing of the log has finished
     */
    public boolean processNextLog() {

        currentLog = getNextLog(logclass);
        boolean goOn = false;

        if (currentLog != null) {
            goOn = true;

            if (lastAppEventTime == null) {
                Long appStartTime = currentLog.getTimestamp() - 180;  // start using the app 3 minutes before first activity in app
                app = factory.createApplication(participant, agentName + "'s SSG client", appStartTime.toString());
            }

            //event from "perfect" log
            if (this.logclass.equals("PerfectLog")) {
                String event = currentLog.getActivity();
                String[] splitevent;
                splitevent = event.split(":");

                if (splitevent.length < 1) {
                    logger.warn("Invalid log line: " + event + "; skipping...");
                    return goOn;
                }
                //set value to empty string if nonexistent
                if (splitevent.length <= 1) {
                    //logger.debug("empty event ({})", event);
                    splitevent = new String[2];
                    splitevent[0] = event;
                    splitevent[1] = "";
                }

                String eventKey = splitevent[0];
                String eventValue = splitevent[1];

                logger.debug("processing event {}", eventKey);

                try {
                    if (eventKey.equals("questionnaire")) {
                        //get agent metric entity
                        uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.metrics.Entity entity
                                = MetricHelper.getEntityFromName(participant.agent.getFriendlyName(), eccLogger.metricGenerator);

                        //get value from log
                        int i = 0;
                        for (String v : eventValue.split(";")) {

                            String attrName = genericAttributes.get(i);
                            int vIndex = Integer.parseInt(v) - 1;
                            String value = ordinalValues.get(i).get(vIndex);

                            if (attrName != null && value != null) {
                                eccLogger.pushSimpleMetric(entity.getName(), attrName, value);
                            }

                            i++;
                        }
                    } else {
                        lastAppEventTime = currentLog.getTimestamp();

                        if (eventKey.equals("lwtservice")) {
                            Content liftinfo = factory.retrieveDataFromService(participant, app, lwtService,
                                    null, "lift waiting times", currentLog.getTimestamp().toString(), currentLog.getDuration());
                        } else if (eventKey.equals("weather")) {
                            Content weatherinfo = factory.retrieveDataFromService(participant, app, weatherService,
                                    null, "weather", currentLog.getTimestamp().toString(), currentLog.getDuration());
                        } else if (eventKey.equals("hottweet")) {
                            Content tweet = factory.retrieveDataFromService(participant, app, twitterService,
                                    null, "hot tweets", currentLog.getTimestamp().toString(), currentLog.getDuration());
                        }
                    }

                } catch (Exception e) {
                    logger.error("Error processing \"perfect\" log", e);
                }
            }
        } else {
            // we have reached the end of the log
            Long appEndTime = lastAppEventTime + 300;
            try {
                factory.destroyApplication(app, appEndTime.toString()); // close app 5 minutes affter last activity
            } catch (DataFormatException ex) {
                logger.error("Problem with last app timestamp", ex);
            }
        }
        return goOn;
    }

    /**
     * Get next log from memory. Obviously needs to be used AFTER reading log to
     * memory.
     *
     * @param logClass the type (Java class name) of log
     * @return the log object
     */
    private ILog getNextLog(String logClass) {

        //sleep if pause required
        if (SPEED > 0) {
            try {
                Thread.sleep(SPEED);
            } catch (InterruptedException e) {
                logger.warn("Error slowing parser down for more realistic simulation", e);
            }
        }

        ILog log = null;

        if (logClass.equals("Log")) {

            //only process complete logs, abort otherwise
            if (rawlog.size() > 2) {

                String[] lines = new String[3];
                lines[0] = rawlog.pollFirst();

                String date1 = lines[0].substring(0, 19);

                //check date
                //as expected, append next line
                if (rawlog.peekFirst().startsWith(date1)) {
                    lines[1] = rawlog.pollFirst();
                    //check next date
                    //as expected, append last line
                    if (rawlog.peekFirst().startsWith(date1)) {
                        lines[2] = rawlog.pollFirst();
                        //unexpected, we must have started at line 2 instead of line 1
                    } else {
                        lines[0] = rawlog.pollFirst();
                        lines[1] = rawlog.pollFirst();
                        lines[2] = rawlog.pollFirst();
                    }
                    //unexpected - date has changed, so we must be at the first one of a block of 3
                } else {
                    lines[0] = rawlog.pollFirst();
                    lines[1] = rawlog.pollFirst();
                    lines[2] = rawlog.pollFirst();
                }

                if (lines[0] != null && lines[1] != null && lines[2] != null) {
                    log = new Log(lines);
                }
            }

        } else if (logClass.equals("PerfectLog")) {
            if (rawlog.peekFirst() != null) {
                log = new PerfectLog(rawlog.pollFirst());
            }
        } else {
            logger.error("Incorrect log type, aborting...");
        }

        return log;
    }

    //GETTERS/SETTERS//////////////////////////////////////////////////////////////////////////////
    public ILog getCurrentLog() {
        return currentLog;
    }

    public LinkedList<String> getRawlog() {
        return rawlog;
    }

    public ExperimediaFactory getFactory() {
        return factory;
    }

    public ECCSimpleLogger getEccLogger() {
        return eccLogger;
    }
}
