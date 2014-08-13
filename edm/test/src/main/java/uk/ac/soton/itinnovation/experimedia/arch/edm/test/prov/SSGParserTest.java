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
//      Created Date :          2014-04-14
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.TreeSet;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMActivity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMAgent;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMEntity;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.common.dataModel.provenance.EDMProvFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.factory.EDMProvPersistenceFactory;
import uk.ac.soton.itinnovation.experimedia.arch.ecc.edm.impl.prov.dao.EDMProvDataStoreImpl;

public final class SSGParserTest {

	private final String logdir = "/home/sw/projects/Experimedia/ssglogs";
	private final Logger logger = LoggerFactory.getLogger(SSGParserTest.class);
	private final Properties props = new Properties();

	private HashSet<String> types = new HashSet<String>();
	private HashMap<String, LinkedList<Log>> lines = new HashMap<String, LinkedList<Log>>();
	private HashMap<String, TreeSet<Log>> logs = new HashMap<String, TreeSet<Log>>();

	private int numDir = 0;
	private int numFile = 0;

	public SSGParserTest() {

		logger.info("Starting SSGParserTest");

		try {
			logger.info("Loading properties file");
			props.load(KnowledgeBaseTest.class.getClassLoader().getResourceAsStream("prov.properties"));
		} catch (IOException e) {
			logger.error("Error loading properties file", e);
		}
		logger.info("Loaded properties file");

		try {
			File[] files = new File(logdir).listFiles();
			parseFiles(files);
			logger.info("Parsed " + numDir + " directories and " + numFile + " files");
		} catch (Exception e) {
			logger.error("Error parsing logfiles", e);
		}

		analyseFiles();

		createProvenance();
	}

	public static void main(String[] args) {
		SSGParserTest test = new SSGParserTest();
	}

	public final void parseFiles(File[] files) {

		if (files==null) { return; }

		for (File file : files) {
			if (file.isDirectory()) {
				numDir++;
				parseFiles(file.listFiles());
			} else {
				logger.debug("File: " + file.getAbsolutePath());
				numFile++;
				try {
					FileReader fr = new FileReader(file);
					BufferedReader br = new BufferedReader(fr);

					String line;
					while((line = br.readLine()) != null) {

						Log log;
						try {
							log = new Log(file.getName(), line);
						} catch (ParseException e) {
							logger.debug("Error creating Log object", e);
							continue;
						}


						if (log.type==null) { continue; }

						//collect all the different types
						types.add(log.type.trim());

						//add to collection of lines ordered by type
						if (lines.get(log.type)==null) {
							lines.put(log.type, new LinkedList<Log>());
						}
						lines.get(log.type).add(log);

						//add to collection of lines ordered by device
						if (logs.get(log.device)==null) {
							logs.put(log.device, new TreeSet<Log>());
						}
						logs.get(log.device).add(log);
					}
				} catch (IOException e) {
					logger.error("Error in recursive function", e);
				}

			}
		}
	}

