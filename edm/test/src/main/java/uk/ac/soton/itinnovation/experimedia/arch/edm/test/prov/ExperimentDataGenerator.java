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

package uk.ac.soton.itinnovation.experimedia.arch.edm.test.prov;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.DataFormatException;
import javax.xml.datatype.DatatypeConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMActivity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMAgent;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMEntity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMTriple;

public class ExperimentDataGenerator {

	//how many seconds to wait until reading the next log. Set to 0 for max speed
	public static final int SPEED = 1;

	private LinkedList<String> rawlog = new LinkedList<String>();
	private String logname;

	private EDMProvFactory factory;

	private static final Properties props = new Properties();
	private static final Logger logger = LoggerFactory.getLogger(ExperimentDataGenerator.class);

	public ExperimentDataGenerator() {

		logger.info("Starting ExperimentDataGenerator");

		try {
			logger.info("Loading properties file");
			props.load(ExperimentDataGeneratorTest.class.getClassLoader().getResourceAsStream("prov.properties"));
		} catch (IOException e) {
			logger.error("Error loading properties file", e);
		}
	}

	/**
	 * Reads the logfile and saves contents in memory
	 * @param logfile the input logfile; name only as it is read from classpath
	 */
	public void readLog(String logfile) {
		try {
				String logfilePath = ExperimentDataGeneratorTest.class.getClassLoader().getResource(logfile).getPath();
				FileReader fr = new FileReader(new File(logfilePath));
				BufferedReader br = new BufferedReader(fr);

				String line;
				while((line = br.readLine()) != null) {
					rawlog.add(line.trim());
				}
				logname = logfile.split("\\.")[0];
			} catch (IOException e) {
				logger.error("Error reading from logfile " + logfile, e);
			}
	}

