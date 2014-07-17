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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMActivity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMAgent;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMEntity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvFactory;

public class ExperimentDataGenerator {

	//how many seconds to wait until reading the next log. Set to 0 for max speed
	public static final int SPEED = 1;
	//located in resources folder
	public static final String LOGFILE = "sample.log";

	private LinkedList<String> rawlog = new LinkedList<String>();

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
	 */
	public void readLog() {
		try {
				String logfilePath = ExperimentDataGeneratorTest.class.getClassLoader().getResource(LOGFILE).getPath();
				FileReader fr = new FileReader(new File(logfilePath));
				BufferedReader br = new BufferedReader(fr);

				String line;
				while((line = br.readLine()) != null) {
					rawlog.add(line.trim());
				}
			} catch (IOException e) {
				logger.error("Error reading from logfile " + LOGFILE, e);
			}
	}

	/**
	 * Parses the in-memory log and creates prov statements
	 */
	public void parseLog() {

		try {
			//init factory
			factory = EDMProvFactory.getInstance();
			factory.addOntology("foaf", "http://xmlns.com/foaf/0.1/");
			factory.addOntology("sioc", "http://rdfs.org/sioc/ns#");

			//add ontology namespaces
			factory.addOntology("eee", "http://experimedia.eu/ontologies/ExperimediaExperimentExplorer#");
			factory.addOntology("ski", "http://www.semanticweb.org/sw/ontologies/skiing#");

			//create bob
			EDMAgent bob = factory.createAgent("agent_" + UUID.randomUUID(), "Bob");
			bob.addOwlClass(factory.getNamespaceForPrefix("foaf") + "Person");
			bob.addOwlClass(factory.getNamespaceForPrefix("eee") + "Participant");
			//TODO: link bob to his metric entity representation

			//create app
			EDMActivity useApp = bob.startActivity("useAppActivity_" + UUID.randomUUID(), "Use app", "1387531200");
			EDMEntity appEntity = useApp.generateEntity("entity_" + UUID.randomUUID(), "App", "1387531201");
			appEntity.addOwlClass(factory.getNamespaceForPrefix("eee") + "Application");
			EDMAgent appAgent = factory.createAgent(appEntity.getIri(), appEntity.getFriendlyName());
			appAgent.addTriple(factory.getNamespaceForPrefix("owl") + "sameAs", appEntity.getIri());
			appAgent.actOnBehalfOf(bob);

			//create services
			EDMEntity twitterService = factory.createEntity("entity_" + UUID.randomUUID(), "Twitter service");
			twitterService.addOwlClass(factory.getNamespaceForPrefix("eee") + "Service");
			EDMEntity messageService = factory.createEntity("entity_" + UUID.randomUUID(), "Message service");
			messageService.addOwlClass(factory.getNamespaceForPrefix("eee") + "Service");

			//create skilifts
			EDMEntity[] skilifts = new EDMEntity[5];

			EDMEntity skilift1 = factory.createEntity("skilift-37", "Fritz Blitz");
			skilift1.addOwlClass(factory.getNamespaceForPrefix("ski") + "Skilift");
			skilifts[0] = skilift1;

			EDMEntity skilift2 = factory.createEntity("skilift-31", "Sonneckbahn");
			skilift1.addOwlClass(factory.getNamespaceForPrefix("ski") + "Skilift");
			skilifts[1] = skilift2;

			EDMEntity skilift3 = factory.createEntity("skilift-33", "Märchenwiesebahn");
			skilift1.addOwlClass(factory.getNamespaceForPrefix("ski") + "Skilift");
			skilifts[2] = skilift3;

			EDMEntity skilift4 = factory.createEntity("skilift-27", "Lärchkogelbahn");
			skilift1.addOwlClass(factory.getNamespaceForPrefix("ski") + "Skilift");
			skilifts[3] = skilift4;

			EDMEntity skilift5 = factory.createEntity("skilift-35", "Weitmoos-Tellerlift");
			skilift1.addOwlClass(factory.getNamespaceForPrefix("ski") + "Skilift");
			skilifts[4] = skilift5;

			int messageReceived = 0;
			int tweetReceived = 0;
			LinkedList<EDMEntity> messages = new LinkedList<EDMEntity>();
			LinkedList<EDMEntity> tweets = new LinkedList<EDMEntity>();

			Random rand = new Random();

			for (Log log = getNextLog(); log !=null; log = getNextLog()) {

				//random event: throw dice
				double random = rand.nextDouble();

				//logger.debug("random: " + random);

				if (random>0.95) {

					//only makes sense while not skiing
					if ((new Double(log.speed))<5) {
						//choose random skilift
						EDMEntity skilift = skilifts[rand.nextInt(skilifts.length)];

						//use skilift
						EDMActivity a = bob.doDiscreteActivity("useSkiliftActivity_" + UUID.randomUUID(), "Use skilift " + skilift.getFriendlyName(), log.timestamp.toString());
						a.addOwlClass(factory.getNamespaceForPrefix("ski") + "UsingSkiliftActivity");
						a.useEntity(skilift);

						//create some random liftwaiting time data + QoE
						EDMActivity b = bob.doDiscreteActivity("generateQoEActivity_" + UUID.randomUUID(), "Generate QoE data", log.timestamp.toString());
						b.useEntity(appEntity);
						EDMEntity qoe = b.generateEntity("QoEEntity_" + UUID.randomUUID(), "QoE data", log.timestamp.toString());
						//TODO: link QoE entity to metrics

					}
				} else if (random<=0.95 && random>0.9) {
					//navigate app
					EDMActivity a = bob.doDiscreteActivity("navigateAppActitivy_" + UUID.randomUUID(), "Navigate app", log.timestamp.toString());
					a.useEntity(appEntity);

				} else if (random<=0.9 && random>0.85) {
					//read message
					if (messageReceived>0) {
						messageReceived -= 1;
						EDMActivity a = bob.doDiscreteActivity("readMessageActivity_" + UUID.randomUUID(), "Read Message", log.timestamp.toString());
						a.useEntity(messages.pollFirst());
					}
				} else if (random<=0.8 && random>0.75) {
					//read tweet
					if (tweetReceived>0) {
						tweetReceived -= 1;
						EDMActivity a = bob.doDiscreteActivity("readTweetActivity_" + UUID.randomUUID(), "Read Tweet", log.timestamp.toString());
						a.useEntity(tweets.pollFirst());
					}
				} else if (random<=0.7 && random>0.65) {
					//receive message
					messageReceived += 1;
					EDMActivity a = bob.doDiscreteActivity("receiveMessageActivity_" + UUID.randomUUID(), "Receive Message", log.timestamp.toString());
					a.useEntity(appEntity);
					EDMActivity b = appAgent.doDiscreteActivity("retrieveMessageActivity_" + UUID.randomUUID(), "Retrieve Message", log.timestamp.toString());
					a.informActivity(b);
					b.useEntity(messageService);
					EDMEntity data = b.generateEntity("MessageData_" + UUID.randomUUID(), "Message data", log.timestamp.toString());
					data.addOwlClass(factory.getNamespaceForPrefix("eee") + "Content");
					a.useEntity(data);
					messages.add(data);
					//TODO: put metric data here, link to relevant entities
				} else if (random<=0.6 && random>0.55) {
					//receive tweet
					tweetReceived += 1;
					EDMActivity a = bob.doDiscreteActivity("receiveTweetActivity_" + UUID.randomUUID(), "Receive Tweet", log.timestamp.toString());
					a.useEntity(appEntity);
					EDMActivity b = appAgent.doDiscreteActivity("retrieveTweetActivity_" + UUID.randomUUID(), "Retrieve Tweet", log.timestamp.toString());
					a.informActivity(b);
					b.useEntity(twitterService);
					EDMEntity data = b.generateEntity("TweetData_" + UUID.randomUUID(), "Tweet data", log.timestamp.toString());
					data.addOwlClass(factory.getNamespaceForPrefix("eee") + "Content");
					a.useEntity(data);
					tweets.add(data);
					//TODO: put metric data here, link to relevant entities
				} else if (random<=0.55 && random>0.5) {
					//tweet
					EDMActivity a = bob.doDiscreteActivity("TweetActivity_" + UUID.randomUUID(), bob.getFriendlyName() + " tweets", log.timestamp.toString());
					EDMEntity tweet = a.generateEntity("Tweet_" + UUID.randomUUID(), bob.getFriendlyName() + "'s tweet", log.timestamp.toString());
					tweet.addOwlClass(factory.getNamespaceForPrefix("eee") + "Content");
					EDMActivity b = appAgent.startActivity("SendTweetActivity_" + UUID.randomUUID(), "Send tweet to server", log.timestamp.toString());
					b.useEntity(twitterService);
					b.useEntity(tweet);
					a.informActivity(b);

				} else {
					//do nothing
				}
			}

			useApp.invalidateEntity(appEntity);
			bob.stopActivity(useApp, "1387533300");

			logger.info("finished parsing log");

		} catch (Exception e) {
			logger.error("Error filling EDMProvFactory with data", e);
		}
	}