	public void analyseFiles() {

		LinkedList<String> unknownTypes = new LinkedList<String>();
		HashSet<String> skilifts = new HashSet<String>();

		for (String type: types) {
			//Speed of the user	speed in km/h
			if (type.equals("speed")) {
				logger.info(lines.get(type).size() + " speed logs found");
			//Location of the user	gps position
			} else if (type.equals("gps")) {
				logger.info(lines.get(type).size() + " gps logs found");
			//User entered notificaiton	millseconds since 1970
			} else if (type.equals("notification-start")) {
				logger.info(lines.get(type).size() + " notification-start");
			//User entered indivudal	millseconds since 1970
			} else if (type.equals("individual-start")) {
				logger.info(lines.get(type).size() + " individual-start logs found");
			//User entered lift info	millseconds since 1970
			} else if (type.equals("liftinfo-start")) {
				logger.info(lines.get(type).size() + " liftinfo-start logs found");
			//User entered Hospitality	millseconds since 1970
			} else if (type.equals("hospitality-start")) {
				logger.info(lines.get(type).size() + " hospitality-start logs found");
			//User entered Community	millseconds since 1970
			} else if (type.equals("community-start")) {
				logger.info(lines.get(type).size() + " community-start logs found");
			//User entered weather	millseconds since 1970
			} else if (type.equals("weather-start")) {
				logger.info(lines.get(type).size() + " weather-start logs found");
			//User entered navigation	millseconds since 1970
			} else if (type.equals("navigation-start")) {
				logger.info(lines.get(type).size() + " navigation-start logs found");
			//Time spend in notification	time in milliseconds spent in screen
			} else if (type.equals("notification-duration")) {
				logger.info(lines.get(type).size() + " notification-duration logs found");
			//Time spent in duration	time in milliseconds spent in screen
			} else if (type.equals("individual-duration")) {
				logger.info(lines.get(type).size() + " individual-duration logs found");
			//Time spent in lift info	time in milliseconds spent in screen
			} else if (type.equals("liftinfo-duration")) {
				logger.info(lines.get(type).size() + " liftinfo-duration logs found");
			//Time spent in hospitality	time in milliseconds spent in screen
			} else if (type.equals("hospitality-duration")) {
				logger.info(lines.get(type).size() + " hospitality-duration logs found");
			//Time spend in community	time in milliseconds spent in screen
			} else if (type.equals("community-duration")) {
				logger.info(lines.get(type).size() + " community-duration logs found");
			//Time spent in weather	time in milliseconds spent in screen
			} else if (type.equals("weather-duration")) {
				logger.info(lines.get(type).size() + " weather-duration logs found");
			//Time spent in navigation	time in milliseconds spent in screen
			} else if (type.equals("navigation-duration")) {
				logger.info(lines.get(type).size() + " navigation-duration logs found");
			//percentage	battery level 0-100
			} else if (type.equals("battery-level-ssg")) {
				logger.info(lines.get(type).size() + " battery-level-ssg logs found");
			//Current 3G reception level of gateway application	network reception level 0-100
			} else if (type.equals("network-level")) {
				logger.info(lines.get(type).size() + " network-level logs found");
			//round trip time of request	milliseconds of request taken
			} else if (type.equals("roundtriptime")) {
				logger.info(lines.get(type).size() + " roundtriptime logs found");
			//average speed during last track	average speed of user in km/h
			} else if (type.equals("avgtrackspeed")) {
				logger.info(lines.get(type).size() + " avgtrackspeed logs found");
			//last lift used	name of lift
			} else if (type.equals("usedlift") || type.equals("used-lift")) {
				logger.info(lines.get(type).size() + " usedlift logs found");
				for (Log log: lines.get(type)) {
					skilifts.add(log.csv[7]);
				}
			//time taken until user read notification	milliseconds between receiving and reading
			} else if (type.equals("userreactiontime")) {
				logger.info(lines.get(type).size() + " userreactiontime logs found");
			//navigation route initiated	name of source and target
			} else if (type.equals("navigationinitated")) {
				logger.info(lines.get(type).size() + " navigationinitated logs found");
			//number of pois started to navigate to	number of pois
			} else if (type.equals("poinavigated")) {
				logger.info(lines.get(type).size() + " poinavigated logs found");
			//number of pois completed to navigate to	number of pois
			} else if (type.equals("poicompleted")) {
				logger.info(lines.get(type).size() + " poicompleted logs found");
			//lift wait time of Planai 6er	number of measurements received during last 5 minutes
			} else if (type.equals("liftwaittime1")) {
				logger.info(lines.get(type).size() + " liftwaittime1 logs found");
			//lift wait time of Mitterhausbahn	number of measurements received during last 5 minutes
			} else if (type.equals("liftwaittime2")) {
				logger.info(lines.get(type).size() + " liftwaittime2 logs found");
			//lift wait time of Märchenwiesebahn	number of measurements received during last 5 minutes
			} else if (type.equals("liftwaittime3")) {
				logger.info(lines.get(type).size() + " liftwaittime3 logs found");
			//lift wait time of Laerchkogelbahn	number of measurements received during last 5 minutes
			} else if (type.equals("liftwaittime4")) {
				logger.info(lines.get(type).size() + " liftwaittime4 logs found");


				//LinkedList<String> list = lines.get(type);
				//for (String line: list) {
				//	logger.info(line);
				//}
			} else {
				unknownTypes.add(type);
			}
		}

		//logger.info("Unknown log types:");
		//for (String u: unknownTypes) {
		//	logger.info(u);
		//}

		logger.info(skilifts.size() + " skilifts found");
		for (String s: skilifts) {
			logger.info(s);
		}

	}