	/**
	 * Parses the in-memory log and creates prov statements
	 * @param logClass the ype of log used (Log, PerfectLog)
	 */
	public void parseLog(String logClass) {

		try {
			//init factory
			factory = EDMProvFactory.getInstance();
			factory.addOntology("foaf", "http://xmlns.com/foaf/0.1/");
			factory.addOntology("sioc", "http://rdfs.org/sioc/ns#");

			//add ontology namespaces
			factory.addOntology("eee", "http://experimedia.eu/ontologies/ExperimediaExperimentExplorer#");
			factory.addOntology("ski", "http://www.semanticweb.org/sw/ontologies/skiing#");

			//create participant
			EDMAgent participant;
			if (logClass.equals("PerfectLog")) {
				//create participant as per name of logfile
				String agentName = logname.substring(0,1).toUpperCase() + logname.substring(1).toLowerCase();
				participant = factory.createAgent("agent_" + UUID.randomUUID(), agentName);
			} else {
				//create randy, the random participant
				participant = factory.createAgent("agent_" + UUID.randomUUID(), "Randy");
			}
			participant.addOwlClass(factory.getNamespaceForPrefix("foaf") + "Person");
			participant.addOwlClass(factory.getNamespaceForPrefix("eee") + "Participant");
			//TODO: link participant to his metric entity representation

			//create app for participant
			EDMActivity useApp = participant.startActivity("useAppActivity_" + UUID.randomUUID(), "Use app", "1387531200");
			EDMEntity appEntity = useApp.generateEntity("entity_" + UUID.randomUUID(), "App", "1387531201");
			appEntity.addOwlClass(factory.getNamespaceForPrefix("eee") + "Application");
			EDMAgent appAgent = factory.createAgent("entity_" + UUID.randomUUID(), "App");
			appAgent.addTriple(factory.getNamespaceForPrefix("owl") + "sameAs", appEntity.getIri(), EDMTriple.TRIPLE_TYPE.OBJECT_PROPERTY);
			appAgent.actOnBehalfOf(participant);

			//create services
			EDMEntity twitterService = createService("entity_" + UUID.randomUUID(), "Twitter service");
			EDMEntity messageService = createService("entity_" + UUID.randomUUID(), "Message service");
			EDMEntity babylonService = createService("entity_" + UUID.randomUUID(), "Babylon service");
			EDMEntity lwtService = createService("entity_" + UUID.randomUUID(), "Lift Waiting Time service");

			//create skilifts
			HashMap<String, EDMEntity> lifts = new HashMap<String, EDMEntity>();

			EDMEntity skilift1 = factory.createEntity("skilift-37", "Fritz Blitz");
			skilift1.addOwlClass(factory.getNamespaceForPrefix("ski") + "Skilift");
			lifts.put("37", skilift1);

			EDMEntity skilift2 = factory.createEntity("skilift-31", "Sonneckbahn");
			skilift1.addOwlClass(factory.getNamespaceForPrefix("ski") + "Skilift");
			lifts.put("31", skilift2);

			EDMEntity skilift3 = factory.createEntity("skilift-33", "Märchenwiesebahn");
			skilift1.addOwlClass(factory.getNamespaceForPrefix("ski") + "Skilift");
			lifts.put("33", skilift3);

			EDMEntity skilift4 = factory.createEntity("skilift-27", "Lärchkogelbahn");
			skilift1.addOwlClass(factory.getNamespaceForPrefix("ski") + "Skilift");
			lifts.put("27", skilift4);

			EDMEntity skilift5 = factory.createEntity("skilift-35", "Weitmoos-Tellerlift");
			skilift1.addOwlClass(factory.getNamespaceForPrefix("ski") + "Skilift");
			lifts.put("35", skilift5);

			LinkedList<EDMEntity> messages = new LinkedList<EDMEntity>();
			LinkedList<EDMEntity> tweets = new LinkedList<EDMEntity>();

			Random rand = new Random();

			ILog log;

			for (log = getNextLog(logClass); log !=null; log = getNextLog(logClass)) {

				//event from "perfect" log
				if (logClass.equals("PerfectLog")) {
					String event = log.toString().split(",")[1];
					String[] splitevent = event.split(":");
					if (splitevent.length<2) {
						logger.warn("Invalid log line: " + event + "; skipping...");
						continue;
					}
					String eventKey = splitevent[0];
					String eventValue = splitevent[1];

					try {
						if (eventKey.equals("lift")) {

							if (lifts.containsKey(eventValue)) {

								//use skilift
								EDMActivity a = useRealWorldEntity(participant, lifts.get(eventValue), log.getTimestamp().toString());
								a.addOwlClass(factory.getNamespaceForPrefix("ski") + "UsingSkiliftActivity");

								//create some liftwaiting time QoE data
								EDMEntity qoe = createDataAtService(participant, appAgent, appEntity, babylonService, "liftrating", log.getTimestamp().toString());

								//TODO: link QoE entity to metrics
							}
						} else if (eventKey.equals("lwtservice")) {
							EDMEntity liftinfo = retrieveDataFromService(participant, appAgent, appEntity, lwtService, "lwtinfo", log.getTimestamp().toString());

							//TODO: link liftinfo entity to metrics
						} else if (eventKey.equals("questionnaire")) {
							//TODO
						}

					} catch (Exception e) {
						logger.error("Error processing \"perfect\" log", e);
					}
				}

				//random event: throw dice
				double random = rand.nextDouble();

				if (random>0.99) {
					//navigate app
					navigateClient(participant, appEntity, log.getTimestamp().toString());
				} else if (random<=0.99 && random>0.98) {
					//read message
					if (messages.size()>0) {
						useDataOnClient(participant, appEntity, messages.pollFirst(), "message", log.getTimestamp().toString());
					}
				} else if (random<=0.98 && random>0.97) {
					//create data on client
					EDMEntity photo = createDataOnClient(participant, appEntity, "photo", log.getTimestamp().toString());
				} else if (random<=0.97 && random>0.96) {
					//read tweet
					if (tweets.size()>0) {
						useDataOnClient(participant, appEntity, tweets.pollFirst(), "tweet", log.getTimestamp().toString());
					}
				} else if (random<=0.96 && random>0.95) {
					//receive message
					messages.add(retrieveDataFromService(participant, appAgent, appEntity, messageService, "message", log.getTimestamp().toString()));
					//TODO: put metric data here, link to relevant entities
				} else if (random<=0.95 && random>0.94) {
					//receive tweet
					tweets.add(retrieveDataFromService(participant, appAgent, appEntity, twitterService, "tweet", log.getTimestamp().toString()));
					//TODO: put metric data here, link to relevant entities
				} else if (random<=0.94 && random>0.93) {
					//tweet
					createDataAtService(participant, appAgent, appEntity, twitterService, "tweet", log.getTimestamp().toString());
					//TODO: put metric data here, link to relevant entities
				}
			}

			useApp.invalidateEntity(appEntity);
			participant.stopActivity(useApp, "1387533300");

			logger.info("finished parsing log");

		} catch (Exception e) {
			logger.error("Error filling EDMProvFactory with data", e);
		}
	}