	private Log getNextLog() {

		//sleep if pause required
		if (SPEED>0) {
			try {
				Thread.sleep(SPEED);
			} catch (InterruptedException e) {
				logger.warn("Error slowing parser down for more realistic simulation", e);
			}
		}

		Log log = null;

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

		return log;
	}


	// PRIVATE CLASS
	private class Log {
		public String[] lines;
		public String date;
		public String time;
		public Long timestamp;

		public String longitude = null;
		public String latitude = null;
		public String altitude = null;

		public String speed = null;
		public String temperature = null;

		Log(String[] lines) {

			this.lines = lines;

			//date/time
			this.date = lines[0].substring(0,10);
			this.time = lines[0].substring(11,19);
			//logger.debug(this.date + ", " + this.time);

			//timestamp
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
				Date fullDate = formatter.parse(this.date + " " + this.time);
				this.timestamp = fullDate.getTime()/1000L;
			} catch (ParseException e) {
				logger.error("Error parsing date to create timestamp", e);
			}

			//check for long/lat/alt
			if (lines[0].split(",").length>2) {
				this.latitude = lines[0].split(",")[2];
				if (!this.latitude.matches("\\d{1,2}\\.\\d{1,15}")) {
					this.latitude = null;
				}
			}
			if (lines[0].split(",").length>3) {
				this.longitude = lines[0].split(",")[3];
				if (!this.longitude.matches("\\d{1,2}\\.\\d{1,15}")) {
					this.longitude = null;
				}
			}
			if (lines[0].split(",").length>4) {
				this.altitude = lines[0].split(",")[4];
				if (!this.altitude.matches("\\d{1,4}\\.\\d{1,2}")) {
					this.altitude = null;
				}
			}
			//logger.debug("lat: " + this.latitude + ", long: " + this.longitude + ", alt: " + this.altitude);

			//speed/temp
			if (lines[1].split(",").length>2) {
				this.speed = lines[1].split(",")[2];
			}
			if (lines[2].split(",").length>2) {
				this.temperature = lines[2].split(",")[2];
			}
			//logger.debug("speed: " + this.speed + ", temperature: " + this.temperature);
		}

		@Override
		public String toString() {
			return this.lines[0] + "\n" + this.lines[1] + "\n" + this.lines[2];
		}
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