	public void createProvenance() {

		try {
			//init
			EDMProvFactory factory = EDMProvFactory.getInstance();
			factory.addOntology("foaf", "http://xmlns.com/foaf/0.1/");
			factory.addOntology("sioc", "http://rdfs.org/sioc/ns#");
			factory.addOntology("ski", "http://www.semanticweb.org/sw/ontologies/skiing#");

			//data container for long term applicationUseActivities
			ArrayList<ActivityCollection> applicationUseActivities = new ArrayList<ActivityCollection>();

			//add all agents (i.e. SSG devices in this case)
			for (String device: logs.keySet()) {
				EDMAgent agent = factory.createAgent("agent_" + device, device);

				logger.info("logs: " + logs.get(device).size());
				//process logs by device
				Iterator<Log> it = logs.get(device).iterator();
				while (it.hasNext()) {
					Log log = it.next();
					//lift use
					if (log.type.equals("used-lift")) {
						logger.debug("Used lift: " + log.csv[7] + " (" + log.csv[8] + ") at " + log.timestamp);

						EDMEntity skilift = factory.getEntity(log.csv[7]);
						if (skilift==null) {
							skilift = factory.createEntity(log.csv[7], log.csv[8]);
						}
						skilift.addOwlClass(factory.getNamespaceForPrefix("ski") + "Skilift");

						EDMActivity usedlift = agent.doDiscreteActivity("activity_" + UUID.randomUUID(), "Used skilift", log.timestamp);
						usedlift.addOwlClass(factory.getNamespaceForPrefix("ski") + "UsingSkiliftActivity");
						usedlift.useEntity(skilift);
					//tweet read
					} else if (log.type.equals("tweet-read")) {
						//logger.info("Tweet: " + log.line);
						agent.startActivity("activity_" + UUID.randomUUID(), "Tweet read", log.timestamp);
					//tweet received
					} else if (log.type.equals("tweet-received")) {
						agent.startActivity("activity_" + UUID.randomUUID(), "Tweet received", log.timestamp);
					//message received
					} else if (log.type.equals("message-received")) {
						agent.startActivity("activity_" + UUID.randomUUID(), "Message received", log.timestamp);
					//message read
					} else if (log.type.equals("message-read")) {
						//logger.info("Message: " + log.line);
						agent.startActivity("activity_" + UUID.randomUUID(), "Message read", log.timestamp);
					//start using SSG
					} else if (log.type.equals("application-startup")) {
						EDMActivity applicationStartup = agent.startActivity("activity_" + UUID.randomUUID(), "Use SSG " + log.device, log.timestamp);
						//logger.info("Started activity " + applicationStartup.getFriendlyName() + " at " + log.date + ", " + log.time);
						applicationUseActivities.add(new ActivityCollection(applicationStartup, log));
					//stop using SSG
					} else if (log.type.equals("application-shutdown")) {
						//look for previously started applicationUseActivities
						Iterator<ActivityCollection> iter = applicationUseActivities.iterator();
						while (iter.hasNext()) {
							ActivityCollection ac = iter.next();
							if (ac.log.device.equals(log.device)) {
								//check timestamps
								int oldTime = new Integer(ac.log.timestamp);
								int newTime = new Integer(log.timestamp);
								if (oldTime<newTime) {
									agent.stopActivity(ac.activity);
									iter.remove();
									//logger.info("Stopped activity " + ac.activity.getFriendlyName() + " at " + log.date + ", " + log.time);
									break;
								}
							}
						}
					} else if (log.type.equals("poi-reached")) {
						//logger.info("Reached POI: "  + log.csv[7] + " (" + log.csv[8] + ")");
						//agent.doDiscreteActivity("activity_" + UUID.randomUUID(),
						//		"Reached POI " + log.csv[7] + " (" + log.csv[8] + ") at " + log.date + ", " + log.time, log.timestamp);
					}
				}
			}
			//logger.info(factory.toString());

			//store it!
			EDMProvPersistenceFactory persistenceFactory;
			EDMProvDataStoreImpl store;
			persistenceFactory = EDMProvPersistenceFactory.getInstance(props);
			store = persistenceFactory.getStore();
			props.setProperty("owlim.repositoryID", "SSG-Test");
			props.setProperty("owlim.repositoryName", "Smart Ski Goggles Experiment Prov Store");
			store.createRepository(props.getProperty("owlim.repositoryID"), props.getProperty("owlim.repositoryName"));

			store.importOntology("experimedia.rdf",
				"http://it-innovation.soton.ac.uk/ontologies/experimedia#", "experimedia", SSGParserTest.class);
			store.importOntology("http://www.w3.org/ns/prov-o#/",
				"http://www.w3.org/ns/prov#", "prov", SSGParserTest.class);
			store.importOntology("http://xmlns.com/foaf/0.1/",
				"http://xmlns.com/foaf/0.1/", "foaf", SSGParserTest.class);
			store.importOntology("http://rdfs.org/sioc/ns#",
				"http://rdfs.org/sioc/ns#", "sioc", SSGParserTest.class);
			store.importOntology("skiing.rdf",
				"http://www.semanticweb.org/sw/ontologies/skiing#", "ski", SSGParserTest.class);

			store.getProvWriter().storeReport(factory.createProvReport());

			//EDMProvBaseElement result = store.getProvElementReader().getElement("http://it-innovation.soton.ac.uk/ontologies/experimedia#agent_EVO-OA-07");
			//logger.info(result.toString());

		} catch (Throwable e) {
			logger.error("Error filling EDMProvFactory with test data", e);
		}
	}