	/**
	 * Creates a participant including all required triples. Note that name doesn't have to be unique.
	 *
	 * @param name the name of the participant. Will be used as a label.
	 * @return the participant object
	 */
	public EDMAgent createParticipant(String name) {
		EDMAgent participant = null;
		try {
			participant = factory.createAgent("agent_" + UUID.randomUUID(), name);
			participant.addOwlClass(factory.getNamespaceForPrefix("foaf") + "Person");
			participant.addOwlClass(factory.getNamespaceForPrefix("eee") + "Participant");
		} catch (Exception e) {
			logger.error("Error creating participant", e);
		}
		return participant;
	}

	/**
	 * Creates an experimedia service including all required triples.
	 *
	 * @param uniqueIdentifier the local name of the service
	 * @param label a human readable label
	 * @return the service object
	 */
	public EDMEntity createService(String uniqueIdentifier, String label) {
		EDMEntity service = null;
		try {
			service = factory.createEntity(uniqueIdentifier, label);
			service.addOwlClass(factory.getNamespaceForPrefix("eee") + "Service");
		} catch (Exception e) {
			logger.error("Could not create service", e);
		}
		return service;
	}

	/**
	 * Make a participant use a real world entity
	 *
	 * @param participant the participant
	 * @param entity the entity to be used
	 * @param timestamp the unix timestamp of this discrete activity (start=end)
	 *
	 * @return the activity of the agent using the entity
	 */
	public EDMActivity useRealWorldEntity(EDMAgent participant, EDMEntity entity, String timestamp) {

		EDMActivity a = null;
		try {
			a = participant.doDiscreteActivity("useSkiliftActivity_" + UUID.randomUUID(), "Use " + entity.getFriendlyName(), timestamp);
			a.useEntity(entity);
		} catch (Exception e) {
			logger.error("Error creating \"use entity\" pattern", e);
		}
		return a;
	}

	/**
	 * Make the participant create data on the client without sending it to the service
	 *
	 * @param participant the participant
	 * @param client the client which the participant is using
	 * @param dataName the name of the data to be used for labelling
	 * @param timestamp the unix timestamp of this discrete activity (start=end)
	 *
	 * @return the data created on the client
	 */
	public EDMEntity createDataOnClient(EDMAgent participant, EDMEntity client, String dataName, String timestamp) {

		EDMEntity data = null;
		try {
			String dName = dataName.substring(0,1).toUpperCase() + dataName.replaceAll(" ", "").substring(1);
			EDMActivity a = participant.doDiscreteActivity("Create" + dName + "Activity_" + UUID.randomUUID(), "Create " + dataName, timestamp);
			a.useEntity(client);
			data = a.generateEntity("Content_" + UUID.randomUUID(), dataName);
			data.addOwlClass(factory.getNamespaceForPrefix("eee") + "Content");
		} catch (Exception e) {
			logger.error("Error creating \"create data on client\" pattern", e);
		}
		return data;
	}

	/**
	 * Make the participant create data on the client without sending it to the service
	 *
	 * @param participant the participant
	 * @param client the client which the participant is using
	 * @param data the data which is used
	 * @param dataName the name of the data to be used for labelling
	 * @param timestamp the unix timestamp of this discrete activity (start=end)
	 */
	public void useDataOnClient(EDMAgent participant, EDMEntity client, EDMEntity data,
			String dataName, String timestamp) {

		try {
			String dName = dataName.substring(0,1).toUpperCase() + dataName.replaceAll(" ", "").substring(1);
			EDMActivity a = participant.doDiscreteActivity("Use" + dName + "Activity_" + UUID.randomUUID(), "Use " + dataName, timestamp);
			a.useEntity(data);
			a.useEntity(client);
		} catch (Exception e) {
			logger.error("Error creating \"use data on client\" pattern", e);
		}
	}

	/**
	 * Make the participant navigate the client
	 *
	 * @param participant the participant
	 * @param client the client which the participant is using
	 * @param timestamp the unix timestamp of this discrete activity (start=end)
	 */
	public void navigateClient(EDMAgent participant, EDMEntity client, String timestamp) {

		try {
			EDMActivity a = participant.doDiscreteActivity("NavigateClientActitivy_" + UUID.randomUUID(), "Navigate " + client.getFriendlyName(), timestamp);
			a.useEntity(client);
		} catch (Exception e) {
			logger.error("Error creating \"navigate client\" pattern", e);
		}
	}