	private class ActivityCollection {
		public EDMActivity activity;
		public Log log;

		ActivityCollection(EDMActivity activity, Log log) {
			this.activity = activity;
			this.log = log;
		}
	}

	private class Log implements Comparable<Log> {

		//2013-12-20 09:22:36,gps,47.40415954589844,14.199807167053223,1644.0
		//2013-12-20 09:22:36,speed,2
		//2013-12-20 09:22:36,temperature,0.0

		public String line;
		public String date;
		public String time;
		public String timestamp;
		public String device;
		public String longitude = null;
		public String latitude = null;
		public String altitude = null;
		public String type = null;

		public String[] csv;

		Log(String filename, String line) throws ParseException {
			this.line = line.trim();

			//log is mostly separated by ;
			csv = line.split(";");

			//date and time are always the first two columns
			if (csv.length>=1) {
				this.date = csv[0].trim();
				//necessary because of non-standard whitespace
				while (this.date.length()>10) {
					this.date = this.date.substring(1);
				}
				//check if date is valid (I really wonder who created these logfiles!)
				if (this.date.length()<10 || !this.date.substring(2,3).equals("-") || !this.date.substring(5,6).equals("-")) {
					throw new ParseException("Invalid date: " + this.date, 0);
				}
			}
			if (csv.length>=2) {
				this.time = csv[1].trim();
				//necessary because of non-standard whitespace
				while (this.time.length()>8) {
					this.time = this.time.substring(1);
				}
			}
			//timestamp
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
				Date fullDate = formatter.parse(this.date + "_" + this.time);
				Long unixtime = fullDate.getTime()/1000L;
				this.timestamp = unixtime.toString();
			} catch (ParseException e) {
				logger.error("Error parsing date to create timestamp", e);
			}

			//filter device name from filename
			this.device = filename;
			if (this.device.endsWith(".txt")) {
				this.device = this.device.substring(0, this.device.length()-4);
			}
			int pos = this.device.indexOf("E");
			if (pos > 0) {
				this.device = this.device.substring(pos);
			}

			//check for long/lat/alt
			if (csv.length>=3) {
				this.latitude = csv[2].trim();
				if (!this.latitude.matches("\\d{1,2}\\.\\d{6}")) {
					this.latitude = null;
				}
			}
			if (csv.length>=4) {
				this.longitude = csv[3].trim();
				if (!this.longitude.matches("\\d{1,2}\\.\\d{6}")) {
					this.longitude = null;
				}
			}
			if (csv.length>=5) {
				this.altitude = csv[4].trim();
				if (!this.altitude.matches("\\d{1,4}")) {
					this.altitude = null;
				}
			}
			//most common types
			if (csv.length==7 && csv[6].equals("gps") || csv.length==6 && csv[5].equals("gps")) {
				type = "gps";
			} else if (csv.length>=7 && csv[6].toLowerCase().startsWith("feature")) {
				type = csv[6].toLowerCase().replace("_", "-").trim();
			} else if (csv.length==6) {
				type = csv[5];
			} else if (csv.length==8) {
				if (csv[6].contains("\"\"")) {
					type = csv[5].toLowerCase().replace("_", "-").trim();
				} else {
					type = csv[6].toLowerCase().replace("_", "-").trim();
				}
			} else if (csv.length==7 && (csv[6].contains("bluetooth") || csv[6].contains("navigation") || csv[6].contains("experiment"))) {
				type = csv[6].toLowerCase().replace("_", "-").trim();

			//various types
			} else {
				//replace _ with - to prevent duplicate log types
				String lowerline = this.line.toLowerCase().replace("_", "-");

				if (lowerline.contains("application-startup")) {
					type = "application-startup";
				} else if (lowerline.contains("application-shutdown")) {
					type = "application-shutdown";
				} else if (lowerline.contains("poi-reached")) {
					type = "poi-reached";
				} else if (lowerline.contains("navigation-started")) {
					type = "navigation-started";
				} else if (lowerline.contains("user-statistic")) {
					type = "user-statistic";
				} else if (lowerline.contains("tweet-read")) {
					type = "tweet-read";
				} else if (lowerline.contains("message-read")) {
					type = "message-read";
				} else if (lowerline.contains("tweet-received")) {
					type = "tweet-received";
				} else if (lowerline.contains("lift-feedback")) {
					type = "lift-feedback";
				} else if (lowerline.contains("used-lift") || lowerline.contains("usedlift")) {
					type = "used-lift";
				} else if (lowerline.contains("exception-trace") || lowerline.contains("exception")) {
					type = "exception";
				} else if (lowerline.contains("individual-start")) {
					type = "individual-start";
				} else if (lowerline.contains("navigation-start")) {
					type = "navigation-start";
				} else if (lowerline.contains("navigation-duration")) {
					type = "navigation-duration";
				} else if (lowerline.contains("navigation-retrieved")) {
					type = "navigation-retrieved";
				} else if (lowerline.contains("weather-start")) {
					type = "weather-start";
				} else if (lowerline.contains("weather-duration")) {
					type = "weather-duration";
				} else if (lowerline.contains("community-start")) {
					type = "community-start";
				} else if (lowerline.contains("community-duration")) {
					type = "community-duration";
				} else if (lowerline.contains("hospitality-start")) {
					type = "hospitality-start";
				} else if (lowerline.contains("hospitality-duration")) {
					type = "hospitality-duration";
				} else if (lowerline.contains("liftinfo-start")) {
					type = "liftinfo-start";
				} else if (lowerline.contains("liftinfo-duration")) {
					type = "liftinfo-duration";
				} else if (lowerline.contains("individual-duration")) {
					type = "individual-duration";
				} else if (lowerline.contains("roundtriptime")) {
					type = "roundtriptime";
				} else if (lowerline.contains("3g-network-level")) {
					type = "3g-network-level";
				} else if (lowerline.contains("notification-start")) {
					type = "notification-start";
				} else if (lowerline.contains("notification-duration")) {
					type = "notification-duration";
				} else if (lowerline.contains("batterylevel")) {
					type = "batterylevel";
				} else if (lowerline.contains("message-received")) {
					type = "message-received";
					line = line.replace("\"\"", "\"");
				} else if (lowerline.contains("run")) {
					type = "run";
				} else if (lowerline.contains("temperature")) {
					type = "temperature";
				} else if (lowerline.contains("speed")) {
					type = "speed";


				} else {
					logger.debug("unknown log type encountered: " + line);
				}
			}

		}

		@Override
		public int compareTo(Log l) {
			//sorting logs based on date and time
			SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
			Date d1;
			Date d2;
			try {
				d1 = format.parse(date.trim());
				d2 = format.parse(l.date.trim());

				//compare date
				if (d1.before(d2)) {
					return -1;
				} else if (d1.after(d2)) {
					return 1;
				} else {
					//now compare time because date is the same
					format = new SimpleDateFormat("HH-mm-ss");
					d1 = format.parse(time);
					d2 = format.parse(l.time);
					if (d1.before(d2)) {
						return -1;
					} else if (d1.after(d2)) {
						return 1;
					} else {
						//date and time are the same, use device
						int devicenum1 = new Integer(device.substring(device.length()-2));
						int devicenum2 = new Integer(l.device.substring(l.device.length()-2));
						if (devicenum1<devicenum2) {
							return -1;
						} else if (devicenum2<devicenum1) {
							return 1;
						} else {
							//date, time and device are the same, use type
							return type.compareTo(l.type);
						}
					}
				}
			} catch (ParseException e) {
				logger.error("Could not parse date/time: " + date + time + l.date + l.time, e);
			}

			//if they are equal return 0
			return 0;
		}
	}
}