	/**
	 * Creates the "Application Service Interaction (retrieve data from service)" pattern.
	 *
	 * @param participant the participant
	 * @param clientAgent agent representation of the client
	 * @param clientEntity entity representation of the client
	 * @param service the service
	 * @param activityName the name of the activity to be used for labelling
	 * @param timestamp the unix timestamp of this discrete activity (start=end)
	 *
	 * @return the data retrieved from the service
	 */
	public EDMEntity retrieveDataFromService(EDMAgent participant, EDMAgent clientAgent,
			EDMEntity clientEntity, EDMEntity service, String activityName, String timestamp) {

		EDMEntity data = null;
		try {
			String actName = activityName.substring(0,1).toUpperCase() + activityName.substring(1);
			EDMActivity a = participant.doDiscreteActivity("Receive" + actName + "Activity_" + UUID.randomUUID(), "Receive " + activityName, timestamp);
			a.useEntity(clientEntity);
			EDMActivity b = clientAgent.doDiscreteActivity("Retrieve" + actName + "Activity_" + UUID.randomUUID(), "Retrieve " + activityName, timestamp);
			a.informActivity(b);
			b.useEntity(service);
			data = b.generateEntity(actName + "Data_" + UUID.randomUUID(), actName + " data", timestamp);
			data.addOwlClass(factory.getNamespaceForPrefix("eee") + "Content");
			a.useEntity(data);
		} catch (Exception e) {
			logger.error("Error creating \"retrieve data from service\" pattern", e);
		}
		return data;
	}

	/**
	 * Creates the "Application Service Interaction (create data at service)" pattern.
	 *
	 * @param participant the participant
	 * @param clientAgent agent representation of the client
	 * @param clientEntity entity representation of the client
	 * @param service the service
	 * @param activityName the name of the activity to be used for labelling
	 * @param timestamp the unix timestamp of this discrete activity (start=end)
	 *
	 * @return the data created in the process
	 */
	public EDMEntity createDataAtService(EDMAgent participant, EDMAgent clientAgent,
			EDMEntity clientEntity, EDMEntity service, String activityName, String timestamp) {

		EDMEntity data = null;

		try {
			String actName = activityName.substring(0,1).toUpperCase() + activityName.substring(1);
			EDMActivity a = participant.doDiscreteActivity("Create" + actName + "Activity_" + UUID.randomUUID(), participant.getFriendlyName() + " tweets", timestamp);
			a.useEntity(clientEntity);
			data = a.generateEntity(actName + "_" + UUID.randomUUID(), participant.getFriendlyName() + "'s " + activityName, timestamp);
			data.addOwlClass(factory.getNamespaceForPrefix("eee") + "Content");
			EDMActivity b = clientAgent.startActivity("Send" + actName + "Activity_" + UUID.randomUUID(), "Send " + activityName + " to server", timestamp);
			b.useEntity(service);
			b.useEntity(data);
			a.informActivity(b);
		} catch (Exception e) {
			logger.error("Error creating \"create data at service\" pattern", e);
		}

		return data;
	}

	private ILog getNextLog(String logClass) {

		//sleep if pause required
		if (SPEED>0) {
			try {
				Thread.sleep(SPEED);
			} catch (InterruptedException e) {
				logger.warn("Error slowing parser down for more realistic simulation", e);
			}
		}

		ILog log = null;

		if (logClass.equals("Log")) {

			//only process complete logs, abort otherwise
			if (rawlog.size()>2) {

				String[] lines = new String[3];
				lines[0] = rawlog.pollFirst();

				String date1 = lines[0].substring(0,19);

				//check date
				//as expected, append next line
				if (rawlog.peekFirst().startsWith(date1)) {
					lines[1] = rawlog.pollFirst();
					//check next date
					//as expected, append last line
					if(rawlog.peekFirst().startsWith(date1)) {
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


				if (lines[0]!=null && lines[1]!=null && lines[2]!=null) {
					log = new Log(lines);
				}
			}

		} else if (logClass.equals("PerfectLog")) {
			if (rawlog.peekFirst()!=null) {
				log = new PerfectLog(rawlog.pollFirst());
			}
		} else {
			logger.error("Incorrect log type, aborting...");
		}

		return log;
	}

	//GETTERS/SETTERS//////////////////////////////////////////////////////////////////////////////

	public LinkedList<String> getRawlog() {
		return rawlog;
	}

	public EDMProvFactory getFactory() {
		return factory;
	}

	public static Properties getProps() {
		return props;
	}

}
